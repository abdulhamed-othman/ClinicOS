public class Appointment {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private String appointmentDateTime; // مثال: "2026-11-10 10:30"
    private String status;              // "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED"
    private String qrCode;              // نص التشفير أو رابط التحقق الخاص برمز QR
    public Appointment() {
    }
    public Appointment(Long id, Long patientId, Long doctorId, String appointmentDateTime, String status, String qrCode) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status;
        this.qrCode = qrCode;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(String appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}