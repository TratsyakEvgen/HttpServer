package org.example.service;

import org.example.Parser;
import org.example.entity.EntitySocket;
import org.example.entity.User;
import org.example.repository.SocketRepository;
import org.example.util.ConvertDataUtil;

import java.io.*;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocketService implements Runnable {
    private final EntitySocket entitySocket;
    private final User user;

    public SocketService(EntitySocket entitySocket) throws Throwable {
        this.entitySocket = new SocketRepository(entitySocket).getEntitySocket();
        this.user = entitySocket.getUser();
    }

    @Override
    public void run() {
        try {
            writeResponse(Objects.requireNonNull(readInputHeaders()));
        } catch (Throwable e) {
            System.out.println(e.getMessage().toCharArray());
        } finally {
            try {
                entitySocket.getSocket().close();
            } catch (Throwable e) {
                e.getStackTrace();
            }
        }
    }

    private void writeResponse(HttpResponse<InputStream> response) throws IOException {
        byte[] byteBody;
        InputStream inputStreamBody = response.body();
        byteBody = inputStreamBody.readAllBytes();

        String stringBody = new String(byteBody, "windows-1251");
        Pattern pattern = Pattern.compile("(/ASTUP_WEB[A-Za-z./]+jpg)|" +
                "(/ASTUP_WEB[A-Za-z./]+svg)|" +
                "(/ASTUP_WEB[A-Za-z./]+png)|" +
                "(/ASTUP_WEB[A-Za-z./]+gif)");
        Matcher matcher = pattern.matcher(stringBody);

        StringBuilder newStringBody = new StringBuilder();
        int start = 0;
        while (matcher.find()) {
            String localUrl = stringBody.substring(matcher.start(), matcher.end());
            int end = matcher.start();
            newStringBody.append(stringBody, start, end).append("http://10.247.16.133:8080").append(localUrl);
            start = matcher.end();

        }
        newStringBody.append(stringBody.substring(start));
        stringBody = String.valueOf(newStringBody);


        HttpHeaders headers = response.headers();
        String responseHeaders = getResponseHeaders(headers.map().entrySet());


        String status;
        if (response.statusCode() == 302) {
            status = " Found";
        } else {
            status = " OK";
        }


        String stringResponse = "HTTP/1.1 " + response.statusCode() + status + "\r\n" +
                "content-length" + stringBody.length() +
                "\r\n" +
                user.getCookie() +
                responseHeaders +
                "\r\n\r\n" +
                stringBody;

        entitySocket.getOutputStream().write(stringResponse.getBytes("windows-1251"));//
        entitySocket.getOutputStream().flush();
    }

    private HttpResponse<InputStream> readInputHeaders() throws Throwable {
        String host = "http://10.247.16.133:8080";

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entitySocket.getInputStream()));
        String requestUrl = bufferedReader.readLine();
        String[] requestHeaders = getHeadersRequest(bufferedReader);

        if (requestUrl.contains("GET")) {
            return Parser.requestGet(host, requestUrl, requestHeaders);
        }

        if (requestUrl.contains("POST")) {
            StringBuilder postData = new StringBuilder();
            while (bufferedReader.ready()) {
                postData.append((char) bufferedReader.read());
            }
            return Parser.requestPost(host, requestUrl, requestHeaders, String.valueOf(postData));
        }
        return null;
    }

    public String[] getHeadersRequest(BufferedReader bufferedReader) {
        List<String> requestData = new ArrayList<>();
        try {
            String string;
            while ((string = bufferedReader.readLine()).length() != 0) {
                if (string.contains("Content-Type: ") | string.contains("Cookie: ")) {
                    String[] pair = string.split(": ");
                    requestData.add(pair[0]);
                    requestData.add(pair[1]);
                }
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
        return requestData.toArray(new String[0]);
    }

    private String getResponseHeaders(Set<Map.Entry<String, List<String>>> set) {
        StringBuilder headers = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : set) {
            String key = entry.getKey();
            String string = ConvertDataUtil.convertMapEntryToString(entry);
            if (!key.equals("content-length") & !key.equals("transfer-encoding")) {
                if (key.equals("set-cookie")) {
                    if (user.getCookie().equals("")) {
                        headers.append(string);
                        user.setCookie(string);
                    }
                } else {
                    headers.append(string);
                }
            }
        }
        return String.valueOf(headers);
    }

}

