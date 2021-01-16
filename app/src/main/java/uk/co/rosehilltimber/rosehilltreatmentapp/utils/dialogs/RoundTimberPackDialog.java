package uk.co.rosehilltimber.rosehilltreatmentapp.utils.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.RoundTimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPackType;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;

public class RoundTimberPackDialog extends TimberPackDialog<RoundTimberPack>
{

    private static final int INIT_ROUND_TIMBER_PACK_QUANTITY = 0;
    private static final double INIT_ROUND_TIMBER_PACK_LENGTH = 0d;
    private static final double INIT_ROUND_TIMBER_PACK_RADIUS = 0d;

    private EditText mTimberPackQuantityEditText;
    private EditText mTimberLengthEditText;
    private EditText mTimberDiameterEditText;

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
            super.mTimberPack = new RoundTimberPack(
                    INIT_ROUND_TIMBER_PACK_QUANTITY,
                    INIT_ROUND_TIMBER_PACK_LENGTH,
                    INIT_ROUND_TIMBER_PACK_RADIUS
            );
            mNewTimberPack = true;
        } else if (bundleArguments.containsKey(BundleKey.TIMBER_PACK_INSTANCE)) {
            final RoundTimberPack existingTimberPack = bundleArguments.getParcelable(BundleKey.TIMBER_PACK_INSTANCE);
            if (existingTimberPack == null) {
                super.setShowsDialog(false);
                super.dismiss();
                return null;
            } else {
                mTimberPack = new RoundTimberPack(
                        existingTimberPack.getUUID(),
                        existingTimberPack.getQuantity(),
                        existingTimberPack.getLengthM(),
                        existingTimberPack.getRadiusM()
                );
                mNewTimberPack = false;
            }
        }

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        final LayoutInflater layoutInflater = activity.getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.dialog_round_timber_pack, null);

        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setTitle(TimberPackType.ROUND.getName());
        if (mNewTimberPack) {
            alertDialogBuilder.setPositiveButton(
                    R.string.util_create,
                    (dialog, which) -> {}
            );
        } else {
            alertDialogBuilder.setPositiveButton(
                    R.string.util_update,
                    (dialog, which) -> {}
            );
        }
        alertDialogBuilder.setNegativeButton(
                android.R.string.cancel,
                (dialog, which) -> dialog.cancel()
        );

        super.mAlertDialog = alertDialogBuilder.create();
        super.mAlertDialog.setCanceledOnTouchOutside(false);

        mTimberPackQuantityEditText = dialogView.findViewById(R.id.dialog_round_pack_quantity);
        mTimberLengthEditText = dialogView.findViewById(R.id.dialog_round_pack_length);
        mTimberDiameterEditText = dialogView.findViewById(R.id.dialog_round_pack_diameter);

        if (!mNewTimberPack) {
            mTimberPackQuantityEditText.setText(String.valueOf(mTimberPack.getQuantity()));
            mTimberLengthEditText.setText(super.getString(R.string.length_format, super.mTimberPack.getLengthM()));
            mTimberDiameterEditText.setText(super.getString(R.string.length_format, super.mTimberPack.getRadiusM() * 2));
        }

        return super.mAlertDialog;
    }

    @Override
    public void onClick(final View view)
    {
        if (view == null) {
            return;
        }

        final String quantityInput = mTimberPackQuantityEditText.getText().toString();
        final String lengthInput = mTimberLengthEditText.getText().toString();
        final String diameterInput = mTimberDiameterEditText.getText().toString();

        try {
            super.mTimberPack.setQuantity(Integer.parseInt(quantityInput));
            super.mTimberPack.setLengthM(Double.parseDouble(lengthInput));
            super.mTimberPack.setRadiusM(Double.parseDouble(diameterInput) / 2);
            if (mTimberPack.getPackVolume() <= 0) {
                throw new IllegalArgumentException();
            }
        } catch (final Exception e) {
            DialogUtility.buildOkAlertDialog(
                    super.getActivity(),
                    R.string.dialog_title_invalid_timber_pack_dimensions,
                    R.string.dialog_message_invalid_timber_pack_dimensions
            );
            return;
        }

        final boolean valid = super.mTimberPackDialogListener.onValidateTimberPackDialogResult(super.mTimberPack, super.mNewTimberPack);
        if (valid) {
            super.mTimberPackDialogListener.onTimberPackDialogResult(super.mTimberPack, super.mNewTimberPack);
            super.dismiss();
        }
    }
}
