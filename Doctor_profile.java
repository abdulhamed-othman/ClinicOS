public class Doctor_profile {
    private Long id;
    private Long userId;
    private String specialization;
    private Double consultationFee;
    private String bio;

    public Doctor_profile() {
    }
    public Doctor_profile(Long id, Long userId, String specialization, Double consultationFee, String bio) {
        this.id = id;
        this.userId = userId;
        this.specialization = specialization;
        this.consultationFee = consultationFee;
        this.bio = bio;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(Double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}