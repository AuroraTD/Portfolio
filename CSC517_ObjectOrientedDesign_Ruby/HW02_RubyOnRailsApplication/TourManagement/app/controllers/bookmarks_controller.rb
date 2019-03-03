class BookmarksController < ApplicationController
  before_action :set_bookmark, only: [:show, :edit, :update, :destroy]

  # GET /bookmarks
  # GET /bookmarks.json
  def index
    @bookmarks = Bookmark.find_user_bookmarks(params)
    @page_title = set_page_title
  end

  # GET /bookmarks/1
  # GET /bookmarks/1.json
  def show
  end

  # GET /bookmarks/new
  def new
    # Save tour so that the view may use it (otherwise user would have to select tour)
    @tour = Tour.find(params['tour_id'])
    @bookmark = Bookmark.new
  end

  # GET /bookmarks/1/edit
  def edit
  end

  # POST /bookmarks
  # POST /bookmarks.json
  def create
    @bookmark = Bookmark.new(bookmark_params)

    respond_to do |format|
      if @bookmark.save
        format.html { redirect_to @bookmark, notice: 'Bookmark was successfully created.' }
        format.json { render :show, status: :created, location: @bookmark }
      else
        format.html { render :show, notice: 'Bookmark already created' }
        format.json { render json: @bookmark.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /bookmarks/1
  # PATCH/PUT /bookmarks/1.json
  def update
    respond_to do |format|
      if @bookmark.update(bookmark_params)
        format.html { redirect_to @bookmark, notice: 'Bookmark was successfully updated.' }
        format.json { render :show, status: :ok, location: @bookmark }
      else
        format.html { render :edit }
        format.json { render json: @bookmark.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /bookmarks/1
  # DELETE /bookmarks/1.json
  def destroy

    # Destroy bookmark
    @bookmark.destroy

    # Respond
    success_notice = 'Bookmark was successfully destroyed.'
    respond_to do |format|
      if current_user_can_see_all_bookmarks?
        format.html { redirect_to bookmarks_url, notice: success_notice }
      elsif current_user_can_see_bookmarks_for_their_tours?
        format.html { redirect_to bookmarks_path(listing_user_id: current_user.id), notice: success_notice }
      elsif current_user_can_see_their_bookmarks?
        format.html { redirect_to bookmarks_path(bookmarks_user: current_user.id), notice: success_notice }
      else
        format.html { redirect_to login_path, notice: success_notice }
      end
      format.json { head :no_content }
    end

  end

  private

  def set_page_title
    return "My Bookmarks" if params['bookmarks_user']
    return "Bookmarks for My Tours" if params['listing_user']
    "All Bookmarks"
  end

  # Use callbacks to share common setup or constraints between actions.
    def set_bookmark
      @bookmark = Bookmark.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def bookmark_params
      params.require(:bookmark).permit(:user_id, :tour_id)
    end
end
