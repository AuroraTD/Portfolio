class UsersController < ApplicationController
  before_action :set_user, only: [:show, :edit, :update, :destroy]

  # GET /users
  # GET /users.json
  def index
    @users = User.all
  end

  # GET /users/1
  # GET /users/1.json
  def show
  end

  # GET /users/new
  def new
    @user = User.new
  end

  # GET /users/1/edit
  def edit
  end

  # POST /users
  # POST /users.json
  def create

    # Create user record
    @user = User.new(user_params)

    # Respond
    respond_to do |format|
      if @user.save
        if logged_in?
          # If you created a new user when already logged in, you're an admin
          format.html { redirect_to @user, notice: 'User account was successfully created.' }
        else
          # If you created a new user when not logged in, you must be signing up
          format.html { redirect_to login_path, notice: 'User account was successfully created.  Please Log In.' }
        end
        format.json { render :show, status: :created, location: @user }
      else
        format.html { render :new }
        format.json { render json: @user.errors, status: :unprocessable_entity }
      end
    end

  end

  # PATCH/PUT /users/1
  # PATCH/PUT /users/1.json
  def update

    # Users are not allowed to modify login and password
    # Only name, agent boolean, and customer boolean
    update_name_success = @user.update_attribute(:name, user_params['name'])
    update_agent_success = @user.update_attribute(:agent, user_params['agent'].to_i)
    update_customer_success = @user.update_attribute(:customer, user_params['customer'].to_i)

    # Now proceed
    respond_to do |format|
      if update_name_success && update_agent_success && update_customer_success
        format.html { redirect_to @user, notice: 'User was successfully updated.' }
        format.json { render :show, status: :ok, location: @user }
      else
        format.html { render :edit }
        format.json { render json: @user.errors, status: :unprocessable_entity }
      end
    end

  end

  # DELETE /users/1
  # DELETE /users/1.json
  def destroy

    # Destroy user
    @user.destroy

    # Respond
    success_notice = 'User was successfully destroyed.'
    respond_to do |format|
      if current_user_can_see_all_users?
        format.html { redirect_to users_url, notice: success_notice }
      else
        format.html { redirect_to login_path, notice: success_notice }
      end
      format.json { head :no_content }
    end

  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_user
      @user = User.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def user_params
      params.require(:user).permit(
        :email,
        :password,
        :password_digest,
        :name,
        :admin,
        :agent,
        :customer
      )
    end
end
