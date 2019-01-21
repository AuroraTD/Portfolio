To install cpplint from PyPI, run:
use $ pip install cpplint

Clone the source code of mySQL, mongoDB and postgreSQL from Github.

Use bash to locate the path of source code folder, then run:
$ cpplint --linelength=120 --counting=detailed $( find . -name "*.h" -or -name "*.cc" | grep -vE "^./build/" )