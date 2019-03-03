require 'test_helper'

class UserTest < ActiveSupport::TestCase

  setup do
    @user_1 = users(:one)
    @user_2 = users(:two)
    @user_3 = users(:three)
    @user_4 = users(:four)
  end

  test "test user friendly user type description" do
    assert_equal "Admin", @user_1.user_type
    assert_equal "Agent", @user_2.user_type
    assert_equal "Customer", @user_3.user_type
    assert_equal "Agent / Customer", @user_4.user_type
  end

end
