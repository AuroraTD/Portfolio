require 'test_helper'

class ListingTest < ActiveSupport::TestCase

  setup do
    @tour = tours(:one)
    @user = users(:one)
  end

  test "test getting agent name for given tour" do
    agent_name = Listing.get_agent_name_for_tour(@tour)
    assert_equal agent_name, @user.name
  end

end
