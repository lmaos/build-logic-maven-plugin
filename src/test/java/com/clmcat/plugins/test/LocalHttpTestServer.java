package com.clmcat.plugins.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

final class LocalHttpTestServer implements AutoCloseable {

    static final int PORT = 18080;

    private final HttpServer server;

    private LocalHttpTestServer(HttpServer server) {
        this.server = server;
    }

    static LocalHttpTestServer start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
        server.createContext("/", LocalHttpTestServer::handleRequest);
        server.start();
        return new LocalHttpTestServer(server);
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        int status = 200;
        String body;
        if (path.endsWith("/success")) {
            body = "{\"message\":\"ok\"}";
        } else if (path.endsWith("/headers")) {
            body = "{\"message\":\"headers\"}";
        } else {
            status = 404;
            body = "{\"msg\":\"Not Found\",\"path\":\"" + path + "\"}";
        }

        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=UTF-8");
        exchange.sendResponseHeaders(status, bodyBytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bodyBytes);
        } finally {
            exchange.close();
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
