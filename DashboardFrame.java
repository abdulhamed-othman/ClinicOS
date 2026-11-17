import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class DashboardFrame extends JFrame {

    private User currentUser;
    private AppointmentService appointmentService;
    private JPanel contentArea;
    private CardLayout cardLayout;
    private JPanel appointmentsListPanel;

    private List<DoctorCardInfo> allDoctorsList = new ArrayList<>();
    private JPanel doctorsCardsGrid;
    private JTextField searchField;
    private JComboBox<String> specialtyFilterCombo;

    private static class DoctorCardInfo {
        long id;
        String name;
        String specialty;
        double fee;
        String bio;
        String days;
        String start;
        String end;
    }

    public DashboardFrame(User user, AppointmentService service) {
        this.currentUser = user;
        this.appointmentService = service;

        setTitle("ClinicOS - لوحة تحكم المريض");
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
        sidebar.setBackground(new Color(26, 54, 93));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(25, 18, 25, 18));

        JPanel topSidebar = new JPanel();
        topSidebar.setOpaque(false);
        topSidebar.setLayout(new BoxLayout(topSidebar, BoxLayout.Y_AXIS));

        JLabel logoLabel = new JLabel("ClinicOS");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userGreeting = new JLabel("أهلاً، " + currentUser.getfull_name());
        userGreeting.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userGreeting.setForeground(new Color(148, 163, 184));
        userGreeting.setBorder(new EmptyBorder(8, 0, 25, 0));
        userGreeting.setAlignmentX(Component.LEFT_ALIGNMENT);

        topSidebar.add(logoLabel);
        topSidebar.add(userGreeting);

        JButton doctorsNavBtn = createNavButton("الأطباء المتاحون", true);
        JButton myApptsNavBtn = createNavButton("مواعيدي المحجوزة", false);

        topSidebar.add(doctorsNavBtn);
        topSidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        topSidebar.add(myApptsNavBtn);

        sidebar.add(topSidebar, BorderLayout.NORTH);

        JButton logoutBtn = createNavButton("تسجيل الخروج", false);
        logoutBtn.setBackground(new Color(153, 27, 27));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame(appointmentService).setVisible(true);
        });
        sidebar.add(logoutBtn, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);

        // ================= 2. منطقة المحتوى المتغيرة =================
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(new Color(241, 245, 249));

        contentArea.add(buildDoctorsView(), "DOCTORS");
        contentArea.add(buildMyAppointmentsView(), "APPOINTMENTS");

        add(contentArea, BorderLayout.CENTER);

        doctorsNavBtn.addActionListener(e -> {
            styleActiveNav(doctorsNavBtn, myApptsNavBtn);
            cardLayout.show(contentArea, "DOCTORS");
        });

        myApptsNavBtn.addActionListener(e -> {
            styleActiveNav(myApptsNavBtn, doctorsNavBtn);
            refreshAppointmentsList();
            cardLayout.show(contentArea, "APPOINTMENTS");
        });
    }

    private List<DoctorCardInfo> fetchDoctorsFromDB() {
        List<DoctorCardInfo> doctors = new ArrayList<>();
        String sql = "SELECT d.id, d.name, p.specialization, p.consultation_fee, p.bio, "
                   + "w.day_of_week, w.start_time, w.end_time "
                   + "FROM doctors d "
                   + "LEFT JOIN doctor_profiles p ON d.id = p.doctor_id "
                   + "LEFT JOIN working_hours w ON d.id = w.doctor_id";

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                DoctorCardInfo info = new DoctorCardInfo();
                info.id = rs.getLong("id");
                info.name = rs.getString("name");
                info.specialty = rs.getString("specialization") != null ? rs.getString("specialization") : "General";
                info.fee = rs.getDouble("consultation_fee");
                info.bio = rs.getString("bio") != null ? rs.getString("bio") : "";
                info.days = rs.getString("day_of_week") != null ? rs.getString("day_of_week") : "All Week";
                info.start = rs.getString("start_time") != null ? rs.getString("start_time") : "09:00 AM";
                info.end = rs.getString("end_time") != null ? rs.getString("end_time") : "03:00 PM";
                doctors.add(info);
            }
        } catch (SQLException e) {
            System.out.println("Error loading doctors for UI: " + e.getMessage());
        }
        return doctors;
    }

    private JPanel buildDoctorsView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(241, 245, 249));
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel topSection = new JPanel(new BorderLayout(0, 15));
        topSection.setOpaque(false);
        topSection.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel header = new JLabel("الأطباء المتاحون للحجز", SwingConstants.RIGHT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(new Color(15, 23, 42));
        topSection.add(header, BorderLayout.NORTH);

        JPanel searchBarPanel = new JPanel(new BorderLayout(12, 0));
        searchBarPanel.setOpaque(false);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(0, 42));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.putClientProperty("JTextField.placeholderText", "ابحث عن طبيب بالاسم...");
        searchField.putClientProperty("JComponent.roundRect", true);

        allDoctorsList = fetchDoctorsFromDB();
        Set<String> specialties = new LinkedHashSet<>();
        specialties.add("جميع الاختصاصات");
        for (DoctorCardInfo d : allDoctorsList) {
            specialties.add(d.specialty);
        }

        specialtyFilterCombo = new JComboBox<>(specialties.toArray(new String[0]));
        specialtyFilterCombo.setPreferredSize(new Dimension(210, 42));
        specialtyFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        searchBarPanel.add(searchField, BorderLayout.CENTER);
        searchBarPanel.add(specialtyFilterCombo, BorderLayout.EAST);

        topSection.add(searchBarPanel, BorderLayout.SOUTH);
        panel.add(topSection, BorderLayout.NORTH);

        doctorsCardsGrid = new JPanel(new GridLayout(0, 2, 20, 20));
        doctorsCardsGrid.setOpaque(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterDoctors(); }
            @Override public void removeUpdate(DocumentEvent e) { filterDoctors(); }
            @Override public void changedUpdate(DocumentEvent e) { filterDoctors(); }
        });

        specialtyFilterCombo.addActionListener(e -> filterDoctors());

        filterDoctors();

        JScrollPane scrollPane = new JScrollPane(doctorsCardsGrid);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void filterDoctors() {
        String query = searchField.getText().trim().toLowerCase();
        String selectedSpec = (String) specialtyFilterCombo.getSelectedItem();

        doctorsCardsGrid.removeAll();
        int matchCount = 0;

        for (DoctorCardInfo doc : allDoctorsList) {
            boolean matchesName = doc.name.toLowerCase().contains(query) || doc.bio.toLowerCase().contains(query);
            boolean matchesSpec = selectedSpec == null || "جميع الاختصاصات".equals(selectedSpec) || doc.specialty.equalsIgnoreCase(selectedSpec);

            if (matchesName && matchesSpec) {
                doctorsCardsGrid.add(createDoctorCard(doc));
                matchCount++;
            }
        }

        if (matchCount == 0) {
            JLabel noResultLbl = new JLabel("لا يوجد أطباء مطابقون لبحثك.", SwingConstants.CENTER);
            noResultLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            noResultLbl.setForeground(new Color(100, 116, 139));
            doctorsCardsGrid.add(noResultLbl);
        }

        doctorsCardsGrid.revalidate();
        doctorsCardsGrid.repaint();
    }

    private JPanel createDoctorCard(DoctorCardInfo doc) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel nameLbl = new JLabel(doc.name);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 19));
        nameLbl.setForeground(new Color(15, 23, 42));

        JLabel specLbl = new JLabel("التخصص: " + doc.specialty);
        specLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        specLbl.setForeground(new Color(14, 116, 144));
        specLbl.setBorder(new EmptyBorder(6, 0, 4, 0));

        JLabel bioLbl = new JLabel(doc.bio);
        bioLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bioLbl.setForeground(new Color(100, 116, 139));

        JLabel feeLbl = new JLabel("الكشفية: " + doc.fee + " SYP");
        feeLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        feeLbl.setForeground(new Color(22, 101, 52));
        feeLbl.setBorder(new EmptyBorder(12, 0, 4, 0));

        JLabel scheduleLbl = new JLabel("الدوام: " + doc.days + " (" + doc.start + " - " + doc.end + ")");
        scheduleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        scheduleLbl.setForeground(new Color(51, 65, 85));

        infoPanel.add(nameLbl);
        infoPanel.add(specLbl);
        infoPanel.add(bioLbl);
        infoPanel.add(feeLbl);
        infoPanel.add(scheduleLbl);

        card.add(infoPanel, BorderLayout.CENTER);

        JButton bookBtn = new JButton("احجز موعداً الآن");
        bookBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bookBtn.setBackground(new Color(14, 116, 144));
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setPreferredSize(new Dimension(0, 40));
        bookBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bookBtn.putClientProperty("JButton.buttonType", "roundRect");

        bookBtn.addActionListener(e -> openBookingDialog(doc));

        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.setBorder(new EmptyBorder(15, 0, 0, 0));
        btnWrapper.add(bookBtn, BorderLayout.CENTER);

        card.add(btnWrapper, BorderLayout.SOUTH);
        return card;
    }

    private void openBookingDialog(DoctorCardInfo doc) {
        JTextField dateField = new JTextField("2026-11-15");
        String[] timeSlots = {"09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "12:00 PM", "01:00 PM", "02:00 PM"};
        JComboBox<String> timeCombo = new JComboBox<>(timeSlots);

        Object[] inputs = {
            "الطبيب المختار: " + doc.name,
            "تاريخ الموعد (YYYY-MM-DD):", dateField,
            "الساعة المتاحة:", timeCombo
        };

        int res = JOptionPane.showConfirmDialog(this, inputs, "تأكيد حجز موعد جديد", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String date = dateField.getText().trim();
            String time = (String) timeCombo.getSelectedItem();

            if (date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "الرجاء إدخال تاريخ الموعد!", "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String combinedDateTime = date + " " + time;
            Appointment booked = appointmentService.bookAppointment(currentUser.getId(), doc.id, combinedDateTime);

            if (booked != null) {
                showDigitalReceiptDialog(booked, doc);
            } else {
                JOptionPane.showMessageDialog(this, "عذراً! هذا الموعد محجوز مسبقاً لدى الطبيب، يرجى اختيار وقت آخر.", "تضارب في الموعد", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showDigitalReceiptDialog(Appointment appt, DoctorCardInfo doc) {
        JDialog receiptDialog = new JDialog(this, "إيصال تأكيد الحجز الرقمي", true);
        receiptDialog.setSize(400, 480);
        receiptDialog.setLocationRelativeTo(this);
        receiptDialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("تم تأكيد موعدك بنجاح");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(22, 101, 52));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel qrLabel = new JLabel(new ImageIcon(generateQRCodeImage(appt.getQrCode(), 170)));
        qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrLabel.setBorder(new EmptyBorder(15, 0, 15, 0));

        JLabel codeLbl = new JLabel("رمز التذكرة: " + appt.getQrCode());
        codeLbl.setFont(new Font("Consolas", Font.BOLD, 14));
        codeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel detailsLbl = new JLabel("<html><div style='text-align:center; margin-top:10px;'>"
                + "<b>المريض:</b> " + currentUser.getfull_name() + "<br>"
                + "<b>الطبيب:</b> " + doc.name + "<br>"
                + "<b>الموعد:</b> " + appt.getAppointmentDateTime() + "<br>"
                + "<b>الكشفية:</b> " + doc.fee + " SYP"
                + "</div></html>");
        detailsLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        detailsLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton closeBtn = new JButton("تم");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setBackground(new Color(14, 116, 144));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> receiptDialog.dispose());

        mainPanel.add(title);
        mainPanel.add(qrLabel);
        mainPanel.add(codeLbl);
        mainPanel.add(detailsLbl);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 18)));
        mainPanel.add(closeBtn);

        receiptDialog.add(mainPanel, BorderLayout.CENTER);
        receiptDialog.setVisible(true);
    }

    private BufferedImage generateQRCodeImage(String seedText, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        g.setColor(new Color(15, 23, 42));

        int grid = 21;
        int cell = size / grid;
        Random rand = new Random(seedText.hashCode());

        for (int r = 0; r < grid; r++) {
            for (int c = 0; c < grid; c++) {
                boolean isFinder = (r < 7 && c < 7) || (r < 7 && c >= grid - 7) || (r >= grid - 7 && c < 7);
                if (isFinder) {
                    boolean border = r == 0 || r == 6 || c == 0 || c == 6 || r == grid - 7 || r == grid - 1 || c == grid - 7 || c == grid - 1;
                    boolean center = (r >= 2 && r <= 4 && c >= 2 && c <= 4) || (r >= 2 && r <= 4 && c >= grid - 5 && c <= grid - 3) || (r >= grid - 5 && r <= grid - 3 && c >= 2 && c <= 4);
                    if (border || center) g.fillRect(c * cell, r * cell, cell, cell);
                } else if (rand.nextBoolean()) {
                    g.fillRect(c * cell, r * cell, cell, cell);
                }
            }
        }
        g.dispose();
        return img;
    }

    private JPanel buildMyAppointmentsView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(241, 245, 249));
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel header = new JLabel("مواعيدي المحجوزة ونشاطاتي", SwingConstants.RIGHT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(new Color(15, 23, 42));
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(header, BorderLayout.NORTH);

        appointmentsListPanel = new JPanel();
        appointmentsListPanel.setLayout(new BoxLayout(appointmentsListPanel, BoxLayout.Y_AXIS));
        appointmentsListPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(appointmentsListPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshAppointmentsList() {
        appointmentsListPanel.removeAll();
        List<Appointment> list = appointmentService.getAppointmentsByPatient(currentUser.getId());

        if (list.isEmpty()) {
            JLabel emptyLbl = new JLabel("لا توجد مواعيد محجوزة حالياً.");
            emptyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            emptyLbl.setForeground(new Color(100, 116, 139));
            appointmentsListPanel.add(emptyLbl);
        } else {
            for (Appointment appt : list) {
                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(Color.WHITE);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
                row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                        new EmptyBorder(15, 20, 15, 20)
                ));

                boolean isCancelled = "CANCELLED".equalsIgnoreCase(appt.getStatus());
                String statusBadge = isCancelled ? "ملغى" : "مؤكد";

                JLabel info = new JLabel("<html><b>رقم الطبيب:</b> #" + appt.getDoctorId()
                        + " &nbsp;|&nbsp; <b>التاريخ والوقت:</b> " + appt.getAppointmentDateTime()
                        + "<br><span style='color:gray;'>الحالة: " + statusBadge + " &nbsp;|&nbsp; الكود: " + appt.getQrCode() + "</span></html>");
                info.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                row.add(info, BorderLayout.CENTER);

                if (!isCancelled) {
                    JButton cancelBtn = new JButton("إلغاء الموعد");
                    cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    cancelBtn.setBackground(new Color(220, 38, 38));
                    cancelBtn.setForeground(Color.WHITE);
                    cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    cancelBtn.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this, "هل أنت متأكد من إلغاء هذا الموعد؟", "تأكيد الإلغاء", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            appointmentService.cancelAppointment(appt.getId(), currentUser.getId());
                            refreshAppointmentsList();
                        }
                    });
                    row.add(cancelBtn, BorderLayout.EAST);
                }

                appointmentsListPanel.add(row);
                appointmentsListPanel.add(Box.createRigidArea(new Dimension(0, 12)));
            }
        }
        appointmentsListPanel.revalidate();
        appointmentsListPanel.repaint();
    }

    private JButton createNavButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(active ? new Color(14, 116, 144) : new Color(30, 64, 110));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setMaximumSize(new Dimension(220, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
        return btn;
    }

    private void styleActiveNav(JButton activeBtn, JButton inactiveBtn) {
        activeBtn.setBackground(new Color(14, 116, 144));
        inactiveBtn.setBackground(new Color(30, 64, 110));
    }
}
