################################################################################
# A user is a registered entity that interacts with the app. There are
# currently 3 types of users; admin, agent, and customer.
#
# Ensure that the e-mail address is case insensitive, i.e.
# george@george.com == George@george.com
# Rubify code.

class User < ApplicationRecord

  # Define straightforward one-to-many relationship with reviews
  has_many :reviews, dependent: :destroy

  # Define intermediate relationships, needed to support "through" concept below
  has_many :listings, dependent: :destroy
  has_many :bookmarks, dependent: :destroy
  has_many :bookings, dependent: :destroy
  has_many :waitlists, dependent: :destroy

  # Define has-many-through relationships, and what to do if dependent record goes away
  has_many :tours, through: :listings, dependent: :destroy
  has_many :tours, through: :bookmarks, dependent: :destroy
  has_many :tours, through: :bookings, dependent: :destroy
  has_many :tours, through: :waitlists, dependent: :destroy

  # Define validations
  validates :email, presence: true, uniqueness: {case_sensitive: false},
                                                 format: {
                    with: URI::MailTo::EMAIL_REGEXP,
                    message: "addresses should be of the form xx@xx.xx" }
  validates :name, presence: true
  validates :password, presence: true, length: { in: 6..40 }

  # Do NOT validate presence of boolean fields (admin / agent / customer)
  # Seems to see false as not-present
  # We should make sure that unless a user has admin TRUE it cannot act as admin
  # That is, lack of value should be interpreted as FALSE by our application logic
  # validates :admin
  # validates :name
  # validates :customer

  # Include ability to authenticate password
  # https://www.railstutorial.org/book/modeling_users#sec-creating_and_authenticating_a_user
  has_secure_password

  # Include ability to create password digests (to support test fixtures)
  # https://www.railstutorial.org/book/basic_login
  # Returns the hash digest of the given string.
  def self.digest(string)
    cost = ActiveModel::SecurePassword.min_cost ? BCrypt::Engine::MIN_COST : BCrypt::Engine.cost
    BCrypt::Password.create(string, cost: cost)
  end

  # Method to generate a screen-friendly description of user type
  def user_type
    user_types = []
    if admin
      user_types << "Admin"
    end
    if agent
      user_types << "Agent"
    end
    if customer
      user_types << "Customer"
    end
    user_types.join(" / ")
  end

end
