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
 * MissionServlet - Student Mission Board controller.
 * Organizes programming missions into categories and displays status
 * without exposing the correct_answer field to students.
 */
@WebServlet("/MissionServlet")
public class MissionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error="
                    + URLEncoder.encode("Please log in to view the Mission Board.", StandardCharsets.UTF_8.toString()));
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String categoryFilter = request.getParameter("category");
        if (categoryFilter == null || categoryFilter.trim().isEmpty()) {
            categoryFilter = "All";
        } else {
            categoryFilter = categoryFilter.trim();
        }

        List<Mission> missionList = new ArrayList<>();

        // Base query joins with mission_submissions to determine whether this specific warrior already attempted
        // NOTE: Strictly omitting correct_answer for student security!
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT m.id, m.title, m.description, m.category, m.difficulty, ")
           .append("m.option_a, m.option_b, m.option_c, m.option_d, m.xp_reward, ")
           .append("ms.id AS sub_id, ms.is_correct, ms.answer AS user_answer ")
           .append("FROM missions m ")
           .append("LEFT JOIN mission_submissions ms ON m.id = ms.mission_id AND ms.user_id = ? ");

        boolean hasCategoryFilter = !categoryFilter.equalsIgnoreCase("All");
        if (hasCategoryFilter) {
            sql.append("WHERE m.category = ? ");
        }

        sql.append("ORDER BY m.id ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setInt(1, userId);
            if (hasCategoryFilter) {
                stmt.setString(2, categoryFilter);
            }

            try (ResultSet rs = stmt.executeQuery()) {
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

                    int subId = rs.getInt("sub_id");
                    if (subId > 0) {
                        m.setAttempted(true);
                        m.setUserCorrect(rs.getBoolean("is_correct"));
                        m.setUserAnswer(rs.getString("user_answer"));
                    } else {
                        m.setAttempted(false);
                        m.setUserCorrect(null);
                        m.setUserAnswer(null);
                    }

                    missionList.add(m);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error loading missions from the database.");
        }

        request.setAttribute("missionList", missionList);
        request.setAttribute("selectedCategory", categoryFilter);

        request.getRequestDispatcher("missions.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
