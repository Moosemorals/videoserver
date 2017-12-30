package com.moosemorals.videoserver;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoEntity implements HttpEntity, Runnable {

    private final Logger log = LoggerFactory.getLogger(VideoEntity.class);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Object lock = new Object();
    byte[] buff = new byte[4096 * 2];
    private int availableBytes = 0;

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public Header getContentType() {
        return new BasicHeader("Content-Type", "vldeo/webm");
    }

    @Override
    public Header getContentEncoding() {
        return null;
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not this puppy");
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {

        if (running.compareAndSet(false, true)) {
            new Thread(this, "Transcoder").start();
        }

        synchronized (lock) {
            while (running.get()) {
                while (availableBytes == 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException ex) {
                        log.warn("Unexpected interruption", ex);
                    }
                }

                if (availableBytes > 0) {
                    outputStream.write(buff, 0, availableBytes);
                    availableBytes = 0;
                } else {
                    outputStream.close();
                    log.debug("Closed output");
                    return;
                }
            }
        }
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

    @Override
    public void consumeContent() throws IOException {

    }

    @Override
    public void run() {
        try {
            log.debug("Starting process");
            Process ffmpeg = new ProcessBuilder(
                    "C:\\Users\\Osric\\Desktop\\ffmpeg-20171229-0c78b6a-win64-static\\bin\\ffmpeg.exe",
                    "-i", "C:\\Users\\Osric\\Desktop\\tg.mpg",
                    "-c:v", "libvpx-vp9",
                    "-crf", "31",
                    "-b:v", "0",
                //    "-deadline", "realtime",
                    "-f", "webm",
                    "pipe:1"
            ).start();

            InputStream in = ffmpeg.getInputStream();

            new Thread(() -> {
                try (BufferedReader err = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()))) {

                    String line;
                    while ((line = err.readLine()) != null) {
                        log.error("FFMPEG: {}", line);
                    }
                } catch (IOException ex) {
                    log.error("Problem reading error stream", ex);
                }
            }, "Errors").start();

            int r;
            while ((r = in.read(buff)) > 0) {
                synchronized (lock) {
                    availableBytes = r;
                    lock.notifyAll();
                }
            }

            log.debug("End of input");
            running.set(false);
            synchronized (lock) {
                availableBytes = -1;
                lock.notifyAll();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
