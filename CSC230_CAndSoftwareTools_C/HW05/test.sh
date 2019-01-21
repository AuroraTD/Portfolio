#!/bin/bash
FAIL=0

# make a fresh copy of the target programs
make clean
make
if [ $? -ne 0 ]; then
  echo "**** Make (compilation) FAILED"
  FAIL=1
fi

# Check the results of an execution of squeeze.
# Splitting this out from the test function makes it easier
# to handle some error tests.
checkSqueeze() {
  TESTNO=$1
  ESTATUS=$2

  # Make sure the program exited with the right exit status.
  if [ $STATUS -ne $ESTATUS ]; then
      echo "**** Test failed - incorrect exit status. Expected: $ESTATUS Got: $STATUS"
      FAIL=1
      return 1
  fi

  # Check different things, depending on whether it's successful.
  if [ -s stdout.txt ]; then
      echo "**** Test failed - program should print anything to standard output"
      FAIL=1
      return 1
  fi
      
  # Check different things, depending on whether it's successful.
  if [ $ESTATUS -eq 0 ]; then
      # Make sure the output matches the expected output.
      diff -q compressed-$TESTNO.bin compressed.bin >/dev/null 2>&1
      if [ $? -ne 0 ]; then
	  echo "**** Test FAILED - compressed output doesn't match expected"
	  FAIL=1
	  return 1
      fi
      
      if [ -s stderr.txt ]; then
	  echo "**** Test FAILED - shouldn't have printed any error output"
	  FAIL=1
	  return 1
      fi
  else
      # Make sure the output matches the expected output.
      diff -q stderr-$TESTNO.txt stderr.txt >/dev/null 2>&1
      if [ $? -ne 0 ]; then
	  echo "**** Test FAILED - printed the wrong error message"
	  FAIL=1
	  return 1
      fi
  fi

  echo "Test $TESTNO PASS"
  return 0
}

# Test the compression program.
testSqueeze() {
  rm -f compressed.bin stdout.txt stderr.txt

  echo "Test $1: ./squeeze input-$1.txt compressed.bin > stdout.txt 2> stderr.txt"
  ./squeeze input-$1.txt compressed.bin > stdout.txt 2> stderr.txt
  STATUS=$?

  checkSqueeze "$1" "$2"
}

# Check the results of an execution of unsqueeze.  Same idea as with
# checkSqueeze
checkUnsqueeze() {
  TESTNO=$1
  ESTATUS=$2

  # Make sure the program exited with the right exit status.
  if [ $STATUS -ne $ESTATUS ]; then
      echo "**** Test failed - incorrect exit status. Expected: $ESTATUS Got: $STATUS"
      FAIL=1
      return 1
  fi

  # Check different things, depending on whether it's successful.
  if [ -s stdout.txt ]; then
      echo "**** Test failed - program should print anything to standard output"
      FAIL=1
      return 1
  fi
      
  # Check different things, depending on whether it's successful.
  if [ $ESTATUS -eq 0 ]; then
      # The file we get back should match the corresponding input.
      diff -q input-$TESTNO.txt output.txt >/dev/null 2>&1
      if [ $? -ne 0 ]; then
	  echo "**** Test FAILED - uncompressed output doesn't match expected"
	  FAIL=1
	  return 1
      fi
      
      if [ -s stderr.txt ]; then
	  echo "**** Test FAILED - shouldn't have printed any error output"
	  FAIL=1
	  return 1
      fi
  else
      # Make sure the output matches the expected output.
      diff -q stderr-$TESTNO.txt stderr.txt >/dev/null 2>&1
      if [ $? -ne 0 ]; then
	  echo "**** Test FAILED - printed the wrong error message"
	  FAIL=1
	  return 1
      fi
  fi

  echo "Test $TESTNO PASS"
  return 0
}

 # Test the decompression program.
testUnsqueeze() {
  rm -f output.txt stdout.txt stderr.txt

  echo "Test $1: ./unsqueeze compressed-$1.bin output.txt > stdout.txt 2> stderr.txt"
  ./unsqueeze compressed-$1.bin output.txt > stdout.txt 2> stderr.txt
  STATUS=$?

  checkUnsqueeze "$1" "$2"
}

# Test the squeeze program
echo
echo "Testing squeeze"
testSqueeze 1 0
testSqueeze 2 0
testSqueeze 3 0
testSqueeze 4 0
testSqueeze 5 0
testSqueeze 6 0
testSqueeze 7 0
testSqueeze 8 0

testSqueeze 9 1

# invalid command-line arguments.
rm -f output.bin stdout.txt
echo "Test 10: ./squeeze a > stdout.txt 2> stderr.txt"
./squeeze a > stdout.txt 2> stderr.txt
STATUS=$?
checkSqueeze 10 1

# Test the unsqueeze program
echo
echo "Testing unsqueeze"
testUnsqueeze 1 0
testUnsqueeze 2 0
testUnsqueeze 3 0
testUnsqueeze 4 0
testUnsqueeze 5 0
testUnsqueeze 6 0
testUnsqueeze 7 0
testUnsqueeze 8 0

testUnsqueeze 11 1

# invalid command-line arguments.
rm -f output.bin stdout.txt
echo "Test 12: ./unsqueeze a b c > stdout.txt 2> stderr.txt"
./unsqueeze a b c > stdout.txt 2> stderr.txt
STATUS=$?
checkUnsqueeze 12 1

if [ $FAIL -ne 0 ]; then
  echo "FAILING TESTS!"
  exit 13
else 
  echo "Tests successful"
  exit 0
fi
