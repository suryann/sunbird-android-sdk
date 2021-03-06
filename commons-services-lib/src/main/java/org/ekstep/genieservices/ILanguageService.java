package org.ekstep.genieservices;

import org.ekstep.genieservices.commons.bean.GenieResponse;

/**
 * This is the interface with all the required APIs for performing Language related
 * operations.
 */
public interface ILanguageService {

    /**
     * This api is used to get the traversal rule for the language
     * <p>
     * <p>
     * On successful retrieval of the traversal rule from the server, the response will be with status TRUE and the result will be set to {@link String}
     * <p>
     * <p>
     * On failing to retrieve the rule,the response with return status as FALSE with one of the following errors
     * <p>CONNECTION_ERROR
     * <p>SERVER_ERROR
     * <p>NETWORK_ERROR
     *
     * @param languageId - {@link String}
     * @return {@link GenieResponse<String>}
     */
    GenieResponse<String> getLanguageTraversalRule(String languageId);

    /**
     * This api is used to search the language
     * <p>
     * <p>
     * On successful search of the language from the server, the response will be with status TRUE and the result will be set to {@link String}
     * <p>
     * <p>
     * On failing to retrieve the rule,the response with return status as FALSE with one of the following errors
     * <p>CONNECTION_ERROR
     * <p>SERVER_ERROR
     * <p>NETWORK_ERROR
     *
     * @param requestData - {@link String}
     * @return {@link GenieResponse<String>}
     */
    GenieResponse<String> getLanguageSearch(String requestData);
}
