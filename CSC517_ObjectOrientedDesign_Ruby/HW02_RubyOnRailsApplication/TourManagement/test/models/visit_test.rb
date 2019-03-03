require 'test_helper'

class VisitTest < ActiveSupport::TestCase

  setup do
    @tour = tours(:one)
    @location = locations(:one)
  end

  test "test getting location ids for given tour" do
    tour_location_ids = Visit.get_location_ids_for_tour(@tour)
    assert_equal tour_location_ids.length, 1
    assert_equal Location.find(tour_location_ids[0]).state, @location.state
    assert_equal Location.find(tour_location_ids[0]).country, @location.country
  end

  test "test getting ith location id for given tour" do
    ith_location_id = Visit.get_ith_location_id_for_tour(@tour, 0)
    assert_operator ith_location_id, :>, 0
    assert_equal Location.find(ith_location_id).state, @location.state
    assert_equal Location.find(ith_location_id).country, @location.country
    ith_location_id = Visit.get_ith_location_id_for_tour(@tour, 1)
    assert_equal ith_location_id, -1
  end

end
