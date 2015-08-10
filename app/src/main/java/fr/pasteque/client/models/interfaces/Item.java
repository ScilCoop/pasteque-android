package fr.pasteque.client.models.interfaces;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.IOException;

/**
 * Created by nsvir on 10/08/15.
 * n.svirchevsky@gmail.com
 */
public interface Item {
    String getLabel();
    String getId();
    boolean hasImage();
    Bitmap getImage(Context ctx);
}
