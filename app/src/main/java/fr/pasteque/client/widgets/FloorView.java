package fr.pasteque.client.widgets;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.R;
import fr.pasteque.client.models.Floor;
import fr.pasteque.client.models.Place;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by svirch_n on 23/05/16
 * Last edited at 16:08.
 */
public class FloorView extends RelativeLayout {

    private final Floor floor;
    private FloorOnClickListener listener;
    private List<PlaceButton> buttonList = new LinkedList<>();

    public FloorView(Context context, Floor floor) {
        super(context);
        this.floor = floor;
        addChildren();
    }

    private void addChildren() {
        for (Place place : floor.getPlaces()) {
            PlaceButton button = createButton(place);
            addButton(button);
        }
    }

    private void addButton(PlaceButton button) {
        addView(button);
        this.buttonList.add(button);
    }

    private PlaceButton createButton(final Place place) {
        PlaceButton button = new PlaceButton(getContext(), place);
        button.setText(place.getName());
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FloorView.this.listener != null) {
                    FloorView.this.listener.onClick(FloorView.this.floor, place);
                }
            }
        });
        button.update();
        return button;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                ((PlaceButton) getChildAt(i)).rate(r - l, b - t);
            }
        }
        super.onLayout(changed, l, t, r, b);
    }

    public void setOnPlaceClickListener(FloorOnClickListener listener) {
        this.listener = listener;
    }

    public void update() {
        for (PlaceButton each:buttonList) {
            each.update();
        }
    }

    public interface FloorOnClickListener {
        void onClick(Floor floor, Place place);
    }
}
