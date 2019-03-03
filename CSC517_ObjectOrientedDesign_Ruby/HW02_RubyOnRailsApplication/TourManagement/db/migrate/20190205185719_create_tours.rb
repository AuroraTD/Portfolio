class CreateTours < ActiveRecord::Migration[5.2]
  def change
    create_table :tours do |t|
      t.string :name
      t.string :description
      t.integer :price_in_cents
      t.date :deadline
      t.date :start_date
      t.date :end_date
      t.string :operator_contact
      t.string :status
      t.integer :num_seats

      t.timestamps
    end
  end
end
