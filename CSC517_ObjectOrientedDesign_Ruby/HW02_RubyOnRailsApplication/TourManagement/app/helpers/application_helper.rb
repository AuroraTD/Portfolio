module ApplicationHelper

  # Methods to help bookings & waitlists work together

  # Bookings edit page does double-duty (booking / waitlist)
  # So make sure you know which one you're dealing with
  # Waitlist override will be true if we are editing a "lonely" waitlist
  # Waitlist override will be false if we are editing a booking
  #   (which may or may not have an associated booking)
  def get_booking_and_waitlist_from_params(parameters)

    if parameters['waitlist_override'] || (parameters['waitlist'] && parameters['waitlist']['waitlist_override'])
      # Waitlist is whatever ID was passed in
      waitlist = Waitlist.find(parameters['id'].to_i)
      # No associated booking
      booking = nil
    else
      # Booking is whatever ID was passed in
      booking = Booking.find(parameters['id'].to_i)
      # There may also be an associated waitlist
      waitlist = booking.waitlist_same_user_same_tour
    end

    return booking, waitlist

  end

  # Examine booking / waitlisting strategy and do some error checking to reject silly attempts
  # 1 - Book All Seats
  # 2 - Book Available Seats, Waitlist Remaining Seats
  # 3 - Waitlist All Seats
  # http://ruby-doc.com/docs/ProgrammingRuby/html/tut_expressions.html#S5
  # https://stackoverflow.com/questions/8252783/passing-error-messages-through-flash
  def booking_strategy_okay?(strategy, num_seats_requested, num_seats_available)

    strategy_okay = false
    case strategy
      # 1 - Book All Seats
    when 1
      if num_seats_requested < 1
        flash[:error] = "Cannot book #{num_seats_requested} seats"
      elsif num_seats_requested > num_seats_available
        flash[:error] = "Cannot book #{num_seats_requested} seats (only #{num_seats_available} seats available)"
      else
        strategy_okay = true
      end
      # 2 - Book Available Seats, Waitlist Remaining Seats
    when 2
      if num_seats_requested < 1
        flash[:error] = "Cannot book #{num_seats_requested} seats"
      elsif num_seats_requested <= num_seats_available
        flash[:error] = "No need to waitlist #{num_seats_requested} seats (there are #{num_seats_available} seats available)"
      else
        strategy_okay = true
      end
      # 3 - Waitlist All Seats
    when 3
      if num_seats_requested < 1
        flash[:error] = "Cannot waitlist #{num_seats_requested} seats"
      elsif num_seats_requested <= num_seats_available
        flash[:error] = "No need to waitlist #{num_seats_requested} seats (there are #{num_seats_available} seats available)"
      else
        strategy_okay = true
      end
    else
      flash[:error] = "Did not recognize book / waitlist strategy # #{strategy}"
    end

    return strategy_okay

  end

  # A method to call from bookings#update or from waitlists#update
  # This keeps all the smarts in one place
  def update_booking_waitlist(booking, waitlist, record_parameters)

    # Get some basic info we use several places below
    tour_id = record_parameters[:tour_id].to_i
    num_seats_already_booked = booking ? booking.num_seats : 0
    num_seats_requested = record_parameters[:num_seats].to_i
    num_seats_available = Booking.get_available_seats_for_tour(Tour.find(tour_id)) + num_seats_already_booked
    booking_strategy = record_parameters[:strategy].to_i

    # Support altered params for booking / waitlisting
    # We have strategy in params, but not needed (or wanted) by model instantiation
    # We may have waitlist_override in params, but not needed (or wanted) by model instantiation
    params_book = record_parameters.dup
    params_book.delete(:strategy)
    params_book.delete(:waitlist_override)
    params_waitlist = record_parameters.dup
    params_waitlist.delete(:strategy)
    params_waitlist.delete(:waitlist_override)

    # Examine booking / waitlisting strategy and do some error checking to reject silly attempts
    if booking_strategy_okay?(booking_strategy, num_seats_requested, num_seats_available)

      # Create booking / waitlist records
      case booking_strategy
        # 1 - Book All Seats
      when 1
        # We can book all of these seats
        # We need a booking
        #   If there was a booking already, update it
        #   If there was not a booking already, create it
        # We do not need a waitlist
        #   If there was a waitlist already, destroy it
        if booking
          booking.update(params_book)
        else
          booking = Booking.new(params_book)
        end
        if waitlist
          # Destroy the record
          waitlist.destroy!
          # Make sure the variable is nil so you don't try to use it later
          waitlist = nil
        end
        # 2 - Book Available Seats, Waitlist Remaining Seats
      when 2
        # We can do a split booking / waitlisting
        # We need a booking
        #   If there was a booking already, update it
        #   If there was not a booking already, create it
        # We need a waitlist
        #   If there was a waitlist already, update it
        #   If there was not a waitlist already, create it
        params_book[:num_seats] = num_seats_available
        params_waitlist[:num_seats] = num_seats_requested - num_seats_available
        if booking
          booking.update(params_book)
        else
          booking = Booking.new(params_book)
        end
        if waitlist
          waitlist.update(params_waitlist)
        else
          waitlist = Waitlist.new(params_waitlist)
        end
        # 3 - Waitlist All Seats
      when 3
        # We can waitlist all of these seats
        # We do not need a booking
        #   If there was a booking already, destroy it
        # We need a waitlist
        #   If there was a waitlist already, update it
        #   If there was not a waitlist already, create it
        if booking
          # Destroy the record
          booking.destroy!
          # Make sure the variable is nil so you don't try to use it later
          booking = nil
        end
        if waitlist
          waitlist.update(params_waitlist)
        else
          waitlist = Waitlist.new(params_waitlist)
        end
      else
        nil
      end
    end

    # Attempt to save booking (if there is one) and waitlist (if there is one)
    if flash[:error].blank? && booking
      booking_saved = booking.save
    end
    if flash[:error].blank? && waitlist
      waitlist_saved = waitlist.save
    end

    # Redirect based on what happened above
    respond_to do |format|
      if booking && booking_saved
        format.html { redirect_to booking, notice: 'Booking was successfully updated.' }
        format.json { render :show, status: :created, location: booking }
      elsif waitlist && waitlist_saved
        format.html { redirect_to waitlist, notice: 'Waitlist was successfully updated.' }
        format.json { render :show, status: :created, location: booking }
      else if booking
        format.html { redirect_to edit_booking_path(strategy: booking_strategy)}
        format.json { render json: booking.errors, status: :unprocessable_entity }
      else
        format.html { redirect_to edit_booking_path(strategy: booking_strategy, waitlist_override: true) }
        format.json { render json: booking.errors, status: :unprocessable_entity }
      end
    end

    end

  end

end
