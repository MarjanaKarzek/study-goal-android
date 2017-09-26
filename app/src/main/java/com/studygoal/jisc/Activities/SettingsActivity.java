package com.studygoal.jisc.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.studygoal.jisc.Fragments.Friends.FriendsFragment;
import com.studygoal.jisc.Fragments.Settings.HomeScreenFragment;
import com.studygoal.jisc.Fragments.Settings.LanguageScreenFragment;
import com.studygoal.jisc.Fragments.Settings.TrophiesFragment;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.R;
import com.studygoal.jisc.SettingsLandscape.ListMenuAdapter;
import com.studygoal.jisc.SettingsLandscape.ProfileFragment;
import com.studygoal.jisc.Utils.Connection.ConnectionHandler;

import java.io.ByteArrayOutputStream;

public class SettingsActivity extends AppCompatActivity {

    public TextView fragmentTitle;
    private ProfileFragment fragment;
    private ListView listMenu;
    private ListMenuAdapter[] adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_settings);
        DataManager.getInstance().currActivity = this;
        findViewById(R.id.main_screen_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        adapter = new ListMenuAdapter[]{new ListMenuAdapter(this, 0)};
        fragment = new ProfileFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();

        ((TextView) findViewById(R.id.settings_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
        (fragmentTitle = (TextView) findViewById(R.id.fragment_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
        listMenu = (ListView) findViewById(R.id.list_menu);
        listMenu.setAdapter(adapter[0]);
        listMenu.setOnItemClickListener(new SettingsItemClickListener());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (intent == null) {
            return;
        }

        if (requestCode == 100) {
            final String imagePath = getRealPathFromURI(fragment.imageUri);
            showProgressBar(null);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (NetworkManager.getInstance().updateProfileImage(imagePath)) {
                        SettingsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fragment.refresh_image();
                                hideProgressBar();
                            }
                        });
                    }
                    SettingsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressBar();
                        }
                    });
                }
            }).start();

        } else if (requestCode == 101) {
            Uri pickedImage = intent.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = this.getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();

            final String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            showProgressBar(null);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (NetworkManager.getInstance().updateProfileImage(imagePath)) {
                        SettingsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fragment.refresh_image();
                                hideProgressBar();
                            }
                        });
                    }
                    SettingsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressBar();
                        }
                    });
                }
            }).start();
        }
    }

    private void sendBugReportEmail() {
        if(ConnectionHandler.isConnected(this)) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "learning.analytics@jisc.ac.uk", null));
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Bug/Feature idea " + DataManager.getInstance().institution);
            emailIntent.putExtra(Intent.EXTRA_TEXT, "+ Bug or feature?\n" +
                    "+ Which parts of the app are affected (feeds, stats, logs, targets, other)??\n" +
                    "+ Further Detail: ");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } else {
            ConnectionHandler.showNoInternetConnectionSnackbar();
        }
    }

    private void showProgressBar(@Nullable String text) {
        findViewById(R.id.blackout).setVisibility(View.VISIBLE);
        findViewById(R.id.blackout).setOnClickListener(null);
    }

    private void hideProgressBar() {
        findViewById(R.id.blackout).setVisibility(View.INVISIBLE);
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getRealPathFromURI(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private class SettingsItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 5) {
                sendBugReportEmail();
            } else {
                adapter[0] = new ListMenuAdapter(SettingsActivity.this, position);
                listMenu.setAdapter(adapter[0]);

                Fragment destination = null;
                switch (position) {
                    case 0: {
                        fragment = new ProfileFragment();
                        destination = fragment;
                        break;
                    }
                    case 1: {
                        destination = new FriendsFragment();
                        break;
                    }
                    case 2: {
                        destination = new HomeScreenFragment();
                        break;
                    }
                    case 3: {
                        destination = new TrophiesFragment();
                        break;
                    }
                    case 4: {
                        destination = new LanguageScreenFragment();
                        break;
                    }
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, destination)
                        .commit();
            }
        }
    }
}

