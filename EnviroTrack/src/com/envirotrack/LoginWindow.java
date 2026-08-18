package com.envirotrack;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;

public class LoginWindow extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;
    private JLabel lblStatus;

    private static final Color BG_COLOR      = new Color(240, 244, 248);
    private static final Color CARD_COLOR    = Color.WHITE;
    private static final Color BRAND_PRIMARY = new Color(34, 110, 80);
    private static final Color BRAND_DARK    = new Color(20, 70, 50);
    private static final Color INPUT_BORDER  = new Color(200, 210, 205);
    private static final Color INPUT_FOCUS   = new Color(34, 110, 80);
    private static final Color TEXT_MUTED    = new Color(100, 115, 110);
    private static final Color TEXT_PRIMARY  = new Color(30, 40, 35);

    public LoginWindow() {
        setTitle("EnviroTrack");
        setSize(420, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(BG_COLOR);
        setContentPane(bg);

        // ── Card ──
        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_COLOR);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(340, 460));

        // Use GridBagLayout for the card itself so every row stretches full width
        card.setLayout(new GridBagLayout());

        GridBagConstraints row = new GridBagConstraints();
        row.gridx = 0;
        row.fill = GridBagConstraints.HORIZONTAL;
        row.weightx = 1.0;
        row.anchor = GridBagConstraints.WEST;
        row.insets = new Insets(0, 36, 0, 36);

        // ── Signal wave logo (centered) ──
        JPanel logoCircle = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BRAND_PRIMARY);
                g2.fillOval(0, 0, 64, 64);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.fillOval(29, 29, 6, 6);
                g2.drawArc(20, 20, 24, 24, 225, 90);
                g2.drawArc(13, 13, 38, 38, 225, 90);
                g2.drawArc(6, 6, 52, 52, 225, 90);
                g2.dispose();
            }
        };
        logoCircle.setOpaque(false);
        logoCircle.setPreferredSize(new Dimension(64, 64));

        // Center the logo using its own panel
        JPanel logoWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoWrapper.setOpaque(false);
        logoWrapper.add(logoCircle);

        // ── App name (centered) ──
        JLabel lblAppName = new JLabel("EnviroTrack", SwingConstants.CENTER);
        lblAppName.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblAppName.setForeground(TEXT_PRIMARY);

        // ── Tagline (centered) ──
        JLabel lblTagline = new JLabel("Environmental Sensor Monitoring", SwingConstants.CENTER);
        lblTagline.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblTagline.setForeground(TEXT_MUTED);

        // ── Separator ──
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(230, 235, 232));

        // ── Username label ──
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblUser.setForeground(TEXT_MUTED);

        // ── Username field ──
        txtUser = new JTextField();
        txtUser.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtUser.setForeground(TEXT_PRIMARY);
        txtUser.setPreferredSize(new Dimension(0, 36));
        txtUser.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(INPUT_BORDER, 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtUser.addFocusListener(focusBorder(txtUser));
        txtUser.addActionListener(e -> txtPass.requestFocus());

        // ── Password label ──
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblPass.setForeground(TEXT_MUTED);

        // ── Password field ──
        txtPass = new JPasswordField();
        txtPass.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtPass.setForeground(TEXT_PRIMARY);
        txtPass.setPreferredSize(new Dimension(0, 36));
        txtPass.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(INPUT_BORDER, 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtPass.addFocusListener(focusBorder(txtPass));
        txtPass.addActionListener(e -> checkLogin());

        // ── Sign In button ──
        btnLogin = new JButton("Sign In") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() || getModel().isRollover() ? BRAND_DARK : BRAND_PRIMARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        btnLogin.setPreferredSize(new Dimension(0, 44));
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> checkLogin());

        // ── Status ──
        lblStatus = new JLabel(" ", SwingConstants.LEFT);
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblStatus.setForeground(TEXT_MUTED);

        // ── Footer ──
        JLabel lblFooter = new JLabel("v1.0  •  Environmental Monitoring System", SwingConstants.CENTER);
        lblFooter.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblFooter.setForeground(new Color(160, 175, 170));

        // ── Assemble card rows top to bottom ──
        int r = 0;

        row.gridy = r++; row.insets = new Insets(8, 36, 0, 36);
        card.add(logoWrapper, row);

        row.gridy = r++; row.insets = new Insets(12, 36, 0, 36);
        card.add(lblAppName, row);

        row.gridy = r++; row.insets = new Insets(4, 36, 0, 36);
        card.add(lblTagline, row);

        row.gridy = r++; row.insets = new Insets(18, 36, 0, 36);
        card.add(sep, row);

        row.gridy = r++; row.insets = new Insets(22, 36, 0, 36);
        card.add(lblUser, row);

        row.gridy = r++; row.insets = new Insets(6, 36, 0, 36);
        card.add(txtUser, row);

        row.gridy = r++; row.insets = new Insets(14, 36, 0, 36);
        card.add(lblPass, row);

        row.gridy = r++; row.insets = new Insets(6, 36, 0, 36);
        card.add(txtPass, row);

        row.gridy = r++; row.insets = new Insets(22, 36, 0, 36);
        card.add(btnLogin, row);

        row.gridy = r++; row.insets = new Insets(10, 36, 0, 36);
        card.add(lblStatus, row);

        // Spacer pushes footer to bottom
        GridBagConstraints spacer = new GridBagConstraints();
        spacer.gridx = 0; spacer.gridy = r++; spacer.weighty = 1.0;
        card.add(new JLabel(""), spacer);

        row.gridy = r; row.insets = new Insets(0, 36, 24, 36);
        card.add(lblFooter, row);

        bg.add(card);
    }

    private FocusAdapter focusBorder(JTextField field) {
        return new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(INPUT_FOCUS, 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            }
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(INPUT_BORDER, 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            }
        };
    }

    private void checkLogin() {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setForeground(new Color(180, 60, 60));
            lblStatus.setText("Please enter username and password.");
            return;
        }

        lblStatus.setForeground(TEXT_MUTED);
        lblStatus.setText("Checking credentials...");
        btnLogin.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            protected Boolean doInBackground() throws Exception {
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM users WHERE username=? AND password=?");
                    ps.setString(1, username);
                    ps.setString(2, password);
                    return ps.executeQuery().next();
                }
            }
            protected void done() {
                try {
                    if (get()) {
                        lblStatus.setForeground(BRAND_PRIMARY);
                        lblStatus.setText("Login successful. Opening dashboard...");
                        Timer t = new Timer(600, ev -> { dispose(); new Main().setVisible(true); });
                        t.setRepeats(false);
                        t.start();
                    } else {
                        lblStatus.setForeground(new Color(180, 60, 60));
                        lblStatus.setText("Invalid username or password.");
                        txtPass.setText("");
                        txtPass.requestFocus();
                        btnLogin.setEnabled(true);
                    }
                } catch (Exception ex) {
                    lblStatus.setForeground(new Color(180, 60, 60));
                    lblStatus.setText("Database error: " + ex.getMessage());
                    btnLogin.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
    }
}