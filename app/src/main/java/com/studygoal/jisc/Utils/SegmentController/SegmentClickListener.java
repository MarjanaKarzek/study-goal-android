package com.studygoal.jisc.Utils.SegmentController;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.studygoal.jisc.R;

import java.util.ArrayList;


/**
 * Created by Marjana-Tbox on 25/09/17.
 */

public class SegmentClickListener implements View.OnClickListener {

    private ViewFlipper viewFlipper;
    private ArrayList<TextView> segments;
    private int previousPosition;
    private Context context;

    public SegmentClickListener(ViewFlipper viewFlipper, ArrayList<TextView> segments, Context context, int firstSelection) {
        this.viewFlipper = viewFlipper;
        this.segments = segments;
        this.context = context;
        this.previousPosition = firstSelection;
    }

    @Override
    public void onClick(View view) {
        //deactivate previous segment
        segments.get(previousPosition).setBackground(null);
        segments.get(previousPosition).setBackgroundColor(Color.TRANSPARENT);
        segments.get(previousPosition).setTextColor(Color.parseColor("#3792ef"));

        //activate current segment
        int position = segments.indexOf(view);
        if (position == segments.size() - 1) {
            Drawable activeDrawable = ContextCompat.getDrawable(context, R.drawable.round_corners_segmented_active_right);
            segments.get(position).setBackground(activeDrawable);
            segments.get(position).setTextColor(Color.WHITE);
        } else if (position != 0) {
            Drawable activeDrawable = ContextCompat.getDrawable(context, R.drawable.round_corners_segmented_active_middle);
            segments.get(position).setBackground(activeDrawable);
            segments.get(position).setTextColor(Color.WHITE);
        } else {
            Drawable activeDrawable = ContextCompat.getDrawable(context, R.drawable.round_corners_segmented_active);
            segments.get(position).setBackground(activeDrawable);
            segments.get(position).setTextColor(Color.WHITE);
        }

        if (viewFlipper != null) {
            viewFlipper.setDisplayedChild(position);
        }

        previousPosition = position;
    }
}
