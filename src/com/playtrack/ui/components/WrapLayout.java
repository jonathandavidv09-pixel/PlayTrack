package com.playtrack.ui.components;

import java.awt.*;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

/**
 * FlowLayout subclass that fully supports wrapping of components.
 */
public class WrapLayout extends FlowLayout {
    private static final long serialVersionUID = 1L;

    public WrapLayout() {
        super();
    }

    public WrapLayout(int align) {
        super(align);
    }

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = resolveTargetWidth(target);

            // Conservative fallback before first layout pass:
            // prefer taller wrapping over clipped content.
            if (targetWidth <= 0) {
                targetWidth = 320;
            }

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
            int maxWidth = targetWidth - horizontalInsetsAndGap;
            if (maxWidth <= 0) {
                maxWidth = Integer.MAX_VALUE;
            }

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);

                if (m.isVisible()) {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                    if (rowWidth + d.width > maxWidth) {
                        addRow(dim, rowWidth, rowHeight);
                        rowWidth = 0;
                        rowHeight = 0;
                    }

                    if (rowWidth != 0) {
                        rowWidth += hgap;
                    }

                    rowWidth += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
            }

            addRow(dim, rowWidth, rowHeight);

            dim.width += horizontalInsetsAndGap;
            dim.height += insets.top + insets.bottom + vgap * 2;

            Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (scrollPane != null && target.isValid()) {
                dim.width -= (hgap + 1);
            }

            return dim;
        }
    }

    private void addRow(Dimension dim, int rowWidth, int rowHeight) {
        dim.width = Math.max(dim.width, rowWidth);
        if (dim.height > 0) {
            dim.height += getVgap();
        }
        dim.height += rowHeight;
    }

    private int resolveTargetWidth(Container target) {
        int bestWidth = Integer.MAX_VALUE;

        int width = target.getSize().width;
        if (width > 0) {
            bestWidth = Math.min(bestWidth, width);
        }

        Container current = target;
        while (current != null) {
            width = current.getSize().width;
            if (width > 0) {
                bestWidth = Math.min(bestWidth, width);
            }

            if (current instanceof JViewport) {
                width = ((JViewport) current).getExtentSize().width;
                if (width > 0) {
                    bestWidth = Math.min(bestWidth, width);
                }
            }

            current = current.getParent();
        }

        return bestWidth == Integer.MAX_VALUE ? 0 : bestWidth;
    }
}
