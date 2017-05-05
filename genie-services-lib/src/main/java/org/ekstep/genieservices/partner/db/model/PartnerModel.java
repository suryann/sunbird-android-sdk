package org.ekstep.genieservices.partner.db.model;


import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.db.core.ContentValues;
import org.ekstep.genieservices.commons.db.core.IReadable;
import org.ekstep.genieservices.commons.db.core.IResultSet;
import org.ekstep.genieservices.commons.db.core.IWritable;
import org.ekstep.genieservices.commons.db.operations.IDBSession;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.ekstep.genieservices.partner.db.contract.PartnerEntry;

import java.util.Locale;
import java.util.Set;

public class PartnerModel implements IWritable, IReadable {
    private static final String TAG = "model-Partner";
    private Long id = -1L;
    private String publicKeyId;
    private String partnerID;
    private String publicKey;
    private IDBSession dbSession;

    private PartnerModel(IDBSession dbSession, String partnerID, String publicKey, String publicKeyId) {
        this.partnerID = partnerID;
        this.dbSession = dbSession;
        this.publicKey = publicKey;
        this.publicKeyId = publicKeyId;
    }

    public static PartnerModel findByPartnerId(IDBSession idbSession, String partnerID) {
        PartnerModel partnerModel = new PartnerModel(idbSession, partnerID, null, null);
        idbSession.read(partnerModel);
        if (StringUtil.isNullOrEmpty(partnerModel.publicKey)) {
            return null;
        } else {
            return partnerModel;
        }
    }

    public static PartnerModel build(IDBSession idbSession, String partnerID, String publicKey, String publicKeyId) {
        return new PartnerModel(idbSession, partnerID, publicKey, publicKeyId);
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PartnerEntry.COLUMN_NAME_UID, partnerID);
        contentValues.put(PartnerEntry.COLUMN_NAME_KEY, publicKey);
        contentValues.put(PartnerEntry.COLUMN_NAME_KEY_ID, publicKeyId);
        return contentValues;
    }

    @Override
    public void updateId(long id) {

    }

    @Override
    public IReadable read(IResultSet cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getLong(0);
            partnerID = cursor.getString(1);
            publicKey = cursor.getString(2);
            publicKeyId = cursor.getString(3);
        }
        return this;
    }

    @Override
    public String getTableName() {
        return PartnerEntry.TABLE_NAME;
    }

    @Override
    public void beforeWrite(AppContext context) {

    }

    @Override
    public String orderBy() {
        return "";
    }

    @Override
    public String filterForRead() {
        String selectionCriteria = String.format(Locale.US, "where partnerID = '%s'", partnerID);
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
       dbSession.create(this);
    }


 }
