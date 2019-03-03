require 'test_helper'

class ToursControllerTest < ActionDispatch::IntegrationTest
  setup do
    @tour = tours(:one)
  end

  test "should get index" do
    get tours_url
    assert_response :success
  end

  test "should get new" do
    get new_tour_url
    assert_response :success
  end

  test "should create tour" do
    assert_difference('Tour.count') do
      post tours_url, params: { tour: {
        deadline: @tour.deadline,
        description: @tour.description,
        end_date: @tour.end_date,
        # Change from auto-generated test: tour name has uniqueness constraint
        # so cannot just use name from fixture tour
        # Removed uniqueness constraint from model, use tour_id instead
#        name: "MyUniqueName",
        name: @tour.name,
        num_seats: @tour.num_seats,
        operator_contact: @tour.operator_contact,
        price_in_dollars: @tour.price_in_dollars,
        start_date: @tour.start_date,
        cancelled: @tour.cancelled,
        # Tours need at least 1 location id
        # But this is not stored in the tour model
        # So cannot add this to the fixture
        # Minitest::UnexpectedError: ActiveRecord::Fixture::FixtureError: table "tours" has no columns named "location1".
        # Instead, location injected in the test code itself
        location1: 1
      } }
    end

    assert_redirected_to tour_url(Tour.last)
  end

  test "should show tour" do
    get tour_url(@tour)
    assert_response :success
  end

  test "should get edit" do
    get edit_tour_url(@tour)
    assert_response :success
  end

  test "should update tour" do
    patch tour_url(@tour), params: { tour: {
      deadline: @tour.deadline,
      description: @tour.description,
      end_date: @tour.end_date,
      # Change from auto-generated test: tour name has uniqueness constraint
      # so cannot just use name from fixture tour
      # Remove uniqueness constraint from model, use same name in test
#      name: "MyNewUniqueName",
      name: @tour.name,
      num_seats: @tour.num_seats,
      operator_contact: @tour.operator_contact,
      price_in_dollars: @tour.price_in_dollars,
      start_date: @tour.start_date,
      cancelled: @tour.cancelled,
      # Tours need at least 1 location id
      # But this is not stored in the tour model
      # So cannot add this to the fixture
      # Minitest::UnexpectedError: ActiveRecord::Fixture::FixtureError: table "tours" has no columns named "location1".
      # Instead, location injected in the test code itself
      location1: 1
    } }
    assert_redirected_to tour_url(@tour)
  end

  test "should destroy tour" do
    assert_difference('Tour.count', -1) do
      delete tour_url(@tour)
    end

    assert_redirected_to tours_url
  end

  test "should get new bookmark view" do
    get new_bookmark_url(tour_id: @tour.id)
    assert_response :success
  end

end
