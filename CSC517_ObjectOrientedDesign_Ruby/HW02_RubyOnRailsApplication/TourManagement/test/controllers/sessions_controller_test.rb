require 'test_helper'

class SessionsControllerTest < ActionDispatch::IntegrationTest
  test "should get new" do
    # Updated "get" per https://www.railstutorial.org/book/basic_login
    get login_path
    assert_response :success
  end

end
