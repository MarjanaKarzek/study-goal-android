package com.studygoal.jisc.Utils;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class PageControl extends LinearLayout {

    private int indicatorSize = 7;

    private Drawable activeDrawable;
    private Drawable inactiveDrawable;

    private ArrayList<ImageView> indicators;

    private int pageCount = 0;
    private int currentPage = 0;

    private Context context;
    private OnPageControlClickListener onPageControlClickListener = null;

    public PageControl(Context context) {
        super(context);
        this.context = context;
        initPageControl();
    }

    public PageControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onFinishInflate() {
        initPageControl();
    }

    public void initPageControl() {

        indicators = new ArrayList<>();

        activeDrawable = new ShapeDrawable();
        inactiveDrawable = new ShapeDrawable();

        activeDrawable.setBounds(0, 0, indicatorSize, indicatorSize);
        inactiveDrawable.setBounds(0, 0, indicatorSize, indicatorSize);

        Shape s1 = new OvalShape();
        s1.resize(indicatorSize, indicatorSize);

        Shape s2 = new OvalShape();
        s2.resize(indicatorSize, indicatorSize);

        int i[] = new int[2];
        i[0] = android.R.attr.textColorSecondary;
        i[1] = android.R.attr.textColorSecondaryInverse;
        TypedArray a = context.getTheme().obtainStyledAttributes(i);

        ((ShapeDrawable) activeDrawable).getPaint().setColor(a.getColor(0, Color.DKGRAY));
        ((ShapeDrawable) inactiveDrawable).getPaint().setColor(a.getColor(1, Color.LTGRAY));

        ((ShapeDrawable) activeDrawable).setShape(s1);
        ((ShapeDrawable) inactiveDrawable).setShape(s2);

        indicatorSize = (int) (indicatorSize * getResources().getDisplayMetrics().density);

        setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (onPageControlClickListener != null) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_UP:

                            if (PageControl.this.getOrientation() == LinearLayout.HORIZONTAL) {
                                if (event.getX() < (PageControl.this.getWidth() / 2)) //if on left of view
                                {
                                    if (currentPage > 0) {
                                        onPageControlClickListener.goBackwards();
                                    }
                                } else //if on right of view
                                {
                                    if (currentPage < (pageCount - 1)) {
                                        onPageControlClickListener.goForwards();
                                    }
                                }
                            } else {
                                if (event.getY() < (PageControl.this.getHeight() / 2)) //if on top half of view
                                {
                                    if (currentPage > 0) {
                                        onPageControlClickListener.goBackwards();
                                    }
                                } else //if on bottom half of view
                                {
                                    if (currentPage < (pageCount - 1)) {
                                        onPageControlClickListener.goForwards();
                                    }
                                }
                            }


                            return false;
                    }
                }
                return true;
            }
        });
    }

    /**
     * Set the drawable object for an active page indicator
     *
     * @param d The drawable object for an active page indicator
     */
    public void setActiveDrawable(Drawable d) {
        activeDrawable = d;

        indicators.get(currentPage).setBackgroundDrawable(activeDrawable);

    }

    /**
     * Return the current drawable object for an active page indicator
     *
     * @return Returns the current drawable object for an active page indicator
     */
    public Drawable getActiveDrawable() {
        return activeDrawable;
    }

    /**
     * Set the drawable object for an inactive page indicator
     *
     * @param d The drawable object for an inactive page indicator
     */
    public void setInactiveDrawable(Drawable d) {
        inactiveDrawable = d;

        for (int i = 0; i < pageCount; i++) {
            indicators.get(i).setBackgroundDrawable(inactiveDrawable);
        }

        indicators.get(currentPage).setBackgroundDrawable(activeDrawable);
    }

    /**
     * Return the current drawable object for an inactive page indicator
     *
     * @return Returns the current drawable object for an inactive page indicator
     */
    public Drawable getInactiveDrawable() {
        return inactiveDrawable;
    }

    /**
     * Set the number of pages this PageControl should have
     *
     * @param pageCount The number of pages this PageControl should have
     */
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
        for (int i = 0; i < pageCount; i++) {
            final ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(indicatorSize, indicatorSize);
            params.setMargins(indicatorSize / 2, indicatorSize, indicatorSize / 2, indicatorSize);
            imageView.setLayoutParams(params);
            imageView.setBackgroundDrawable(inactiveDrawable);

            indicators.add(imageView);
            addView(imageView);
        }
    }

    /**
     * Return the number of pages this PageControl has
     *
     * @return Returns the number of pages this PageControl has
     */
    public int getPageCount() {
        return pageCount;
    }

    /**
     * Set the current page the PageControl should be on
     *
     * @param currentPage The current page the PageControl should be on
     */
    public void setCurrentPage(int currentPage) {
        if (currentPage < pageCount) {
            indicators.get(this.currentPage).setBackgroundDrawable(inactiveDrawable);//reset old indicator
            indicators.get(currentPage).setBackgroundDrawable(activeDrawable);//set up new indicator
            this.currentPage = currentPage;
        }
    }

    /**
     * Return the current page the PageControl is on
     *
     * @return Returns the current page the PageControl is on
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Set the size of the page indicator drawables
     *
     * @param indicatorSize The size of the page indicator drawables
     */
    public void setIndicatorSize(int indicatorSize) {
        this.indicatorSize = indicatorSize;
        for (int i = 0; i < pageCount; i++) {
            indicators.get(i).setLayoutParams(new LayoutParams(this.indicatorSize, this.indicatorSize));
        }
    }

    /**
     * Return the size of the page indicator drawables
     *
     * @return Returns the size of the page indicator drawables
     */
    public int getIndicatorSize() {
        return indicatorSize;
    }

    /**
     * @author Jason Fry - jasonfry.co.uk
     *         <p>
     *         Interface definition for a callback to be invoked when a PageControl is clicked.
     */
    public interface OnPageControlClickListener {
        /**
         * Called when the PageControl should go forwards
         */
        public abstract void goForwards();

        /**
         * Called when the PageControl should go backwards
         */
        public abstract void goBackwards();
    }

    /**
     * Set the OnPageControlClickListener object for this PageControl
     *
     * @param onPageControlClickListener The OnPageControlClickListener you wish to set
     */
    public void setOnPageControlClickListener(OnPageControlClickListener onPageControlClickListener) {
        this.onPageControlClickListener = onPageControlClickListener;
    }

    /**
     * Return the OnPageControlClickListener that has been set on this PageControl
     *
     * @return Returns the OnPageControlClickListener that has been set on this PageControl
     */
    public OnPageControlClickListener getOnPageControlClickListener() {
        return onPageControlClickListener;
    }
}
