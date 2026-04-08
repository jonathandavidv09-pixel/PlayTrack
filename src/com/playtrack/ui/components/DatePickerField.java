package com.playtrack.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DatePickerField extends JTextField {
    private String placeholder;
    private JPopupMenu popup;
    private Color BORDER_SUBTLE = new Color(55, 65, 85);
    private Color BG_FIELD = new Color(30, 36, 52);
    private Calendar currentCalendar = Calendar.getInstance();

    public DatePickerField(String placeholder) {
        this.placeholder = placeholder;

        setPreferredSize(new Dimension(200, 28));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        setFont(new Font("Segoe UI", Font.PLAIN, 12));
        setBackground(BG_FIELD);
        setForeground(StyleConfig.TEXT_COLOR);
        setCaretColor(StyleConfig.TEXT_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        setEditable(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPicker();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (getText().isEmpty() && !isFocusOwner() && placeholder != null) {
            g2.setColor(new Color(85, 95, 115));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
        }

        // Draw Calendar Icon on the right side
        int iconSize = 16;
        int cx = getWidth() - iconSize / 2 - 12;
        int cy = getHeight() / 2;

        boolean isHovered = getMousePosition() != null;
        Color iconColor = isHovered ? StyleConfig.PRIMARY_COLOR : new Color(110, 120, 140);
        
        UIUtils.drawCalendarIcon(g2, cx, cy, iconSize, iconColor);

        g2.dispose();
    }

    private void showPicker() {
        if (popup != null && popup.isVisible()) return;
        popup = new JPopupMenu();
        popup.setBackground(BG_FIELD);
        popup.setBorder(BorderFactory.createLineBorder(StyleConfig.PRIMARY_COLOR));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_FIELD);
        panel.setPreferredSize(new Dimension(240, 220));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JButton prev = createHeaderButton("<");
        JButton next = createHeaderButton(">");
        
        JLabel monthLbl = new JLabel(new SimpleDateFormat("MMMM yyyy").format(currentCalendar.getTime()), SwingConstants.CENTER);
        monthLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        monthLbl.setForeground(Color.WHITE);
        
        header.add(prev, BorderLayout.WEST);
        header.add(monthLbl, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        panel.add(header, BorderLayout.NORTH);

        // Days Grid
        JPanel grid = new JPanel(new GridLayout(0, 7, 4, 4));
        grid.setOpaque(false);
        String[] days = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
        for (String d : days) {
            JLabel dl = new JLabel(d, SwingConstants.CENTER);
            dl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            dl.setForeground(StyleConfig.PRIMARY_COLOR);
            grid.add(dl);
        }

        Calendar calc = (Calendar) currentCalendar.clone();
        calc.set(Calendar.DAY_OF_MONTH, 1);
        int startDay = calc.get(Calendar.DAY_OF_WEEK);
        int maxDays = calc.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i < startDay; i++) {
            grid.add(new JLabel("")); // empty
        }
        for (int d = 1; d <= maxDays; d++) {
            final int day = d;
            JButton btn = new JButton(String.valueOf(day));
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setForeground(StyleConfig.TEXT_COLOR);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setForeground(StyleConfig.PRIMARY_COLOR); }
                public void mouseExited(MouseEvent e) { btn.setForeground(StyleConfig.TEXT_COLOR); }
            });

            btn.addActionListener(e -> {
                Calendar res = (Calendar) currentCalendar.clone();
                res.set(Calendar.DAY_OF_MONTH, day);
                setText(new SimpleDateFormat("yyyy-MM-dd").format(res.getTime()));
                popup.setVisible(false);
            });
            grid.add(btn);
        }

        prev.addActionListener(e -> { currentCalendar.add(Calendar.MONTH, -1); popup.setVisible(false); showPicker(); });
        next.addActionListener(e -> { currentCalendar.add(Calendar.MONTH, 1); popup.setVisible(false); showPicker(); });

        panel.add(grid, BorderLayout.CENTER);
        popup.add(panel);
        popup.show(this, 0, getHeight());
    }

    private JButton createHeaderButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(StyleConfig.TEXT_SECONDARY);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { btn.setForeground(StyleConfig.TEXT_SECONDARY); }
        });
        return btn;
    }
}
