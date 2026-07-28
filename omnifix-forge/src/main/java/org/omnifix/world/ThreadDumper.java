package org.omnifix.world;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public final class ThreadDumper {

    private ThreadDumper() {}

    public static String obtainThreadDump() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] infos = bean.dumpAllThreads(true, true);
        StringBuilder sb = new StringBuilder(4096);
        for (ThreadInfo info : infos) {
            sb.append(info.toString());
            sb.append('\n');
        }
        return sb.toString();
    }
}
