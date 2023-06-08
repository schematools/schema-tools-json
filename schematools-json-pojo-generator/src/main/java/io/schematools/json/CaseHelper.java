package io.schematools.json;

import org.apache.commons.text.CaseUtils;

public class CaseHelper {

    public static String convertToCamelCase(String in, boolean capitalizeFirstLetter) {
        return CaseUtils.toCamelCase(in, capitalizeFirstLetter, '-', '_');
    }

}
