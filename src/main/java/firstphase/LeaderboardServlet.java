package firstphase;

import java.io.IOException;
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
 * LeaderboardServlet - Global Warrior Rankings controller.
 * Ranks all registered student warriors dynamically by total_xp DESC.
 */
@WebServlet("/LeaderboardServlet")
public class LeaderboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<LeaderboardUser> leaderboardList = new ArrayList<>();

        String query = "SELECT u.id, u.username, u.total_xp, u.player_level, "
                + "COUNT(CASE WHEN ms.is_correct = TRUE THEN 1 END) AS completed_missions "
                + "FROM users u "
                + "LEFT JOIN mission_submissions ms ON u.id = ms.user_id "
                + "GROUP BY u.id, u.username, u.total_xp, u.player_level "
                + "ORDER BY u.total_xp DESC, completed_missions DESC, u.id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            int currentRank = 1;
            while (rs.next()) {
                LeaderboardUser user = new LeaderboardUser();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setTotalXp(rs.getInt("total_xp"));
                
                int level = rs.getInt("player_level");
                // dynamically recalculate to ensure rank accuracy
                level = DBConnection.calculateLevel(user.getTotalXp());
                user.setPlayerLevel(level);
                user.setRankTitle(DBConnection.getRankTitle(level));
                
                user.setCompletedMissions(rs.getInt("completed_missions"));
                user.setRank(currentRank++);

                leaderboardList.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error loading Global Warrior Rankings from the database.");
        }

        // Identify currently logged in student user (if any) to highlight row
        HttpSession session = request.getSession(false);
        Integer currentUserId = null;
        if (session != null && session.getAttribute("userId") != null) {
            currentUserId = (Integer) session.getAttribute("userId");
        }

        request.setAttribute("leaderboardList", leaderboardList);
        request.setAttribute("currentUserId", currentUserId);

        request.getRequestDispatcher("leaderboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
