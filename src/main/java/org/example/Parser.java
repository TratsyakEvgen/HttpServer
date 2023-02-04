package org.example;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Parser {

    public static Map<String, String> requestGet(String protocol,
                                                 String host,
                                                 String requestUrl,
                                                 Map<String, String> mapRequestHeaders) {

        String url = getUrl(protocol, host, requestUrl, "GET");

        try {
            Connection.Response response = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .headers(mapRequestHeaders)
                    .execute().bufferUp();

            return getResponse(protocol, host, response);
        } catch (IOException e) {
            e.getStackTrace();
        }
        return null;
    }

    public static Map<String, String> requestPost(String protocol,
                                                  String host, String requestUrl,
                                                  Map<String, String> mapRequestHeaders,
                                                  Map<String, String> postData) {

        String url = getUrl(protocol, host, requestUrl, "POST");

        try {
            Connection.Response response = Jsoup.connect(url)
                    .method(Connection.Method.POST)
                    .headers(mapRequestHeaders)
                    .data(postData)
                    .execute().bufferUp();

            return getResponse(protocol, host, response);
        } catch (IOException e) {
            e.getStackTrace();
        }
        return null;
    }

    private static String convertMapToString(Map<String, String> map) {
        return map.keySet().stream()
                .map(key -> key + ": " + map.get(key))
                .collect(Collectors.joining("\r\n"));
    }

    private static String getUrl(String protocol, String host, String requestUrl, String regex) {
        requestUrl = requestUrl.replace(regex + " ", "");
        requestUrl = requestUrl.replace(" HTTP/1.1", "");
        return protocol + host + requestUrl;
    }

    private static Map<String, String> getResponse(String protocol, String host, Connection.Response response) throws IOException {
        Map<String, String> map = response.headers();
        map.remove("Content-Encoding");
        String responseHeaders = convertMapToString(map);

        String body = response.parse().html();
        body = body.replaceAll(protocol + host, "http://localhost:8080");

        Map<String, String> responseDocument = new HashMap<>();
        responseDocument.put(responseHeaders, body);
        return responseDocument;
    }
}
