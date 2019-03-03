require 'test_helper'

class LocationTest < ActiveSupport::TestCase

  setup do
    @location = locations(:one)
  end

  test "test user friendly location description" do
    location_description = @location.user_friendly_description
    assert_equal location_description, "North Carolina, US"
  end

end
