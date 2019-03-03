################################################################################
# A listing records the agent/admin that created a tour.
#

class Listing < ApplicationRecord

  # Establish relationships
  belongs_to :user
  belongs_to :tour

  # Method to get the id of the agent who created the given tour
  def self.get_agent_id_for_tour(tour)
    matching_listing = Listing.find_by(tour_id: tour.id)
    if matching_listing
      matching_user_id = matching_listing.read_attribute("user_id")
      return matching_user_id
    else
      return nil
    end
  end

  # Method to get the name of the agent who created the given tour
  def self.get_agent_name_for_tour(tour)
    matching_user_id = Listing.get_agent_id_for_tour(tour)
    if matching_user_id
      matching_user = User.find(matching_user_id)
      matching_user_name = matching_user.read_attribute("name")
      return matching_user_name
    else
      return nil
    end
  end

end
