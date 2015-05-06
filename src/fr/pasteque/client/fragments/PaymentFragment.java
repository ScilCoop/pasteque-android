package fr.pasteque.client.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.pasteque.client.R;

public class PaymentFragment extends ViewPageFragment {

    public static PaymentFragment newInstance(int pageNumber) {
        PaymentFragment frag = new PaymentFragment();
        ViewPageFragment.initPageNumber(pageNumber, frag);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.payment_zone, container, false);
        return layout;
    }
}
