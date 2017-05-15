package org.ekstep.genieservices.content;

import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.ekstep.genieservices.BaseService;
import org.ekstep.genieservices.IConfigService;
import org.ekstep.genieservices.IContentFeedbackService;
import org.ekstep.genieservices.IContentService;
import org.ekstep.genieservices.IUserService;
import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.Content;
import org.ekstep.genieservices.commons.bean.ContentAccess;
import org.ekstep.genieservices.commons.bean.ContentAccessCriteria;
import org.ekstep.genieservices.commons.bean.ContentCriteria;
import org.ekstep.genieservices.commons.bean.ContentData;
import org.ekstep.genieservices.commons.bean.ContentSearchCriteria;
import org.ekstep.genieservices.commons.bean.ContentSearchResult;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.MasterData;
import org.ekstep.genieservices.commons.bean.MasterDataValues;
import org.ekstep.genieservices.commons.bean.Profile;
import org.ekstep.genieservices.commons.bean.UserSession;
import org.ekstep.genieservices.commons.bean.Variant;
import org.ekstep.genieservices.commons.bean.enums.ContentType;
import org.ekstep.genieservices.commons.bean.enums.MasterDataType;
import org.ekstep.genieservices.commons.db.contract.ContentEntry;
import org.ekstep.genieservices.commons.utils.DateUtil;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.commons.utils.Logger;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.ekstep.genieservices.content.bean.ContentVariant;
import org.ekstep.genieservices.content.db.model.ContentModel;
import org.ekstep.genieservices.content.db.model.ContentsModel;
import org.ekstep.genieservices.content.network.ContentDetailsAPI;
import org.ekstep.genieservices.content.network.ContentSearchAPI;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created on 5/10/2017.
 *
 * @author anil
 */
public class ContentServiceImpl extends BaseService implements IContentService {

    private static final String TAG = ContentServiceImpl.class.getSimpleName();
    private static final int DEFAULT_PACKAGE_VERSION = -1;

    private IUserService userService;
    private IContentFeedbackService contentFeedbackService;
    private IConfigService configService;

    public ContentServiceImpl(AppContext appContext, IUserService userService, IContentFeedbackService contentFeedbackService, IConfigService configService) {
        super(appContext);

        this.userService = userService;
        this.contentFeedbackService = contentFeedbackService;
        this.configService = configService;
    }

    @Override
    public GenieResponse<Content> getContentDetails(String contentIdentifier) {
        // TODO: Telemetry logger
        String methodName = "getContentDetails@ContentServiceImpl";

        GenieResponse<Content> response;
        ContentModel contentModelInDB = ContentModel.find(mAppContext.getDBSession(), contentIdentifier);

        if (contentModelInDB == null) {
            // Fetch from server if detail is not available in DB
            contentModelInDB = fetchContentDetails(contentIdentifier, contentModelInDB);
            if (contentModelInDB == null) {
                response = GenieResponseBuilder.getErrorResponse(ServiceConstants.NO_DATA_FOUND, "No content found for identifier = " + contentIdentifier, TAG);
                return response;
            }
        } else {
            refreshContentDetails(contentIdentifier, contentModelInDB);
        }

        Content content = getContent(contentModelInDB, true, true);

        response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        response.setResult(content);
        return response;
    }

    private void refreshContentDetails(final String contentIdentifier, final ContentModel existingContentModel) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                fetchContentDetails(contentIdentifier, existingContentModel);
            }
        }).start();
    }

    private ContentModel fetchContentDetails(String contentIdentifier, ContentModel contentModelInDB) {
        ContentDetailsAPI api = new ContentDetailsAPI(mAppContext, contentIdentifier);
        GenieResponse apiResponse = api.get();

        if (apiResponse.getStatus()) {
            String body = apiResponse.getResult().toString();

            // Save into DB
            LinkedTreeMap map = GsonUtil.fromJson(body, LinkedTreeMap.class);
            LinkedTreeMap result = (LinkedTreeMap) map.get("result");
            String contentDataString = GsonUtil.toJson(result.get("content"));
            ContentModel contentModel = ContentModel.build(mAppContext.getDBSession(), GsonUtil.fromJson(contentDataString, Map.class), null);

            if (contentModelInDB == null) {
                // Visibility is setting to Parent because We not need to show any content in local content list
                // until its not imported  as spine or as artifact.
                contentModel.setVisibility(ContentConstants.Visibility.PARENT);
                contentModel.addOrUpdateRefCount(0);
                contentModel.addOrUpdateContentState(ContentConstants.State.SEEN_BUT_NOT_AVAILABLE);

                contentModel.save();
            } else {
                contentModel.setVisibility(contentModelInDB.getVisibility());
                contentModel.addOrUpdateRefCount(contentModelInDB.getRefCount());
                contentModel.addOrUpdateContentState(contentModelInDB.getContentState());

                contentModel.update();
            }

            return contentModel;
        }

        return null;
    }

    private Content getContent(ContentModel contentModel, boolean attachFeedback, boolean attachContentAccess) {
        Content content = new Content();
        content.setIdentifier(contentModel.getIdentifier());

        ContentData serverData = null;
        if (contentModel.getServerData() != null) {
            serverData = GsonUtil.fromJson(contentModel.getServerData(), ContentData.class);
        }

        ContentData localData = null;
        if (contentModel.getLocalData() != null) {
            localData = GsonUtil.fromJson(contentModel.getLocalData(), ContentData.class);
        }

        ContentData contentData = null;
        if (serverData != null) {
            contentData = serverData;

            addContentVariants(contentData, contentModel.getServerData());
        } else if (localData != null) {
            contentData = localData;

            addContentVariants(contentData, contentModel.getLocalData());
        }
        content.setContentData(contentData);

        content.setMimeType(contentModel.getMimeType());
        content.setBasePath(contentModel.getPath());
        content.setContentType(contentModel.getContentType());
        content.setAvailableLocally(isAvailableLocally(contentModel.getContentState()));
        content.setReferenceCount(contentModel.getRefCount());
        content.setUpdateAvailable(isUpdateAvailable(serverData, localData));

        long contentCreationTime = 0;
        String localLastUpdatedTime = contentModel.getLocalLastUpdatedTime();
        if (!StringUtil.isNullOrEmpty(localLastUpdatedTime)) {
            contentCreationTime = DateUtil.dateToEpoch(localLastUpdatedTime.substring(0, localLastUpdatedTime.lastIndexOf(".")), "yyyy-MM-dd'T'HH:mm:ss");
        }
        content.setLastUpdatedTime(contentCreationTime);

        String uid = getCurrentUserId();
        if (attachFeedback) {
            addContentFeedback(content, uid);
        }

        if (attachContentAccess && userService != null) {
            addContentAccess(content, uid);
        }

        return content;
    }

    private void addContentVariants(ContentData contentData, String dataJson) {
        List<Variant> variantList = new ArrayList<>();

        Map<String, Object> dataMap = GsonUtil.fromJson(dataJson, Map.class);
        Object variants = dataMap.get("variants");
        if (variants != null) {
            String variantsString;
            if (variants instanceof Map) {
                variantsString = GsonUtil.getGson().toJson(variants);
            } else {
                variantsString = (String) variants;
            }

            variantsString = variantsString.replace("\\", "");
            ContentVariant contentVariant = GsonUtil.fromJson(variantsString, ContentVariant.class);

            if (contentVariant.getSpine() != null) {
                Variant variant = new Variant("spine", contentVariant.getSpine().getEcarUrl(), contentVariant.getSpine().getSize());
                variantList.add(variant);
            }
        }

        contentData.setVariants(variantList);
    }

    private void addContentFeedback(Content content, String uid) {
        if (contentFeedbackService != null) {
            content.setContentFeedback(contentFeedbackService.getFeedback(uid, content.getIdentifier()).getResult());
        }
    }

    private void addContentAccess(Content content, String uid) {
        if (userService != null) {
            ContentAccessCriteria criteria = new ContentAccessCriteria();
            criteria.setUid(uid);
            criteria.setContentIdentifier(content.getIdentifier());

            List<ContentAccess> contentAccessList = userService.getAllContentAccess(criteria).getResult();
            if (contentAccessList.size() > 0) {
                content.setContentAccess(contentAccessList.get(0));
            }
        }
    }

    private boolean isUpdateAvailable(ContentData serverData, ContentData localData) {
        float lVersion = DEFAULT_PACKAGE_VERSION;
        float sVersion = DEFAULT_PACKAGE_VERSION;

        if (serverData != null && !StringUtil.isNullOrEmpty(serverData.getPkgVersion())) {
            sVersion = Float.valueOf(serverData.getPkgVersion());
        }

        if (localData != null && !StringUtil.isNullOrEmpty(localData.getPkgVersion())) {
            lVersion = Float.valueOf(localData.getPkgVersion());
        }

        return sVersion > 0 && lVersion > 0 && sVersion > lVersion;
    }

    private boolean isAvailableLocally(int contentState) {
        return contentState == ContentConstants.State.ARTIFACT_AVAILABLE;
    }

    @Override
    public GenieResponse<List<Content>> getAllLocalContent(ContentCriteria criteria) {
        // TODO: Telemetry logger
        String methodName = "getAllLocalContent@ContentServiceImpl";

        GenieResponse<List<Content>> response;
        if (criteria == null) {
            criteria = new ContentCriteria();
        }

        String contentTypes;
        if (criteria.getContentTypes() != null) {
            List<String> contentTypeList = new ArrayList<>();
            for (ContentType contentType : criteria.getContentTypes()) {
                contentTypeList.add(contentType.getValue());
            }
            contentTypes = StringUtil.join("','", contentTypeList);
        } else {
            contentTypes = StringUtil.join("','", ContentType.values());
        }

        String isContentType = String.format(Locale.US, "%s in ('%s')", ContentEntry.COLUMN_NAME_CONTENT_TYPE, contentTypes);
        String isVisible = String.format(Locale.US, "%s = '%s'", ContentEntry.COLUMN_NAME_VISIBILITY, ContentConstants.Visibility.DEFAULT);
        // For hiding the non compatible imported content, which visibility is DEFAULT.
        String isArtifactAvailable = String.format(Locale.US, "%s = '%s'", ContentEntry.COLUMN_NAME_CONTENT_STATE, ContentConstants.State.ARTIFACT_AVAILABLE);

        String filter = String.format(Locale.US, " where (%s AND %s AND %s)", isVisible, isArtifactAvailable, isContentType);

        List<ContentModel> contentModelListInDB;
        ContentsModel contentsModel = ContentsModel.find(mAppContext.getDBSession(), filter);
        if (contentsModel != null) {
            contentModelListInDB = contentsModel.getContentModelList();
        } else {
            contentModelListInDB = new ArrayList<>();
        }

        String uid;
        if (!StringUtil.isNullOrEmpty(criteria.getUid())) {
            uid = criteria.getUid();
        } else {
            uid = getCurrentUserId();
        }

        // Get the content access for profile.
        List<ContentAccess> contentAccessList;
        if (userService != null) {
            ContentAccessCriteria contentAccessCriteria = new ContentAccessCriteria();
            contentAccessCriteria.setUid(uid);
            contentAccessList = userService.getAllContentAccess(contentAccessCriteria).getResult();
        } else {
            contentAccessList = new ArrayList<>();
        }

        List<Content> contentList = new ArrayList<>();
        for (ContentAccess contentAccess : contentAccessList) {
            ContentModel contentModel = ContentModel.find(mAppContext.getDBSession(), contentAccess.getIdentifier());
            if (contentModel != null && contentModelListInDB.contains(contentModel)) {
                Content c = getContent(contentModel, criteria.isAttachFeedback(), criteria.isAttachContentAccess());
                c.setContentAccess(contentAccess);
                contentList.add(c);
                contentModelListInDB.remove(contentModel);
            }
        }

        // Add the remaining content into list
        for (ContentModel contentModel : contentModelListInDB) {
            Content c = getContent(contentModel, criteria.isAttachFeedback(), criteria.isAttachContentAccess());
            contentList.add(c);
        }

        response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        response.setResult(contentList);
        return response;
    }

    private String getCurrentUserId() {
        if (userService != null) {
            UserSession userSession = userService.getCurrentUserSession().getResult();
            if (userSession != null) {
                return userSession.getUid();
            }
        }
        return null;
    }

    @Override
    public GenieResponse<List<Content>> getChildContents(String contentIdentifier, int levelAndState) {
        // TODO: Telemetry logger
        String methodName = "getChildContents@ContentServiceImpl";

        GenieResponse<List<Content>> response;
        ContentModel contentModel = ContentModel.find(mAppContext.getDBSession(), contentIdentifier);
        if (contentModel == null) {
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.NO_DATA_FOUND, "No content found for identifier = " + contentIdentifier, TAG);
            return response;
        }

        List<Content> childContentList = new ArrayList<>();

        switch (levelAndState) {
            case ContentConstants.ChildContents.FIRST_LEVEL_ALL:
                childContentList = getAllChildContents(content, childContents);
                break;

            case ContentConstants.ChildContents.FIRST_LEVEL_DOWNLOADED:
                childContentList = populateChildren(content, childContents);
                break;

            case ContentConstants.ChildContents.FIRST_LEVEL_SPINE:
                childContentList = populateChildren(content, childContents);
                break;

            default:
        }

        response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        response.setResult(childContentList);
        return response;
    }

    @Override
    public GenieResponse<Void> deleteContent(String contentIdentifier, int level) {
        GenieResponse<Void> response;
        ContentModel contentModel = ContentModel.find(mAppContext.getDBSession(), contentIdentifier);

        if (contentModel == null) {
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.NO_DATA_FOUND, "No content found to delete for identifier = " + contentIdentifier, TAG);
            return response;
        }

        // TODO: Removing external content code
//        if (contentModel.isExternalContent() && mAppContext.getDeviceInfo().getAndroidSdkVersion() <= mAppContext.getDeviceInfo().getKitkatVersionCode()) {
//            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.FAILED_RESPONSE, "This content cannot be deleted.", TAG);
//            return response;
//        }

        if (contentModel.hasPreRequisites()) {
            List<String> preRequisitesIdentifier = contentModel.getPreRequisitesIdentifiers();
            ContentsModel contentsModel = ContentsModel.findAllContentsWithIdentifiers(mAppContext.getDBSession(), preRequisitesIdentifier);

            if (contentsModel != null) {
                for (ContentModel c : contentsModel.getContentModelList()) {
                    deleteOrUpdateContent(c, true, level);
                }
            }
        }

        //delete or update child items
        if (contentModel.hasChildren()) {
            deleteAllChild(contentModel, level);
        }

        //delete or update parent items
        deleteOrUpdateContent(contentModel, false, level);

        // TODO: Removing external content code
//        if (contentModel.isExternalContent()) {
//            FileHandler.refreshSDCard();
//        }

        response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        return response;
    }

    private void deleteAllChild(ContentModel contentModel, int level) {
        Queue<ContentModel> queue = new LinkedList<>();

        queue.add(contentModel);

        ContentModel node;
        while (!queue.isEmpty()) {
            node = queue.remove();

            if (node.hasChildren()) {
                List<String> childContentsIdentifiers = node.getChildContentsIdentifiers();
                ContentsModel contentsModel = ContentsModel.findAllContentsWithIdentifiers(mAppContext.getDBSession(), childContentsIdentifiers);
                if (contentsModel != null) {
                    queue.addAll(contentsModel.getContentModelList());
                }
            }

            // Deleting only child content
            if (!contentModel.getIdentifier().equalsIgnoreCase(node.getIdentifier())) {
                deleteOrUpdateContent(node, true, level);
            }
        }
    }

    private void deleteOrUpdateContent(ContentModel contentModel, boolean isChildItems, int level) {

        int refCount = contentModel.getRefCount();

        if (level == ContentConstants.Delete.NESTED) {
            // If visibility is Default it means this content was visible in my downloads.
            // After deleting artifact for this content it should not visible as well so reduce the refCount also for this.
            if (refCount > 1 && ContentConstants.Visibility.DEFAULT.equalsIgnoreCase(contentModel.getVisibility())) {
                refCount = refCount - 1;

                // Update visibility
                contentModel.setVisibility(ContentConstants.Visibility.PARENT);
            }

            // Update the contentState
            // Do not update the content state if contentType is Collection / TextBook / TextBookUnit
            if (ContentConstants.Type.COLLECTION.equalsIgnoreCase(contentModel.getContentType())
                    || ContentConstants.Type.TEXTBOOK.equalsIgnoreCase(contentModel.getContentType())
                    || ContentConstants.Type.TEXTBOOK_UNIT.equalsIgnoreCase(contentModel.getContentType())) {
                contentModel.addOrUpdateContentState(ContentConstants.State.ARTIFACT_AVAILABLE);
            } else {
                contentModel.addOrUpdateContentState(ContentConstants.State.ONLY_SPINE);

                // if there are no entry in DB for any content then on this case contentModel.getPath() will be null
                if (contentModel.getPath() != null) {
                    FileHandler.rm(new File(contentModel.getPath()), contentModel.getIdentifier());
                }
            }

        } else {
            // TODO: This check should be before updating the existing refCount.
            // Do not update the content state if contentType is Collection / TextBook / TextBookUnit and refCount is more than 1.
            if ((ContentConstants.Type.COLLECTION.equalsIgnoreCase(contentModel.getContentType())
                    || ContentConstants.Type.TEXTBOOK.equalsIgnoreCase(contentModel.getContentType())
                    || ContentConstants.Type.TEXTBOOK_UNIT.equalsIgnoreCase(contentModel.getContentType()))
                    && refCount > 1) {
                contentModel.addOrUpdateContentState(ContentConstants.State.ARTIFACT_AVAILABLE);
            } else if (refCount > 1 && isChildItems) {  //contentModel.isVisibilityDefault() &&
                // Visibility will remain Default only.

                contentModel.addOrUpdateContentState(ContentConstants.State.ARTIFACT_AVAILABLE);
            } else {

                // Set the visibility to Parent so that this content will not visible in My contents / Downloads section.
                // Update visibility
                if (ContentConstants.Visibility.DEFAULT.equalsIgnoreCase(contentModel.getVisibility())) {
                    contentModel.setVisibility(ContentConstants.Visibility.PARENT);
                }

                contentModel.addOrUpdateContentState(ContentConstants.State.ONLY_SPINE);

                // if there are no entry in DB for any content then on this case contentModel.getPath() will be null
                if (contentModel.getPath() != null) {
                    FileHandler.rm(new File(contentModel.getPath()), contentModel.getIdentifier());
                }
            }

            refCount = refCount - 1;
        }

        // Update the refCount
        contentModel.addOrUpdateRefCount(refCount);

        // if there are no entry in DB for any content then on this case contentModel.getPath() will be null
        if (contentModel.getPath() != null) {
            contentModel.update();
        }
    }

    @Override
    public GenieResponse<ContentSearchResult> searchContent(ContentSearchCriteria contentSearchCriteria) {
        GenieResponse<ContentSearchResult> response;

        Map<String, String[]> filter = contentSearchCriteria.getFilter();
        // Apply profile specific filter
        if (userService != null) {
            GenieResponse<Profile> profileResponse = userService.getCurrentUser();
            if (profileResponse.getStatus()) {
                Profile currentProfile = profileResponse.getResult();

                // Add age filter
                applyFilter(MasterDataType.AGEGROUP, String.valueOf(currentProfile.getAge()), filter);

                // Add board filter
                if (currentProfile.getBoard() != null) {
                    applyFilter(MasterDataType.BOARD, currentProfile.getBoard(), filter);
                }

                // Add medium filter
                if (currentProfile.getMedium() != null) {
                    applyFilter(MasterDataType.MEDIUM, currentProfile.getMedium(), filter);
                }

                // Add standard filter
                applyFilter(MasterDataType.GRADELEVEL, String.valueOf(currentProfile.getStandard()), filter);
            }
        }

        // Apply partner specific filer
        // TODO: 5/15/2017 - Uncomment after partner getting the API for getPartnerInfo in PartnerService.
//        HashMap<String, String> partnerInfo = GsonUtil.fromJson(getSharedPreferenceWrapper().getString(Constants.KEY_PARTNER_INFO, null), HashMap.class);
//        if (partnerInfo != null) {
//            //Apply Channel filter
//            String channel = partnerInfo.get(Constant.BUNDLE_KEY_PARTNER_CHANNEL);
//            if (channel != null) {
//                applyFilter(MasterDataType.CHANNEL, channel, filter);
//            }
//
//            //Apply Purpose filter
//            String purpose = partnerInfo.get(Constant.BUNDLE_KEY_PARTNER_PURPOSE);
//            if (purpose != null) {
//                applyFilter(MasterDataType.PURPOSE, purpose, filter);
//            }
//        }

        contentSearchCriteria.setFilter(filter);

        // Populating implicit search criteria.
        List<String> facets = contentSearchCriteria.getFacets();
        facets.addAll(Arrays.asList("contentType", "domain", "ageGroup", "language", "gradeLevel"));
        contentSearchCriteria.setFacets(facets);

        addFiltersIfNotAvailable(contentSearchCriteria, "objectType", Arrays.asList("Content"));
        addFiltersIfNotAvailable(contentSearchCriteria, "contentType", Arrays.asList("Story", "Worksheet", "Collection", "Game", "TextBook"));
        addFiltersIfNotAvailable(contentSearchCriteria, "status", Arrays.asList("Live"));

        ContentSearchAPI contentSearchAPI = new ContentSearchAPI(mAppContext, getRequest(contentSearchCriteria));
        GenieResponse apiResponse = contentSearchAPI.post();
        if (apiResponse.getStatus()) {
            String body = apiResponse.getResult().toString();

            LinkedTreeMap map = GsonUtil.fromJson(body, LinkedTreeMap.class);
            String id = (String) map.get("id");
            LinkedTreeMap responseParams = (LinkedTreeMap) map.get("params");
            LinkedTreeMap result = (LinkedTreeMap) map.get("result");
            String responseFacetsString = GsonUtil.toJson(result.get("facets"));
            String contentDataListString = GsonUtil.toJson(result.get("content"));

            Type type = new TypeToken<List<HashMap<String, Object>>>() {
            }.getType();
            List<Map<String, Object>> responseFacets = GsonUtil.getGson().fromJson(responseFacetsString, type);
            List<Map<String, Object>> contentDataList = GsonUtil.getGson().fromJson(contentDataListString, type);

            List<Content> contents = new ArrayList<>();
            for (Map contentDataMap : contentDataList) {
                // TODO: 5/15/2017 - Can fetch content from DB and return in response.
                ContentModel contentModel = ContentModel.build(mAppContext.getDBSession(), contentDataMap, null);
                Content content = getContent(contentModel, false, false);
                contents.add(content);
            }

            ContentSearchResult searchResult = new ContentSearchResult();
            searchResult.setId(id);
            searchResult.setParams(responseParams);
            searchResult.setFacets(getSortedFacets(responseFacets));
            searchResult.setRequest(getRequest(contentSearchCriteria));
            searchResult.setContents(contents);

            response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
            response.setResult(searchResult);
            return response;
        }

        response = GenieResponseBuilder.getErrorResponse(apiResponse.getError(), (String) apiResponse.getErrorMessages().get(0), TAG);
        return response;
    }

    // TODO: TO be done by Swayangjit
    private void applyFilter(MasterDataType masterDataType, String propertyValue, Map<String, String[]> filter) {
        try {

            if (masterDataType == MasterDataType.AGEGROUP) {
                masterDataType = MasterDataType.AGE;
            }

            MasterDataValues masterDataValues = null;
            if (configService != null) {
                GenieResponse<MasterData> masterDataResponse = configService.getMasterData(masterDataType);

                MasterData masterData = null;
                if (masterDataResponse.getStatus()) {
                    masterData = masterDataResponse.getResult();
                }

                for (MasterDataValues values : masterData.getValues()) {
                    if (values.getValue().equals(propertyValue)) {
                        masterDataValues = values;
                        break;
                    }
                }
            }
            Map termMap = (Map) map.get(propertyValue);

            String masterDataTypeValue = masterDataType.getValue();

            Set termSet = new HashSet((List) termMap.get(masterDataTypeValue));
            if (filter.containsKey(masterDataTypeValue)) {
                if (filter.get(masterDataTypeValue) != null) {
                    Set set = new HashSet(Arrays.asList(filter.get(masterDataTypeValue)));
                    if (set != null && termSet != null) {
                        termSet.addAll(set);
                    }
                }
            }

            String[] strArr = new String[termSet.size()];
            termSet.toArray(strArr);
            filter.put(masterDataTypeValue, strArr);
        } catch (Exception e) {
            Logger.e(TAG, "Failed to apply filter");
        }
    }

    private void addFiltersIfNotAvailable(ContentSearchCriteria contentSearchCriteria, String key, List<String> values) {
        Map<String, String[]> filter = contentSearchCriteria.getFilter();
        if (filter == null) {
            filter = new HashMap<>();
        }

        if (filter.isEmpty() || filter.get(key) == null) {
            String[] newValues = values.toArray(new String[values.size()]);
            filter.put(key, newValues);
        }

        contentSearchCriteria.setFilter(filter);
    }

    private HashMap<String, Object> getRequest(ContentSearchCriteria criteria) {
        HashMap<String, Object> request = new HashMap<>();
        request.put("query", criteria.getQuery());
        request.put("limit", criteria.getLimit());
        request.put("mode", "soft");

        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("compatibilityLevel", getCompatibilityLevel());
        if (criteria.getFilter() != null) {
            filterMap.putAll(criteria.getFilter());
        }
        request.put("filters", filterMap);

        if (criteria.getSort() != null) {
            request.put("sort_by", criteria.getSort());
        }

        if (criteria.getFacets() != null) {
            request.put("facets", criteria.getFacets());
        }

        return request;
    }

    private HashMap<String, Integer> getCompatibilityLevel() {
        HashMap<String, Integer> compatLevelMap = new HashMap<>();
        compatLevelMap.put("max", ContentModel.maxCompatibilityLevel);
        compatLevelMap.put("min", ContentModel.minCompatibilityLevel);
        return compatLevelMap;
    }

    private List<Map<String, Object>> getSortedFacets(List<Map<String, Object>> facets) {
        if (configService == null) {
            return facets;
        }

        GenieResponse<Map<String, Object>> ordinalsResponse = configService.getOrdinals();
        if (ordinalsResponse.getStatus()) {

            Map<String, Object> ordinalsMap = ordinalsResponse.getResult();

            if (ordinalsMap != null) {
                List<Map<String, Object>> sortedFacetList = new ArrayList<>();
                for (Map<String, Object> facetMap : facets) {
                    for (String nameKey : facetMap.keySet()) {
                        if (nameKey.equals("name")) {
                            String facetName = (String) facetMap.get(nameKey);

                            String facetValuesString = GsonUtil.toJson(facetMap.get("values"));
                            Type facetType = new TypeToken<List<Map<String, Object>>>() {
                            }.getType();
                            List<Map<String, Object>> facetValues = GsonUtil.getGson().fromJson(facetValuesString, facetType);

                            if (ordinalsMap.containsKey(facetName)) {
                                String dataString = GsonUtil.toJson(ordinalsMap.get(facetName));
                                Type type = new TypeToken<List<String>>() {
                                }.getType();
                                List<String> facetsOrder = GsonUtil.getGson().fromJson(dataString, type);

                                List<Map<String, Object>> valuesList = sortOrder(facetValues, facetsOrder);

                                HashMap<String, Object> map = new HashMap<>();
                                map.put("name", facetName);
                                map.put("values", valuesList);

                                sortedFacetList.add(map);
                            }
                            break;
                        }
                    }
                }

                return sortedFacetList;
            }
        }

        return facets;
    }

    private List<Map<String, Object>> sortOrder(List<Map<String, Object>> facetValues, List<String> facetsOrder) {
        Map<Integer, Map<String, Object>> map = new TreeMap<>();

        for (Map<String, Object> value : facetValues) {
            String name = (String) value.get("name");
            int index = indexOf(facetsOrder, name);
            map.put(index, value);
        }

        List<Map<String, Object>> valuesList = new ArrayList<>(map.values());
        return valuesList;
    }

    private int indexOf(List<String> responseFacets, String key) {
        if (!StringUtil.isNullOrEmpty(key)) {
            for (int i = 0; i < responseFacets.size(); i++) {
                if (key.equalsIgnoreCase(responseFacets.get(i))) {
                    return i;
                }
            }
        }

        return -1;
    }

}
