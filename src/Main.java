import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class Main {
    private static final String URL = "jdbc:mysql://localhost:3000/vehicle_parking";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "L2411021061679";
    private static final int TOTAL_SLOTS = 5;
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private JFrame frame;
    private JTextField registrationField;
    private JComboBox<String> vehicleTypeBox;
    private JComboBox<Integer> availableSlotBox;
    private JLabel availableLabel;
    private JTable recordsTable;
    private DefaultTableModel tableModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Main().createWindow();
            }
        });
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    private void createWindow() {
        frame = new JFrame("Vehicle Parking and Slot Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 550);
        frame.setLocationRelativeTo(null);

        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 8, 8));
        inputPanel.add(new JLabel("Registration Number:"));
        registrationField = new JTextField();
        inputPanel.add(registrationField);
        inputPanel.add(new JLabel("Vehicle Type:"));
        vehicleTypeBox = new JComboBox<String>(new String[] {"Car", "Bike"});
        inputPanel.add(vehicleTypeBox);
        inputPanel.add(new JLabel("Available Slot:"));
        availableSlotBox = new JComboBox<Integer>();
        inputPanel.add(availableSlotBox);

        JButton parkButton = new JButton("Park Vehicle");
        parkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                parkVehicle();
            }
        });
        inputPanel.add(parkButton);

        JButton releaseButton = new JButton("Release Vehicle");
        releaseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                releaseVehicle();
            }
        });
        inputPanel.add(releaseButton);

        tableModel = new DefaultTableModel(
                new String[] {"Slot Number", "Registration Number", "Vehicle Type",
                        "Entry Time", "Exit Time", "Parking Fee"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recordsTable = new JTable(tableModel);
        recordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        availableLabel = new JLabel("Available Slots: 0");
        bottomPanel.add(availableLabel);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(recordsTable), BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        try {
            refreshData();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void parkVehicle() {
        String registrationNumber = registrationField.getText().trim();
        String vehicleType = (String) vehicleTypeBox.getSelectedItem();

        if (registrationNumber.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a registration number.");
            return;
        }

        try (Connection connection = connect()) {
            String duplicateSql = "SELECT record_id FROM parking_records "
                    + "WHERE registration_number = ? AND active = 1";
            PreparedStatement duplicateStatement = connection.prepareStatement(duplicateSql);
            duplicateStatement.setString(1, registrationNumber);
            ResultSet duplicateResult = duplicateStatement.executeQuery();
            if (duplicateResult.next()) {
                JOptionPane.showMessageDialog(frame,
                        "This vehicle is already parked.");
                return;
            }

            String slotSql = "SELECT slot_id FROM parking_slots "
                    + "WHERE occupied = 0 ORDER BY slot_id LIMIT 1";
            PreparedStatement slotStatement = connection.prepareStatement(slotSql);
            ResultSet slotResult = slotStatement.executeQuery();
            if (!slotResult.next()) {
                JOptionPane.showMessageDialog(frame, "All parking slots are occupied.");
                return;
            }

            int slotId = slotResult.getInt("slot_id");
            String insertSql = "INSERT INTO parking_records "
                    + "(registration_number, vehicle_type, slot_id, entry_time, active) "
                    + "VALUES (?, ?, ?, ?, 1)";
            PreparedStatement insertStatement = connection.prepareStatement(insertSql);
            insertStatement.setString(1, registrationNumber);
            insertStatement.setString(2, vehicleType);
            insertStatement.setInt(3, slotId);
            insertStatement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            insertStatement.executeUpdate();

            String updateSql = "UPDATE parking_slots SET occupied = 1 WHERE slot_id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);
            updateStatement.setInt(1, slotId);
            updateStatement.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Vehicle parked in slot " + slotId + ".");
            registrationField.setText("");
            refreshData();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void releaseVehicle() {
        int selectedRow = recordsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Select a parked vehicle from the table.");
            return;
        }

        int slotId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String registrationNumber = (String) tableModel.getValueAt(selectedRow, 1);
        String entryText = (String) tableModel.getValueAt(selectedRow, 3);

        try (Connection connection = connect()) {
            LocalDateTime entryTime = LocalDateTime.parse(entryText, TIME_FORMAT);
            LocalDateTime exitTime = LocalDateTime.now();
            long minutes = Duration.between(entryTime, exitTime).toMinutes();
            long hours = (minutes + 59) / 60;
            if (hours < 1) {
                hours = 1;
            }
            int fee = (int) hours * 20;

            String recordSql = "UPDATE parking_records SET exit_time = ?, fee = ?, "
                    + "active = 0 WHERE registration_number = ? AND slot_id = ? AND active = 1";
            PreparedStatement recordStatement = connection.prepareStatement(recordSql);
            recordStatement.setTimestamp(1, Timestamp.valueOf(exitTime));
            recordStatement.setInt(2, fee);
            recordStatement.setString(3, registrationNumber);
            recordStatement.setInt(4, slotId);
            int changedRecords = recordStatement.executeUpdate();
            if (changedRecords == 0) {
                JOptionPane.showMessageDialog(frame,
                        "This vehicle has already been released.");
                return;
            }

            String slotSql = "UPDATE parking_slots SET occupied = 0 WHERE slot_id = ?";
            PreparedStatement slotStatement = connection.prepareStatement(slotSql);
            slotStatement.setInt(1, slotId);
            slotStatement.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Vehicle released. Parking fee: Rs. " + fee);
            refreshData();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void refreshData() throws SQLException {
        tableModel.setRowCount(0);
        availableSlotBox.removeAllItems();

        try (Connection connection = connect()) {
            String slotSql = "SELECT slot_id FROM parking_slots "
                    + "WHERE occupied = 0 ORDER BY slot_id";
            PreparedStatement slotStatement = connection.prepareStatement(slotSql);
            ResultSet slotResult = slotStatement.executeQuery();
            int availableCount = 0;
            while (slotResult.next()) {
                availableSlotBox.addItem(slotResult.getInt("slot_id"));
                availableCount++;
            }
            availableLabel.setText("Available Slots: " + availableCount + " / " + TOTAL_SLOTS);

            String recordSql = "SELECT record_id, slot_id, registration_number, vehicle_type, "
                    + "entry_time, exit_time, fee FROM parking_records "
                    + "ORDER BY active DESC, slot_id, entry_time";
            PreparedStatement recordStatement = connection.prepareStatement(recordSql);
            ResultSet recordResult = recordStatement.executeQuery();
            while (recordResult.next()) {
                String entry = recordResult.getTimestamp("entry_time")
                        .toLocalDateTime().format(TIME_FORMAT);
                Object exitValue = recordResult.getTimestamp("exit_time");
                String exit = exitValue == null ? "-" :
                        ((Timestamp) exitValue).toLocalDateTime().format(TIME_FORMAT);
                Object fee = recordResult.getObject("fee");
                tableModel.addRow(new Object[] {
                        recordResult.getInt("slot_id"),
                        recordResult.getString("registration_number"),
                        recordResult.getString("vehicle_type"),
                        entry,
                        exit,
                        fee == null ? "-" : fee
                });
            }
        }
    }

    private void showDatabaseError(SQLException ex) {
        JOptionPane.showMessageDialog(frame,
                "Database connection or query failed.\n"
                        + "Check MySQL, the database password, port, and Connector/J JAR.\n\n"
                        + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
