require 'test_helper'

class BookingTest < ActiveSupport::TestCase

  setup do
    @tour_1 = tours(:one)
    @tour_2 = tours(:two)
    @booking_1 = bookings(:one)
    @booking_2 = bookings(:two)
  end

  test "test number of available seats" do
    assert_equal 9, Booking.get_available_seats_for_tour(@tour_1)
    assert_equal 18, Booking.get_available_seats_for_tour(@tour_2)
  end

  test "test finding corresponding waitlist" do
    assert_equal @booking_1.user_id, @booking_1.waitlist_same_user_same_tour.user_id
    assert_equal @booking_1.tour_id, @booking_1.waitlist_same_user_same_tour.tour_id
    assert_nil @booking_2.waitlist_same_user_same_tour
  end

end
