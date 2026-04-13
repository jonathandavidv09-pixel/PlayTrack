package com.playtrack.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
// Custom date picker component.
public class DatePickerField extends JTextField {
    private static final long serialVersionUID = 1L;
    private String placeholder;
    private JPopupMenu popup;
    private Color BORDER_SUBTLE = StyleConfig.SURFACE_STROKE;
    private Color BG_FIELD = StyleConfig.INPUT_BG;
    private Calendar currentCalendar = Calendar.getInstance();
    private boolean hovered = false;

    public DatePickerField(String placeholder) {
        this.placeholder = placeholder;

        setPreferredSize(new Dimension(200, 28));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        setFont(new Font("Arial", Font.PLAIN, 12));
        setBackground(BG_FIELD);
        setForeground(StyleConfig.TEXT_COLOR);
        setCaretColor(StyleConfig.TEXT_COLOR);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 30));
        setEditable(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPicker();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setPaint(new GradientPaint(
                0, 0, hovered ? StyleConfig.INPUT_BG_FOCUS : StyleConfig.INPUT_BG,
                0, getHeight(), StyleConfig.BACKGROUND_LIGHT));
        g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));

        g2.setColor(hovered ? new Color(StyleConfig.SECONDARY_COLOR.getRed(), StyleConfig.SECONDARY_COLOR.getGreen(),
                StyleConfig.SECONDARY_COLOR.getBlue(), 120) : BORDER_SUBTLE);
        g2.draw(new java.awt.geom.RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));

        super.paintComponent(g);

        if (getText().isEmpty() && !isFocusOwner() && placeholder != null) {
            g2.setColor(new Color(255, 255, 255, 120));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
        }

        // Trailing calendar icon so users know this field opens a date picker.
        int iconSize = 16;
        int cx = getWidth() - iconSize / 2 - 12;
        int cy = getHeight() / 2;

        Color iconColor = hovered ? StyleConfig.SECONDARY_COLOR : StyleConfig.TEXT_LIGHT;
        
        UIUtils.drawCalendarIcon(g2, cx, cy, iconSize, iconColor);

        g2.dispose();
    }

    private void showPicker() {
        if (popup != null && popup.isVisible()) return;
        popup = new JPopupMenu();
        popup.setBackground(StyleConfig.BACKGROUND_LIGHT);
        popup.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 38)));

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.BACKGROUND_LIGHT, 0, getHeight(), StyleConfig.BACKGROUND_COLOR));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(240, 220));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        // Month navigation buttons for the calendar popup header.
        JButton prev = createHeaderButton("<");
        JButton next = createHeaderButton(">");
        
        JLabel monthLbl = new JLabel(new SimpleDateFormat("MMMM yyyy").format(currentCalendar.getTime()), SwingConstants.CENTER);
        monthLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        monthLbl.setForeground(Color.WHITE);
        monthLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        monthLbl.setToolTipText("Select month and year");
        monthLbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showMonthYearDropdown(monthLbl);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                monthLbl.setForeground(StyleConfig.SECONDARY_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                monthLbl.setForeground(Color.WHITE);
            }
        });
        
        header.add(prev, BorderLayout.WEST);
        header.add(monthLbl, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        panel.add(header, BorderLayout.NORTH);

        
        JPanel grid = new JPanel(new GridLayout(0, 7, 4, 4));
        grid.setOpaque(false);
        String[] days = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
        for (String d : days) {
            JLabel dl = new JLabel(d, SwingConstants.CENTER);
            dl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            dl.setForeground(StyleConfig.SECONDARY_COLOR);
            grid.add(dl);
        }

        Calendar calc = (Calendar) currentCalendar.clone();
        calc.set(Calendar.DAY_OF_MONTH, 1);
        int startDay = calc.get(Calendar.DAY_OF_WEEK);
        int maxDays = calc.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i < startDay; i++) {
            grid.add(new JLabel("")); 
        }
        for (int d = 1; d <= maxDays; d++) {
            final int day = d;
            // Clickable day button for selecting a specific day in the current month.
            JButton btn = new JButton(String.valueOf(day));
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setForeground(StyleConfig.TEXT_COLOR);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setForeground(StyleConfig.SECONDARY_COLOR); }
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
        // Reusable button style for previous/next month navigation.
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

    private void showMonthYearDropdown(Component anchor) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog chooserDialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Select Month and Year", true);
        chooserDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel chooser = new JPanel(new GridBagLayout());
        chooser.setBackground(StyleConfig.BACKGROUND_LIGHT);
        chooser.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 38), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        String[] monthsRaw = new DateFormatSymbols().getMonths();
        String[] months = new String[12];
        System.arraycopy(monthsRaw, 0, months, 0, 12);
        JComboBox<String> monthBox = new JComboBox<>(months);
        styleSelector(monthBox);
        monthBox.setMaximumRowCount(12);
        monthBox.setPreferredSize(new Dimension(110, 30));
        monthBox.setSelectedIndex(currentCalendar.get(Calendar.MONTH));

        int minYear = 1900;
        int maxYear = Calendar.getInstance().get(Calendar.YEAR) + 5;
        Integer[] years = new Integer[maxYear - minYear + 1];
        int idx = 0;
        for (int y = maxYear; y >= minYear; y--) {
            years[idx++] = y;
        }
        JComboBox<Integer> yearBox = new JComboBox<>(years);
        styleSelector(yearBox);
        yearBox.setMaximumRowCount(16);
        yearBox.setPreferredSize(new Dimension(88, 30));
        yearBox.setSelectedItem(currentCalendar.get(Calendar.YEAR));

        JButton applyBtn = createPickerActionButton("Go", StyleConfig.SECONDARY_COLOR, StyleConfig.PRIMARY_LIGHT);
        applyBtn.addActionListener(e -> applyMonthYearSelection(monthBox, yearBox, chooserDialog));

        c.gridx = 0; c.gridy = 0;
        chooser.add(createPickerLabel("Month"), c);
        c.gridx = 1;
        chooser.add(createPickerLabel("Year"), c);

        c.gridx = 0; c.gridy = 1;
        chooser.add(monthBox, c);
        c.gridx = 1;
        chooser.add(yearBox, c);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionRow.setOpaque(false);
        actionRow.add(applyBtn);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        c.insets = new Insets(8, 4, 2, 4);
        c.anchor = GridBagConstraints.EAST;
        chooser.add(actionRow, c);

        chooserDialog.setContentPane(chooser);
        chooserDialog.pack();
        chooserDialog.setResizable(false);
        try {
            Point p = anchor.getLocationOnScreen();
            chooserDialog.setLocation(p.x, p.y + anchor.getHeight() + 4);
        } catch (Exception ex) {
            chooserDialog.setLocationRelativeTo(owner != null ? owner : this);
        }
        chooserDialog.setVisible(true);
    }

    private void styleSelector(JComboBox<?> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        box.setBackground(StyleConfig.INPUT_BG);
        box.setForeground(StyleConfig.TEXT_COLOR);
        box.setFocusable(false);
        box.setCursor(new Cursor(Cursor.HAND_CURSOR));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 28), 1, true),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));
    }

    private JLabel createPickerLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(StyleConfig.TEXT_SECONDARY);
        return label;
    }

    private JButton createPickerActionButton(String text, Color bgColor, Color hoverColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    private void applyMonthYearSelection(JComboBox<String> monthBox, JComboBox<Integer> yearBox, Window chooserWindow) {
        int selectedMonth = monthBox.getSelectedIndex();
        Integer selectedYear = (Integer) yearBox.getSelectedItem();
        if (selectedMonth < 0 || selectedYear == null) {
            return;
        }
        int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
        currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
        currentCalendar.set(Calendar.YEAR, selectedYear);
        currentCalendar.set(Calendar.MONTH, selectedMonth);
        int maxDay = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        currentCalendar.set(Calendar.DAY_OF_MONTH, Math.min(currentDay, maxDay));

        if (chooserWindow != null) {
            chooserWindow.dispose();
        }
        if (popup != null) {
            popup.setVisible(false);
        }
        showPicker();
    }
}
