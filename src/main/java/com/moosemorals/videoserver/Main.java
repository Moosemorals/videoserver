package com.moosemorals.videoserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {

        MainHandler handler = new MainHandler();

        Server server = new Server(handler);

        handler.setServer(server.getServer());

        server.start();

    }

}

