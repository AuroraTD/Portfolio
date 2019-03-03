require 'test_helper'

class BookingsControllerTest < ActionDispatch::IntegrationTest
  setup do

    @booking = bookings(:one)
    @tour = tours(:one)
    @user = users(:one)

  end

  test "should get index" do
    get bookings_url
    assert_response :success
  end

  test "should get new" do
    # The bookings new view expects to be passed the tour we're currently working with,
    #   so that the user doesn't have to select a tour
    get new_booking_url(tour_id: @tour.id)
    assert_response :success
  end

  test "should create booking" do
    assert_difference('Booking.count') do
      post bookings_url, params: { booking: {
        num_seats: @booking.num_seats,
        tour_id: @booking.tour_id,
        user_id: @booking.user_id,
        # Add a booking strategy (1 - Book All Seats)
        strategy: 1
      } }
    end

    assert_redirected_to booking_url(Booking.last)
  end

  test "should show booking" do
    get booking_url(@booking)
    assert_response :success
  end

  test "should get edit" do
    get edit_booking_url(@booking)
    assert_response :success
  end

  test "should update booking" do
    patch booking_url(@booking), params: { booking: {
      num_seats: @booking.num_seats,
      tour_id: @booking.tour_id,
      user_id: @booking.user_id
    } }
    # We use a regex to describe where we expect to be redirected to
    # to account for extra parameters passed during redirect
    # (lazy approach - just be super flexible about where we redirect to)
    # https://api.rubyonrails.org/v5.2.2/classes/ActionDispatch/Assertions/ResponseAssertions.html
    assert_redirected_to /http.*bookings.*/
  end

  test "should destroy booking" do
    assert_difference('Booking.count', -1) do
      delete booking_url(@booking)
    end
    assert_redirected_to bookings_url
  end
end
