package com.billsdesk.github.monitorwindow;

import java.util.Random;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @author Bill
 * @version $Rev: 8246 $ $Date: 2020-07-21 14:09:23 -0700 (Tue, 21 Jul 2020) $
 */
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class MonitorWindowTest {

    @Test
    public void display() {
        final MonitorWindow monitor = new MonitorWindow().options(100, 1.5, 0.9, 0.8);
        final ThreadPoolExecutor m1 = MonitorWindow.getNamedExecutor(monitor, 15, 15, "M1");
        final ThreadPoolExecutor m2 = MonitorWindow.getNamedExecutor(monitor, 5, 5, "M2");
        monitor.setVisible(true);

        monitor.addMonitor("M1", m1);
        monitor.addMonitor("M2", m2);

        while (monitor.isVisible()) {
            if (m1.getActiveCount() < 5) {
                final int count = RandomIndex.getInclusive(20, 50);
                for (int i = 0; i < count; i++) {
                    try {
                        m1.submit(new Task(RandomIndex.getInclusive(2, 10)));
                    } catch (final RejectedExecutionException error) {
                    }
                }
            }
            if (m2.getActiveCount() < 3) {
                final int count = RandomIndex.getInclusive(5, 10);
                for (int i = 0; i < count; i++) {
                    try {
                        m2.submit(new Task(RandomIndex.getInclusive(2, 3)));
                    } catch (final RejectedExecutionException error) {
                    }
                }
            }
            pause(1000);
        }
    }

    public class Task
        implements
            Runnable {

        private final int mSeconds;

        public Task(final int seconds) {
            mSeconds = seconds;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(mSeconds * 1000);
            } catch (final InterruptedException error) {
                System.out.println("Interrupted");
            }
        }
    }

    public static final class RandomIndex {

        private static final Random GENERATOR = new Random();

        public static int getInclusive(final int min, final int max) {
            return GENERATOR.nextInt(max - min + 1) + min;
        }

        private RandomIndex() {
        }
    }

    public static void pause(final long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (final InterruptedException error) {
            // Ignore
        }
    }
}
