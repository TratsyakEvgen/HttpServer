package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpResponse;
import java.util.*;


public class Main {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new SocketProcessor(socket)).start();
            }
        } catch (Throwable e){
            System.out.println(e.getMessage().toCharArray());
        }
    }

    private static class SocketProcessor implements Runnable {

        private final Socket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;


        private SocketProcessor(Socket socket) throws Throwable {
            this.socket = socket;
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
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

        private void writeResponse(HttpResponse<String> response) throws IOException {
            String body = response.body();
            String allResponse = "HTTP/1.1 200 OK\r\n" +
                    "Content-Length: " + response.body().length() +
                    "\r\n" +
                    getStringResponseHeaders(response) +
                    "\r\n" +
                    body;
            outputStream.write(allResponse.getBytes("windows-1251"));//
            outputStream.flush();
        }

        private HttpResponse<String> readInputHeaders() throws Throwable {
            String protocol = "http://";
            String host = "10.247.16.133:8080";

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String requestUrl = bufferedReader.readLine();
            System.out.println(requestUrl);
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
                    if (string.contains("Cookie: ") ||
                            string.contains("Accept: ")) {
                        System.out.println(string);
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

        private String getStringResponseHeaders(HttpResponse<String> response) {
            Set<Map.Entry<String, List<String>>> set = response.headers().map().entrySet();
            StringBuilder headers = new StringBuilder();
            for (Map.Entry<String, List<String>> s : set) {
                String key = s.getKey();
                if (key.equals("Set-Cookie") ||
                        key.equals("content-type") ||
                        key.equals("Content-Type") ||
                        key.equals("ETag") ||
                        key.equals("Last-Modified") ||
                        key.equals("Server")) {
                    headers.append(key).append(": ");
                    for (String l : s.getValue()) {
                        headers.append(l).append("; ");
                    }
                }
                int size = headers.length();
                if (size != 0){
                    headers.replace(size - 2, size, "\r\n");
                }
            }
            System.out.println(headers);
            return String.valueOf(headers);
        }
    }
}