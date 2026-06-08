package com.pelajtech.marketi;

import com.pelajtech.marketi.log.Logging;

public class Marketi {

    static void main() {
        var marketi = new Marketi();
        marketi.start();

        Runtime.getRuntime().addShutdownHook(new Thread(marketi::shutdown));
    }

    public void start() {
        Logging.LOG.info("Starting Marketi.");
    }

    public void shutdown() {
        Logging.LOG.info("Marketi shutting down.");
    }

}
