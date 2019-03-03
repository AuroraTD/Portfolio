class AddCancelledToTours < ActiveRecord::Migration[5.2]
  def change
    add_column :tours, :cancelled, :boolean
  end
end
