package org.ekstep.genieservices.profile.network;

import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.IParams;
import org.ekstep.genieservices.commons.network.SunbirdBaseAPI;

import java.util.Locale;
import java.util.Map;

/**
 * Created on 1/3/18.
 *
 * @author anil
 */

public class TenantInfoAPI extends SunbirdBaseAPI {

    private static final String TAG = TenantInfoAPI.class.getSimpleName();

    private static final CharSequence SERVICE_ENDPOINTS = "tenant/info";

    private Map<String, String> headers;

    public TenantInfoAPI(AppContext appContext, Map<String, String> customHeaders, String slug) {
        super(appContext,
                String.format(Locale.US, "%s/%s/%s",
                        appContext.getParams().getString(IParams.Key.ORG_SERVICE_BASE_URL), SERVICE_ENDPOINTS,
                        slug),
                TAG);

        this.headers = customHeaders;
    }

    @Override
    protected Map<String, String> getRequestHeaders() {
        return this.headers;
    }

    @Override
    protected String createRequestData() {
        return null;
    }
}
