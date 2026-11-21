
package firstphase;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * DBConnection - Database Connection Factory and Auto-Initializer.
 * Connects AnimeQuest to TiDB Cloud.
 */
public class DBConnection {

    // =========================================================
    // TI DB CLOUD CONNECTION SETTINGS
    // =========================================================

    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static final String DB_NAME = "codequest_db";

    private static final String DB_URL =
            "jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/"
            + DB_NAME
            + "?sslMode=VERIFY_IDENTITY&serverTimezone=UTC";

    // These values must be configured as environment variables.
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static volatile boolean initialized = false;

    // Load JDBC Driver
    static {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("[AnimeQuest] MySQL JDBC Driver not found: "
                    + e.getMessage());
        }
    }

    private DBConnection() {
    }

    // =========================================================
    // DATABASE CONNECTION
    // =========================================================

    public static Connection getConnection() throws SQLException {

        if (DB_USER == null || DB_USER.isBlank()
                || DB_PASSWORD == null || DB_PASSWORD.isBlank()) {
            throw new SQLException(
                    "Database credentials are missing. "
                    + "Configure DB_USER and DB_PASSWORD environment variables."
            );
        }

        if (!initialized) {
            synchronized (DBConnection.class) {
                if (!initialized) {
                    initializeDatabase();

                    if (!initialized) {
                        throw new SQLException(
                                "AnimeQuest database initialization failed."
                        );
                    }
                }
            }
        }

        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // =========================================================
    // DATABASE INITIALIZATION
    // =========================================================

    public static synchronized void initializeDatabase() {

        System.out.println(
                "[AnimeQuest] Starting TiDB database initialization..."
        );

        if (DB_USER == null || DB_USER.isBlank()
                || DB_PASSWORD == null || DB_PASSWORD.isBlank()) {
            System.err.println(
                    "[AnimeQuest] DB_USER or DB_PASSWORD is missing."
            );
            return;
        }

        try (Connection conn =
                     DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Table 1: admins
            String createAdminsTable =
                    "CREATE TABLE IF NOT EXISTS admins ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT, "
                    + "username VARCHAR(50) UNIQUE NOT NULL, "
                    + "password VARCHAR(255) NOT NULL"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

            stmt.executeUpdate(createAdminsTable);

            // Table 2: users
            String createUsersTable =
                    "CREATE TABLE IF NOT EXISTS users ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT, "
                    + "username VARCHAR(50) UNIQUE NOT NULL, "
                    + "email VARCHAR(100) UNIQUE NOT NULL, "
                    + "password VARCHAR(255) NOT NULL, "
                    + "total_xp INT DEFAULT 0, "
                    + "player_level INT DEFAULT 1, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

            stmt.executeUpdate(createUsersTable);

            // Table 3: missions
            String createMissionsTable =
                    "CREATE TABLE IF NOT EXISTS missions ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT, "
                    + "title VARCHAR(100) NOT NULL, "
                    + "description TEXT NOT NULL, "
                    + "category VARCHAR(30) NOT NULL, "
                    + "difficulty VARCHAR(20) NOT NULL, "
                    + "option_a VARCHAR(255) NOT NULL, "
                    + "option_b VARCHAR(255) NOT NULL, "
                    + "option_c VARCHAR(255) NOT NULL, "
                    + "option_d VARCHAR(255) NOT NULL, "
                    + "correct_answer VARCHAR(255) NOT NULL, "
                    + "xp_reward INT DEFAULT 50, "
                    + "created_by INT, "
                    + "FOREIGN KEY (created_by) REFERENCES admins(id) "
                    + "ON DELETE SET NULL"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

            stmt.executeUpdate(createMissionsTable);

            // Table 4: mission_submissions
            String createSubmissionsTable =
                    "CREATE TABLE IF NOT EXISTS mission_submissions ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT, "
                    + "user_id INT NOT NULL, "
                    + "mission_id INT NOT NULL, "
                    + "answer VARCHAR(255) NOT NULL, "
                    + "is_correct BOOLEAN DEFAULT FALSE, "
                    + "completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (user_id) REFERENCES users(id) "
                    + "ON DELETE CASCADE, "
                    + "FOREIGN KEY (mission_id) REFERENCES missions(id) "
                    + "ON DELETE CASCADE, "
                    + "UNIQUE KEY unique_user_mission (user_id, mission_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

            stmt.executeUpdate(createSubmissionsTable);

            System.out.println(
                    "[AnimeQuest] All 4 database tables verified successfully."
            );

            // Create default admin only if absent
            seedDefaultAdmin(conn);

            // Create starter missions only if the table is empty
            seedStarterMissions(conn);

            initialized = true;

            System.out.println(
                    "[AnimeQuest] TiDB database initialization completed."
            );

        } catch (SQLException e) {
            initialized = false;

            System.err.println(
                    "[AnimeQuest] Database initialization failed: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // =========================================================
    // DEFAULT ADMIN
    // =========================================================

    private static void seedDefaultAdmin(Connection conn) {

        String checkAdminSql =
                "SELECT COUNT(*) FROM admins WHERE username = 'admin'";

        String insertAdminSql =
                "INSERT INTO admins (username, password) VALUES (?, ?)";

        try (PreparedStatement checkStmt =
                     conn.prepareStatement(checkAdminSql);
             ResultSet rs = checkStmt.executeQuery()) {

            if (rs.next() && rs.getInt(1) == 0) {

                try (PreparedStatement insertStmt =
                             conn.prepareStatement(insertAdminSql)) {

                    insertStmt.setString(1, "admin");
                    insertStmt.setString(2, hashPassword("admin123"));

                    insertStmt.executeUpdate();

                    System.out.println(
                            "[AnimeQuest] Default admin created successfully."
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "[AnimeQuest] Error seeding admin: " + e.getMessage()
            );
        }
    }

    // =========================================================
    // STARTER MISSIONS
    // =========================================================

    private static void seedStarterMissions(Connection conn) {

        String checkMissionsSql =
                "SELECT COUNT(*) FROM missions";

        String getAdminIdSql =
                "SELECT id FROM admins WHERE username = 'admin' LIMIT 1";

        String insertMissionSql =
                "INSERT INTO missions (title, description, category, difficulty, "
                + "option_a, option_b, option_c, option_d, correct_answer, "
                + "xp_reward, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement checkStmt =
                     conn.prepareStatement(checkMissionsSql);
             ResultSet rs = checkStmt.executeQuery()) {

            if (rs.next() && rs.getInt(1) == 0) {

                int adminId = 1;

                try (PreparedStatement adminStmt =
                             conn.prepareStatement(getAdminIdSql);
                     ResultSet adminRs = adminStmt.executeQuery()) {

                    if (adminRs.next()) {
                        adminId = adminRs.getInt("id");
                    }
                }

                String[][] starterMissions = {

                    {
                        "Java Encapsulation Principle",
                        "In Java Object-Oriented Programming, which access modifier is primarily used to restrict direct access to class fields, enforcing encapsulation via getter and setter methods?",
                        "Java", "Easy",
                        "public", "private", "protected", "static",
                        "private", "50"
                    },

                    {
                        "Java Class Inheritance Syntax",
                        "Which keyword is used in Java by a subclass to inherit the instance variables and accessible methods of a superclass?",
                        "Java", "Medium",
                        "implements", "inherits", "extends", "superclass",
                        "extends", "75"
                    },

                    {
                        "SQL Unique Row Selection",
                        "Which SQL clause is used inside a standard SELECT query to eliminate duplicate rows from the returned result set?",
                        "SQL", "Easy",
                        "UNIQUE", "DISTINCT", "GROUP ONLY", "FILTER DUPES",
                        "DISTINCT", "50"
                    },

                    {
                        "SQL Primary Key Constraints",
                        "Which combination of fundamental constraints is automatically enforced on any column defined as a PRIMARY KEY in relational databases?",
                        "SQL", "Medium",
                        "NOT NULL and UNIQUE",
                        "CHECK and FOREIGN KEY",
                        "DEFAULT and AUTO_INCREMENT",
                        "NULLABLE and INDEXED",
                        "NOT NULL and UNIQUE", "75"
                    },

                    {
                        "Array Search Time Complexity",
                        "What is the worst-case time complexity of searching for an element in an unsorted contiguous array of size n using linear search?",
                        "DSA", "Hard",
                        "O(1)", "O(log n)", "O(n)", "O(n log n)",
                        "O(n)", "100"
                    }
                };

                try (PreparedStatement insertStmt =
                             conn.prepareStatement(insertMissionSql)) {

                    for (String[] m : starterMissions) {

                        insertStmt.setString(1, m[0]);
                        insertStmt.setString(2, m[1]);
                        insertStmt.setString(3, m[2]);
                        insertStmt.setString(4, m[3]);
                        insertStmt.setString(5, m[4]);
                        insertStmt.setString(6, m[5]);
                        insertStmt.setString(7, m[6]);
                        insertStmt.setString(8, m[7]);
                        insertStmt.setString(9, m[8]);
                        insertStmt.setInt(10, Integer.parseInt(m[9]));
                        insertStmt.setInt(11, adminId);

                        insertStmt.addBatch();
                    }

                    insertStmt.executeBatch();

                    System.out.println(
                            "[AnimeQuest] 5 starter missions created."
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "[AnimeQuest] Error seeding missions: " + e.getMessage()
            );
        }
    }

    // =========================================================
    // XP AND RANK SYSTEM
    // =========================================================

    public static int calculateLevel(int totalXp) {

        if (totalXp < 0) {
            totalXp = 0;
        }

        int level = (totalXp / 500) + 1;

        if (level > 8) {
            level = 8;
        }

        return level;
    }

    public static String getRankTitle(int level) {

        switch (level) {
            case 1:
                return "Rookie Ninja";
            case 2:
                return "Genin Warrior";
            case 3:
                return "Chunin Warrior";
            case 4:
                return "Elite Shinobi";
            case 5:
                return "Shadow Warrior";
            case 6:
                return "Legendary Warrior";
            case 7:
                return "Anime Legend";
            case 8:
                return "Grandmaster";
            default:
                if (level > 8) {
                    return "Grandmaster";
                }
                return "Rookie Ninja";
        }
    }

    public static int getNextLevelThreshold(int level) {

        if (level >= 8) {
            return 3500;
        }

        return level * 500;
    }

    public static int getCurrentLevelFloor(int level) {

        if (level <= 1) {
            return 0;
        }

        if (level > 8) {
            return 3500;
        }

        return (level - 1) * 500;
    }

    // =========================================================
    // PASSWORD HASHING - PBKDF2
    // =========================================================

    public static String hashPassword(String password) {

        if (password == null) {
            throw new IllegalArgumentException(
                    "Password cannot be null"
            );
        }

        try {
            SecureRandom random = new SecureRandom();

            byte[] salt = new byte[16];
            random.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    10000,
                    256
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return toHex(salt) + ":" + toHex(hash);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error hashing password: " + e.getMessage(),
                    e
            );
        }
    }

    public static boolean verifyPassword(
            String candidatePassword,
            String storedHash) {

        if (candidatePassword == null || storedHash == null) {
            return false;
        }

        try {
            String[] parts = storedHash.split(":");

            if (parts.length != 2) {
                return storedHash.equals(candidatePassword);
            }

            byte[] salt = fromHex(parts[0]);
            byte[] expectedHash = fromHex(parts[1]);

            KeySpec spec = new PBEKeySpec(
                    candidatePassword.toCharArray(),
                    salt,
                    10000,
                    256
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            byte[] candidateHash =
                    factory.generateSecret(spec).getEncoded();

            return MessageDigest.isEqual(
                    expectedHash,
                    candidateHash
            );

        } catch (Exception e) {
            System.err.println(
                    "[AnimeQuest] Password verification failed: "
                    + e.getMessage()
            );

            return false;
        }
    }

    // =========================================================
    // HEX CONVERSION HELPERS
    // =========================================================

    private static String toHex(byte[] array) {

        StringBuilder sb =
                new StringBuilder(array.length * 2);

        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    private static byte[] fromHex(String hex) {

        int len = hex.length();

        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {

            data[i / 2] = (byte) (
                    (Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16)
            );
        }

        return data;
    }
}
