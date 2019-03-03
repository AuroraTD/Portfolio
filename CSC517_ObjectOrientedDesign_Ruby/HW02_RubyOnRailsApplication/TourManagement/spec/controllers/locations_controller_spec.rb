require 'rails_helper'

RSpec.describe LocationsController, type: :controller do
  fixtures :locations, :users

  describe "GET #index" do
    it "populates an array of locations" do
      location = [locations(:one), locations(:two)]
      get :index#locations
      expect(assigns(:locations)).to eq(location)
    end
    it "renders the :index view" do
      get :index
      expect(response).to render_template :index
    end
  end

  describe "GET #show" do
    it "assigns the requested location to @location" do
      location = locations(:one)
      get :show, params: { id: location.id }
      expect(assigns(:location)).to eq(location)
    end
    it "renders the :show template" do
      get :show, params: { id: locations(:one).id }
      expect(response).to render_template :show
    end
  end

  describe "GET #edit" do
    it "renders the :edit template" do
      get :edit, params: { id: locations(:one).id }
      expect(response).to render_template :edit
    end
  end

  describe "GET #new" do
    it "assigns a new Location to @location" do
      get :new
      expect(assigns(:location)).to_not eq nil
      expect(assigns(:location)).to be_a_new(Location)
    end
    it "renders the :new template" do
      get :new
      expect(response).to render_template :new
    end
  end

  describe "POST #create" do
    let(:valid_params) { { location: {
        state: "New State",
        country: "USA",
    }}}
    let(:invalid_params) { { location: {
        state: "",
        country: "USA",
    }}}
    it "saves valid location and redirects to location_url" do
      previous_count = Location.count
      post :create, params: valid_params
      expect(Location.count).to eq(previous_count+1)
      expect(response).to redirect_to location_url(Location.last)
    end

    it "rejects invalid location and renders the new view" do
      previous_count = Location.count
      post :create, params: invalid_params
      expect(Location.count).to eq(previous_count)
      expect(response).to render_template :new
    end
  end

  describe "PATCH/PUT #update" do
    let(:invalid_params) { { location: {
        state: locations(:one).state,
        country: "",
    }}}
    context "with valid location" do
      it "updates and redirects" do
        patch :update, params: {"id"=>locations(:one).id,
                                "location"=>{state: "New State",
                                             country: locations(:one).country}}
        expect(response).to redirect_to("/locations/#{locations(:one).id}")
      end
    end
    context "with invalid location" do
      it "does not update, and re-renders the form" do
        patch :update, params: {"id"=>locations(:one).id,
                                "location"=>{state: "",
                                             country: locations(:one).country}}
        expect(response).to render_template :edit
      end
    end
  end

  describe "DELETE #destroy" do
    context "for admins" do
      it "deletes the location and redirects to location_url" do
        session[:user_id] = users(:one).id
        @location = Location.last
        previous_count = Location.count
        delete :destroy, params: { id: locations(:two).id }
        expect(Location.count).to eq(previous_count-1)
        expect(response).to redirect_to locations_url
      end
    end
    context "for agents" do
      it "deletes the location and redirects to location_url" do
        session[:user_id] = users(:two).id
        @location = Location.last
        previous_count = Location.count
        delete :destroy, params: { id: locations(:two).id }
        expect(Location.count).to eq(previous_count-1)
        expect(response).to redirect_to locations_url
      end
    end
  end

end
