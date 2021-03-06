package org.ekstep.genieservices.content.chained.export;

import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.ContentExportResponse;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.chained.IChainable;
import org.ekstep.genieservices.commons.utils.FileUtil;
import org.ekstep.genieservices.commons.utils.Logger;
import org.ekstep.genieservices.content.bean.ExportContentContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created on 6/19/2017.
 *
 * @author anil
 */
public class DeviceMemoryCheck implements IChainable<ContentExportResponse, ExportContentContext> {

    private static final String TAG = DeviceMemoryCheck.class.getSimpleName();

    private IChainable<ContentExportResponse, ExportContentContext> nextLink;

    @Override
    public GenieResponse<ContentExportResponse> execute(AppContext appContext, ExportContentContext exportContext) {
        long deviceUsableSpace = FileUtil.getFreeUsableSpace(exportContext.getDestinationFolder());
        long fileSize = 0;
        List<Map<String, Object>> items = exportContext.getItems();

        //check for export files
        if (items != null) {
            try {
                for (Map item : items) {
                    try {
                        fileSize = fileSize + new BigDecimal((Double) item.get("size")).longValue();
                    } catch (Exception e) {
                        Logger.e(TAG, e.getMessage());
                    }
                }

                if (!FileUtil.isFreeSpaceAvailable(deviceUsableSpace, fileSize, 0)) {
                    return GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.EXPORT_FAILED, "Device memory full.", TAG);
                }
            } catch (Exception e) {
                Logger.e(TAG, e.getMessage());
            }
        }

        if (nextLink != null) {
            return nextLink.execute(appContext, exportContext);
        } else {
            return GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.EXPORT_FAILED, "Export content failed", TAG);
        }
    }

    @Override
    public IChainable<ContentExportResponse, ExportContentContext> then(IChainable<ContentExportResponse, ExportContentContext> link) {
        nextLink = link;
        return link;
    }

}
