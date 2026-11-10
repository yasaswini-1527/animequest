package firstphase;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * ViewMissionsServlet - Retrieves all programming missions for administrative management.
 */
@WebServlet("/ViewMissionsServlet")
public class ViewMissionsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect(request.getContextPath() + "/admin-login.jsp?error="
                    + URLEncoder.encode("Mission Master login required.", StandardCharsets.UTF_8.toString()));
            return;
        }

        List<Mission> missionList = new ArrayList<>();
        String query = "SELECT id, title, description, category, difficulty, "
                + "option_a, option_b, option_c, option_d, xp_reward FROM missions ORDER BY id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Mission m = new Mission();
                m.setId(rs.getInt("id"));
                m.setTitle(rs.getString("title"));
                m.setDescription(rs.getString("description"));
                m.setCategory(rs.getString("category"));
                m.setDifficulty(rs.getString("difficulty"));
                m.setOptionA(rs.getString("option_a"));
                m.setOptionB(rs.getString("option_b"));
                m.setOptionC(rs.getString("option_c"));
                m.setOptionD(rs.getString("option_d"));
                m.setXpReward(rs.getInt("xp_reward"));
                missionList.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error retrieving missions from database.");
        }

        request.setAttribute("missionList", missionList);
        request.getRequestDispatcher("view-missions.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
