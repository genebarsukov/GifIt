package codewrencher.gifit.helpers.async;
import android.view.MotionEvent;

import codewrencher.gifit.helpers.async.interfaces.GestureListenerWrapListener;

/**
 * Created by Gene on 12/26/2015.
 */
public class GestureListenerWrap extends android.view.GestureDetector.SimpleOnGestureListener {
    private static final int MAJOR_MOVE = 50;
    private GestureListenerWrapListener gesture_detector_listener;

    public void registerListener(GestureListenerWrapListener gesture_detector_listener) {
        this.gesture_detector_listener = gesture_detector_listener;
    }
    @Override
    public boolean onDown(MotionEvent event) {
        gesture_detector_listener.onDownDetected();
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        int dy = (int) (event2.getY() - event1.getY());
        int dx = (int) (event2.getX() - event1.getX());
        if (Math.abs(dy) > MAJOR_MOVE && Math.abs(velocityY) > Math.abs(velocityX)) {
            if (velocityY < 0) {
                gesture_detector_listener.onFlingUpDetected();
            } else if (velocityY > 0) {
                gesture_detector_listener.onFlingDownDetected();
            }
            return true;
        } else {
            return false;
        }
    }
}
