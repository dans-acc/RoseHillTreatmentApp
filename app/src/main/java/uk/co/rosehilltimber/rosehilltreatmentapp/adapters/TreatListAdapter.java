package uk.co.rosehilltimber.rosehilltreatmentapp.adapters;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.TreatType;

public class TreatListAdapter extends EditableListAdapter<Treat>
{

    private static class TreatItemViewHolder
    {
        private ImageView mColourImageView;
        private TextView mNumberTextViewField;
        private TextView mPacksTextViewField;
        private TextView mVolumeTextViewField;
        private TextView mTypeTextViewField;
        private ImageButton mDeleteImageButton;
    }

    @SuppressWarnings("WeakerAccess")
    public TreatListAdapter(final Activity mActivity,
                            final View.OnClickListener mOnClickListener,
                            final List<Treat> mTreats,
                            final boolean mEditable)
    {
        super(
                mActivity,
                mOnClickListener,
                mTreats,
                R.layout.item_treat,
                mEditable
        );
    }

    public TreatListAdapter(final Activity mActivity,
                            final View.OnClickListener mOnClickListener,
                            final List<Treat> mTreats)
    {
        super(
                mActivity,
                mOnClickListener,
                mTreats,
                R.layout.item_treat
        );
    }

    @NonNull
    @Override
    public final View getView(final int position, @Nullable View view, @NonNull final ViewGroup viewGroup)
    {
        final TreatItemViewHolder treatItemViewHolder;
        if (view == null) {

            // Create a new instance of the treat item layout.
            view = LayoutInflater.from(super.mActivity).inflate(
                    super.mResource,
                    viewGroup,
                    BaseListAdapter.DEFAULT_ATTACH_TO_ROOT
            );

            // Cache the views components for faster access.
            treatItemViewHolder = new TreatItemViewHolder();
            treatItemViewHolder.mColourImageView = view.findViewById(R.id.item_treat_iv_colour_tag);
            treatItemViewHolder.mNumberTextViewField = view.findViewById(R.id.item_treat_tv_number_field);
            treatItemViewHolder.mPacksTextViewField = view.findViewById(R.id.item_treat_tv_packs_count_field);
            treatItemViewHolder.mVolumeTextViewField = view.findViewById(R.id.item_treat_tv_volume_field);
            treatItemViewHolder.mTypeTextViewField = view.findViewById(R.id.item_treat_tv_type_field);
            treatItemViewHolder.mDeleteImageButton = view.findViewById(R.id.item_treat_ib_delete_action);

            // Set the on click listeners.
            treatItemViewHolder.mDeleteImageButton.setOnClickListener(super.mOnClickListener);

            view.setTag(treatItemViewHolder);
        } else {
            treatItemViewHolder = (TreatItemViewHolder) view.getTag();
        }

        // Update the button positions.
        treatItemViewHolder.mDeleteImageButton.setTag(position);

        // 'Enable' / 'Disable' the items buttons depending on whether or not the view is editable.
        if (super.mEditable) {
            treatItemViewHolder.mDeleteImageButton.setVisibility(View.VISIBLE);
        } else {
            treatItemViewHolder.mDeleteImageButton.setVisibility(View.GONE);
        }

        final Treat treat = super.getItem(position);
        if (treat == null) {
            return view;
        }

        // Update the treat colour tag to depict the type of treatment.
        final TreatType treatType = treat.getType();
        switch (treatType) {
            case GREEN:
                treatItemViewHolder.mColourImageView.setImageResource(R.drawable.ic_treat_type_green);
                break;
            case ROUND_GREEN:
                treatItemViewHolder.mColourImageView.setImageResource(R.drawable.ic_treat_type_round_green);
                break;
            case BROWN:
                treatItemViewHolder.mColourImageView.setImageResource(R.drawable.ic_treat_type_brown);
                break;
        }

        // Update the view with the relevant information.
        treatItemViewHolder.mNumberTextViewField.setText(String.valueOf(treat.getNumber()));
        treatItemViewHolder.mPacksTextViewField.setText(String.valueOf(treat.getTimberPacks().size()));
        treatItemViewHolder.mVolumeTextViewField.setText(super.mActivity.getString(R.string.volume_format,
                treat.getTotalTreatVolume()));
        treatItemViewHolder.mTypeTextViewField.setText(treatType.getName());

        return view;
    }

}
