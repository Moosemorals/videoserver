package com.moosemorals.videoserver;

import io.undertow.Undertow;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;

public class MainHandler implements HttpHandler {

    private final Logger log = LoggerFactory.getLogger(MainHandler.class);

    private Undertow server;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String path = exchange.getRequestPath();

        if ("/favicon.ico".equals(path)) {
            exchange.setResponseCode(404);
            return;
        } else if ("/shutdown".equals(path)) {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseSender().send("Shutting down");

            server.stop();
        } else if ("/".equals(path)) {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html;charset=utf-8");
            exchange.getResponseSender().send(getResource("/index.html"));
        } else if (path.startsWith("/video/")) {
            sendVideo(exchange, path);
        }
    }

    private void sendVideo(HttpServerExchange exchange, String path) throws FileNotFoundException {



        

        File f = new File("C:\\Users\\Osric\\Desktop\\tg.webm");

        log.debug("Check file exists {}", f.canRead());

        FileInputStream stream = new FileInputStream(f);

        FileChannel channel = stream.getChannel();


        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "video/webm");
        exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, f.length());
        exchange.getResponseSender().transferFrom(channel, new IoCallback() {
            @Override
            public void onComplete(HttpServerExchange httpServerExchange, Sender sender) {
                log.debug("done");
            }

            @Override
            public void onException(HttpServerExchange httpServerExchange, Sender sender, IOException e) {
                log.debug("error", e);
            }
        });


    }

    void setServer(Undertow server) {
        this.server = server;
    }

    private String getResource(String name) throws IOException {
        StringBuilder result = new StringBuilder();

        try (Reader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(name), "utf-8"))) {
            char[] buf = new char[8192];
            int r;

            while ((r = in.read(buf)) > 0) {
                log.debug("Read {}", r);
                result.append(buf, 0, r);
            }
        }

        return result.toString();
    }
}
