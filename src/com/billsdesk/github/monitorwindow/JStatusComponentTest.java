package com.billsdesk.github.monitorwindow;

import java.awt.GridLayout;

import javax.swing.JFrame;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.billsdesk.github.monitorwindow.JStatusComponent.ThresholdData;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class JStatusComponentTest {

    @Test
    public void horizontal() {
        final ThresholdData threshold = new ThresholdData();
        threshold.alert().setValue(50.0);
        final JStatusComponent status1 = new JStatusComponent(false, "Book Title 1");
        status1.setThreshold(threshold);
        status1.addValue(60);
        status1.addValue(25);
        status1.addValue(25);

        final JStatusComponent status2 = new JStatusComponent(false, "Book Title 2");
        status2.setThreshold(threshold);
        status2.addValue(60);
        status2.addValue(25);

        final JStatusComponent status3 = new JStatusComponent(false, "Book Title 3");
        status3.setThreshold(threshold);
        status3.addValue(60);
        status3.addValue(25);

        final JFrame frame = new JFrame("JStatusComponent");
        frame.setSize(500, 500);
        frame.getContentPane().setLayout(new GridLayout(0, 1));
        frame.getContentPane().add(status1);
        frame.getContentPane().add(status2);
        frame.getContentPane().add(status3);
        frame.setVisible(true);
        while (frame.isVisible()) {
            pause(200);
        }
        frame.dispose();
    }

    @Test
    public void vertical() {
        final ThresholdData threshold = new ThresholdData();
        threshold.alert().setValue(50.0);
        final JStatusComponent status1 = new JStatusComponent(true, "Book Title 1");
        status1.setThreshold(threshold);
        status1.addValue(60);
        status1.addValue(25);
        status1.addValue(25);

        final JStatusComponent status2 = new JStatusComponent(true, "Book Title 2");
        status2.setThreshold(threshold);
        status2.addValue(60);
        status2.addValue(25);

        final JStatusComponent status3 = new JStatusComponent(true, "Book Title 3");
        status3.setThreshold(threshold);
        status3.addValue(60);
        status3.addValue(25);

        final JFrame frame = new JFrame("JStatusComponent");
        frame.setSize(500, 500);
        frame.getContentPane().setLayout(new GridLayout(0, 1));
        frame.getContentPane().add(status1);
        frame.getContentPane().add(status2);
        frame.getContentPane().add(status3);
        frame.setVisible(true);
        while (frame.isVisible()) {
            pause(200);
        }
        frame.dispose();
    }

    public static void pause(final long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (final InterruptedException error) {
            // Ignore
        }
    }
}
