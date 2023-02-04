package org.example;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class Main {

    public static void main(String[] args) throws Throwable {
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new SocketProcessor(socket)).start();

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
                writeResponse(readInputHeaders());
            } catch (Throwable e) {
                e.getStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (Throwable e) {
                    e.getStackTrace();
                }
            }
        }

        private void writeResponse(Map<String, String> responseDocument) throws IOException {
            String responseBody = null;
            String responseHeaders = null;
            for (Map.Entry<String, String> entry : responseDocument.entrySet()) {
                responseHeaders = entry.getKey();
                responseBody = entry.getValue();
            }
            String response = "HTTPS/1.1 200 OK\r\n" +
                    "Content-Length: " + responseBody.length() +
                    "\r\n" +
                    responseHeaders +
                    "\r\n\r\n" +
                    responseBody;
            System.out.println(responseHeaders);
            outputStream.write(response.getBytes());//"windows-1251"
            outputStream.flush();
        }

        private Map<String, String> readInputHeaders() throws Throwable {
            String protocol = "https://";
            String host = "qna.habr.com";

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String requestUrl = bufferedReader.readLine();
            String stringRequestHeaders = getHeadersRequest(bufferedReader);
            Map<String, String> mapRequestHeaders = convertStringToMap(stringRequestHeaders, ": ");
            mapRequestHeaders.replace("Host", host);


            if (requestUrl.contains("GET")) {
                return Parser.requestGet(protocol, host, requestUrl, mapRequestHeaders);
            }

            if (requestUrl.contains("POST")) {
                StringBuilder stringPostData = new StringBuilder();
                while (bufferedReader.ready()) {
                    stringPostData.append((char) bufferedReader.read());
                }
                Map<String, String> postData = convertStringToMap(stringPostData.toString(), "=");
                return Parser.requestPost(protocol, host, requestUrl, mapRequestHeaders, postData);
            }
            return null;
        }

        public String getHeadersRequest(BufferedReader bufferedReader) {
            StringBuilder requestData = new StringBuilder();
            try {
                requestData.append(bufferedReader.readLine());
                String string;
                while ((string = bufferedReader.readLine()).length() != 0) {
                    requestData
                            .append("&")
                            .append(string);
                }
            } catch (IOException e) {
                e.getStackTrace();
            }
            return requestData.toString();
        }

        public Map<String, String> convertStringToMap(String string, String regex) {
            string = string.replace("+", "");
            Map<String, String> map = new HashMap<>();
            String[] array = string.split("&");
            for (String s : array) {
                String[] pair = s.split(regex);
                map.put(pair[0], pair[1]);
            }
            return map;
        }
    }
}