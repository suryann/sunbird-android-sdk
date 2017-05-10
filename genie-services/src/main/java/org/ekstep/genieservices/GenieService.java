package org.ekstep.genieservices;

import android.content.Context;

import org.ekstep.genieservices.commons.AndroidAppContext;
import org.ekstep.genieservices.commons.AndroidLogger;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.utils.Logger;
import org.ekstep.genieservices.config.ConfigService;
import org.ekstep.genieservices.profile.UserProfileService;
import org.ekstep.genieservices.telemetry.SyncServiceImpl;
import org.ekstep.genieservices.telemetry.TelemetryServiceImpl;

/**
 * Created on 4/14/2017.
 *
 * @author anil
 */
public class GenieService {

    private static GenieService service;

    private AppContext<Context>  applicationContext;
    private ConfigService sConfigService;
    private TelemetryServiceImpl sTelemetryService;
    private UserProfileService sProfileService;
    private SyncServiceImpl syncService;

    public static GenieService getService() {
        return service;
    }

    public static GenieService init(Context context, String packageName, String apiKey, String gDataId) {

        if (service == null) {
            AppContext<Context> applicationContext = AndroidAppContext.buildAppContext(context, packageName, apiKey, gDataId);
            Logger.init(new AndroidLogger());
            service = new GenieService(applicationContext);
        }
        return service;
    }

    private GenieService(AppContext<Context> applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ConfigService getConfigService() {
        if (sConfigService == null) {
            sConfigService = new ConfigService(applicationContext);
        }
        return sConfigService;
    }

    public UserProfileService getUserProfileService() {
        if (sProfileService == null) {
            sProfileService = new UserProfileService(applicationContext);
        }
        return sProfileService;
    }

    public TelemetryServiceImpl getTelemetryService() {
        if (sTelemetryService == null) {
            sTelemetryService = new TelemetryServiceImpl(applicationContext);
        }
        return sTelemetryService;
    }

    public SyncServiceImpl getSyncService() {
        if (syncService == null) {
            syncService = new SyncServiceImpl(applicationContext);
        }
        return syncService;
    }
}
