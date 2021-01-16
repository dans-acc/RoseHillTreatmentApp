package uk.co.rosehilltimber.rosehilltreatmentapp.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.TreatType;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.CuboidTimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.RoundTimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPackType;

import java.util.List;
import java.util.Locale;

public class TimberPackListAdapter extends EditableListAdapter<TimberPack>
{

    private static class TimberPackItemViewHolder
    {
        // The drawable image responsible for visualising the pack type.
        private ImageView mColourImageView;

        // Layouts encapsulating dynamic fields.
        private LinearLayout mDiameterTextViewFieldLayout;
        private LinearLayout mBreadthTextViewFieldLayout;
        private LinearLayout mHeightTextViewFieldLayout;

        // The individual field views used for displaying information.
        private TextView mTypeTextViewField;
        private TextView mVolumeTextViewField;
        private TextView mQuantityTextValueField;
        private TextView mLengthTextViewField;
        private TextView mDiameterTextViewField;
        private TextView mBreadthTextViewField;
        private TextView mHeightTextViewField;

        // The buttons employed to interact with the individual item.
        private ImageView mEditImageButton;
        private ImageView mDeleteImageButton;
    }

    private static final String VOLUME_FORMAT = "%.3f";
    private static final String LENGTH_FORMAT = "%.2f";

    private final TreatType mTreatType;

    public TimberPackListAdapter(final Activity mActivity,
                                 final View.OnClickListener mOnClickListener,
                                 final List<TimberPack> mTimberPacks,
                                 final boolean mEditable,
                                 final TreatType mTreatType)
    {
        super(
                mActivity,
                mOnClickListener,
                mTimberPacks,
                R.layout.item_timber_pack,
                mEditable
        );

        this.mTreatType = mTreatType;
    }

    public TimberPackListAdapter(final Activity mActivity,
                                 final View.OnClickListener mOnClickListener,
                                 final List<TimberPack> mTimberPacks,
                                 final TreatType mTreatType)
    {
        super(
                mActivity,
                mOnClickListener,
                mTimberPacks,
                R.layout.item_timber_pack
        );
        this.mTreatType = mTreatType;
    }

    @NonNull
    @Override
    public final View getView(final int position, @Nullable View view, @NonNull final ViewGroup viewGroup)
    {
        final TimberPackItemViewHolder timberPackItemViewHolder;
        if (view == null) {

            // Inflate a new instance of the item resource.
            view = LayoutInflater.from(super.mActivity).inflate(
                    super.mResource,
                    viewGroup,
                    BaseListAdapter.DEFAULT_ATTACH_TO_ROOT
            );

            // Create a view holder for obtaining faster access to the items components.
            timberPackItemViewHolder = new TimberPackItemViewHolder();

            // Init the item colour tag.
            timberPackItemViewHolder.mColourImageView = view.findViewById(R.id.item_treat_iv_colour_tag);

            // Init dynamic view holder fields.
            timberPackItemViewHolder.mDiameterTextViewFieldLayout = view.findViewById(R.id.item_timber_pack_layout_diameter_field);
            timberPackItemViewHolder.mBreadthTextViewFieldLayout = view.findViewById(R.id.item_timber_pack_layout_breadth_field);
            timberPackItemViewHolder.mHeightTextViewFieldLayout = view.findViewById(R.id.item_timber_pack_layout_height_field);

            // Init the view holder fields.
            timberPackItemViewHolder.mTypeTextViewField = view.findViewById(R.id.item_timber_pack_tv_type_field);
            timberPackItemViewHolder.mVolumeTextViewField = view.findViewById(R.id.item_timber_pack_tv_volume_field);
            timberPackItemViewHolder.mQuantityTextValueField = view.findViewById(R.id.item_timber_pack_tv_quantity_field);
            timberPackItemViewHolder.mLengthTextViewField = view.findViewById(R.id.item_timber_pack_tv_length_field);
            timberPackItemViewHolder.mDiameterTextViewField = timberPackItemViewHolder.mDiameterTextViewFieldLayout
                    .findViewById(R.id.item_timber_pack_tv_diameter_field);
            timberPackItemViewHolder.mBreadthTextViewField = timberPackItemViewHolder.mBreadthTextViewFieldLayout
                    .findViewById(R.id.item_timber_pack_tv_breadth_field);
            timberPackItemViewHolder.mHeightTextViewField = timberPackItemViewHolder.mHeightTextViewFieldLayout
                    .findViewById(R.id.item_timber_pack_tv_height_field);

            // Init the buttons used for editing / deleting the timber packs.
            timberPackItemViewHolder.mEditImageButton = view.findViewById(R.id.item_timber_pack_ib_edit_action);
            timberPackItemViewHolder.mDeleteImageButton = view.findViewById(R.id.item_timber_pack_ib_delete_action);

            // Set the on click listener.
            timberPackItemViewHolder.mEditImageButton.setOnClickListener(super.mOnClickListener);
            timberPackItemViewHolder.mDeleteImageButton.setOnClickListener(super.mOnClickListener);

            view.setTag(timberPackItemViewHolder);
        } else {
            timberPackItemViewHolder = (TimberPackItemViewHolder) view.getTag();
        }

        // Update the positions to represent the current item.
        timberPackItemViewHolder.mEditImageButton.setTag(position);
        timberPackItemViewHolder.mDeleteImageButton.setTag(position);

        // If the packs are not editable, hide the edit and delete buttons.
        if (super.mEditable) {
            timberPackItemViewHolder.mEditImageButton.setVisibility(View.VISIBLE);
            timberPackItemViewHolder.mDeleteImageButton.setVisibility(View.VISIBLE);
        } else {
            timberPackItemViewHolder.mEditImageButton.setVisibility(View.GONE);
            timberPackItemViewHolder.mDeleteImageButton.setVisibility(View.GONE);
        }

        final TimberPack timberPack = super.getItem(position);
        if (timberPack == null) {
            return view;
        }

        // Update the timber packs colour tag to represent the type of pack.
        final TimberPackType timberPackType = timberPack.getTimberPackType();
        if (mTreatType == TreatType.GREEN || mTreatType == TreatType.ROUND_GREEN) {
            if (timberPackType == TimberPackType.CUBOID) {
                timberPackItemViewHolder.mColourImageView.setImageResource(R.drawable.ic_treat_type_green);
            } else {
                timberPackItemViewHolder.mColourImageView.setImageResource(R.drawable.ic_treat_type_round_green);
            }
        } else {
            timberPackItemViewHolder.mColourImageView.setImageResource(R.drawable.ic_treat_type_brown);
        }

        // Update the type and volume fields.
        timberPackItemViewHolder.mTypeTextViewField.setText(timberPackType.getName());
        timberPackItemViewHolder.mVolumeTextViewField.setText(String.format(
                Locale.UK,
                VOLUME_FORMAT,
                timberPack.getPackVolume())
        );
        timberPackItemViewHolder.mQuantityTextValueField.setText(String.valueOf(timberPack.getQuantity()));

        // Update the fields depending on whether or not the pack is cuboid or round.
        if (timberPackType == TimberPackType.CUBOID) {

            // Enable and disable fields for a cuboid pack.
            timberPackItemViewHolder.mDiameterTextViewFieldLayout.setVisibility(View.GONE);
            timberPackItemViewHolder.mBreadthTextViewFieldLayout.setVisibility(View.VISIBLE);
            timberPackItemViewHolder.mHeightTextViewFieldLayout.setVisibility(View.VISIBLE);

            // Update the appropriate fields for the cuboid timber pack.
            final CuboidTimberPack cuboidTimberPack = (CuboidTimberPack) timberPack;
            timberPackItemViewHolder.mLengthTextViewField.setText(String.format(
                    Locale.UK,
                    LENGTH_FORMAT,
                    cuboidTimberPack.getLengthM())
            );
            timberPackItemViewHolder.mBreadthTextViewField.setText(String.valueOf(cuboidTimberPack.getBreadthMM()));
            timberPackItemViewHolder.mHeightTextViewField.setText(String.valueOf(cuboidTimberPack.getHeightMM()));
        } else {

            // Enable and disable appropriate fields for a round pack.
            timberPackItemViewHolder.mDiameterTextViewFieldLayout.setVisibility(View.VISIBLE);
            timberPackItemViewHolder.mBreadthTextViewFieldLayout.setVisibility(View.GONE);
            timberPackItemViewHolder.mHeightTextViewFieldLayout.setVisibility(View.GONE);

            // Update round pack fields.
            final RoundTimberPack roundTimberPack = (RoundTimberPack) timberPack;
            timberPackItemViewHolder.mLengthTextViewField.setText(String.format(
                    Locale.UK,
                    LENGTH_FORMAT,
                    roundTimberPack.getLengthM())
            );
            timberPackItemViewHolder.mDiameterTextViewField.setText(String.valueOf(roundTimberPack.getRadiusM() * 2));
        }

        return view;
    }
}
