import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDashboardFrame extends JFrame {

    private User currentDoctor;
    private AppointmentService appointmentService;
    private JPanel appointmentsContainer;
    private JTextField searchQrField;
    private JLabel statsLabel;
    private List<DoctorApptItem> allDoctorAppts = new ArrayList<>();

    private static class DoctorApptItem {
        long apptId;
        String patientName;
        String patientPhone;
        String dateTime;
        String status;
        String qrCode;
    }

    public DoctorDashboardFrame(User doctor, AppointmentService service) {
        this.currentDoctor = doctor;
        this.appointmentService = service;

        setTitle("ClinicOS - لوحة تحكم الطبيب والعيادة");
        java.net.URL iconURL = getClass().getResource("/logo.png");
        if (iconURL != null) {
            setIconImage(new ImageIcon(iconURL).getImage());
        } else {
            setIconImage(new ImageIcon("logo.png").getImage());
        }
        setSize(1080, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. الشريط الجانبي الكحلي =================
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, 680));
        sidebar.setBackground(new Color(15, 23, 42));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(25, 18, 25, 18));

        JPanel topSidebar = new JPanel();
        topSidebar.setOpaque(false);
        topSidebar.setLayout(new BoxLayout(topSidebar, BoxLayout.Y_AXIS));

        JLabel logoLabel = new JLabel("ClinicOS");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleBadge = new JLabel("بوابة الطبيب والعيادة");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleBadge.setForeground(new Color(56, 189, 248));
        roleBadge.setBorder(new EmptyBorder(4, 0, 12, 0));
        roleBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel docGreeting = new JLabel(currentDoctor.getfull_name());
        docGreeting.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        docGreeting.setForeground(new Color(226, 232, 240));
        docGreeting.setBorder(new EmptyBorder(0, 0, 25, 0));
        docGreeting.setAlignmentX(Component.LEFT_ALIGNMENT);

        topSidebar.add(logoLabel);
        topSidebar.add(roleBadge);
        topSidebar.add(docGreeting);

        JButton scheduleBtn = new JButton("جدول مواعيد المرضى");
        scheduleBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        scheduleBtn.setForeground(Color.WHITE);
        scheduleBtn.setBackground(new Color(14, 116, 144));
        scheduleBtn.setMaximumSize(new Dimension(220, 44));
        scheduleBtn.setBorderPainted(false);
        scheduleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        scheduleBtn.addActionListener(e -> loadDoctorAppointments());

        topSidebar.add(scheduleBtn);
        sidebar.add(topSidebar, BorderLayout.NORTH);

        JButton logoutBtn = new JButton("تسجيل الخروج");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(153, 27, 27));
        logoutBtn.setMaximumSize(new Dimension(220, 44));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame(appointmentService).setVisible(true);
        });
        sidebar.add(logoutBtn, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);

        // ================= 2. منطقة المحتوى الرئيسية =================
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(new Color(241, 245, 249));
        mainArea.setBorder(new EmptyBorder(25, 30, 25, 30));

        // الرأس وشريط البحث برمز QR أو اسم المريض
        JPanel headerPanel = new JPanel(new BorderLayout(0, 15));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel titleLbl = new JLabel("مواعيد المرضى المحجوزة في العيادة", SwingConstants.RIGHT);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLbl.setForeground(new Color(15, 23, 42));

        statsLabel = new JLabel("إجمالي الحجوزات: 0");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(new Color(14, 116, 144));

        titleRow.add(titleLbl, BorderLayout.EAST);
        titleRow.add(statsLabel, BorderLayout.WEST);
        headerPanel.add(titleRow, BorderLayout.NORTH);

        JPanel filterBar = new JPanel(new BorderLayout(12, 0));
        filterBar.setOpaque(false);

        searchQrField = new JTextField();
        searchQrField.setPreferredSize(new Dimension(0, 42));
        searchQrField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchQrField.putClientProperty("JTextField.placeholderText", "ابحث باسم المريض، رقم هاتفه، أو رمز تذكرة QR...");
        searchQrField.putClientProperty("JComponent.roundRect", true);

        JButton refreshBtn = new JButton("تحديث القائمة");
        refreshBtn.setPreferredSize(new Dimension(140, 42));
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshBtn.setBackground(new Color(30, 64, 110));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadDoctorAppointments());

        filterBar.add(searchQrField, BorderLayout.CENTER);
        filterBar.add(refreshBtn, BorderLayout.WEST);
        headerPanel.add(filterBar, BorderLayout.SOUTH);

        mainArea.add(headerPanel, BorderLayout.NORTH);

        appointmentsContainer = new JPanel();
        appointmentsContainer.setLayout(new BoxLayout(appointmentsContainer, BoxLayout.Y_AXIS));
        appointmentsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(appointmentsContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainArea.add(scrollPane, BorderLayout.CENTER);

        add(mainArea, BorderLayout.CENTER);

        searchQrField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { renderFilteredList(); }
            @Override public void removeUpdate(DocumentEvent e) { renderFilteredList(); }
            @Override public void changedUpdate(DocumentEvent e) { renderFilteredList(); }
        });

        loadDoctorAppointments();
    }

    // جلب مواعيد هذا الطبيب مع بيانات المرضى من قاعدة البيانات
    private void loadDoctorAppointments() {
        allDoctorAppts.clear();
        String sql = "SELECT a.id, a.date_time, a.status, a.qr_code, p.name AS patient_name, p.phone AS patient_phone "
                   + "FROM appointments a "
                   + "JOIN patients p ON a.patient_id = p.id "
                   + "WHERE a.doctor_id = ? "
                   + "ORDER BY a.date_time ASC";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, currentDoctor.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DoctorApptItem item = new DoctorApptItem();
                item.apptId = rs.getLong("id");
                item.patientName = rs.getString("patient_name");
                item.patientPhone = rs.getString("patient_phone");
                item.dateTime = rs.getString("date_time");
                item.status = rs.getString("status");
                item.qrCode = rs.getString("qr_code");
                allDoctorAppts.add(item);
            }
        } catch (SQLException e) {
            System.out.println("Error loading doctor schedule: " + e.getMessage());
        }

        statsLabel.setText("إجمالي الحجوزات: " + allDoctorAppts.size());
        renderFilteredList();
    }

    private void renderFilteredList() {
        String query = searchQrField.getText().trim().toLowerCase();
        appointmentsContainer.removeAll();
        int shown = 0;

        for (DoctorApptItem item : allDoctorAppts) {
            boolean matches = item.patientName.toLowerCase().contains(query)
                           || item.patientPhone.contains(query)
                           || item.qrCode.toLowerCase().contains(query);

            if (matches) {
                appointmentsContainer.add(createAppointmentRow(item));
                appointmentsContainer.add(Box.createRigidArea(new Dimension(0, 12)));
                shown++;
            }
        }

        if (shown == 0) {
            JLabel emptyLbl = new JLabel("لا توجد مواعيد مطابقة للعرض حالياً.");
            emptyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            emptyLbl.setForeground(new Color(100, 116, 139));
            appointmentsContainer.add(emptyLbl);
        }

        appointmentsContainer.revalidate();
        appointmentsContainer.repaint();
    }

    private JPanel createAppointmentRow(DoctorApptItem item) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        String statusArabic;
        String statusColor;
        if ("COMPLETED".equalsIgnoreCase(item.status)) {
            statusArabic = "تم الفحص";
            statusColor = "#15803d";
        } else if ("CANCELLED".equalsIgnoreCase(item.status)) {
            statusArabic = "ملغى";
            statusColor = "#b91c1c";
        } else {
            statusArabic = "مؤكد - بانتظار المريض";
            statusColor = "#0e7490";
        }

        JLabel infoLbl = new JLabel("<html>"
                + "<div style='font-size:13px;'><b>المريض:</b> " + item.patientName
                + " &nbsp;|&nbsp; <b>الهاتف:</b> " + item.patientPhone + "</div>"
                + "<div style='margin-top:5px;'><b>الموعد:</b> " + item.dateTime
                + " &nbsp;|&nbsp; <b>رمز QR:</b> <span style='font-family:Consolas;'>" + item.qrCode + "</span></div>"
                + "<div style='margin-top:5px;'><b>الحالة:</b> <span style='color:" + statusColor + "; font-weight:bold;'>" + statusArabic + "</span></div>"
                + "</html>");
        infoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        row.add(infoLbl, BorderLayout.CENTER);

        // أزرار التحكم بالموعد للطبيب
        if ("CONFIRMED".equalsIgnoreCase(item.status)) {
            JPanel actionsPanel = new JPanel(new GridLayout(2, 1, 0, 8));
            actionsPanel.setOpaque(false);

            JButton completeBtn = new JButton("تأكيد الحضور والفحص");
            completeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            completeBtn.setBackground(new Color(22, 163, 74));
            completeBtn.setForeground(Color.WHITE);
            completeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            completeBtn.addActionListener(e -> {
                appointmentService.updateAppointmentStatusByDoctor(item.apptId, currentDoctor.getId(), "COMPLETED");
                loadDoctorAppointments();
            });

            JButton cancelBtn = new JButton("إلغاء الموعد");
            cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            cancelBtn.setBackground(new Color(220, 38, 38));
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cancelBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this, "هل تريد إلغاء موعد المريض " + item.patientName + "؟", "تأكيد الإلغاء", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    appointmentService.updateAppointmentStatusByDoctor(item.apptId, currentDoctor.getId(), "CANCELLED");
                    loadDoctorAppointments();
                }
            });

            actionsPanel.add(completeBtn);
            actionsPanel.add(cancelBtn);
            row.add(actionsPanel, BorderLayout.WEST);
        }

        return row;
    }
}