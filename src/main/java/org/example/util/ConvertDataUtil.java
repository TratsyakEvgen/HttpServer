package org.example.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ConvertDataUtil {
    public static String getMapEntryToString(Map.Entry<String, List<String>> entry) {
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

    public static String getInputStreamToString(InputStream inputStream, String charsetName) throws IOException {
        return new String(inputStream.readAllBytes(), charsetName);
    }

    public static String getBufferReaderToString(BufferedReader bufferedReader) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        while (bufferedReader.ready()){
            stringBuilder.append((char) bufferedReader.read());
        }
        return String.valueOf(stringBuilder);
    }


}
