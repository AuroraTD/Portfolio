require 'test_helper'

class WaitlistTest < ActiveSupport::TestCase

  setup do
    @waitlist_1 = waitlists(:one)
    @waitlist_2 = waitlists(:two)
  end

  test "test finding corresponding booking" do
    assert_equal @waitlist_1.user_id, @waitlist_1.booking_same_user_same_tour.user_id
    assert_equal @waitlist_1.tour_id, @waitlist_1.booking_same_user_same_tour.tour_id
    assert_nil @waitlist_2.booking_same_user_same_tour
  end

end
