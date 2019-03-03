module ToursHelper

  # Method to determine the tour name previously selected by the user in search filtering
  def user_selected_tour_name
    if flash[:filters].length.positive? && flash[:filters][:tour_name.to_s]
      return flash[:filters][:tour_name.to_s]
    else
      return ""
    end
  end

  # Method to determine if the given location is one selected by the user in search filtering
  def user_selected_location?(location)
    if flash[:filters].length.positive? && flash[:filters][:desired_location.to_s]
      return flash[:filters][:desired_location.to_s].to_i == location.id
    else
      return false
    end
  end

  # Method to determine the maximum price previously selected by the user in search filtering
  def user_selected_maximum_price
    if flash[:filters].length.positive? && flash[:filters][:max_price_dollars.to_s]
      return flash[:filters][:max_price_dollars.to_s].to_f
    else
      return ""
    end
  end

  # Method to determine the earliest start date previously selected by the user in search filtering
  # If there is no such user selection, return a very early start date
  # Dates are stored in YYYY-MM-DD format
  def user_selected_earliest_start
    if (
      flash[:filters].length.positive? &&
      flash[:filters][:earliest_start.to_s] &&
      # https://stackoverflow.com/questions/15989329/what-is-good-if-an-empty-string-is-truthy
      flash[:filters][:earliest_start.to_s].length.positive?
    )
      date_object = Date.strptime(flash[:filters][:earliest_start.to_s], "%Y-%m-%d")
    else
      date_object = Date.today.prev_month
    end
    return date_object
  end

  # Method to determine the latest end date previously selected by the user in search filtering
  # If there is no such user selection, return a very late end date
  # Dates are stored in YYYY-MM-DD format
  # noinspection RubyParenthesesAroundConditionInspection
  def user_selected_latest_end
    if (
      flash[:filters].length.positive? &&
      flash[:filters][:latest_end.to_s] &&
      # https://stackoverflow.com/questions/15989329/what-is-good-if-an-empty-string-is-truthy
      flash[:filters][:latest_end.to_s].length.positive?
    )
      date_object = Date.strptime(flash[:filters][:latest_end.to_s], "%Y-%m-%d")
    else
      date_object = Date.today.next_month
    end
    return date_object
  end

  # Method to determine the minimum # available seats previously selected by the user in search filtering
  # If there is no such user selection, return zero
  def user_selected_min_seats
    if flash[:filters].length.positive? && flash[:filters][:min_seats.to_s]
      return flash[:filters][:min_seats.to_s]
    else
      return 0
    end
  end

end
