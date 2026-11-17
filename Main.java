import java.util.Scanner;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();

        AppointmentService service = new AppointmentService();
        Scanner scanner = new Scanner(System.in);

        // إضافة أطباء افتراضيين مع التخصص وسعر الكشفية وساعات وأيام الدوام
        service.registerDoctor("Dr. Samer Ali", "0933111222", "samer@clinic.com", "pass1", 
                               "Cardiology", 50000.0, "Consultant Cardiologist & Heart Specialist", 
                               "Sun, Tue, Thu", "09:00 AM", "03:00 PM");

        service.registerDoctor("Dr. Nour Hasan", "0955333444", "nour@clinic.com", "pass2", 
                               "Dentistry", 40000.0, "Dental Surgeon & Orthodontics", 
                               "Mon, Wed, Sat", "10:00 AM", "04:00 PM");

        User currentPatient = null;
        boolean running = true;

        while (running) {
            System.out.println("\n------------------------------------");
            System.out.println("      CLINIC APPOINTMENT SYSTEM     ");
            if (currentPatient != null) {
                System.out.println(" Logged in as: " + currentPatient.getfull_name() + " (ID: " + currentPatient.getId() + ")");
            } else {
                System.out.println(" Status: Not logged in");
            }
            System.out.println("------------------------------------");
            System.out.println("Select an option:");
            System.out.println("1. Register New Patient");
            System.out.println("2. Login Patient");
            System.out.println("3. View Available Doctors & Schedules");
            System.out.println("4. Book an Appointment");
            System.out.println("5. View My Booked Appointments");
            System.out.println("6. Cancel an Appointment");
            System.out.println("7. Logout");
            System.out.println("8. Exit");
            System.out.print("Enter your choice (1-8): ");

            int choice = -1;
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();
            } else {
                scanner.nextLine();
                System.out.println("Invalid input! Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter Patient Full Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Phone Number: ");
                    String phone = scanner.nextLine();
                    System.out.print("Enter Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter Password: ");
                    String password = scanner.nextLine();

                    User newPatient = service.registerPatient(name, phone, email, password);
                    if (newPatient != null) {
                        currentPatient = newPatient;
                        System.out.println("SUCCESS: Registered and logged in as " + currentPatient.getfull_name() + " (ID: " + currentPatient.getId() + ")");
                    }
                    break;

                case 2:
                    System.out.print("Enter Registered Phone Number: ");
                    String loginPhone = scanner.nextLine();
                    System.out.print("Enter Password: ");
                    String loginPass = scanner.nextLine();

                    User loggedIn = service.loginPatient(loginPhone, loginPass);
                    if (loggedIn != null) {
                        currentPatient = loggedIn;
                        System.out.println("SUCCESS: Welcome back, " + currentPatient.getfull_name() + "!");
                    } else {
                        System.out.println("ERROR: Invalid phone number or password!");
                    }
                    break;

                case 3:
                    service.printAvailableDoctorsDetailed();
                    break;

                case 4:
                    if (currentPatient == null) {
                        System.out.println("WARNING: Please register or login first before booking!");
                        break;
                    }
                    System.out.print("Enter Doctor ID: ");
                    long doctorId = scanner.nextLong();
                    scanner.nextLine();
                    System.out.print("Enter Appointment Date/Time (e.g. 2026-11-15 10:30 AM): ");
                    String dateTime = scanner.nextLine();

                    Appointment apt = service.bookAppointment(currentPatient.getId(), doctorId, dateTime);
                    if (apt != null) {
                        System.out.println("RECEIPT: Appointment ID: " + apt.getId());
                        System.out.println("QR Security Code: " + apt.getQrCode());
                    }
                    break;

                case 5:
                    if (currentPatient == null) {
                        System.out.println("WARNING: Please register or login first to view your appointments!");
                        break;
                    }
                    List<Appointment> myApts = service.getAppointmentsByPatient(currentPatient.getId());
                    System.out.println("\n--- My Booked Appointments ---");
                    if (myApts.isEmpty()) {
                        System.out.println("You have no appointments booked yet.");
                    } else {
                        for (Appointment a : myApts) {
                            System.out.println("Receipt ID: " + a.getId() + " | Doctor ID: " + a.getDoctorId() + " | Time: " + a.getAppointmentDateTime() + " | Status: " + a.getStatus());
                        }
                    }
                    break;

                case 6:
                    if (currentPatient == null) {
                        System.out.println("WARNING: Please register or login first to cancel an appointment!");
                        break;
                    }
                    System.out.print("Enter Appointment Receipt ID to cancel: ");
                    long aptId = scanner.nextLong();
                    scanner.nextLine();

                    service.cancelAppointment(aptId, currentPatient.getId());
                    break;

                case 7:
                    if (currentPatient != null) {
                        System.out.println("Patient " + currentPatient.getfull_name() + " logged out successfully.");
                        currentPatient = null;
                    } else {
                        System.out.println("No patient is currently logged in.");
                    }
                    break;

                case 8:
                    running = false;
                    System.out.println("Exiting system. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option! Please choose between 1 and 8.");
            }
        }
        scanner.close();
    }
}