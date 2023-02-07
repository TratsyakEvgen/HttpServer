package org.example.util;

import java.util.List;
import java.util.Map;

public class ConvertDataUtil {
    public static String convertMapEntryToString(Map.Entry<String, List<String>> entry) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(entry.getKey()).append(": ");
        for (String value : entry.getValue()) {
            stringBuilder.append(value).append("; ");
        }
        int size = stringBuilder.length();
        if (size != 0) {
            stringBuilder.replace(size - 2, size, "\r\n");
        }
        return stringBuilder.toString();
    }


}
