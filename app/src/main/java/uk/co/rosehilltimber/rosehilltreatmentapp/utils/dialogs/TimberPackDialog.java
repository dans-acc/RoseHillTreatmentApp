package uk.co.rosehilltimber.rosehilltreatmentapp.utils.dialogs;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPack;

public abstract class TimberPackDialog<T extends TimberPack> extends AppCompatDialogFragment
    implements View.OnClickListener
{

    public interface TimberPackDialogListener
    {
        boolean onValidateTimberPackDialogResult(final TimberPack timberPack, final boolean newTimberPack);
        void onTimberPackDialogResult(final TimberPack timberPack, final boolean newTimberPack);
    }

    public static class BundleKey
    {
        public static final String TIMBER_PACK_INSTANCE = "TIMBER_PACK_INSTANCE";
    }

    protected T mTimberPack;
    protected boolean mNewTimberPack;

    protected AlertDialog mAlertDialog;

    protected TimberPackDialogListener mTimberPackDialogListener;

    @Override
    public void onAttach(final Context context)
    {
        super.onAttach(context);
        try {
            mTimberPackDialogListener = (TimberPackDialogListener) context;
        } catch (final ClassCastException e) {
            e.printStackTrace();
            throw new ClassCastException(context.toString() + " must implement " + TimberPackDialogListener.class.getName());
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
    }

}
