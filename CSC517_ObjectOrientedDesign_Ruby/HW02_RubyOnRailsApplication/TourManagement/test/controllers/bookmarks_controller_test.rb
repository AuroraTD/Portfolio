require 'test_helper'

class BookmarksControllerTest < ActionDispatch::IntegrationTest
  setup do
    @bookmark = bookmarks(:one)
#    @tour = tours(:two)
  end

  test "should get index" do
    get bookmarks_url
    assert_response :success
  end

  # This test has been moved to tours_controller_test.rb
  # test "should get new bookmark view" do
  #   get new_bookmark_url(tour_id: @tour.id)
  #   assert_response :success
  # end

  # Use different bookmark to pass uniqueness criteria
  test "should create unique bookmark" do
    assert_difference('Bookmark.count') do
      post bookmarks_url, params: {bookmark: {
          tour_id: tours(:three).id,
          user_id: users(:three).id
      } }
    end
    assert_redirected_to bookmark_url(Bookmark.last)
  end

  # Use same tour, unique user
  test "should create bookmark with same tour, unique user" do
    assert_difference('Bookmark.count') do
      post bookmarks_url, params: {bookmark: {
          tour_id: tours(:three).id,
          user_id: users(:four).id
      } }
    end
    assert_redirected_to bookmark_url(Bookmark.last)
  end

  # Use same user, unique tour
  test "should create bookmark with same user, unique tour" do
    assert_difference('Bookmark.count') do
      post bookmarks_url, params: {bookmark: {
          tour_id: tours(:one).id,
          user_id: users(:two).id
      } }
    end
    assert_redirected_to bookmark_url(Bookmark.last)
  end

  # Ensure that uniqueness constraint holds
  test "should not create same bookmark" do
    assert_no_difference('Bookmark.count') do
      post bookmarks_url, params: { bookmark: {
          tour_id: @bookmark.tour_id,
          user_id: @bookmark.user_id
      } }
    end
    assert_response :success
  end

  test "should show bookmark" do
    get bookmark_url(@bookmark)
    assert_response :success
  end

  # Edit is not supported for bookmarks
  # The only edit is delete

  test "should update bookmark" do
    patch bookmark_url(@bookmark), params: { bookmark: {
        tour_id: @bookmark.tour_id,
        user_id: @bookmark.user_id
    } }
    assert_redirected_to bookmark_url(@bookmark)
  end

  test "should destroy bookmark" do
    assert_difference('Bookmark.count', -1) do
      delete bookmark_url(@bookmark)
    end

    assert_redirected_to bookmarks_url
  end
end
