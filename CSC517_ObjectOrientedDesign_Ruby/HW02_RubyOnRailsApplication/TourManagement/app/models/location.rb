################################################################################
# A location is a state/province and country that tour can start_at or visit[].
#
# Ensure that the country and state pair are unique.
# Rubify user_friendly_description
# Put column validations at the top

class Location < ApplicationRecord

  # Validations
  validates :country, presence: true, uniqueness: {scope: :state}
  validates :state, presence: true, uniqueness: {scope: :country}

  # Relationships
  has_many :start_ats, dependent: :destroy
  has_many :visits, dependent: :destroy
  has_many :tours, through: :start_ats, dependent: :destroy
  has_many :tours, through: :visits, dependent: :destroy

  # Method to get a user-friendly string describing the location
  def user_friendly_description
    state + ", " + country
  end

end
