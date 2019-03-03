class ToursController < ApplicationController
  before_action :set_tour, only: [:show, :edit, :update, :destroy]

  # GET /tours
  # GET /tours.json
  def index

    # Get tours
    @tours = Tour.get_tours(params)

    # Support filtering tours according to user desires
    # https://www.justinweiss.com/articles/search-and-filter-rails-models-without-bloating-your-controller/
    flash[:filters] = {}
    filtering_params(params).each do |key, value|
      # Filter the tours by this criteria IF a "real" value was provided for the filter
      if value.length.positive? && (!is_number?(value) || value.to_f.positive?)
        @tours = @tours.public_send(key, value) if value.present?
      end
      # Persist this filter information for one request
      # so that we can still show the user what they filtered by
      flash[:filters][key] = value
    end

    # Set page title
    set_page_title


  end

  # GET /tours/1
  # GET /tours/1.json
  def show

    # Get all reviews associated with this tour so that the View may show them
    @reviews = Review.where(tour_id: @tour.id)

    # Get all photos associated with this tour so that the View may show them
    @photos = Photo.where(tour_id: @tour.id)

    # Get all locations associated with this tour so that the View may show them
    @locations = Visit.where(tour_id: @tour.id).map do |matching_visit|
      Location.find(matching_visit.location_id)
    end

    # Get all guests associated with this tour so that the View may show them
    # Guests are those that have booked (not just waitlisted) the tour
    @guests = Booking.where(tour_id: @tour.id).map do |matching_booking|
      User.find(matching_booking.user_id)
    end.uniq

  end

  # GET /tours/new
  def new
    @tour = Tour.new
  end

  # GET /tours/1/edit
  def edit
  end

  # POST /tours
  # POST /tours.json
  def create

    # Create the tour (not yet saved to DB)
    @tour = Tour.new(params_without_locations)

    # Handle the creation in a transaction
    # so that any problems can be rolled back and it will be like the creation never happened
    # http://vaidehijoshi.github.io/blog/2015/08/18/safer-sql-using-activerecord-transactions/
    # https://stackoverflow.com/questions/51959746/how-to-know-why-a-transaction-was-rolled-back
    transaction_success = false
    Tour.transaction do

      # Attempt save
      @tour.save(params_without_locations)

      # Attempt to create a listing relationship between the new tour and the agent
      # The assumption here is that the current user is an agent
      # If not, they should not have been allowed to create a tour
      # Do not complain if we have a nil current user
      # This could happen during test (when nobody is logged in)
      # Do this after the tour is saved (otherwise the listing is no good)
      if current_user
        new_listing = Listing.new(tour_id: @tour.id, user_id: current_user.id)
        new_listing.save
      end

      # Attempt to create relationships between the tour and its locations
      link_to_locations

      # Everything still okay?
      unless @tour.errors.empty?
        raise ActiveRecord::Rollback
      end

      # Need ruby 2.5 or greater to rescue error inside do block
      # so we have a bit of extra code to tell, outside of the transaction, if it was rolled back
      # We will only get here if an exception was NOT raised
      transaction_success = true

    end

    # React based on whether or not the transaction succeeded
    if transaction_success
      # This code only runs if the transaction succeeded
      respond_to do |format|
        format.html { redirect_to @tour, notice: 'Tour was successfully created.' }
        format.json { render :show, status: :created, location: @tour }
      end
    else
      # This code only runs if the transaction was rolled back
      respond_to do |format|
        format.html { render :new }
        format.json { render json: @tour.errors, status: :unprocessable_entity }
      end
    end

  end

  # PATCH/PUT /tours/1
  # PATCH/PUT /tours/1.json
  def update

    # Handle the update in a transaction
    # so that any problems can be rolled back and it will be like the update never happened
    # http://vaidehijoshi.github.io/blog/2015/08/18/safer-sql-using-activerecord-transactions/
    # https://stackoverflow.com/questions/51959746/how-to-know-why-a-transaction-was-rolled-back
    transaction_success = false
    Tour.transaction do

      # Attempt all of the actions that belong together in a transaction
      @tour.update(params_without_locations)
      link_to_locations
      unless @tour.errors.empty?
        raise ActiveRecord::Rollback
      end

      # Need ruby 2.5 or greater to rescue error inside do block
      # so we have a bit of extra code to tell, outside of the transaction, if it was rolled back
      # We will only get here if an exception was NOT raised
      transaction_success = true

    end

    # React based on whether or not the transaction succeeded
    if transaction_success
      # This code only runs if the transaction succeeded
      respond_to do |format|
        format.html { redirect_to @tour, notice: 'Tour was successfully updated.' }
        format.json { render :show, status: :ok, location: @tour }
      end
    else
      # This code only runs if the transaction was rolled back
      respond_to do |format|
        format.html { render :edit }
        format.json { render json: @tour.errors, status: :unprocessable_entity }
      end
    end

  end

  # DELETE /tours/1
  # DELETE /tours/1.json
  def destroy

    # Destroy tour
    @tour.destroy

    # Respond
    success_notice = 'Tour was successfully destroyed.'
    respond_to do |format|
      # Current logic when this code was written is that everyone can see all reviews
      if current_user_can_see_all_tours?
        format.html { redirect_to tours_url, notice: success_notice }
      else
        format.html { redirect_to login_path, notice: success_notice }
      end
      format.json { head :no_content }
    end

  end

  private

    # Use callbacks to share common setup or constraints between actions.
    def set_tour
      @tour = Tour.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def tour_params
      params.require(:tour).permit(
        # Support searching
        # Permit things that will come through in a search post
        # Implemented per...
        # https://www.justinweiss.com/articles/search-and-filter-rails-models-without-bloating-your-controller/
        :tour,
        :utf8,
        :authenticity_token,
        :commit,
        # Permit the normal stuff
        :name,
        :description,
        :price_in_dollars,
        :deadline,
        :start_date,
        :end_date,
        :operator_contact,
        :cancelled,
        :num_seats,
        # Also permit up to 10 locations in the itinerary
        # Any un-selected locations will still come through
        # (just with a special value that we can use to ignore them later)
        :location1,
        :location2,
        :location3,
        :location4,
        :location5,
        :location6,
        :location7,
        :location8,
        :location9,
        :location10,
        # Also permit filtering
        :desired_location
      )
    end

    # Get a copy of the tour parameters that does NOT include the locations
    # Locations in the itinerary are not stored in the tour itself
    # But rather in explicit relationships
    # This way, if we change the number of possible locations in the tour
    # we do not need to change anything about our models
    def params_without_locations
      return tour_params.except(
        # Locations in the itinerary are not stored in the tour itself
        # But rather in explicit relationships
        # This way, if we change the number of possible locations in the tour
        # we do not need to change anything about our models
        :location1,
        :location2,
        :location3,
        :location4,
        :location5,
        :location6,
        :location7,
        :location8,
        :location9,
        :location10
      )
    end

    # Create / Update relationship between a tour and the locations that it visits
    # The view presents 10 slots for the itinerary
    # Anything the user didn't select will default to a location ID of -1
    def link_to_locations

      # Remove any existing links
      # (this method is used in both create and update)
      Visit.where(tour_id: @tour.id).each(&:destroy)

      # Create new links
      got_start_location = false
      (1..10).each do |i|

        # Grab a location id from the parameters
        # Be forgiving
        # No such key?  Okay, set to -1 (flag for no location in this itinerary slot)
        selected_location_id =
          tour_params["location" + i.to_s] ?
          tour_params["location" + i.to_s].to_i :
          -1

        # Create visits relationship (or skip to next iteration)
        next unless selected_location_id.positive?
        new_visits_rel = Visit.new(
          tour_id: @tour.id,
          location_id: selected_location_id
        )
        new_visits_rel.save

        # Create starts at relationship (or skip to next iteration)
        next if got_start_location
        new_start_at_rel = StartAt.new(
          tour_id: @tour.id,
          location_id: selected_location_id
        )
        new_start_at_rel.save
        got_start_location = true

      end

      # Gotta have at least one location in the itinerary
      # https://stackoverflow.com/questions/5320934/how-to-add-custom-errors-to-the-user-errors-collection
      unless got_start_location
        @tour.errors[:base] << "A tour must have at least one location"
      end

    end

    # A list of the param names that can be used for filtering the Product list
    # https://www.justinweiss.com/articles/search-and-filter-rails-models-without-bloating-your-controller/
    def filtering_params(params)
      params.slice(:desired_location, :max_price_dollars, :tour_name, :earliest_start, :latest_end, :min_seats)
    end

    # Method to determine if a string represents a number
    # https://stackoverflow.com/questions/5661466/test-if-string-is-a-number-in-ruby-on-rails
    def is_number?(string)
      true if Float(string) rescue false
    end

    # Produce a helpful title for the page to be used in the view
    def set_page_title
      if params['listing_user_id']
        @page_title = "My Tours"
      else
        @page_title = "All Tours"
      end
    end

end
