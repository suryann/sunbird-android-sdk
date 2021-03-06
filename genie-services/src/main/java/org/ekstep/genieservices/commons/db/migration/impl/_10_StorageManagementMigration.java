package org.ekstep.genieservices.commons.db.migration.impl;

import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.db.contract.ContentEntry;
import org.ekstep.genieservices.commons.db.contract.NoSqlEntry;
import org.ekstep.genieservices.commons.db.migration.Migration;
import org.ekstep.genieservices.commons.utils.FileUtil;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.ekstep.genieservices.content.ContentHandler;
import org.ekstep.genieservices.content.db.model.ContentModel;
import org.ekstep.genieservices.content.db.model.ContentsModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class _10_StorageManagementMigration extends Migration {

    //DON'T CHANGE THESE VALUES
    private static final int MIGRATION_NUMBER = 10;
    private static final int TARGET_DB_VERSION = 15;

    public _10_StorageManagementMigration() {
        super(MIGRATION_NUMBER, TARGET_DB_VERSION);
    }

    @Override
    public void apply(AppContext appContext) {
        // Create nosql Table
        appContext.getDBSession().execute(NoSqlEntry.getCreateEntry());

        // Create size col in content table
        appContext.getDBSession().execute(ContentEntry.getAlterEntryForContentSize());

        updateContentPath(appContext);
    }

    private void updateContentPath(AppContext appContext) {
        ContentsModel contentsModel = ContentsModel.find(appContext.getDBSession(), "");
        if (contentsModel != null) {
            for (ContentModel contentModel : contentsModel.getContentModelList()) {
                String path = contentModel.getPath();

                if (!path.endsWith(contentModel.getIdentifier())) {
                    String contentRootPath = StringUtil.getFirstPartOfThePathNameOnLastDelimiter(path);

                    if (contentRootPath != null) {
                        StringBuilder stringBuilder = new StringBuilder(contentRootPath);
                        stringBuilder.append("/");
                        stringBuilder.append(contentModel.getIdentifier());

                        //Rename the folder
                        if (renameOldFolder(path, stringBuilder.toString())) {

                            //set the path
                            path = stringBuilder.toString();
                        }
                    }
                }

                //update the size of the content here
                long size = FileUtil.getFileSize(new File(path));

                //update both the path and the size
                String updateQuery = String.format(Locale.US, "UPDATE %s SET %s = '%s', %s = '%s'  where %s = '%s';",
                        ContentEntry.TABLE_NAME,
                        ContentEntry.COLUMN_NAME_PATH, path,
                        ContentEntry.COLUMN_NAME_SIZE_ON_DEVICE, size,
                        ContentEntry.COLUMN_NAME_IDENTIFIER, contentModel.getIdentifier());

                appContext.getDBSession().execute(updateQuery);

                //create the manifest for this content
                List<ContentModel> contentModelList = new ArrayList<>();
                contentModelList.add(contentModel);

                ContentHandler.createAndWriteManifest(appContext, contentModelList);
            }
        }
    }

    private boolean renameOldFolder(String oldName, String newName) {
        File oldPath = new File(oldName);
        File newPath = new File(newName);

        return oldPath.renameTo(newPath);
    }
}
