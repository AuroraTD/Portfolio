################################################################################
# This file test the location controller. The initial version is auto-generated.
#
# Fix should_create_location to handle uniqueness requirement.
# Add 3 tests to check uniqueness requirement.

require 'test_helper'

class LocationsControllerTest < ActionDispatch::IntegrationTest
  setup do
    @location = locations(:one)
  end

  test "should get index" do
    get locations_url
    assert_response :success
  end

  test "should get new" do
    get new_location_url
    assert_response :success
  end

  # Use different location to pass uniqueness criteria
  test "should create unique location" do
    assert_difference('Location.count') do
      post locations_url, params: {location: {
          country: "Unique Country",
          state: "Unique State"
      } }
    end

    assert_redirected_to location_url(Location.last)
  end

  # Use same country, unique state
  test "should create location with same country, unique state" do
    assert_difference('Location.count') do
      post locations_url, params: {location: {
          country: "Unique Country",
          state: "Unique State1"
      } }
    end

    assert_redirected_to location_url(Location.last)
  end

  # Use same state, unique country
  test "should create location with same state, unique country" do
    assert_difference('Location.count') do
      post locations_url, params: {location: {
          country: "Unique Country1",
          state: "Unique State"
      } }
    end

    assert_redirected_to location_url(Location.last)
  end

  # Ensure that uniqueness constraint holds
  test "should not create same location" do
    assert_no_difference('Location.count') do
      post locations_url, params: {location: {country: @location.country,
                                              state: @location.state}}
    end
    assert_response :success
  end

  test "should show location" do
    get location_url(@location)
    assert_response :success
  end

  test "should get edit" do
    get edit_location_url(@location)
    assert_response :success
  end

  test "should update location" do
    patch location_url(@location), params: { location: { country: @location.country, state: @location.state } }
    assert_redirected_to location_url(@location)
  end

  test "should destroy location" do
    assert_difference('Location.count', -1) do
      delete location_url(@location)
    end

    assert_redirected_to locations_url
  end
end
