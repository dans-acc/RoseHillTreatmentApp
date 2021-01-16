package uk.co.rosehilltimber.rosehilltreatmentapp.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.dialogs.CuboidTimberPackDialog;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.dialogs.RoundTimberPackDialog;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.dialogs.SettingsAuthorisationDialog;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.dialogs.TimberPackDialog;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPackType;

public class DialogUtility
{

    // Default dialog options.
    private static final boolean DEFAULT_ALERT_SHOW_DIALOG = true;

    private static boolean isInvalidActivity(final Activity activity)
    {
        return activity == null || activity.isFinishing() || activity.isDestroyed();
    }

    public static <T extends SettingsAuthorisationDialog.PasscodeDialogListener> SettingsAuthorisationDialog
    buildSettingsAuthorisationDialog(final T listener, final boolean cancelable)
    {

        final SettingsAuthorisationDialog settingsAuthorisationDialog = new SettingsAuthorisationDialog();
        settingsAuthorisationDialog.setCancelable(false);

        final Bundle bundleArguments = new Bundle();
        bundleArguments.putBoolean(
                SettingsAuthorisationDialog.BundleKey.CANCELABLE,
                cancelable
        );
        settingsAuthorisationDialog.setArguments(bundleArguments);

        return settingsAuthorisationDialog;
    }

    public static <T extends TimberPackDialog.TimberPackDialogListener> TimberPackDialog
    buildTimberPackDialog(final T activity, final TimberPack timberPack)
    {
        final TimberPackDialog timberPackDialog = buildTimberPackDialog(activity, timberPack.getTimberPackType());

        final Bundle bundleArguments = new Bundle();
        bundleArguments.putParcelable(
                TimberPackDialog.BundleKey.TIMBER_PACK_INSTANCE,
                timberPack
        );
        timberPackDialog.setArguments(bundleArguments);

        return timberPackDialog;
    }

    public static <T extends TimberPackDialog.TimberPackDialogListener> TimberPackDialog
    buildTimberPackDialog(final T activity, final TimberPackType timberPackType)
    {
        final TimberPackDialog timberPackDialog;
        if (timberPackType == TimberPackType.CUBOID) {
            timberPackDialog = new CuboidTimberPackDialog();
        } else {
            timberPackDialog = new RoundTimberPackDialog();
        }
        return timberPackDialog;
    }

    public static ProgressDialog buildProgressDialog(final Activity activity,
                                                     @StringRes final int progressDialogMessage)
    {
        return buildProgressDialog(
                activity,
                R.string.dialog_title_please_wait,
                progressDialogMessage,
                DEFAULT_ALERT_SHOW_DIALOG
        );
    }

    public static ProgressDialog buildProgressDialog(final Activity activity,
                                                     @StringRes final int progressDialogTitle,
                                                     @StringRes final int progressDialogMessage,
                                                     final boolean show)
    {
        if (isInvalidActivity(activity)) {
            return null;
        }

        return DialogUtility.buildProgressDialog(
                activity,
                activity.getString(progressDialogTitle),
                activity.getString(progressDialogMessage),
                show
        );
    }

    public static ProgressDialog buildProgressDialog(final Activity activity,
                                                     final String progressDialogTitle,
                                                     final String progressDialogMessage,
                                                     final boolean show)
    {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(progressDialogTitle);
        progressDialog.setMessage(progressDialogMessage);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        if (show) {
            progressDialog.show();
        }

        return progressDialog;
    }

    public static AlertDialog buildSelectionDialog(final Activity activity,
                                                   @StringRes final int alertDialogTitle,
                                                   final CharSequence[] selectableItems,
                                                   final DialogInterface.OnClickListener selectableItemClickListener)
    {
        return DialogUtility.buildSelectionDialog(
                activity,
                alertDialogTitle,
                selectableItems,
                selectableItemClickListener,
                DEFAULT_ALERT_SHOW_DIALOG
        );
    }

    public static AlertDialog buildSelectionDialog(final Activity activity,
                                                   @StringRes final int alertDialogTitle,
                                                   final CharSequence[] selectableItems,
                                                   final DialogInterface.OnClickListener selectableItemClickListener,
                                                   final boolean show)
    {
        if (isInvalidActivity(activity)) {
            return null;
        }

        final AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(alertDialogTitle)
                .setItems(
                        selectableItems,
                        selectableItemClickListener
                )
                .setNegativeButton(
                        android.R.string.cancel,
                        (dialog, which) -> dialog.cancel()
                ).create();
        if (show) {
            alertDialog.show();
        }

        return alertDialog;
    }

    public static AlertDialog buildConfirmDialog(final Activity activity,
                                                 @StringRes final int alertDialogTitle,
                                                 @StringRes final int alertDialogMessage,
                                                 final DialogInterface.OnClickListener positiveListener,
                                                 @StringRes final int positiveButtonName)
    {
        return buildConfirmDialog(
                activity,
                alertDialogTitle,
                alertDialogMessage,
                positiveListener,
                positiveButtonName,
                (dialog, which) -> dialog.cancel(),
                DEFAULT_ALERT_SHOW_DIALOG
        );
    }

    public static AlertDialog buildConfirmDialog(final Activity activity,
                                                 @StringRes final int alertDialogTitle,
                                                 @StringRes final int alertDialogMessage,
                                                 final DialogInterface.OnClickListener positiveClickListener,
                                                 @StringRes final int positiveButtonName,
                                                 final DialogInterface.OnClickListener negativeClickListener,
                                                 final boolean show)
    {
        if (isInvalidActivity(activity)) {
            return null;
        }



        return buildConfirmDialog(
                activity,
                activity.getString(alertDialogTitle),
                activity.getString(alertDialogMessage),
                positiveClickListener,
                positiveButtonName,
                negativeClickListener,
                show
        );
    }

    public static AlertDialog buildConfirmDialog(final Activity activity,
                                                 final String alertDialogTitle,
                                                 final String alertDialogMessage,
                                                 final DialogInterface.OnClickListener positiveClickListener,
                                                 @StringRes final int positiveButtonName,
                                                 final DialogInterface.OnClickListener negativeClickListener,
                                                 final boolean show)
    {
        if (isInvalidActivity(activity)) {
            return null;
        }

        final AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(alertDialogTitle)
                .setMessage(alertDialogMessage)
                .setPositiveButton(
                        positiveButtonName,
                        positiveClickListener
                )
                .setNegativeButton(
                        android.R.string.cancel,
                        negativeClickListener
                ).create();

        if (show) {
            alertDialog.show();
        }

        return alertDialog;
    }

    @UiThread
    public static AlertDialog buildOkAlertDialog(final Activity activity,
                                                 @StringRes final int alertDialogTitle,
                                                 @StringRes final int alertDialogMessage)
    {
        return buildOkAlertDialog(activity, alertDialogTitle, alertDialogMessage, DEFAULT_ALERT_SHOW_DIALOG);
    }

    @UiThread
    public static AlertDialog buildOkAlertDialog(final Activity activity,
                                                 @StringRes final int alertDialogTitle,
                                                 @StringRes final int alertDialogMessage,
                                                 final boolean show)
    {
        if (isInvalidActivity(activity)) {
            return null;
        }

        final AlertDialog alertDialog =  new AlertDialog.Builder(activity)
            .setTitle(alertDialogTitle)
            .setMessage(alertDialogMessage)
            .setNeutralButton(
                    android.R.string.ok,
                    (dialog, which) -> dialog.cancel()
            ).create();


        if (show) {
            alertDialog.show();
        }

        return alertDialog;
    }
}
