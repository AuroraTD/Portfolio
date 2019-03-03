json.extract! listing, :id, :user_id, :tour_id, :created_at, :updated_at
json.url listing_url(listing, format: :json)
