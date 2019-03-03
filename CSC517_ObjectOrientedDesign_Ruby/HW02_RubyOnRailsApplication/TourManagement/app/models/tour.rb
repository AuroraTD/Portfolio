################################################################################
# A tour is a series of locations that are visited between a start_date and
# end_date. Tours are created by agents/admins and bookmarked by
# admins/customers.
#
# Rubify code.

class Tour < ApplicationRecord

  # Support all of the "through" relationships
  # Destroy dependents if a tour is destroyed to avoid foreign key exceptions
  has_many :reviews, dependent: :destroy
  has_many :photos, dependent: :destroy
  has_many :visits, dependent: :destroy
  has_many :bookmarks, dependent: :destroy
  has_many :bookings, dependent: :destroy
  has_many :waitlists, dependent: :destroy
  has_many :listings, dependent: :destroy
  has_many :start_ats, dependent: :destroy

  # Establish "through" relationships
  has_many :locations, through: :visits, dependent: :destroy
  has_many :users, through: :bookmarks, dependent: :destroy
  has_many :users, through: :bookings, dependent: :destroy
  has_many :users, through: :waitlists, dependent: :destroy
  has_one :user, through: :listings, dependent: :destroy
  has_one :location, through: :start_ats, dependent: :destroy

  # Remove uniqueness validation for name, uniqueness is from the ID
  validates :name, presence: true
  validates :description, presence: true
  validates :price_in_cents, presence: true
  validates :deadline, presence: true
  validates :start_date, presence: true, date: {before_or_equal_to: :end_date}
  validates :end_date, presence: true, date: {after_or_equal_to: :start_date}
  validates :operator_contact, presence: true
  validates :num_seats,
            presence: true,
            numericality: { only_integer: true, greater_than_or_equal_to: 0 }

  # Do NOT validate presence of boolean fields (cancelled)
  # Seems to see false as not-present

  # Support filtering tours
  # https://www.justinweiss.com/articles/search-and-filter-rails-models-without-bloating-your-controller/
  # https://guides.rubyonrails.org/active_record_querying.html#scopes

  # https://www.scimedsolutions.com/articles/74-arel-part-i-case-insensitive-searches-with-partial-matching
  # https://stackoverflow.com/questions/2876789/how-can-i-search-case-insensitive-in-a-column-using-like-wildcard
  scope :tour_name, ->(tour_name) { where("LOWER(name) like LOWER(?)", "%" + tour_name + "%") }

  # https://guides.rubyonrails.org/active_record_querying.html#joining-tables
  scope :desired_location, ->(desired_loc_id) {
    joins "INNER JOIN visits ON visits.tour_id = tours.id AND visits.location_id = #{desired_loc_id}"
  }

  # https://stackoverflow.com/questions/11317662/rails-using-greater-than-less-than-with-a-where-statement/23936233
  scope :max_price_dollars, ->(max_price_dollars) {
    where("price_in_cents <= ?", (max_price_dollars.to_f * 100).to_i)
  }

  # https://stackoverflow.com/questions/4224600/can-you-do-greater-than-comparison-on-a-date-in-a-rails-3-search
  scope :earliest_start, ->(earliest_start) {
    where("start_date >= ?", earliest_start)
  }
  scope :latest_end, ->(latest_end) {
    where("end_date <= ?", latest_end)
  }

  # Using where differently here (take advantage of existing booking model method)
  # https://guides.rubyonrails.org/active_record_querying.html#scopes
  # "All scope methods will return an ActiveRecord::Relation object
  # which will allow for further methods (such as other scopes) to be called on it."
  # So, we should be able to effectively do Tours.select rather than Tours.where
  scope :min_seats, ->(min_seats) {
    select { |tour| Booking.get_available_seats_for_tour(tour) >= min_seats.to_i }
  }

  # Get a collection of tours depending on why the user is visiting the tours index page
  def self.get_tours(params)
    # Support filtering tours according to user desires
    # https://www.justinweiss.com/articles/search-and-filter-rails-models-without-bloating-your-controller/
    if params['listing_user_id']
      tours = Tour.joins(
        "INNER JOIN listings ON tours.id = listings.tour_id AND
        listings.user_id = #{params['listing_user_id'].to_i}"
      )
    else
      tours = Tour.all
    end
    return tours
  end

  # Produce a description of the tour status (to show onscreen)
  def status_description
    return "Cancelled" if cancelled
    return "Completed" if has_ended?
    "In Future"
  end

  # Produce an itinerary for the tour (toshow onscreen)
  def itinerary
    itinerary_array = []
    Visit.get_locations_for_tour(self).each do |location|
      itinerary_array << location.user_friendly_description
    end
    # Join with newlines to conserve horizontal space onscreen
    itinerary_array.join("\n")
  end

  # Method to determine whether the tour has started
  def has_started?
    Date.current >= start_date
  end

  # Method to determine whether the tour has ended
  # If it is not a cancelled tour, this should make the status "Completed"
  def has_ended?
    Date.current > end_date
  end

  # Method to determine whether the tour's booking deadline has passed
  def booking_deadline_has_passed?
    Date.current > deadline
  end

  # Methods to deal with currency
  def price_in_dollars
    price_in_cents/100.0 unless price_in_cents.nil?
  end
  def price_in_dollars=(val)
    self.price_in_cents = (val.to_f * 100).to_int
  end


end
