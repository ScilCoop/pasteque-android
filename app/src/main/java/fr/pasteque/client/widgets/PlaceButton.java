package fr.pasteque.client.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageButton;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.R;
import fr.pasteque.client.models.Place;

/**
 * Created by svirch_n on 23/05/16
 * Last edited at 17:09.
 */
public class PlaceButton extends Button {

    private final Place place;

    public PlaceButton(Context context, Place place) {
        super(context);
        this.place = place;
    }

    public void rate(int width, int height) {
        float widthRate = width / Pasteque.getRestaurantMapWidth();
        float heightRate = height / Pasteque.getRestaurantMapHeight();
        setX(place.getX() * widthRate);
        setY(place.getY() * heightRate);
        getLayoutParams().width = (width / 8);
        getLayoutParams().height = (height / 8);
    }

    private void setOccupied() {
        this.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.avatar_default, 0);
    }

    public void update() {
        if (this.place.isOccupied()) {
            setOccupied();
        }
    }
}
