package org.ekstep.genieservices.content.chained.move;

import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.MoveContentResponse;
import org.ekstep.genieservices.commons.chained.IChainable;
import org.ekstep.genieservices.commons.utils.CollectionUtil;
import org.ekstep.genieservices.commons.utils.FileUtil;
import org.ekstep.genieservices.content.bean.MoveContentContext;

import java.util.List;

/**
 * Created on 9/18/2017.
 *
 * @author anil
 */
public class DeviceMemoryCheck implements IChainable<List<MoveContentResponse>, MoveContentContext> {

    private static final String TAG = DeviceMemoryCheck.class.getSimpleName();

    private IChainable<List<MoveContentResponse>, MoveContentContext> nextLink;

    @Override
    public GenieResponse<List<MoveContentResponse>> execute(AppContext appContext, MoveContentContext moveContentContext) {
        long deviceUsableSpace = FileUtil.getFreeUsableSpace(moveContentContext.getDestinationFolder());
        // TODO: 9/25/2017 - Write the query to read the summation of size on device.
        long spaceRequired = 0;
        if (CollectionUtil.isNullOrEmpty(moveContentContext.getContentIds())) {
            // Get size of all content stored in DB.
        } else {
            // Get the size of given identifier.
        }

        if (!FileUtil.isFreeSpaceAvailable(deviceUsableSpace, spaceRequired, 0)) {
            return GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.MOVE_FAILED,
                    String.format("%s bytes available free space, required %s bytes.", deviceUsableSpace, spaceRequired),
                    TAG);
        }

        return nextLink.execute(appContext, moveContentContext);
    }

    @Override
    public IChainable<List<MoveContentResponse>, MoveContentContext> then(IChainable<List<MoveContentResponse>, MoveContentContext> link) {
        nextLink = link;
        return link;
    }
}
