package org.example.service;

import org.example.entity.User;
import org.example.util.ConvertDataUtil;
import org.example.util.HtmlUtil;
import org.example.util.ParserUtil;

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

        StringBuilder stringResponse = new StringBuilder("HTTP/1.1 ");
        stringResponse.append(statusCode).append(" ").append(stringStatus).append("\r\n")
                .append("content-length: ").append(stringBody.length()).append("\r\n")
                .append(user.getCookie())
                .append(responseHeaders).append("\r\n")
                .append("\r\n")
                .append(stringBody);

        outputStream.write(String.valueOf(stringResponse).getBytes(charsetName));
        outputStream.flush();
    }



    private HttpResponse<InputStream> executeRequest() throws Throwable {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String general = bufferedReader.readLine();

        String[] requestHeaders = getHeadersRequest(bufferedReader);

        if (general.contains("GET")) {
            return new ParserUtil().executeGetRequest(general, requestHeaders);
        }

        if (general.contains("POST")) {
            return new ParserUtil().executePostRequest(general,
                    requestHeaders,
                    ConvertDataUtil.getBufferReaderToString(bufferedReader));
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

