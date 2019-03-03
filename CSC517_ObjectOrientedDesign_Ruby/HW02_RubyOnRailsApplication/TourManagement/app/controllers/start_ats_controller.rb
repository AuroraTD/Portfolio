class StartAtsController < ApplicationController
  before_action :set_start, only: [:show, :edit, :update, :destroy]

  # GET /starts
  # GET /starts.json
  def index
    @start_ats = StartAt.all
  end

  # GET /starts/1
  # GET /starts/1.json
  def show
  end

  # GET /starts/new
  def new
    @start_at = StartAt.new
  end

  # GET /starts/1/edit
  def edit
  end

  # POST /starts
  # POST /starts.json
  def create
    @start_at = StartAt.new(start_params)

    respond_to do |format|
      if @start_at.save
        format.html { redirect_to @start_at, notice: 'StartAt was successfully
created.' }
        format.json { render :show, status: :created, location: @start_at }
      else
        format.html { render :new }
        format.json { render json: @start_at.errors, status:
            :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /starts/1
  # PATCH/PUT /starts/1.json
  def update
    respond_to do |format|
      if @start_at.update(start_params)
        format.html { redirect_to @start_at, notice: 'StartAt was successfully
updated.' }
        format.json { render :show, status: :ok, location: @start_at }
      else
        format.html { render :edit }
        format.json { render json: @start_at.errors, status:
            :unprocessable_entity }
      end
    end
  end

  # DELETE /starts/1
  # DELETE /starts/1.json
  def destroy
    # Did not think about / update redirect logic here
    # It is not expected that a start_at will ever be destroyed by a user
    @start_at.destroy
    respond_to do |format|
      format.html { redirect_to start_ats_url, notice: 'StartAt was successfully destroyed.' }
      format.json { head :no_content }
    end
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_start
      @start_at = StartAt.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def start_params
      params.require(:start_at).permit(:tour_id, :location_id)
    end
end
