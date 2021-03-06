package org.ekstep.genieservices.config.db.model;

import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.db.BaseColumns;
import org.ekstep.genieservices.commons.db.contract.ResourceBundleEntry;
import org.ekstep.genieservices.commons.db.core.ContentValues;
import org.ekstep.genieservices.commons.db.core.ICleanable;
import org.ekstep.genieservices.commons.db.core.IReadable;
import org.ekstep.genieservices.commons.db.core.IResultSet;
import org.ekstep.genieservices.commons.db.core.IUpdatable;
import org.ekstep.genieservices.commons.db.core.IWritable;
import org.ekstep.genieservices.commons.db.operations.IDBSession;

import java.util.Locale;

public class ResourceBundleModel implements IWritable, IReadable, IUpdatable, ICleanable {
    private static final String TAG = "model-ResourceBundle";
    private Long id = -1L;
    private String mIdentifier;
    private String mJson;
    private IDBSession mDBSession;

    private ResourceBundleModel(IDBSession dbSession, String identifier) {
        this.mDBSession = dbSession;
        mIdentifier = identifier;
    }

    private ResourceBundleModel(IDBSession dbSession, String identifier, String json) {
        this.mDBSession = dbSession;
        mJson = json;
        mIdentifier = identifier;
    }

    public static ResourceBundleModel build(IDBSession dbSession, String identifier, String json) {
        return new ResourceBundleModel(dbSession, identifier, json);
    }

    public static ResourceBundleModel findById(IDBSession dbSession, String identifier) {
        ResourceBundleModel resourceBundle = new ResourceBundleModel(dbSession, identifier);
        dbSession.read(resourceBundle);
        if (resourceBundle.getResourceString() == null) {
            return null;
        } else {
            return resourceBundle;
        }
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ResourceBundleEntry.COLUMN_NAME_BUNDLE_IDENTIFIER, mIdentifier);
        contentValues.put(ResourceBundleEntry.COLUMN_NAME_BUNDLE_JSON, mJson.getBytes());
        return contentValues;
    }

    @Override
    public void updateId(long id) {
        this.id = id;
    }

    @Override
    public ContentValues getFieldsToUpdate() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ResourceBundleEntry.COLUMN_NAME_BUNDLE_JSON, mJson);
        return contentValues;
    }

    @Override
    public IReadable read(IResultSet resultSet) {
        if (resultSet != null && resultSet.moveToFirst())
            readByMovingToFirst(resultSet);
        return this;
    }

    /**
     * Moves the resultSet to the top and reads
     *
     * @param resultSet
     */
    private void readByMovingToFirst(IResultSet resultSet) {
        if (resultSet != null && resultSet.moveToFirst()) {
            id = resultSet.getLong(resultSet.getColumnIndex(BaseColumns._ID));
            mIdentifier = resultSet.getString(resultSet.getColumnIndex(ResourceBundleEntry.COLUMN_NAME_BUNDLE_IDENTIFIER));
            byte[] bytes = resultSet.getBlob(resultSet.getColumnIndex(ResourceBundleEntry.COLUMN_NAME_BUNDLE_JSON));
            mJson = new String(bytes);
        } else {
            mIdentifier = "";
        }
    }

    @Override
    public String getTableName() {
        return ResourceBundleEntry.TABLE_NAME;
    }

    @Override
    public void beforeWrite(AppContext context) {

    }

    @Override
    public void clean() {
        id = -1L;
        mIdentifier = null;
        mJson = null;
    }

    @Override
    public String selectionToClean() {
        return String.format(Locale.US, "WHERE %s = '%s';", ResourceBundleEntry.COLUMN_NAME_BUNDLE_IDENTIFIER, mIdentifier);
    }

    @Override
    public String updateBy() {
        return String.format(Locale.US, "%s = '%s'", ResourceBundleEntry.COLUMN_NAME_BUNDLE_IDENTIFIER, mIdentifier);
    }

    @Override
    public String orderBy() {
        return "";
    }

    @Override
    public String filterForRead() {
        String selectionCriteria = String.format(Locale.US, "where %s = '%s'", ResourceBundleEntry.COLUMN_NAME_BUNDLE_IDENTIFIER, mIdentifier);
        return selectionCriteria;
    }

    @Override
    public String[] selectionArgsForFilter() {
        return null;
    }

    @Override
    public String limitBy() {
        return "limit 1";
    }

    public void save() {
        mDBSession.create(this);
    }

    public void update() {
        mDBSession.update(this);
    }

    public String getResourceString() {
        return mJson;
    }
    public String getIdentifier() {
        return mIdentifier;
    }

}
