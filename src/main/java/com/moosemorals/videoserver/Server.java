package com.moosemorals.videoserver;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;

public class Server {

    private final Undertow server;

    Server(HttpHandler handler) {
        server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(handler).build();
    }

    void start() {
        server.start();
    }

    void stop() {
        server.stop();
    }

    Undertow getServer() {
        return server;
    }

}
