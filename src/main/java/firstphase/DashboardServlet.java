package firstphase;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * DashboardServlet - Student warrior dashboard controller.
 * Retrieves XP, rank, levels, mission statistics, and recent activity.
 */
@WebServlet("/DashboardServlet")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error="
                    + URLEncoder.encode("Please log in to enter your Warrior Dashboard.", StandardCharsets.UTF_8.toString()));
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        String userQuery = "SELECT id, username, email, total_xp, player_level FROM users WHERE id = ?";
        String statsQuery = "SELECT "
                + "COUNT(*) AS total_attempted, "
                + "SUM(CASE WHEN is_correct = TRUE THEN 1 ELSE 0 END) AS total_correct "
                + "FROM mission_submissions WHERE user_id = ?";
        String totalMissionsQuery = "SELECT COUNT(*) FROM missions";
        String recentActivityQuery = "SELECT m.title, m.category, m.xp_reward, ms.is_correct, ms.completed_at "
                + "FROM mission_submissions ms "
                + "JOIN missions m ON ms.mission_id = m.id "
                + "WHERE ms.user_id = ? "
                + "ORDER BY ms.completed_at DESC LIMIT 5";

        try (Connection conn = DBConnection.getConnection()) {

            // 1. Fetch User Profile
            String username = (String) session.getAttribute("username");
            String email = "";
            int totalXp = 0;
            int playerLevel = 1;

            try (PreparedStatement stmt = conn.prepareStatement(userQuery)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        username = rs.getString("username");
                        email = rs.getString("email");
                        totalXp = rs.getInt("total_xp");
                        playerLevel = rs.getInt("player_level");
                    }
                }
            }

            // Sync level if XP changed
            int calculatedLevel = DBConnection.calculateLevel(totalXp);
            if (calculatedLevel != playerLevel) {
                playerLevel = calculatedLevel;
                try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE users SET player_level = ? WHERE id = ?")) {
                    updateStmt.setInt(1, playerLevel);
                    updateStmt.setInt(2, userId);
                    updateStmt.executeUpdate();
                }
            }

            // 2. Fetch Submission Stats
            int totalAttempted = 0;
            int totalCorrect = 0;
            try (PreparedStatement stmt = conn.prepareStatement(statsQuery)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalAttempted = rs.getInt("total_attempted");
                        totalCorrect = rs.getInt("total_correct");
                    }
                }
            }

            int totalAvailableMissions = 0;
            try (PreparedStatement stmt = conn.prepareStatement(totalMissionsQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalAvailableMissions = rs.getInt(1);
                }
            }

            // 3. Calculate Rank, XP Progress, and Level Bounds
            String rankTitle = DBConnection.getRankTitle(playerLevel);
            int currentLevelFloor = DBConnection.getCurrentLevelFloor(playerLevel);
            int nextLevelThreshold = DBConnection.getNextLevelThreshold(playerLevel);

            int xpInCurrentLevel = totalXp - currentLevelFloor;
            int xpSpan = nextLevelThreshold - currentLevelFloor;
            int progressPercent = 0;
            if (playerLevel >= 8) {
                progressPercent = 100;
                xpInCurrentLevel = 500;
                xpSpan = 500;
            } else if (xpSpan > 0) {
                progressPercent = (int) Math.min(100, Math.max(0, ((double) xpInCurrentLevel / xpSpan) * 100));
            }

            // 4. Fetch Recent Activity
            List<Map<String, Object>> recentActivities = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(recentActivityQuery)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> act = new HashMap<>();
                        act.put("title", rs.getString("title"));
                        act.put("category", rs.getString("category"));
                        act.put("xpReward", rs.getInt("xp_reward"));
                        act.put("isCorrect", rs.getBoolean("is_correct"));
                        act.put("completedAt", rs.getTimestamp("completed_at"));
                        recentActivities.add(act);
                    }
                }
            }

            // Set request attributes
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("totalXp", totalXp);
            request.setAttribute("playerLevel", playerLevel);
            request.setAttribute("rankTitle", rankTitle);
            request.setAttribute("totalAttempted", totalAttempted);
            request.setAttribute("totalCorrect", totalCorrect);
            request.setAttribute("totalAvailableMissions", totalAvailableMissions);
            request.setAttribute("currentLevelFloor", currentLevelFloor);
            request.setAttribute("nextLevelThreshold", nextLevelThreshold);
            request.setAttribute("xpInCurrentLevel", xpInCurrentLevel);
            request.setAttribute("progressPercent", progressPercent);
            request.setAttribute("recentActivities", recentActivities);

            request.getRequestDispatcher("dashboard.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Unable to load dashboard data: " + e.getMessage());
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
