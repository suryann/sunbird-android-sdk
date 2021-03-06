package org.ekstep.genieservices.tag.cache;

import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.utils.CollectionUtil;
import org.ekstep.genieservices.commons.utils.DateUtil;
import org.ekstep.genieservices.tag.model.TagModel;
import org.ekstep.genieservices.tag.model.TagsModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created on 26/4/17.
 *
 * @author swayangjit
 */
public class TelemetryTagCache {

    private static Set<String> hashedtags;
    private static long validUntil;

    public static Set<String> activeTags(AppContext appContext) {
        if (CollectionUtil.isNullOrEmpty(hashedtags) || (DateUtil.getEpochTime() > validUntil)) {
            hashedtags = new HashSet<>();
            TagsModel telemetryTags = TagsModel.find(appContext.getDBSession());
            hashedtags.addAll(activeTagHashes(telemetryTags.getTags()));
            validUntil = DateUtil.getTodayMidnightEpochTime();
        }
        return hashedtags;
    }

    public static void clearCache(AppContext appContext) {
        hashedtags = null;
        validUntil = 0;
    }

    private static Set<String> activeTagHashes(List<TagModel> tags) {
        Set<String> hashedTags = new HashSet<>();
        for (TagModel tag : tags) {
            if (DateUtil.isTodayWithin(tag.startDate(), tag.endDate())) {
                hashedTags.add(tag.tagHash());
            }
        }
        return hashedTags;
    }
}
