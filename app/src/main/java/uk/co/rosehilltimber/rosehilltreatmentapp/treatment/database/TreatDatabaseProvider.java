package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.database;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

public class TreatDatabaseProvider extends ContentProvider
{

    private static final int TREATMENT = 100;
    private static final int CUBOID_TIMBER_PACK = 101;
    private static final int ROUND_TIMBER_PACK = 102;


    // Create the URI matcher for the database and add the supported URI's.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        final String content = TreatContract.CONTENT_AUTHORITY;

        // Create the different types of URIs supported by the content provider.
        sUriMatcher.addURI(content, TreatContract.PATH_TREATMENTS, TREATMENT);
        sUriMatcher.addURI(content, TreatContract.PATH_CUBOID_TIMBER_PACKS, CUBOID_TIMBER_PACK);
        sUriMatcher.addURI(content, TreatContract.PATH_ROUND_TIMBER_PACKS, ROUND_TIMBER_PACK);
    }

    // The underlying database.
    private TreatDatabaseHelper mTreatDatabaseHelper;

    @Override
    public boolean onCreate()
    {
        final Context context = super.getContext();
        if (context == null) {
            return false;
        }
        mTreatDatabaseHelper = TreatDatabaseHelper.getInstance(context);
        return true;
    }

    @Override
    public String getType(@NonNull final Uri uri)
    {
        switch (sUriMatcher.match(uri)) {
            case TREATMENT:
                return TreatContract.TreatEntry.CONTENT_TYPE;
            case CUBOID_TIMBER_PACK:
                return TreatContract.CuboidTimberPackEntry.CONTENT_TYPE;
            case ROUND_TIMBER_PACK:
                return TreatContract.RoundTimberPackEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown content URI: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull final Uri uri,
                        final String[] projection,
                        String selection,
                        String[] selectedArgs,
                        final String sortOrder)
    {

        final String tableName;
        final String batchParameterName;
        final int matchedUri = sUriMatcher.match(uri);

        switch (matchedUri) {
            case TREATMENT:
                tableName = TreatContract.TreatEntry.TABLE_NAME;

                final String treatUUID = uri.getQueryParameter(TreatContract.TreatEntry.URI_PARAMETER_TREAT_UUID);
                if (treatUUID != null && !treatUUID.isEmpty()) {
                    selection = TreatContract.TreatEntry._ID + " = ?";
                    selectedArgs = new String[] {treatUUID};
                }

                break;
            case CUBOID_TIMBER_PACK:
                tableName = TreatContract.CuboidTimberPackEntry.TABLE_NAME;

                final String cuboidPackTreatUUID = uri.getQueryParameter(TreatContract.CuboidTimberPackEntry.URI_PARAMETER_TREAT_UUID);
                if (cuboidPackTreatUUID != null && !cuboidPackTreatUUID.isEmpty()) {
                    selection = TreatContract.CuboidTimberPackEntry.COLUMN_TREAT_ID + " = ?";
                    selectedArgs = new String[] {cuboidPackTreatUUID};
                    break;
                }

                final String cuboidPackUUID = uri.getQueryParameter(TreatContract.CuboidTimberPackEntry.URI_PARAMETER_PACK_UUID);
                if (cuboidPackUUID != null && !cuboidPackUUID.isEmpty()) {
                    selection = TreatContract.CuboidTimberPackEntry._ID + " = ?";
                    selectedArgs = new String[] {cuboidPackUUID};
                }

                break;
            case ROUND_TIMBER_PACK:
                tableName = TreatContract.RoundTimberPackEntry.TABLE_NAME;

                final String roundPackTreatUUID = uri.getQueryParameter(TreatContract.RoundTimberPackEntry.URI_PARAMETER_TREAT_UUID);
                if (roundPackTreatUUID != null && !roundPackTreatUUID.isEmpty()) {
                    selection = TreatContract.RoundTimberPackEntry.COLUMN_TREAT_ID + " = ?";
                    selectedArgs = new String[] {roundPackTreatUUID};
                    break;
                }

                final String roundPackUUID = uri.getQueryParameter(TreatContract.RoundTimberPackEntry.URI_PARAMETER_PACK_UUID);
                if (roundPackUUID != null && !roundPackUUID.isEmpty()) {
                    selection = TreatContract.RoundTimberPackEntry._ID + " = ?";
                    selectedArgs = new String[] {roundPackUUID};
                }

                break;
            default:
                throw new UnsupportedOperationException("Unknown content URI: " + uri);
        }

        final SQLiteDatabase sqLiteDatabase = mTreatDatabaseHelper.getReadableDatabase();
        return sqLiteDatabase.query(
                tableName,
                projection,
                selection,
                selectedArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public Uri insert(@NonNull final Uri uri,
                      @Nullable final ContentValues contentValues)
    {

        final String tableName;
        final String newTransactionParameterName;
        final String uuid;
        final int matchedUri = sUriMatcher.match(uri);

        switch (matchedUri) {
            case TREATMENT:
                tableName = TreatContract.TreatEntry.TABLE_NAME;
                newTransactionParameterName = TreatContract.TreatEntry.URI_PARAMETER_NEW_TRANSACTION;
                uuid = uri.getQueryParameter(TreatContract.TreatEntry.URI_PARAMETER_TREAT_UUID);
                break;
            case CUBOID_TIMBER_PACK:
                tableName = TreatContract.CuboidTimberPackEntry.TABLE_NAME;
                newTransactionParameterName = TreatContract.CuboidTimberPackEntry.URI_PARAMETER_NEW_TRANSACTION;
                uuid = uri.getQueryParameter(TreatContract.CuboidTimberPackEntry.URI_PARAMETER_PACK_UUID);
                break;
            case ROUND_TIMBER_PACK:
                tableName = TreatContract.RoundTimberPackEntry.TABLE_NAME;
                newTransactionParameterName = TreatContract.RoundTimberPackEntry.URI_PARAMETER_NEW_TRANSACTION;
                uuid = uri.getQueryParameter(TreatContract.RoundTimberPackEntry.URI_PARAMETER_PACK_UUID);
                break;
            default:
                throw new UnsupportedOperationException("Unknown content URI: " + uri);
        }


        final String newTransactionParameter = uri.getQueryParameter(newTransactionParameterName);
        final boolean newTransaction = newTransactionParameter != null && !newTransactionParameter.isEmpty()
                && Boolean.parseBoolean(newTransactionParameter);

        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        int treatNumber = -1;
        try {
            sqLiteDatabase = mTreatDatabaseHelper.getWritableDatabase();
            if (newTransaction) {
                sqLiteDatabase.beginTransaction();
            }

            if (matchedUri == TREATMENT) {

                final String initialTreatNumberParameter = uri.getQueryParameter(TreatContract.TreatEntry.URI_PARAMETER_INITIAL_TREAT_NUMBER);
                if (initialTreatNumberParameter == null || initialTreatNumberParameter.isEmpty()) {
                    throw new IllegalArgumentException("Could not insert a new treatment for uri: "
                            + uri.toString() + ". Initial treat number must be set!");
                }

                if (contentValues == null) {
                    throw new IllegalArgumentException("Unable to insert treatment for uri: "
                            + uri + ". Please provide the necessary content values.");
                }

                final int initialTreatNumber = Integer.parseInt(initialTreatNumberParameter);

                final String treatCountQuery = "SELECT COUNT(*) FROM " + TreatContract.TreatEntry.TABLE_NAME;
                final SQLiteStatement treatCountStatement = sqLiteDatabase.compileStatement(treatCountQuery);
                long treatCount = treatCountStatement.simpleQueryForLong();
                treatCountStatement.close();

                if (treatCount == 0) {
                    treatNumber = initialTreatNumber;
                } else {

                    cursor = sqLiteDatabase.rawQuery("SELECT MAX(" + TreatContract.TreatEntry.COLUMN_NUMBER + ") AS MAX FROM "
                            + TreatContract.TreatEntry.TABLE_NAME + " WHERE " + TreatContract.TreatEntry.COLUMN_DATE + " <= ?",
                            new String[]{contentValues.getAsString(TreatContract.TreatEntry.COLUMN_DATE)});

                    Log.wtf("date", contentValues.getAsString(TreatContract.TreatEntry.COLUMN_DATE));

                    if (cursor.getCount() == 0) {
                        treatNumber = initialTreatNumber;
                    } else if (cursor.moveToFirst()) {
                        treatNumber = cursor.getInt(cursor.getColumnIndex("MAX")) + 1;
                        Log.wtf("MAX:", treatNumber + " <<<");
                        if (treatNumber < initialTreatNumber) {
                            treatNumber = initialTreatNumber;
                        }
                    } else {
                        cursor.close();
                        throw new IllegalStateException("Unable to insert treatment for uri: " + uri);
                    }
                }

                sqLiteDatabase.execSQL("UPDATE " + TreatContract.TreatEntry.TABLE_NAME
                                + " SET " + TreatContract.TreatEntry.COLUMN_NUMBER +" = " + TreatContract.TreatEntry.COLUMN_NUMBER + " + 1 "
                                + " WHERE " + TreatContract.TreatEntry.COLUMN_NUMBER + " >= " + treatNumber
                );

                contentValues.put(TreatContract.TreatEntry.COLUMN_NUMBER, treatNumber);
            }

            sqLiteDatabase.insertOrThrow(
                    tableName,
                    null,
                    contentValues
            );

            if (newTransaction) {
                sqLiteDatabase.setTransactionSuccessful();
            }
        } finally {
            if (sqLiteDatabase != null && newTransaction) {
                sqLiteDatabase.endTransaction();
            }
        }

        final Uri retUri;
        switch (matchedUri) {
            case TREATMENT:
                retUri = TreatContract.TreatEntry.buildTreatUri(UUID.fromString(uuid));
                break;
            case CUBOID_TIMBER_PACK:
                retUri = TreatContract.CuboidTimberPackEntry.buildCuboidTimberPackUri(UUID.fromString(uuid));
                break;
            case ROUND_TIMBER_PACK:
                retUri = TreatContract.RoundTimberPackEntry.buildRoundTimberPackUri(UUID.fromString(uuid));
                break;
            default:
                throw new RuntimeException("Failed to create uri.");
        }

        return retUri;
    }

    @Override
    public int delete(@NonNull final Uri uri, String selection, String[] selectionArgs)
    {

        final String tableName;
        final String newTransactionParameterName;
        final int matchedUri = sUriMatcher.match(uri);

        switch (matchedUri) {

            case TREATMENT:
                tableName = TreatContract.TreatEntry.TABLE_NAME;
                newTransactionParameterName = TreatContract.TreatEntry.URI_PARAMETER_NEW_TRANSACTION;

                final String treatUUID = uri.getQueryParameter(TreatContract.TreatEntry.URI_PARAMETER_TREAT_UUID);
                if (treatUUID != null && !treatUUID.isEmpty()) {
                    selection = TreatContract.TreatEntry._ID + " = ?";
                    selectionArgs = new String[] {treatUUID};
                } else {
                    throw new UnsupportedOperationException("URI: " + uri
                            + " has not defined the parameter: "
                            + TreatContract.TreatEntry.URI_PARAMETER_TREAT_UUID);
                }
                break;

            case CUBOID_TIMBER_PACK:
                tableName = TreatContract.CuboidTimberPackEntry.TABLE_NAME;
                newTransactionParameterName = TreatContract.CuboidTimberPackEntry.URI_PARAMETER_NEW_TRANSACTION;

                final String cuboidPackTreatUUID = uri.getQueryParameter(TreatContract.CuboidTimberPackEntry.URI_PARAMETER_TREAT_UUID);
                if (cuboidPackTreatUUID != null && !cuboidPackTreatUUID.isEmpty()) {
                    selection = TreatContract.CuboidTimberPackEntry.COLUMN_TREAT_ID;
                    selectionArgs = new String[] {cuboidPackTreatUUID};
                    break;
                }

                final String cuboidPackUUID = uri.getQueryParameter(TreatContract.CuboidTimberPackEntry.URI_PARAMETER_PACK_UUID);
                if (cuboidPackUUID != null && !cuboidPackUUID.isEmpty()) {
                    selection = TreatContract.CuboidTimberPackEntry._ID + " = ?";
                    selectionArgs = new String[] {cuboidPackUUID};
                }

                break;

            case ROUND_TIMBER_PACK:
                tableName = TreatContract.RoundTimberPackEntry.TABLE_NAME;
                newTransactionParameterName = TreatContract.RoundTimberPackEntry.URI_PARAMETER_NEW_TRANSACTION;

                final String roundPackTreatUUID = uri.getQueryParameter(TreatContract.RoundTimberPackEntry.URI_PARAMETER_TREAT_UUID);
                if (roundPackTreatUUID != null && !roundPackTreatUUID.isEmpty()) {
                    selection = TreatContract.RoundTimberPackEntry.COLUMN_TREAT_ID;
                    selectionArgs = new String[] {roundPackTreatUUID};
                    break;
                }

                final String roundPackUUID = uri.getQueryParameter(TreatContract.RoundTimberPackEntry.URI_PARAMETER_PACK_UUID);
                if (roundPackUUID != null && !roundPackUUID.isEmpty()) {
                    selection = TreatContract.RoundTimberPackEntry._ID + " = ?";
                    selectionArgs = new String[] {roundPackUUID};
                }

                break;

            default:
                throw new UnsupportedOperationException("Unknown content URI: " + uri);
        }

        final String newTransactionParameter = uri.getQueryParameter(newTransactionParameterName);
        final boolean newTransaction = newTransactionParameter != null && !newTransactionParameter.isEmpty()
                && Boolean.parseBoolean(newTransactionParameter);

        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        long treatNumber = -1;
        int rowsAffected = 0;

        try {

            sqLiteDatabase = mTreatDatabaseHelper.getWritableDatabase();
            if (newTransaction) {
                sqLiteDatabase.beginTransaction();
            }

            if (matchedUri == TREATMENT) {
                final String treatNumberQuery
                        = "SELECT " + TreatContract.TreatEntry.COLUMN_NUMBER + " FROM " + TreatContract.TreatEntry.TABLE_NAME
                        + " WHERE " + TreatContract.TreatEntry._ID + " = \'" + selectionArgs[0] + "\'";
                final SQLiteStatement treatNumberStatement = sqLiteDatabase.compileStatement(treatNumberQuery);
                treatNumber = treatNumberStatement.simpleQueryForLong();
                treatNumberStatement.close();
            }

            rowsAffected = sqLiteDatabase.delete(
                    tableName,
                    selection,
                    selectionArgs
            );

            if (matchedUri == TREATMENT) {
                sqLiteDatabase.execSQL(
                        "UPDATE " + TreatContract.TreatEntry.TABLE_NAME
                        + " SET " + TreatContract.TreatEntry.COLUMN_NUMBER +" = " + TreatContract.TreatEntry.COLUMN_NUMBER + " - " + rowsAffected
                        + " WHERE " + TreatContract.TreatEntry.COLUMN_NUMBER + " > " + treatNumber
                );
            }

            if (newTransaction) {
                sqLiteDatabase.setTransactionSuccessful();
            }
        } finally {
            if (sqLiteDatabase != null && newTransaction) {
                sqLiteDatabase.endTransaction();
            }
        }

        return rowsAffected;
    }

    @Override
    public int update(@NonNull final Uri uri, final ContentValues contentValues,
                      String selection, String[] selectionArgs)
    {

        final String tableName;
        final String newTransactionParameterName;
        final int matchedUri = sUriMatcher.match(uri);

        switch (matchedUri) {

            case TREATMENT:

                tableName = TreatContract.TreatEntry.TABLE_NAME;
                final String treatUUID = uri.getQueryParameter(TreatContract.TreatEntry.URI_PARAMETER_TREAT_UUID);
                if (treatUUID != null && !treatUUID.isEmpty()) {
                    selection = TreatContract.TreatEntry._ID + " = ?";
                    selectionArgs = new String[] {treatUUID};
                }

                break;
            case CUBOID_TIMBER_PACK:

                tableName = TreatContract.CuboidTimberPackEntry.TABLE_NAME;
                final String cuboidPackTreatUUID = uri.getQueryParameter(TreatContract.CuboidTimberPackEntry.URI_PARAMETER_TREAT_UUID);
                if (cuboidPackTreatUUID != null && !cuboidPackTreatUUID.isEmpty()) {
                    selection = TreatContract.CuboidTimberPackEntry.COLUMN_TREAT_ID;
                    selectionArgs = new String[] {cuboidPackTreatUUID};
                    break;
                }

                break;
            case ROUND_TIMBER_PACK:

                tableName = TreatContract.RoundTimberPackEntry.TABLE_NAME;
                final String roundPackTreatUUID = uri.getQueryParameter(TreatContract.RoundTimberPackEntry.URI_PARAMETER_TREAT_UUID);
                if (roundPackTreatUUID != null && !roundPackTreatUUID.isEmpty()) {
                    selection = TreatContract.RoundTimberPackEntry.COLUMN_TREAT_ID;
                    selectionArgs = new String[] {roundPackTreatUUID};
                    break;
                }

                break;
            default:
                throw new UnsupportedOperationException("Unknown content URI: " + uri);
        }

        Log.wtf("Table name::", tableName);

        SQLiteDatabase sqLiteDatabase = null;
        int rowsAffected = 0;
        sqLiteDatabase = mTreatDatabaseHelper.getWritableDatabase();
        rowsAffected = sqLiteDatabase.update(
                tableName,
                contentValues,
                selection,
                selectionArgs
        );

        if (super.getContext() != null && rowsAffected > 0) {
            super.getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsAffected;
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull final ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException
    {
        ContentProviderResult[] contentProviderResults = null;
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = mTreatDatabaseHelper.getWritableDatabase();
            sqLiteDatabase.beginTransaction();
            contentProviderResults = super.applyBatch(operations);
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            if (sqLiteDatabase != null) {
                sqLiteDatabase.endTransaction();
            }
        }
        return contentProviderResults;
    }

}
