package org.ekstep.genieservices.profile.chained.export;

import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.ProfileExportResponse;
import org.ekstep.genieservices.commons.chained.IChainable;
import org.ekstep.genieservices.commons.db.contract.MetaEntry;
import org.ekstep.genieservices.commons.db.operations.IDBSession;
import org.ekstep.genieservices.commons.utils.CollectionUtil;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.importexport.bean.ExportProfileContext;
import org.ekstep.genieservices.importexport.db.model.MetadataModel;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created on 6/10/2017.
 *
 * @author anil
 */
public class CreateMetadata implements IChainable<ProfileExportResponse, ExportProfileContext> {

    private static final String TAG = CreateMetadata.class.getSimpleName();
    private IChainable<ProfileExportResponse, ExportProfileContext> nextLink;

    @Override
    public GenieResponse<ProfileExportResponse> execute(AppContext appContext, ExportProfileContext exportContext) {
        IDBSession destinationDBSession = appContext.getExternalDBSession(exportContext.getDestinationDBFilePath());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(ServiceConstants.VERSION, String.valueOf(appContext.getDBSession().getDBVersion()));
        metadata.put(ServiceConstants.EXPORT_TYPES, GsonUtil.toJson(Collections.singletonList(ServiceConstants.EXPORT_TYPE_PROFILE)));
        metadata.put(ServiceConstants.DID, appContext.getDeviceInfo().getDeviceID());
        metadata.put(ServiceConstants.EXPORT_ID, UUID.randomUUID().toString());
        metadata.put(ServiceConstants.FILE_SIZE, new File(exportContext.getDestinationDBFilePath()).length());
        int groupCount = 0;
        if (!CollectionUtil.isNullOrEmpty(exportContext.getGroupIds())) {
            groupCount = exportContext.getGroupIds().size();
        }
        metadata.put(ServiceConstants.PROFILES_COUNT, String.valueOf(exportContext.getUserIds().size() + groupCount));

        destinationDBSession.execute(MetaEntry.getCreateEntry());
        for (String key : metadata.keySet()) {
            MetadataModel metadataModel = MetadataModel.build(destinationDBSession, key, String.valueOf(metadata.get(key)));
            metadataModel.save();
        }

        exportContext.setMetadata(metadata);

        if (nextLink != null) {
            return nextLink.execute(appContext, exportContext);
        } else {
            return GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.EXPORT_FAILED, "Export profile failed", TAG);
        }
    }

    @Override
    public IChainable<ProfileExportResponse, ExportProfileContext> then(IChainable<ProfileExportResponse, ExportProfileContext> link) {
        nextLink = link;
        return link;
    }
}
