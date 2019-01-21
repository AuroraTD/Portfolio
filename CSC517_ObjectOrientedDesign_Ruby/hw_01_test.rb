################################################################################
#
# File:         hw_01_test.rb
#
# Author:       ATTD -  Aurora Tiffany-Davis
#
# Description:  Test cases for hw_01.rb
#
################################################################################

load 'hw_01.rb'

# rubocop:disable Metrics/MethodLength, Metrics/AbcSize, Style/GlobalVars

# Global variables
$correct = 0
$wrong = 0
$full = 100

def print_running_score

  puts "\n #{caller_locations(1..1).first.label}"
  puts "correct: #{$correct}"
  puts "wrong: #{$wrong}"

end

def test_average

  average([4, 2, 6, 1]) == 3 ? $correct += 1 : $wrong += 1
  average([10, 20, 30, 40]) == 25 ? $correct += 1 : $wrong += 1
  average([1, 2, 3, 4]) == 2.5 ? $correct += 1 : $wrong += 1
  average([-1, -2, -3, -4]) == -2.5 ? $correct += 1 : $wrong += 1
  average([1, 217, 1000]) == 217 ? $correct += 1 : $wrong += 1
  average([2, 4]).zero? ? $correct += 1 : $wrong += 1
  average([2]).zero? ? $correct += 1 : $wrong += 1
  average([]).zero? ? $correct += 1 : $wrong += 1

rescue StandardError => e
  $wrong += 1
  puts e.message
  puts e.backtrace

ensure
  print_running_score

end

def test_twin_prime

  twin_prime?(3, 5) ? $correct += 1 : $wrong += 1
  twin_prime?(41, 43) ? $correct += 1 : $wrong += 1
  twin_prime?(43, 41) ? $correct += 1 : $wrong += 1
  !twin_prime?(9, 11) ? $correct += 1 : $wrong += 1
  !twin_prime?(7, 11) ? $correct += 1 : $wrong += 1
  !twin_prime?(0, 0) ? $correct += 1 : $wrong += 1
  !twin_prime?(0, 1) ? $correct += 1 : $wrong += 1
  !twin_prime?(-1, 1) ? $correct += 1 : $wrong += 1

rescue StandardError => e
  $wrong += 1
  puts e.message
  puts e.backtrace

ensure
  print_running_score

end

def test_check_product

  check_product?([1, 2, 3], 6) ? $correct += 1 : $wrong += 1
  check_product?([1, 2, 3, 0, 6], 0) ? $correct += 1 : $wrong += 1
  check_product?([1, 2, 3, 0, 6], 6) ? $correct += 1 : $wrong += 1
  check_product?([1, 2, 3, 0, 6], 36) ? $correct += 1 : $wrong += 1
  check_product?([-1, 2, 3, 0, 6], -18) ? $correct += 1 : $wrong += 1
  check_product?([1, 2, 2, 3, 3], 6) ? $correct += 1 : $wrong += 1
  check_product?([1, 2, 2, 3, 3], 12) ? $correct += 1 : $wrong += 1
  !check_product?([1, 2, 3], 2) ? $correct += 1 : $wrong += 1
  !check_product?([1, 2, 2, 3, 3], 24) ? $correct += 1 : $wrong += 1
  !check_product?([-1, 2, 3, 0, 6], -36) ? $correct += 1 : $wrong += 1
  !check_product?([1, 2], 2) ? $correct += 1 : $wrong += 1
  !check_product?([1], 1) ? $correct += 1 : $wrong += 1
  !check_product?([], 0) ? $correct += 1 : $wrong += 1

rescue StandardError => e
  $wrong += 1
  puts e.message
  puts e.backtrace

ensure
  print_running_score

end

def test_count_vowels

  count_vowels("Home") == 2 ? $correct += 1 : $wrong += 1
  count_vowels("Root") == 1 ? $correct += 1 : $wrong += 1
  count_vowels("Education") == 5 ? $correct += 1 : $wrong += 1
  count_vowels("AaBbCcDdEe") == 2 ? $correct += 1 : $wrong += 1
  count_vowels("Fly").zero? ? $correct += 1 : $wrong += 1
  count_vowels("xyz").zero? ? $correct += 1 : $wrong += 1
  count_vowels("").zero? ? $correct += 1 : $wrong += 1

rescue StandardError => e
  $wrong += 1
  puts e.message
  puts e.backtrace

ensure
  print_running_score

end

def test_valid_parentheses

  valid_parentheses?("()[]{}") ? $correct += 1 : $wrong += 1
  valid_parentheses?("(a)[b]{c}") ? $correct += 1 : $wrong += 1
  valid_parentheses?("a(b)c[d]e{f}g") ? $correct += 1 : $wrong += 1
  valid_parentheses?("(()())") ? $correct += 1 : $wrong += 1
  valid_parentheses?("(()([{}]))") ? $correct += 1 : $wrong += 1
  valid_parentheses?("") ? $correct += 1 : $wrong += 1
  valid_parentheses?("abc") ? $correct += 1 : $wrong += 1
  !valid_parentheses?("(()({[}]))") ? $correct += 1 : $wrong += 1
  !valid_parentheses?("[[)([{}]))") ? $correct += 1 : $wrong += 1
  !valid_parentheses?("(}") ? $correct += 1 : $wrong += 1
  !valid_parentheses?("({}") ? $correct += 1 : $wrong += 1
  !valid_parentheses?("(a}") ? $correct += 1 : $wrong += 1

rescue StandardError => e
  $wrong += 1
  puts e.message
  puts e.backtrace

ensure
  print_running_score

end

def test_longest_common_prefix

  longest_common_prefix(["a", "aa", "abc"]) == "a" ? $correct += 1 : $wrong += 1
  longest_common_prefix(["!@#$%^&*(()", "@#$%^&*()"]) == "" ? $correct += 1 : $wrong += 1
  longest_common_prefix(["!@#$%^&*(()", "!@#zork"]) == "!@#" ? $correct += 1 : $wrong += 1
  longest_common_prefix(["987", "9876"]) == "987" ? $correct += 1 : $wrong += 1
  longest_common_prefix(["aa", "bb", "cc"]) == "" ? $correct += 1 : $wrong += 1
  longest_common_prefix([]) == "" ? $correct += 1 : $wrong += 1

rescue StandardError => e
  $wrong += 1
  puts e.message
  puts e.backtrace

ensure
  print_running_score

end

def test_student_class

  # All arguments correct
  student = Student.new("Kevin", "02-19-2019")
  student.name == "Kevin" ? $correct += 1 : $wrong += 1
  student.enrollment_date == "02-19-2019" ? $correct += 1 : $wrong += 1
  student.got_enrolled_on == "Kevin - February 19 2019" ? $correct += 1 : $wrong += 1

  # All arguments correct
  student = Student.new("Mary Robinson", "01-15-2019")
  student.name == "Mary Robinson" ? $correct += 1 : $wrong += 1
  student.enrollment_date == "01-15-2019" ? $correct += 1 : $wrong += 1
  student.got_enrolled_on == "Mary Robinson - January 15 2019" ? $correct += 1 : $wrong += 1

  # Date is early
  student = Student.new("Kevin", "01-01-1900")
  student.name == "Kevin" ? $correct += 1 : $wrong += 1
  student.enrollment_date == "01-01-1900" ? $correct += 1 : $wrong += 1
  student.got_enrolled_on == "Kevin - January 1 1900" ? $correct += 1 : $wrong += 1

  # Date is late
  student = Student.new("Kevin", "12-31-4321")
  student.name == "Kevin" ? $correct += 1 : $wrong += 1
  student.enrollment_date == "12-31-4321" ? $correct += 1 : $wrong += 1
  student.got_enrolled_on == "Kevin - December 31 4321" ? $correct += 1 : $wrong += 1

  # Date is properly formatted but invalid
  student = Student.new("Kevin", "01-41-2019")
  student.name == "Kevin" ? $correct += 1 : $wrong += 1
  student.enrollment_date == "01-41-2019" ? $correct += 1 : $wrong += 1
  student.got_enrolled_on == "Invalid Date" ? $correct += 1 : $wrong += 1

  # Date is properly formatted but invalid
  student = Student.new("Kevin", "02-29-2019")
  student.name == "Kevin" ? $correct += 1 : $wrong += 1
  student.enrollment_date == "02-29-2019" ? $correct += 1 : $wrong += 1
  student.got_enrolled_on == "Invalid Date" ? $correct += 1 : $wrong += 1

  # Date is properly formatted but invalid
  student = Student.new("Kevin", "02-00-2019")
  student.name == "Kevin" ? $correct += 1 : $wrong += 1
  student.enrollment_date == "02-00-2019" ? $correct += 1 : $wrong += 1
  student.got_enrolled_on == "Invalid Date" ? $correct += 1 : $wrong += 1

  # Date is properly formatted but invalid
  student = Student.new("Kevin", "13-19-2019")
  student.name == "Kevin" ? $correct += 1 : $wrong += 1
  student.enrollment_date == "13-19-2019" ? $correct += 1 : $wrong += 1
  student.got_enrolled_on == "Invalid Date" ? $correct += 1 : $wrong += 1

  # Student name is nil
  begin
    Student.new(nil, "02-19-2019")
  rescue ArgumentError
    $correct += 1
  else
    $wrong += 1
  end

  # Student name is empty
  begin
    Student.new("", "02-19-2019")
  rescue ArgumentError
    $correct += 1
  else
    $wrong += 1
  end

  # Date is nil
  begin
    Student.new("Kevin", nil)
  rescue ArgumentError
    $correct += 1
  else
    $wrong += 1
  end

  # Date is empty
  begin
    Student.new("Kevin", "")
  rescue ArgumentError
    $correct += 1
  else
    $wrong += 1
  end

  # Date is improperly formatted
  begin
    Student.new("Kevin", "02192019")
  rescue ArgumentError
    $correct += 1
  else
    $wrong += 1
  end

  # Date is improperly formatted
  begin
    Student.new("Kevin", "02/19/2019")
  rescue ArgumentError
    $correct += 1
  else
    $wrong += 1
  end

  # Date is improperly formatted
  begin
    Student.new("Kevin", "2-19-2019")
  rescue ArgumentError
    $correct += 1
  else
    $wrong += 1
  end

  # Date is improperly formatted
  begin
    Student.new("Kevin", "02-19-2019 08:00:00")
  rescue ArgumentError
    $correct += 1
  else
    $wrong += 1
  end

rescue StandardError => e
  $wrong += 1
  puts e.message
  puts e.backtrace

ensure
  print_running_score

end

def scores

  test_average
  test_twin_prime
  test_check_product
  test_count_vowels
  test_valid_parentheses
  test_longest_common_prefix
  test_student_class

  # Report test results
  print_running_score
  puts "correct + wrong: #{$correct + $wrong}"
  return ( ($correct * $full).to_f / ($wrong + $correct) ).round(2)

end

puts scores

# rubocop:enable Metrics/MethodLength, Metrics/AbcSize, Style/GlobalVars