package uk.co.rosehilltimber.rosehilltreatmentapp.utils;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.WorkerThread;
import android.util.Log;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.TreatType;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.database.TreatContract;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.CuboidTimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.RoundTimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPack;

import java.time.LocalDate;
import java.util.*;

public class DatabaseUtility
{

    @WorkerThread
    public static Treat selectTreat(final ContentResolver contentResolver,
                                    final UUID uuid)
    {
        Cursor cursor = null;
        try {

            final String selection = TreatContract.TreatEntry._ID + " = ?";
            final String[] selectionArgs = {uuid.toString()};

            cursor = contentResolver.query(
                    TreatContract.TreatEntry.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    null
            );

            final Treat treat = treatFromCursor(
                    cursor,
                    uuid
            );
            if (treat == null) {
                return null;
            }

            if (treat.getType() == TreatType.ROUND_GREEN) {
                final List<TimberPack> timberPacks = DatabaseUtility.selectTreatTimberPacks(contentResolver, uuid, treat.getType());
                if (timberPacks == null) {
                    return null;
                }
                treat.getTimberPacks().addAll(timberPacks);
            } else {
                final List<CuboidTimberPack> cuboidTimberPacks = DatabaseUtility.selectTreatCuboidTimberPacks(contentResolver, uuid);
                if (cuboidTimberPacks == null) {
                    return null;
                }
                treat.getTimberPacks().addAll(cuboidTimberPacks);
            }

            return treat;
        } catch (final SQLiteException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @WorkerThread
    public static List<Treat> selectAllTreats(final ContentResolver contentResolver)
    {
        return DatabaseUtility.selectAllTreats(contentResolver, null, null);
    }

    @WorkerThread
    public static List<Treat> selectAllTreats(final ContentResolver contentResolver,
                                              final LocalDate weekDate)
    {
        if (weekDate == null) {
            return DatabaseUtility.selectAllTreats(contentResolver,
                    null, null);
        }

        final String selection = TreatContract.TreatEntry.COLUMN_DATE + " = ?";
        final String[] selectionArgs = {weekDate.format(
                DateUtility.DATABASE_DATE_FORMATTER)};

        return DatabaseUtility.selectAllTreats(
                contentResolver,
                selection,
                selectionArgs
        );
    }

    @WorkerThread
    private static List<Treat> selectAllTreats(final ContentResolver contentResolver,
                                               final String selection,
                                               final String[] selectionArgs)
    {
        Cursor cursor = null;
        try {

            cursor = contentResolver.query(
                    TreatContract.TreatEntry.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    null
            );

            if (cursor != null && cursor.getCount() == 0) {
                return new ArrayList<>();
            } else if (isCursorInvalid(cursor)) {
                return null;
            }

            final List<Treat> treats = new ArrayList<>(cursor.getCount());

            Treat treat = null;
            List<TimberPack> timberPacks = null;
            while (cursor.moveToNext()) {

                treat = treatFromCursor(cursor);
                if (treat == null) {
                    continue;
                }

                timberPacks = DatabaseUtility.selectTreatTimberPacks(
                        contentResolver,
                        treat.getUUID(),
                        treat.getType()
                );

                if (timberPacks == null) {
                    return null;
                }

                treat.getTimberPacks().addAll(timberPacks);
                treats.add(treat);
            }

            return treats;
        } catch (final SQLiteException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @WorkerThread
    public static boolean deleteTreats(final ContentResolver contentResolver,
                                       final UUID... treatUUIDs)
    {
        return deleteTreats(contentResolver, Arrays.asList(treatUUIDs));
    }

    @WorkerThread
    public static boolean deleteTreats(final ContentResolver contentResolver,
                                       final LocalDate localDate)
    {
        final String selection = TreatContract.TreatEntry.COLUMN_DATE + " = ?";
        final String[] selectionArgs = {localDate.format(DateUtility.DATABASE_DATE_FORMATTER)};
        return DatabaseUtility.deleteTreat(
                contentResolver,
                selection,
                selectionArgs
        );
    }

    @WorkerThread
    public static boolean deleteTreats(final ContentResolver contentResolver,
                                       final List<UUID> treatUUIDs)
    {
        if (treatUUIDs.isEmpty()) {
            return false;
        }

        final String selection = TreatContract.TreatEntry._ID + " = ?";
        final ArrayList<ContentProviderOperation> deleteOperations = new ArrayList<>(treatUUIDs.size());

        Uri deletedTreatUri = null;
        for (final UUID uuid : treatUUIDs) {
            deletedTreatUri = TreatContract.TreatEntry.buildTreatUri(uuid, false);
            deleteOperations.add(ContentProviderOperation.newDelete(deletedTreatUri)
                    .withSelection(
                            selection,
                            new String[]{uuid.toString()})
                    .build());
        }

        try {
            final ContentProviderResult[] contentProviderResults = contentResolver.applyBatch(
                    TreatContract.CONTENT_AUTHORITY,
                    deleteOperations
            );
            return contentProviderResults.length == deleteOperations.size();
        } catch (final SQLiteException
                | OperationApplicationException
                | RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @WorkerThread
    public static boolean deleteTreat(final ContentResolver contentResolver,
                                      final Treat treat)
    {
        return deleteTreat(contentResolver, treat.getUUID());
    }

    @WorkerThread
    public static boolean deleteTreat(final ContentResolver contentResolver,
                                      final UUID treatUUID)
    {
        final String selection = TreatContract.TreatEntry._ID + " = ?";
        final String[] selectionArgs = {treatUUID.toString()};
        return deleteTreat(contentResolver, selection, selectionArgs);
    }

    @WorkerThread
    public static boolean deleteTreat(final ContentResolver contentResolver,
                                      final String selection,
                                      final String[] selectionArgs)
    {
        try {
            final int rowsAffected = contentResolver.delete(
                    TreatContract.TreatEntry.CONTENT_URI,
                    selection,
                    selectionArgs
            );
            return rowsAffected > 0;
        } catch (final SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }



    public static boolean deleteTimberPacks(final ContentResolver contentResolver,
                                            final List<TimberPack> timberPacks)
    {
        if (timberPacks.isEmpty()) {
            return true;
        }

        final String cuboidTableSelection = TreatContract.CuboidTimberPackEntry._ID + " = ?",
                roundTableSelection = TreatContract.RoundTimberPackEntry._ID + " = ?";

        final ArrayList<ContentProviderOperation> deleteOperations = new ArrayList<>(timberPacks.size());

        Uri deleteTimberPackUri = null;
        String selection = null;
        for (final TimberPack timberPack : timberPacks) {
            if (timberPack instanceof CuboidTimberPack) {
                deleteTimberPackUri = TreatContract.CuboidTimberPackEntry
                        .buildCuboidTimberPackUri(timberPack.getUUID(), false);
                selection = cuboidTableSelection;
            } else {
                deleteTimberPackUri = TreatContract.RoundTimberPackEntry
                        .buildTreatRoundTimberPackUri(timberPack.getUUID(), false);
                selection = roundTableSelection;
            }
            deleteOperations.add(ContentProviderOperation.newDelete(deleteTimberPackUri)
                    .withSelection(selection, new String[]{timberPack.getUUID().toString()})
                    .build());
        }

        try {
            final ContentProviderResult[] contentProviderResults = contentResolver.applyBatch(
                    TreatContract.CONTENT_AUTHORITY,
                    deleteOperations
            );
            return contentProviderResults.length > 0;
        } catch (final SQLiteException
                | OperationApplicationException
                | RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteCuboidTimberPack(final ContentResolver contentResolver,
                                                 final CuboidTimberPack cuboidTimberPack)
    {
        final String selection = TreatContract.CuboidTimberPackEntry._ID + " = ?";
        final String[] selectionArgs = new String[] {cuboidTimberPack.getUUID().toString()};

        return DatabaseUtility.deleteTreat(
                contentResolver,
                selection,
                selectionArgs
        );
    }

    public static boolean deleteCuboidTimberPacks(final ContentResolver contentResolver,
                                                  final String selection,
                                                  final String[] selectionArgs)
    {
        try {
            final int rowsAffected = contentResolver.delete(
                    TreatContract.CuboidTimberPackEntry.CONTENT_URI,
                    selection,
                    selectionArgs
            );
            return rowsAffected > 0;
        } catch (final SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteRoundTimberPack(final ContentResolver contentResolver,
                                                final RoundTimberPack roundTimberPack)
    {
        final String selection = TreatContract.RoundTimberPackEntry._ID + " = ?";
        final String[] selectionArgs = {roundTimberPack.getUUID().toString()};

        return deleteCuboidTimberPacks(
                contentResolver,
                selection,
                selectionArgs
        );
    }

    public static boolean deleteRoundTimberPacks(final ContentResolver contentResolver,
                                                 final String selection,
                                                 final String[] selectionArgs)
    {
        try {
            final int rowsAffected = contentResolver.delete(
                    TreatContract.RoundTimberPackEntry.CONTENT_URI,
                    selection,
                    selectionArgs
            );
            return rowsAffected > 0;
        } catch (final SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @WorkerThread
    public static Map<UUID, List<TimberPack>> selectAllTimberPacks(final ContentResolver contentResolver)
    {
        final Cursor cuboidTimberPackCursor = selectCuboidTimberPacks(contentResolver, null, null);
        final Cursor roundTimberPackCursor = selectRoundTimberPacks(contentResolver, null, null);
        if (cuboidTimberPackCursor == null || roundTimberPackCursor == null) {
            if (cuboidTimberPackCursor != null) {
                cuboidTimberPackCursor.close();
            }
            if (roundTimberPackCursor != null) {
                roundTimberPackCursor.close();
            }
            return null;
        } else if (!cuboidTimberPackCursor.moveToFirst() || !roundTimberPackCursor.moveToFirst()) {
            cuboidTimberPackCursor.close();
            roundTimberPackCursor.close();
        }

        final Map<UUID, List<TimberPack>> timberPackMap = new HashMap<>();

        UUID uuid = null;
        List<TimberPack> timberPacks = null;

        CuboidTimberPack cuboidTimberPack = null;
        while (cuboidTimberPackCursor.moveToNext()) {

            uuid = UUID.fromString(cuboidTimberPackCursor.getString(cuboidTimberPackCursor
                    .getColumnIndex(TreatContract.CuboidTimberPackEntry.COLUMN_TREAT_ID)));
            if (timberPackMap.containsKey(uuid)) {
                timberPacks = timberPackMap.get(uuid);
            } else {
                timberPacks = new ArrayList<>();
                timberPackMap.put(uuid, timberPacks);
            }

            if (timberPacks == null) {
                throw new NullPointerException("Cuboid timber pack list is null.");
            }

            cuboidTimberPack = DatabaseUtility.cuboidTimberPackFromCursor(cuboidTimberPackCursor);
            timberPacks.add(cuboidTimberPack);
        }
        cuboidTimberPackCursor.close();

        RoundTimberPack roundTimberPack = null;
        while (roundTimberPackCursor.moveToNext()) {

            uuid = UUID.fromString(roundTimberPackCursor.getString(roundTimberPackCursor
                    .getColumnIndex(TreatContract.RoundTimberPackEntry.COLUMN_TREAT_ID)));
            if (timberPackMap.containsKey(uuid)) {
                timberPacks = timberPackMap.get(uuid);
            } else {
                timberPacks = new ArrayList<>();
                timberPackMap.put(uuid, timberPacks);
            }

            if (timberPacks == null) {
                throw new NullPointerException("Round timber pack list is null.");
            }

            roundTimberPack = roundTimberPackFromCursor(roundTimberPackCursor);
            timberPacks.add(roundTimberPack);
        }
        roundTimberPackCursor.close();

        return timberPackMap;
    }

    @WorkerThread
    public static List<TimberPack> selectTreatTimberPacks(final ContentResolver contentResolver,
                                                          final UUID treatUUID,
                                                          final TreatType treatType) {
        if (contentResolver == null) {
            return null;
        } else if (treatUUID == null || treatUUID.toString().isEmpty()) {
            return null;
        } else if (treatType == null) {
            return null;
        }

        final List<TimberPack> timberPacks = new ArrayList<>();
        if (treatType == TreatType.ROUND_GREEN) {
            final List<RoundTimberPack> roundTimberPacks = selectTreatRoundTimberPacks(
                    contentResolver,
                    treatUUID
            );
            if (roundTimberPacks == null) {
                return null;
            }
            timberPacks.addAll(roundTimberPacks);
        }

        final List<CuboidTimberPack> cuboidTimberPacks = DatabaseUtility.selectTreatCuboidTimberPacks(
                contentResolver,
                treatUUID
        );
        if (cuboidTimberPacks == null) {
            return null;
        }
        timberPacks.addAll(cuboidTimberPacks);

        return timberPacks;
    }

    @WorkerThread
    public static List<CuboidTimberPack> selectTreatCuboidTimberPacks(final ContentResolver contentResolver,
                                                                      final UUID treatUUID)
    {
        final String selection = TreatContract.CuboidTimberPackEntry.COLUMN_TREAT_ID + " = ?";
        final String[] selectionArgs = {treatUUID.toString()};

        final Cursor cursor = DatabaseUtility.selectCuboidTimberPacks(
                contentResolver,
                selection,
                selectionArgs
        );

        if (cursor == null) {
            return null;
        }

        final List<CuboidTimberPack> cuboidTimberPacks = new ArrayList<>(cursor.getCount());
        if (cursor.getCount() == 0) {
            cursor.close();
            return cuboidTimberPacks;
        }

        CuboidTimberPack cuboidTimberPack = null;
        while (cursor.moveToNext()) {
            cuboidTimberPack = DatabaseUtility.cuboidTimberPackFromCursor(cursor);
            if (cuboidTimberPack != null) {
                cuboidTimberPacks.add(cuboidTimberPack);
            }
        }

        cursor.close();
        return cuboidTimberPacks;
    }

    @WorkerThread
    public static Cursor selectCuboidTimberPacks(final ContentResolver contentResolver,
                                                 final String selection,
                                                 final String[] selectionArgs)
    {
        try {
            return contentResolver.query(
                    TreatContract.CuboidTimberPackEntry.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    null
            );
        } catch (final SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    @WorkerThread
    public static List<RoundTimberPack> selectTreatRoundTimberPacks(final ContentResolver contentResolver,
                                                                    final UUID treatUUID)
    {
        final String selection = TreatContract.RoundTimberPackEntry.COLUMN_TREAT_ID + " = ?";
        final String[] selectionArgs = {treatUUID.toString()};
        final Cursor cursor = selectRoundTimberPacks(
                contentResolver,
                selection,
                selectionArgs
        );

        if (cursor == null) {
            return null;
        }

        final List<RoundTimberPack> roundTimberPacks = new ArrayList<>(cursor.getCount());
        if (cursor.getCount() == 0) {
            cursor.close();
            return roundTimberPacks;
        }

        RoundTimberPack roundTimberPack = null;
        while (cursor.moveToNext()) {
            roundTimberPack = DatabaseUtility.roundTimberPackFromCursor(cursor);
            if (roundTimberPack != null) {
                roundTimberPacks.add(roundTimberPack);
            }
        }

        cursor.close();
        return roundTimberPacks;
    }

    @WorkerThread
    public static Cursor selectRoundTimberPacks(final ContentResolver contentResolver,
                                                final String selection,
                                                final String[] selectionArgs)
    {
        try {
            return contentResolver.query(
                    TreatContract.RoundTimberPackEntry.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    null
            );
        } catch (final SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    @WorkerThread
    public static Treat treatFromCursor(final Cursor cursor)
    {
        if (DatabaseUtility.isCursorInvalid(cursor)) {
            return null;
        } else if (cursor.isBeforeFirst() && !cursor.moveToFirst()) {
            return null;
        }

        return DatabaseUtility.treatFromCursor(
                cursor,
                UUID.fromString(cursor.getString(cursor.getColumnIndex(TreatContract.TreatEntry._ID)))
        );
    }

    @WorkerThread
    public static Treat treatFromCursor(final Cursor cursor, final UUID treatUUID)
    {
        if (DatabaseUtility.isCursorInvalid(cursor)) {
            return null;
        } else if (cursor.isBeforeFirst() && !cursor.moveToFirst()) {
            return null;
        }

        return new Treat(
                treatUUID,
                cursor.getInt(cursor.getColumnIndexOrThrow(TreatContract.TreatEntry.COLUMN_NUMBER)),
                DateUtility.fromString(
                        cursor.getString(cursor.getColumnIndexOrThrow(TreatContract.TreatEntry.COLUMN_DATE)),
                        DateUtility.DATABASE_DATE_FORMATTER
                ),
                TreatType.valueOf(cursor.getString(
                        cursor.getColumnIndexOrThrow(TreatContract.TreatEntry.COLUMN_TYPE)
                ))
        );
    }

    @WorkerThread
    public static CuboidTimberPack cuboidTimberPackFromCursor(final Cursor cursor)
    {
        if (DatabaseUtility.isCursorInvalid(cursor)) {
            return null;
        } else if (cursor.isBeforeFirst() && !cursor.moveToFirst()) {
            return null;
        }

        return new CuboidTimberPack(
                UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(TreatContract.CuboidTimberPackEntry._ID))),
                cursor.getInt(cursor.getColumnIndexOrThrow(TreatContract.CuboidTimberPackEntry.COLUMN_QUANTITY)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(TreatContract.CuboidTimberPackEntry.COLUMN_LENGTH)),
                cursor.getInt(cursor.getColumnIndexOrThrow(TreatContract.CuboidTimberPackEntry.COLUMN_BREADTH)),
                cursor.getInt(cursor.getColumnIndexOrThrow(TreatContract.CuboidTimberPackEntry.COLUMN_HEIGHT))
        );
    }

    @WorkerThread
    public static RoundTimberPack roundTimberPackFromCursor(final Cursor cursor)
    {
        if (DatabaseUtility.isCursorInvalid(cursor)) {
            return null;
        } else if (cursor.isBeforeFirst() && !cursor.moveToFirst()) {
            return null;
        }

        return new RoundTimberPack(
                UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(TreatContract.RoundTimberPackEntry._ID))),
                cursor.getInt(cursor.getColumnIndexOrThrow(TreatContract.RoundTimberPackEntry.COLUMN_QUANTITY)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(TreatContract.RoundTimberPackEntry.COLUMN_LENGTH)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(TreatContract.RoundTimberPackEntry.COLUMN_RADIUS))
        );
    }

    public static boolean isCursorInvalid(final Cursor cursor)
    {
        return cursor == null || cursor.isClosed() || cursor.isAfterLast();
    }
}
