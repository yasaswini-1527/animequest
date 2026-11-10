package firstphase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * AdminLoginServlet - Mission Master authentication.
 * Explicitly demonstrates the usage of ServletConfig and ServletContext.
 */
@WebServlet(
    urlPatterns = {"/AdminLoginServlet"},
    initParams = {
        @WebInitParam(name = "adminRoleTitle", value = "Mission Master")
    }
)
public class AdminLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private String servletRoleTitle;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Demonstrating ServletConfig: retrieve init parameter defined for this specific servlet
        this.servletRoleTitle = config.getInitParameter("adminRoleTitle");
        if (this.servletRoleTitle == null) {
            this.servletRoleTitle = "Mission Master";
        }
        
        // Demonstrating ServletContext: retrieve application-wide context parameter
        ServletContext context = config.getServletContext();
        String appName = context.getInitParameter("appName");
        System.out.println("[AnimeQuest] AdminLoginServlet initialized for: " + appName + " (" + this.servletRoleTitle + ")");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Expose ServletConfig and ServletContext data to the request for demonstration
        request.setAttribute("configRoleTitle", this.servletRoleTitle);
        request.setAttribute("contextAppName", getServletContext().getInitParameter("appName"));
        
        request.getRequestDispatcher("admin-login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username != null) username = username.trim();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("errorMessage", "Mission Master credentials are required.");
            request.setAttribute("enteredAdminUser", username);
            request.setAttribute("configRoleTitle", this.servletRoleTitle);
            request.getRequestDispatcher("admin-login.jsp").forward(request, response);
            return;
        }

        String adminQuery = "SELECT id, username, password FROM admins WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(adminQuery)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int adminId = rs.getInt("id");
                    String adminUser = rs.getString("username");
                    String storedHash = rs.getString("password");

                    if (DBConnection.verifyPassword(password, storedHash)) {
                        // Invalidate any previous session
                        HttpSession oldSession = request.getSession(false);
                        if (oldSession != null) {
                            oldSession.invalidate();
                        }

                        // Create new session specifically for admin
                        HttpSession session = request.getSession(true);
                        session.setAttribute("adminId", adminId);
                        session.setAttribute("adminUsername", adminUser);
                        session.setAttribute("adminRole", "ADMIN");

                        // Redirect to Admin Dashboard
                        response.sendRedirect(request.getContextPath() + "/admin-dashboard.jsp");
                        return;

                    } else {
                        request.setAttribute("errorMessage", "Access Denied: Invalid Mission Master credentials.");
                        request.setAttribute("enteredAdminUser", username);
                        request.setAttribute("configRoleTitle", this.servletRoleTitle);
                        request.getRequestDispatcher("admin-login.jsp").forward(request, response);
                        return;
                    }
                } else {
                    request.setAttribute("errorMessage", "Access Denied: Mission Master account not found.");
                    request.setAttribute("enteredAdminUser", username);
                    request.setAttribute("configRoleTitle", this.servletRoleTitle);
                    request.getRequestDispatcher("admin-login.jsp").forward(request, response);
                    return;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Database connection error during administrator authentication.");
            request.setAttribute("enteredAdminUser", username);
            request.setAttribute("configRoleTitle", this.servletRoleTitle);
            request.getRequestDispatcher("admin-login.jsp").forward(request, response);
        }
    }
}
