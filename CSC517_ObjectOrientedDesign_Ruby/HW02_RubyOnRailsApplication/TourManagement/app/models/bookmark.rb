################################################################################
# A bookmark is a set of tours selected (bookmarked) by users.
#
# Rubify code.
# Move logic from index controller to here.

class Bookmark < ApplicationRecord
  belongs_to :user
  belongs_to :tour

  validates :user_id, uniqueness: {scope: :tour_id}
  validates :tour_id, uniqueness: {scope: :user_id}

  def self.find_user_bookmarks(params)
    return self.where(user_id: params['bookmarks_user']) if params['bookmarks_user']
    return self.joins("INNER JOIN listings ON
                    bookmarks.tour_id = listings.tour_id AND
                    listings.user_id = #{params['listing_user'].to_i}") if params['listing_user']
    return self.all
  end
end
