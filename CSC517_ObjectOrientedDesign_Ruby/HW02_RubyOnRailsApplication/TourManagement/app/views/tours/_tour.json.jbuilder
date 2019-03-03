json.extract! tour, :id, :name, :description, :price_in_cents, :deadline, :start_date, :end_date, :operator_contact, :cancelled, :num_seats, :created_at, :updated_at
json.url tour_url(tour, format: :json)
