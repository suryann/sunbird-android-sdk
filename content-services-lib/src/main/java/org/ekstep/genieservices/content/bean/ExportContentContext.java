package org.ekstep.genieservices.content.bean;

import org.ekstep.genieservices.commons.utils.FileUtil;
import org.ekstep.genieservices.content.db.model.ContentModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 5/16/2017.
 *
 * @author anil
 */
public class ExportContentContext {

    private File ecarFile;
    private File tmpLocation;
    private File destinationFolder;
    private List<Map<String, Object>> items;
    private List<ContentModel> contentModelsToExport;
    private Map<String, Object> metadata;
    private Map<String, Object> manifest;

    public ExportContentContext(Map<String, Object> metadata, File destinationFolder, File ecarFile, List<ContentModel> contentModelsToExport) {
        this.metadata = metadata;
        this.destinationFolder = destinationFolder;
        this.ecarFile = ecarFile;
        this.contentModelsToExport = contentModelsToExport;

        this.manifest = new HashMap<>();
        this.tmpLocation = FileUtil.getTempLocation(destinationFolder);
        this.items = new ArrayList<>();
    }

    public File getEcarFile() {
        return ecarFile;
    }

    public File getTmpLocation() {
        return tmpLocation;
    }

    public File getDestinationFolder() {
        return destinationFolder;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public List<ContentModel> getContentModelsToExport() {
        return contentModelsToExport;
    }

    public Map<String, Object> getManifest() {
        return manifest;
    }
}
