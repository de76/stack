package com.sufnom;

import com.sufnom.stack.StackRequest;
import com.sufnom.sys.Config;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Base64;

@SuppressWarnings("WeakerAccess")
public class Main {
    public static final boolean debug = true;
    public static final int PORT =
            Integer.parseInt(Config.getSession().getValue(Config.KEY_STACK_PORT));

    public static void main(String[] args)  {
        try {
            System.out.println("Stack Path : " + Config.getSession().getValue(Config.KEY_STACK_PATH));
            onStart();
        }
        catch (Exception e){e.printStackTrace();}
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    static void onStart() throws Exception{
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        HttpContext context = server.createContext("/", new DefaultHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Listening to " + PORT);
    }

    static class DefaultHandler implements HttpHandler {
        public void handle(HttpExchange t){
            try {
                String encoded_request = t.getRequestURI().getPath();
                byte[] headerBytes = Base64.getDecoder().decode(
                        URLDecoder.decode(encoded_request,"utf-8"));
                StackRequest request = new StackRequest(headerBytes);
                request.processRequest();
                sendResponse(t, 200, request.getResponse());
            }
            catch (Exception e){
                e.printStackTrace();
                sendResponse(t, 200, "error".getBytes());
            }
        }

        @SuppressWarnings("SameParameterValue")
        private void sendResponse(HttpExchange t, int status, byte[] rawResponse){
            try {
                if (debug) System.out.println(status + " : length=" + rawResponse.length);
                t.getResponseHeaders().set("Content-Type","application/octet-stream");
                t.sendResponseHeaders(status, rawResponse.length);
                OutputStream os = t.getResponseBody();
                os.write(rawResponse);
                os.close();
            }
            catch (Exception e){e.printStackTrace();}
        }
    }
}
