package com.studygoal.jisc.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.studygoal.jisc.Adapters.ActivityTypeAdapter;
import com.studygoal.jisc.Adapters.ChooseActivityAdapter;
import com.studygoal.jisc.Adapters.GenericAdapter;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Managers.xApi.entity.LogActivityEvent;
import com.studygoal.jisc.Managers.xApi.XApiManager;
import com.studygoal.jisc.Models.Module;
import com.studygoal.jisc.Models.Targets;
import com.studygoal.jisc.Models.ToDoTasks;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.Utils;
import com.studygoal.jisc.databinding.TargetAddTargetBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTarget extends BaseFragment {
    private static final String TAG = AddTarget.class.getSimpleName();
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public Boolean isInEditMode = false;
    public Boolean isSingleTarget = false;
    private Boolean mIsRecurringTarget;
    private TargetAddTargetBinding mBinding = null;

    private Calendar mToDoDate = null;

    public Targets item;
    public ToDoTasks itemToDo;

    private AppCompatTextView mActivityType;
    private AppCompatTextView mChooseActivity;
    private AppCompatTextView mEvery;
    private AppCompatTextView mIn;

    private EditText mHours;
    private EditText mMinutes;
    private EditText mBecause;

    private View mRoot;
    private RelativeLayout mAddModuleLayout;

    public AddTarget() {

    }

    private TextWatcher mHoursWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            int maxValue = 8;
            if (mEvery.getText().toString().equals(getActivity().getString(R.string.day))) {
                maxValue = 8;
            } else if (mEvery.getText().toString().equals(getActivity().getString(R.string.week))) {
                maxValue = 40;
            } else if (mEvery.getText().toString().equals(getActivity().getString(R.string.month))) {
                maxValue = 99;
            }

            Log.e("Jisc", "Max: " + maxValue);

            if (s.toString().length() != 0) {
                int value = Integer.parseInt(s.toString());
                if (value < 0)
                    mHours.setText("0");
                if (value > maxValue)
                    mHours.setText("" + maxValue);
                mHours.setSelection(mHours.getText().length());
            }
        }
    };

    private TextWatcher mMinutesWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            if (s.toString().length() != 0) {
                int value = Integer.parseInt(s.toString());
                if (value < 0)
                    mMinutes.setText("00");
                if (value > 59)
                    mMinutes.setText("59");
                mMinutes.setSelection(mMinutes.getText().length());
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (isInEditMode) {
            DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.edit_target));
            mBinding.targetSelector.setVisibility(View.GONE);

            if (isSingleTarget) {
                mBinding.targetSelector.check(mBinding.targetSingle.getId());
            } else {
                mBinding.targetSelector.check(mBinding.targetRecurring.getId());
            }
        } else {
            DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.add_target));
        }

        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(8);
        DataManager.getInstance().addTarget = 1;

        EditText module = ((EditText) mRoot.findViewById(R.id.add_module_edit_text));
        String moduleName = (module != null && module.getText() != null) ? module.getText().toString() : null;
        XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.AddTarget, moduleName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.target_add_target, container, false);
        mRoot = mBinding.root;

        DataManager.getInstance().reload();
        applyTypeface();

        mBinding.targetSelector.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.target_recurring) {
                mIsRecurringTarget = true;
                mBinding.recurringLayout.setVisibility(View.VISIBLE);
                mBinding.singleLayout.setVisibility(View.GONE);
            } else {
                mIsRecurringTarget = false;
                mBinding.recurringLayout.setVisibility(View.GONE);
                mBinding.singleLayout.setVisibility(View.VISIBLE);
            }
        });

        mIsRecurringTarget = true;
        mBinding.recurringLayout.setVisibility(View.VISIBLE);
        mBinding.singleLayout.setVisibility(View.GONE);

        mActivityType = ((AppCompatTextView) mRoot.findViewById(R.id.addtarget_activityType_textView));
        mActivityType.setSupportBackgroundTintList(ColorStateList.valueOf(0xFF8a63cc));

        mChooseActivity = ((AppCompatTextView) mRoot.findViewById(R.id.addtarget_chooseActivity_textView));
        mChooseActivity.setSupportBackgroundTintList(ColorStateList.valueOf(0xFF8a63cc));

        mAddModuleLayout = (RelativeLayout) mRoot.findViewById(R.id.add_new_module_layout);
        mAddModuleLayout.setVisibility(View.GONE);

        mRoot.findViewById(R.id.add_module_button_text).setOnClickListener(v -> onAddModule());
        mBecause = ((EditText) mRoot.findViewById(R.id.addtarget_edittext_because));

        mHours = ((EditText) mRoot.findViewById(R.id.addtarget_text_timer_1));
        mMinutes = ((EditText) mRoot.findViewById(R.id.addtarget_text_timer_3));

        mHours.addTextChangedListener(mHoursWatcher);
        mMinutes.addTextChangedListener(mMinutesWatcher);

        mEvery = ((AppCompatTextView) mRoot.findViewById(R.id.addtarget_every_textView));
        mEvery.setSupportBackgroundTintList(ColorStateList.valueOf(0xFF8a63cc));

        mIn = ((AppCompatTextView) mRoot.findViewById(R.id.addtarget_in_textView));
        mIn.setSupportBackgroundTintList(ColorStateList.valueOf(0xFF8a63cc));

        mBecause.setOnTouchListener((view, event) -> {
            if (view.getId() == R.id.log_activity_edittext_note) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
            }
            return false;
        });

        mToDoDate = Calendar.getInstance();
        mBinding.addtargetTextDate.setText(Utils.formatDate(mToDoDate.getTimeInMillis()));
        mBinding.addtargetTextDate.setOnClickListener(v -> onSelectDate());

        if (isInEditMode) {
            if (isSingleTarget) {
                mBinding.addtargetEdittextMyGoalSingle.setText(itemToDo.description);
                mBinding.addtargetEdittextBecauseSingle.setText(itemToDo.reason);

                try {
                    Date date = sDateFormat.parse(itemToDo.endDate);
                    mToDoDate.setTimeInMillis(date.getTime());
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }

                mBinding.addtargetTextDate.setText(Utils.formatDate(mToDoDate.getTimeInMillis()));
                String moduleName = "";

                if (itemToDo.module != null && !itemToDo.module.isEmpty()) {
                    Module module = new Select().from(Module.class).where("module_name = ?", itemToDo.module).executeSingle();

                    if (module != null) {
                        moduleName = module.name;
                    }
                }

                if (moduleName == null || moduleName.isEmpty()) {
                    moduleName = DataManager.getInstance().mainActivity.getString(R.string.any_module);
                }

                mBinding.addtargetInTextViewSingle.setText(moduleName);
            } else {
                for (Map.Entry<String, String> entry : DataManager.getInstance().api_values.entrySet()) {
                    if (entry.getValue().equals(item.activity_type))
                        mActivityType.setText(entry.getKey());
                }
                for (Map.Entry<String, String> entry : DataManager.getInstance().api_values.entrySet()) {
                    if (entry.getValue().equals(item.activity))
                        mChooseActivity.setText(entry.getKey());
                }

                mHours.setText(Integer.parseInt(item.total_time) / 60 > 10 ? "" + Integer.parseInt(item.total_time) / 60 : "0" + Integer.parseInt(item.total_time) / 60);
                mMinutes.setText(Integer.parseInt(item.total_time) % 60 > 10 ? "" + Integer.parseInt(item.total_time) % 60 : "0" + Integer.parseInt(item.total_time) % 60);

                for (Map.Entry<String, String> entry : DataManager.getInstance().api_values.entrySet()) {
                    if (entry.getValue().toLowerCase().equals(item.time_span.toLowerCase())) {
                        String value = entry.getKey();
                        value = value.substring(0, 1).toUpperCase() + value.substring(1, value.length());
                        mEvery.setText(value);
                    }
                }

                String moduleName;

                if (item.module_id.equals("")) {
                    moduleName = DataManager.getInstance().mainActivity.getString(R.string.any_module);
                } else {
                    moduleName = ((Module) (new Select().from(Module.class).where("module_id = ?", item.module_id).executeSingle())).name;
                }

                mIn.setText(moduleName);
                mBecause.setText(item.because);
                mActivityType.setOnClickListener(v -> onAddTargetActivityType());
                mChooseActivity.setOnClickListener(v -> onAddTargetChooseActivity());
            }
        } else {
            mActivityType.setText(DataManager.getInstance().activity_type.get(0));
            mActivityType.setOnClickListener(v -> onAddTargetActivityType());
            mChooseActivity.setText(DataManager.getInstance().choose_activity.get(DataManager.getInstance().activity_type.get(0)).get(0));
            mChooseActivity.setOnClickListener(v -> onAddTargetChooseActivity());
            mEvery.setText(DataManager.getInstance().period.get(0));
            mIn.setText(DataManager.getInstance().mainActivity.getString(R.string.any_module));
            mBinding.addtargetInTextViewSingle.setText(DataManager.getInstance().mainActivity.getString(R.string.any_module));
        }

        mEvery.setOnClickListener(v -> onAddTargetEvery());
        mBinding.addtargetInTextViewSingle.setOnClickListener(v -> onAddTargetInSingle());
        mBinding.addtargetInTextViewSingle.setSupportBackgroundTintList(ColorStateList.valueOf(0xFF8a63cc));
        mBinding.addtargetSaveBtn.setOnClickListener(v -> onAddTargetSave());
        mBinding.addtargetSaveBtnSingle.setOnClickListener(v -> onAddTargetSingleSave());

        mIn.setOnClickListener(v -> onAddTargetIn());

        final View contentView = container;
        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int mPreviousHeight;

            @Override
            public void onGlobalLayout() {
                int newHeight = contentView.getHeight();
                if (mPreviousHeight != 0) {
                    if (mPreviousHeight > newHeight) {
                        // Height decreased: keyboard was shown
                        mRoot.findViewById(R.id.content_scroll).setPadding(0, 0, 0, 200);

                        if (mBecause.isFocused()) {
                            final Handler handler = new Handler();
                            handler.postDelayed(() -> {
                                //Do something after 100ms
                                ScrollView scrollView = (ScrollView) mRoot.findViewById(R.id.addtarget_container);
                                scrollView.scrollTo(0, mRoot.findViewById(R.id.content_scroll).getHeight());
                            }, 100);
                        }
                    } else if (mPreviousHeight < newHeight) {
                        mRoot.findViewById(R.id.content_scroll).setPadding(0, 0, 0, 0);
                    } else {
                        // No change
                    }
                }
                mPreviousHeight = newHeight;
            }
        });

        return mRoot;
    }

    private void onAddModule() {
        EditText add_module_edit_text = (EditText) mAddModuleLayout.findViewById(R.id.add_module_edit_text);
        final String moduleName = add_module_edit_text.getText().toString();
        if (moduleName.length() == 0) {
            Snackbar.make(mRoot, R.string.module_name_invalid, Snackbar.LENGTH_LONG).show();
            return;
        }

        new Thread(() -> {
            final HashMap<String, String> params = new HashMap<>();
            params.put("student_id", DataManager.getInstance().user.id);
            params.put("module", moduleName);
            params.put("is_social", "yes");

            if (NetworkManager.getInstance().addModule(params)) {
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                });
            } else {
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    (DataManager.getInstance().mainActivity).hideProgressBar();
                    Snackbar.make(mRoot, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();

        mAddModuleLayout.setVisibility(View.GONE);
        return;
    }

    private void onAddTargetActivityType() {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_spinner_layout);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (DataManager.getInstance().mainActivity.isLandscape) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            DataManager.getInstance().mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = (int) (displaymetrics.widthPixels * 0.3);

            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = width;
            dialog.getWindow().setAttributes(params);
        }
        ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
        ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.choose_activity_type);

        final ListView listView = (ListView) dialog.findViewById(R.id.dialog_listview);
        listView.setAdapter(new ActivityTypeAdapter(DataManager.getInstance().mainActivity, mActivityType.getText().toString()));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            mActivityType.setText(((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString());
            mChooseActivity.setText(DataManager.getInstance().choose_activity.get(mActivityType.getText().toString()).get(0));
            dialog.dismiss();
        });

        dialog.show();
    }

    private void onAddTargetChooseActivity() {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_spinner_layout);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (DataManager.getInstance().mainActivity.isLandscape) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            DataManager.getInstance().mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = (int) (displaymetrics.widthPixels * 0.3);

            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = width;
            dialog.getWindow().setAttributes(params);
        }
        ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
        ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.choose_activity);

        final ListView listView = (ListView) dialog.findViewById(R.id.dialog_listview);
        listView.setAdapter(new ChooseActivityAdapter(DataManager.getInstance().mainActivity, mChooseActivity.getText().toString(), mActivityType.getText().toString()));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            mChooseActivity.setText(((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void onAddTargetCardView() {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.timespent_layout);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (DataManager.getInstance().mainActivity.isLandscape) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            DataManager.getInstance().mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = (int) (displaymetrics.widthPixels * 0.3);

            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = width;
            dialog.getWindow().setAttributes(params);
        }

        final NumberPicker hourPicker = (NumberPicker) dialog.findViewById(R.id.hour_picker);
        hourPicker.setMinValue(0);

        if (mEvery.getText().toString().equals(getString(R.string.daily))) {
            hourPicker.setMaxValue(23);
        } else {
            hourPicker.setMaxValue(71);
        }

        hourPicker.setValue(Integer.parseInt(mHours.getText().toString()));
        hourPicker.setFormatter(value -> String.format("%02d", value));
        final NumberPicker minutePicker = (NumberPicker) dialog.findViewById(R.id.minute_picker);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setValue(Integer.parseInt(mMinutes.getText().toString()));
        minutePicker.setFormatter(value -> {
            if (value < 10) {
                return "0" + value;
            } else {
                return value + "";
            }
        });

        ((TextView) dialog.findViewById(R.id.timespent_save_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
        dialog.findViewById(R.id.timespent_save_btn).setOnClickListener(v1 -> {
            int hour = hourPicker.getValue();
            if (hour < 10)
                mHours.setText("0" + hour);
            else
                mHours.setText("" + hour);
            int minute = minutePicker.getValue();
            if (minute < 10)
                mMinutes.setText("0" + minute);
            else
                mMinutes.setText("" + minute);
            dialog.dismiss();
        });

        ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
        dialog.show();
    }

    private void onAddTargetEvery() {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_spinner_layout);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (DataManager.getInstance().mainActivity.isLandscape) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            DataManager.getInstance().mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = (int) (displaymetrics.widthPixels * 0.3);

            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = width;
            dialog.getWindow().setAttributes(params);
        }
        ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
        ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.choose_interval);

        final ListView listView = (ListView) dialog.findViewById(R.id.dialog_listview);
        listView.setAdapter(new GenericAdapter(DataManager.getInstance().mainActivity, mEvery.getText().toString(), DataManager.getInstance().period));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            mEvery.setText(((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString());
            mHours.setText(mHours.getText().toString());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void onAddTargetIn() {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_spinner_layout);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (DataManager.getInstance().mainActivity.isLandscape) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            DataManager.getInstance().mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = (int) (displaymetrics.widthPixels * 0.3);

            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = width;
            dialog.getWindow().setAttributes(params);
        }

        ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
        ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.choose_module);

        final ListView listView = (ListView) dialog.findViewById(R.id.dialog_listview);

        final ArrayList<String> items = new ArrayList<>();
        items.add(DataManager.getInstance().mainActivity.getString(R.string.any_module));
        List<Module> modules = new Select().from(Module.class).orderBy("module_name").execute();

        for (int i = 0; i < modules.size(); i++) {
            items.add(modules.get(i).name);
        }

        if (DataManager.getInstance().user.isSocial) {
            items.add(AddTarget.this.getActivity().getString(R.string.add_module));
        }

        listView.setAdapter(new GenericAdapter(DataManager.getInstance().mainActivity, mIn.getText().toString(), items));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (DataManager.getInstance().user.isSocial && position == items.size() - 1) {
                //add new module
                EditText add_module_edit_text = (EditText) mAddModuleLayout.findViewById(R.id.add_module_edit_text);
                add_module_edit_text.setText("");
                mAddModuleLayout.setVisibility(View.VISIBLE);
                dialog.dismiss();
            } else {
                mIn.setText(((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString());
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void onAddTargetInSingle() {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_spinner_layout);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (DataManager.getInstance().mainActivity.isLandscape) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            DataManager.getInstance().mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = (int) (displaymetrics.widthPixels * 0.3);

            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = width;
            dialog.getWindow().setAttributes(params);
        }

        ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
        ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.choose_module);

        final ListView listView = (ListView) dialog.findViewById(R.id.dialog_listview);

        final ArrayList<String> items = new ArrayList<>();
        items.add(DataManager.getInstance().mainActivity.getString(R.string.any_module));
        List<Module> modules = new Select().from(Module.class).orderBy("module_name").execute();

        for (int i = 0; i < modules.size(); i++) {
            items.add(modules.get(i).name);
        }

        if (DataManager.getInstance().user.isSocial) {
            items.add(AddTarget.this.getActivity().getString(R.string.add_module));
        }

        listView.setAdapter(new GenericAdapter(DataManager.getInstance().mainActivity, mBinding.addtargetInTextViewSingle.getText().toString(), items));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (DataManager.getInstance().user.isSocial && position == items.size() - 1) {
                //add new module
                EditText add_module_edit_text = (EditText) mAddModuleLayout.findViewById(R.id.add_module_edit_text);
                add_module_edit_text.setText("");
                mAddModuleLayout.setVisibility(View.VISIBLE);
                dialog.dismiss();
            } else {
                mBinding.addtargetInTextViewSingle.setText(((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString());
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void onAddTargetSave() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if (isInEditMode) {
            if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddTarget.this.getActivity());
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_edittarget) + "</font>"));
                alertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return;
            }

            final int total_time = Integer.parseInt(mHours.getText().toString()) * 60 + Integer.parseInt(mMinutes.getText().toString());

            if (total_time == Integer.parseInt(item.total_time)
                    && mEvery.getText().toString().toLowerCase().equals(item.time_span.toLowerCase())
                    && (item.because.equals(mBecause.getText().toString()))
                    && (item.module_id.equals("") && mIn.getText().toString().equals(DataManager.getInstance().mainActivity.getString(R.string.any_module)))) {
                DataManager.getInstance().mainActivity.onBackPressed();
                return;
            }

            if (total_time == 0) {
                Snackbar.make(mRoot, R.string.fail_to_edit_target_insuficient_time, Snackbar.LENGTH_LONG).show();
                return;
            } else {
                Module module = ((new Select().from(Module.class).where("module_name = ?", mIn.getText().toString()).executeSingle()));
                String id;

                if (module == null) {
                    id = "";
                } else if (module.id == null) {
                    id = "";
                } else {
                    id = module.id;
                }

                String selectedEvery = "";
                if(mEvery.getText().toString().toLowerCase().equals("day")){
                    selectedEvery = "Daily";
                } else {
                    selectedEvery = mEvery.getText().toString() + "ly";
                }

                if (new Select().from(Targets.class).where("activity = ?", mChooseActivity.getText().toString()).and("time_span = ?",selectedEvery).and("module_id = ?", id).exists()) {
                    Snackbar.make(mRoot, R.string.target_same_parameters, Snackbar.LENGTH_LONG).show();
                    return;
                }

                final HashMap<String, String> params = new HashMap<>();
                params.put("student_id", DataManager.getInstance().user.id);
                params.put("target_id", item.target_id);
                params.put("total_time", total_time + "");
                params.put("time_span",selectedEvery);

                if (!mIn.getText().toString().toLowerCase().equals(DataManager.getInstance().mainActivity.getString(R.string.any_module).toLowerCase()))
                    params.put("module", ((Module) (new Select().from(Module.class).where("module_name = ?", mIn.getText().toString()).executeSingle())).id);
                if (mBecause.getText().toString().length() > 0)
                    params.put("because", mBecause.getText().toString());
                Log.d(TAG, "EDIT_TARGET: " + params.toString());
                DataManager.getInstance().mainActivity.showProgressBar(null);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String modified_date = dateFormat.format(Calendar.getInstance().getTime());
                final String finalModified_date = modified_date;

                new Thread(() -> {
                    if (NetworkManager.getInstance().editTarget(params)) {
                        DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                            item.total_time = total_time + "";
                            item.time_span = DataManager.getInstance().api_values.get(mEvery.getText().toString().toLowerCase());
                            if (!mIn.getText().toString().toLowerCase().equals(DataManager.getInstance().mainActivity.getString(R.string.any_module).toLowerCase()))
                                item.module_id = ((Module) new Select().from(Module.class).where("module_name = ?", mIn.getText().toString()).executeSingle()).id;
                            else
                                item.module_id = "";
                            item.because = mBecause.getText().toString();
                            item.modified_date = finalModified_date;

                            DataManager.getInstance().mainActivity.hideProgressBar();
                            DataManager.getInstance().mainActivity.onBackPressed();
//                          Snackbar.make(mRoot, R.string.target_saved, Snackbar.LENGTH_LONG).show();
                        });
                    } else {
                        DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                            DataManager.getInstance().mainActivity.hideProgressBar();
                            Snackbar.make(mRoot, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                        });
                    }
                }).start();
            }
        } else {
            if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddTarget.this.getActivity());
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_addtarget) + "</font>"));
                alertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return;
            }

            String hours = mHours.getText().toString();
            if (hours.equals(""))
                hours = "0";
            String minutes = mMinutes.getText().toString();
            if (minutes.equals(""))
                minutes = "0";

            int total_time = Integer.parseInt(hours) * 60 + Integer.parseInt(minutes);

            if (total_time == 0) {
                Snackbar.make(mRoot, R.string.fail_to_add_target_insufficient_time, Snackbar.LENGTH_LONG).show();
                return;
            } else {
                Module module = ((new Select().from(Module.class).where("module_name = ?", mIn.getText().toString()).executeSingle()));
                String id;

                if (module == null) {
                    id = "";
                } else if (module.id == null) {
                    id = "";
                } else {
                    id = module.id;
                }

                String selectedEvery = "";
                if(mEvery.getText().toString().toLowerCase().equals("day")){
                    selectedEvery = "Daily";
                } else {
                    selectedEvery = mEvery.getText().toString() + "ly";
                }

                if (new Select().from(Targets.class).where("activity = ?", mChooseActivity.getText().toString()).and("time_span = ?",selectedEvery).and("module_id = ?", id).exists()) {
                    Snackbar.make(mRoot, R.string.target_same_parameters, Snackbar.LENGTH_LONG).show();
                    return;
                }

                final HashMap<String, String> params = new HashMap<>();
                params.put("student_id", DataManager.getInstance().user.id);
                params.put("activity_type", DataManager.getInstance().api_values.get(mActivityType.getText().toString()));
                params.put("activity", DataManager.getInstance().api_values.get(mChooseActivity.getText().toString()));
                params.put("total_time", total_time + "");
                params.put("time_span",selectedEvery);

                if (!mIn.getText().toString().toLowerCase().equals(DataManager.getInstance().mainActivity.getString(R.string.any_module).toLowerCase())) {
                    params.put("module", ((Module) (new Select().from(Module.class).where("module_name = ?", mIn.getText().toString()).executeSingle())).id);
                }

                if (mBecause.getText().toString().length() > 0) {
                    params.put("because", mBecause.getText().toString());
                }

                Log.d(TAG, "ADD_TARGET: " + params.toString());
                DataManager.getInstance().mainActivity.showProgressBar(null);

                new Thread(() -> {
                    if (NetworkManager.getInstance().addTarget(params)) {
                        NetworkManager.getInstance().getTargets(DataManager.getInstance().user.id);
                        DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                            DataManager.getInstance().mainActivity.hideProgressBar();
                            DataManager.getInstance().mainActivity.onBackPressed();
//                          Snackbar.make(mRoot, R.string.target_saved, Snackbar.LENGTH_LONG).show();
                        });
                    } else {
                        DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                            (DataManager.getInstance().mainActivity).hideProgressBar();
                            Snackbar.make(mRoot, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                        });
                    }
                }).start();

            }
        }
    }

    private void onAddTargetSingleSave() {
        View view = getActivity().getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if (isInEditMode) {
            if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddTarget.this.getActivity());
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_edittarget) + "</font>"));
                alertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return;
            }

            Date date = new Date();
            date.setTime(mToDoDate.getTimeInMillis());
            String endDate = sDateFormat.format(date);

            final HashMap<String, String> params = new HashMap<>();
            params.put("student_id", DataManager.getInstance().user.id);
            params.put("end_date", endDate);
            params.put("record_id", itemToDo.taskId);

            if (!mBinding.addtargetInTextViewSingle.getText().toString().toLowerCase().equals(DataManager.getInstance().mainActivity.getString(R.string.any_module).toLowerCase())) {
                Module module = new Select().from(Module.class).where("module_name = ?", mBinding.addtargetInTextViewSingle.getText().toString()).executeSingle();

                if (module != null) {
                    params.put("module", module.name);
                }
            }

            if (!params.containsKey("module")) {
                params.put("module", "no_module");
            }

            if (mBinding.addtargetEdittextMyGoalSingle.getText().toString().length() > 0) {
                params.put("description", mBinding.addtargetEdittextMyGoalSingle.getText().toString());
            }

            if (mBinding.addtargetEdittextBecauseSingle.getText().toString().length() > 0) {
                params.put("reason", mBinding.addtargetEdittextBecauseSingle.getText().toString());
            }

            new Thread(() -> {
                if (NetworkManager.getInstance().editToDoTask(params)) {
                    DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                        DataManager.getInstance().mainActivity.hideProgressBar();
                        DataManager.getInstance().mainActivity.onBackPressed();
                    });
                } else {
                    DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                        DataManager.getInstance().mainActivity.hideProgressBar();
                        Snackbar.make(mRoot, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                    });
                }
            }).start();

        } else {
            if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddTarget.this.getActivity());
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_addtarget) + "</font>"));
                alertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return;
            }

            Date date = new Date();
            date.setTime(mToDoDate.getTimeInMillis());
            String endDate = sDateFormat.format(date);

            final HashMap<String, String> params = new HashMap<>();
            params.put("student_id", DataManager.getInstance().user.id);
            params.put("end_date", endDate);

            if (!mBinding.addtargetInTextViewSingle.getText().toString().toLowerCase().equals(DataManager.getInstance().mainActivity.getString(R.string.any_module).toLowerCase())) {
                Module module = new Select().from(Module.class).where("module_name = ?", mBinding.addtargetInTextViewSingle.getText().toString()).executeSingle();

                if (module != null) {
                    params.put("module", module.id);
                }
            }

            if (!params.containsKey("module")) {
                params.put("module", "no_module");
            }

            if (mBinding.addtargetEdittextMyGoalSingle.getText().toString().length() > 0) {
                params.put("description", mBinding.addtargetEdittextMyGoalSingle.getText().toString());
            }

            if (mBinding.addtargetEdittextBecauseSingle.getText().toString().length() > 0) {
                params.put("reason", mBinding.addtargetEdittextBecauseSingle.getText().toString());
            }

            Log.d(TAG, "ADD_SINGLE_TARGET: " + params.toString());
            DataManager.getInstance().mainActivity.showProgressBar(null);

            new Thread(() -> {
                if (NetworkManager.getInstance().addToDoTask(params)) {
                    NetworkManager.getInstance().getToDoTasks(DataManager.getInstance().user.id);

                    runOnUiThread(() -> {
                        DataManager.getInstance().mainActivity.hideProgressBar();
                        DataManager.getInstance().mainActivity.onBackPressed();
                    });
                } else {
                    runOnUiThread(() -> {
                        (DataManager.getInstance().mainActivity).hideProgressBar();
                        Snackbar.make(mRoot, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                    });
                }
            }).start();
        }
    }

    private void applyTypeface() {
        if (mRoot != null) {
            mBinding.targetRecurring.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.targetSingle.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetActivityTypeText.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetTextChoose.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetEveryTextView.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetActivityTypeText.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetChooseActivityTextView.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetEdittextBecause.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetEdittextBecauseSingle.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetEdittextMyGoalSingle.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetTextTimer1.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetTextTimer3.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetTextHours.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetTextMinutes.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addModuleEditText.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addModuleButtonText.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetTextFor.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetEveryText.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetInText.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetInText.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetInTextView.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetInTextSingle.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetInTextViewSingle.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetTextBecauseTitle.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetTextBecauseTitleSingle.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetTextDateTitle.setTypeface(DataManager.getInstance().myriadpro_regular);
            mBinding.addtargetTextDate.setTypeface(DataManager.getInstance().myriadpro_regular);
        }
    }

    private void onSelectDate() {
        DatePickerForTargets newFragment = new DatePickerForTargets();
        newFragment.setListener((view, year, monthOfYear, dayOfMonth) -> {
            mToDoDate.set(year, monthOfYear, dayOfMonth);
            mBinding.addtargetTextDate.setText(Utils.formatDate(year, monthOfYear, dayOfMonth));
            mBinding.addtargetTextDate.setTag(year + "-" + ((monthOfYear + 1) < 10 ? "0" + (monthOfYear + 1) : (monthOfYear + 1)) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth));
        });

        newFragment.show(DataManager.getInstance().mainActivity.getSupportFragmentManager(), "datePicker");
    }
}
