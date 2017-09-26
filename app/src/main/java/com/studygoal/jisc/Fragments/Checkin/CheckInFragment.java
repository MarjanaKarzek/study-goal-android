package com.studygoal.jisc.Fragments.Checkin;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.studygoal.jisc.Adapters.ActivitiesHistoryAdapter;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.Connection.ConnectionHandler;

public class CheckInFragment extends Fragment {
    private static final String TAG = CheckInFragment.class.getSimpleName();

    private View mainView;
    private ListView list;
    private ActivitiesHistoryAdapter adapter;
    private SwipeRefreshLayout layout;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;

    private ProgressDialog progressDialog;

    @Override
    public void onResume() {
        super.onResume();
        ((TextView) mainView.findViewById(R.id.pin_text_edit)).setText("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        mainView = inflater.inflate(R.layout.checkin_fragment, container, false);

        DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.check_in));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(5);

        final TextView pinTextEdit = (TextView) mainView.findViewById(R.id.pin_text_edit);
        pinTextEdit.setTypeface(DataManager.getInstance().oratorstd_typeface);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 102);
        }

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {

            CheckInFragment.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setMessage(getActivity().getResources().getString(R.string.gps_network_not_enabled));
                    dialog.setPositiveButton(getActivity().getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            Intent myIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            getActivity().startActivity(myIntent);
                        }
                    });
                    dialog.setNegativeButton(getActivity().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        }
                    });
                    dialog.show();
                }
            });
        }

        ((TextView) mainView.findViewById(R.id.pin_send_button)).setTypeface(DataManager.getInstance().oratorstd_typeface);

        mainView.findViewById(R.id.pin_send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog(true);
                if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                    showProgressDialog(false);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CheckInFragment.this.getActivity());
                    alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_setcheckinpin) + "</font>"));
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

                if (DataManager.getInstance().user.isStaff
                        || DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                    showProgressDialog(false);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CheckInFragment.this.getActivity());
                    alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.alert_invalid_pin) + "</font>"));
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

                if(ConnectionHandler.isConnected(getContext())) {
                    final String pin_text_edit_text = pinTextEdit.getText().toString();
                    if (pin_text_edit_text.length() == 0) {
                        showProgressDialog(false);
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CheckInFragment.this.getActivity());
                        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.alert_invalid_pin) + "</font>"));
                        alertDialogBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            } catch (SecurityException se) {
                                se.printStackTrace();
                            }

                            final boolean result = NetworkManager.getInstance().setUserPin(pin_text_edit_text, "LOCATION");

                            // commented, not used
                            //debug for testing getsettings - by tmobiledevcore
                            //final boolean result_getsetting_attendanceData = NetworkManager.getInstance().getSetting("attendanceData");
                            //final boolean result_getsetting_checkinData = NetworkManager.getInstance().getSetting("checkinData");


                            CheckInFragment.this.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String message;
                                    if (result) {
                                        message = CheckInFragment.this.getActivity().getString(R.string.alert_valid_pin);
                                    } else {
                                        message = CheckInFragment.this.getActivity().getString(R.string.alert_invalid_pin);
                                    }

                                    showProgressDialog(false);
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CheckInFragment.this.getActivity());
                                    alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + message + "</font>"));
                                    alertDialogBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();

                                    pinTextEdit.setText("");
                                }
                            });
                        }
                    }).start();
                } else {
                    showProgressDialog(false);
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }
        });

        GridLayout grid = (GridLayout) mainView.findViewById(R.id.grid_layout);
        int childCount = grid.getChildCount();

        for (int i = 0; i < childCount; i++) {
            if (grid.getChildAt(i) instanceof ImageView) {
                final ImageView text = (ImageView) grid.getChildAt(i);
                text.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        // your click code here
                        String pin_text_edit_text = pinTextEdit.getText().toString();
                        if (pin_text_edit_text.length() > 0)
                            pinTextEdit.setText(pin_text_edit_text.substring(0, pin_text_edit_text.length() - 1));
                    }
                });
            } else {
                final TextView text = (TextView) grid.getChildAt(i);
                text.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        // your click code here
                        pinTextEdit.setText(pinTextEdit.getText().toString() + text.getText().toString());
                    }
                });
            }
        }

        return mainView;
    }

    private void showProgressDialog(final boolean show) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setMessage("Validating PIN...");
                    progressDialog.setCancelable(false);
                }
                if (show) {
                    progressDialog.show();
                } else {
                    progressDialog.dismiss();
                }
            }
        });
    }
}
