require 'test_helper'

class StartAtsControllerTest < ActionDispatch::IntegrationTest
  setup do
    @start_at = start_ats(:one)
  end

  test "should get index" do
    get start_ats_url
    assert_response :success
  end

  test "should get new" do
    get new_start_at_url
    assert_response :success
  end

  test "should create start" do
    assert_difference('StartAt.count') do
      post start_ats_url, params: { start_at: {
        location_id: @start_at.location_id,
        tour_id: @start_at.tour_id
      } }
    end

    assert_redirected_to start_at_url(StartAt.last)
  end

  test "should show start" do
    get start_at_url(@start_at)
    assert_response :success
  end

  test "should get edit" do
    get edit_start_at_url(@start_at)
    assert_response :success
  end

  test "should update start" do
    patch start_at_url(@start_at), params: { start_at: { location_id: @start_at
                                                                  .location_id, tour_id: @start_at.tour_id } }
    assert_redirected_to start_at_url(@start_at)
  end

  test "should destroy start" do
    assert_difference('StartAt.count', -1) do
      delete start_at_url(@start_at)
    end

    assert_redirected_to start_ats_url
  end
end
