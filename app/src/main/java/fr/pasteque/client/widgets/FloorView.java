package fr.pasteque.client.widgets;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.models.Floor;
import fr.pasteque.client.models.Place;

/**
 * Created by svirch_n on 23/05/16
 * Last edited at 16:08.
 */
public class FloorView extends RelativeLayout {

    private final Floor floor;
    private FloorOnClickListener listener;

    public FloorView(Context context, Floor floor) {
        super(context);
        this.floor = floor;
        addChildren();
    }

    private void addChildren() {
        for (Place place : floor.getPlaces()) {
            Button button = createButton(place);
            addView(button);
        }
    }

    private Button createButton(final Place place) {
        PlaceButton button = new PlaceButton(getContext());
        button.setPlaceX(place.getX());
        button.setPlaceY(place.getY());
        button.setText(place.getName());
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FloorView.this.listener != null) {
                    FloorView.this.listener.onClick(FloorView.this.floor, place);
                }
            }
        });
        return button;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((PlaceButton)getChildAt(i)).rate(r - l, b - t);
        }
        super.onLayout(changed, l, t, r, b);
    }

    public void setOnPlaceClickListener(FloorOnClickListener listener) {
        this.listener = listener;
    }

    public interface FloorOnClickListener {
        void onClick(Floor floor, Place place);
    }
}
