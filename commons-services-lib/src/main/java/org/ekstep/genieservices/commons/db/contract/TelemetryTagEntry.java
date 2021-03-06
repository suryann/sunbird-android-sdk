package org.ekstep.genieservices.commons.db.contract;

import org.ekstep.genieservices.commons.db.BaseColumns;
import org.ekstep.genieservices.commons.db.DbConstants;

public abstract class TelemetryTagEntry implements BaseColumns {

    public static final String TABLE_NAME = "telemetry_tags";
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_HASH = "hash";
    public static final String COLUMN_NAME_DESCRIPTION = "description";
    public static final String COLUMN_NAME_START_DATE = "start_date";
    public static final String COLUMN_NAME_END_DATE = "end_date";

    public static String getCreateEntry() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME_NAME + DbConstants.TEXT_TYPE + DbConstants.COMMA_SEP +
                COLUMN_NAME_HASH + DbConstants.TEXT_TYPE + DbConstants.COMMA_SEP +
                COLUMN_NAME_DESCRIPTION + DbConstants.TEXT_TYPE + DbConstants.COMMA_SEP +
                COLUMN_NAME_START_DATE + DbConstants.DATE_TYPE + DbConstants.COMMA_SEP +
                COLUMN_NAME_END_DATE + DbConstants.DATE_TYPE +
                " )";
    }

    public static String getDeleteEntry() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
