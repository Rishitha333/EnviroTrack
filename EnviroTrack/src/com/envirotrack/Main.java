package com.envirotrack;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class Main extends JFrame {

    private JTextField txtName, txtValue;
    private JButton btnAdd, btnUpdate, btnDelete, btnShowAlerts;
    private JTable table;
    private DefaultTableModel model;
    private GraphPanel graphPanel;
    private JLabel lblStatus;
    private List<String> alertHistory = new ArrayList<>();
    private JTextArea txtPredictions;
    private JButton btnRunAnalysis;
    private JButton btnSimulator;
    private Process simulatorProcess = null;
    private javax.swing.Timer autoRefreshTimer;

    // Sensor colours for graph lines
    private static final Color[] SENSOR_COLORS = {
        new Color(33, 150, 243),   // blue
        new Color(244, 67, 54),    // red
        new Color(76, 175, 80),    // green
        new Color(255, 152, 0),    // orange
        new Color(156, 39, 176),   // purple
        new Color(0, 188, 212),    // cyan
    };

    public Main() {
        setTitle("EnviroTrack - Sensor Management");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(new Color(245, 247, 250));
        setLocationRelativeTo(null);

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(null);
        inputPanel.setBackground(new Color(200, 230, 201));
        inputPanel.setBorder(BorderFactory.createLineBorder(new Color(150, 200, 150), 1));
        inputPanel.setBounds(50, 50, 800, 150);
        add(inputPanel);

        JLabel lblName = new JLabel("Sensor Name:");
        lblName.setBounds(20, 20, 120, 25);
        inputPanel.add(lblName);

        txtName = new JTextField();
        txtName.setBounds(150, 20, 200, 25);
        inputPanel.add(txtName);

        JLabel lblValue = new JLabel("Value:");
        lblValue.setBounds(20, 70, 120, 25);
        inputPanel.add(lblValue);

        txtValue = new JTextField();
        txtValue.setBounds(150, 70, 200, 25);
        inputPanel.add(txtValue);

        btnAdd = new JButton("Add");
        btnAdd.setBounds(400, 20, 120, 30);
        btnAdd.setBackground(new Color(76, 175, 80));
        btnAdd.setForeground(Color.WHITE);
        inputPanel.add(btnAdd);

        btnUpdate = new JButton("Update");
        btnUpdate.setBounds(550, 20, 120, 30);
        btnUpdate.setBackground(new Color(255, 152, 0));
        btnUpdate.setForeground(Color.WHITE);
        inputPanel.add(btnUpdate);

        btnDelete = new JButton("Delete");
        btnDelete.setBounds(700, 20, 120, 30);
        btnDelete.setBackground(new Color(244, 67, 54));
        btnDelete.setForeground(Color.WHITE);
        inputPanel.add(btnDelete);

        // Table
        model = new DefaultTableModel(new Object[]{"ID", "Name", "Value", "Timestamp"}, 0);
        table = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                Object valObj = getValueAt(row, 2);
                if (valObj instanceof Double) {
                    double val = (double) valObj;
                    if (val >= 80) c.setBackground(new Color(255, 205, 205));
                    else if (val >= 50) c.setBackground(new Color(255, 249, 196));
                    else c.setBackground(new Color(200, 230, 201));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        table.setRowHeight(24);
        table.getTableHeader().setBackground(new Color(63, 84, 186));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(50, 230, 800, 500);
        add(scrollPane);

        // Graph panel
        graphPanel = new GraphPanel();
        graphPanel.setBounds(900, 50, 650, 450);
        graphPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        add(graphPanel);

        // Buttons row
        btnShowAlerts = new JButton("Show Alerts");
        btnShowAlerts.setBounds(900, 520, 180, 35);
        btnShowAlerts.setBackground(new Color(96, 125, 139));
        btnShowAlerts.setForeground(Color.WHITE);
        add(btnShowAlerts);

        btnSimulator = new JButton("▶  Start Simulation");
        btnSimulator.setBounds(1100, 520, 200, 35);
        btnSimulator.setBackground(new Color(34, 139, 34));
        btnSimulator.setForeground(Color.WHITE);
        btnSimulator.setFont(new Font("SansSerif", Font.BOLD, 13));
        add(btnSimulator);

        btnRunAnalysis = new JButton("⚙  Run Analysis");
        btnRunAnalysis.setBounds(900, 570, 180, 35);
        btnRunAnalysis.setBackground(new Color(63, 84, 186));
        btnRunAnalysis.setForeground(Color.WHITE);
        btnRunAnalysis.setFont(new Font("SansSerif", Font.BOLD, 13));
        add(btnRunAnalysis);

        JLabel lblPredictions = new JLabel("Predictions & Anomaly Detection:");
        lblPredictions.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblPredictions.setBounds(900, 620, 350, 25);
        add(lblPredictions);

        txtPredictions = new JTextArea();
        txtPredictions.setEditable(false);
        txtPredictions.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtPredictions.setBackground(new Color(245, 245, 245));
        JScrollPane predScroll = new JScrollPane(txtPredictions);
        predScroll.setBounds(900, 650, 650, 200);
        add(predScroll);

        lblStatus = new JLabel("Status: Loading data...");
        lblStatus.setBounds(50, 760, 900, 25);
        lblStatus.setForeground(new Color(0, 102, 0));
        lblStatus.setFont(new Font("SansSerif", Font.ITALIC, 12));
        add(lblStatus);

        loadTableData();

        autoRefreshTimer = new javax.swing.Timer(5000, e -> {
            loadTableData();
            loadPredictions();
        });

        // ---------------- Button Actions ----------------

        btnAdd.addActionListener(e -> {
            try {
                double value = Double.parseDouble(txtValue.getText());
                if (value >= 80) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Value is HIGH. Add anyway?",
                            "High Value Alert", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;
                    alertHistory.add("High value added: " + txtName.getText() + " = " + value);
                }
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "INSERT INTO sensors (name, value, timestamp) VALUES (?, ?, NOW())";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, txtName.getText());
                    ps.setDouble(2, value);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Sensor Added!");
                    loadTableData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                try (Connection conn = DBConnection.getConnection()) {
                    int id = (int) model.getValueAt(selectedRow, 0);
                    double value = Double.parseDouble(txtValue.getText());
                    if (value >= 80) {
                        int confirm = JOptionPane.showConfirmDialog(this,
                                "Value is HIGH. Update anyway?",
                                "High Value Alert", JOptionPane.YES_NO_OPTION);
                        if (confirm != JOptionPane.YES_OPTION) return;
                        alertHistory.add("High value updated: " + txtName.getText() + " = " + value);
                    }
                    String sql = "UPDATE sensors SET name=?, value=?, timestamp=NOW() WHERE id=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, txtName.getText());
                    ps.setDouble(2, value);
                    ps.setInt(3, id);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Sensor Updated!");
                    loadTableData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(null, "Select a row to update!");
            }
        });

        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                try (Connection conn = DBConnection.getConnection()) {
                    int id = (int) model.getValueAt(selectedRow, 0);
                    String sql = "DELETE FROM sensors WHERE id=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Sensor Deleted!");
                    loadTableData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(null, "Select a row to delete!");
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow();
                txtName.setText(model.getValueAt(selectedRow, 1).toString());
                txtValue.setText(model.getValueAt(selectedRow, 2).toString());
            }
        });

        btnShowAlerts.addActionListener(e -> {
            if (alertHistory.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No alerts yet!");
                return;
            }
            JTextArea area = new JTextArea();
            for (String a : alertHistory) area.append(a + "\n");
            area.setEditable(false);
            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(400, 300));
            JOptionPane.showMessageDialog(this, sp, "Alert History", JOptionPane.INFORMATION_MESSAGE);
        });

        btnSimulator.addActionListener(e -> {
            if (simulatorProcess == null) {
                try {
                    ProcessBuilder pb = new ProcessBuilder("py", "EnviroTrack\\python\\simulator.py");
                    pb.redirectErrorStream(true);
                    simulatorProcess = pb.start();
                    autoRefreshTimer.start();
                    btnSimulator.setText("⏹  Stop Simulation");
                    btnSimulator.setBackground(new Color(178, 34, 34));
                    lblStatus.setText("Status: Simulation running — auto-refreshing every 5 seconds...");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to start simulator: " + ex.getMessage());
                }
            } else {
                simulatorProcess.destroy();
                simulatorProcess = null;
                autoRefreshTimer.stop();
                btnSimulator.setText("▶  Start Simulation");
                btnSimulator.setBackground(new Color(34, 139, 34));
                lblStatus.setText("Status: Simulation stopped.");
            }
        });

        btnRunAnalysis.addActionListener(e -> {
            lblStatus.setText("Status: Running Python analysis...");
            new Thread(() -> {
                try {
                    ProcessBuilder pb = new ProcessBuilder("py", "EnviroTrack\\python\\analyze.py");
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    process.waitFor();
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setText("Status: Analysis complete.");
                        loadPredictions();
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                        lblStatus.setText("Status: Python error - " + ex.getMessage()));
                }
            }).start();
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (simulatorProcess != null) simulatorProcess.destroy();
            }
        });

        setVisible(true);
    }

    // ---------------- Load Table and Update Graph ----------------
    private void loadTableData() {
        model.setRowCount(0);
        // Map: sensorName -> list of values (for multi-line graph)
        LinkedHashMap<String, List<Double>> sensorMap = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM sensors WHERE name IN ('Temperature Sensor', 'Humidity Sensor', 'Air Quality Sensor') ORDER BY timestamp DESC LIMIT 60";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                double val = rs.getDouble("value");
                String name = rs.getString("name");
                model.addRow(new Object[]{
                        rs.getInt("id"), name, val, rs.getTimestamp("timestamp")
                });
                sensorMap.computeIfAbsent(name, k -> new ArrayList<>()).add(val);
                if (val >= 80) {
                    String entry = "Auto alert: " + name + " = " + val + " at " + rs.getTimestamp("timestamp");
                    if (!alertHistory.contains(entry)) alertHistory.add(entry);
                }
            }
            lblStatus.setText("Status: Loaded " + model.getRowCount() + " sensor record(s).");
        } catch (Exception ex) {
            lblStatus.setText("Status: Error loading data.");
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
        graphPanel.setSensorData(sensorMap);
        graphPanel.repaint();
    }

    // ---------------- Load Predictions ----------------
    private void loadPredictions() {
        txtPredictions.setText("");
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT sensor_name, predicted_value, anomaly_flag, anomaly_reason FROM predictions";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-25s %-18s %-12s %s%n", "Sensor", "Predicted Value", "Anomaly", "Reason"));
            sb.append("-".repeat(80)).append("\n");
            while (rs.next()) {
                sb.append(String.format("%-25s %-18.2f %-12s %s%n",
                    rs.getString("sensor_name"),
                    rs.getDouble("predicted_value"),
                    rs.getBoolean("anomaly_flag") ? "YES !" : "No",
                    rs.getString("anomaly_reason")
                ));
            }
            txtPredictions.setText(sb.toString());
        } catch (Exception ex) {
            txtPredictions.setText("Error loading predictions: " + ex.getMessage());
        }
    }

    // ---------------- Multi-Sensor Graph Panel ----------------
    class GraphPanel extends JPanel {
        private LinkedHashMap<String, List<Double>> sensorData = new LinkedHashMap<>();
        private final int PAD_LEFT = 55;
        private final int PAD_RIGHT = 20;
        private final int PAD_TOP = 20;
        private final int PAD_BOTTOM = 40;

        public void setSensorData(LinkedHashMap<String, List<Double>> data) {
            this.sensorData = data;
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);

            // Border
            g2.setColor(new Color(220, 220, 220));
            g2.drawRect(PAD_LEFT, PAD_TOP, w - PAD_LEFT - PAD_RIGHT, h - PAD_TOP - PAD_BOTTOM);

            if (sensorData.isEmpty()) {
                g2.setColor(Color.GRAY);
                g2.drawString("No data yet", w / 2 - 30, h / 2);
                return;
            }

            // Find global max/min across all sensors
            double globalMax = sensorData.values().stream()
                .flatMap(List::stream).mapToDouble(v -> v).max().orElse(100);
            double globalMin = sensorData.values().stream()
                .flatMap(List::stream).mapToDouble(v -> v).min().orElse(0);
            globalMax = Math.ceil(globalMax / 10) * 10;
            globalMin = Math.max(0, Math.floor(globalMin / 10) * 10);
            double range = globalMax - globalMin == 0 ? 1 : globalMax - globalMin;

            int graphW = w - PAD_LEFT - PAD_RIGHT;
            int graphH = h - PAD_TOP - PAD_BOTTOM;

            // Y-axis grid lines and labels
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            int ySteps = 5;
            for (int i = 0; i <= ySteps; i++) {
                double val = globalMin + (range * i / ySteps);
                int y = PAD_TOP + graphH - (int) (graphH * i / ySteps);
                g2.setColor(new Color(240, 240, 240));
                g2.drawLine(PAD_LEFT, y, PAD_LEFT + graphW, y);
                g2.setColor(Color.GRAY);
                g2.drawString(String.format("%.0f", val), 5, y + 4);
            }

            // Draw each sensor as a separate coloured line
            int colorIdx = 0;
            List<String> sensorNames = new ArrayList<>(sensorData.keySet());
            for (String sensorName : sensorNames) {
                List<Double> values = sensorData.get(sensorName);
                if (values.size() < 2) { colorIdx++; continue; }

                Color lineColor = SENSOR_COLORS[colorIdx % SENSOR_COLORS.length];
                g2.setColor(lineColor);
                g2.setStroke(new BasicStroke(2.0f));

                for (int i = 1; i < values.size(); i++) {
                    int x1 = PAD_LEFT + (i - 1) * graphW / (values.size() - 1);
                    int y1 = PAD_TOP + graphH - (int) ((values.get(i - 1) - globalMin) / range * graphH);
                    int x2 = PAD_LEFT + i * graphW / (values.size() - 1);
                    int y2 = PAD_TOP + graphH - (int) ((values.get(i) - globalMin) / range * graphH);
                    g2.drawLine(x1, y1, x2, y2);
                }
                colorIdx++;
            }

            // Legend (bottom)
            colorIdx = 0;
            int legendX = PAD_LEFT + 10;
            int legendY = h - PAD_BOTTOM + 18;
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            for (String name : sensorNames) {
                Color c = SENSOR_COLORS[colorIdx % SENSOR_COLORS.length];
                g2.setColor(c);
                g2.fillRect(legendX, legendY - 10, 14, 10);
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(name, legendX + 18, legendY);
                legendX += g2.getFontMetrics().stringWidth(name) + 40;
                colorIdx++;
            }
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}