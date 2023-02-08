package org.example.service;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class ParserService {

    private final String url;
    private final String[] requestHeaders;
    private String postData;

    public ParserService(String url, String[] requestHeaders) {
        this.url = url;
        this.requestHeaders = requestHeaders;
    }

    public ParserService(String url, String[] requestHeaders, String postData) {
        this.url = url;
        this.requestHeaders = requestHeaders;
        this.postData = postData;
    }

    public HttpResponse<InputStream> executeGetRequest() {
        try {
            HttpRequest request = getBuilder().GET().build();
            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage() + Arrays.toString(e.getStackTrace()));
        }
        return null;
    }

    public HttpResponse<InputStream> executePostRequest() {
        try {
            HttpRequest request = getBuilder().POST(HttpRequest.BodyPublishers.ofString(postData)).build();
            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch ( IOException | InterruptedException e) {
            System.out.println(e.getMessage() + Arrays.toString(e.getStackTrace()));
        }
        return null;
    }

    private HttpRequest.Builder getBuilder() {
        var builder = HttpRequest.newBuilder();
        try {
            builder.uri(new URI(url));
        } catch (URISyntaxException | NullPointerException  e) {
            System.out.println(e.getMessage() + Arrays.toString(e.getStackTrace()));
        }
        if (requestHeaders.length != 0) {
            builder.headers(requestHeaders);
        }
        return builder;
    }




}
