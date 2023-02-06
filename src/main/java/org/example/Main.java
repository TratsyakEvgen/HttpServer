package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {


    public static void main(String[] args) {
        String cookie = "";
        String[] requestHeaders = new String[0];
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new SocketProcessor(socket, cookie, requestHeaders)).start();
            }
        } catch (Throwable e) {
            System.out.println(e.getMessage().toCharArray());
        }
    }

    private static class SocketProcessor implements Runnable {

        private final Socket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private String cookie;
        private String[] requestHeaders;


        private SocketProcessor(Socket socket, String cookie, String[] requestHeaders) throws Throwable {
            this.socket = socket;
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
            this.cookie = cookie;
            this.requestHeaders = requestHeaders;
        }

        @Override
        public void run() {
            try {
                writeResponse(Objects.requireNonNull(readInputHeaders()));
            } catch (Throwable e) {
                System.out.println(e.getMessage().toCharArray());
            } finally {
                try {
                    socket.close();
                } catch (Throwable e) {
                    e.getStackTrace();
                }
            }
        }

        private void writeResponse(HttpResponse<InputStream> response) throws IOException {
            // BufferedReader bufferedReaderBody = new BufferedReader(new InputStreamReader(response.body()));
            // StringBuilder body = new StringBuilder();
            // String string;
            // int value;
            // while ((value = bufferedReaderBody.read()) != -1) {
            //    body.append((char) value);
            // }
            System.out.println(socket.getInetAddress());
            byte[] allBytes;
            InputStream fromIs = response.body();
            allBytes = fromIs.readAllBytes();

            String bodyS = new String(allBytes, "windows-1251");
            Pattern pattern = Pattern.compile("(/ASTUP_WEB[A-Za-z./]+jpg)|" +
                    "(/ASTUP_WEB[A-Za-z./]+svg)|" +
                    "(/ASTUP_WEB[A-Za-z./]+png)|" +
                    "(/ASTUP_WEB[A-Za-z./]+gif)");
            Matcher matcher = pattern.matcher(bodyS);

            StringBuilder newString = new StringBuilder();
            int start = 0;
            while (matcher.find()) {
                String localUrl = bodyS.substring(matcher.start(), matcher.end());
                //System.out.println(localUrl);
                int end = matcher.start();
                newString.append(bodyS, start, end).append("http://10.247.16.133:8080").append(localUrl);
                start = matcher.end();

            }
            newString.append(bodyS.substring(start));
            bodyS = String.valueOf(newString);


            //System.out.println(bodyS);
            //allBytes = bodyS.getBytes();
            //System.out.println(allBytes.length);
            HttpHeaders headers = response.headers();
            String responseHeaders = getStringResponseHeaders(headers.map().entrySet());
            //System.out.println(responseHeaders);
            ///System.out.println("11111");

            String status;
            if (response.statusCode() == 302) {
                status = " Found";
            } else {
                status = " OK";
            }

            //System.out.println();


            // responseHeaders = responseHeaders +
            //  "content-length: " + body.length();

            String allResponse = "HTTP/1.1 " + response.statusCode() + status + "\r\n" +
                    "content-length" + bodyS.length() +
                    "\r\n" +
                    cookie +
                    responseHeaders +
                    "\r\n\r\n" +
                    bodyS;

            outputStream.write(allResponse.getBytes("windows-1251"));//
            outputStream.flush();
        }

        private HttpResponse<InputStream> readInputHeaders() throws Throwable {
            String protocol = "http://";
            String host = "10.247.16.133:8080";

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String requestUrl = bufferedReader.readLine();
            String[] requestHeaders = getHeadersRequest(bufferedReader, host);

            if (requestUrl.contains("GET")) {
                return Parser.requestGet(protocol, host, requestUrl, requestHeaders);
            }

            if (requestUrl.contains("POST")) {
                StringBuilder postData = new StringBuilder();
                while (bufferedReader.ready()) {
                    postData.append((char) bufferedReader.read());
                }
                return Parser.requestPost(protocol, host, requestUrl, requestHeaders, String.valueOf(postData));
            }
            return null;
        }

        public String[] getHeadersRequest(BufferedReader bufferedReader, String host) {
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

        private String getStringResponseHeaders(Set<Map.Entry<String, List<String>>> set) {
            StringBuilder headers = new StringBuilder();
            for (Map.Entry<String, List<String>> s : set) {
                String key = s.getKey();
                if (cookie.equals("") & key.equals("set-cookie")) {
                    headers.append(key).append(": ");
                    for (String l : s.getValue()) {
                        headers.append(l).append("; ");
                    }
                    int size = headers.length();
                    if (size != 0) {
                        headers.replace(size - 2, size, "\r\n");
                        cookie = headers.toString();
                    }
                } else if (!key.equals("set-cookie") & !key.equals("content-length") & !key.equals("transfer-encoding")) {
                    headers.append(key).append(": ");
                    for (String l : s.getValue()) {
                        headers.append(l).append("; ");
                    }
                    int size = headers.length();
                    if (size != 0) {
                        headers.replace(size - 2, size, "\r\n");
                    }
                }
            }
            return String.valueOf(headers);
        }
    }
}