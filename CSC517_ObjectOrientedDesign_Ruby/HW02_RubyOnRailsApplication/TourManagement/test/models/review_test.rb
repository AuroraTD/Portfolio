require 'test_helper'

class ReviewTest < ActiveSupport::TestCase

  setup do
    @review = reviews(:one)
    @tour = tours(:one)
    @user = users(:one)
  end

  test "test getting user name from review" do
    user_name = @review.get_user_name
    assert_equal user_name, @user.name
  end

  test "test getting tour name from review" do
    tour_name = @review.get_tour_name
    assert_equal tour_name, @tour.name
  end

end
