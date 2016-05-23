package fr.pasteque.client.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by svirch_n on 23/05/16
 * Last edited at 12:41.
 */
public class FloorView extends View {

    private Paint paint;
    private Rect rectangle;

    public FloorView(Context context) {
        super(context);
        init();
    }

    public FloorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FloorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(0xff000000);
        rectangle = new Rect(10, 10, 100, 100);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(rectangle, paint);
    }
}
