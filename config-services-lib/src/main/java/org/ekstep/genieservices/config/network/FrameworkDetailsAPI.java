package org.ekstep.genieservices.config.network;

import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.IParams;
import org.ekstep.genieservices.commons.network.BaseAPI;

import java.util.Locale;
import java.util.Map;

/**
 * Created on 15/03/2018.
 *
 * @author Indraja Machani
 */
public class FrameworkDetailsAPI extends BaseAPI {

    private static final String TAG = FrameworkDetailsAPI.class.getSimpleName();

    private static final String ENDPOINT = "read";

    public FrameworkDetailsAPI(AppContext appContext, String frameworkId) {
        super(appContext,
                String.format(Locale.US, "%s/%s/%s",
                        appContext.getParams().getString(IParams.Key.FRAMEWORK_SERVICE_BASE_URL),
                        ENDPOINT, frameworkId + "?categories=board,gradeLevel,subject,medium"),
                TAG);
    }


    @Override
    protected Map<String, String> getRequestHeaders() {
        return null;
    }

    @Override
    protected String createRequestData() {
        return null;
    }
}
