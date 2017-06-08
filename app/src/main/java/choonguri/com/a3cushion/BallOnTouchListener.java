package choonguri.com.a3cushion;

import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * BallOnTouchListener
 * Created by Choonghyun Seo on 2017. 6. 2.
 */

public class BallOnTouchListener implements View.OnTouchListener {

    private RelativeLayout parentLayout;

    private float xDelta;
    private float yDelta;

    private RectF tableRect;

    public BallOnTouchListener(RelativeLayout parentLayout, RectF tableRect) {
        this.parentLayout = parentLayout;
        this.tableRect = tableRect;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xDelta = x - view.getX();
                yDelta = y - view.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                x -= xDelta;
                y -= yDelta;

                if (tableRect.contains(x, y)) {
                    view.setX(x);
                    view.setY(y);
                    view.invalidate();
                }
                break;
        }
        return true;
    }
}
