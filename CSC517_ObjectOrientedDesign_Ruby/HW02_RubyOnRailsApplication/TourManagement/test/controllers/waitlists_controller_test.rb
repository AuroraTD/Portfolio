require 'test_helper'

class WaitlistsControllerTest < ActionDispatch::IntegrationTest

  # You may notice that this controller LACKS some common tests
  # That's because waitlists & bookings are so closely related
  # Often, the user is routed to bookings to get things done
  # This keeps us from having lots of duplicated code

  setup do
    @waitlist = waitlists(:one)
  end

  test "should create waitlist" do
    assert_difference('Waitlist.count') do
      post waitlists_url, params: { waitlist: {
        num_seats: @waitlist.num_seats,
        tour_id: @waitlist.tour_id,
        user_id: @waitlist.user_id
      } }
    end
    assert_redirected_to waitlist_url(Waitlist.last)
  end

  test "should show waitlist" do
    get waitlist_url(@waitlist)
    assert_response :success
  end

  test "should update waitlist" do
    patch waitlist_url(@waitlist), params: { waitlist: {
      num_seats: @waitlist.num_seats,
      tour_id: @waitlist.tour_id,
      user_id: @waitlist.user_id
    } }
    # We use a regex to describe where we expect to be redirected to
    # to account for extra parameters passed during redirect
    # (lazy approach - just be super flexible about where we redirect to)
    # https://api.rubyonrails.org/v5.2.2/classes/ActionDispatch/Assertions/ResponseAssertions.html
    # Also, we redirect to bookings as bookings effectively handles both bookings and waitlists
    assert_redirected_to /http.*bookings.*/
  end

  test "should destroy waitlist" do
    assert_difference('Waitlist.count', -1) do
      delete waitlist_url(@waitlist)
    end
    # We use a regex to describe where we expect to be redirected to
    # to account for extra parameters passed during redirect
    # (lazy approach - just be super flexible about where we redirect to)
    # https://api.rubyonrails.org/v5.2.2/classes/ActionDispatch/Assertions/ResponseAssertions.html
    # Also, we redirect to bookings as bookings effectively handles both bookings and waitlists
    assert_redirected_to /http.*bookings.*/
  end
end
