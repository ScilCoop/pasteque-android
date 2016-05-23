package fr.pasteque.client.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.pasteque.client.R;

/**
 * Created by svirch_n on 23/05/16
 * Last edited at 12:13.
 */
public class RestaurantTicketSelectFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.restaurant_ticket_fragment, container, false);
    }
}
