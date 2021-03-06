package org.ekstep.genieresolvers.user;

import android.content.Context;
import android.net.Uri;

import org.ekstep.genieresolvers.BaseTask;
import org.ekstep.genieresolvers.util.Constants;
import org.ekstep.genieservices.commons.bean.GenieResponse;

import java.util.Map;

/**
 * Created on 23/5/17.
 * shriharsh
 */

public class DeleteUserTask extends BaseTask {
    private String appQualifier;
    private String userId;

    public DeleteUserTask(Context context, String appQualifier, String userId) {
        super(context);
        this.appQualifier = appQualifier;
        this.userId = userId;
    }

    @Override
    protected String getLogTag() {
        return DeleteUserTask.class.getSimpleName();
    }

    @Override
    protected GenieResponse<Map> execute() {
        int response = contentResolver.delete(getUri(), null, new String[]{userId});

        if (response != 1) {
           return getErrorResponse(Constants.PROCESSING_ERROR, getErrorMessage(), DeleteUserTask.class.getSimpleName());
        }

        return getSuccessResponse(Constants.SUCCESSFUL);
    }

    @Override
    protected String getErrorMessage() {
        return "Unable delete the user!";
    }

    private Uri getUri() {
        String authority = String.format("content://%s.profiles", appQualifier);
        return Uri.parse(authority);
    }

}
