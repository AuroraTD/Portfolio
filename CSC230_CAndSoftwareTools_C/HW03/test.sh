#!/bin/bash
FAIL=0

# make a fresh copy of the target programs
make clean
make
if [ $? -ne 0 ]; then
  echo "**** Make (compilation) FAILED"
  FAIL=1
fi

# Function to run the cross program against a test case and check
# its output and exit status for correct behavior
testCross() {
  TESTNO=$1
  WORDFILE=$2
  ESTATUS=$3

  rm -f output.txt stderr.txt

  echo "Cross test $TESTNO: ./cross $WORDFILE < input-cross$TESTNO.txt > output.txt 2> stderr.txt"
  ./cross $WORDFILE < input-cross$TESTNO.txt > output.txt 2> stderr.txt
  STATUS=$?

  # Make sure the program exited with the right exit status.
  if [ $STATUS -ne $ESTATUS ]
  then
      echo "**** Cross test $TESTNO FAILED - incorrect exit status. Expected: $ESTATUS Got: $STATUS"
      FAIL=1
      return 1
  fi

  # On error tests, make sure we got the right error otuput.
  if [ $ESTATUS -ne 0 ]
  then
      diff -q stderr-cross$TESTNO.txt stderr.txt >/dev/null 2>&1
      if [ $? -ne 0 ]
      then
	  echo "**** Cross test $TESTNO FAILED - error output didn't match exptected"
	  FAIL=1
	  return 1
      fi
  fi

  # Make sure the output matches the expected output.
  diff -q expected-cross$TESTNO.txt output.txt >/dev/null 2>&1
  if [ $? -ne 0 ]
  then
      echo "**** Cross test $TESTNO FAILED - stdout output didn't match expected"
      FAIL=1
      return 1
  fi

  echo "Cross test $TESTNO PASS"
  return 0
}

# Function to run the connect program against a test case and check
# its output and exit status for correct behavior
testConnect() {
  TESTNO=$1
  ESTATUS=$2

  rm -f output.txt stderr.txt

  echo "Connect test $TESTNO: ./connect < input-con$TESTNO.txt > output.txt 2> stderr.txt"
  ./connect < input-con$TESTNO.txt > output.txt 2> stderr.txt
  STATUS=$?

  # Make sure the program exited with the right exit status.
  if [ $STATUS -ne $ESTATUS ]
  then
      echo "**** Connect test $TESTNO FAILED - incorrect exit status. Expected: $ESTATUS Got: $STATUS"
      FAIL=1
      return 1
  fi

  # On error tests, make sure we got the right error otuput.
  if [ $ESTATUS -ne 0 ]
  then
      diff -q stderr-con$TESTNO.txt stderr.txt >/dev/null 2>&1
      if [ $? -ne 0 ]
      then
	  echo "**** Connect test $TESTNO FAILED - error output didn't match exptected"
	  FAIL=1
	  return 1
      fi
  fi

  # Make sure the output matches the expected output.
  diff -q expected-con$TESTNO.txt output.txt >/dev/null 2>&1
  if [ $? -ne 0 ]
  then
      echo "**** Connect test $TESTNO FAILED - stdout output didn't match expected"
      FAIL=1
      return 1
  fi

  echo "Connect test $TESTNO PASS"
  return 0
}

# Test the cross program
testCross 1 words-small.txt 0
testCross 2 words-small.txt 0
testCross 3 words-med.txt 0
testCross 4 words-large.txt 0
testCross 5 words-med.txt 0
testCross 6 words-bad6.txt 1
testCross 7 words-bad7.txt 1
testCross 8 words-bad8.txt 1

# Test the connect program.
testConnect 1 0
testConnect 2 0
testConnect 3 0
testConnect 4 0
testConnect 5 0
testConnect 6 1
testConnect 7 0

if [ $FAIL -ne 0 ]; then
  echo "FAILING TESTS!"
  exit 13
else 
  echo "Tests successful"
  exit 0
fi
