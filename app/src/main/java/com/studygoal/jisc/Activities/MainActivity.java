package com.studygoal.jisc.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.util.Log;
import com.lb.auto_fit_textview.AutoResizeTextView;
import com.studygoal.jisc.Adapters.DrawerAdapter;
import com.studygoal.jisc.AppCore;
import com.studygoal.jisc.Constants;
import com.studygoal.jisc.Fragments.ActivityFeed.FeedFragment;
import com.studygoal.jisc.Fragments.Checkin.CheckInFragment;
import com.studygoal.jisc.Fragments.Friends.FriendsFragment;
import com.studygoal.jisc.Fragments.Log.LogActivityHistoryFragment;
import com.studygoal.jisc.Fragments.Log.LogNewActivityFragment;
import com.studygoal.jisc.Fragments.Settings.SettingsFragment;
import com.studygoal.jisc.Fragments.Stats.StatsAppUsageFragment;
import com.studygoal.jisc.Fragments.Stats.StatsAttainmentFragment;
import com.studygoal.jisc.Fragments.Stats.StatsAttendanceFragment;
import com.studygoal.jisc.Fragments.Stats.StatsEventAttendanceFragment;
import com.studygoal.jisc.Fragments.Stats.StatsLeaderBoardFragment;
import com.studygoal.jisc.Fragments.Stats.StatsPointsFragment;
import com.studygoal.jisc.Fragments.Stats.StatsVLEActivityFragment;
import com.studygoal.jisc.Fragments.Target.AddTargetFragment;
import com.studygoal.jisc.Fragments.Target.TargetFragment;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Managers.xApi.XApiManager;
import com.studygoal.jisc.Models.CurrentUser;
import com.studygoal.jisc.Models.Module;
import com.studygoal.jisc.Models.ReceivedRequest;
import com.studygoal.jisc.Models.RunningActivity;
import com.studygoal.jisc.Preferences;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Services.Syncronize;
import com.studygoal.jisc.Utils.CircleTransform;
import com.studygoal.jisc.Utils.Connection.ConnectionHandler;
import com.studygoal.jisc.Utils.Event.EventReloadImage;
import com.studygoal.jisc.Utils.GlideConfig.GlideApp;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Locale;

public class MainActivity extends FragmentActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int CAMERA_REQUEST_CODE = 100;

    public DrawerLayout drawer;
    public RelativeLayout friend, settings, addTarget, send, timer, back;
    private SettingsFragment settingsFragment;
    private LogActivityHistoryFragment logFragment;
    public FeedFragment feedFragment;
    public boolean isLandscape = DataManager.getInstance().isLandscape;
    private int selectedPosition;
    private ListView navigationView;
    public DrawerAdapter adapter;
    private View menu, blackout;
    public boolean displaySingleTarget = false;

    private Context context;
    private int statOpenedNum = 4;
    //statOpenedNum should be variable depending on whether or not attendance is being shown. This is a temp fix only.

    public String mCurrentPhotoPath;
    private int backpressed = 0;
    private int lastSelected = 1;

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.getInstance().checkForbidden = true;

        try {
            GlideApp.with(DataManager.getInstance().mainActivity)
                    .load(NetworkManager.getInstance().host + DataManager.getInstance().user.profile_pic)
                    .transform(new CircleTransform(DataManager.getInstance().mainActivity))
                    .into(DataManager.getInstance().mainActivity.adapter.profile_pic);
        } catch (Exception ignored) {
        }
    }

    public void refreshDrawer() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread thread = new Thread(() -> {
            NetworkManager.getInstance().getAppUsage(null, null);
            if (!DataManager.getInstance().sessionLogged) {
                android.util.Log.d(TAG, "onCreate: Session about to get logged");
                NetworkManager.getInstance().updateAppUsage("" + (Integer.valueOf(DataManager.getInstance().appUsageData.sessions) + 1),
                        DataManager.getInstance().appUsageData.activities,
                        DataManager.getInstance().appUsageData.setTargets,
                        DataManager.getInstance().appUsageData.metTargets,
                        DataManager.getInstance().appUsageData.failedTargets);
                DataManager.getInstance().sessionLogged = true;
            }
        });
        thread.start();

        Log.setEnabled(true);
        isLandscape = DataManager.getInstance().isLandscape;
        DataManager.getInstance().checkForbidden = true;
        super.onCreate(savedInstanceState);
        DataManager.getInstance().currActivity = this;
        if (getResources().getBoolean(R.bool.landscape_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            isLandscape = true;
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isLandscape = false;
        }

        if (DataManager.isTestBuild)
            CrashManager.register(this, "ff42f3b167f44d7e87edf69ffc1c7cbb", new CrashManagerListener() {
                public boolean shouldAutoUploadCrashes() {
                    return true;
                }
            });

        setContentView(R.layout.activity_layout_main);

        blackout = findViewById(R.id.blackout);

        back = (RelativeLayout) findViewById(R.id.main_screen_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        timer = (RelativeLayout) findViewById(R.id.main_screen_running);
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        send = (RelativeLayout) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_postfeed) + "</font>"));
                    alertDialogBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    return;
                }

                if (feedFragment != null) {
                    feedFragment.post();
                }
            }
        });

        friend = (RelativeLayout) findViewById(R.id.main_screen_friend);
        friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v.getTag() != null && v.getTag().equals("from_list")) {

                    v.setTag("");
                    final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.layout_dialog_confirmation);
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    if (DataManager.getInstance().mainActivity.isLandscape) {
                        DisplayMetrics displaymetrics = new DisplayMetrics();
                        DataManager.getInstance().mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                        int width = (int) (displaymetrics.widthPixels * 0.45);

                        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                        params.width = width;
                        dialog.getWindow().setAttributes(params);
                    }

                    ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
                    ((TextView) dialog.findViewById(R.id.dialog_title)).setText("");

                    ((TextView) dialog.findViewById(R.id.dialog_message)).setTypeface(DataManager.getInstance().myriadpro_regular);
                    ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.do_you_want_to_respond_now);

                    ((TextView) dialog.findViewById(R.id.dialog_no_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
                    ((TextView) dialog.findViewById(R.id.dialog_no_text)).setText(R.string.no);

                    ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
                    ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setText(R.string.yes);

                    dialog.findViewById(R.id.dialog_ok).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FriendsFragment friendsFragment = new FriendsFragment();

                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.main_fragment, friendsFragment)
                                    .addToBackStack(null)
                                    .commit();

                            dialog.dismiss();
                        }
                    });
                    dialog.findViewById(R.id.dialog_no).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else {
                    FriendsFragment friendsFragment = new FriendsFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment, friendsFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
        settings = (RelativeLayout) findViewById(R.id.main_screen_settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLandscape) {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                } else {
                    settingsFragment = new SettingsFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment, settingsFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
        addTarget = (RelativeLayout) findViewById(R.id.main_screen_addtarget);

        DataManager.getInstance().mainActivity = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

                if (DataManager.getInstance().user.isStaff) {
                    ActiveAndroid.beginTransaction();
                    new Delete().from(Module.class).execute();
                    for (int i = 0; i < 3; i++) {
                        Module modules = new Module();
                        modules.id = "DUMMY_" + (i + 1);
                        modules.name = "Dummy Module " + (i + 1);
                        modules.save();
                    }
                    ActiveAndroid.setTransactionSuccessful();
                    ActiveAndroid.endTransaction();
                } else {
                    if (DataManager.getInstance().user.isSocial) {
                        NetworkManager.getInstance().getSocialModules();
                    } else {
                        NetworkManager.getInstance().getModules();
                    }
                }

                NetworkManager.getInstance().getStretchTargets(DataManager.getInstance().user.id);
                NetworkManager.getInstance().getFriends(DataManager.getInstance().user.id);
                NetworkManager.getInstance().getFriendRequests(DataManager.getInstance().user.id);

                Preferences pref = AppCore.getInstance().getPreferences();
                pref.setAttendanceData(XApiManager.getInstance().getSettingAttendanceData());
                pref.setAttainmentData(XApiManager.getInstance().getSettingAttainmentData());
                pref.setStudyGoalAttendance(XApiManager.getInstance().getSettingStudyGoalAttendance());
//                NetworkManager.getInstance().getSettings(getString(R.string.attendanceData));
//                NetworkManager.getInstance().getSettings(getString(R.string.studyGoalAttendance));
//                NetworkManager.getInstance().getSettings(getString(R.string.attainmentData));
                NetworkManager.getInstance().getWeeklyAttendance();

                // change left menu after login
                runOnUiThread(() -> {
                    adapter = new DrawerAdapter(MainActivity.this);
                    navigationView.setAdapter(adapter);
                });
            }
        }).start();

        Intent intentService = new Intent(this, Syncronize.class);
        startService(intentService);

        android.util.Log.d(TAG, "onCreate: selected screen " + DataManager.getInstance().home_screen);
        if (DataManager.getInstance().home_screen == null) {
            DataManager.getInstance().home_screen = "feed";
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, new FeedFragment())
                    .commit();
        } else if (DataManager.getInstance().home_screen.equals("")) {
            DataManager.getInstance().home_screen = "feed";
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, new FeedFragment())
                    .commit();
        } else if (DataManager.getInstance().home_screen.toLowerCase().equals("feed")) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, new FeedFragment())
                    .commit();
        } else if (DataManager.getInstance().home_screen.toLowerCase().equals("stats")) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, new StatsVLEActivityFragment())
                    .commit();
        } else if (DataManager.getInstance().home_screen.toLowerCase().equals("log")) {
            logFragment = new LogActivityHistoryFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, logFragment)
                    .commit();
        } else if (DataManager.getInstance().home_screen.toLowerCase().equals("target")) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, new TargetFragment())
                    .commit();
        } else if (DataManager.getInstance().home_screen.toLowerCase().equals("checkin")) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, new CheckInFragment())
                    .commit();
        } else if (DataManager.getInstance().home_screen.toLowerCase().equals("friends")) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, new FriendsFragment())
                    .commit();
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (DataManager.getInstance().language == null || DataManager.getInstance().language.toLowerCase().equals("english") || DataManager.getInstance().language.toLowerCase().equals("SAESNEG".toLowerCase())) {
            Locale locale = new Locale("en");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        } else if (DataManager.getInstance().language.toLowerCase().equals("welsh") || DataManager.getInstance().language.toLowerCase().equals("CYMRAEG".toLowerCase())) {
            Locale locale = new Locale("cy");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
        DataManager.getInstance().context = this;
        DataManager.getInstance().reload();

        ((TextView) findViewById(R.id.main_screen_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
        menu = findViewById(R.id.main_screen_menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (selectedPosition < 0) {
                    return;
                }

                String selection = adapter.values[selectedPosition];
                Fragment destination = null;

                if (selection.equals(getString(R.string.feed))) {
                    destination = new FeedFragment();
                } else if (selection.equals(getString(R.string.check_in))) {
                    destination = new CheckInFragment();
                } else if (selection.equals(getString(R.string.attainment))) {
                    destination = new StatsAttainmentFragment();
                } else if (selection.equals(getString(R.string.friends))) {
                    destination = new FriendsFragment();
                } else if (selection.equals(getString(R.string.settings))) {
                    destination = new SettingsFragment();
                } else if (selection.equals(getString(R.string.graphs))) {
                    destination = new StatsVLEActivityFragment();
                } else if (selection.equals(getString(R.string.points))) {
                    destination = new StatsPointsFragment();
                } else if (selection.equals(getString(R.string.log))) {
                    logFragment = new LogActivityHistoryFragment();
                    destination = logFragment;
                } else if (selection.equals(getString(R.string.target))) {
                    destination = new TargetFragment();
                } else if (selection.equals(getString(R.string.leader_board))) {
                    destination = new StatsLeaderBoardFragment();
                } else if (selection.equals(getString(R.string.events_attended))) {
                    destination = new StatsEventAttendanceFragment();
                } else if (selection.equals(getString(R.string.attendance))) {
                    destination = new StatsAttendanceFragment();
                } else if (selection.equals(getString(R.string.app_usage))) {
                    destination = new StatsAppUsageFragment();
                }

                if (destination != null) {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment, destination);

                    // allows back
                    if (isInsideStats(selection)) {
                        fragmentTransaction.addToBackStack(null);
                    }
                    fragmentTransaction.commit();
//                            .commit();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        adapter = new DrawerAdapter(this);
        navigationView = (ListView) findViewById(R.id.nav_view);
        navigationView.setAdapter(adapter);
        navigationView.setDivider(null);
        navigationView.setDividerHeight(0);
        navigationView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (position != 0) {

                    if (adapter.values[position].equals(getString(R.string.stats))) {
                        adapter.statsOpened = !adapter.statsOpened;
                        adapter.notifyDataSetChanged();
                    }
                    if (adapter.selected_image != null) {
                        adapter.selected_image.setColorFilter(0x00FFFFFF);
                        adapter.selected_image = (ImageView) view.findViewById(R.id.drawer_item_icon);
                        adapter.selected_image.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.default_blue));
                    }
                    if (adapter.selected_text != null) {
                        adapter.selected_text.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.light_grey));
                        adapter.selected_text = (TextView) view.findViewById(R.id.drawer_item_text);
                        adapter.selected_text.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.default_blue));
                    }

                    int staticMenuItems = 8;
                    for (String menuItem : adapter.values) {
                        if (menuItem.equals(getString(R.string.check_in))) {
                            staticMenuItems++;
                        }
                    }
                    int statOpenedNum = adapter.values.length - staticMenuItems;


                    if (adapter.values[position].equals(getString(R.string.stats))) {
                        return;
                    }

                    for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                        getSupportFragmentManager().popBackStackImmediate();
                    }

                    selectedPosition = position;
                    if (!adapter.statsOpened && position > 3) {
                        selectedPosition = position + statOpenedNum;
                    }
                    drawer.closeDrawer(GravityCompat.START);

                    if (!isInsideStats(adapter.values[selectedPosition])) {
                        lastSelected = selectedPosition;
                    }


                    if (adapter.selected_text != null && adapter.selected_text.getText().toString().equals(MainActivity.this.getString(R.string.logout))) {
                        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.layout_dialog_confirmation);
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        if (DataManager.getInstance().mainActivity.isLandscape) {
                            DisplayMetrics displaymetrics = new DisplayMetrics();
                            DataManager.getInstance().mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                            int width = (int) (displaymetrics.widthPixels * 0.45);

                            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                            params.width = width;
                            dialog.getWindow().setAttributes(params);
                        }

                        ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
                        ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.confirm);

                        ((TextView) dialog.findViewById(R.id.dialog_message)).setTypeface(DataManager.getInstance().myriadpro_regular);
                        ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.confirm_logout_message);

                        ((TextView) dialog.findViewById(R.id.dialog_no_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
                        ((TextView) dialog.findViewById(R.id.dialog_no_text)).setText(R.string.no);

                        ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
                        ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setText(R.string.yes);

                        dialog.findViewById(R.id.dialog_ok).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                android.webkit.CookieManager.getInstance().removeAllCookie();
                                DataManager.getInstance().checkForbidden = false;
                                DataManager.getInstance().set_jwt("");

                                getSharedPreferences("jisc", Context.MODE_PRIVATE).edit().putString("jwt", "").apply();
                                getSharedPreferences("jisc", Context.MODE_PRIVATE).edit().putString("is_checked", "").apply();
                                getSharedPreferences("jisc", Context.MODE_PRIVATE).edit().putString("is_staff", "").apply();
                                getSharedPreferences("jisc", Context.MODE_PRIVATE).edit().putString("is_institution", "").apply();

                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                        .edit().remove("trophies").apply();

                                DataManager.getInstance().fromLogout = true;
                                DataManager.getInstance().sessionLogged = false;

                                new Delete().from(CurrentUser.class).execute();
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                MainActivity.this.finish();
                            }
                        });
                        dialog.findViewById(R.id.dialog_no).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                }
            }
        });

        if (!DataManager.getInstance().user.isSocial) {
            updateDeviceInfo();
        } else {
            updateDeviceInfoSocial();
        }
    }

    private boolean isInsideStats(String selection) {
        if (selection.equals(getString(R.string.attainment))
                || (selection.equals(getString(R.string.graphs)))
                || (selection.equals(getString(R.string.points)))
                || (selection.equals(getString(R.string.events_attended)))
                || (selection.equals(getString(R.string.attendance)))) {
            return true;
        }
        return false;
    }

    private void updateDeviceInfo() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.contains("push_token")
                && sharedPreferences.getString("push_token", "").length() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NetworkManager.getInstance().updateDeviceDetails();
                }
            }).start();
        }
    }

    private void updateDeviceInfoSocial() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.contains("push_token")
                && sharedPreferences.getString("push_token", "").length() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NetworkManager.getInstance().updateDeviceDetailsSocial();
                }
            }).start();
        }
    }

    public void setTitle(String title) {
        ((AutoResizeTextView) findViewById(R.id.main_screen_title)).setText(title);
        ((AutoResizeTextView) findViewById(R.id.main_screen_title)).setLines(1);
        ((AutoResizeTextView) findViewById(R.id.main_screen_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
    }

    @Override
    public void onBackPressed() {
        if (DataManager.getInstance().fromTargetItem) {
            DataManager.getInstance().fragment = 3;
            selectedPosition = 3;
            DataManager.getInstance().fromTargetItem = false;
            adapter.notifyDataSetChanged();
            for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                getSupportFragmentManager().popBackStackImmediate();
            }
            logFragment = new LogActivityHistoryFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, logFragment)
                    .commit();
            return;
        }
        if (DataManager.getInstance().addTarget == 1) {
            selectedPosition = 4;
            DataManager.getInstance().fragment = 4;
            adapter.notifyDataSetChanged();
            for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                getSupportFragmentManager().popBackStackImmediate();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, new TargetFragment())
                    .commit();
            DataManager.getInstance().addTarget = 0;
            return;
        }
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                backpressed = 0;
                getSupportFragmentManager().popBackStackImmediate();
                selectedPosition = lastSelected;
                DataManager.getInstance().fragment = lastSelected;
                adapter.notifyDataSetChanged();
            } else if (backpressed == 0) {
                backpressed = 1;
                Snackbar.make(findViewById(R.id.main_fragment), R.string.press_back_again_to_exit_app, Snackbar.LENGTH_LONG).show();
            } else
                super.onBackPressed();
        }
    }

    public void showProgressBar(@Nullable String text) {
        if (blackout != null) {
            blackout.setVisibility(View.VISIBLE);
            blackout.requestLayout();
            blackout.setOnClickListener(null);
        }
    }

    public void showProgressBar2(@Nullable String text) {
        if (blackout != null) {
            blackout.setVisibility(View.VISIBLE);

            if (blackout.findViewById(R.id.progress_bar) != null)
                blackout.findViewById(R.id.progressbar).setVisibility(View.GONE);

            blackout.requestLayout();
            blackout.setOnClickListener(null);
        }
    }

    public void hideProgressBar() {
        try {
            blackout.findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
        } catch (Exception ignored) {
        }
        blackout.setVisibility(View.GONE);
    }

    public void hideAllButtons() {
        friend.setVisibility(View.GONE);
        settings.setVisibility(View.GONE);
        addTarget.setVisibility(View.GONE);
        send.setVisibility(View.GONE);
        timer.setVisibility(View.GONE);
        back.setVisibility(View.GONE);
        menu.setVisibility(View.GONE);
    }

    public void showCertainButtons(int fragment) {
        switch (fragment) {
            case 1: {
                menu.setVisibility(View.VISIBLE);
                friend.setVisibility(View.VISIBLE);
                //settings.setVisibility(View.VISIBLE);
                int count = new Select().from(ReceivedRequest.class).count();
                if (count > 0) {
                    findViewById(R.id.incoming_fr).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.incoming_fr_text)).setText(count + "");
                } else
                    findViewById(R.id.incoming_fr).setVisibility(View.GONE);
                break;
            }
            case 3: {
                //settings.setVisibility(View.VISIBLE);
                addTarget.setVisibility(View.VISIBLE);
                menu.setVisibility(View.VISIBLE);

                if (new Select().from(RunningActivity.class).exists()) {
                    addTarget.setVisibility(View.GONE);
                    timer.setVisibility(View.VISIBLE);

                    timer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DataManager.getInstance().mainActivity.getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.main_fragment, new LogNewActivityFragment(), "newActivity")
                                    .addToBackStack(null)
                                    .commit();
                        }
                    });
                } else {
                    addTarget.setVisibility(View.VISIBLE);
                    timer.setVisibility(View.GONE);

                    addTarget.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (logFragment != null) {
                                logFragment.showDialog();
                            }
                        }
                    });
                }
                break;
            }
            case 4: {
                menu.setVisibility(View.VISIBLE);
                addTarget.setVisibility(View.VISIBLE);
                //settings.setVisibility(View.VISIBLE);
                Context context = this;

                addTarget.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ConnectionHandler.isConnected(context)) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.main_fragment, new AddTargetFragment())
                                    .addToBackStack(null)
                                    .commit();
                        } else {
                            ConnectionHandler.showNoInternetConnectionSnackbar();
                        }
                    }
                });
                break;
            }
            case 5: {
                menu.setVisibility(View.VISIBLE);
//                settings.setVisibility(View.VISIBLE);
                break;
            }
            case 6: {
                send.setVisibility(View.VISIBLE);
                break;
            }
            case 7: {
                back.setVisibility(View.VISIBLE);
                break;
            }
            case 8: {
                back.setVisibility(View.VISIBLE);
                //settings.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void saveBitmap(Bitmap bmp) {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        OutputStream outStream;
        File file = new File(extStorageDirectory, Constants.TEMP_IMAGE_FILE);

        if (file.exists()) {
            file.delete();
            file = new File(extStorageDirectory, Constants.TEMP_IMAGE_FILE);
        }

        try {
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            int newWidth;
            int newHeight;

            if (width >= Constants.DEFAULT_PROFILE_IMAGE_SIZE && height >= Constants.DEFAULT_PROFILE_IMAGE_SIZE) {
                if (width > height) {
                    newWidth = Constants.DEFAULT_PROFILE_IMAGE_SIZE;
                    newHeight = (Constants.DEFAULT_PROFILE_IMAGE_SIZE * height / width);
                } else {
                    newHeight = Constants.DEFAULT_PROFILE_IMAGE_SIZE;
                    newWidth = (Constants.DEFAULT_PROFILE_IMAGE_SIZE * width / height);
                }
            } else {
                newWidth = width;
                newHeight = height;
            }

            outStream = new FileOutputStream(file);
            bmp = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, false);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            showProgressBar(null);
            Bitmap bitmap = null;
            String path = mCurrentPhotoPath;

            if (path != null) {
                bitmap = BitmapFactory.decodeFile(path);
            } else if (intent.getExtras() != null && intent.getExtras().containsKey("data")) {
                bitmap = (Bitmap) intent.getExtras().get("data");
            }

            if (bitmap != null) {
                bitmap = fixOrientation(path, bitmap);
                saveBitmap(bitmap);
                final String imagePath = Environment.getExternalStorageDirectory().toString() + "/" + Constants.TEMP_IMAGE_FILE;

                new Thread(() -> {
                    if (NetworkManager.getInstance().updateProfileImage(imagePath)) {
                        MainActivity.this.runOnUiThread(() -> {
                            //settingsFragment.refresh_image();
                            EventBus.getDefault().post(new EventReloadImage());
                            hideProgressBar();
                        });
                    }
                    MainActivity.this.runOnUiThread(() -> hideProgressBar());
                }).start();
            }

            mCurrentPhotoPath = null;
        } else if (requestCode == 101) {
            if (intent != null && intent.getData() != null) {
                showProgressBar(null);
                final String path = getRealPathFromURI(MainActivity.this, intent.getData());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                saveBitmap(bitmap);
                final String imagePath = Environment.getExternalStorageDirectory().toString() + "/" + Constants.TEMP_IMAGE_FILE;

                new Thread(() -> {
                    if (NetworkManager.getInstance().updateProfileImage(imagePath)) {
                        MainActivity.this.runOnUiThread(() -> {
                            //settingsFragment.refresh_image();
                            EventBus.getDefault().post(new EventReloadImage());
                            hideProgressBar();
                        });
                    }

                    MainActivity.this.runOnUiThread(() -> hideProgressBar());
                }).start();
            }
        }
    }

    private Bitmap fixOrientation(String filePath, Bitmap bitmap) {
        Bitmap result = bitmap;

        try {
            if (bitmap != null) {
                if (isLandscape) {
                    // no need rotate
                } else {
                    File imageFile = new File(filePath);
                    ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    int rotate = 0;

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotate = 270;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotate = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotate = 90;
                            break;
                    }

                    if (rotate > 0) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(rotate);
                        result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}