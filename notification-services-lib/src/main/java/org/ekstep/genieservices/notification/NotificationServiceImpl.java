package org.ekstep.genieservices.notification;

import org.ekstep.genieservices.BaseService;
import org.ekstep.genieservices.INotificationService;
import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.Notification;
import org.ekstep.genieservices.commons.bean.NotificationFilterCriteria;
import org.ekstep.genieservices.notification.db.model.NotificationModel;
import org.ekstep.genieservices.notification.db.model.NotificationsModel;

import java.util.List;

public class NotificationServiceImpl extends BaseService implements INotificationService {
    private static final String TAG = NotificationServiceImpl.class.getSimpleName();

    public NotificationServiceImpl(AppContext appContext) {
        super(appContext);
    }

    @Override
    public GenieResponse<Void> addNotification(Notification notification) {
        try {
            NotificationModel notificationModel = NotificationHandler.convertNotificationMapToModel(mAppContext.getDBSession(), notification);
            NotificationModel oldNotification = NotificationModel.find(mAppContext.getDBSession(), notification.getMsgid());

            if (oldNotification != null) {
                notificationModel.update();
            } else {
                notificationModel.save();
            }

            return GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        } catch (Exception e) {
            e.printStackTrace();
            return GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.ADD_FAILED,
                    ServiceConstants.ErrorMessage.FAILED_TO_ADD_UPDATE_NOTIFICATION, TAG);
        }
    }

    @Override
    public GenieResponse<Notification> updateNotification(Notification notification) {
        // -1 to update all the notifications
        double msgId = notification.getMsgid();
        try {
            NotificationsModel notificationsUpdate = NotificationsModel.build(mAppContext.getDBSession(), NotificationHandler.getFilterConditionToUpdate(msgId));
            notificationsUpdate.update();
            return GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        } catch (Exception e) {
            return GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.UPDATE_FAILED, ServiceConstants.ErrorMessage.FAILED_TO_UPDATE_THE_NOTIFICATION, TAG);
        }
    }

    @Override
    public GenieResponse<Void> deleteNotification(int msgId) {
        try {
            NotificationModel notification = NotificationModel.build(mAppContext.getDBSession(), msgId);
            mAppContext.getDBSession().clean(notification);

            return GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        } catch (Exception e) {
            return GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.DELETE_FAILED, ServiceConstants.ErrorMessage.FAILED_TO_DELETE_NOTIFICATION, TAG);
        }
    }

    @Override
    public GenieResponse<List<Notification>> getAllNotifications(NotificationFilterCriteria criteria) {
        try {
            NotificationsModel notifications = NotificationsModel.build(mAppContext.getDBSession(), NotificationHandler.getFilterCondition(criteria));

            //Deletes all expired notifications
            mAppContext.getDBSession().clean(notifications);
            //Reads all valid notifications
            mAppContext.getDBSession().read(notifications);

            GenieResponse successResponse = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
            List<Notification> notificationBeans = notifications.getNotificationBeans();
//
            successResponse.setResult(notificationBeans);
            return successResponse;
        } catch (Exception e) {
            return GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.NO_NOTIFICATIONS_FOUND, ServiceConstants.ErrorMessage.ERROR_WHILE_GETTING_NOTIFICATIONS, TAG);
        }
    }
}
