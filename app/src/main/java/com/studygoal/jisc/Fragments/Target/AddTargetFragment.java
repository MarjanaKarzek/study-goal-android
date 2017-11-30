package com.studygoal.jisc.Fragments.Target;

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
import com.studygoal.jisc.Fragments.BaseFragment;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Managers.xApi.entity.LogActivityEvent;
import com.studygoal.jisc.Managers.xApi.XApiManager;
import com.studygoal.jisc.Models.Module;
import com.studygoal.jisc.Models.Targets;
import com.studygoal.jisc.Models.ToDoTasks;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.DatePicker.DatePickerForTargets;
import com.studygoal.jisc.Utils.Utils;
import com.studygoal.jisc.databinding.TargetAddTargetBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTargetFragment extends BaseFragment {
    private static final String TAG = AddTargetFragment.class.getSimpleName();

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public Boolean isInEditMode = false;
    public Boolean isSingleTarget = false;
    private Boolean isRecurringTarget;
    private TargetAddTargetBinding binding = null;

    private Calendar toDoDate = null;

    public Targets item;
    public ToDoTasks itemToDo;

    private AppCompatTextView activityType;
    private AppCompatTextView chooseActivity;
    private AppCompatTextView every;
    private AppCompatTextView in;

    private EditText hours;
    private EditText minutes;
    private EditText because;

    private View root;
    private RelativeLayout addModuleLayout;

    private TextWatcher hoursWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            int maxValue = 8;
            if (every.getText().toString().equals(getActivity().getString(R.string.day))) {
                maxValue = 8;
            } else if (every.getText().toString().equals(getActivity().getString(R.string.week))) {
                maxValue = 40;
            } else if (every.getText().toString().equals(getActivity().getString(R.string.month))) {
                maxValue = 99;
            }

            Log.e("Jisc", "Max: " + maxValue);

            if (s.toString().length() != 0) {
                int value = Integer.parseInt(s.toString());
                if (value < 0)
                    hours.setText("0");
                if (value > maxValue)
                    hours.setText("" + maxValue);
                hours.setSelection(hours.getText().length());
            }
        }
    };

    private TextWatcher minutesWatcher = new TextWatcher() {
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
                    minutes.setText("00");
                if (value > 59)
                    minutes.setText("59");
                minutes.setSelection(minutes.getText().length());
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (isInEditMode) {
            DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.edit_target));
            binding.targetSelector.setVisibility(View.GONE);

            if (isSingleTarget) {
                binding.targetSelector.check(binding.targetSingle.getId());
            } else {
                binding.targetSelector.check(binding.targetRecurring.getId());
            }
        } else {
            DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.add_target));
        }

        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(8);
        DataManager.getInstance().addTarget = 1;

        EditText module = ((EditText) root.findViewById(R.id.add_module_edit_text));
        String moduleName = (module != null && module.getText() != null) ? module.getText().toString() : null;
        XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.AddTarget, moduleName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.target_add_target, container, false);
        root = binding.root;

        DataManager.getInstance().reload();
        applyTypeface();

        if(DataManager.getInstance().mainActivity.displaySingleTarget){
            binding.targetSingle.setChecked(true);
            isRecurringTarget = false;
            binding.recurringLayout.setVisibility(View.GONE);
            binding.singleLayout.setVisibility(View.VISIBLE);
        } else {
            isRecurringTarget = true;
            binding.recurringLayout.setVisibility(View.VISIBLE);
            binding.singleLayout.setVisibility(View.GONE);
        }

        binding.targetSelector.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.target_recurring) {
                isRecurringTarget = true;
                binding.recurringLayout.setVisibility(View.VISIBLE);
                binding.singleLayout.setVisibility(View.GONE);
                DataManager.getInstance().mainActivity.displaySingleTarget = false;
            } else {
                isRecurringTarget = false;
                binding.recurringLayout.setVisibility(View.GONE);
                binding.singleLayout.setVisibility(View.VISIBLE);
                DataManager.getInstance().mainActivity.displaySingleTarget = true;
            }
        });

        activityType = ((AppCompatTextView) root.findViewById(R.id.addtarget_activityType_textView));
        activityType.setSupportBackgroundTintList(ColorStateList.valueOf(0xFF8a63cc));

        chooseActivity = ((AppCompatTextView) root.findViewById(R.id.addtarget_chooseActivity_textView));
        chooseActivity.setSupportBackgroundTintList(ColorStateList.valueOf(0xFF8a63cc));

        addModuleLayout = (RelativeLayout) root.findViewById(R.id.add_new_module_layout);
        addModuleLayout.setVisibility(View.GONE);

        root.findViewById(R.id.add_module_button_text).setOnClickListener(v -> onAddModule());
        because = ((EditText) root.findViewById(R.id.addtarget_edittext_because));

        hours = ((EditText) root.findViewById(R.id.addtarget_text_timer_1));
        minutes = ((EditText) root.findViewById(R.id.addtarget_text_timer_3));

        hours.addTextChangedListener(hoursWatcher);
        minutes.addTextChangedListener(minutesWatcher);

        every = ((AppCompatTextView) root.findViewById(R.id.addtarget_every_textView));
        every.setSupportBackgroundTintList(ColorStateList.valueOf(0xFF8a63cc));

        in = ((AppCompatTextView) root.findViewById(R.id.addtarget_in_textView));
        in.setSupportBackgroundTintList(ColorStateList.valueOf(0xFF8a63cc));

        because.setOnTouchListener((view, event) -> {
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

        toDoDate = Calendar.getInstance();
        binding.addtargetTextDate.setText(Utils.formatDate(toDoDate.getTimeInMillis()));
        binding.addtargetTextDate.setOnClickListener(v -> onSelectDate());

        if (isInEditMode) {
            if (isSingleTarget) {
                binding.addtargetEdittextMyGoalSingle.setText(itemToDo.description);
                binding.addtargetEdittextBecauseSingle.setText(itemToDo.reason);

                try {
                    Date date = simpleDateFormat.parse(itemToDo.endDate);
                    toDoDate.setTimeInMillis(date.getTime());
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }

                binding.addtargetTextDate.setText(Utils.formatDate(toDoDate.getTimeInMillis()));
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

                binding.addtargetInTextViewSingle.setText(moduleName);
            } else {
                for (Map.Entry<String, String> entry : DataManager.getInstance().api_values.entrySet()) {
                    if (entry.getValue().equals(item.activity_type))
                        activityType.setText(entry.getKey());
                }
                for (Map.Entry<String, String> entry : DataManager.getInstance().api_values.entrySet()) {
                    if (entry.getValue().equals(item.activity))
                        chooseActivity.setText(entry.getKey());
                }

                hours.setText(Integer.parseInt(item.total_time) / 60 > 10 ? "" + Integer.parseInt(item.total_time) / 60 : "0" + Integer.parseInt(item.total_time) / 60);
                minutes.setText(Integer.parseInt(item.total_time) % 60 > 10 ? "" + Integer.parseInt(item.total_time) % 60 : "0" + Integer.parseInt(item.total_time) % 60);

                for (Map.Entry<String, String> entry : DataManager.getInstance().api_values.entrySet()) {
                    if (entry.getValue().toLowerCase().equals(item.time_span.toLowerCase() + "ly")) {
                        String value = entry.getKey();
                        value = value.substring(0, 1).toUpperCase() + value.substring(1, value.length()-2);
                        every.setText(value);
                    }
                }

                String moduleName;

                if (item.module_id.equals("")) {
                    moduleName = DataManager.getInstance().mainActivity.getString(R.string.any_module);
                } else {
                    moduleName = ((Module) (new Select().from(Module.class).where("module_id = ?", item.module_id).executeSingle())).name;
                }

                in.setText(moduleName);
                because.setText(item.because);
                activityType.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar.make(root, R.string.not_change_activity_in_edit_mode_hint, Snackbar.LENGTH_LONG).show();
                    }
                });
                chooseActivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar.make(root, R.string.not_change_activity_in_edit_mode_hint, Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            activityType.setText(DataManager.getInstance().activity_type.get(0));
            activityType.setOnClickListener(v -> onAddTargetActivityType());
            chooseActivity.setText(DataManager.getInstance().choose_activity.get(DataManager.getInstance().activity_type.get(0)).get(0));
            chooseActivity.setOnClickListener(v -> onAddTargetChooseActivity());
            every.setText(DataManager.getInstance().period.get(0));
            in.setText(DataManager.getInstance().mainActivity.getString(R.string.any_module));
            binding.addtargetInTextViewSingle.setText(DataManager.getInstance().mainActivity.getString(R.string.any_module));
        }

        every.setOnClickListener(v -> onAddTargetEvery());
        binding.addtargetInTextViewSingle.setOnClickListener(v -> onAddTargetInSingle());
        binding.addtargetInTextViewSingle.setSupportBackgroundTintList(ColorStateList.valueOf(0xFF8a63cc));
        binding.addtargetSaveBtn.setOnClickListener(v -> onAddTargetSave());
        binding.addtargetSaveBtnSingle.setOnClickListener(v -> onAddTargetSingleSave());

        in.setOnClickListener(v -> onAddTargetIn());

        final View contentView = container;
        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousHeight;

            @Override
            public void onGlobalLayout() {
                int newHeight = contentView.getHeight();
                if (previousHeight != 0) {
                    if (previousHeight > newHeight) {
                        // Height decreased: keyboard was shown
                        root.findViewById(R.id.content_scroll).setPadding(0, 0, 0, 200);

                        if (because.isFocused()) {
                            final Handler handler = new Handler();
                            handler.postDelayed(() -> {
                                //Do something after 100ms
                                ScrollView scrollView = (ScrollView) root.findViewById(R.id.addtarget_container);
                                scrollView.scrollTo(0, root.findViewById(R.id.content_scroll).getHeight());
                            }, 100);
                        }
                    } else if (previousHeight < newHeight) {
                        root.findViewById(R.id.content_scroll).setPadding(0, 0, 0, 0);
                    } else {
                        // No change
                    }
                }
                previousHeight = newHeight;
            }
        });

        return root;
    }

    private void onAddModule() {
        EditText addModuleEditText = (EditText) addModuleLayout.findViewById(R.id.add_module_edit_text);
        final String moduleName = addModuleEditText.getText().toString();
        if (moduleName.length() == 0) {
            Snackbar.make(root, R.string.module_name_invalid, Snackbar.LENGTH_LONG).show();
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
                    Snackbar.make(root, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();

        addModuleLayout.setVisibility(View.GONE);
        return;
    }

    private void onAddTargetActivityType() {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.snippet_custom_spinner);
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
        listView.setAdapter(new ActivityTypeAdapter(DataManager.getInstance().mainActivity, activityType.getText().toString()));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            activityType.setText(((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString());
            chooseActivity.setText(DataManager.getInstance().choose_activity.get(activityType.getText().toString()).get(0));
            dialog.dismiss();
        });

        dialog.show();
    }

    private void onAddTargetChooseActivity() {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.snippet_custom_spinner);
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
        listView.setAdapter(new ChooseActivityAdapter(DataManager.getInstance().mainActivity, chooseActivity.getText().toString(), activityType.getText().toString()));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            chooseActivity.setText(((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void onAddTargetCardView() {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.snippet_time_spent);
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

        if (every.getText().toString().equals(getString(R.string.day))) {
            hourPicker.setMaxValue(23);
        } else {
            hourPicker.setMaxValue(71);
        }

        hourPicker.setValue(Integer.parseInt(hours.getText().toString()));
        hourPicker.setFormatter(value -> String.format("%02d", value));
        final NumberPicker minutePicker = (NumberPicker) dialog.findViewById(R.id.minute_picker);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setValue(Integer.parseInt(minutes.getText().toString()));
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
                hours.setText("0" + hour);
            else
                hours.setText("" + hour);
            int minute = minutePicker.getValue();
            if (minute < 10)
                minutes.setText("0" + minute);
            else
                minutes.setText("" + minute);
            dialog.dismiss();
        });

        ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
        dialog.show();
    }

    private void onAddTargetEvery() {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.snippet_custom_spinner);
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
        listView.setAdapter(new GenericAdapter(DataManager.getInstance().mainActivity, every.getText().toString(), DataManager.getInstance().period));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            every.setText(((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString());
            hours.setText(hours.getText().toString());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void onAddTargetIn() {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.snippet_custom_spinner);
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
            items.add(AddTargetFragment.this.getActivity().getString(R.string.add_module));
        }

        listView.setAdapter(new GenericAdapter(DataManager.getInstance().mainActivity, in.getText().toString(), items));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (DataManager.getInstance().user.isSocial && position == items.size() - 1) {
                EditText addModuleEditText = (EditText) addModuleLayout.findViewById(R.id.add_module_edit_text);
                addModuleEditText.setText("");
                addModuleLayout.setVisibility(View.VISIBLE);
                dialog.dismiss();
            } else {
                in.setText(((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString());
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void onAddTargetInSingle() {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.snippet_custom_spinner);
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
            items.add(AddTargetFragment.this.getActivity().getString(R.string.add_module));
        }

        listView.setAdapter(new GenericAdapter(DataManager.getInstance().mainActivity, binding.addtargetInTextViewSingle.getText().toString(), items));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (DataManager.getInstance().user.isSocial && position == items.size() - 1) {
                EditText addModuleEditText = (EditText) addModuleLayout.findViewById(R.id.add_module_edit_text);
                addModuleEditText.setText("");
                addModuleLayout.setVisibility(View.VISIBLE);
                dialog.dismiss();
            } else {
                binding.addtargetInTextViewSingle.setText(((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString());
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
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddTargetFragment.this.getActivity());
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_edittarget) + "</font>"));
                alertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return;
            }

            final int totalTime = Integer.parseInt(hours.getText().toString()) * 60 + Integer.parseInt(minutes.getText().toString());

            if (totalTime == Integer.parseInt(item.total_time)
                    && every.getText().toString().toLowerCase().equals(item.time_span.toLowerCase())
                    && (item.because.equals(because.getText().toString()))
                    && (item.module_id.equals("") && in.getText().toString().equals(DataManager.getInstance().mainActivity.getString(R.string.any_module)))) {
                DataManager.getInstance().mainActivity.onBackPressed();
                return;
            }

            if (totalTime == 0) {
                Snackbar.make(root, R.string.fail_to_edit_target_insuficient_time, Snackbar.LENGTH_LONG).show();
                return;
            } else {
                Module module = ((new Select().from(Module.class).where("module_name = ?", in.getText().toString()).executeSingle()));
                String id;

                if (module == null) {
                    id = "";
                } else if (module.id == null) {
                    id = "";
                } else {
                    id = module.id;
                }

                String selectedEvery = every.getText().toString();

                if (new Select().from(Targets.class).where("activity = ?", chooseActivity.getText().toString()).and("time_span = ?",selectedEvery).and("module_id = ?", id).and("total_time = ?",totalTime).exists()) {
                    Snackbar.make(root, R.string.target_same_parameters, Snackbar.LENGTH_LONG).show();
                    return;
                }

                final HashMap<String, String> params = new HashMap<>();
                params.put("student_id", DataManager.getInstance().user.id);
                params.put("target_id", item.target_id);
                params.put("total_time", totalTime + "");
                params.put("time_span",selectedEvery);

                if (!in.getText().toString().toLowerCase().equals(DataManager.getInstance().mainActivity.getString(R.string.any_module).toLowerCase()))
                    params.put("module", ((Module) (new Select().from(Module.class).where("module_name = ?", in.getText().toString()).executeSingle())).id);
                if (because.getText().toString().length() > 0)
                    params.put("because", because.getText().toString());
                Log.d(TAG, "EDIT_TARGET: " + params.toString());
                DataManager.getInstance().mainActivity.showProgressBar(null);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String modified_date = dateFormat.format(Calendar.getInstance().getTime());
                final String finalModified_date = modified_date;

                new Thread(() -> {
                    if (NetworkManager.getInstance().editTarget(params)) {
                        DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                            item.total_time = totalTime + "";
                            item.time_span = DataManager.getInstance().api_values.get(every.getText().toString().toLowerCase());
                            if (!in.getText().toString().toLowerCase().equals(DataManager.getInstance().mainActivity.getString(R.string.any_module).toLowerCase()))
                                item.module_id = ((Module) new Select().from(Module.class).where("module_name = ?", in.getText().toString()).executeSingle()).id;
                            else
                                item.module_id = "";
                            item.because = because.getText().toString();
                            item.modified_date = finalModified_date;

                            DataManager.getInstance().mainActivity.hideProgressBar();
                            DataManager.getInstance().mainActivity.onBackPressed();
                        });
                    } else {
                        DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                            DataManager.getInstance().mainActivity.hideProgressBar();
                            Snackbar.make(root, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                        });
                    }
                }).start();
            }
        } else {
            if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddTargetFragment.this.getActivity());
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_addtarget) + "</font>"));
                alertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return;
            }

            String hoursValue = hours.getText().toString();
            if (hoursValue.equals(""))
                hoursValue = "0";
            String minutesValue = this.minutes.getText().toString();
            if (minutesValue.equals(""))
                minutesValue = "0";

            int total_time = Integer.parseInt(hoursValue) * 60 + Integer.parseInt(minutesValue);

            if (total_time == 0) {
                Snackbar.make(root, R.string.fail_to_add_target_insufficient_time, Snackbar.LENGTH_LONG).show();
                return;
            } else {
                Module module = ((new Select().from(Module.class).where("module_name = ?", in.getText().toString()).executeSingle()));
                String id;

                if (module == null) {
                    id = "";
                } else if (module.id == null) {
                    id = "";
                } else {
                    id = module.id;
                }

                String selectedEvery = every.getText().toString();

                if (new Select().from(Targets.class).where("activity = ?", chooseActivity.getText().toString()).and("time_span = ?",selectedEvery).and("module_id = ?", id).exists()) {
                    Snackbar.make(root, R.string.target_same_parameters, Snackbar.LENGTH_LONG).show();
                    return;
                }

                final HashMap<String, String> params = new HashMap<>();
                params.put("student_id", DataManager.getInstance().user.id);
                params.put("activity_type", DataManager.getInstance().api_values.get(activityType.getText().toString()));
                params.put("activity", DataManager.getInstance().api_values.get(chooseActivity.getText().toString()));
                params.put("total_time", total_time + "");
                params.put("time_span",selectedEvery);

                if (!in.getText().toString().toLowerCase().equals(DataManager.getInstance().mainActivity.getString(R.string.any_module).toLowerCase())) {
                    params.put("module", ((Module) (new Select().from(Module.class).where("module_name = ?", in.getText().toString()).executeSingle())).id);
                }

                if (because.getText().toString().length() > 0) {
                    params.put("because", because.getText().toString());
                }

                Log.d(TAG, "ADD_TARGET: " + params.toString());
                DataManager.getInstance().mainActivity.showProgressBar(null);

                new Thread(() -> {
                    if (NetworkManager.getInstance().addTarget(params)) {

                        NetworkManager.getInstance().getTargets(DataManager.getInstance().user.id);
                        DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                            DataManager.getInstance().mainActivity.hideProgressBar();
                            DataManager.getInstance().mainActivity.onBackPressed();
                        });

                        XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.AddRecurringTarget);
                    } else {
                        DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                            (DataManager.getInstance().mainActivity).hideProgressBar();
                            Snackbar.make(root, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
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
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddTargetFragment.this.getActivity());
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_edittarget) + "</font>"));
                alertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return;
            }

            Date date = new Date();
            date.setTime(toDoDate.getTimeInMillis());
            String endDate = simpleDateFormat.format(date);

            final HashMap<String, String> params = new HashMap<>();
            params.put("student_id", DataManager.getInstance().user.id);
            params.put("end_date", endDate);
            params.put("record_id", itemToDo.taskId);

            if (!binding.addtargetInTextViewSingle.getText().toString().toLowerCase().equals(DataManager.getInstance().mainActivity.getString(R.string.any_module).toLowerCase())) {
                Module module = new Select().from(Module.class).where("module_name = ?", binding.addtargetInTextViewSingle.getText().toString()).executeSingle();

                if (module != null) {
                    params.put("module", module.name);
                }
            }

            if (!params.containsKey("module")) {
                params.put("module", "no_module");
            }

            if (binding.addtargetEdittextMyGoalSingle.getText().toString().length() > 0) {
                params.put("description", binding.addtargetEdittextMyGoalSingle.getText().toString());
            } else {
                Snackbar.make(root, R.string.add_single_target_missing_goal, Snackbar.LENGTH_LONG).show();
                return;
            }

            if (binding.addtargetEdittextBecauseSingle.getText().toString().length() > 0) {
                params.put("reason", binding.addtargetEdittextBecauseSingle.getText().toString());
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
                        Snackbar.make(root, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                    });
                }
            }).start();

        } else {
            if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddTargetFragment.this.getActivity());
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_addtarget) + "</font>"));
                alertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return;
            }

            Date date = new Date();
            date.setTime(toDoDate.getTimeInMillis());
            String endDate = simpleDateFormat.format(date);

            final HashMap<String, String> params = new HashMap<>();
            params.put("student_id", DataManager.getInstance().user.id);
            params.put("end_date", endDate);

            if (!binding.addtargetInTextViewSingle.getText().toString().toLowerCase().equals(DataManager.getInstance().mainActivity.getString(R.string.any_module).toLowerCase())) {
                Module module = new Select().from(Module.class).where("module_name = ?", binding.addtargetInTextViewSingle.getText().toString()).executeSingle();

                if (module != null) {
                    params.put("module", module.id);
                }
            }

            if (!params.containsKey("module")) {
                params.put("module", "no_module");
            }

            if (binding.addtargetEdittextMyGoalSingle.getText().toString().length() > 0) {
                params.put("description", binding.addtargetEdittextMyGoalSingle.getText().toString());
            } else {
                Snackbar.make(root, R.string.add_single_target_missing_goal, Snackbar.LENGTH_LONG).show();
                return;
            }

            if (binding.addtargetEdittextBecauseSingle.getText().toString().length() > 0) {
                params.put("reason", binding.addtargetEdittextBecauseSingle.getText().toString());
            }

            Log.d(TAG, "ADD_SINGLE_TARGET: " + params.toString());
            DataManager.getInstance().mainActivity.showProgressBar(null);

            new Thread(() -> {
                if (NetworkManager.getInstance().addToDoTask(params)) {

                    runOnUiThread(() -> {
                        DataManager.getInstance().mainActivity.hideProgressBar();
                        DataManager.getInstance().mainActivity.onBackPressed();
                    });
                    XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.AddSingleTarget);
                } else {
                    runOnUiThread(() -> {
                        (DataManager.getInstance().mainActivity).hideProgressBar();
                        Snackbar.make(root, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                    });
                }
            }).start();
        }
    }

    private void applyTypeface() {
        if (root != null) {
            binding.targetRecurring.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.targetSingle.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetActivityTypeText.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetTextChoose.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetEveryTextView.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetActivityTypeText.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetChooseActivityTextView.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetEdittextBecause.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetEdittextBecauseSingle.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetEdittextMyGoalSingle.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetTextTimer1.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetTextTimer3.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetTextHours.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetTextMinutes.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addModuleEditText.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addModuleButtonText.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetTextFor.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetEveryText.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetInText.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetInText.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetInTextView.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetInTextSingle.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetInTextViewSingle.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetTextBecauseTitle.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetTextBecauseTitleSingle.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetTextDateTitle.setTypeface(DataManager.getInstance().myriadpro_regular);
            binding.addtargetTextDate.setTypeface(DataManager.getInstance().myriadpro_regular);
        }
    }

    private void onSelectDate() {
        DatePickerForTargets newFragment = new DatePickerForTargets();
        newFragment.setListener((view, year, monthOfYear, dayOfMonth) -> {
            toDoDate.set(year, monthOfYear, dayOfMonth);
            binding.addtargetTextDate.setText(Utils.formatDate(year, monthOfYear, dayOfMonth));
            binding.addtargetTextDate.setTag(year + "-" + ((monthOfYear + 1) < 10 ? "0" + (monthOfYear + 1) : (monthOfYear + 1)) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth));
        });

        newFragment.show(DataManager.getInstance().mainActivity.getSupportFragmentManager(), "datePicker");
    }
}
