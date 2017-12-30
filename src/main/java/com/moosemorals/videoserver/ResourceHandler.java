package com.moosemorals.videoserver;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

class ResourceHandler implements HttpRequestHandler{

    private final Logger log = LoggerFactory.getLogger(ResourceHandler.class);

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
        StringEntity body = new StringEntity(getResource("/index.html"));

        body.setContentType("text/html");

        httpResponse.setEntity(body);
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
