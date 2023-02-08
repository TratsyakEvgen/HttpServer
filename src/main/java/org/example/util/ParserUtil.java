package org.example.util;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ParserUtil {
    private final String HOST = "http://10.247.16.133:8080";

    public HttpResponse<InputStream> executeGetRequest(String requestUrl, String[] requestHeaders) {
        String url = getUrl(requestUrl, "GET");
        try {
            HttpRequest request;
            if (requestHeaders.length != 0) {
                request = HttpRequest.newBuilder(new URI(url))
                        .GET()
                        .headers(requestHeaders)
                        .build();
            } else {
                request = HttpRequest.newBuilder(new URI(url))
                        .GET()
                        .build();
            }
            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.out.println(e.getMessage().toCharArray());
        }
        return null;
    }

    public HttpResponse<InputStream> executePostRequest(String requestUrl, String[] requestHeaders, String postData) {
        String url = getUrl(requestUrl, "POST");
        postData = postData.replace("+", " ");
        try {
            HttpRequest request;
            if (requestHeaders.length != 0) {
                request = HttpRequest.newBuilder(new URI(url))
                        .POST(HttpRequest.BodyPublishers.ofString(postData))
                        .headers(requestHeaders)
                        .build();
            } else {
                request = HttpRequest.newBuilder(new URI(url))
                        .GET()
                        .build();
            }
            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.out.println(e.getMessage().toCharArray());
        }
        return null;
    }

    private String getUrl(String requestUrl, String regex) {
        requestUrl = requestUrl.replace(regex + " ", "");
        requestUrl = requestUrl.replace(" HTTP/1.1", "");
        if (requestUrl.equals("/")) {
            requestUrl = "/ASTUP_WEB/";
        }
        return HOST + requestUrl;
    }
}
