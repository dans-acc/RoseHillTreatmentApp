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
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.CuboidTimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPackType;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DialogUtility;

public class CuboidTimberPackDialog extends TimberPackDialog<CuboidTimberPack>
{

    private static final int INIT_CUBOID_TIMBER_PACK_QUANTITY = 0;
    private static final double INIT_CUBOID_TIMBER_PACK_LENGTH = 0d;
    private static final int INIT_CUBOID_TIMBER_PACK_BREADTH = 0;
    private static final int INIT_CUBOID_TIMBER_PACK_HEIGHT = 0;

    private EditText mTimberPackQuantityEditText;
    private EditText mTimberLengthEditText;
    private EditText mTimberBreadthEditText;
    private EditText mTimberHeightEditText;

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
            super.mTimberPack = new CuboidTimberPack(
                    INIT_CUBOID_TIMBER_PACK_QUANTITY,
                    INIT_CUBOID_TIMBER_PACK_LENGTH,
                    INIT_CUBOID_TIMBER_PACK_BREADTH,
                    INIT_CUBOID_TIMBER_PACK_HEIGHT
            );
            mNewTimberPack = true;
        } else if (bundleArguments.containsKey(BundleKey.TIMBER_PACK_INSTANCE)) {
            final CuboidTimberPack existingTimberPack = bundleArguments.getParcelable(BundleKey.TIMBER_PACK_INSTANCE);
            if (existingTimberPack == null) {
                super.setShowsDialog(false);
                super.dismiss();
                return null;
            } else {
                mTimberPack = new CuboidTimberPack(
                        existingTimberPack.getUUID(),
                        existingTimberPack.getQuantity(),
                        existingTimberPack.getLengthM(),
                        existingTimberPack.getBreadthMM(),
                        existingTimberPack.getHeightMM()
                );
                mNewTimberPack = false;
            }
        }

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        final LayoutInflater layoutInflater = activity.getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.dialog_cuboid_timber_pack, null);

        alertDialogBuilder.setTitle(TimberPackType.CUBOID.getName());
        alertDialogBuilder.setView(dialogView);
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

        mTimberPackQuantityEditText = dialogView.findViewById(R.id.dialog_cuboid_pack_et_quantity);
        mTimberLengthEditText = dialogView.findViewById(R.id.dialog_cuboid_pack_et_length);
        mTimberBreadthEditText = dialogView.findViewById(R.id.dialog_cuboid_pack_et_breadth);
        mTimberHeightEditText = dialogView.findViewById(R.id.dialog_cuboid_pack_et_height);

        if (!mNewTimberPack)  {
            mTimberPackQuantityEditText.setText(String.valueOf(super.mTimberPack.getQuantity()));
            mTimberLengthEditText.setText(super.getString(R.string.length_format, super.mTimberPack.getLengthM()));
            mTimberBreadthEditText.setText(String.valueOf(super.mTimberPack.getBreadthMM()));
            mTimberHeightEditText.setText(String.valueOf(super.mTimberPack.getHeightMM()));
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
        final String breadthInput = mTimberBreadthEditText.getText().toString();
        final String heightInput = mTimberHeightEditText.getText().toString();

        try {
            super.mTimberPack.setQuantity(Integer.parseInt(quantityInput));
            super.mTimberPack.setLengthM(Double.parseDouble(lengthInput));
            super.mTimberPack.setBreadthMM(Integer.parseInt(breadthInput));
            super.mTimberPack.setHeightMM(Integer.parseInt(heightInput));
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
