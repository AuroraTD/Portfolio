class BookingsController < ApplicationController
  before_action :set_booking, only: [:show, :edit, :update, :destroy]

  # https://stackoverflow.com/questions/1266623/how-do-i-call-a-method-in-application-helper-from-a-view
  include ApplicationHelper

  # GET /bookings
  # GET /bookings.json
  def index
    @bookings, @lonely_waitlists = Booking.get_bookings_and_waitlists(params)
    set_page_title
  end

  # GET /bookings/1
  # GET /bookings/1.json
  def show
  end

  # GET /bookings/new
  def new

    # Create an empty booking object
    @booking = Booking.new

    # Remember what tour we are working with and make this available to the view
    # This way the view can pass the tour info along in links as needed
    @tour = Tour.find(params['tour_id'])

  end

  # GET /bookings/1/edit
  def edit

    # Edit page does double-duty (booking / waitlist)
    @booking, @waitlist = get_booking_and_waitlist_from_params(params)

    # Remember what tour we are working with and make this available to the view
    # This way the view can pass the tour info along in links / form fields as needed
    # This is to avoid bothering the user to enter the tour
    @tour = @booking ? @booking.tour : @waitlist.tour

  end

  # POST /bookings
  # POST /bookings.json
  def create

    # Get some basic info we use several places below
    tour_id = booking_params[:tour_id].to_i
    num_seats = booking_params[:num_seats].to_i
    num_seats_available = Booking.get_available_seats_for_tour(Tour.find(tour_id))
    strategy = booking_params[:strategy].to_i

    # Support altered params for booking / waitlisting
    # We got strategy in params, but not needed (or wanted) by model instantiation
    params_book = booking_params.dup
    params_book.delete(:strategy)
    params_waitlist = booking_params.dup
    params_waitlist.delete(:strategy)

    # Examine booking / waitlisting strategy and do some error checking to reject silly attempts
    if booking_strategy_okay?(strategy, num_seats, num_seats_available)
      # Create booking / waitlist records
      case strategy
      # 1 - Book All Seats
      when 1
        @booking = Booking.new(params_book)
      # 2 - Book Available Seats, Waitlist Remaining Seats
      when 2
        params_book[:num_seats] = num_seats_available
        params_waitlist[:num_seats] = num_seats - num_seats_available
        @booking = Booking.new(params_book)
        @waitlist = Waitlist.new(params_waitlist)
      # 3 - Waitlist All Seats
      when 3
        @waitlist = Waitlist.new(params_waitlist)
      else
        # type code here
      end
    end

    # Attempt to save booking (if there is one) and waitlist (if there is one)
    if flash[:error].blank? && @booking
      booking_saved = @booking.save
    end
    if flash[:error].blank? && @waitlist
      waitlist_saved = @waitlist.save
    end

    # Redirect based on what happened above
    respond_to do |format|
      if @booking && booking_saved
        format.html { redirect_to @booking, notice: 'Booking was successfully created.' }
        format.json { render :show, status: :created, location: @booking }
      elsif @waitlist && waitlist_saved
        format.html { redirect_to @waitlist, notice: 'Waitlist was successfully created.' }
        format.json { render :show, status: :created, location: @booking }
      else
        # Failed - going back to the new view - make sure to pass along what tour we're working with
        format.html { redirect_to new_booking_path(tour_id: tour_id) }
        format.json { render json: @booking.errors, status: :unprocessable_entity }
      end
    end

  end

  # PATCH/PUT /bookings/1
  # PATCH/PUT /bookings/1.json
  def update

    # Bookings edit page does double-duty (booking / waitlist)
    @booking, @waitlist = get_booking_and_waitlist_from_params(params)
    update_booking_waitlist(@booking, @waitlist, booking_params)

  end

  # DELETE /bookings/1
  # DELETE /bookings/1.json
  def destroy

    # Destroy associated waitlist if it exists
    # Do this first so that the destroy of the booking does not trigger
    # a rollover of waitlist seats to the booking
    if @waitlist
      @waitlist.destroy
    end

    # Destroy booking
    @booking.destroy

    # Respond
    success_notice = 'Booking was successfully destroyed.'
    respond_to do |format|
      if current_user_can_see_all_bookings?
        format.html { redirect_to bookings_url, notice: success_notice }
      elsif current_user_can_see_bookings_for_their_tours?
        format.html { redirect_to bookings_path(listing_user_id: current_user.id), notice: success_notice }
      elsif current_user_can_see_their_bookings?
        format.html { redirect_to bookings_path(booking_user_id: current_user.id), notice: success_notice }
      else
        format.html { redirect_to login_path, notice: success_notice }
      end
      format.json { head :no_content }
    end

  end

  private

    # Use callbacks to share common setup or constraints between actions.
    def set_booking
      @booking, @waitlist = get_booking_and_waitlist_from_params(params)
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def booking_params
      params.require(:booking).permit(
        :num_seats,
        :user_id,
        :tour_id,
        :strategy,
        :booking_user_id,
        :listing_user_id,
        :waitlist_override
      )
    end

    # Produce a helpful title for the page to be used in the view
    def set_page_title
      if params['booking_user_id']
        @page_title = "My Bookings"
      elsif params['listing_user_id']
        @page_title = "Bookings for My Tours"
      else
        @page_title = "All Bookings"
      end
    end

end
