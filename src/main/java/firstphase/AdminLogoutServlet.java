package firstphase;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * AdminLogoutServlet - Clears Mission Master administrator session.
 */
@WebServlet("/AdminLogoutServlet")
public class AdminLogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processAdminLogout(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processAdminLogout(request, response);
    }

    private void processAdminLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("adminId");
            session.removeAttribute("adminUsername");
            session.removeAttribute("adminRole");
            session.invalidate();
        }

        String msg = URLEncoder.encode("Mission Master logged out safely.", StandardCharsets.UTF_8.toString());
        response.sendRedirect(request.getContextPath() + "/admin-login.jsp?message=" + msg);
    }
}
