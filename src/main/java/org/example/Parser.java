package org.example;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Parser {

    public static HttpResponse<InputStream> requestGet(String host,
                                                       String requestUrl,
                                                       String[] requestHeaders) {
        String url = getUrl(host, requestUrl, "GET");
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

    public static HttpResponse<InputStream> requestPost(String host,
                                                        String requestUrl,
                                                        String[] requestHeaders,
                                                        String postData) {


        String url = getUrl(host, requestUrl, "POST");
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

    private static String getUrl(String host, String requestUrl, String regex) {
        requestUrl = requestUrl.replace(regex + " ", "");
        requestUrl = requestUrl.replace(" HTTP/1.1", "");
        if (requestUrl.equals("/")) {
            requestUrl = "/ASTUP_WEB/";
        }
        return host + requestUrl;
    }
}
