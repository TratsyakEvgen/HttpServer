package org.example.service;

import org.example.entity.User;
import org.example.util.ConvertDataUtil;
import org.example.util.HtmlUtil;

import java.io.*;
import java.net.Socket;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.*;

public class SocketService implements Runnable {
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final User user;

    public SocketService(Socket socket, User user) throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        this.user = user;
    }


    @Override
    public void run() {
        try {
            sendResponse(Objects.requireNonNull(executeRequest()));
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    private void sendResponse(HttpResponse<InputStream> response) throws IOException {
        String charsetName = "windows-1251";

        String stringBody = ConvertDataUtil.getInputStreamToString(response.body(), charsetName);

        stringBody = HtmlUtil.replaceLinksForImage(stringBody,
                "/ASTUP_WEB/[A-Za-z./]+",
                "http://10.247.16.133:8080");

        HttpHeaders httpHeaders = response.headers();
        String responseHeaders = getResponseHeaders(httpHeaders.map().entrySet());

        int statusCode = response.statusCode();
        String stringStatus;
        if (statusCode == 302) {
            stringStatus = " Found";
        } else {
            stringStatus = " OK";
        }

        String stringResponse = "HTTP/1.1 " + statusCode + " " + stringStatus + "\r\n" +
                user.getCookie() +
                responseHeaders + "\r\n" +
                "\r\n" +
                stringBody;
        outputStream.write(stringResponse.getBytes(charsetName));
        outputStream.flush();
    }


    private HttpResponse<InputStream> executeRequest() throws Throwable {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String general = bufferedReader.readLine();
        String url = getUrl(general);
        String[] requestHeaders = getHeadersRequest(bufferedReader);


        if (general.contains("GET")) {

            return new ParserService(url, requestHeaders).executeGetRequest();
        }

        if (general.contains("POST")) {
            String postData = ConvertDataUtil.getBufferReaderToString(bufferedReader);
            postData = postData.replace("+", " ");
            return new ParserService(url, requestHeaders, postData).executePostRequest();
        }
        return null;
    }


    private String[] getHeadersRequest(BufferedReader bufferedReader) {
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
            String string = ConvertDataUtil.getMapEntryToString(entry);
            if (!key.equals("content-length") & !key.equals("transfer-encoding")) {
                if (key.equals("set-cookie")) {
                    if (user.getCookie().equals("")) {
                        user.setCookie(string);
                    }
                } else {
                    headers.append(string);
                }
            }
        }
        return String.valueOf(headers);
    }

    private String getUrl(String requestUrl) {
        requestUrl = requestUrl.replace("GET ", "");
        requestUrl = requestUrl.replace("POST ", "");
        requestUrl = requestUrl.replace(" HTTP/1.1", "");
        if (requestUrl.equals("/")) {
            requestUrl = "/ASTUP_WEB/";
        }
        return "http://10.247.16.133:8080" + requestUrl;
    }


}

