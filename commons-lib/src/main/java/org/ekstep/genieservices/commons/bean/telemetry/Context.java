package org.ekstep.genieservices.commons.bean.telemetry;

import org.ekstep.genieservices.commons.bean.CorrelationData;

import java.util.List;

/**
 * Created by swayangjit on 16/11/17.
 */

public class Context {

    private String channel;
    private ProducerData pdata;
    private String env;
    private String sid;
    private String did;
    private List<CorrelationData> cdata;

    public String getChannel() {
        return channel;
    }

    public ProducerData getPdata() {
        return pdata;
    }

    public String getEnv() {
        return env;
    }

    public String getSid() {
        return sid;
    }

    public String getDid() {
        return did;
    }

    public List<CorrelationData> getCdata() {
        return cdata;
    }

    public void setCdata(List<CorrelationData> cdata) {
        this.cdata = cdata;
    }

    public void setEnvironment(String env) {
        this.env = env;
    }
}
