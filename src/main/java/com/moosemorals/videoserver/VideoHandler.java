package com.moosemorals.videoserver;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class VideoHandler implements HttpRequestHandler, Runnable {

    private final Logger log = LoggerFactory.getLogger(VideoHandler.class);

    private final Object lock = new Object();
    private int state = 0;

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
        log.debug("Handling request");

        new Thread(this, "Transcoder").start();

        // Wait for the process to start
        synchronized (lock) {
            while (state == 0) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new IOException("Unexpected interruption", e);
                }
            }
        }

        response.setStatusCode(302);
        response.setHeader("Location", "http://localhost:8081");
    }


    @Override
    public void run() {
        try {
            log.debug("Starting process");
            Process ffmpeg = new ProcessBuilder(
                    "C:\\Users\\Osric\\Desktop\\ffmpeg-20171229-0c78b6a-win64-static\\bin\\ffmpeg.exe",
                    // "-i", "C:\\Users\\Osric\\Desktop\\tg.mpg",
                    "-i", "\\\\PTAH\\big-media\\Video\\Doctor Who\\Doctor Who (2005)\\12th Doctor\\Doctor Who - 10x05 - Oxygen.ts",
                    "-c:v", "libvpx",
                    "-crf", "40",
                    "-b:v", "8M",
                    "-cpu-used", "15",
                    "-deadline", "realtime",
                    "-listen", "1",
                    "-f", "webm",
                    "http://localhost:8081"
            ).start();

            new Thread(() -> {
                try (BufferedReader err = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()))) {

                    String line;
                    while ((line = err.readLine()) != null) {
                        log.debug("FFMPEG: {}", line);
                    }
                } catch (IOException ex) {
                    log.error("Problem reading error stream", ex);
                }
            }, "Errors").start();

            try {
                synchronized (lock) {
                    state = 1;
                    lock.notifyAll();
                }
                ffmpeg.waitFor();
                log.debug("Transcode finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
