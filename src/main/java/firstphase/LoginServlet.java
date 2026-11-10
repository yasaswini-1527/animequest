package firstphase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * LoginServlet - Handles student warrior authentication, password verification,
 * session creation, and "Remember Me" cookie management.
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Direct GET requests to login.jsp
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String usernameOrEmail = request.getParameter("usernameOrEmail");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");

        if (usernameOrEmail != null) usernameOrEmail = usernameOrEmail.trim();

        // Validation
        if (usernameOrEmail == null || usernameOrEmail.isEmpty() ||
            password == null || password.isEmpty()) {
            
            request.setAttribute("errorMessage", "Both username/email and password are required to enter the adventure.");
            request.setAttribute("enteredUser", usernameOrEmail);
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        String querySql = "SELECT id, username, email, password, total_xp, player_level FROM users WHERE username = ? OR email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(querySql)) {

            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail.toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    String username = rs.getString("username");
                    String storedHash = rs.getString("password");

                    // Verify password with PBKDF2
                    if (DBConnection.verifyPassword(password, storedHash)) {
                        
                        // Authentication success: Invalidate any old session and start fresh
                        HttpSession oldSession = request.getSession(false);
                        if (oldSession != null) {
                            oldSession.invalidate();
                        }

                        HttpSession session = request.getSession(true);
                        session.setAttribute("userId", userId);
                        session.setAttribute("username", username);
                        session.setAttribute("userRole", "STUDENT");

                        // Manage "Remember Me" cookie
                        String cookiePath = request.getContextPath().isEmpty() ? "/" : request.getContextPath();
                        if ("on".equalsIgnoreCase(rememberMe) || "true".equalsIgnoreCase(rememberMe)) {
                            Cookie userCookie = new Cookie("rememberedUser", username);
                            userCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
                            userCookie.setPath(cookiePath);
                            response.addCookie(userCookie);
                        } else {
                            // Clear existing remember cookie if unchecked
                            Cookie clearCookie = new Cookie("rememberedUser", "");
                            clearCookie.setMaxAge(0);
                            clearCookie.setPath(cookiePath);
                            response.addCookie(clearCookie);
                        }

                        // Redirect to student dashboard
                        response.sendRedirect(request.getContextPath() + "/DashboardServlet");
                        return;

                    } else {
                        request.setAttribute("errorMessage", "Incorrect password. Check your credentials and try again.");
                        request.setAttribute("enteredUser", usernameOrEmail);
                        request.getRequestDispatcher("login.jsp").forward(request, response);
                        return;
                    }
                } else {
                    request.setAttribute("errorMessage", "No warrior account found with that username or email.");
                    request.setAttribute("enteredUser", usernameOrEmail);
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                    return;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Database connection error during authentication.");
            request.setAttribute("enteredUser", usernameOrEmail);
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}
