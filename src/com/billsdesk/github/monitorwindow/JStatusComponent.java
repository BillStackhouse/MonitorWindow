package com.billsdesk.github.monitorwindow;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Display a series of threshold bars with threshold lines.
 * <p>
 * <b>Screenshot</b>
 * <p>
 * <img src="doc-files/JStatusComponent.jpg" width="100%" alt="JStatusComponent.jpg">
 * </p>
 *
 * @author Bill
 * @version $Rev: 8246 $ $Date: 2020-07-21 14:09:23 -0700 (Tue, 21 Jul 2020) $
 */
public class JStatusComponent
    extends
        JButton {

    private static final long  serialVersionUID = 1L;

    private boolean            mVertical        = false;
    private double             mMaxAxisValue;
    private boolean            mAdjustable      = true;
    private Color              mBarBackground;
    private Color              mBarColor;
    private ThresholdData      mThreshold;
    private int                mGridLines       = -1;
    private final List<Double> mBars            = new ArrayList<Double>();

    /**
     * Create component with title.
     *
     * @param vertical
     *            true then draw bar vertically
     * @param title
     *            title for status
     */
    public JStatusComponent(final boolean vertical, final String title) {
        super(title);
        mVertical = vertical;
        mThreshold = new ThresholdData();
        super.setActionCommand(title);
        setBarBackground(Color.WHITE);
        setBarColor(Color.DARK_GRAY);
        setFont(new Font("SansSerif", Font.BOLD, 12));
        setOpaque(false);
        setUI(mVertical ? new StatusVerticalUI() : new StatusHorizontalUI());
    }

    @Override
    public void updateUI() {
        setUI(mVertical ? new StatusVerticalUI() : new StatusHorizontalUI());
    }

    /**
     * Set the max value to draw. If a bar value is greater then the bar will stop at the top of the
     * area.
     *
     * @param value
     *            value
     */
    public void setAxisMax(final double value) {
        mMaxAxisValue = value;
        adjustScale((int) value);
    }

    public double getAxisMax() {
        return mMaxAxisValue;
    }

    /**
     * @param value
     *            true then adjust the axis max value based on the new value of the bar.
     */
    public void setAdjustable(final boolean value) {
        mAdjustable = value;
    }

    public boolean isAdjustable() {
        return mAdjustable;
    }

    /**
     * @return number of bars in component.
     */
    public int getBarCount() {
        return mBars.size();
    }

    /**
     * Get value for a bar.
     *
     * @param index
     *            index of bar
     * @return value
     * @throws IndexOutOfBoundsException
     *             invalid index
     */
    public double getValueAt(final int index) throws IndexOutOfBoundsException {
        return mBars.get(index);
    }

    /**
     * Set the value for a bar. Bar must exist.
     *
     * @param value
     *            value
     * @param index
     *            index of bar
     * @throws IndexOutOfBoundsException
     *             invalid index
     */
    public void setValueAt(final double value, final int index) throws IndexOutOfBoundsException {
        if (index < mBars.size()) {
            mBars.set(index, value);
            if (value > 0) {
                adjustScale((int) value);
            }
            setToolTipText(toString());
            repaint();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Add a new bar to component. Bars are added in order.
     *
     * @param value
     *            value
     */
    public void addValue(final double value) {
        mBars.add(value);
        if (value > 0) {
            adjustScale((int) value);
        }
        setToolTipText(toString());
        repaint();
    }

    public void setThreshold(final ThresholdData threshold) {
        mThreshold = threshold;
        repaint();
    }

    public ThresholdData getThreshold() {
        return mThreshold;
    }

    /**
     * Draw lines ever multiple of the vale, e.g. 10 would draw the lines aver ten units.
     *
     * @param value
     *            the spacing of the lines
     */
    public void setGridLines(final int value) {
        mGridLines = value;
    }

    /**
     * Set the color of the background.
     *
     * @param color
     *            color. Default is white
     */
    public final void setBarBackground(final Color color) {
        mBarBackground = color;
        repaint();
    }

    /**
     * Set the color of the bar.
     *
     * @param color
     *            color. Default is dark gray
     */
    public final void setBarColor(final Color color) {
        mBarColor = color;
        repaint();
    }

    protected void adjustScale(final int value) {
        final int max = (int) getAxisMax();
        if (mAdjustable && value > max) {
            setAxisMax(adjustValue(value));
        }
    }

    private int adjustValue(final int value) {
        return ((value / 100) + (value % 100 == 0 ? 0 : 1)) * 100;
    }

    @Override
    public String toString() {
        return String.format("%s %s",
                             getText(),
                             mBars.stream()
                                  .map((number) -> String.format("%,.2f", number))
                                  .collect(Collectors.joining(", ")));
    }

    private static class StatusVerticalUI
        extends
            BasicButtonUI {

        @Override
        public void paint(final Graphics oldGraphics,
                          final JComponent component) throws IllegalArgumentException {
            final Graphics2D graphics = (Graphics2D) oldGraphics.create();
            try {
                final JStatusComponent status = (JStatusComponent) component;
                // layout
                final FontMetrics metrics = graphics.getFontMetrics();
                final int topOfText = component.getHeight() - metrics.getHeight() - 3;
                final Rectangle textRect = new Rectangle(2,
                        topOfText,
                        component.getWidth() - 4,
                        metrics.getHeight());
                final Rectangle barRect = new Rectangle(5,
                        5,
                        component.getWidth() - 10,
                        component.getHeight() - textRect.height - 10);
                // paint
                graphics.setColor(status.getBackground());
                graphics.fillRect(0, 0, component.getWidth(), component.getHeight());
                paintGraph(graphics, status, barRect);
                paintText(graphics, status, textRect, status.getText(), Color.BLACK);
            } catch (final Exception error) {
                throw new IllegalArgumentException(error);
            } finally {
                graphics.dispose();
            }
        }

        private void paintGraph(final Graphics oldGraphics,
                                final JStatusComponent status,
                                final Rectangle barRect) {
            final Graphics2D graphics = (Graphics2D) oldGraphics.create();
            graphics.setClip(barRect.x, barRect.y, barRect.width, barRect.height);
            try {
                final Rectangle barArea = new Rectangle(barRect);
                final int individualBarWidth = status.getBarCount() == 0 ? barArea.width
                                                                         : barArea.width
                                                                           / status.getBarCount();
                barArea.width = individualBarWidth * status.getBarCount();
                // draw bar area background draw box around it
                graphics.setColor(status.mBarBackground);
                graphics.fillRect(barArea.x, barArea.y, barArea.width, barArea.height + 1);
                graphics.setColor(Color.DARK_GRAY);
                graphics.drawRect(barArea.x, barArea.y, barArea.width, barArea.height + 1);
                // draw each bar
                barArea.setBounds(barArea.x + 1,
                                  barArea.y + 1,
                                  barArea.width - 2,
                                  barArea.height - 2);
                int individualBarLeft = 0;
                for (int i = 0; i < status.getBarCount(); i++) {
                    graphics.setColor(status.getThreshold() == null ? status.mBarColor
                                                                    : status.getThreshold()
                                                                            .getColor(status.getValueAt(i)));
                    final Rectangle bar = new Rectangle(barArea.x + individualBarLeft + 5,
                            barTop(status, barArea, status.getValueAt(i)),
                            individualBarWidth - 10,
                            barHeight(status, barArea, status.getValueAt(i)));
                    final Rectangle value = new Rectangle(bar.x, bar.y, bar.width, 15);
                    graphics.fillRect(bar.x, bar.y, bar.width, bar.height);
                    paintText(graphics,
                              status,
                              value,
                              Integer.toString((int) status.getValueAt(i)),
                              status.getThreshold().getColorReversed(status.getValueAt(i)));
                    individualBarLeft += individualBarWidth;
                }
                // draw threshold lines
                if (status.mThreshold != null) {
                    drawThresholdLine(graphics,
                                      status,
                                      barArea,
                                      status.mThreshold.crisis().getValue());
                    drawThresholdLine(graphics,
                                      status,
                                      barArea,
                                      status.mThreshold.alert().getValue());
                    drawThresholdLine(graphics,
                                      status,
                                      barArea,
                                      status.mThreshold.warning().getValue());
                } else if (status.mGridLines != -1) {
                    for (int i = 0; i < status.getAxisMax(); i += status.mGridLines) {
                        graphics.drawLine(barArea.x,
                                          barTop(status, barArea, i),
                                          barArea.x + barArea.width,
                                          barTop(status, barArea, i));
                    }
                }
            } finally {
                graphics.dispose();
            }
        }

        private void paintText(final Graphics oldGraphics,
                               final JStatusComponent status,
                               final Rectangle textRect,
                               final String text,
                               final Color color) {
            final Graphics2D graphics = (Graphics2D) oldGraphics.create();
            try {
                if (text != null && !text.equals("")) {
                    //graphics.setFont(UIManager.getFont("SmallLabel.font"));
                    final FontMetrics metrics = graphics.getFontMetrics();
                    graphics.setColor(color);
                    final int stringWidth = metrics.stringWidth(text);
                    // vertically center text
                    final int topSpacing = (textRect.height
                                            - (metrics.getAscent() + metrics.getDescent()))
                                           / 2;
                    final int offset = (textRect.width - stringWidth) / 2;
                    graphics.drawString(text,
                                        textRect.x + offset,
                                        textRect.y + topSpacing + metrics.getAscent());
                }
            } finally {
                graphics.dispose();
            }
        }

        private void drawThresholdLine(final Graphics g,
                                       final JStatusComponent status,
                                       final Rectangle barArea,
                                       final double threshold) {
            final Graphics2D graphics = (Graphics2D) g.create();
            try {
                if ((int) threshold != ThresholdData.IGNORE) {
                    graphics.setColor(Color.black);
                    graphics.drawLine(barArea.x,
                                      barTop(status, barArea, threshold),
                                      barArea.x + barArea.width + 1,
                                      barTop(status, barArea, threshold));
                }
            } finally {
                graphics.dispose();
            }
        }

        /**
         * Compute the y point for the top of the bar.
         *
         * @param status
         *            JStatusComponent
         * @param barArea
         *            rectangle where the bar will be drawn
         * @param value
         *            value
         * @return the top of the rectangle
         */
        private int barTop(final JStatusComponent status,
                           final Rectangle barArea,
                           final double value) {
            return barArea.y + barArea.height + 1 - barHeight(status, barArea, value);
        }

        /**
         * Compute the height of the bar as a percentage of the area height not to the taller that
         * the area.
         *
         * @param status
         *            JStatusComponent
         * @param barArea
         *            rectangle where the bar will be drawn
         * @param value
         *            value
         * @return the height
         */
        private int barHeight(final JStatusComponent status,
                              final Rectangle barArea,
                              final double value) {
            return (int) (barArea.height
                          * (Math.min(value, status.getAxisMax()) / status.getAxisMax()));

        }
    }

    private static class StatusHorizontalUI
        extends
            BasicButtonUI {

        @Override
        public void paint(final Graphics graphics,
                          final JComponent component) throws IllegalArgumentException {
            try {
                final JStatusComponent status = (JStatusComponent) component;
                final Color oldColor = graphics.getColor();
                // layout
                Rectangle barRect = null;
                Rectangle textRect = null;
                final FontMetrics metrics = graphics.getFontMetrics();
                final int topOfText = component.getHeight() - metrics.getHeight() - 3;
                textRect = new Rectangle(2,
                        topOfText,
                        component.getWidth() - 4,
                        metrics.getHeight());
                barRect = new Rectangle(5,
                        5,
                        component.getWidth() - 10,
                        component.getHeight() - textRect.height - 10);
                // paint
                graphics.setColor(status.getBackground());
                graphics.fillRect(0, 0, component.getWidth(), component.getHeight());
                paintGraph(graphics, status, barRect);
                paintText(graphics, status, textRect, status.getText());
                // restore
                graphics.setColor(oldColor);
            } catch (final Exception error) {
                throw new IllegalArgumentException(error);
            }
        }

        protected void paintGraph(final Graphics graphics,
                                  final JStatusComponent status,
                                  final Rectangle barRect) {
            final Rectangle barArea = new Rectangle(barRect);
            final int individualBarHeight = status.getBarCount() == 0 ? barArea.height
                                                                      : barArea.height
                                                                        / status.getBarCount();
            barArea.height = individualBarHeight * status.getBarCount();
            // draw bar area background draw box around it
            graphics.setColor(status.mBarBackground);
            graphics.fillRect(barArea.x, barArea.y, barArea.width, barArea.height + 1);
            graphics.setColor(Color.DARK_GRAY);
            graphics.drawRect(barArea.x, barArea.y, barArea.width, barArea.height + 1);
            // draw each bar
            barArea.setBounds(barArea.x + 1, barArea.y + 1, barArea.width - 2, barArea.height - 2);
            int individualBarTop = 0;
            for (int i = 0; i < status.getBarCount(); i++) {
                graphics.setColor(status.getThreshold() == null ? status.mBarColor
                                                                : status.getThreshold()
                                                                        .getColor(status.getValueAt(i)));
                graphics.fillRect(barArea.x,
                                  barArea.y + individualBarTop,
                                  value2offset(status, barArea, status.getValueAt(i)),
                                  individualBarHeight);
                if (i != 0) {
                    // draw a line at top of bar to provide some separation
                    graphics.setColor(Color.LIGHT_GRAY);
                    graphics.drawLine(barArea.x,
                                      barArea.y + individualBarTop,
                                      barArea.x + barArea.width,
                                      barArea.y + individualBarTop);
                }
                individualBarTop += individualBarHeight;
            }
            // draw threshold lines
            if (status.mThreshold != null) {
                drawThresholdLine(graphics, status, barArea, status.mThreshold.crisis().getValue());
                drawThresholdLine(graphics, status, barArea, status.mThreshold.alert().getValue());
                drawThresholdLine(graphics,
                                  status,
                                  barArea,
                                  status.mThreshold.warning().getValue());
            }
        }

        protected void paintText(final Graphics graphics,
                                 final JStatusComponent status,
                                 final Rectangle textRect,
                                 final String text) {
            if (text != null && !text.equals("")) {
                //graphics.setFont(UIManager.getFont("SmallLabel.font"));
                final FontMetrics metrics = graphics.getFontMetrics();
                graphics.setColor(status.getForeground());
                final int stringWidth = metrics.stringWidth(text);
                // vertically center text
                final int topSpacing = (textRect.height
                                        - (metrics.getAscent() + metrics.getDescent()))
                                       / 2;
                final int offset = (textRect.width - stringWidth) / 2;
                graphics.drawString(text,
                                    textRect.x + offset,
                                    textRect.y + topSpacing + metrics.getAscent());
            }
        }

        protected void drawThresholdLine(final Graphics graphics,
                                         final JStatusComponent status,
                                         final Rectangle barRect,
                                         final double threshold) {
            if ((int) threshold != ThresholdData.IGNORE) {
                graphics.setColor(Color.black);
                final int offset = value2offset(status, barRect, threshold);
                graphics.drawLine(barRect.x + offset,
                                  barRect.y,
                                  barRect.x + offset,
                                  barRect.y + barRect.height + 1);
            }
        }

        protected int value2offset(final JStatusComponent status,
                                   final Rectangle barRect,
                                   final double value) {
            return (int) (barRect.width * (value / status.getAxisMax()));
        }
    }

    public static class ThresholdData {

        public static final int      NOTSET        = 0;
        public static final int      GOOD          = 1;
        public static final int      WARNING       = 2;
        public static final int      ALERT         = 3;
        public static final int      CRISIS        = 4;

        public static final Color[]  COLOR         = {
                                                      Color.WHITE, Color.GREEN, Color.YELLOW,
                                                      Color.RED, Color.GRAY
        };
        public static final Color[]  COLOR_REVERSE = {
                                                      Color.BLACK, Color.BLACK, Color.BLACK,
                                                      Color.WHITE, Color.WHITE
        };

        public static final int      IGNORE        = -1;

        private int                  mIndex;
        private String               mName;                                                  // for display in panel which displays all threshold values
        private final ThresholdValue mWarning;
        private final ThresholdValue mAlert;
        private final ThresholdValue mCrisis;

        public ThresholdData() {
            mWarning = new ThresholdValue();
            mAlert = new ThresholdValue();
            mCrisis = new ThresholdValue();
        }

        public ThresholdData(final int inIndex,
                             final String name,
                             final double warning,
                             final double alert,
                             final double crisis) {
            this();
            mIndex = inIndex;
            mName = name;
            warning().setValue(warning);
            alert().setValue(alert);
            crisis().setValue(crisis);
        }

        public ThresholdValue warning() {
            return mWarning;
        }

        public ThresholdValue alert() {
            return mAlert;
        }

        public ThresholdValue crisis() {
            return mCrisis;
        }

        public int getIndex() {
            return mIndex;
        }

        public String getName() {
            return mName;
        }

        public void setName(final String value) {
            mName = value;
        }

        public int getStatus(final double value) {
            if (crisis().isTriggered(value)) {
                return ThresholdData.CRISIS;
            } else if (alert().isTriggered(value)) {
                return ThresholdData.ALERT;
            } else if (warning().isTriggered(value)) {
                return ThresholdData.WARNING;
            } else {
                return ThresholdData.GOOD;
            }
        }

        public Color getColor(final double value) {
            return ThresholdData.COLOR[getStatus(value)];
        }

        public Color getColorReversed(final double value) {
            return ThresholdData.COLOR_REVERSE[getStatus(value)];
        }

        @Override
        public String toString() {
            return String.format("%s %d W:%2f A:%2f C:%2f",
                                 getName(),
                                 getIndex(),
                                 warning().toString(),
                                 alert().toString(),
                                 crisis().toString());
        }

        public static class ThresholdValue {

            private double mValue = ThresholdData.IGNORE;

            public double getValue() {
                return mValue;
            }

            public void setValue(final double value) {
                mValue = value;
            }

            public boolean isTriggered(final double value) {
                return mValue != ThresholdData.IGNORE && value >= mValue;
            }

            @Override
            public String toString() {
                return mValue == ThresholdData.IGNORE ? "-" : String.valueOf(mValue);
            }
        }
    }
}
