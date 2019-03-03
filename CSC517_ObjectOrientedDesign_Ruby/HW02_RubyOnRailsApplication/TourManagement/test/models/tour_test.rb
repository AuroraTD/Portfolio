require 'test_helper'

class TourTest < ActiveSupport::TestCase

  setup do
    @tour_1 = tours(:one)
    @tour_2 = tours(:two)
    @tour_3 = tours(:three)
  end

  test "test user friendly status description" do
    assert_equal "Completed", @tour_1.status_description
    assert_equal "In Future", @tour_2.status_description
    assert_equal "Cancelled", @tour_3.status_description
  end

  test "test check for past tour" do
    assert @tour_1.has_ended?
    assert_not @tour_2.has_ended?
  end

  test "test check for deadline elapsed tour" do
    assert @tour_1.booking_deadline_has_passed?
    assert_not @tour_2.booking_deadline_has_passed?
  end

  test "test tour itinerary" do
    # First tour has just one location
    assert_equal "North Carolina, US", @tour_1.itinerary
    # Second tour has multiple locations
    # Fixtures do not get sequential IDs by default in Rails
    # Can give fixtures explicit IDs but this causes downstream headaches
    # End result it, we don't know how the visits will be ordered, in the test environment
    # So the best we can do here is to see if the string includes everything that it should
    assert_match "North Carolina, US", @tour_2.itinerary
    assert_match "\n", @tour_2.itinerary
    assert_match "South Carolina, US", @tour_2.itinerary
    # Third tour has no locations
    assert_equal "", @tour_3.itinerary
  end

end
