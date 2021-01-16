package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

public class TreatDatabaseHelper extends SQLiteOpenHelper
{

    // Basic database information.
    private static final String DATABASE_NAME = "Treatments";
    private static final int DATABASE_VERSION = 1;

    private static volatile TreatDatabaseHelper sInstance;

    // The context for which the database exists.
    private Context mApplicationContext;

    @SuppressWarnings("WeakerAccess")
    protected TreatDatabaseHelper(@NonNull final Context mApplicationContext)
    {
        super(mApplicationContext, DATABASE_NAME, null, DATABASE_VERSION);

        this.mApplicationContext = mApplicationContext.getApplicationContext();
    }

    public static TreatDatabaseHelper getInstance(final Context context)
    {
        if (sInstance == null) {
            synchronized (TreatDatabaseHelper.class) {
                if (sInstance != null) {
                    return sInstance;
                }
                sInstance = new TreatDatabaseHelper(context.getApplicationContext());
            }
        }
        return sInstance;
    }

    @NonNull
    public Context getApplicationContext()
    {
        return mApplicationContext;
    }

    @Override
    public void onCreate(final SQLiteDatabase sqLiteDatabase)
    {
        // Create and define the treatment table schema i.e. columns and types.
        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TreatContract.TreatEntry.TABLE_NAME
                        + " ("

                        // Treat table columns.
                        + TreatContract.TreatEntry._ID + " TEXT NOT NULL,"
                        + TreatContract.TreatEntry.COLUMN_NUMBER + " INTEGER NOT NULL,"
                        + TreatContract.TreatEntry.COLUMN_DATE + " TEXT NOT NULL,"
                        + TreatContract.TreatEntry.COLUMN_TYPE + " TEXT NOT NULL,"

                        // Treat table primary key
                        + "PRIMARY KEY(" + TreatContract.TreatEntry._ID + ")"
                        + " );"
        );

        // The common pack table foreign key - used for referencing the treatment table.
        final String commonPackTableForeignKey =
                "FOREIGN KEY(%s) " +
                "REFERENCES " + TreatContract.TreatEntry.TABLE_NAME + "(" + TreatContract.TreatEntry._ID + ") " +
                "ON UPDATE CASCADE ON DELETE CASCADE";

        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TreatContract.CuboidTimberPackEntry.TABLE_NAME
                        + " ("

                        // Table columns.
                        + TreatContract.CuboidTimberPackEntry._ID + " TEXT NOT NULL,"
                        + TreatContract.CuboidTimberPackEntry.COLUMN_TREAT_ID + " TEXT NOT NULL,"
                        + TreatContract.CuboidTimberPackEntry.COLUMN_QUANTITY + " INTEGER NOT NULL,"
                        + TreatContract.CuboidTimberPackEntry.COLUMN_LENGTH + " REAL NOT NULL,"
                        + TreatContract.CuboidTimberPackEntry.COLUMN_BREADTH + " INTEGER NOT NULL,"
                        + TreatContract.CuboidTimberPackEntry.COLUMN_HEIGHT + " INTEGER NOT NULL,"

                        // Keys within the table.
                        + "PRIMARY KEY(" + TreatContract.CuboidTimberPackEntry._ID + "), "
                        + String.format(commonPackTableForeignKey, TreatContract.CuboidTimberPackEntry.COLUMN_TREAT_ID)
                        + " );"
        );

        // Creates and defines the table schema used by the round timber pack table.
        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TreatContract.RoundTimberPackEntry.TABLE_NAME
                        + " ("

                        // Defines the round timber pack table columns.
                        + TreatContract.RoundTimberPackEntry._ID + " TEXT NOT NULL,"
                        + TreatContract.RoundTimberPackEntry.COLUMN_TREAT_ID + " TEXT NOT NULL,"
                        + TreatContract.RoundTimberPackEntry.COLUMN_QUANTITY + " INTEGER NOT NULL,"
                        + TreatContract.RoundTimberPackEntry.COLUMN_LENGTH + " REAL NOT NULL,"
                        + TreatContract.RoundTimberPackEntry.COLUMN_RADIUS + " REAL NOT NULL,"

                        // Defines the table keys (primary and secondary).
                        + "PRIMARY KEY(" + TreatContract.RoundTimberPackEntry._ID + "), "
                        + String.format(commonPackTableForeignKey, TreatContract.RoundTimberPackEntry.COLUMN_TREAT_ID)
                        + " );"
        );
    }

    @Override
    public void onUpgrade(final SQLiteDatabase sqLiteDatabase, final int oldVersion, final int newVersion)
    {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TreatContract.TreatEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TreatContract.CuboidTimberPackEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TreatContract.RoundTimberPackEntry.TABLE_NAME);

        onCreate(sqLiteDatabase);
    }

    @Override
    public void onOpen(final SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    public void truncateTables()
            throws SQLiteException
    {
        truncateTables(super.getWritableDatabase());
    }

    public void truncateTables(final SQLiteDatabase sqLiteDatabase)
            throws SQLiteException
    {
        sqLiteDatabase.delete(TreatContract.CuboidTimberPackEntry.TABLE_NAME, null, null);
        sqLiteDatabase.delete(TreatContract.RoundTimberPackEntry.TABLE_NAME, null, null);
        sqLiteDatabase.delete(TreatContract.TreatEntry.TABLE_NAME, null, null);
    }
}
