ant debug	:	compile the tests

ant installd	:	install current compiled debug on the device

ant test	:	launch the test installed on the device

ant compile	:	compile unit test

ant junit	:	launch unit test

Test tree looks like this:
.
├── gen (temp)
├── bin (temp)
├── classes (temp: hold unit test)
├── reports (temp: hold test results)
├── libs
├── res
└── src
    └── fr
        └── pasteque
            └── client
               ├── test (unit test running with android instrumentation [debug install test])
	       └── unit (unit test running with junit [compile junit])
