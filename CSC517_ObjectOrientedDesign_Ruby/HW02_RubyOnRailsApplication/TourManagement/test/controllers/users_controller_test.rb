require 'test_helper'

class UsersControllerTest < ActionDispatch::IntegrationTest
  setup do
    @admin = users(:one)
    @user = users(:two)
  end

  test "should get index" do
    get users_url
    assert_response :success
  end

  test "should get new user view" do
    get new_user_url
    assert_response :success
  end

  test "should create user" do
    assert_difference('User.count') do
      post users_url, params: { user: {
        admin: @user.admin,
        agent: @user.agent,
        customer: @user.customer,
        # Change from auto-generated test: user email has uniqueness constraint
        # so cannot just use name from fixture user
        email: "my_unique_email@whatever.com",
        name: @user.name,
        # We have to explicitly pass this and not get from fixture
        # because passwords are encrypted (comes out of fixture as 'nil')
        password: "test_password"
      } }
    end

    assert_redirected_to user_url(User.last)
  end

  test "should show user" do
    get user_url(@user)
    assert_response :success
  end

  test "should get edit" do
    get edit_user_url(@user)
    assert_response :success
  end

  test "should update user" do
    patch user_url(@user), params: { user: {
      admin: @user.admin,
      agent: @user.agent,
      customer: @user.customer,
      name: "John Q. Public"
      # No user allowed to change their email address or password
    } }
    assert_redirected_to user_url(@user)
  end

  test "should destroy user" do
    assert_difference('User.count', -1) do
      delete user_url(@user)
      puts @user.name
    end

    assert_redirected_to users_url
  end
end
