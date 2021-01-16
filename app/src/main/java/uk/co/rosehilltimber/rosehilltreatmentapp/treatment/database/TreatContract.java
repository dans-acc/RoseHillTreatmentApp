package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.UUID;

public class TreatContract
{

    public static final String CONTENT_AUTHORITY = "uk.co.rosehilltimber.rosehilltreatmentapp.provider.treat";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TREATMENTS = "treatments";
    public static final String PATH_PACKS = PATH_TREATMENTS + "packs";
    public static final String PATH_CUBOID_TIMBER_PACKS = PATH_PACKS + "cuboid";
    public static final String PATH_ROUND_TIMBER_PACKS = PATH_PACKS + "round";


    public static final class TreatEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TREATMENTS).build();

        public static final String URI_PARAMETER_TREAT_UUID = "uuid";
        public static final String URI_PARAMETER_NEW_TRANSACTION = "new_transaction";
        public static final String URI_PARAMETER_INITIAL_TREAT_NUMBER = "init_num";

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_URI
                + "/" + PATH_TREATMENTS;

        public static final String TABLE_NAME = "Treats";

        public static final String COLUMN_NUMBER = "Number";
        public static final String COLUMN_DATE = "Date";
        public static final String COLUMN_TYPE = "Type";

        public static Uri buildTreatUri(final UUID treatUUID)
        {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(
                            URI_PARAMETER_TREAT_UUID,
                            treatUUID.toString()
                    ).build();
        }

        public static Uri buildTreatUri(final UUID treatUUID,
                                        final boolean newTransaction)
        {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(
                            URI_PARAMETER_TREAT_UUID,
                            treatUUID.toString()
                    )
                    .appendQueryParameter(
                            URI_PARAMETER_NEW_TRANSACTION,
                            Boolean.toString(newTransaction)
                    ).build();
        }

        public static Uri buildTreatUri(final UUID treatUUID,
                                        final boolean newTransaction,
                                        final int initialTreatNumber)
        {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(
                            URI_PARAMETER_TREAT_UUID,
                            treatUUID.toString()
                    )
                    .appendQueryParameter(
                            URI_PARAMETER_NEW_TRANSACTION,
                            Boolean.toString(newTransaction)
                    )
                    .appendQueryParameter(
                            URI_PARAMETER_INITIAL_TREAT_NUMBER,
                            String.valueOf(initialTreatNumber)
                    )
                    .build();
        }
    }


    public static final class CuboidTimberPackEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CUBOID_TIMBER_PACKS).build();

        public static final String URI_PARAMETER_TREAT_UUID = "treat_uuid";
        public static final String URI_PARAMETER_PACK_UUID = "cuboid_pack_uuid";
        public static final String URI_PARAMETER_NEW_TRANSACTION = "new_transaction";

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_URI
                + "/" + PATH_CUBOID_TIMBER_PACKS;

        public static final String TABLE_NAME = "Cuboid";

        public static final String COLUMN_TREAT_ID = "Treat_ID";
        public static final String COLUMN_QUANTITY = "Quantity";
        public static final String COLUMN_LENGTH = "Length";
        public static final String COLUMN_BREADTH = "Breadth";
        public static final String COLUMN_HEIGHT = "Height";

        public static Uri buildTreatCuboidTimberPackUri(final UUID treatUUID)
        {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(
                            URI_PARAMETER_TREAT_UUID,
                            treatUUID.toString()
                    ).build();
        }

        public static Uri buildCuboidTimberPackUri(final UUID packUUID)
        {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(
                            URI_PARAMETER_PACK_UUID,
                            packUUID.toString()
                    ).build();
        }

        public static Uri buildTreatCuboidTimberPackUri(final UUID treatUUID,
                                                        final boolean newTransaction)
        {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(
                            URI_PARAMETER_TREAT_UUID,
                            treatUUID.toString()
                    )
                    .appendQueryParameter(
                            URI_PARAMETER_NEW_TRANSACTION,
                            Boolean.toString(newTransaction)
                    ).build();
        }

        public static Uri buildCuboidTimberPackUri(final UUID packUUID,
                                                   final boolean newTransaction)
        {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(
                            URI_PARAMETER_PACK_UUID,
                            packUUID.toString()
                    )
                    .appendQueryParameter(
                            URI_PARAMETER_NEW_TRANSACTION,
                            Boolean.toString(newTransaction)
                    ).build();
        }
    }

    public static final class RoundTimberPackEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROUND_TIMBER_PACKS).build();

        public static final String URI_PARAMETER_TREAT_UUID = "treat_uuid";
        public static final String URI_PARAMETER_PACK_UUID = "round_pack_uuid";
        public static final String URI_PARAMETER_NEW_TRANSACTION = "new_transaction";

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_URI
                + "/" + PATH_ROUND_TIMBER_PACKS;
        public static final String CONTENT_TYPE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_URI
                + "/" + PATH_ROUND_TIMBER_PACKS;

        public static final String TABLE_NAME = "Round";

        public static final String COLUMN_TREAT_ID = "Treat_ID";
        public static final String COLUMN_QUANTITY = "Quantity";
        public static final String COLUMN_LENGTH = "Length";
        public static final String COLUMN_RADIUS = "Radius";

        public static Uri buildTreatRoundTimberPackUri(final UUID treatUUID)
        {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(
                            URI_PARAMETER_TREAT_UUID,
                            treatUUID.toString()
                    ).build();
        }

        public static Uri buildTreatRoundTimberPackUri(final UUID treatUUID,
                                                       final boolean newTransaction)
        {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(
                            URI_PARAMETER_TREAT_UUID,
                            treatUUID.toString()
                    )
                    .appendQueryParameter(
                            URI_PARAMETER_NEW_TRANSACTION,
                            Boolean.toString(newTransaction)
                    )
                    .build();
        }

        public static Uri buildRoundTimberPackUri(final UUID packUUID)
        {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(
                            URI_PARAMETER_PACK_UUID,
                            packUUID.toString()
                    ).build();
        }

        public static Uri buildRoundTimberPackUri(final UUID packUUID,
                                                  final boolean newTransaction)
        {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(
                            URI_PARAMETER_PACK_UUID,
                            packUUID.toString()
                    )
                    .appendQueryParameter(
                            URI_PARAMETER_NEW_TRANSACTION,
                            Boolean.toString(newTransaction)
                    )
                    .build();
        }
    }
}
