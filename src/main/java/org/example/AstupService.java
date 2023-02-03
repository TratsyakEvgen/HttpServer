package org.example;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AstupService {

    private String cookies;

    public String getCookies() {
        return cookies;
    }

    public void setCookies(String cookies) {
        this.cookies = cookies;
    }

    public String authorize() {
        Connection.Response response = null;
        try {
            response = Jsoup.connect("http://10.247.16.133:8080/ASTUP_WEB/")
                    .method(Connection.Method.GET)
                    .execute().bufferUp();
            String body = response.parse().html();
            this.cookies = convertWithStream(response.cookies());
            body = body.replaceAll("/ASTUP_WEB/", "http://10.247.16.133:8080/ASTUP_WEB/");
            body = body.replaceAll("http://10.247.16.133:8080/ASTUP_WEB//auth/logon_form.do", "http://localhost:8080/ASTUP_WEB//auth/logon_form.do");
            return body;
        } catch (IOException e) {
            e.getStackTrace();
        }
        return null;
    }

    public String PostRequest(String url, String payload) {
        Connection.Response response;
        try {
            System.out.println(this.cookies);
            System.out.println(payload);
            response = Jsoup.connect(url)
                    .method(Connection.Method.POST)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    //.header("Referer", "http://10.247.16.133:8080/ASTUP_WEB/page/astupMenu.jsp?command=refresh")
                    .cookies(convertStringToMap(this.cookies))
                    .data(convertStringToMap(payload))
                    .execute().bufferUp();
            String body = response.parse().html();
            //System.out.println(body);
            body = body.replaceAll("/ASTUP_WEB/", "http://10.247.16.133:8080/ASTUP_WEB/");
            body = body.replaceAll("\"http://10.247.16.133:8080/ASTUP_WEB/\"", "\"http://localhost:8080/ASTUP_WEB/\"");
            return body;
        } catch (IOException e) {
            e.getStackTrace();
        }
        return null;
    }

    public String getRequest(String url) {
        Connection.Response response = null;
        try {
            response = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .cookies(convertStringToMap(this.cookies))
                    .execute().bufferUp();
            String body = response.parse().html();
            this.cookies = convertWithStream(response.cookies());
            body = body.replaceAll("/ASTUP_WEB/", "http://10.247.16.133:8080/ASTUP_WEB/");
            body = body.replaceAll("\"http://10.247.16.133:8080/ASTUP_WEB/\"", "\"http://localhost:8080/ASTUP_WEB/\"");
            body = body.replaceAll("http://10.247.16.133:8080/ASTUP_WEB/page", "http://localhost:8080/ASTUP_WEB/page");
            body = body.replaceAll("http://localhost:8080/ASTUP_WEB/page/css", "http://10.247.16.133:8080/ASTUP_WEB/page/css");
            body = body.replaceAll("http://localhost:8080/ASTUP_WEB/page/images", "http://10.247.16.133:8080/ASTUP_WEB/page/images");
            return body;
        } catch (IOException e) {
            e.getStackTrace();
        }
        return null;
    }

    public String convertWithStream(Map<String, String> map) {
        String mapAsString = map.keySet().stream()
                .map(key -> key + "=" + map.get(key))
                .collect(Collectors.joining(", "));
        return mapAsString;
    }

    public Map<String, String> convertStringToMap(String string) {
        string = string.replace("+", "");
        Map<String, String> map = new HashMap<>();
        String[] array = string.split("&");
        for (int i = 0; i < array.length; i++) {
            String[] pair = array[i].split("=");
            map.put(pair[0], pair[1]);        }
        return map;
    }

}
