package firstphase;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * RegisterServlet - Handles student warrior registration, input validation,
 * uniqueness checks, password hashing, and user persistence.
 */
@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Direct GET requests to register.jsp
        request.getRequestDispatcher("register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Trim inputs
        if (username != null) username = username.trim();
        if (email != null) email = email.trim().toLowerCase();

        // Server-side validation
        if (username == null || username.isEmpty() ||
            email == null || email.isEmpty() ||
            password == null || password.isEmpty() ||
            confirmPassword == null || confirmPassword.isEmpty()) {
            
            request.setAttribute("errorMessage", "All warrior registration fields are mandatory.");
            request.setAttribute("enteredUsername", username);
            request.setAttribute("enteredEmail", email);
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        if (username.length() < 3 || username.length() > 50) {
            request.setAttribute("errorMessage", "Warrior username must be between 3 and 50 characters.");
            request.setAttribute("enteredUsername", username);
            request.setAttribute("enteredEmail", email);
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        // Email regex pattern check
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex)) {
            request.setAttribute("errorMessage", "Please provide a valid email address.");
            request.setAttribute("enteredUsername", username);
            request.setAttribute("enteredEmail", email);
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        if (password.length() < 6) {
            request.setAttribute("errorMessage", "Password must be at least 6 characters long.");
            request.setAttribute("enteredUsername", username);
            request.setAttribute("enteredEmail", email);
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Passwords do not match. Please verify.");
            request.setAttribute("enteredUsername", username);
            request.setAttribute("enteredEmail", email);
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        // Check uniqueness in database
        String checkUsernameSql = "SELECT id FROM users WHERE username = ?";
        String checkEmailSql = "SELECT id FROM users WHERE email = ?";
        String insertUserSql = "INSERT INTO users (username, email, password, total_xp, player_level) VALUES (?, ?, ?, 0, 1)";

        try (Connection conn = DBConnection.getConnection()) {

            // Check username uniqueness
            try (PreparedStatement checkUserStmt = conn.prepareStatement(checkUsernameSql)) {
                checkUserStmt.setString(1, username);
                try (ResultSet rs = checkUserStmt.executeQuery()) {
                    if (rs.next()) {
                        request.setAttribute("errorMessage", "The warrior username '" + username + "' is already taken. Choose another.");
                        request.setAttribute("enteredUsername", username);
                        request.setAttribute("enteredEmail", email);
                        request.getRequestDispatcher("register.jsp").forward(request, response);
                        return;
                    }
                }
            }

            // Check email uniqueness
            try (PreparedStatement checkEmailStmt = conn.prepareStatement(checkEmailSql)) {
                checkEmailStmt.setString(1, email);
                try (ResultSet rs = checkEmailStmt.executeQuery()) {
                    if (rs.next()) {
                        request.setAttribute("errorMessage", "Email is already registered. Please log in or use a different email.");
                        request.setAttribute("enteredUsername", username);
                        request.setAttribute("enteredEmail", email);
                        request.getRequestDispatcher("register.jsp").forward(request, response);
                        return;
                    }
                }
            }

            // Hash password securely with PBKDF2
            String hashedPassword = DBConnection.hashPassword(password);

            // Insert new student user
            try (PreparedStatement insertStmt = conn.prepareStatement(insertUserSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, email);
                insertStmt.setString(3, hashedPassword);
                insertStmt.executeUpdate();
            }

            // Redirect on success to prevent double submission
            String successMsg = URLEncoder.encode("Warrior registered successfully! Welcome Rookie Ninja, please log in.", StandardCharsets.UTF_8.toString());
            response.sendRedirect(request.getContextPath() + "/login.jsp?success=" + successMsg);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Database error occurred during registration. Please try again later.");
            request.setAttribute("enteredUsername", username);
            request.setAttribute("enteredEmail", email);
            request.getRequestDispatcher("register.jsp").forward(request, response);
        }
    }
}
