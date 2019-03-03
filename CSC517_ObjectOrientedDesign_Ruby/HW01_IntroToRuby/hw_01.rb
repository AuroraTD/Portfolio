###################################################################################
#
# File:         hw_01.rb
#
# Author:       ATTD -  Aurora Tiffany-Davis
#
# Description:  Introduction to programming in Ruby
#
###################################################################################

# Require modules
require "prime"
require "date"

# Part 1

###################################################################################
#
# Method:       average
#
# Author:       ATTD -  Aurora Tiffany-Davis
#
# Arguments:    Array of integers
#
# Return:       Average of all elements other than smallest and largest
#               as a float value
#
# Description:  Find the average of an array of integers, ignoring min and max
#
###################################################################################
def average (array_of_integers)

  # Establish default return value
  average_value = 0.0

  # If 2 or fewer elements, just return default value
  if array_of_integers.size > 2

    # Delete minimum
    array_of_integers.delete_at(array_of_integers.index(array_of_integers.min))

    # Delete maximum
    array_of_integers.delete_at(array_of_integers.index(array_of_integers.max))

    # Compute average of remaining elements
    sum_of_values = array_of_integers.inject(&:+)
    average_value = sum_of_values.to_f / array_of_integers.size

  end

  # Return
  return average_value

end

###################################################################################
#
# Method:       twin_prime?
#
# Author:       ATTD -  Aurora Tiffany-Davis
#
# Arguments:    Two integers
#
# Return:       True if integers are twin primes, false otherwise
#
# Description:  Determine if two integers are twin primes.
#               The order of the arguments to this function should not matter.
#
###################################################################################
def twin_prime? (candidate_a, candidate_b)

  # Twin primes if both are primes and the difference between them is 2
  return  Prime.prime?(candidate_a) &&
          Prime.prime?(candidate_b) &&
          (candidate_a - candidate_b).abs == 2

end

###################################################################################
#
# Method:       check_product?
#
# Author:       ATTD -  Aurora Tiffany-Davis
#
# Arguments:    Array of integers, desired product
#
# Return:       True if three elements in array can be used to get desired product
#
# Description:  Takes an array of integers and an additional integer product as
#                 arguments and returns true if there exist three elements
#                 in the array (each of which can be used only once)
#                 whose product equals product.
#               check_product?([], product)
#                 should return false for any value of product, by definition.
#               The return value should be a boolean, not a string.
#               Do not return the string "true" or "false"!)
#               If the array has fewer than three elements,
#                 the method also returns false.
#
###################################################################################
def check_product? (array, product)

  # Get all 3-element combos and see if any of them produce the desired product
  return  array.size > 2 &&
          array.combination(3).any? { |a, b, c| a * b * c == product }

end

# Part 2

###################################################################################
#
# Method:       count_vowels
#
# Author:       ATTD -  Aurora Tiffany-Davis
#
# Arguments:    String we want to count vowels in
#
# Return:       Number of distinct vowels in string (case-insensitive)
#
# Description:  Takes a string as an argument and returns
#                 the number of distinct vowels in a string.
#               Ignore case when counting distinct vowels in a string.
#               So, for instance, count_vowels("Ee") should return 1.
###################################################################################
def count_vowels (string)

  # Downcase for case insensitivity, get vowels, drop non-unique, count
  return string.downcase.scan(/[aeiou]/).uniq.count

end

###################################################################################
#
# Method:       valid_parentheses?
#
# Author:       ATTD -  Aurora Tiffany-Davis
#
# Arguments:    String possibly containing parentheticals
#
# Return:       True if the string nests parentheticals correctly, false otherwise
#
# Description:  Takes a string as an argument containing just the characters
#                 '(', ')', '{', '}', '[' and ']',
#                 determines whether the brackets are matched.
#               Nested brackets are allowed, if they are nested correctly.
#               The method returns true if the input string is valid,
#                 otherwise returns false.
#               The return value should be a boolean, not a string.
#               Do not return the string "true" or "false"!
#               The brackets must close in the correct order,
#                 "()" and "()[]{}" are valid but "(]" and "([)]" are not.
#               It's okay to have non-brackety stuff in the string,
#                 e.g. "(a)[b]{c}" and "abc" fine.
#
###################################################################################
def valid_parentheses? (string)

  # Assume validity until we know otherwise
  valid = true

  # Establish stack and collections of brackety stuff
  stack = []
  openers = ['(', '[', '{']
  closers = [')', ']', '}']

  # Openers are pushed, closers are popped, anything unexpected is invalid
  string.each_char do |c|
    if openers.include?(c)
      stack.push(c)
    elsif closers.include?(c)
      # Top of stack should have matching opener
      # Cannot closer char, cast to int, decrement, cast back to char
      #   because ASCII goes like this: (, ), ... [, \, ], ... {, |, }
      matching_opener = openers[closers.find_index(c)]
      if stack.last == matching_opener
        stack.pop
      else
        valid = false
        break
      end
    end

  end

  # Anything left over is invalid
  unless stack.empty?
    valid = false
  end

  # Return
  return valid

end

###################################################################################
#
# Method:       longest_common_prefix
#
# Author:       ATTD -  Aurora Tiffany-Davis
#
# Arguments:    Array of strings
#
# Returns:      Longest common prefix string among the array of strings
#
# Description:  Returns the longest common prefix string
#               among the given array of strings.
#
###################################################################################
def longest_common_prefix (array_of_strings)

  # Establish default value
  lcp = ""

  # Get the length of the shortest string
  min_length =
    if array_of_strings.size.zero?
      0
    else
      array_of_strings.min_by(&:length).size
    end

  # Go through all of the strings, but not past the length of the shortest string
  min_length.times do |i|

    # If all of the strings have the same character at this index,
    # then it's part of the LCP
    chars_at_index = array_of_strings.map { |s| s[i] }

    if chars_at_index.uniq.count == 1
      lcp += chars_at_index[0]
    else
      break
    end

  end

  # Return
  return lcp

end

# Part 3

###################################################################################
#
# Class:        Student
#
# Author:       ATTD -  Aurora Tiffany-Davis
#
# Description:  You will be implementing a Student class.
#               Each Student has two attributes - name and enrollment_date.
#               The attributes shouldn't be publicly accessible;
#                 rather they should be read and modified through
#                 proper getters and setters.
#               You need to use attr* methods or define your own for
#                 setting/getting the class/instance variables.
#
###################################################################################
class Student

  # Create both getter and setter for name and enrollment date
  attr_accessor :name, :enrollment_date

  #################################################################################
  #
  # Method:       initialize
  #
  # Author:       ATTD -  Aurora Tiffany-Davis
  #
  # Arguments:    name of student, enrollment date for student ("mm-dd-yyyy")
  #
  # Return:       student
  #
  # Description:  Constructor for Student class.
  #               The constructor should accept the name (string) as the first
  #                 argument and enrollment_date
  #                 (string, the date format "mm-dd-yyyy") as second argument,
  #                 and should raise ArgumentError
  #                 (one of Ruby's built-in exception types) if
  #                 the name is nil or the empty string or if
  #                 enrollment_date is nil or its format is incorrect.
  #               We may assume the date entered will lie between
  #                 "01-01-1970" and "12-31-2020"
  #
  #################################################################################
  def initialize (name, enrollment_date)

    # Instance variables
    @name = name
    @enrollment_date = enrollment_date

    # Check arguments
    if name.nil?
      raise ArgumentError, "name is nil"
    elsif name.empty?
      raise ArgumentError, "name is empty"
    elsif enrollment_date.nil?
      raise ArgumentError, "enrollment date is nil"
    elsif enrollment_date.empty?
      raise ArgumentError, "enrollment date is empty"
    elsif enrollment_date !~ /^\d{2}-\d{2}-\d{4}$/
      raise ArgumentError, "enrollment date is improperly formatted"
    end

  end

  #################################################################################
  #
  # Method:       got_enrolled_on
  #
  # Author:       ATTD -  Aurora Tiffany-Davis
  #
  # Arguments:    None
  #
  # Return:       String noting student name and enrollment date in the format
  #               "Kevin - January 1 2019"
  #
  # Description:  Returns a string in the following format
  #                 (Assume the date is between "01-01-1970" and "12-31-2020"):
  #               If enrollment_date is invalid
  #                 (such as "01-41-2019" or "02-29-2019") return "Invalid Date".
  #               If enrollment_date is valid, and date is "01-15-2019",
  #                 (assuming name of the student is Kevin)
  #                 return a string in the format - "Kevin - January 1 2019".
  #
  #################################################################################
  def got_enrolled_on

    begin
      # Get full month name, non-zero-padded date, and full year
      formatted_date = Date.strptime(enrollment_date, "%m-%d-%Y").strftime("%B %-d %Y")
      enrollment_string = name + " - " + formatted_date
    rescue ArgumentError
      enrollment_string = "Invalid Date"
    end

    return enrollment_string

  end

end
