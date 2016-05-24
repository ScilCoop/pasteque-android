package fr.pasteque.client.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TabHost;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.R;
import fr.pasteque.client.RestaurantTicketSelect;
import fr.pasteque.client.data.Data;
import fr.pasteque.client.models.Floor;
import fr.pasteque.client.models.Place;
import fr.pasteque.client.widgets.FloorView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by svirch_n on 23/05/16
 * Last edited at 12:13.
 */
public class RestaurantTicketSelectFragment extends Fragment implements FloorView.FloorOnClickListener {

    private static String currentTabTag;
    private TabHost.OnTabChangeListener onTabChangedListener = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            currentTabTag = tabId;
        }
    };

    private List<FloorView> floorViewList = new LinkedList<>();
    private RestaurantTicketSelect restaurantTicketSelect;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.restaurant_ticket_fragment, container, false);
        TabHost tabHost = (TabHost) result.findViewById(R.id.tabhost);
        tabHost.setup();
        FrameLayout frameLayout = (FrameLayout) tabHost.findViewById(android.R.id.tabcontent);
        for (int i = 0; i < Data.Place.floors.size(); i++) {
            Floor floor = Data.Place.floors.get(i);
            FloorView floorView = new FloorView(getContext(), floor);
            floorViewList.add(floorView);
            floorView.setOnPlaceClickListener(this);
            final int id = i;
            floorView.setId(id);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            frameLayout.addView(floorView, params);
            TabHost.TabSpec tabSpec = tabHost.newTabSpec(floor.getId()).setIndicator(floor.getName());
            tabSpec.setContent(id);
            tabHost.addTab(tabSpec);
        }
        if (currentTabTag != null) {
            tabHost.setCurrentTabByTag(currentTabTag);
        }
        tabHost.setOnTabChangedListener(onTabChangedListener);
        return result;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RestaurantTicketSelect) {
            this.restaurantTicketSelect = (RestaurantTicketSelect) context;
        }
   }

    public void refreshView() {
        for (FloorView each: floorViewList) {
            each.update();
        }
    }

    @Override
    public void onClick(Floor floor, Place place) {
        if (this.restaurantTicketSelect != null) {
            this.restaurantTicketSelect.accessPlace(place);
        }
    }
}
