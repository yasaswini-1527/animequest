package firstphase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
 * DBConnection - Singleton-style Database Connection Factory and Auto-Initializer.
 * Manages JDBC connections for AnimeQuest and automatically initializes the database,
 * tables, admin account, and starter missions.
 */
public class DBConnection {

    // Database connection constants
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String DB_NAME = "codequest_db";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/" + DB_NAME + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    
    // Change the MySQL password according to your local MySQL configuration.
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "vinoothna@10";

    // Track initialization state
    private static volatile boolean initialized = false;

    // Load JDBC Driver statically
    static {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("[AnimeQuest] MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    /**
     * Private constructor to enforce utility pattern.
     */
    private DBConnection() {}

    /**
     * Obtains a fresh JDBC Connection for the caller.
     * Ensures database is initialized on the first invocation.
     *
     * @return fresh java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            synchronized (DBConnection.class) {
                if (!initialized) {
                    initializeDatabase();
                    initialized = true;
                }
            }
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Automatically creates the database, required tables, default admin,
     * and starter missions if they do not exist.
     */
    public static synchronized void initializeDatabase() {
        System.out.println("[AnimeQuest] Starting database auto-initialization...");

        // Step 1: Create database if not exists
        try (Connection rootConn = DriverManager.getConnection(BASE_URL, DB_USER, DB_PASSWORD);
             Statement stmt = rootConn.createStatement()) {
            
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            System.out.println("[AnimeQuest] Database '" + DB_NAME + "' verified/created successfully.");
        } catch (SQLException e) {
            System.err.println("[AnimeQuest] Error connecting to MySQL server root: " + e.getMessage());
            // Continue attempt with direct DB_URL in case root user is already scoped
        }

        // Step 2: Create tables and seed initial data
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Table 1: admins
            String createAdminsTable = "CREATE TABLE IF NOT EXISTS admins ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT, "
                    + "username VARCHAR(50) UNIQUE NOT NULL, "
                    + "password VARCHAR(255) NOT NULL"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.executeUpdate(createAdminsTable);

            // Table 2: users
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
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
            String createMissionsTable = "CREATE TABLE IF NOT EXISTS missions ("
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
                    + "FOREIGN KEY (created_by) REFERENCES admins(id) ON DELETE SET NULL"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.executeUpdate(createMissionsTable);

            // Table 4: mission_submissions
            String createSubmissionsTable = "CREATE TABLE IF NOT EXISTS mission_submissions ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT, "
                    + "user_id INT NOT NULL, "
                    + "mission_id INT NOT NULL, "
                    + "answer VARCHAR(255) NOT NULL, "
                    + "is_correct BOOLEAN DEFAULT FALSE, "
                    + "completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (mission_id) REFERENCES missions(id) ON DELETE CASCADE, "
                    + "UNIQUE KEY unique_user_mission (user_id, mission_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.executeUpdate(createSubmissionsTable);

            System.out.println("[AnimeQuest] All 4 database tables verified/created successfully.");

            // Step 3: Seed default administrator if not present
            seedDefaultAdmin(conn);

            // Step 4: Seed starter programming missions if missions table is empty
            seedStarterMissions(conn);

        } catch (SQLException e) {
            System.err.println("[AnimeQuest] Error during database initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Seeds the default Mission Master (admin) account if none exists.
     * Default username: admin, Default password: admin123
     */
    private static void seedDefaultAdmin(Connection conn) {
        String checkAdminSql = "SELECT COUNT(*) FROM admins WHERE username = 'admin'";
        String insertAdminSql = "INSERT INTO admins (username, password) VALUES (?, ?)";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkAdminSql);
             ResultSet rs = checkStmt.executeQuery()) {

            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertAdminSql)) {
                    insertStmt.setString(1, "admin");
                    insertStmt.setString(2, hashPassword("admin123"));
                    insertStmt.executeUpdate();
                    System.out.println("[AnimeQuest] Default Mission Master 'admin' created successfully.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnimeQuest] Error seeding default admin: " + e.getMessage());
        }
    }

    /**
     * Seeds five starter programming missions if the missions table is empty.
     */
    private static void seedStarterMissions(Connection conn) {
        String checkMissionsSql = "SELECT COUNT(*) FROM missions";
        String getAdminIdSql = "SELECT id FROM admins WHERE username = 'admin' LIMIT 1";
        String insertMissionSql = "INSERT INTO missions (title, description, category, difficulty, "
                + "option_a, option_b, option_c, option_d, correct_answer, xp_reward, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkMissionsSql);
             ResultSet rs = checkStmt.executeQuery()) {

            if (rs.next() && rs.getInt(1) == 0) {
                int adminId = 1;
                try (PreparedStatement adminStmt = conn.prepareStatement(getAdminIdSql);
                     ResultSet adminRs = adminStmt.executeQuery()) {
                    if (adminRs.next()) {
                        adminId = adminRs.getInt("id");
                    }
                }

                // Starter missions array
                String[][] starterMissions = {
                    {
                        "Java Encapsulation Principle",
                        "In Java Object-Oriented Programming, which access modifier is primarily used to restrict direct access to class fields, enforcing encapsulation via getter and setter methods?",
                        "Java",
                        "Easy",
                        "public",
                        "private",
                        "protected",
                        "static",
                        "private",
                        "50"
                    },
                    {
                        "Java Class Inheritance Syntax",
                        "Which keyword is used in Java by a subclass to inherit the instance variables and accessible methods of a superclass?",
                        "Java",
                        "Medium",
                        "implements",
                        "inherits",
                        "extends",
                        "superclass",
                        "extends",
                        "75"
                    },
                    {
                        "SQL Unique Row Selection",
                        "Which SQL clause is used inside a standard SELECT query to eliminate duplicate rows from the returned result set?",
                        "SQL",
                        "Easy",
                        "UNIQUE",
                        "DISTINCT",
                        "GROUP ONLY",
                        "FILTER DUPES",
                        "DISTINCT",
                        "50"
                    },
                    {
                        "SQL Primary Key Constraints",
                        "Which combination of fundamental constraints is automatically enforced on any column defined as a PRIMARY KEY in relational databases?",
                        "SQL",
                        "Medium",
                        "NOT NULL and UNIQUE",
                        "CHECK and FOREIGN KEY",
                        "DEFAULT and AUTO_INCREMENT",
                        "NULLABLE and INDEXED",
                        "NOT NULL and UNIQUE",
                        "75"
                    },
                    {
                        "Array Search Time Complexity",
                        "What is the worst-case time complexity of searching for an element in an unsorted contiguous array of size n using linear search?",
                        "DSA",
                        "Hard",
                        "O(1)",
                        "O(log n)",
                        "O(n)",
                        "O(n log n)",
                        "O(n)",
                        "100"
                    }
                };

                try (PreparedStatement insertStmt = conn.prepareStatement(insertMissionSql)) {
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
                    System.out.println("[AnimeQuest] 5 starter programming missions successfully seeded.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnimeQuest] Error seeding starter missions: " + e.getMessage());
        }
    }

    // =========================================================================
    // PROGRESSION AND RANK SYSTEM UTILITIES
    // =========================================================================

    /**
     * Calculates warrior level based on total XP (500 cumulative XP per level).
     * Level 1: 0 - 499 XP
     * Level 2: 500 - 999 XP
     * ...
     * Level 8: 3500+ XP (Maximum rank: Grandmaster)
     *
     * @param totalXp total experience points
     * @return calculated warrior level between 1 and 8
     */
    public static int calculateLevel(int totalXp) {
        if (totalXp < 0) totalXp = 0;
        int level = (totalXp / 500) + 1;
        if (level > 8) level = 8;
        return level;
    }

    /**
     * Returns the anime warrior rank title corresponding to the player level.
     *
     * @param level warrior level (1 to 8)
     * @return rank title string
     */
    public static String getRankTitle(int level) {
        switch (level) {
            case 1: return "Rookie Ninja";
            case 2: return "Genin Warrior";
            case 3: return "Chunin Warrior";
            case 4: return "Elite Shinobi";
            case 5: return "Shadow Warrior";
            case 6: return "Legendary Warrior";
            case 7: return "Anime Legend";
            case 8: return "Grandmaster";
            default:
                if (level > 8) return "Grandmaster";
                return "Rookie Ninja";
        }
    }

    /**
     * Returns the XP required to reach the next warrior rank.
     * For max level 8, returns 3500.
     */
    public static int getNextLevelThreshold(int level) {
        if (level >= 8) return 3500;
        return level * 500;
    }

    /**
     * Returns the base XP required for the current level.
     */
    public static int getCurrentLevelFloor(int level) {
        if (level <= 1) return 0;
        if (level > 8) return 3500;
        return (level - 1) * 500;
    }

    // =========================================================================
    // SECURITY: PASSWORD HASHING UTILITIES (PBKDF2)
    // =========================================================================

    /**
     * Hashes a plain-text password using standard Java PBKDF2 with HMAC-SHA256.
     *
     * @param password plain-text password
     * @return saltHex:hashHex string representation
     */
    public static String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 10000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();

            return toHex(salt) + ":" + toHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password with PBKDF2: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies a candidate password against the stored PBKDF2 hash.
     * Includes a secure fallback to direct equality if legacy plain text is present.
     *
     * @param candidatePassword user entered password
     * @param storedHash stored password representation
     * @return true if matches, false otherwise
     */
    public static boolean verifyPassword(String candidatePassword, String storedHash) {
        if (candidatePassword == null || storedHash == null) {
            return false;
        }
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                // Fail-safe check for unhashed passwords in legacy test data
                return storedHash.equals(candidatePassword);
            }
            byte[] salt = fromHex(parts[0]);
            byte[] expectedHash = fromHex(parts[1]);

            KeySpec spec = new PBEKeySpec(candidatePassword.toCharArray(), salt, 10000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] candidateHash = factory.generateSecret(spec).getEncoded();

            return MessageDigest.isEqual(expectedHash, candidateHash);
        } catch (Exception e) {
            System.err.println("[AnimeQuest] Password verification failed: " + e.getMessage());
            return false;
        }
    }

    // Hex converter helpers
    private static String toHex(byte[] array) {
        StringBuilder sb = new StringBuilder(array.length * 2);
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
