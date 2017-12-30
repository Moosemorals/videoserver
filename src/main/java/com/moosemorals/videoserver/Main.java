package com.moosemorals.videoserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) throws IOException, InterruptedException {

        Server server = new Server();
        server.start();

    }

}

