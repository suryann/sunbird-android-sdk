package org.ekstep.genieservices.importexport.bean;

import java.util.List;
import java.util.Map;

/**
 * Created on 5/16/2017.
 *
 * @author anil
 */
public class ExportProfileContext {

    private List<String> userIds;
    private String destinationFolder;
    private String destinationDBFilePath;
    private Map<String, Object> metadata;

    public ExportProfileContext(List<String> userIds, String destinationFolder, String destinationDBFilePath) {
        this.userIds = userIds;
        this.destinationFolder = destinationFolder;
        this.destinationDBFilePath = destinationDBFilePath;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public String getDestinationFolder() {
        return destinationFolder;
    }

    public String getDestinationDBFilePath() {
        return destinationDBFilePath;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
