ant debug	:	compile the tests

ant installd	:	install current compiled debug on the device

ant test	:	launch the test installed on the device

the 'test' target is known by ant in the root project (parent folder)
It calls the 'test' target of the test project.
