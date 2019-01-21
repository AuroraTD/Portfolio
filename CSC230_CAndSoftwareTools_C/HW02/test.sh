#!/bin/bash
FAIL=0

# make a fresh copy of the target programs
make clean
make
if [ $? -ne 0 ]; then
  echo "**** Make (compilation) FAILED"
  FAIL=1
fi

# Function to run the dent program against a test case and check
# its output and exit status for correct behavior
testDent() {
  TEST_NO=$1
  ESTATUS=$2

  rm -f output.txt

  echo "Dent test $TEST_NO: ./dent < input_d$TEST_NO.txt > output.txt"
  ./dent < input_d$TEST_NO.txt > output.txt
  STATUS=$?

  # Make sure the program exited with the right exit status.
  if [ $STATUS -ne $ESTATUS ]
  then
      echo "**** Dent test $TEST_NO FAILED - incorrect exit status. Expected: $ESTATUS Got: $STATUS"
      FAIL=1
      return 1
  fi

  # Make sure the output matches the expected output.
  diff -q expected_d$TEST_NO.txt output.txt >/dev/null 2>&1
  if [ $? -ne 0 ]
  then
      echo "**** Dent test $TEST_NO FAILED - output didn't match the expected output"
      FAIL=1
      return 1
  fi

  echo "Dent test $TEST_NO PASS"
  return 0
}

# Function to run the pie program against a test case and check its
# output and exit status for correct behavior
testPie() {
  TEST_NO=$1
  ESTATUS=$2

  # Name of the output file depends on whether this is an error test case.
  if [ $ESTATUS -eq 0 ]
  then
      EXPECTED="expected_p$TEST_NO.ppm"
      OUTPUT="output.ppm"
  else
      EXPECTED="expected_p$TEST_NO.txt"
      OUTPUT="output.txt"
  fi

  rm -f "$OUTPUT"

  echo "Pie test $TEST_NO: ./pie < input_p$TEST_NO.txt > $OUTPUT"
  ./pie < input_p$TEST_NO.txt > $OUTPUT
  STATUS=$?

  # Make sure the program exited with the right exit status.
  if [ $STATUS -ne $ESTATUS ]
  then
      echo "**** Pie test $TEST_NO FAILED - incorrect exit status. Expected: $ESTATUS Got: $STATUS"
      FAIL=1
      return 1
  fi

  # Make sure the output matches the expected output.
  diff -q $EXPECTED $OUTPUT >/dev/null 2>&1
  if [ $? -ne 0 ]
  then
      echo "**** Pie test $TEST_NO FAILED - output didn't match the expected output"
      FAIL=1
      return 1
  fi

  echo "Pie test $TEST_NO PASS"
  return 0
}

# Test the dent program
testDent 1 0
testDent 2 0
testDent 3 0
testDent 4 0
testDent 5 0
testDent 6 100
testDent 7 100

# Test the pie program.
testPie 1 0
testPie 2 0
testPie 3 0
testPie 4 0
testPie 5 100
testPie 6 100
testPie 7 100

if [ $FAIL -ne 0 ]; then
  echo "FAILING TESTS!"
  exit 13
else 
  echo "Tests successful"
  exit 0
fi
