package org.ekstep.genieservices.auth;

import org.ekstep.genieservices.IAuthSession;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.bean.Session;
import org.ekstep.genieservices.commons.network.NetworkConstants;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.commons.utils.StringUtil;

/**
 * Created on 1/3/18.
 *
 * @author souvikmondal
 */
public abstract class AbstractAuthSessionImpl implements IAuthSession<Session> {

    protected AppContext mAppContext;
    private Session mSession;

    @Override
    public void initAuth(AppContext appContext) {
        mAppContext = appContext;
        init();
    }

    private void init() {
        String s = mAppContext.getKeyValueStore().getString(NetworkConstants.OAUTH_TOKEN, "");
        if (!StringUtil.isNullOrEmpty(s)) {
            mSession = GsonUtil.fromJson(s, Session.class);
        }
    }

    @Override
    public void startSession(Session session) {
        mSession = session;
        saveSession(session);
    }

    private void saveSession(Session session) {
        mAppContext.getKeyValueStore().putString(NetworkConstants.OAUTH_TOKEN, GsonUtil.toJson(session));
    }

    @Override
    public void endSession() {
        mSession = null;
        clearSession();
    }

    private void clearSession() {
        mAppContext.getKeyValueStore().remove(NetworkConstants.OAUTH_TOKEN);
    }

    @Override
    public Session getSessionData() {
        return mSession;
    }

}
