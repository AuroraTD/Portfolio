class CreateUsers < ActiveRecord::Migration[5.2]
  def change
    create_table :users do |t|
      t.string :email
      t.string :password
      t.string :name
      t.boolean :admin
      t.boolean :agent
      t.boolean :customer

      t.timestamps
    end
  end
end
