package org.example;


import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.Charset;


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
                AstupService astupService = new AstupService();
                writeResponse(readInputHeaders(astupService), astupService);
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

        private void writeResponse(String body, AstupService astupService) throws Throwable {
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html;charset=windows-1251\r\n" +
                    "Set-Cookie: " + astupService.getCookies() + "\r\n" +
                    "Content-Length: " + body.length() + "\r\n\r\n";
            String result = response + body;
            outputStream.write(result.getBytes("windows-1251"));
            outputStream.flush();
        }

        private String readInputHeaders(AstupService astupService) throws Throwable {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String requestData = bufferedReader.readLine();
            System.out.println(requestData);
            String body = null;

            if (requestData.equals("GET / HTTP/1.1")) {
                body = astupService.authorize();
            }

            if (requestData.contains("GET /ASTUP_WEB/")) {
                String cookies = null;
                String string;

                while ((string = bufferedReader.readLine()).length() != 0) {

                    if (string.contains("Cookie")) {
                        cookies = string.replace("Cookie: ", "");
                        astupService.setCookies(cookies);
                    }
                }
                requestData = requestData.replace("GET /ASTUP_WEB/", "http://10.247.16.133:8080/ASTUP_WEB/");
                requestData = requestData.replace(" HTTP/1.1", "");
                astupService.setCookies(cookies);
                body = astupService.getRequest(requestData);
            }

            if (requestData.contains("POST /ASTUP_WEB/")) {
                String cookies = null;
                String string;
                String requestHeaders = requestData;
                while ((string = bufferedReader.readLine()).length() != 0) {
                    requestHeaders = requestHeaders + string;
                    if (string.contains("Cookie")) {
                        cookies = string.replace("Cookie: ", "");
                        astupService.setCookies(cookies);
                    }
                }
                StringBuilder payload = new StringBuilder();
                while (bufferedReader.ready()) {
                    payload.append((char) bufferedReader.read());
                }
                requestData = requestData.replace("POST /ASTUP_WEB/", "http://10.247.16.133:8080/ASTUP_WEB/");
                requestData = requestData.replace(" HTTP/1.1", "");
                astupService.setCookies(cookies);
                System.out.println(requestHeaders);
                body = astupService.PostRequest(requestData, String.valueOf(payload));
            }
            return body;

        }
    }
}