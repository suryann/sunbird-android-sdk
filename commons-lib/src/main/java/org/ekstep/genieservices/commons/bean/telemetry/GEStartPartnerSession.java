package org.ekstep.genieservices.commons.bean.telemetry;

import org.ekstep.genieservices.commons.bean.GameData;
import org.ekstep.genieservices.commons.utils.DateUtil;

import java.util.HashMap;

/**
 * Created by swayangjit on 2/5/17.
 */

public class GEStartPartnerSession extends BaseTelemetry {

    private final String eid = "GE_START_PARTNER_SESSION";

    public GEStartPartnerSession(GameData gameData, String partnerID, String deviceId, String partnerSID) {
        super(gameData);
        setEks(createEKS(partnerID, partnerSID));
        setDid(deviceId);
        setTs(DateUtil.getCurrentTimestamp());
    }

    protected HashMap<String, Object> createEKS(String partnerID, String partnerSID) {
        HashMap<String, Object> eks = new HashMap<>();
        eks.put("partnerid", partnerID);
        eks.put("sid", partnerSID);
        return eks;
    }

    @Override
    public String getEID() {
        return eid;
    }

}

