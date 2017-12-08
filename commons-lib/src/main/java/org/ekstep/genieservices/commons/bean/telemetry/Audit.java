package org.ekstep.genieservices.commons.bean.telemetry;

import org.ekstep.genieservices.commons.utils.GsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 15/11/17.
 *
 * @author swayangjit
 */
public class Audit extends Telemetry {

    private static final String EID = "AUDIT";

    private Audit(List<String> props, String currentState, String prevState, String actorType) {
        super(EID);
        setEData(createEData(props, currentState, prevState));
        setActor(new Actor(actorType));
    }

    protected Map<String, Object> createEData(List<String> props, String currentState, String prevState) {
        Map<String, Object> eData = new HashMap<>();
        eData.put("props", props);
        eData.put("state", currentState);
        eData.put("prevstate", prevState);
        return eData;
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }

    public static class Builder {
        private List<String> props;
        private String currentState;
        private String prevState;
        private String actorType;

        public Builder updatedProperties(List<String> properties) {
            this.props = properties;
            return this;
        }

        public Builder currentState(String currentState) {
            this.currentState = currentState;
            return this;
        }

        public Builder previousState(String prevState) {
            this.prevState = prevState;
            return this;
        }

        public Builder actorType(String actorType) {
            this.actorType = actorType;
            return this;
        }

        public Audit build() {
            if (props == null) {
                props = new ArrayList<>();
            }

            if (currentState == null) {
                currentState = "";
            }

            if (prevState == null) {
                prevState = "";
            }

            return new Audit(props, currentState, prevState, actorType);
        }
    }
}
