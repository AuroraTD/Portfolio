#!/bin/bash
FAIL=0

# make a fresh copy of the target programs
make clean
make
if [ $? -ne 0 ]; then
  echo "**** Make (compilation) FAILED"
  FAIL=1
fi

# Function to run the program against a test case and check
# its output and exit status for correct behavior
runTest() {
  TESTNO=$1
  ESTATUS=$2

  rm -f output.txt

  echo "Test $TESTNO: ./daily < input-$TESTNO.txt > output.txt"
  ./daily < input-$TESTNO.txt > output.txt
  STATUS=$?

  # Make sure the program exited with the right exit status.
  if [ $STATUS -ne $ESTATUS ]
  then
      echo "**** Test $TESTNO FAILED - incorrect exit status. Expected: $ESTATUS Got: $STATUS"
      FAIL=1
      return 1
  fi

  # Make sure the output matches the expected output.
  diff -q expected-$TESTNO.txt output.txt >/dev/null 2>&1
  if [ $? -ne 0 ]
  then
      echo "**** Test $TESTNO FAILED - stdout output didn't match expected"
      FAIL=1
      return 1
  fi

  echo "Test $TESTNO PASS"
  return 0
}

# Run each test.
runTest 1 0
runTest 2 0
runTest 3 0
runTest 4 0
runTest 5 0
runTest 6 0
runTest 7 0
runTest 8 0
runTest 9 0
runTest 10 0
runTest 11 0
runTest 12 0
runTest 13 0
runTest 14 0

if [ $FAIL -ne 0 ]; then
  echo "FAILING TESTS!"
  exit 13
else 
  echo "Tests successful"
  exit 0
fi
