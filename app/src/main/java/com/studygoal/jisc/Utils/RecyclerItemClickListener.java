package com.studygoal.jisc.Utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Recycler Item Click Listener
 * <p>
 * Provides the handling of the view "Activity Feed". Displays push notifications and feed items.
 *
 * @author Therapy Box - Marjana Karzek
 * @version 1.5
 * @date 07.12.17
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

    private OnItemClickListener listener;
    private GestureDetector gestureDetector;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onLongItemClick(View view, int position);
    }

    public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
        this.listener = listener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null && RecyclerItemClickListener.this.listener != null) {
                    RecyclerItemClickListener.this.listener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
                }
            }
        });
    }

    /**
     * Captures the touch event.
     *
     * @param view view object
     * @param e    motion event
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && listener != null && gestureDetector.onTouchEvent(e)) {
            listener.onItemClick(childView, view.getChildAdapterPosition(childView));
            return true;
        }
        return false;
    }

    /**
     * Overridden to do nothing.
     *
     * @param view        unused parameter
     * @param motionEvent unused parameter
     */
    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        // do nothing
    }

    /**
     * Overridden to do nothing.
     *
     * @param disallowIntercept unused parameter
     */
    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // do nothing
    }

}
