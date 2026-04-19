package com.playtrack.ui.components;

import java.awt.event.MouseWheelEvent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Timer;

public final class SmoothScrollSupport {
    private static final String CLIENT_PROPERTY_KEY = "playtrack.smooth.vertical";
    private static final int TIMER_DELAY_MS = 16;
    private static final long RAPID_WHEEL_EVENT_NS = 20_000_000L;

    private SmoothScrollSupport() {
    }

    public static void installVertical(JScrollPane scrollPane) {
        if (scrollPane == null) {
            return;
        }
        if (Boolean.TRUE.equals(scrollPane.getClientProperty(CLIENT_PROPERTY_KEY))) {
            return;
        }
        scrollPane.putClientProperty(CLIENT_PROPERTY_KEY, Boolean.TRUE);

        final JScrollBar bar = scrollPane.getVerticalScrollBar();
        if (bar == null) {
            return;
        }

        final int[] target = { bar.getValue() };
        final long[] lastWheelEventNs = { 0L };
        final Timer animator = new Timer(TIMER_DELAY_MS, e -> {
            int max = maxValue(bar);
            target[0] = clamp(target[0], 0, max);
            int current = bar.getValue();
            int diff = target[0] - current;

            if (Math.abs(diff) <= 1) {
                bar.setValue(target[0]);
                ((Timer) e.getSource()).stop();
                return;
            }

            // Aggressive step keeps the motion smooth without queueing too many tiny repaints.
            int step = Math.max(2, (int) Math.ceil(Math.abs(diff) * 0.4));
            step = Math.min(step, 96);
            bar.setValue(current + (diff > 0 ? step : -step));
        });
        animator.setCoalesce(true);

        bar.addAdjustmentListener(e -> {
            if (!e.getValueIsAdjusting() && !animator.isRunning()) {
                target[0] = bar.getValue();
            }
        });

        scrollPane.setWheelScrollingEnabled(false);
        scrollPane.addMouseWheelListener(evt -> {
            if (evt.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL
                    && evt.getScrollType() != MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
                return;
            }

            int max = maxValue(bar);
            if (max <= 0) {
                return;
            }

            int direction = evt.getPreciseWheelRotation() >= 0 ? 1 : -1;
            int unit = Math.max(16, bar.getUnitIncrement(direction));
            double speedFactor = evt.isShiftDown() ? 1.6 : 2.4;
            int delta = (int) Math.round(evt.getPreciseWheelRotation() * unit * speedFactor);
            if (delta == 0) {
                delta = direction;
            }
            long now = System.nanoTime();
            boolean rapidSequence = now - lastWheelEventNs[0] < RAPID_WHEEL_EVENT_NS;
            lastWheelEventNs[0] = now;

            // Trackpads/high-frequency wheel events should scroll directly to avoid lag buildup.
            if (rapidSequence || Math.abs(evt.getPreciseWheelRotation()) < 0.8d) {
                animator.stop();
                int immediate = clamp(bar.getValue() + delta, 0, max);
                target[0] = immediate;
                bar.setValue(immediate);
            } else {
                target[0] = clamp(target[0] + delta, 0, max);
                if (!animator.isRunning()) {
                    animator.start();
                }
            }
            evt.consume();
        });

        scrollPane.addPropertyChangeListener("ancestor", e -> {
            if (scrollPane.getParent() == null && animator.isRunning()) {
                animator.stop();
            }
        });
    }

    private static int maxValue(JScrollBar bar) {
        return Math.max(0, bar.getMaximum() - bar.getVisibleAmount());
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }
}
