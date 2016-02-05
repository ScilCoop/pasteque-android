package fr.pasteque.client.activities.test;

import android.test.ActivityInstrumentationTestCase2;
import org.junit.Test;


/**
 * Created by svirch_n on 05/02/16.
 */
public class MPopActivityTest extends ActivityInstrumentationTestCase2<MPopActivity> {

    MPopActivity activity;

    public MPopActivityTest() {
        super(MPopActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
    }

    @Test
    public void testHasActivity() {
        assertNotNull(activity);
    }
}