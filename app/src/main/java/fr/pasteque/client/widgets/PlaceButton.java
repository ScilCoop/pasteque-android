package fr.pasteque.client.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageButton;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.R;

/**
 * Created by svirch_n on 23/05/16
 * Last edited at 17:09.
 */
public class PlaceButton extends Button {

    private int placeX;
    private int placeY;

    public PlaceButton(Context context) {
        super(context);
    }

    public void setPlaceX(int placeX) {
        this.placeX = placeX;
    }

    public void setPlaceY(int placeY) {
        this.placeY = placeY;
    }

    public void rate(int width, int height) {
        float widthRate = width / Pasteque.getRestaurantMapWidth();
        float heightRate = height / Pasteque.getRestaurantMapHeight();
        setX(placeX * widthRate);
        setY(placeY * heightRate);
        getLayoutParams().width = (width / 8);
        getLayoutParams().height = (height / 8);
    }

    public void setOccupied() {
        this.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.avatar_default, 0);
    }
}
