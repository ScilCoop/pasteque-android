/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.pasteque.client;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class fr.pasteque.client.StartTest \
 * fr.pasteque.client.tests/android.test.InstrumentationTestRunner
 */
public class StartTest extends ActivityInstrumentationTestCase2<Start> {

    public StartTest() {
        super("fr.pasteque.client", Start.class);
    }

}