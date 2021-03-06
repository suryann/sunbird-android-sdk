package org.ekstep.genieservices.content.chained.export;

import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.ContentExportResponse;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.chained.IChainable;
import org.ekstep.genieservices.commons.utils.Logger;
import org.ekstep.genieservices.content.bean.ExportContentContext;

import java.io.File;

/**
 * Created on 6/19/2017.
 *
 * @author anil
 */
public class DeleteTemporaryEcar implements IChainable<ContentExportResponse, ExportContentContext> {

    private static final String TAG = DeleteTemporaryEcar.class.getSimpleName();

    private IChainable<ContentExportResponse, ExportContentContext> nextLink;

    @Override
    public GenieResponse<ContentExportResponse> execute(AppContext appContext, ExportContentContext exportContext) {

        try {
            File filePath = exportContext.getTmpLocation();
            if (filePath.isDirectory()) {
                Logger.i(TAG, "Folder Deleted - " + filePath.getName());
                deleteRecursive(filePath);
            }
        } catch (Exception e) {
            Logger.i(TAG, "Error in Deleting Ecar Temporary directory: " + e.getMessage());
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

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        if (!fileOrDirectory.delete()) {
            Logger.i(TAG, "Error in Deleting Ecar Temporary directory: " + fileOrDirectory.getName());
        }
    }
}
