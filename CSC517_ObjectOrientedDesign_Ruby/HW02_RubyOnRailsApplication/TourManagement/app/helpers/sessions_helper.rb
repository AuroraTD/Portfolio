################################################################################
# This module contains a number of helper functions used by the views.
#
# Remove unnecessary return statements.

module SessionsHelper

  # Some content from https://www.railstutorial.org/book/basic_login
  # Then added more methods as needed

  #######################################################################
  # BASIC STUFF
  #######################################################################

  # Logs in the given user.
  def log_in(user)
    session[:user_id] = user.id
  end

  # Returns the current logged-in user (if any).
  def current_user
    if session[:user_id]
      @current_user ||= User.find_by(id: session[:user_id])
    end
  end

  # Returns true if the user is logged in, false otherwise.
  def logged_in?
    !current_user.nil?
  end

  # Logs out the current user.
  def log_out
    session.delete(:user_id)
    @current_user = nil
  end

  # Method to determine if the given user should be logged in
  # If we are NOT in test mode, the user should be logged in if they exist and they authenticate
  # If we ARE in test mode, it's messier.
  # During testing, we need to have a logged in user for some things (e.g. creating a new review)
  # But cannot use session helper from outside of the controller
  # https://stackoverflow.com/questions/39465941/how-to-use-session-in-the-test-controller-in-rails-5?rq=1
  # So, we post to the log in URL with a user's parameters so that we can get to the session controller
  # Once there, we have to decide whether or not to log in the user
  # That brings us to this method: if we are in test mode, just say, yeah, sure, log in
  # The reason we have this dumb approach is that the password is secure
  #   so there's really no good way to pass it around within the application
  def log_in_user_with_password?(user, password)
    return true if Rails.env == "test"
    user && user.authenticate(password)
  end

  #######################################################################
  # USER ROLES
  #######################################################################

  # Method to determine if the current user is an admin
  def current_user_admin?
    current_user && current_user.read_attribute("admin")
  end

  # Method to determine if the current user is an agent
  def current_user_agent?
    current_user && current_user.read_attribute("agent")
  end

  # Method to determine if the current user is a customer
  def current_user_customer?
    current_user && current_user.read_attribute("customer")
  end

  #######################################################################
  # BOOKMARK PERMISSIONS
  #######################################################################

  # Method to determine if the current user can see a list of all bookmarks
  def current_user_can_see_all_bookmarks?
    current_user_admin?
  end

  # Method to determine if the current user can see their bookmarks
  # Customers can do this
  # Admins also can (because admins can do pretty much anything)
  def current_user_can_see_their_bookmarks?
    current_user_admin? || current_user_customer?
  end

  # Method to determine if the current user can see bookmarks for tours which they have created
  # Agents can do this
  # Admins also can (because admins can do pretty much anything)
  def current_user_can_see_bookmarks_for_their_tours?
    current_user_admin? || current_user_agent?
  end

  # Method to determine if the current user can bookmarks a tour
  # Customers can do this
  # Admins also can (because admins can do pretty much anything)
  def current_user_can_bookmark_tours?
    current_user_admin? || current_user_customer?
  end

  # Method to determine if the current user is allowed to bookmark the given tour
  # Doesn't make sense to bookmark a tour that has already ended
  def current_user_can_bookmark_given_tour?(tour)
    current_user_can_bookmark_tours? && !tour.has_ended?
  end

  # Method to determine if the current user is allowed to modify the given bookmark
  # Admin can do this because they can do darn near anything
  # The user who created the bookmark can do this
  def current_user_can_modify_given_bookmark?(bookmark)
    current_user_admin? || bookmark.user_id == current_user.read_attribute("id")
  end

  #######################################################################
  # REVIEW PERMISSIONS
  #######################################################################

  # Method to determine if the current user is allowed to look at reviews
  # Even a casual visitor with no account should be allowed to do this
  def current_user_can_see_all_reviews?
    true
  end

  # Method to determine if the current user is allowed to look at reviews that they have created
  # Customers need this ability
  # Admins get it because they are special and can act as customers
  def current_user_can_see_their_reviews?
    current_user_admin? || current_user_customer?
  end

  # Method to determine if the current user is allowed to look at reviews of tours they have listed
  # Agents need this ability
  # Admins get it because they are special and can act as agents
  def current_user_can_see_reviews_for_their_tours?
    current_user_admin? || current_user_agent?
  end

  # Method to determine if the current user is allowed to create a review
  # Admins and customers can do this
  def current_user_can_create_reviews?
    current_user_admin? || current_user_customer?
  end

  # Method to determine if the given review was created by the currently logged in user
  def current_user_created_given_review?(review)
    review.user_id == current_user.read_attribute("id")
  end

  # Method to determine if the current user is allowed to edit / delete / cancel the given review
  # A review can always be modified by an admin
  # A review can be modified by a customer who has created the review
  def current_user_can_modify_given_review?(review)
    current_user_admin? ||
    (current_user_customer? && current_user_created_given_review?(review))
  end

  # Method to return a collection of the tours taken by the current user
  # Per Piazza,
  # "can a customer only post reviews for tours that are completed and that they were booked on?... Yes."
  def tours_taken_by_current_user
    Booking.where(user_id: current_user.id).map { |booking| Tour.find(booking.tour.id) }.select(&:has_ended?)
  end

  # Method to return a collection of the tours taken by the given user
  # Per Piazza,
  # "can a customer only post reviews for tours that are completed and that they were booked on?... Yes."
  def tours_taken_by_given_user_id(user_id)
    Booking.where(user_id: user_id).map { |booking| Tour.find(booking.tour.id) }.select(&:has_ended?)
  end

  #######################################################################
  # TOUR PERMISSIONS
  #######################################################################

  # Method to determine if the current user is allowed to look at tours
  # Even a casual visitor with no account should be allowed to do this
  def current_user_can_see_all_tours?
    true
  end

  # Method to determine if the current user is allowed to look at tours that they have created
  # Agents need this ability
  # Admins get it because they are special and can also act as agents
  def current_user_can_see_their_tours?
    current_user_admin? || current_user_agent?
  end

  # Method to determine if the current user is allowed to create a tour
  def current_user_can_create_tour?
    current_user_admin? || current_user_agent?
  end

  # Method to determine if the given tour was listed by the currently logged in user
  def current_user_listed_given_tour?(tour)
    matching_user_id = Listing.get_agent_id_for_tour(tour)
    matching_user_id && current_user && matching_user_id == current_user.read_attribute("id")
  end

  # Method to determine if the current user is allowed to edit / delete / cancel the given tour
  # Nobody can modify a tour that is completed
  # A tour in the future can always be modified by an admin
  # A tour in the future can be modified by an agent who has created the tour
  def current_user_can_modify_given_tour?(tour)
    !tour.has_ended? &&
    (
      current_user_admin? ||
      (current_user_agent? && current_user_listed_given_tour?(tour))
    )
  end

  # Method to determine if the current user is allowed to book tours
  def current_user_can_book_tours?
    current_user_admin? || current_user_customer?
  end

  # Method to determine if the current user is allowed to book the given tour
  # Nobody can book a tour that is completed
  # A tour in the future can be booked if:
  #   the current user is allowed to book tours
  #   the tour's booking deadline has not elapsed
  #   the current user doesn't already have a booking / waitlist for the tour
  def current_user_can_book_given_tour?(tour)
    !tour.has_ended? &&
    !tour.booking_deadline_has_passed? &&
    current_user_can_book_tours? &&
    !Booking.given_user_booked_given_tour?(current_user, tour) &&
    !Waitlist.given_user_waitlisted_given_tour?(current_user, tour)
  end

  #######################################################################
  # BOOKING PERMISSIONS
  #######################################################################

  # Method to determine if the current user is allowed to look at all bookings
  # Only an admin should be able to do this
  def current_user_can_see_all_bookings?
    current_user_admin?
  end

  # Method to determine if the current user is allowed to look at bookings they have made
  # Customers need this ability
  # Admins get it because they are special and can act as customers
  def current_user_can_see_their_bookings?
    current_user_admin? || current_user_customer?
  end

  # Method to determine if the current user is allowed to look at bookings of tours they have listed
  # Agents need this ability
  # Admins get it because they are special and can act as agents
  def current_user_can_see_bookings_for_their_tours?
    current_user_admin? || current_user_agent?
  end

  # Method to determine if the current user is allowed to modify the given booking
  # They can do this if they were the one to create the booking or if they are an admin
  # Further restriction: "Customer can cancel a tour before the tour start date"
  # From this we infer that you cannot cancel a booking or waitlist on or after the tour start date
  # From this we infer that you cannot modify a booking or waitlist on or after the tour start date
  def current_user_can_modify_given_booking?(booking)
    !booking.tour.has_started? &&
    (current_user_admin? || (logged_in? && booking.user_id == current_user.id))
  end

  #######################################################################
  # USER PERMISSIONS
  #######################################################################

  # Method to determine if the current user is allowed to look at users
  # Only the admin of a site should be able to see a list of the registered users
  def current_user_can_see_all_users?
    current_user_admin?
  end

  # Method to determine if the current user is allowed to see their own profile
  def current_user_can_see_own_profile?
    logged_in?
  end

  #######################################################################
  # LOCATION PERMISSIONS
  #######################################################################

  # Method to determine if the current user is allowed to look at locations
  # Agents need this so they can plan tours
  # Admins get it too
  def current_user_can_see_all_locations?
    current_user_admin? || current_user_agent?
  end

  # Method to determine if the current user is allowed to Create / Edit / Destroy locations
  # Agents need this so they can plan tours
  # Currently there is no concept of only being able to modify locations that you have entered
  # Because agents share a pool of possible tour locations
  # Admins get this privilege too of course
  def current_user_can_modify_locations?
    current_user_admin? || current_user_agent?
  end

end
