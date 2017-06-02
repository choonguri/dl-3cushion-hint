package choonguri.com.a3cushion;

import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * BallOnTouchListener
 * Created by Choonghyun Seo on 2017. 6. 2.
 */

public class BallOnTouchListener implements View.OnTouchListener {

    private final RelativeLayout parentLayout;
    private int xDelta;
    private int yDelta;

    public BallOnTouchListener(RelativeLayout parentLayout) {
        this.parentLayout = parentLayout;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                xDelta = X - lParams.leftMargin;
                yDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view
                        .getLayoutParams();
                layoutParams.leftMargin = X - xDelta;
                layoutParams.topMargin = Y - yDelta;
//                layoutParams.rightMargin = 250;
//                layoutParams.bottomMargin = 250;
                view.setLayoutParams(layoutParams);
                break;
        }
        parentLayout.invalidate();
        return true;
    }
}
