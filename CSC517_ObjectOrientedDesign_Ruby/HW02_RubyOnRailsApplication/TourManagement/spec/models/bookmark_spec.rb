require 'rails_helper'

RSpec.describe Bookmark, type: :model do
  fixtures :bookmarks, :tours, :users

  describe "model" do

    # Don't need to test for a valid factory. If we can't create this model
    # all of these tests will fail.
    # it "should have a valid factory"

    # Lazily loaded to ensure it's only used when it's needed
    # RSpec tip: Try to avoid @instance_variables if possible. They're slow.
    # let(:bookmark) { Bookmark.new }

    # Test that the indices exist. They're used all over the place so let's
    # be sure.
    describe 'indices' do
      it { should have_db_column(:tour_id) }
      it { should have_db_column(:user_id) }
    end

    # Test that a bookmark is associated with a tour and a user.
    describe "associations" do
      it { should belong_to :user }
      it { should belong_to :tour }
    end

    # Test that each user, tour pair is unique.
    describe "constraints" do
      it { should validate_uniqueness_of(:user_id).scoped_to(:tour_id) }
      it { should validate_uniqueness_of(:tour_id).scoped_to(:user_id) }
    end

    # Test that the model returns the correct bookmarks depending on the type
    # of user
    describe "find_user_bookmark method" do
      it "should find all bookmarks for admin" do
        expected_array = [bookmarks(:one), bookmarks(:two), bookmarks(:three)]
        expect(Bookmark.find_user_bookmarks('admin_user'=>users(:one).id))
            .to match_array(expected_array)
      end
      it "should find listing bookmarks for agent" do
        expected_array = [bookmarks(:two), bookmarks(:three)]
        expect(Bookmark.find_user_bookmarks('listing_user'=>users(:two).id))
            .to match_array(expected_array)
      end
      it "should find personal bookmarks for customer" do
        expected_array = [bookmarks(:three)]
        expect(Bookmark.find_user_bookmarks('bookmarks_user'=>users(:three).id))
            .to match_array(expected_array)
      end
    end

  end

end
