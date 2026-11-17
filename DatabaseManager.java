import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:clinic.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(URL);
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return conn;
    }

    public static void initializeDatabase() {
        String createPatientsTable = "CREATE TABLE IF NOT EXISTS patients ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "phone TEXT NOT NULL, "
                + "email TEXT, "
                + "password TEXT NOT NULL"
                + ");";

        String createDoctorsTable = "CREATE TABLE IF NOT EXISTS doctors ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "phone TEXT, "
                + "email TEXT, "
                + "password TEXT"
                + ");";

        String createDoctorProfilesTable = "CREATE TABLE IF NOT EXISTS doctor_profiles ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "doctor_id INTEGER NOT NULL, "
                + "specialization TEXT NOT NULL, "
                + "consultation_fee REAL NOT NULL, "
                + "bio TEXT, "
                + "FOREIGN KEY (doctor_id) REFERENCES doctors (id)"
                + ");";

        String createWorkingHoursTable = "CREATE TABLE IF NOT EXISTS working_hours ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "doctor_id INTEGER NOT NULL, "
                + "day_of_week TEXT NOT NULL, "
                + "start_time TEXT NOT NULL, "
                + "end_time TEXT NOT NULL, "
                + "FOREIGN KEY (doctor_id) REFERENCES doctors (id)"
                + ");";

        String createAppointmentsTable = "CREATE TABLE IF NOT EXISTS appointments ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "patient_id INTEGER NOT NULL, "
                + "doctor_id INTEGER NOT NULL, "
                + "date_time TEXT NOT NULL, "
                + "status TEXT NOT NULL, "
                + "qr_code TEXT, "
                + "FOREIGN KEY (patient_id) REFERENCES patients (id), "
                + "FOREIGN KEY (doctor_id) REFERENCES doctors (id)"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createPatientsTable);
            stmt.execute(createDoctorsTable);
            stmt.execute(createDoctorProfilesTable);
            stmt.execute(createWorkingHoursTable);
            stmt.execute(createAppointmentsTable);
            System.out.println("Database initialized successfully with all tables!");
            
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
        }
    }
}