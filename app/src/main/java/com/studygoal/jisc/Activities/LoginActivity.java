package com.studygoal.jisc.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.studygoal.jisc.Adapters.InstitutionsAdapter;
import com.studygoal.jisc.Constants;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Models.CurrentUser;
import com.studygoal.jisc.Models.Institution;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.Utils;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.common.SafeToast;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TWITTER_KEY = "M0NKXVGquYoclGTcG81u49hka";
    private static final String TWITTER_SECRET = "CCpca8rm2GuJFkuHmTdTiwBsTcWdv7Ybi5Qqi7POIA6BvCObY6";
    private TwitterAuthClient mTwitterAuthClient;

    private CallbackManager mCallbackManager;
    private WebView mWebView;
    private RelativeLayout mLoginContent;
    private LinearLayout mLoginStep1;
    private LinearLayout mLoginStep3;
    private ImageView mLoginNextButton;
    private ProgressDialog mProgressDialog;

    private boolean mIsStaff;
    private boolean mRememberMe;
    private int mSocialType;
    private int mRefreshCounter = 0;
    private String mEmail;
    private String mSocialID;
    private boolean mIsRefreshing = false;
    private boolean mFirstTimeConnectionProblem = true;

    private Institution mSelectedInstitution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataManager.getInstance().context = getApplicationContext();
        DataManager.getInstance().init();
        DataManager.getInstance().currActivity = this;

        mIsStaff = false;
        mRememberMe = false;
        mSelectedInstitution = null;

        if (getResources().getBoolean(R.bool.landscape_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            DataManager.getInstance().isLandscape = true;
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            DataManager.getInstance().isLandscape = false;
        }

        setContentView(R.layout.activity_login);

        ActiveAndroid.initialize(this);
        DataManager.getInstance().context = this;
        DataManager.getInstance().loadFonts();

        if (DataManager.getInstance().toast) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(R.string.session_expired_message);
            alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.session_expired_title) + "</font>"));
            alertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            DataManager.getInstance().toast = false;
        }

        if (!getSharedPreferences("jisc", Context.MODE_PRIVATE).contains("guid")) {
            getSharedPreferences("jisc", Context.MODE_PRIVATE).edit().putString("guid", UUID.randomUUID().toString().toUpperCase()).apply();
        }

        DataManager.getInstance().guid = getSharedPreferences("jisc", Context.MODE_PRIVATE).getString("guid", "");

        mLoginContent = (RelativeLayout) findViewById(R.id.login_content);
        mLoginStep1 = (LinearLayout) findViewById(R.id.login_step_1);
        mLoginStep3 = (LinearLayout) findViewById(R.id.login_step_3);

        mLoginNextButton = (ImageView) findViewById(R.id.login_next_button);
        mLoginNextButton.setVisibility(View.GONE);
        mLoginContent.setVisibility(View.VISIBLE);
        mLoginStep1.setVisibility(View.VISIBLE);
        mLoginStep3.setVisibility(View.GONE);

        ((TextView) findViewById(R.id.login_logo_text)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/mmrtext.ttf"));
        ((TextView) findViewById(R.id.login_step_1_imastudent)).setTypeface(DataManager.getInstance().myriadpro_bold);
        ((TextView) findViewById(R.id.login_step_1_imastaff)).setTypeface(DataManager.getInstance().myriadpro_bold);

        mLoginNextButton.setOnClickListener(v -> {
            final InstitutionsAdapter adapter = (InstitutionsAdapter) ((ListView) findViewById(R.id.list)).getAdapter();

            if (adapter.getCount() == 0) {
                final String dialogText;

                if (isConnected()) {
                    dialogText = getString(R.string.slow_internet);
                } else {
                    dialogText = getString(R.string.no_internet);
                }

                LoginActivity.this.runOnUiThread(() -> {
                    if (!isFinishing()) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + dialogText + "</font>"));
                        alertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> {
                            mRefreshCounter = 0;

                            if (mRefreshCounter >= Constants.LOGIN_COUNT_CONNECTION_TRY) {
                                refreshData();
                            }

                            dialog.dismiss();
                        });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        DataManager.getInstance().toast = false;
                    }
                });
            } else {
                mLoginContent.setVisibility(View.GONE);
                mLoginStep1.setVisibility(View.GONE);
                mLoginStep3.setVisibility(View.VISIBLE);
            }
        });

        final TextView tvStudent = (TextView) findViewById(R.id.login_step_1_imastudent);
        final TextView tvStaff = (TextView) findViewById(R.id.login_step_1_imastaff);
        tvStudent.setOnClickListener(view -> {
            LoginActivity.this.mIsStaff = false;

            mLoginNextButton.setVisibility(View.VISIBLE);

            tvStudent.setBackgroundResource(R.drawable.round_corners_transparent_2_selected);
            tvStudent.setTextColor(Color.parseColor("#ffffff"));

            tvStaff.setBackgroundResource(R.drawable.round_corners_transparent_2);
            tvStaff.setTextColor(Color.parseColor("#ffffff"));
        });

        tvStaff.setOnClickListener(v -> {
            LoginActivity.this.mIsStaff = true;

            mLoginNextButton.setVisibility(View.VISIBLE);

            tvStudent.setBackgroundResource(R.drawable.round_corners_transparent_2);
            tvStudent.setTextColor(Color.parseColor("#ffffff"));

            tvStaff.setBackgroundResource(R.drawable.round_corners_transparent_2_selected);
            tvStaff.setTextColor(Color.parseColor("#ffffff"));
        });

        ((CheckBox) findViewById(R.id.login_check_rememberme)).setTypeface(DataManager.getInstance().myriadpro_regular);

        findViewById(R.id.login_check_rememberme).setOnClickListener(v -> {
            LoginActivity.this.mRememberMe = true;
        });

        ((TextView) findViewById(R.id.login_searchinstitution_title)).setTypeface(DataManager.getInstance().myriadpro_bold);
        ((EditText) findViewById(R.id.search_field)).setTypeface(DataManager.getInstance().myriadpro_regular);
        ((TextView) findViewById(R.id.login_institutionnotlisted)).setTypeface(DataManager.getInstance().myriadpro_bold);
        ((TextView) findViewById(R.id.login_signinwith)).setTypeface(DataManager.getInstance().myriadpro_bold);
        ((TextView) findViewById(R.id.login_demomode)).setTypeface(DataManager.getInstance().myriadpro_bold);

        findViewById(R.id.login_demomode).setOnClickListener(v -> {
            String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE0ODgzNjU2NzcsImp0aSI6IjFtbjhnU3YrWk9mVzJlYXV1NmVrN0Rzbm1MUjA0dDRyT0V0SEQ5Z1BGdk09IiwiaXNzIjoiaHR0cDpcL1wvc3AuZGF0YVwvYXV0aCIsIm5iZiI6MTQ4ODM2NTY2NywiZXhwIjoxNjYyNTY0NTY2NywiZGF0YSI6eyJlcHBuIjoiIiwicGlkIjoiZGVtb3VzZXJAZGVtby5hYy51ayIsImFmZmlsaWF0aW9uIjoic3R1ZGVudEBkZW1vLmFjLnVrIn19.xM6KkBFvHW7vtf6dF-X4f_6G3t_KGPVNylN_rMJROsh1MXIg9sK5j77L0Jzg1JR8fhXZf-0jFMnZz6FMotAeig";
//                String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE0ODg0NDkxNzksImp0aSI6IjdnOHFHVWlDKzRIdTdyN2ZUcTBOcldjaUpGTzByR1wvdUhpZVhvN0NBSjZvPSIsImlzcyI6Imh0dHA6XC9cL3NwLmRhdGFcL2F1dGgiLCJuYmYiOjE0ODg0NDkxNjksImV4cCI6MTQ5MjU5NjM2OSwiZGF0YSI6eyJlcHBuIjoiIiwicGlkIjoiczE1MTI0OTNAZ2xvcy5hYy51ayIsImFmZmlsaWF0aW9uIjoic3RhZmZAZ2xvcy5hYy51ayJ9fQ.xO_Yk6ZgTWgg0UHVXglFKD1tMP2wq98b8IU4alaGQvjtlYcjoz5W8gZbAX0Gcktl0nDs_bkvsB1g5OaYkkY6yg";
            DataManager.getInstance().set_jwt(token);

            new Thread(() -> {
                showProgressDialog(true);

                if (NetworkManager.getInstance().checkIfUserRegistered()) {
                    if (NetworkManager.getInstance().login()) {
                        DataManager.getInstance().institution = "1";
                        DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk");
                        showProgressDialog(false);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        LoginActivity.this.finish();
                        return;
                    }
                }

                runOnUiThread(() -> SafeToast.makeText(getApplicationContext(),
                        getString(R.string.slow_internet),
                        Toast.LENGTH_SHORT).show());

                showProgressDialog(false);
            }).start();


        });

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setVisibility(View.INVISIBLE);
        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                if (url.contains("?{")) {
                    mWebView.setVisibility(View.INVISIBLE);
                    String json = url.split("\\?")[1];

                    try {
                        JSONObject jsonObject = new JSONObject(java.net.URLDecoder.decode(json, "UTF-8"));

                        // Token can be replaced here for testing individuals.
                        String token = jsonObject.getString("jwt");
                        DataManager.getInstance().set_jwt(token);
                        getSharedPreferences("jisc", Context.MODE_PRIVATE).edit().putString("jwt", DataManager.getInstance().get_jwt()).apply();

                        if (LoginActivity.this.mRememberMe) {
                            getSharedPreferences("jisc", Context.MODE_PRIVATE).edit().putString("is_checked", "yes").apply();

                            if (LoginActivity.this.mIsStaff) {
                                getSharedPreferences("jisc", Context.MODE_PRIVATE).edit().putString("is_staff", "yes").apply();
                            }
                        }

                        if (LoginActivity.this.mIsStaff) {
                            new Thread(() -> {
                                if (NetworkManager.getInstance().checkIfStaffRegistered()) {
                                    if (NetworkManager.getInstance().loginStaff()) {
                                        DataManager.getInstance().institution = mSelectedInstitution.name;
                                        updateLastKnownUser();
                                        getSharedPreferences("jisc", Context.MODE_PRIVATE).edit().putString("is_institution", DataManager.getInstance().institution).apply();
                                        String firstlogin = DataManager.getInstance().first_time;
                                        Intent intent = null;
                                        if (firstlogin.equals("yes")) {
                                            NetworkManager.getInstance().updateAppUsage("0","0","0","0","0");
                                            intent = new Intent(LoginActivity.this, TermsActivity.class);
                                        } else {
                                            intent = new Intent(LoginActivity.this, MainActivity.class);
                                        }
                                        startActivity(intent);
                                        LoginActivity.this.finish();
                                    } else {
                                        //TODO: complete login staff workflow
                                    }
                                } else {
                                    //TODO: register staff
                                }
                            }).start();
                        } else {
                            new Thread(() -> {
                                if (NetworkManager.getInstance().checkIfUserRegistered()) {
                                    if (NetworkManager.getInstance().login()) {
                                        DataManager.getInstance().institution = mSelectedInstitution.name;
                                        updateLastKnownUser();
                                        getSharedPreferences("jisc", Context.MODE_PRIVATE).edit().putString("is_institution", DataManager.getInstance().institution).apply();
                                        String firstlogin = DataManager.getInstance().first_time;
                                        Intent intent = null;
                                        if (firstlogin.equals("yes")) {
                                            NetworkManager.getInstance().updateAppUsage("0","0","0","0","0");
                                            intent = new Intent(LoginActivity.this, TermsActivity.class);
                                        } else {
                                            intent = new Intent(LoginActivity.this, MainActivity.class);
                                        }
                                        startActivity(intent);
                                        LoginActivity.this.finish();
                                    } else {
                                        //TODO: Need more information about the register flow so i can deal with other situations
                                    }
                                } else {
                                    runOnUiThread(() -> {
                                        //TODO: register student
                                        mWebView.loadUrl("https://sp.data.alpha.jisc.ac.uk/Shibboleth.sso/Login?entityID=https://" + mSelectedInstitution.url +
                                                "&target=https://sp.data.alpha.jisc.ac.uk/secure/register/form.php?u=" + DataManager.getInstance().get_jwt());
                                        mWebView.setVisibility(View.VISIBLE);
                                    });
                                }
                            }).start();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mWebView.setVisibility(View.INVISIBLE);
                        hideProgressBar();
                    }
                }
            }
        });

        final InstitutionsAdapter institutionsAdapter = new InstitutionsAdapter(LoginActivity.this);
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(institutionsAdapter);

        list.setOnItemClickListener((parent, view, position, id) -> {
            showProgressBar();

            if (getCurrentFocus() != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            mWebView.loadUrl("about:blank");

            LoginActivity.this.mSelectedInstitution = (Institution) view.getTag();

            String url = "https://sp.data.alpha.jisc.ac.uk/Shibboleth.sso/Login?entityID=https://" +
                    mSelectedInstitution.url + "&target=https://sp.data.alpha.jisc.ac.uk/secure/auth.php?u=" +
                    DataManager.getInstance().guid;

            if (LoginActivity.this.mRememberMe) {
                url += "&lt=true";
            }

            mWebView.setVisibility(View.VISIBLE);
            mWebView.clearCache(true);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.loadUrl(url);
        });

        EditText editText = (EditText) findViewById(R.id.search_field);
        editText.setTypeface(DataManager.getInstance().myriadpro_regular);
        editText.addTextChangedListener(new TextWatcher() {

            LinearLayout region = (LinearLayout) findViewById(R.id.social_login_region_ll);

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                region.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                institutionsAdapter.updateItems(new Select()
                        .from(Institution.class)
                        .where("name LIKE ?", "%" + s.toString() + "%")
                        .orderBy("name ASC")
                        .execute()
                );
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    region.setVisibility(View.VISIBLE);
                }
            }
        });

        //check if REMEMBER ME is active
        String jwt = getSharedPreferences("jisc", Context.MODE_PRIVATE).getString("jwt", "");
        String is_checked = getSharedPreferences("jisc", Context.MODE_PRIVATE).getString("is_checked", "");
        String is_staff = getSharedPreferences("jisc", Context.MODE_PRIVATE).getString("is_staff", "");
        String is_institution = getSharedPreferences("jisc", Context.MODE_PRIVATE).getString("is_institution", "");
        if (is_checked.equals("yes") && jwt.length() > 0) {
            try {
                String jwtDecoded = Utils.jwtDecoded(jwt);
                JSONObject json = new JSONObject(jwtDecoded);

                Long expiration = Long.parseLong(json.optString("exp"));
                Long timestamp = System.currentTimeMillis() / 1000;

                if (expiration < timestamp) {
                    // it is expired
                    getSharedPreferences("jisc", Context.MODE_PRIVATE).edit().putString("jwt", "").apply();
                    getSharedPreferences("jisc", Context.MODE_PRIVATE).edit().putString("is_checked", "").apply();
                    getSharedPreferences("jisc", Context.MODE_PRIVATE).edit().putString("is_staff", "").apply();
                    getSharedPreferences("jisc", Context.MODE_PRIVATE).edit().putString("is_institution", "").apply();
                } else {
                    //continue with login process
                    DataManager.getInstance().set_jwt(jwt);
                    showProgressBar();

                    new Thread(() -> {
                        if (is_staff.equals("yes")) {
                            if (NetworkManager.getInstance().checkIfStaffRegistered()) {
                                if (NetworkManager.getInstance().loginStaff()) {
                                    DataManager.getInstance().institution = is_institution;
                                    updateLastKnownUser();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    LoginActivity.this.finish();
                                } else {
                                    //TODO: complete login staff workflow
                                }
                            } else {
                                //TODO: register staff workflow
                            }
                        } else {
                            if (NetworkManager.getInstance().checkIfUserRegistered()) {
                                if (NetworkManager.getInstance().login()) {
                                    DataManager.getInstance().institution = is_institution;
                                    updateLastKnownUser();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    LoginActivity.this.finish();
                                } else {
                                    //TODO: Need more information about the register flow so i can deal with other situations
                                }

                            } else {
                                //TODO: register student worflow
                            }
                        }
                    }).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        ImageView login_with_google = (ImageView) findViewById(R.id.login_with_google);
        login_with_google.setOnClickListener(view -> {
            mSocialType = 3;

            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, 5005);
        });

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        mTwitterAuthClient = new TwitterAuthClient();
        ImageView login_with_twitter = (ImageView) findViewById(R.id.login_with_twitter);
        login_with_twitter.setOnClickListener(view -> {
            mSocialType = 2;
            mSocialID = "";
            mEmail = "";

            mTwitterAuthClient.authorize(LoginActivity.this, new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> twitterSessionResult) {
                    // Success
                    mSocialID = "" + twitterSessionResult.data.getUserId();
                    mTwitterAuthClient.requestEmail(twitterSessionResult.data, new Callback<String>() {
                        @Override
                        public void success(Result<String> result) {
                            mEmail = result.data;
                            runOnUiThread(() -> loginSocial());
                        }

                        @Override
                        public void failure(TwitterException exception) {
                            android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this);
                            alertDialogBuilder.setMessage(R.string.facebook_error_email);
                            alertDialogBuilder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
                            android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                    });
                }

                @Override
                public void failure(TwitterException e) {
                    e.printStackTrace();
                }
            });
        });

        TextView backToFirstPage = (TextView) findViewById(R.id.back_to_firstpage);
        backToFirstPage.setOnClickListener(view -> onBackPressed());

        mCallbackManager = CallbackManager.Factory.create();

        ImageView login_with_facebook = (ImageView) findViewById(R.id.login_with_facebook);
        login_with_facebook.setOnClickListener(view -> {
            mSocialType = 1;
            LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email"));
        });

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                if (!loginResult.getRecentlyGrantedPermissions().contains("email")) {
                    android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this);
                    alertDialogBuilder.setMessage(R.string.facebook_error_email);
                    alertDialogBuilder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
                    android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    return;
                }

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        (object, response) -> {
                            Log.e("LoginActivity", response.toString());

                            try {
                                mSocialID = object.getString("id");
                                mEmail = object.getString("email");

                                runOnUiThread(() -> loginSocial());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
            }
        });

        if (!isConnected()) {
            if(!DataManager.getInstance().fromLogout & restoreLastKnownUser()) {
                String jwtLastKnownUser = getSharedPreferences("jisc", Context.MODE_PRIVATE).getString("jwt", "");
                DataManager.getInstance().set_jwt(jwtLastKnownUser);
                showProgressBar();

                new Thread(() -> {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    LoginActivity.this.finish();
                }).start();
                return;
            } else {
                refreshData();
            }
        } else{
            refreshData();
        }
    }

    private void showProgressDialog(final boolean show) {
        runOnUiThread(() -> {
            if (!isFinishing()) {
                if (mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(LoginActivity.this);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setMessage(Html.fromHtml("<font color='#000000'>Logging in..."));
                    mProgressDialog.setCancelable(false);
                }

                if (show) {
                    if (!mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }
                } else {
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                }
            }
        });
    }

    public void refreshData() {
        new Thread(() -> NetworkManager.getInstance().getAllTrophies()).start();
        final InstitutionsAdapter adapter = (InstitutionsAdapter) ((ListView) findViewById(R.id.list)).getAdapter();

        if (!mIsRefreshing) {
            mIsRefreshing = true;
            new Thread(() -> {
                List<Institution> items = getInstitution();

                if (items != null && items.size() > 0) {
                    LoginActivity.this.runOnUiThread(() -> {
                        adapter.updateItems(items);
                    });

                    mRefreshCounter = 0;
                } else {
                    boolean isConnectionIssue = false;

                    //refreshCounter is increased to 99 to improve the chance of a connection being made.
                    while (mRefreshCounter < Constants.LOGIN_COUNT_CONNECTION_TRY) {
                        if (!isConnected()) {
                            if (mFirstTimeConnectionProblem) {
                                showBadConnectDialog(getString(R.string.no_internet));
                            }

                            mFirstTimeConnectionProblem = false;
                            isConnectionIssue = true;
                            mRefreshCounter++;
                        } else {
                            List<Institution> institution = getInstitution();

                            if (institution != null && institution.size() > 0) {
                                mRefreshCounter = 0;

                                LoginActivity.this.runOnUiThread(() -> {
                                    adapter.updateItems(institution);
                                });

                                mIsRefreshing = false;
                                return;
                            } else {
                                mRefreshCounter++;
                            }

                            isConnectionIssue = false;
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    final String dialogText = isConnectionIssue ? getString(R.string.no_internet) : getString(R.string.slow_internet);
                    showBadConnectDialog(dialogText);
                }

                mIsRefreshing = false;
            }).start();
        }
    }

    @Override
    public void onBackPressed() {
        if (mLoginStep3.getVisibility() == View.VISIBLE) {
            mLoginStep3.setVisibility(View.GONE);

            mLoginContent.setVisibility(View.VISIBLE);
            mLoginStep1.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    public void showProgressBar() {
        findViewById(R.id.blackout).setVisibility(View.VISIBLE);
        findViewById(R.id.blackout).setOnClickListener(null);
    }

    public void hideProgressBar() {
        findViewById(R.id.blackout).setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSocialType == 1) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        } else if (mSocialType == 2) {
            mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
        } else if (mSocialType == 3) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.getStatus().getResolution() == null) {
            android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this);
            alertDialogBuilder.setMessage(R.string.google_play_services_update_required);
            alertDialogBuilder.setNegativeButton(Html.fromHtml("<font color='#000000'>OK</font>"), (dialog, which) -> dialog.dismiss());
            android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount acct = result.getSignInAccount();

                mEmail = acct.getEmail();
                mSocialID = acct.getId();

                runOnUiThread(() -> loginSocial());

            } else {
                Log.e("JISC", "handleSignInResult: " + result.getStatus().getResolution());
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("JISC", "connection result: " + connectionResult);
    }

    void loginSocial() {
        Integer response = NetworkManager.getInstance().loginSocial(mEmail, mSocialID);
        updateLastKnownUser();

        if (response == 200) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();
            return;
        }

        if (response == 403) {
            android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this);
            alertDialogBuilder.setMessage(R.string.social_login_error);
            alertDialogBuilder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
            android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this);
            alertDialogBuilder.setMessage(R.string.something_went_wrong);
            alertDialogBuilder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
            android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    private boolean isConnected() {
        boolean result = false;

        ConnectivityManager cm = (ConnectivityManager) LoginActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            result = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }

        return result;
    }

    private List<Institution> getInstitution() {
        List<Institution> result = null;

        boolean requestResult = NetworkManager.getInstance().downloadInstitutions();
        int institutionsCount = 0;

        if (requestResult) {
            institutionsCount = new Select().from(Institution.class).count();

            if (institutionsCount > 0) {
                result = new Select().from(Institution.class).orderBy("name").execute();
            }
        }

        return result;
    }

    private void showBadConnectDialog(String message) {
        LoginActivity.this.runOnUiThread(() -> {
            if (!isFinishing()) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + message + "</font>"));
                alertDialogBuilder.setNegativeButton(Html.fromHtml("<font color='#000000'>OK</font>"), (dialog, which) -> {
                    mRefreshCounter = 0;
                    refreshData();
                    dialog.dismiss();
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                DataManager.getInstance().toast = false;
            }
        });
    }

    private void updateLastKnownUser() {
        SharedPreferences sharedPref = this.getSharedPreferences("jisc",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("last_user_id", DataManager.getInstance().user.id);
        editor.putString("last_user_staff_id", DataManager.getInstance().user.staff_id);
        editor.putString("last_user_jisc_student_id", DataManager.getInstance().user.jisc_student_id);
        editor.putString("last_user_pid", DataManager.getInstance().user.pid);
        editor.putString("last_user_name", DataManager.getInstance().user.name);
        editor.putString("last_user_email", DataManager.getInstance().user.email);
        editor.putString("last_user_eppn", DataManager.getInstance().user.eppn);
        editor.putString("last_user_affiliation", DataManager.getInstance().user.affiliation);
        editor.putString("last_user_profile_pic", DataManager.getInstance().user.profile_pic);
        editor.putString("last_user_modules", DataManager.getInstance().user.modules);
        editor.putString("last_user_created_date", DataManager.getInstance().user.created_date);
        editor.putString("last_user_modified_date", DataManager.getInstance().user.modified_date);
        editor.putBoolean("last_user_isStaff", DataManager.getInstance().user.isStaff);
        editor.putBoolean("last_user_isSocial", DataManager.getInstance().user.isSocial);
        editor.putString("last_user_institution", DataManager.getInstance().institution);
        editor.commit();
    }

    private boolean restoreLastKnownUser() {
        SharedPreferences prefs = this.getSharedPreferences("jisc", this.MODE_PRIVATE);
        if (!prefs.getString("last_user_id", "0").equals("0")) {
            DataManager.getInstance().user = new CurrentUser();
            DataManager.getInstance().user.id = prefs.getString("last_user_id", "0");
            DataManager.getInstance().user.staff_id = prefs.getString("last_user_staff_id", "0");
            DataManager.getInstance().user.jisc_student_id = prefs.getString("last_user_jisc_student_id", "0");
            DataManager.getInstance().user.pid = prefs.getString("last_user_pid", "0");
            DataManager.getInstance().user.name = prefs.getString("last_user_name", "0");
            DataManager.getInstance().user.email = prefs.getString("last_user_email", "0");
            DataManager.getInstance().user.eppn = prefs.getString("last_user_eppn", "0");
            DataManager.getInstance().user.affiliation = prefs.getString("last_user_affiliation", "0");
            DataManager.getInstance().user.profile_pic = prefs.getString("last_user_profile_pic", "0");
            DataManager.getInstance().user.modules = prefs.getString("last_user_modules", "0");
            DataManager.getInstance().user.created_date = prefs.getString("last_user_created_date", "0");
            DataManager.getInstance().user.modified_date = prefs.getString("last_user_modified_date", "0");
            DataManager.getInstance().user.isStaff = prefs.getBoolean("last_user_isStaff", false);
            DataManager.getInstance().user.isSocial = prefs.getBoolean("last_user_isSocial", false);
            DataManager.getInstance().institution = prefs.getString("last_user_institution", "0");
            return true;
        }
        return false;
    }
}
