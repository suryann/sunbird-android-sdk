package org.ekstep.genieservices.async;

import android.os.AsyncTask;

import org.ekstep.genieservices.GenieService;
import org.ekstep.genieservices.ITelemetryService;
import org.ekstep.genieservices.commons.IResponseHandler;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.TelemetryExportRequest;
import org.ekstep.genieservices.commons.bean.TelemetryExportResponse;
import org.ekstep.genieservices.commons.bean.TelemetryImportRequest;
import org.ekstep.genieservices.commons.bean.TelemetryStat;
import org.ekstep.genieservices.commons.bean.telemetry.Telemetry;

/**
 * This class provides all the required APIs to perform necessary operations related to Telemetry on a separate thread.
 */
public class TelemetryService {

    private ITelemetryService telemetryService;

    public TelemetryService(GenieService genieService) {
        this.telemetryService = genieService.getTelemetryService();
    }

    /**
     * This api will save the telemetry details passed to it as String.
     * <p>
     * <p> On successful saving the telemetry, the response will return status as TRUE and with "Event Saved Successfully" message.
     * <p>
     * <p>On failing to save the telemetry details, the response will return status as FALSE and the error be the following:
     * <p>PROCESSING_ERROR
     *
     * @param eventString
     * @param responseHandler - {@link IResponseHandler<Void>}
     */
    public void saveTelemetry(final String eventString, IResponseHandler<Void> responseHandler) {
        new AsyncHandler<Void>(responseHandler).execute(new IPerformable<Void>() {
            @Override
            public GenieResponse<Void> perform() {
                return telemetryService.saveTelemetry(eventString);
            }
        });
    }

    /**
     * This api will save the telemetry details passed to it as {@link Telemetry}.
     * <p>
     * <p> On successful saving the telemetry, the response will return status as TRUE and with "Event Saved Successfully" message.
     * <p>
     * <p>On failing to save the telemetry details, the response will return status as FALSE and the error be the following:
     * <p>PROCESSING_ERROR
     *
     * @param event           - {@link Telemetry}
     * @param responseHandler - {@link IResponseHandler<Void>}
     */
    public void saveTelemetry(final Telemetry event, IResponseHandler<Void> responseHandler) {
        new AsyncHandler<Void>(responseHandler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new IPerformable<Void>() {
            @Override
            public GenieResponse<Void> perform() {
                return telemetryService.saveTelemetry(event);
            }
        });
    }

    /**
     * This api will give the telemetry stats about unsynced events and last sync time in {@link TelemetryStat}
     * <p>
     * <p>Response status always be True, with {@link TelemetryStat} set in the result.
     *
     * @param responseHandler - {@link IResponseHandler<TelemetryStat>}
     */
    public void getTelemetryStat(IResponseHandler<TelemetryStat> responseHandler) {
        new AsyncHandler<TelemetryStat>(responseHandler).execute(new IPerformable<TelemetryStat>() {
            @Override
            public GenieResponse<TelemetryStat> perform() {
                return telemetryService.getTelemetryStat();
            }
        });
    }

    /**
     * This api is used to import the telemetry.
     * <p>
     * <p> On successful importing the telemetry, the response will return status as TRUE.
     * <p>
     * <p>On failing to importing the telemetry, the response will return status as FALSE and the error be the following:
     * <p>INVALID_FILE
     *
     * @param telemetryImportRequest - {@link TelemetryImportRequest}
     * @param responseHandler        - {@link IResponseHandler<Void>}
     */
    public void importTelemetry(final TelemetryImportRequest telemetryImportRequest, IResponseHandler<Void> responseHandler) {
        new AsyncHandler<Void>(responseHandler).execute(new IPerformable<Void>() {
            @Override
            public GenieResponse<Void> perform() {
                return telemetryService.importTelemetry(telemetryImportRequest);
            }
        });
    }

    /**
     * This api is used to export the telemetry.
     * <p>
     * <p> On successful exporting the telemetry, the response will return status as TRUE.
     * <p>
     * <p>On failing to exporting the telemetry, the response will return status as FALSE and the error be the following:
     * <p>EXPORT_FAILED
     *
     * @param telemetryExportRequest - {@link TelemetryExportRequest}
     * @param responseHandler        - {@link IResponseHandler<TelemetryExportResponse>}
     */
    public void exportTelemetry(final TelemetryExportRequest telemetryExportRequest, IResponseHandler<TelemetryExportResponse> responseHandler) {
        new AsyncHandler<TelemetryExportResponse>(responseHandler).execute(new IPerformable<TelemetryExportResponse>() {
            @Override
            public GenieResponse<TelemetryExportResponse> perform() {
                return telemetryService.exportTelemetry(telemetryExportRequest);
            }
        });
    }

}
