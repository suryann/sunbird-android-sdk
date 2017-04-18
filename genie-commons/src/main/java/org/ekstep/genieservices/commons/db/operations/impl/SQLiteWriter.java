package org.ekstep.genieservices.commons.db.operations.impl;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.db.core.IWriteToDb;
import org.ekstep.genieservices.commons.db.core.impl.ContentValues;
import org.ekstep.genieservices.commons.db.operations.IOperate;
import org.ekstep.genieservices.commons.exception.DbException;

import java.util.Locale;

/**
 * @author anil
 */
public class SQLiteWriter implements IOperate<SQLiteDatabase> {
    private static final String LOG_TAG = "service-SQLiteWriter";
    private IWriteToDb model;

    public SQLiteWriter(IWriteToDb model) {
        this.model = model;
    }

    @Override
    public Void perform(SQLiteDatabase datasource) {
        long id = datasource.insert(model.getTableName(), null, mapContentValues(model.getContentValues()));
        Log.i(LOG_TAG, "Saving in db:" + model.getTableName());
        if (id != -1) {
            Log.i(LOG_TAG, "Saved successfully in:" + model.getTableName() + " with id:" + id);
            model.updateId(id);
        } else {
            throw new DbException(String.format(Locale.US, "Failed to write to %s", model.getTableName()));
        }
        return null;
    }

    @NonNull
    private android.content.ContentValues mapContentValues(ContentValues values) {
        android.content.ContentValues contentValues = new android.content.ContentValues();

        if (values != null) {
            for (String colName : values.keySet()) {
                Object obj = values.get(colName);

                if (obj != null) {      // Cursor.FIELD_TYPE_NULL
                    if (obj instanceof byte[]) {     // Cursor.FIELD_TYPE_BLOB
                        contentValues.put(colName, (byte[]) obj);
                    } else if (obj instanceof Float || obj instanceof Double) {     // Cursor.FIELD_TYPE_FLOAT
                        contentValues.put(colName, ((Number) obj).doubleValue());
                    } else if (obj instanceof Long || obj instanceof Integer
                            || obj instanceof Short || obj instanceof Byte) {       // Cursor.FIELD_TYPE_INTEGER
                        contentValues.put(colName, ((Number) obj).longValue());
                    } else if (obj instanceof Boolean) {
                        contentValues.put(colName, ((Boolean) obj ? 1 : 0));
                    } else {    // Cursor.FIELD_TYPE_STRING
                        contentValues.put(colName, obj.toString());
                    }
                }
            }
        }

        return contentValues;
    }

    @Override
    public void beforePerform(AppContext context) {
        model.beforeWrite(context);
    }
}