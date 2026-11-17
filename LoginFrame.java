import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.FlatLightLaf;

public class LoginFrame extends JFrame {

    private JTextField phoneField;
    private JPasswordField passwordField;
    private AppointmentService appointmentService;

    public LoginFrame(AppointmentService service) {
        this.appointmentService = service;

        // 1. إعدادات النافذة الأساسية
        setTitle("ClinicOS - نظام إدارة وحجز المواعيد الطبية");
        java.net.URL iconURL = getClass().getResource("/logo.png");
        if (iconURL != null) {
            setIconImage(new ImageIcon(iconURL).getImage());
        } else {
            setIconImage(new ImageIcon("logo.png").getImage());
        }
        setSize(820, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // توسيط النافذة في منتصف الشاشة
        setResizable(false);
        setLayout(new BorderLayout());

        // 2. القسم الأيسر: الهوية البصرية الكحلية (Brand Sidebar)
        JPanel brandPanel = new JPanel();
        brandPanel.setPreferredSize(new Dimension(320, 500));
        brandPanel.setBackground(new Color(26, 54, 93)); // كحلي فخم (#1A365D)
        brandPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 20, 8, 20);

        JLabel iconLabel = new JLabel("🏥");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        brandPanel.add(iconLabel, gbc);

        gbc.gridy++;
        JLabel brandTitle = new JLabel("ClinicOS");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 34));
        brandTitle.setForeground(Color.WHITE);
        brandPanel.add(brandTitle, gbc);

        gbc.gridy++;
        JLabel brandSubtitle = new JLabel("Smart Medical Appointment System");
        brandSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        brandSubtitle.setForeground(new Color(148, 163, 184));
        brandPanel.add(brandSubtitle, gbc);

        add(brandPanel, BorderLayout.WEST);

        // 3. القسم الأيمن: نموذج الدخول (Form Panel)
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(null);

        JLabel welcomeLabel = new JLabel("مرحباً بك في نظام العيادات");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLabel.setForeground(new Color(15, 23, 42));
        welcomeLabel.setBounds(60, 45, 380, 35);
        formPanel.add(welcomeLabel);

        JLabel hintLabel = new JLabel("سجّل دخولك لحجز موعدك الطبي أو إدارة مواعيدك");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hintLabel.setForeground(new Color(100, 116, 139));
        hintLabel.setBounds(60, 80, 380, 25);
        formPanel.add(hintLabel);

        // حقل رقم الهاتف
        JLabel phoneLabel = new JLabel("رقم الهاتف:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        phoneLabel.setForeground(new Color(51, 65, 85));
        phoneLabel.setBounds(60, 130, 360, 22);
        formPanel.add(phoneLabel);

        phoneField = new JTextField();
        phoneField.setBounds(60, 155, 360, 40);
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phoneField.putClientProperty("JTextField.placeholderText", "أدخل رقم الهاتف (مثال: 0981416222)");
        phoneField.putClientProperty("JComponent.roundRect", true);
        formPanel.add(phoneField);

        // حقل كلمة المرور
        JLabel passLabel = new JLabel("كلمة المرور:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLabel.setForeground(new Color(51, 65, 85));
        passLabel.setBounds(60, 210, 360, 22);
        formPanel.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(60, 235, 360, 40);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.putClientProperty("JTextField.placeholderText", "أدخل كلمة المرور");
        passwordField.putClientProperty("JComponent.roundRect", true);
        formPanel.add(passwordField);

        // زر تسجيل الدخول الرئيسي
        JButton loginButton = new JButton("تسجيل الدخول");
        loginButton.setBounds(60, 300, 360, 44);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginButton.setBackground(new Color(14, 116, 144)); // فيروزي غامق فخم
        loginButton.setForeground(Color.WHITE);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.putClientProperty("JButton.buttonType", "roundRect");
        formPanel.add(loginButton);

        // زر إنشاء حساب جديد
        JButton registerButton = new JButton("ليس لديك حساب؟ إنشاء حساب مريض جديد");
        registerButton.setBounds(60, 360, 360, 36);
        registerButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registerButton.setForeground(new Color(14, 116, 144));
        registerButton.setContentAreaFilled(false);
        registerButton.setBorderPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formPanel.add(registerButton);

        // حدث الضغط على تسجيل الدخول
        loginButton.addActionListener(e -> {
            String phone = phoneField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (phone.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "الرجاء إدخال رقم الهاتف وكلمة المرور!", "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }

      User loggedPatient = appointmentService.loginPatient(phone, password);
if (loggedPatient != null) {
    dispose();
    new DashboardFrame(loggedPatient, appointmentService).setVisible(true);
    return;
}

User loggedDoctor = appointmentService.loginDoctor(phone, password);
if (loggedDoctor != null) {
    dispose();
    new DoctorDashboardFrame(loggedDoctor, appointmentService).setVisible(true);
    return;
}

JOptionPane.showMessageDialog(this, "رقم الهاتف أو كلمة المرور غير صحيحة!", "خطأ في الدخول", JOptionPane.ERROR_MESSAGE);
        });

        // حدث الضغط على إنشاء حساب جديد
        registerButton.addActionListener(e -> openQuickRegisterDialog());

        add(formPanel, BorderLayout.CENTER);
    }

    // نافذة منبثقة أنيقة لتسجيل مريض جديد
    private void openQuickRegisterDialog() {
        JTextField nameInput = new JTextField();
        JTextField phoneInput = new JTextField();
        JTextField emailInput = new JTextField();
        JPasswordField passInput = new JPasswordField();

        Object[] fields = {
            "الاسم الكامل:", nameInput,
            "رقم الهاتف:", phoneInput,
            "البريد الإلكتروني:", emailInput,
            "كلمة المرور:", passInput
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "إنشاء حساب مريض جديد", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameInput.getText().trim();
            String phone = phoneInput.getText().trim();
            String email = emailInput.getText().trim();
            String pass = new String(passInput.getPassword()).trim();

            if (name.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "يرجى تعبئة الاسم ورقم الهاتف وكلمة المرور!", "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User newUser = appointmentService.registerPatient(name, phone, email, pass);
            if (newUser != null) {
                JOptionPane.showMessageDialog(this, "تم إنشاء الحساب بنجاح! يمكنك الآن تسجيل الدخول.", "تم التسجيل", JOptionPane.INFORMATION_MESSAGE);
                phoneField.setText(phone);
            } else {
                JOptionPane.showMessageDialog(this, "حدث خطأ أثناء التسجيل (قد يكون الرقم مسجلاً مسبقاً).", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        // تفعيل الثيم العصري FlatLaf
        try {
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("Button.arc", 12);
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        // تهيئة قاعدة البيانات والخدمات
        DatabaseManager.initializeDatabase();
        AppointmentService service = new AppointmentService();

        // ضمان وجود الطبيبين الأساسيين
        service.registerDoctor("Dr. Samer Ali", "0933111222", "samer@clinic.com", "pass1",
                "Cardiology", 50000.0, "Consultant Cardiologist & Heart Specialist",
                "Sun, Tue, Thu", "09:00 AM", "03:00 PM");

        service.registerDoctor("Dr. Nour Hasan", "0955333444", "nour@clinic.com", "pass2",
                "Dentistry", 40000.0, "Dental Surgeon & Orthodontics",
                "Mon, Wed, Sat", "10:00 AM", "04:00 PM");

        SwingUtilities.invokeLater(() -> {
            new LoginFrame(service).setVisible(true);
        });
    }
}