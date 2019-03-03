# This file should contain all the record creation needed to seed the database with its default values.
# The data can then be loaded with the rails db:seed command (or created alongside the database with db:setup).
#
# Examples:
#
#   movies = Movie.create([{ name: 'Star Wars' }, { name: 'Lord of the Rings' }])
#   Character.create(name: 'Luke', movie: movies.first)

# Seed the database with one admin role
# Every team member will need to run db:seed command to get this admin in their DB
# Modified to support user login
# All team members now must remove any existing users and re-seed, or they may get problems like:
# https://stackoverflow.com/questions/11037864/bcrypterrorsinvalidhash-when-trying-to-sign-in

# Use create! instead of create for debuggability
# https://stackoverflow.com/questions/13617015/running-rake-dbseed-isnt-loading-from-seeds-rb

# Seeded users have password attribute explicitly passed, to avoid trouble with password validations
# It's a bit unrealistic because when the application is actually running,
# the password doesn't actually end up explicitly stored in the table,
# instead what gets stored is password_digest for security reasons.
# Okay for seeding though.

User.create!(
  email: "john@john.com",
  name: "John Adminsky",
  password_digest: User.digest('john_password'),
  password: 'john_password',
  admin: true, agent: false, customer: false
)
case Rails.env
when "development"

  # Load users
  agent_1 = User.create!(
    email: "jason@jason.com",
    name: "Jason Agenton",
    password_digest: User.digest('jason_password'),
    password: 'jason_password',
    admin: false, agent: true, customer: false
  )
  User.create!(
    email: "george@george.com",
    name: "George Customerov",
    password_digest: User.digest('george_password'),
    password: 'george_password',
    admin: false, agent: false, customer: true
  )
  agent_2 = User.create!(
    email: "ann@ann.com",
    name: "Ann Agenstomer",
    password_digest: User.digest('ann_password'),
    password: 'ann_password',
    admin: false, agent: true, customer: true
  )

  # Load locations
  location_1 = Location.create!(state: "VA", country: "USA")
  location_2 = Location.create!(state: "NC", country: "USA")
  location_3 = Location.create!(state: "SC", country: "USA")
  location_4 = Location.create!(state: "GA", country: "USA")

  # Load tours
  tour_1 = Tour.create!(
    name: "BBQ Tour",
    description: "Smoky Goodness",
    price_in_cents: 19999,
    deadline: DateTime.new(2019, 3, 2),
    start_date: DateTime.new(2019, 3, 3),
    end_date: DateTime.new(2019, 3, 10),
    operator_contact: "Acme Tour Co",
    num_seats: 100
  )
  tour_2 = Tour.create!(
    name: "History Tour",
    description: "Very Informative",
    price_in_cents: 20100,
    deadline: DateTime.new(2020, 3, 2),
    start_date: DateTime.new(2020, 3, 3),
    end_date: DateTime.new(2020, 3, 10),
    operator_contact: "Widgets Inc",
    num_seats: 10
  )

  # Give tours locations
  Visit.create!(
    tour_id: tour_1.id,
    location_id: location_1.id
  )
  Visit.create!(
    tour_id: tour_1.id,
    location_id: location_2.id
  )
  Visit.create!(
    tour_id: tour_2.id,
    location_id: location_3.id
  )
  Visit.create!(
    tour_id: tour_2.id,
    location_id: location_4.id
  )

  # Give tours agents
  Listing.create!(
    tour_id: tour_1.id,
    user_id: agent_1.id
  )
  Listing.create!(
    tour_id: tour_2.id,
    user_id: agent_2.id
  )

else
  # type code here
end