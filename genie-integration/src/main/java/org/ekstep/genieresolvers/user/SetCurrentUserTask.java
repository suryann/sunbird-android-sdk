package org.ekstep.genieresolvers.user;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ekstep.genieresolvers.BaseTask;
import org.ekstep.genieresolvers.util.Constants;
import org.ekstep.genieservices.commons.bean.GenieResponse;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created on 23/5/17.
 * shriharsh
 */

public class SetCurrentUserTask extends BaseTask {

    private final String TAG = SetCurrentUserTask.class.getSimpleName();
    private String appQualifier;
    private String userId;

    public SetCurrentUserTask(Context context, String appQualifier, String userId) {
        super(context);
        this.appQualifier = appQualifier;
        this.userId = userId;
    }

    @Override
    protected String getLogTag() {
        return SetCurrentUserTask.class.getSimpleName();
    }

    @Override
    protected GenieResponse<Map> execute() {
        Cursor cursor = contentResolver.query(getUri(), null, userId, null, null);
        if (cursor == null || cursor.getCount() == 0) {
            return getErrorResponse(Constants.PROCESSING_ERROR, getErrorMessage(), TAG);
        }

        return getResponse(cursor);
    }

    private GenieResponse<Map> getResponse(Cursor cursor) {
        GenieResponse<Map> mapData = null;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                mapData = readCursor(cursor);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return mapData;
    }

    private GenieResponse<Map> readCursor(Cursor cursor) {
        Gson gson = new Gson();
        String serverData = cursor.getString(0);
        Type type = new TypeToken<GenieResponse<Map>>() {
        }.getType();
        GenieResponse<Map> response = gson.fromJson(serverData, type);
        return response;
    }

    @Override
    protected String getErrorMessage() {
        return "Could not set user!";
    }

    private Uri getUri() {
        String authority = String.format("content://%s.profiles/setUser", appQualifier);
        return Uri.parse(authority);
    }

}
