class CreateStartAts < ActiveRecord::Migration[5.2]
  def change
    create_table :start_ats do |t|
      t.references :tour, foreign_key: true
      t.references :location, foreign_key: true

      t.timestamps
    end
  end
end
