class Review < ApplicationRecord

  # Relationships
  belongs_to :user
  belongs_to :tour

  # Validations
  validates :subject, presence: true
  validates :content, presence: true

  # Get a collection of reviews depending on why the user is visiting the reviews index page
  def self.get_reviews(params)
    if params['reviewing_user_id']
      reviews = Review.where(user_id: params['reviewing_user_id'].to_i)
    elsif params['listing_user_id']
      reviews = Review.joins("INNER JOIN listings ON
                    reviews.tour_id = listings.tour_id AND
                    listings.user_id = #{params['listing_user_id'].to_i}")
    else
      reviews = Review.all
    end
    return reviews
  end

  # Get user name from review
  def get_user_name
    User.find(user_id).read_attribute("name")
  end

  # Get tour name from review
  def get_tour_name
    Tour.find(tour_id).read_attribute("name")
  end

end
