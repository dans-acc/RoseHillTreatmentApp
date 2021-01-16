package uk.co.rosehilltimber.rosehilltreatmentapp.utils.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;

public class SettingsAuthorisationDialog extends AppCompatDialogFragment
    implements View.OnClickListener
{

    public interface PasscodeDialogListener
    {
        boolean onPasscodeAuthorisationAttempt(final String passcode);
        void onPasscodeAuthorisationCanceled();
    }

    public static class BundleKey
    {
        public static final String CANCELABLE = "CANCELABLE";
    }

    private static final boolean DEFAULT_CANCELABLE = true;

    private boolean mCancelable;

    private AlertDialog mAlertDialog;
    private PasscodeDialogListener mPasscodeDialogListener;

    private EditText mPasscodeEditText;

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        final Activity activity = super.getActivity();
        if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
            super.setShowsDialog(false);
            super.dismiss();
            return null;
        }

        final Bundle bundleArguments = super.getArguments();
        if (bundleArguments == null) {
            mCancelable = true;
        } else {
            mCancelable = bundleArguments.getBoolean(BundleKey.CANCELABLE, DEFAULT_CANCELABLE);
        }

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        final LayoutInflater layoutInflater = activity.getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.dialog_settings_authorisation, null);

        alertDialogBuilder.setTitle(R.string.dialog_title_settings_authorisation);
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(
                android.R.string.ok,
                (dialog, which) -> {}
        );

        if (mCancelable) {
            alertDialogBuilder.setNegativeButton(
                    android.R.string.cancel,
                    (dialog, which) -> {}
            );
        }

        mAlertDialog = alertDialogBuilder.create();
        mAlertDialog.setCancelable(false);
        mAlertDialog.setCanceledOnTouchOutside(false);

        mPasscodeEditText = dialogView.findViewById(R.id.dialog_settings_et_passcode);

        return mAlertDialog;
    }

    @Override
    public void onAttach(final Context context)
    {
        super.onAttach(context);
        try {
            mPasscodeDialogListener = (PasscodeDialogListener) context;
        } catch (final ClassCastException e) {
            e.printStackTrace();
            throw new ClassCastException(context.toString() + " must implement " + SettingsAuthorisationDialog.PasscodeDialogListener.class.getName());
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
        if (mCancelable) {
            mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(final View view)
    {
        if (view == null) {
            return;
        }

        if (view == mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE) && mCancelable) {
            mPasscodeDialogListener.onPasscodeAuthorisationCanceled();
            super.dismiss();
            return;
        }

        final String input = mPasscodeEditText.getText().toString();
        if (!input.isEmpty()) {
            final boolean valid = mPasscodeDialogListener.onPasscodeAuthorisationAttempt(input);
            if (valid) {
                super.dismiss();
                return;
            }
        }

        DialogUtility.buildOkAlertDialog(
                super.getActivity(),
                R.string.dialog_title_invalid_passcode,
                R.string.dialog_message_invalid_passcode
        );
    }
}
