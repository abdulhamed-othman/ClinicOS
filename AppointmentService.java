import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    // تسجيل طبيب مع منع التكرار برقم الهاتف وحفظ الملف وساعات الدوام
    public User registerDoctor(String name, String phone, String email, String password, 
                                String specialty, double fee, String bio, 
                                String days, String startTime, String endTime) {
        
        // التحقق إن كان الطبيب مسجلاً مسبقاً بنفس رقم الهاتف
        String checkSql = "SELECT id FROM doctors WHERE phone = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setString(1, phone);
            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next()) {
                long existingId = checkRs.getLong("id");
                return new User(existingId, name, phone, email, password, "DOCTOR");
            }

            // إدخال الطبيب الجديد إذا لم يكن موجوداً
            String sqlDoctor = "INSERT INTO doctors (name, phone, email, password) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDoctor, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.setString(2, phone);
                pstmt.setString(3, email);
                pstmt.setString(4, password);
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    long doctorId = rs.getLong(1);

                    // إضافة الملف المهني
                    String sqlProfile = "INSERT INTO doctor_profiles (doctor_id, specialization, consultation_fee, bio) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement profileStmt = conn.prepareStatement(sqlProfile)) {
                        profileStmt.setLong(1, doctorId);
                        profileStmt.setString(2, specialty);
                        profileStmt.setDouble(3, fee);
                        profileStmt.setString(4, bio);
                        profileStmt.executeUpdate();
                    }

                    // إضافة ساعات الدوام
                    String sqlHours = "INSERT INTO working_hours (doctor_id, day_of_week, start_time, end_time) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement hoursStmt = conn.prepareStatement(sqlHours)) {
                        hoursStmt.setLong(1, doctorId);
                        hoursStmt.setString(2, days);
                        hoursStmt.setString(3, startTime);
                        hoursStmt.setString(4, endTime);
                        hoursStmt.executeUpdate();
                    }

                    return new User(doctorId, name, phone, email, password, "DOCTOR");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error registering doctor: " + e.getMessage());
        }
        return null;
    }

    // تسجيل مريض
    public User registerPatient(String name, String phone, String email, String password) {
        String sql = "INSERT INTO patients (name, phone, email, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                return new User(id, name, phone, email, password, "PATIENT");
            }
        } catch (SQLException e) {
            System.out.println("Error registering patient: " + e.getMessage());
        }
        return null;
    }

    // تسجيل دخول المريض
    public User loginPatient(String phone, String password) {
        String sql = "SELECT * FROM patients WHERE phone = ? AND password = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("password"),
                        "PATIENT"
                );
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return null;
    }

    // استعراض تفاصيل الأطباء والتخصصات والدوام
    public void printAvailableDoctorsDetailed() {
        String sql = "SELECT d.id, d.name, d.phone, p.specialization, p.consultation_fee, p.bio, "
                   + "w.day_of_week, w.start_time, w.end_time "
                   + "FROM doctors d "
                   + "LEFT JOIN doctor_profiles p ON d.id = p.doctor_id "
                   + "LEFT JOIN working_hours w ON d.id = w.doctor_id";

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n================ AVAILABLE DOCTORS & SPECIALISTS ================");
            boolean found = false;
            while (rs.next()) {
                found = true;
                long id = rs.getLong("id");
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                String spec = rs.getString("specialization");
                double fee = rs.getDouble("consultation_fee");
                String bio = rs.getString("bio");
                String days = rs.getString("day_of_week");
                String start = rs.getString("start_time");
                String end = rs.getString("end_time");

                System.out.println("-----------------------------------------------------------------");
                System.out.println("Doctor ID: " + id + " | " + name + " (" + (spec != null ? spec : "General") + ")");
                System.out.println("Consultation Fee: " + fee + " SYP | Phone: " + phone);
                System.out.println("Working Schedule: " + (days != null ? days : "All Week") + " [" + start + " - " + end + "]");
                if (bio != null && !bio.isEmpty()) {
                    System.out.println("About: " + bio);
                }
            }
            if (!found) {
                System.out.println("No doctors registered yet.");
            }
            System.out.println("=================================================================\n");

        } catch (SQLException e) {
            System.out.println("Error fetching doctors details: " + e.getMessage());
        }
    }

    // حجز موعد والتحقق من التضارب
    public Appointment bookAppointment(Long patientId, Long doctorId, String dateTime) {
        String checkSql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND date_time = ? AND status != 'CANCELLED'";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setLong(1, doctorId);
            checkStmt.setString(2, dateTime);
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next() && checkRs.getInt(1) > 0) {
                System.out.println("The doctor already has an appointment at this time!");
                return null;
            }

            String insertSql = "INSERT INTO appointments (patient_id, doctor_id, date_time, status, qr_code) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                String qrCode = "QR-" + System.currentTimeMillis();
                String status = "CONFIRMED";

                insertStmt.setLong(1, patientId);
                insertStmt.setLong(2, doctorId);
                insertStmt.setString(3, dateTime);
                insertStmt.setString(4, status);
                insertStmt.setString(5, qrCode);
                insertStmt.executeUpdate();

                ResultSet rs = insertStmt.getGeneratedKeys();
                if (rs.next()) {
                    long id = rs.getLong(1);
                    System.out.println("Appointment booked successfully! QR Code: " + qrCode);
                    return new Appointment(id, patientId, doctorId, dateTime, status, qrCode);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error booking appointment: " + e.getMessage());
        }
        return null;
    }

    // استعراض مواعيد المريض
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE patient_id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new Appointment(
                        rs.getLong("id"),
                        rs.getLong("patient_id"),
                        rs.getLong("doctor_id"),
                        rs.getString("date_time"),
                        rs.getString("status"),
                        rs.getString("qr_code")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching patient appointments: " + e.getMessage());
        }
        return list;
    }

    // إلغاء موعد
    public boolean cancelAppointment(Long appointmentId, Long patientId) {
        String sql = "UPDATE appointments SET status = 'CANCELLED' WHERE id = ? AND patient_id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, appointmentId);
            pstmt.setLong(2, patientId);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Appointment #" + appointmentId + " has been successfully CANCELLED.");
                return true;
            } else {
                System.out.println("ERROR: Appointment not found or you don't have permission to cancel it.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error cancelling appointment: " + e.getMessage());
            return false;
        }
    }
    // 1. تسجيل دخول الطبيب
    public User loginDoctor(String phone, String password) {
        String sql = "SELECT * FROM doctors WHERE phone = ? AND password = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("password"),
                        "DOCTOR"
                );
            }
        } catch (SQLException e) {
            System.out.println("Doctor login error: " + e.getMessage());
        }
        return null;
    }

    // 2. تحديث حالة الموعد من قبل الطبيب (COMPLETED أو CANCELLED)
    public boolean updateAppointmentStatusByDoctor(Long appointmentId, Long doctorId, String newStatus) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ? AND doctor_id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setLong(2, appointmentId);
            pstmt.setLong(3, doctorId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating appointment status: " + e.getMessage());
            return false;
        }
    }
}