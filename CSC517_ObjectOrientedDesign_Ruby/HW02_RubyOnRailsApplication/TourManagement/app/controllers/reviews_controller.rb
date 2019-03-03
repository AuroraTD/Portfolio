class ReviewsController < ApplicationController
  before_action :set_review, only: [:show, :edit, :update, :destroy]

  # GET /reviews
  # GET /reviews.json
  def index
    @reviews = Review.get_reviews(params)
    set_page_title
  end

  # GET /reviews/1
  # GET /reviews/1.json
  def show

  end

  # GET /reviews/new
  def new
    @review = Review.new
  end

  # GET /reviews/1/edit
  def edit
  end

  # POST /reviews
  # POST /reviews.json
  def create

    # Associate the currently logged in user with this review
    #   This way, the view is not cluttered with the user (they already know who they are)
    # The assumption is that there IS a logged in user
    #   If not then this review creation should fail
    raise "A review cannot be created if there is no logged-in user" unless current_user
    @review = Review.new(review_params.merge(:user_id => current_user.id))

    # Respond
    respond_to do |format|
      if @review.save
        format.html { redirect_to @review, notice: 'Review was successfully created.' }
        format.json { render :show, status: :created, location: @review }
      else
        format.html { render :new }
        format.json { render json: @review.errors, status: :unprocessable_entity }
      end
    end

  end

  # PATCH/PUT /reviews/1
  # PATCH/PUT /reviews/1.json
  def update
    respond_to do |format|
      if @review.update(review_params)
        format.html { redirect_to @review, notice: 'Review was successfully updated.' }
        format.json { render :show, status: :ok, location: @review }
      else
        format.html { render :edit }
        format.json { render json: @review.errors, status: :unprocessable_entity }
      end
    end
  end


  # DELETE /reviews/1
  # DELETE /reviews/1.json
  def destroy

    # Destroy review
    @review.destroy

    # Respond
    success_notice = 'Review was successfully destroyed.'
    respond_to do |format|
      # Current logic when this code was written is that everyone can see all reviews
      if current_user_can_see_all_reviews?
        format.html { redirect_to reviews_url, notice: success_notice }
      else
        format.html { redirect_to login_path, notice: success_notice }
      end
      format.json { head :no_content }
    end

  end

  private

    # Use callbacks to share common setup or constraints between actions.
    def set_review
      @review = Review.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def review_params
      params.require(:review).permit(:subject, :content, :user_id, :tour_id)
    end

    # Produce a helpful title for the page to be used in the view
    def set_page_title
      if params['reviewing_user_id']
        @page_title = "My Reviews"
      elsif params['listing_user_id']
        @page_title = "Reviews for My Tours"
      else
        @page_title = "All Reviews"
      end
    end

end
