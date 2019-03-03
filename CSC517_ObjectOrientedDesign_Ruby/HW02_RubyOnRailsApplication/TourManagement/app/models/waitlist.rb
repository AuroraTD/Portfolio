################################################################################
# A waitlist is the number of seats on a particular tour that a user is
# waiting for.
#
# The number of seats must be a non-negative integer.

class Waitlist < ApplicationRecord

  # Relationships
  belongs_to :user
  belongs_to :tour

  # Validations
  validates :num_seats,
            presence: true,
            numericality: { only_integer: true, greater_than_or_equal_to: 0 }

  # Method to get all waitlists for the given tour
  # Return these in order of creation
  def self.get_waitlists_for_tour_first_come_first_served(tour)
    Waitlist.where(tour_id: tour.id).order("created_at")
  end

  # Method to get the number of waitlisted seats for the given tour
  def self.get_waitlisted_seats_for_tour(tour)
    num_waitlisted_seats = 0
    get_waitlists_for_tour_first_come_first_served(tour).each do |waitlist|
      num_waitlisted_seats += waitlist.num_seats
    end
    return num_waitlisted_seats
  end

  # Method to determine if the given user has waitlisted seats on the given tour
  def self.given_user_waitlisted_given_tour?(user, tour)
    get_waitlists_for_tour_first_come_first_served(tour).any? { |waitlist| waitlist.user_id == user.id }
  end

  # Method to get the booking created by this same user on this same tour
  # If no such booking, will return nil and we'll need to respond appropriately later on
  def booking_same_user_same_tour
    return Booking.where(user_id: user_id).find_by(tour_id: tour_id)
  end

  # Method to get the # seats booked by this same user on this same tour
  # If no such booking, will return zero
  def seats_booked_same_user_same_tour
    return booking_same_user_same_tour ? booking_same_user_same_tour.num_seats : 0
  end

  # Method to get the name of the user who made this waitlisting
  def user_name
    return User.find(user_id).name
  end

end
