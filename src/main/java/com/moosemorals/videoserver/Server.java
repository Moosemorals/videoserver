package com.moosemorals.videoserver;

import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Server {

    private final Logger log = LoggerFactory.getLogger(Server.class);

    private final HttpServer server;

    Server() {

        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer("MyServer-HTTP/1.1"))
                .add(new ResponseContent())
                .add(new ResponseConnControl())
                .build();

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(15000)
                .setTcpNoDelay(true)
                .build();

        server = ServerBootstrap.bootstrap()
                .setListenerPort(8080)
                .setHttpProcessor(httpproc)
                .setSocketConfig(socketConfig)
                .setExceptionLogger(ex -> log.debug("Server exception", ex))
                .registerHandler("/", new ResourceHandler())
                .registerHandler("/video/*", new VideoHandler())
                .create();
    }

    void start() throws InterruptedException, IOException {
        server.start();
        server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    void stop() {
        server.stop();
    }

}
