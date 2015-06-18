How to test Pasteque Android
============================

So far, your root project must be compiled.

Then you compile & install your test with:
$ ant debug install

Finaly, run the test with ant:
$ ant test

If you don't need the device, we have a the package fr.pasteque.client.unit in an unit test purpose.
Compile and run all the classes in the package by doing:
$ ant compile junit

USAGE
=====

ant debug	:	https://github.com/ScilCoop/pasteque-android/edit/master/tests/README.txt#compile the tests

ant installd	:	install current compiled debug on the device

ant test	:	launch the test installed on the device

ant compile	:	compile unit test

ant junit	:	launch unit test

TREE
====

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
