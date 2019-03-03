################################################################################
# A photo is an image that belongs to a tour.
#

class Photo < ApplicationRecord

  # Relationships
  # https://evilmartians.com/chronicles/rails-5-2-active-storage-and-beyond
  belongs_to :tour
  has_one_attached :image

  # Validations
  validates :name, presence: true
  validate :image_presence

  # Validation methods
  # https://stackoverflow.com/questions/48158770/activestorage-file-attachment-validation
  # https://prograils.com/posts/rails-5-2-active-storage-new-approach-to-file-uploads
  def image_presence
    return if image.attached?
    errors.add(:base, "Must attach an image")
  end

end
