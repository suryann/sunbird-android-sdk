package org.ekstep.genieservices.commons.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created on 26/4/17.
 *
 * @author swayangjit
 */
public class CollectionUtil {

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean hasEmptyData(String[] array) {
        if (isEmpty(array)) {
            return true;
        }

        for (String item : array) {
            if (item != null && !item.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public static String mapToCommaSeparatedString(Collection<Object> list) {
        if (list == null) {
            return null;
        }

        StringBuilder commaSeparatedString = new StringBuilder();
        for (Object item : list) {
            commaSeparatedString.append(item);
            commaSeparatedString.append("; ");
        }

        return commaSeparatedString.toString().trim();
    }

    public static boolean containsMap(List<Map<String, Object>> list, String key) {
        if (list == null || list.isEmpty()) {
            return false;
        }

        for (Map<String, Object> map : list) {
            if (map.containsKey(key))
                return true;
        }

        return false;
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isKeyNotAvailable(Map map, String key) {
        return (!map.containsKey(key) || (map.containsKey(key) && StringUtil.isNullOrEmpty(String.valueOf(map.get(key)))));
    }

}
