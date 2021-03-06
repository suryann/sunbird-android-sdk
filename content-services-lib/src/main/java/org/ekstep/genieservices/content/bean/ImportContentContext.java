package org.ekstep.genieservices.content.bean;

import org.ekstep.genieservices.commons.bean.ContentImportResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 5/16/2017.
 *
 * @author anil
 */
public class ImportContentContext {

    private boolean isChildContent;
    private String ecarFilePath;
    private String destinationFolder;

    private Map<String, Object> metadata;
    private String manifestVersion;
    private List<String> skippedItemsIdentifier;
    private List<Map<String, Object>> items;
    private List<String> identifiers;
    private List<ContentImportResponse> contentImportResponseList;

    public ImportContentContext(boolean isChildContent, String ecarFilePath, String destinationFolder) {
        this.isChildContent = isChildContent;
        this.ecarFilePath = ecarFilePath;
        this.destinationFolder = destinationFolder;

        this.metadata = new HashMap<>();
        this.items = new ArrayList<>();
        this.identifiers = new ArrayList<>();
        this.skippedItemsIdentifier = new ArrayList<>();
        this.contentImportResponseList = new ArrayList<>();
    }

    public boolean isChildContent() {
        return isChildContent;
    }

    public String getEcarFilePath() {
        return ecarFilePath;
    }

    public String getDestinationFolder() {
        return destinationFolder;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public String getManifestVersion() {
        return manifestVersion;
    }

    public void setManifestVersion(String manifestVersion) {
        this.manifestVersion = manifestVersion;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public List<String> getSkippedItemsIdentifier() {
        return skippedItemsIdentifier;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public List<ContentImportResponse> getContentImportResponseList() {
        return contentImportResponseList;
    }
}
