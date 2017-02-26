package me.saket.dank.widgets;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.views.GestureImageView;

/**
 * This wrapper exists so that we can easily change libraries in the future.
 * It has happened once so far and can happen again.
 */
public class ZoomableImageView extends GestureImageView {

    private GestureDetector gestureDetector;

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        getController().setOnGesturesListener(new GestureController.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent event) {
                performClick();
                return true;
            }
        });

        // Bug workarounds: GestureImageView doesn't request parent ViewGroups to stop intercepting touch
        // events when it starts consuming them to zoom.
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                getParent().requestDisallowInterceptTouchEvent(true);
                return super.onDoubleTapEvent(e);
            }
        });

        getController().addOnStateChangeListener(new GestureController.OnStateChangeListener() {
            public float lastZoom = -1;

            @Override
            public void onStateChanged(State state) {
                if (lastZoom == -1) {
                    lastZoom = state.getZoom();
                }

                // Overscroll only when zooming in.
                boolean isZoomingIn = state.getZoom() > lastZoom;
                if (isZoomingIn) {
                    getController().getSettings().setOverzoomFactor(2f);
                } else if (state.getZoom() < 1f) {
                    getController().getSettings().setOverzoomFactor(1f);
                }
            }

            @Override
            public void onStateReset(State oldState, State newState) {

            }
        });
    }

    public void setGravity(int gravity) {
        getController().getSettings().setGravity(gravity);
    }

    public int getImageHeight() {
        return getController().getSettings().getImageH();
    }

    public float getZoomedImageHeight() {
        return (float) getImageHeight() * getZoom();
    }

    public float getZoom() {
        return getController().getState().getZoom();
    }

    public boolean canPanUpwardsAnymore() {
        return getController().getState().getY() != 0f;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN && event.getPointerCount() == 2) {
            // Two-finger zoom is probably going to start. Disallow parent from intercepting this gesture.
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        return super.onTouchEvent(event);
    }

}