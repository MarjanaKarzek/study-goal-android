package com.studygoal.jisc.Activities;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.CircleTransform;
import com.studygoal.jisc.Utils.GlideConfig.GlideApp;

import junit.framework.Assert;

/**
 * Trophies Details Activity
 * <p>
 * Displays details of selected Trophy.
 *
 * @author Therapy Box - Marcel C
 * @version 1.5
 * @date 14/01/16
 */
public class TrophyDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = TrophyDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DataManager.getInstance().isLandscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.layout_trophy_details);
        DataManager.getInstance().currActivity = this;
        Bundle bundle = getIntent().getExtras();

        String type = bundle.getString("type");
        String statement = bundle.getString("statement");

        TextView titleView = (TextView) findViewById(R.id.main_screen_title);
        titleView.setTypeface(DataManager.getInstance().myriadpro_regular);
        titleView.setText(bundle.getString("title"));

        TextView trophyDetailsText = (TextView) findViewById(R.id.trophy_details_text);
        trophyDetailsText.setTypeface(DataManager.getInstance().myriadpro_regular);
        trophyDetailsText.setText(statement);

        TextView trophyDetailsType = (TextView) findViewById(R.id.trophy_details_type);
        trophyDetailsType.setTypeface(DataManager.getInstance().myriadpro_regular);

        if (bundle.getString("type").contains("Silver")) {
            trophyDetailsType.setText(getString(R.string.silver));
        } else {
            trophyDetailsType.setText(getString(R.string.gold));
        }

        ImageView image = (ImageView) findViewById(R.id.trophy_details_image);

        String imageName = bundle.getString("image");

        Assert.assertNotNull(this);
        Assert.assertNotNull(imageName);

        GlideApp.with(this)
                .load(this.getResources().getIdentifier(imageName, "drawable", this.getPackageName()))
                .transform(new CircleTransform())
                .into(image);

        RelativeLayout closeButton = (RelativeLayout) findViewById(R.id.close_button);
        closeButton.setOnClickListener(this);

        assert type != null;
        if (type.equals("Gold")) {
            findViewById(R.id.circle).setBackground(ContextCompat.getDrawable(this, R.drawable.circle_gold));
            trophyDetailsType.setTextColor(Color.parseColor("#f19001"));
        }

    }

    @Override
    public void onClick(View v) {
        this.finish();
    }
}