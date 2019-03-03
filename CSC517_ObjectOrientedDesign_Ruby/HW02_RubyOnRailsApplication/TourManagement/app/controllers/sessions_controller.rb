class SessionsController < ApplicationController

  # Content from https://www.railstutorial.org/book/basic_login

  # GET /login
  def new
  end

  # POST /login
  def create
    user = User.find_by(email: params[:session][:email].downcase)
    if log_in_user_with_password?(user, params[:session][:password])
      # Log the user in and redirect to the user's show page
      log_in user
      redirect_to user
    else
      # Create an error message for a rendered page (do not persist beyond this)
      flash.now[:danger] = 'Invalid email/password combination'
      render 'new'
    end
  end

  # DELETE /logout
  def destroy
    log_out
    redirect_to root_url
  end

end
