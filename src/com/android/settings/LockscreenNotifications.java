package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

public class LockscreenNotifications extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, CompoundButton.OnCheckedChangeListener,
        DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

    private static final String KEY_LOCKSCREEN_NOTIFICATIONS = "lockscreen_notifications";
    private static final String KEY_POCKET_MODE = "pocket_mode";
    private static final String KEY_SHOW_ALWAYS = "show_always";
    private static final String KEY_HIDE_LOW_PRIORITY = "hide_low_priority";
    private static final String KEY_HIDE_NON_CLEARABLE = "hide_non_clearable";
    private static final String KEY_DISMISS_ALL = "dismiss_all";
    private static final String KEY_EXPANDED_VIEW = "expanded_view";
    private static final String KEY_FORCE_EXPANDED_VIEW = "force_expanded_view";
    private static final String KEY_NOTIFICATIONS_HEIGHT = "notifications_height";
    private static final String KEY_PRIVACY_MODE = "privacy_mode";
    private static final String KEY_OFFSET_TOP = "offset_top";

    private Switch mEnabledSwitch;
    private boolean mLockscreenNotifications;

    private boolean mDialogClicked;
    private Dialog mEnableDialog;

    private CheckBoxPreference mPocketMode;
    private CheckBoxPreference mShowAlways;
    private CheckBoxPreference mHideLowPriority;
    private CheckBoxPreference mHideNonClearable;
    private CheckBoxPreference mDismissAll;
    private CheckBoxPreference mExpandedView;
    private CheckBoxPreference mForceExpandedView;
    private NumberPickerPreference mNotificationsHeight;
    private CheckBoxPreference mPrivacyMode;
    private SeekBarPreference mOffsetTop;
  

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        final Activity activity = getActivity();
        mEnabledSwitch = new Switch(activity);

        final int padding = activity.getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        mEnabledSwitch.setPaddingRelative(0, 0, padding, 0);
        mEnabledSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        final Activity activity = getActivity();
        activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM);
        activity.getActionBar().setCustomView(mEnabledSwitch, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.END));
        mEnabledSwitch.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS, 0) == 1);
    }

    @Override
    public void onStop() {
        super.onStop();
        final Activity activity = getActivity();
        activity.getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_CUSTOM);
        activity.getActionBar().setCustomView(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_notifications);
        PreferenceScreen prefs = getPreferenceScreen();
        final ContentResolver cr = getActivity().getContentResolver();

        mPocketMode = (CheckBoxPreference) prefs.findPreference(KEY_POCKET_MODE);
        mPocketMode.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_POCKET_MODE, 1) == 1);

        mShowAlways = (CheckBoxPreference) prefs.findPreference(KEY_SHOW_ALWAYS);
        mShowAlways.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_SHOW_ALWAYS, 1) == 1);

        mHideLowPriority = (CheckBoxPreference) prefs.findPreference(KEY_HIDE_LOW_PRIORITY);
        mHideLowPriority.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HIDE_LOW_PRIORITY, 0) == 1);

        mHideNonClearable = (CheckBoxPreference) prefs.findPreference(KEY_HIDE_NON_CLEARABLE);
        mHideNonClearable.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HIDE_NON_CLEARABLE, 0) == 1);

        mDismissAll = (CheckBoxPreference) prefs.findPreference(KEY_DISMISS_ALL);
        mDismissAll.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_DISMISS_ALL, 1) == 1);

        mPrivacyMode = (CheckBoxPreference) prefs.findPreference(KEY_PRIVACY_MODE);
        mPrivacyMode.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_PRIVACY_MODE, 0) == 1);

        mExpandedView = (CheckBoxPreference) prefs.findPreference(KEY_EXPANDED_VIEW);
        mExpandedView.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_EXPANDED_VIEW, 1) == 1);

        mForceExpandedView = (CheckBoxPreference) prefs.findPreference(KEY_FORCE_EXPANDED_VIEW);
        mForceExpandedView.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_FORCE_EXPANDED_VIEW, 0) == 1);

        mOffsetTop = (SeekBarPreference) prefs.findPreference(KEY_OFFSET_TOP);
        mOffsetTop.setProgress((int)(Settings.System.getFloat(cr,
                Settings.System.LOCKSCREEN_NOTIFICATIONS_OFFSET_TOP, 0.3f) * 100));
        mOffsetTop.setTitle(getResources().getText(R.string.offset_top) + " " + mOffsetTop.getProgress() + "%");
        mOffsetTop.setOnPreferenceChangeListener(this);

        mNotificationsHeight = (NumberPickerPreference) prefs.findPreference(KEY_NOTIFICATIONS_HEIGHT);
        mNotificationsHeight.setValue(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HEIGHT, 4));

        Point displaySize = new Point();
        ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(displaySize);
        int max = Math.round((float)displaySize.y * (1f - (mOffsetTop.getProgress() / 100f)) /
                (float)mContext.getResources().getDimensionPixelSize(R.dimen.notification_row_min_height));
        mNotificationsHeight.setMinValue(1);
        mNotificationsHeight.setMaxValue(max);
        mNotificationsHeight.setOnPreferenceChangeListener(this);

        boolean hasProximitySensor = getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY);
        if (!hasProximitySensor) {
            prefs.removePreference(mPocketMode);
            prefs.removePreference(mShowAlways);
        }
        updateDependency();
    }

    private boolean isKeyguardSecure() {
        LockPatternUtils mLockPatternUtils = new LockPatternUtils(getActivity());
        boolean isSecure = mLockPatternUtils.isSecure();
        return isSecure;
    }

    private void updateDependency() {
        mLockscreenNotifications = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS, 0) == 1;
        mPocketMode.setEnabled(mLockscreenNotifications);
        mShowAlways.setEnabled(mPocketMode.isChecked() && mPocketMode.isEnabled());
        mHideLowPriority.setEnabled(mLockscreenNotifications);
        mHideNonClearable.setEnabled(mLockscreenNotifications);
        mDismissAll.setEnabled(!mHideNonClearable.isChecked() && mLockscreenNotifications);
        mNotificationsHeight.setEnabled(mLockscreenNotifications);
        mOffsetTop.setEnabled(mLockscreenNotifications);
        mForceExpandedView.setEnabled(mLockscreenNotifications && mExpandedView.isChecked()
                    && !mPrivacyMode.isChecked());
        mExpandedView.setEnabled(mLockscreenNotifications && !mPrivacyMode.isChecked());
    }


    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver cr = getActivity().getContentResolver();
        if (preference == mPocketMode) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_POCKET_MODE,
                    mPocketMode.isChecked() ? 1 : 0);
            mShowAlways.setEnabled(mPocketMode.isChecked());
        } else if (preference == mShowAlways) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_SHOW_ALWAYS,
                    mShowAlways.isChecked() ? 1 : 0);
        } else if (preference == mHideLowPriority) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_HIDE_LOW_PRIORITY,
                    mHideLowPriority.isChecked() ? 1 : 0);
        } else if (preference == mHideNonClearable) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_HIDE_NON_CLEARABLE,
                    mHideNonClearable.isChecked() ? 1 : 0);
            mDismissAll.setEnabled(!mHideNonClearable.isChecked());
        } else if (preference == mDismissAll) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_DISMISS_ALL,
                    mDismissAll.isChecked() ? 1 : 0);
        } else if (preference == mExpandedView) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_EXPANDED_VIEW,
                    mExpandedView.isChecked() ? 1 : 0);
            mForceExpandedView.setEnabled(mExpandedView.isChecked());
        } else if (preference == mForceExpandedView) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_FORCE_EXPANDED_VIEW,
                    mForceExpandedView.isChecked() ? 1 : 0);
        } else if (preference == mPrivacyMode) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_PRIVACY_MODE,
                    mPrivacyMode.isChecked() ? 1 : 0);
            mForceExpandedView.setEnabled(mLockscreenNotifications && mExpandedView.isChecked()
                        && !mPrivacyMode.isChecked());
            mExpandedView.setEnabled(mLockscreenNotifications && !mPrivacyMode.isChecked());
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object value) {
        if (pref == mNotificationsHeight) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HEIGHT, (Integer)value);
        } else if (pref == mOffsetTop) {
            Settings.System.putFloat(getContentResolver(), Settings.System.LOCKSCREEN_NOTIFICATIONS_OFFSET_TOP,
                    (Integer)value / 100f);
            mOffsetTop.setTitle(getResources().getText(R.string.offset_top) + " " + (Integer)value + "%");
            Point displaySize = new Point();
            ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(displaySize);
            int max = Math.round((float)displaySize.y * (1f - (mOffsetTop.getProgress() / 100f)) /
                    (float)mContext.getResources().getDimensionPixelSize(R.dimen.notification_row_min_height));
            mNotificationsHeight.setMaxValue(max);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final boolean lesConditionnelles = ((Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS_ALLOWED, 0) == 0) && isKeyguardSecure());
        if (buttonView == mEnabledSwitch) {
            if (isChecked && lesConditionnelles) {
                if (isChecked) {
                    mDialogClicked = false;
                    if (mEnableDialog != null) {
                        dismissDialogs();
                    }
                    mEnableDialog = new AlertDialog.Builder(getActivity()).setMessage(
                            getActivity().getResources().getString(
                                    R.string.lockscreen_notifications_dialog_message))
                            .setTitle(R.string.lockscreen_notifications_dialog_title)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setPositiveButton(android.R.string.yes, this)
                            .setNegativeButton(android.R.string.no, this)
                            .show();
                    mEnableDialog.setOnDismissListener(this);
                }
            }

            boolean value = ((Boolean)isChecked).booleanValue();
             Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS,
                    value ? 1 : 0);
            updateDependency();
        }
    }

    private void dismissDialogs() {
        if (mEnableDialog != null) {
            mEnableDialog.dismiss();
            mEnableDialog = null;
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mEnableDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                mDialogClicked = true;
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.LOCKSCREEN_NOTIFICATIONS_ALLOWED, 1);
            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                mDialogClicked = true;
            }
        }
    }

    public void onDismiss(DialogInterface dialog) {
        // ahh!
    }
}
