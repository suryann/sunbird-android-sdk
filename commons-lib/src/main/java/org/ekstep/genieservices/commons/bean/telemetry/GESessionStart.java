package org.ekstep.genieservices.commons.bean.telemetry;

import org.ekstep.genieservices.commons.bean.GameData;
import org.ekstep.genieservices.commons.bean.UserSession;
import org.ekstep.genieservices.commons.utils.DateUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by swayangjit on 2/5/17.
 */

public class GESessionStart extends BaseTelemetry {

    private final String eid = "GE_SESSION_START";
    private final Map<String, Object> edata;

    public GESessionStart(GameData gameData, UserSession session, String loc, String deviceId) {
        super(gameData);
        edata = new HashMap<>();
        setEks(createEKS(loc));
        setEts(DateUtil.getEpochTime());
        setSid(session.getSid());
        setUid(session.getUid());
        setDid(deviceId);
        setTs(DateUtil.getCurrentTimestamp());
    }

    protected HashMap<String, Object> createEKS(String loc) {
        HashMap<String, Object> eks = new HashMap<>();
        eks.put("loc", loc);
        return eks;
    }

    @Override
    public String getEID() {
        return eid;
    }

}

