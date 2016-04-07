package fr.pasteque.client.viewer;


import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by svirch_n on 07/04/16
 * Last edited at 17:01.
 */
public class MainActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.main);
    }
}
