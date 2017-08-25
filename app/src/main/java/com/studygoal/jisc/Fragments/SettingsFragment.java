package com.studygoal.jisc.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.studygoal.jisc.Adapters.GenericAdapter;
import com.studygoal.jisc.Constants;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Models.Friend;
import com.studygoal.jisc.Models.TrophyMy;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.CircleTransform;
import com.studygoal.jisc.Utils.Event.EventReloadImage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SettingsFragment extends Fragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private Uri mImageUri;

    private TextView mHomeValue;

    private TextView mLanguageValue;

    private ImageView mProfileImage;

    private ProgressBar mProfileSpinner;

    @Override
    public void onResume() {
        super.onResume();

        DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.settings));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(5);

        String selected_value = "";

        switch (DataManager.getInstance().home_screen.toLowerCase()) {
            case "feed": {
                selected_value = getActivity().getString(R.string.feed);
                break;
            }
            case "stats": {
                selected_value = getActivity().getString(R.string.stats);
                break;
            }
            case "log": {
                selected_value = getActivity().getString(R.string.log);
                break;
            }
            case "target": {
                selected_value = getActivity().getString(R.string.target);
                break;
            }
        }

        mHomeValue.setText(selected_value.toUpperCase());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.settings_home, container, false);
        TextView friends = (TextView) mainView.findViewById(R.id.friends);
        friends.setTypeface(DataManager.getInstance().myriadpro_regular);
        TextView friends_value = (TextView) mainView.findViewById(R.id.friends_value);
        friends_value.setTypeface(DataManager.getInstance().myriadpro_regular);
        friends_value.setText(new Select().from(Friend.class).count() + "");

        final TextView home = (TextView) mainView.findViewById(R.id.home);
        home.setTypeface(DataManager.getInstance().myriadpro_regular);
        mHomeValue = (TextView) mainView.findViewById(R.id.home_value);
        mHomeValue.setTypeface(DataManager.getInstance().myriadpro_regular);

        String selected_value = "";
        switch (DataManager.getInstance().home_screen.toLowerCase()) {
            case "feed": {
                selected_value = getActivity().getString(R.string.feed);
                break;
            }
            case "stats": {
                selected_value = getActivity().getString(R.string.stats);
                break;
            }
            case "log": {
                selected_value = getActivity().getString(R.string.log);
                break;
            }
            case "target": {
                selected_value = getActivity().getString(R.string.target);
                break;
            }
        }
        mHomeValue.setText(selected_value.toUpperCase());

        TextView trophies = (TextView) mainView.findViewById(R.id.trophies);
        trophies.setTypeface(DataManager.getInstance().myriadpro_regular);
        TextView trophies_value = (TextView) mainView.findViewById(R.id.trophies_value);
        trophies_value.setTypeface(DataManager.getInstance().myriadpro_regular);
        trophies_value.setText(new Select().from(TrophyMy.class).count() + "");//);

        TextView language = (TextView) mainView.findViewById(R.id.language);
        language.setTypeface(DataManager.getInstance().myriadpro_regular);
        mLanguageValue = (TextView) mainView.findViewById(R.id.language_value);
        mLanguageValue.setTypeface(DataManager.getInstance().myriadpro_regular);

        ((TextView) mainView.findViewById(R.id.email_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
        ((TextView) mainView.findViewById(R.id.privacy_text)).setTypeface(DataManager.getInstance().myriadpro_regular);

        mainView.findViewById(R.id.email_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "support@jisclearninganalytics.freshdesk.com", null));
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Bug/Feature idea " + DataManager.getInstance().institution);
                emailIntent.putExtra(Intent.EXTRA_TEXT, "+" + getString(R.string.is_this_a_but_or_a_feature) + "\n" +
                        "+" + getString(R.string.which_part) + "\n" +
                        "+" + getString(R.string.further_detail));
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

        if (DataManager.getInstance().language != null) {
            mLanguageValue.setText(DataManager.getInstance().language.toLowerCase().equals("english") ? getString(R.string.english).toUpperCase() : getString(R.string.welsh).toUpperCase());
        }
        mainView.findViewById(R.id.friends_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataManager.getInstance().mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment, new Friends())
                        .addToBackStack(null)
                        .commit();
            }
        });

        mainView.findViewById(R.id.trophies_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataManager.getInstance().mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment, new Trophies())
                        .addToBackStack(null)
                        .commit();
            }
        });

        TextView name = (TextView) mainView.findViewById(R.id.name);
        name.setTypeface(DataManager.getInstance().myriadpro_regular);
        name.setText(DataManager.getInstance().user.name);

        TextView email = (TextView) mainView.findViewById(R.id.email);
        email.setTypeface(DataManager.getInstance().myriadpro_regular);
        email.setText(DataManager.getInstance().user.email + " | Student ID : " + DataManager.getInstance().user.jisc_student_id);

        Log.e("Test", "UserEmail: " + DataManager.getInstance().user.email);
        Log.e("Test", "Profile Url: " + NetworkManager.getInstance().host + DataManager.getInstance().user.profile_pic);
        mainView.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Test", "Click Mamera");
                if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SettingsFragment.this.getActivity());
                    alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_updateprofileimage) + "</font>"));
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

                final Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_spinner_layout);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
                ((TextView) dialog.findViewById(R.id.dialog_title)).setText(DataManager.getInstance().mainActivity.getString(R.string.select_source));

                final ListView listView = (ListView) dialog.findViewById(R.id.dialog_listview);
                ArrayList<String> list = new ArrayList<>();
                list.add(DataManager.getInstance().mainActivity.getString(R.string.camera));
                list.add(DataManager.getInstance().mainActivity.getString(R.string.library));

                listView.setAdapter(new GenericAdapter(getActivity(), mHomeValue.getText().toString().toUpperCase(), list));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                                    || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                                        102);
                            } else {
                                Intent intent = getIntentGetPhotoFromCamera();
                                DataManager.getInstance().mainActivity.startActivityForResult(intent, 100);
                            }
                        } else {
                            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 103);
                            } else {
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                DataManager.getInstance().mainActivity.startActivityForResult(intent, 101);
                            }
                        }
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        mainView.findViewById(R.id.home_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataManager.getInstance().mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment, new HomeScreen())
                        .addToBackStack(null)
                        .commit();
            }
        });

        mainView.findViewById(R.id.language_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataManager.getInstance().mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment, new LanguageScreen())
                        .addToBackStack(null)
                        .commit();
            }
        });

        mainView.findViewById(R.id.privacy_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataManager.getInstance().mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment, new PrivacyWebViewFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        mProfileImage = (ImageView) mainView.findViewById(R.id.profile_picture);
        mProfileSpinner = (ProgressBar) mainView.findViewById(R.id.profile_spinner);
        mProfileSpinner.setVisibility(View.GONE);
        boolean isLandscape = DataManager.getInstance().isLandscape;

        if (isLandscape) {
            mProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            mProfileImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        Glide.with(this)
                .load(NetworkManager.getInstance().no_https_host + DataManager.getInstance().user.profile_pic)
                .into(mProfileImage);
        return mainView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void loadImageView(final DataManager manager) {
        manager.mainActivity.runOnUiThread(() -> {
            mProfileSpinner.setVisibility(View.VISIBLE);

            Glide.with(manager.mainActivity)
                    .load(NetworkManager.getInstance().no_https_host + manager.user.profile_pic)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            mProfileSpinner.setVisibility(View.INVISIBLE);
                            return false;
                        }
                    })
                    .into(mProfileImage);

            Glide.with(manager.mainActivity)
                    .load(NetworkManager.getInstance().no_https_host + manager.user.profile_pic)
                    .transform(new CircleTransform(manager.mainActivity))
                    .into(manager.mainActivity.adapter.profile_pic);
        });
    }

    private void reloadUserProfile() {
        final DataManager manager = DataManager.getInstance();

        new Thread(() -> {
            boolean needLoadImage = false;

            if (manager.user.isSocial) {
                Integer response = NetworkManager.getInstance().loginSocial(manager.user.email, manager.user.password);

                if (response != 200) {
                    return;
                }

                needLoadImage = true;
            }

            if (manager.user.isStaff) {
                needLoadImage = NetworkManager.getInstance().loginStaff();
            } else {
                needLoadImage = NetworkManager.getInstance().login();
            }

            if (needLoadImage) {
                loadImageView(manager);
            }
        }).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshImageEvenFired(EventReloadImage eventReloadImage) {
        reloadUserProfile();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 103) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                DataManager.getInstance().mainActivity.startActivityForResult(intent, 101);
            }
        } else if (requestCode == 102) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                ContentValues values = new ContentValues();

                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");

                mImageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                DataManager.getInstance().mainActivity.startActivityForResult(intent, 100);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private Intent getIntentGetPhotoFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(), "com.studygoal.jisc.provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            }
        }

        return takePictureIntent;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                Constants.TEMP_IMAGE_FILE_NAME,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        DataManager manager = DataManager.getInstance();
        manager.mainActivity.mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
