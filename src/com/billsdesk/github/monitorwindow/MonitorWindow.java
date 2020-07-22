package com.billsdesk.github.monitorwindow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Class for monitoring multiple thread pools. For each a bar graph will display number of threads,
 * queued tasks, and active tasks. Below a title is a count of total tasks submitted and completed.
 * Threshold lines are drawn at specified percentages showing yellow and red for warning and alert
 * when the count is high.
 * <p>
 * <b>Screenshot</b>
 * <p>
 * <img src="doc-files/MonitorWindow.jpg" width="100%" alt="MonitorWindow.jpg">
 *
 * @author Bill
 * @version $Rev: 8246 $ $Date: 2020-07-21 14:09:23 -0700 (Tue, 21 Jul 2020) $
 */
public class MonitorWindow
    extends
        JFrame {

    private static final long serialVersionUID = 1L;

    /**
     * A factory for creating a named executor.
     *
     * @param frame
     *            if not null then add this monitor to the Monitor Window
     * @param corePoolSize
     *            the number of threads to keep in the pool, even if they are idle, unless
     *            {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize
     *            the maximum number of threads to allow in the pool
     * @param name
     *            prefix name for each thread in pool
     * @return ThreadPoolExecutor
     */
    public static ThreadPoolExecutor getNamedExecutor(@Nullable final MonitorWindow frame,
                                                      final int corePoolSize,
                                                      final int maximumPoolSize,
                                                      final String name) {
        final ThreadPoolExecutor result = new ThreadPoolExecutor(corePoolSize,
                maximumPoolSize,
                500L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new NamedThreadFactory(name));
        if (frame != null) {
            frame.addMonitor(name, result);
        }
        return result;
    }

    private final ThreadPoolExecutor mExecutor;
    private long                     mUpdateFrequency = 100;
    private double                   mScale           = 1;
    private double                   mAlert           = -1;
    private double                   mWarning         = -1;

    private final Dimension          mEmptySize       = new Dimension(120, 200);

    /**
     * Create the window for the monitor. Forms a horizontal grid of individual monitors.
     */
    public MonitorWindow() {
        super("Monitor");
        setType(Window.Type.UTILITY);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(mEmptySize);
        toRightEdge();
        getContentPane().setLayout(new GridLayout(1, 0));

        mExecutor = getNamedExecutor(this, 1, 1, "Monitor");
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        final List<Monitor> components = //
                                Arrays.asList(getContentPane().getComponents())
                                      .stream()
                                      .map(c -> (Monitor) c)
                                      .collect(Collectors.toList());
                        components.stream().forEach(m -> {
                            if (m.isShutdown()) {
                                getContentPane().remove(m);
                                getContentPane().invalidate();
                                getContentPane().repaint();
                                if (getContentPane().getComponentCount() == 0) {
                                    setSize(MonitorWindow.this.mEmptySize);
                                } else {
                                    pack();
                                    toRightEdge();
                                }
                            } else {
                                m.update();
                            }
                        });
                        Thread.sleep(mUpdateFrequency);
                    }
                } catch (final InterruptedException error) {
                    // Ignore
                }
            }
        });

    }

    /**
     * Set options for all monitors.
     *
     * @param updateFrequency
     *            milliseconds between updates. Default 100ms.
     * @param scale
     *            scale the maximum to leave some space above the normally tallest bar, e.g. 1.5.
     *            Default is 1.0.
     * @param alert
     *            percentage above to be displayed red. e.g. 0.9. Default is -1 or no threshold.
     * @param warning
     *            percentage above to be displayed red. e.g. 0.8. Default is -1 or no threshold.
     * @return this
     */
    public MonitorWindow options(final long updateFrequency,
                                 final double scale,
                                 final double alert,
                                 final double warning) {
        mUpdateFrequency = updateFrequency;
        mScale = scale;
        mAlert = alert;
        mWarning = warning;
        return this;
    }

    public void close() {
        dispose();
    }

    @Override
    public void dispose() {
        mExecutor.shutdownNow();
        super.dispose();
    }

    /**
     * Add new monitor.
     *
     * @param name
     *            name to display
     * @param executor
     *            ThreadPoolExecutor to monitor
     */
    public void addMonitor(final String name, final ThreadPoolExecutor executor) {
        final Monitor monitor = new Monitor(name, executor, mScale, mAlert, mWarning);
        monitor.setPreferredSize(mEmptySize);
        getContentPane().add(monitor);
        pack();
        toRightEdge();
    }

    private void toRightEdge() {
        setLocation(GraphicsEnvironment.getLocalGraphicsEnvironment()
                                       .getDefaultScreenDevice()
                                       .getDefaultConfiguration()
                                       .getBounds().width
                    - getBounds().width,
                    getMenuBarHeight());
    }

    public static int getMenuBarHeight() {
        return 22;
    }

    private static class Monitor
        extends
            JPanel {

        private static final long serialVersionUID = 1L;

        private enum Metric {
            THREADS, QUEUED, ACTIVE
        };

        private final String             mName;
        private final ThreadPoolExecutor mExecutor;
        private final JStatusComponent   mStatus;
        private final JLabel             mCounts;

        public Monitor(final String name,
                       final ThreadPoolExecutor executor,
                       final double scale,
                       final double alert,
                       final double warning) {
            mName = name;
            mExecutor = executor;
            mStatus = new JStatusComponent(true, mName);
            mStatus.setGridLines(20);
            mStatus.setAdjustable(false);
            mStatus.setAxisMax(mExecutor.getCorePoolSize() * scale);
            mStatus.addValue(mExecutor.getCorePoolSize());
            mStatus.addValue(mExecutor.getQueue().size());
            mStatus.addValue(mExecutor.getActiveCount());
            if (alert != -1) {
                mStatus.getThreshold().alert().setValue(mStatus.getAxisMax() * alert);
            }
            if (warning != -1) {
                mStatus.getThreshold().warning().setValue(mStatus.getAxisMax() * warning);
            }
            mCounts = new JLabel();
            mCounts.setHorizontalAlignment(SwingConstants.CENTER);
            mCounts.setFont(mStatus.getFont());
            setLayout(new BorderLayout());
            add(mStatus, BorderLayout.CENTER);
            add(mCounts, BorderLayout.SOUTH);
        }

        public void update() {
            mStatus.setValueAt(mExecutor.getCorePoolSize(), Metric.THREADS.ordinal());
            mStatus.setValueAt(mExecutor.getQueue().size(), Metric.QUEUED.ordinal());
            mStatus.setValueAt(mExecutor.getActiveCount(), Metric.ACTIVE.ordinal());
            mCounts.setText(String.format("T: %,d C: %,d",
                                          mExecutor.getTaskCount(),
                                          mExecutor.getCompletedTaskCount()));
        }

        public boolean isShutdown() {
            return mExecutor.isShutdown() || mExecutor.isTerminated();
        }

        @Override
        public String toString() {
            return String.format("%s: %s", mName, mStatus.toString());
        }
    }

    public static class NamedThreadFactory
        implements
            ThreadFactory {

        private static final AtomicInteger    POOL_NUMBER  = new AtomicInteger(1);

        private final transient ThreadGroup   group;
        private final transient AtomicInteger threadNumber = new AtomicInteger(1);
        private final transient String        namePrefix;

        public NamedThreadFactory(final String name) {
            final SecurityManager security = System.getSecurityManager();
            group = (security == null) ? Thread.currentThread().getThreadGroup() // NOPMD
                                       : security.getThreadGroup();
            namePrefix = name + POOL_NUMBER.getAndIncrement() + '-';
        }

        @Override
        public Thread newThread(final Runnable runnable) {
            final Thread thread = new Thread(group,
                    runnable,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                thread.setPriority(Thread.NORM_PRIORITY);
            }
            return thread;
        }
    }
}
