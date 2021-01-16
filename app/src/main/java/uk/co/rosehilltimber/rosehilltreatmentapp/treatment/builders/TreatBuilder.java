package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.builders;

import android.content.ContentProviderOperation;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.widget.ProgressBar;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.TreatType;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.database.TreatContract;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.CuboidTimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.RoundTimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPack;

import java.time.LocalDate;
import java.util.*;

public abstract class TreatBuilder
{

    private static final boolean NEW_TRANSACTION = false;

    protected UUID mUUID;
    protected LocalDate mWeekDate;
    protected TreatType mType;
    protected List<TimberPack> mTimberPacks;

    protected float mMaximumTankVolume;

    protected Stack<Pair<UUID, Pair<TreatBuilderOperation, TimberPack>>> mUndoStack;
    protected Map<UUID, Stack<TreatBuilderOperation>> mBuildMap;

    protected TreatBuilder(final UUID mUUID,
                           final LocalDate mWeekDate,
                           final TreatType mType,
                           final List<TimberPack> mTimberPacks,
                           final float mMaximumTankVolume)
    {
        this.mUUID = mUUID;
        this.mWeekDate = mWeekDate;
        this.mType = mType;
        this.mTimberPacks = mTimberPacks;

        this.mMaximumTankVolume = mMaximumTankVolume;

        mUndoStack = new Stack<Pair<UUID, Pair<TreatBuilderOperation, TimberPack>>>();
        mBuildMap = new HashMap<UUID, Stack<TreatBuilderOperation>>();
    }

    public LocalDate getWeekDate()
    {
        return mWeekDate;
    }

    public void setWeekDate(final LocalDate mWeekDate)
    {
        this.mWeekDate = mWeekDate;
    }

    public UUID getUUID()
    {
        return mUUID;
    }

    public void setUUID(final UUID mUUID)
    {
        this.mUUID = mUUID;
    }

    public TreatType getType()
    {
        return mType;
    }

    public void setType(final TreatType mType)
    {
        this.mType = mType;
    }

    public List<TimberPack> getTimberPacks()
    {
        return mTimberPacks;
    }

    public TimberPack getTimberPackByUUID(final UUID uuid)
    {
        for (final TimberPack timberPack : mTimberPacks) {
            if (timberPack.getUUID().equals(uuid)) {
                return timberPack;
            }
        }
        return null;
    }

    public float getMaximumTankVolume()
    {
        return mMaximumTankVolume;
    }

    public void setMaximumTankVolume(final float mMaximumTankVolume)
    {
        this.mMaximumTankVolume = mMaximumTankVolume;
    }

    public double getTankVolume()
    {
        double totalTreatVolume = 0;
        for (final TimberPack timberPack : mTimberPacks) {
            totalTreatVolume += timberPack.getPackVolume();
        }
        return totalTreatVolume;
    }

    public boolean hasTankCapacity()
    {
        return getTankVolume() < mMaximumTankVolume;
    }

    public boolean hasTankCapacity(final TimberPack timberPack)
    {
        final TimberPack existingTimberPack = getTimberPackByUUID(timberPack.getUUID());
        if (existingTimberPack != null) {
            return getTankVolume() - existingTimberPack.getPackVolume() + timberPack.getPackVolume() <= mMaximumTankVolume;
        } else {
            return getTankVolume() + timberPack.getPackVolume() <= mMaximumTankVolume;
        }
    }

    public Stack<Pair<UUID, Pair<TreatBuilderOperation, TimberPack>>> getUndoStack()
    {
        return mUndoStack;
    }

    public Map<UUID, Stack<TreatBuilderOperation>> getBuildMap()
    {
        return mBuildMap;
    }

    public boolean hasBuildUpdates()
    {
        for (final Map.Entry<UUID, Stack<TreatBuilderOperation>> buildEntry : mBuildMap.entrySet()) {
            if (getTimberPackBuildOperation(buildEntry.getValue()) != null) {
                return true;
            }
        }
        return false;
    }

    public void createTimberPack(final TimberPack timberPack)
    {
        createUndoStackEntry(timberPack.getUUID(), TreatBuilderOperation.CREATE, timberPack);
        createBuildMapEntry(timberPack, TreatBuilderOperation.CREATE);

        mTimberPacks.add(timberPack);
    }

    public void updateTimberPack(final TimberPack timberPack)
    {
        final TimberPack existingTimberPack = getTimberPackByUUID(timberPack.getUUID());
        createUndoStackEntry(existingTimberPack.getUUID(), TreatBuilderOperation.UPDATE, existingTimberPack);
        createBuildMapEntry(timberPack, TreatBuilderOperation.UPDATE);

        mTimberPacks.remove(existingTimberPack);
        mTimberPacks.add(timberPack);
    }

    public void deleteTimberPack(final TimberPack timberPack)
    {
        createUndoStackEntry(timberPack.getUUID(), TreatBuilderOperation.DELETE, timberPack);
        createBuildMapEntry(timberPack, TreatBuilderOperation.DELETE);

        mTimberPacks.remove(timberPack);
    }

    private void createUndoStackEntry(final UUID uuid,
                                      final TreatBuilderOperation treatBuilderOperation,
                                      final TimberPack timberPack)
    {
        final Pair<UUID, Pair<TreatBuilderOperation, TimberPack>> undoEntry
                = new Pair<>(uuid, new Pair<>(treatBuilderOperation, timberPack));
        mUndoStack.push(undoEntry);
    }

    private void createBuildMapEntry(final TimberPack timberPack, final TreatBuilderOperation treatBuilderOperation)
    {
        final UUID timberPackUUID = timberPack.getUUID();
        Stack<TreatBuilderOperation> timberPackBuildStack = mBuildMap.get(timberPackUUID);
        if (timberPackBuildStack == null) {
            timberPackBuildStack = new Stack<TreatBuilderOperation>();
            timberPackBuildStack.push(treatBuilderOperation);
            mBuildMap.put(timberPackUUID, timberPackBuildStack);
        } else {
            timberPackBuildStack.push(treatBuilderOperation);
        }
    }

    public void undoAllTreatBuilderOperations()
    {
        while (!mUndoStack.isEmpty()) {
            undoTreatBuilderOperation();
        }
    }

    public Pair<UUID, Pair<TreatBuilderOperation, TimberPack>>  undoTreatBuilderOperation()
    {
        if (mUndoStack.isEmpty()) {
            return null;
        }

        final Pair<UUID, Pair<TreatBuilderOperation, TimberPack>> undoEntry = mUndoStack.pop();
        final UUID timberPackUUID = undoEntry.first;
        final TreatBuilderOperation treatBuilderOperation = undoEntry.second.first;
        final TimberPack timberPack = undoEntry.second.second;

        switch (treatBuilderOperation) {
            case CREATE:
                mTimberPacks.remove(timberPack);
                break;
            case UPDATE:
                final TimberPack currentTimberPack = getTimberPackByUUID(timberPackUUID);
                mTimberPacks.remove(currentTimberPack);
                mTimberPacks.add(timberPack);
                break;
            case DELETE:
                mTimberPacks.add(timberPack);
                break;
        }

        final Stack<TreatBuilderOperation> timberPackBuildStack = mBuildMap.get(timberPackUUID);
        if (timberPackBuildStack != null) {
            timberPackBuildStack.pop();
            if (timberPackBuildStack.isEmpty()) {
                mBuildMap.remove(timberPackUUID);
            }
        }

        return undoEntry;
    }

    public void notifyResultSuccessful()
    {
        mBuildMap.clear();
        mUndoStack.clear();
    }

    public ArrayList<ContentProviderOperation> buildContentProviderOperations()
    {
        final ArrayList<ContentProviderOperation> contentProviderOperations = new ArrayList<>(mBuildMap.size());

        TreatBuilderOperation buildOperation = null;
        ContentProviderOperation contentProviderOperation = null;
        for (final Map.Entry<UUID, Stack<TreatBuilderOperation>> buildEntry : mBuildMap.entrySet()) {

            buildOperation = getTimberPackBuildOperation(buildEntry.getValue());
            if (buildOperation == null) {
                Log.wtf("Operation ignored", buildEntry.toString());
                continue;
            }

            switch (buildOperation) {
                case CREATE:
                    contentProviderOperation = buildInsertContentProviderOperation(getTimberPackByUUID(buildEntry.getKey()));
                    break;
                case UPDATE:
                    contentProviderOperation = buildUpdateContentProviderOperation(getTimberPackByUUID(buildEntry.getKey()));
                    break;
                case DELETE:
                    contentProviderOperation = buildDeleteContentProviderOperation(buildEntry.getKey());
                    break;
            }

            if (contentProviderOperation != null) {
                contentProviderOperations.add(contentProviderOperation);
            }
        }

        return contentProviderOperations;
    }

    private TreatBuilderOperation getTimberPackBuildOperation(final Stack<TreatBuilderOperation> timberPackBuildStack)
    {
        if (timberPackBuildStack == null) {
            return null;
        } else if (timberPackBuildStack.size() == 1) {
            return timberPackBuildStack.peek();
        }

        final TreatBuilderOperation firstOperation = timberPackBuildStack.firstElement(),
                lastOperation = timberPackBuildStack.lastElement();

        if (firstOperation == lastOperation) {
            return firstOperation;
        } else if (firstOperation == TreatBuilderOperation.CREATE && lastOperation == TreatBuilderOperation.UPDATE) {
            return TreatBuilderOperation.CREATE;
        } else if (firstOperation == TreatBuilderOperation.UPDATE && lastOperation == TreatBuilderOperation.DELETE) {
            return TreatBuilderOperation.DELETE;
        }

        return null;
    }

    private ContentProviderOperation buildInsertContentProviderOperation(final TimberPack timberPack)
    {
        if (timberPack instanceof CuboidTimberPack) {

            final CuboidTimberPack cuboidTimberPack = (CuboidTimberPack) timberPack;
            final Uri newCuboidTimberPackUri = TreatContract.CuboidTimberPackEntry.buildCuboidTimberPackUri(
                    cuboidTimberPack.getUUID(),
                    NEW_TRANSACTION
            );

            return ContentProviderOperation.newInsert(newCuboidTimberPackUri)
                    .withValue(TreatContract.CuboidTimberPackEntry._ID, cuboidTimberPack.getUUID().toString())
                    .withValue(TreatContract.CuboidTimberPackEntry.COLUMN_TREAT_ID, mUUID.toString())
                    .withValue(TreatContract.CuboidTimberPackEntry.COLUMN_QUANTITY, cuboidTimberPack.getQuantity())
                    .withValue(TreatContract.CuboidTimberPackEntry.COLUMN_LENGTH, cuboidTimberPack.getLengthM())
                    .withValue(TreatContract.CuboidTimberPackEntry.COLUMN_BREADTH, cuboidTimberPack.getBreadthMM())
                    .withValue(TreatContract.CuboidTimberPackEntry.COLUMN_HEIGHT, cuboidTimberPack.getHeightMM())
                    .build();

        } else {

            final RoundTimberPack roundTimberPack = (RoundTimberPack) timberPack;
            final Uri newRoundTimberPackUri = TreatContract.RoundTimberPackEntry.buildRoundTimberPackUri(
                    roundTimberPack.getUUID(),
                    NEW_TRANSACTION
            );

            return ContentProviderOperation.newInsert(newRoundTimberPackUri)
                    .withValue(TreatContract.RoundTimberPackEntry._ID, roundTimberPack.getUUID().toString())
                    .withValue(TreatContract.RoundTimberPackEntry.COLUMN_TREAT_ID, mUUID.toString())
                    .withValue(TreatContract.RoundTimberPackEntry.COLUMN_QUANTITY, roundTimberPack.getQuantity())
                    .withValue(TreatContract.RoundTimberPackEntry.COLUMN_LENGTH, roundTimberPack.getLengthM())
                    .withValue(TreatContract.RoundTimberPackEntry.COLUMN_RADIUS, roundTimberPack.getRadiusM())
                    .build();
        }
    }

    private ContentProviderOperation buildUpdateContentProviderOperation(final TimberPack timberPack)
    {
        final String[] selectionArgs = {timberPack.getUUID().toString()};

        if (timberPack instanceof CuboidTimberPack) {

            Log.wtf("Building update for: ", "Pack type: " + timberPack.getTimberPackType());

            final CuboidTimberPack cuboidTimberPack = (CuboidTimberPack) timberPack;
            final Uri newCuboidTimberPackUri = TreatContract.CuboidTimberPackEntry.buildCuboidTimberPackUri(
                    cuboidTimberPack.getUUID(),
                    NEW_TRANSACTION
            );

            final String cuboidSelection = TreatContract.CuboidTimberPackEntry._ID + " = ?";
            return ContentProviderOperation.newUpdate(newCuboidTimberPackUri)
                    .withSelection(cuboidSelection, selectionArgs)
                    .withValue(TreatContract.CuboidTimberPackEntry.COLUMN_QUANTITY, cuboidTimberPack.getQuantity())
                    .withValue(TreatContract.CuboidTimberPackEntry.COLUMN_LENGTH, cuboidTimberPack.getLengthM())
                    .withValue(TreatContract.CuboidTimberPackEntry.COLUMN_BREADTH, cuboidTimberPack.getBreadthMM())
                    .withValue(TreatContract.CuboidTimberPackEntry.COLUMN_HEIGHT, cuboidTimberPack.getHeightMM())
                    .build();

        } else {

            Log.wtf("Building update for: ", "Pack type: " + timberPack.getTimberPackType());

            final RoundTimberPack roundTimberPack = (RoundTimberPack) timberPack;
            final Uri newRoundTimberPackUri = TreatContract.RoundTimberPackEntry.buildRoundTimberPackUri(
                    roundTimberPack.getUUID(),
                    NEW_TRANSACTION
            );

            final String roundSelection = TreatContract.RoundTimberPackEntry._ID + " = ?";
            return ContentProviderOperation.newUpdate(newRoundTimberPackUri)
                    .withSelection(roundSelection, selectionArgs)
                    .withValue(TreatContract.RoundTimberPackEntry.COLUMN_QUANTITY, roundTimberPack.getQuantity())
                    .withValue(TreatContract.RoundTimberPackEntry.COLUMN_LENGTH, roundTimberPack.getLengthM())
                    .withValue(TreatContract.RoundTimberPackEntry.COLUMN_RADIUS, roundTimberPack.getRadiusM())
                    .build();
        }
    }

    private ContentProviderOperation buildDeleteContentProviderOperation(final UUID uuid)
    {

        Pair<UUID, Pair<TreatBuilderOperation, TimberPack>> undoStackEntry = null;
        TimberPack deleteTimberPack = null;
        Uri deleteTimberPackUri = null;

        final Iterator<Pair<UUID, Pair<TreatBuilderOperation, TimberPack>>> iterator = mUndoStack.iterator();
        while (iterator.hasNext()) {

            undoStackEntry = iterator.next();
            if (!undoStackEntry.first.equals(uuid)) {
                continue;
            }

            deleteTimberPack = undoStackEntry.second.second;
            deleteTimberPackUri = deleteTimberPack instanceof CuboidTimberPack
                    ? TreatContract.CuboidTimberPackEntry.buildCuboidTimberPackUri(deleteTimberPack.getUUID(), NEW_TRANSACTION)
                    : TreatContract.RoundTimberPackEntry.buildRoundTimberPackUri(deleteTimberPack.getUUID(), NEW_TRANSACTION);

            return ContentProviderOperation.newDelete(deleteTimberPackUri).build();
        }

        return null;
    }
}
