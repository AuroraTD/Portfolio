class WaitlistsController < ApplicationController
  before_action :set_waitlist, only: [:show, :edit, :update, :destroy]

  # https://stackoverflow.com/questions/1266623/how-do-i-call-a-method-in-application-helper-from-a-view
  include ApplicationHelper

  # You may notice that this controller LACKS some common methods
  # That's because waitlists & bookings are so closely related
  # Often, the user is routed to bookings to get things done
  # This keeps us from having lots of duplicated code

  # GET /waitlists/1
  # GET /waitlists/1.json
  def show
  end

  # POST /waitlists
  # POST /waitlists.json
  def create
    @waitlist = Waitlist.new(waitlist_params)
    respond_to do |format|
      if @waitlist.save
        format.html { redirect_to @waitlist, notice: 'Waitlist was successfully created.' }
        format.json { render :show, status: :created, location: @waitlist }
      else
        format.html { render :new }
        format.json { render json: @waitlist.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /waitlists/1
  # PATCH/PUT /waitlists/1.json
  def update

    # Bookings edit page does double-duty (booking / waitlist)
    @booking, @waitlist = get_booking_and_waitlist_from_params(params)
    update_booking_waitlist(@booking, @waitlist, waitlist_params)

  end

  # DELETE /waitlists/1
  # DELETE /waitlists/1.json
  def destroy

    # Destroy waitlist
    @waitlist.destroy

    # Respond
    success_notice = 'Waitlist was successfully destroyed.'
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
    def set_waitlist
      @waitlist = Waitlist.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def waitlist_params
      params.require(:waitlist).permit(
        :num_seats,
        :user_id,
        :tour_id,
        :strategy,
        :booking_user_id,
        :listing_user_id,
        :waitlist_override
      )
    end

end
