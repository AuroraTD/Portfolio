class AddPasswordDigestToUsers < ActiveRecord::Migration[5.2]
  # Migration created per https://www.railstutorial.org/book/modeling_users#sec-creating_and_authenticating_a_user
  def change
    add_column :users, :password_digest, :string
  end
end
