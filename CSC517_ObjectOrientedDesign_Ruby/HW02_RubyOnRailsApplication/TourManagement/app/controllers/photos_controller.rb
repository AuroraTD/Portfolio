class PhotosController < ApplicationController
  before_action :set_photo, only: [:show, :edit, :update, :destroy]

  # GET /photos
  # GET /photos.json
  def index

    # Show only photos for the tour indicated in the params populated by the link to this action
    # Unless the tour was not indicated in the params (in which case show all photos)
    if params['tour_id']
      @photos = Photo.where(tour_id: params['tour_id'])
      # Remember what tour we are working with and make this available to the view
      # This way the view can pass the tour info along in links as needed
      @tour = Tour.find(params['tour_id'])
    else
      @photos = Photo.all
    end

  end

  # GET /photos/1
  # GET /photos/1.json
  def show
  end

  # GET /photos/new
  def new

    # Create an empty photo object
    @photo = Photo.new

    # Remember what tour we are working with and make this available to the view
    # This way the view can pass the tour info along in links as needed
    @tour = Tour.find(params['tour_id'])

  end

  # GET /photos/1/edit
  def edit

    # Remember what tour we are working with and make this available to the view
    # This way the view can pass the tour info along in links / form fields as needed
    # This is to avoid bothering the user to enter the tour
    @tour = @photo.tour

  end

  # POST /photos
  # POST /photos.json
  def create

    # Attempt creation
    @photo = Photo.new(photo_params)

    # Respond
    respond_to do |format|
      if @photo.save
        format.html { redirect_to @photo, notice: 'Photo was successfully created.' }
        format.json { render :show, status: :created, location: @photo }
      else
        # In case of errors make sure they go into flash
        if @photo.errors.full_messages.length.positive?
          flash[:error] = @photo.errors.full_messages.join(", ")
        end
        # Redirect
        format.html { redirect_to new_photo_path(tour_id: @photo.tour.id) }
        format.json { render json: @photo.errors, status: :unprocessable_entity }
      end
    end

  end

  # PATCH/PUT /photos/1
  # PATCH/PUT /photos/1.json
  def update
    respond_to do |format|
      if @photo.update(photo_params)
        format.html { redirect_to @photo, notice: 'Photo was successfully updated.' }
        format.json { render :show, status: :ok, location: @photo }
      else
        format.html { redirect_to edit_photo_path(tour_id: @photo.tour.id) }
        format.json { render json: @photo.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /photos/1
  # DELETE /photos/1.json
  def destroy

    # Destroy photo
    @photo.destroy

    # Respond
    # There is only one path because
    # any user who can destroy photos can also see a list of all photos for the tour
    respond_to do |format|
      # Make sure to pass along the tour ID in EVERY link that brings you to the photos index
      format.html { redirect_to photos_path(tour_id: @photo.tour.id), notice: 'Photo was successfully destroyed.' }
      format.json { head :no_content }
    end
  end

  private

    # Use callbacks to share common setup or constraints between actions.
    def set_photo
      @photo = Photo.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def photo_params
      # https://evilmartians.com/chronicles/rails-5-2-active-storage-and-beyond
      params.require(:photo).permit(:name, :tour_id, :image)
    end

end
