require 'simplecov'
SimpleCov.start 'rails' do
  add_filter '/bin/'
  add_filter '/db/'
  add_filter '/spec/' # for rspec
  add_filter '/test/' # for minitest
end

ENV['RAILS_ENV'] ||= 'test'
require_relative '../config/environment'
require 'rails/test_help'

class ActiveSupport::TestCase
  # Setup all fixtures in test/fixtures/*.yml for all tests in alphabetical order.
  fixtures :all

  setup do
    # Log in a user (gotta have a logged-in user to do all sorts of test stuff successfully)
    # But cannot use session helper from outside of the controller
    # https://stackoverflow.com/questions/39465941/how-to-use-session-in-the-test-controller-in-rails-5?rq=1
    # So, this is kind of a hack to fake a logged-in user
    # Mimics params sent when logging in manually on the development application
    if self.respond_to? :post
      post '/login', params: { session: { email: "john@john.com", password: "john_password" } }
    end
  end

  rescue Exception # ignore

  # Add more helper methods to be used by all tests here...
end


