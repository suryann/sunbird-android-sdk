package org.ekstep.genieservices.content.downloadmanager;

import org.ekstep.genieservices.IContentService;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.bean.DownloadRequest;
import org.ekstep.genieservices.commons.bean.Request;
import org.ekstep.genieservices.util.DownloadQueueManager;

import java.util.List;

/**
 * Created by swayangjit on 17/5/17.
 */

public class DownloadService {

    private IContentService mContentService;

    private AppContext mAppContext;
    private DownloadQueueManager mDownloadQueueManager = null;

    public DownloadService(AppContext appContext) {
        this.mAppContext = appContext;
        this.mDownloadQueueManager = new DownloadQueueManager(mAppContext.getKeyValueStore());
    }

    public void enQueue(DownloadRequest... downloadRequest) {

        if (downloadRequest.length > 0) {
            for (DownloadRequest request : downloadRequest) {
                 mDownloadQueueManager.save(request);
            }
        }
        startQueue();
    }

    public void startQueue() {
        List<DownloadRequest> downloadRequestList = mDownloadQueueManager.findAll();
        if (downloadRequestList.size() > 0) {
            DownloadRequest request = downloadRequestList.get(0);
            long downloadId = mAppContext.getDownloadManager().enqueue(new Request(request.getDownloadUrl(), request.getIdentifier(), request.getMimeType()));
            mDownloadQueueManager.update(request.getIdentifier(), downloadId);
            mAppContext.getDownloadManager().startDownloadProgressTracker();
        }

    }
}
