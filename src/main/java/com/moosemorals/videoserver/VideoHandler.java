package com.moosemorals.videoserver;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class VideoHandler implements HttpRequestHandler {

    private final Logger log = LoggerFactory.getLogger(VideoHandler.class);

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
        log.debug("Handling request");

        VideoEntity body = new VideoEntity();

        response.setStatusCode(200);
        response.setEntity(body);

    }

}
