package org.omnifix.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Mirrors System.out/err into SLF4J while still writing to the original streams.
 */
public final class TracingPrintStream extends PrintStream {

    private final Logger logger;
    private final boolean error;

    private TracingPrintStream(OutputStream out, Logger logger, boolean error) {
        super(out, true, StandardCharsets.UTF_8);
        this.logger = logger;
        this.error = error;
    }

    public static void install() {
        System.setOut(new TracingPrintStream(System.out, LoggerFactory.getLogger("STDOUT"), false));
        System.setErr(new TracingPrintStream(System.err, LoggerFactory.getLogger("STDERR"), true));
        LogUtils.getLogger().info("[OmniFix] System.out/err mirrored into log4j (STDOUT/STDERR)");
    }

    @Override
    public void println(String x) {
        super.println(x);
        if (x != null && !x.isEmpty()) {
            if (error) {
                logger.warn(x);
            } else {
                logger.info(x);
            }
        }
    }

    @Override
    public void println(Object x) {
        println(String.valueOf(x));
    }
}
