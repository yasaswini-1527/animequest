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
import jakarta.servlet.http.HttpSession;

/**
 * SubmitAnswerServlet - Mission answer evaluation with transactional integrity.
 * Enforces one-attempt policy, evaluates answer against database record,
 * calculates XP and level progression, and updates warrior state safely.
 */
@WebServlet("/SubmitAnswerServlet")
public class SubmitAnswerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirection on accidental GET requests
        response.sendRedirect(request.getContextPath() + "/MissionServlet");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 1. Verify student authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error="
                    + URLEncoder.encode("Session expired. Please log in again.", StandardCharsets.UTF_8.toString()));
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        // 2 & 3. Validate mission ID and selected answer
        String missionIdStr = request.getParameter("mission_id");
        String selectedAnswer = request.getParameter("answer");

        if (missionIdStr == null || missionIdStr.trim().isEmpty() ||
            selectedAnswer == null || selectedAnswer.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/MissionServlet?error="
                    + URLEncoder.encode("Please select an answer before submitting the mission.", StandardCharsets.UTF_8.toString()));
            return;
        }

        int missionId;
        try {
            missionId = Integer.parseInt(missionIdStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/MissionServlet?error="
                    + URLEncoder.encode("Invalid mission identifier.", StandardCharsets.UTF_8.toString()));
            return;
        }

        selectedAnswer = selectedAnswer.trim();

        // Transaction variables
        boolean isCorrect = false;
        int xpReward = 0;
        int xpEarned = 0;
        String missionTitle = "";
        String correctAnswer = "";
        int previousLevel = 1;
        int previousTotalXp = 0;
        int updatedTotalXp = 0;
        int updatedLevel = 1;
        boolean levelUp = false;

        Connection conn = null;
        boolean initialAutoCommit = true;

        try {
            conn = DBConnection.getConnection();
            initialAutoCommit = conn.getAutoCommit();
            // Start atomic database transaction
            conn.setAutoCommit(false);

            // Step 4 & 5: Retrieve mission and correct answer
            String missionQuery = "SELECT title, correct_answer, xp_reward FROM missions WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(missionQuery)) {
                stmt.setInt(1, missionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        missionTitle = rs.getString("title");
                        correctAnswer = rs.getString("correct_answer");
                        xpReward = rs.getInt("xp_reward");
                    } else {
                        conn.rollback();
                        response.sendRedirect(request.getContextPath() + "/MissionServlet?error="
                                + URLEncoder.encode("The requested mission was not found.", StandardCharsets.UTF_8.toString()));
                        return;
                    }
                }
            }

            // Step 6: Check whether student already submitted an answer (prevent double submission)
            String checkSubmissionQuery = "SELECT id FROM mission_submissions WHERE user_id = ? AND mission_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkSubmissionQuery)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, missionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        conn.rollback();
                        response.sendRedirect(request.getContextPath() + "/MissionServlet?error="
                                + URLEncoder.encode("You have already submitted an answer for this mission. One attempt only!", StandardCharsets.UTF_8.toString()));
                        return;
                    }
                }
            }

            // Step 7: Compare submitted answer with correct answer
            isCorrect = selectedAnswer.equalsIgnoreCase(correctAnswer.trim());
            xpEarned = isCorrect ? xpReward : 0;

            // Step 8: Insert submission record
            String insertSubSql = "INSERT INTO mission_submissions (user_id, mission_id, answer, is_correct) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSubSql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, missionId);
                stmt.setString(3, selectedAnswer);
                stmt.setBoolean(4, isCorrect);
                stmt.executeUpdate();
            }

            // Step 9, 10 & 11: If correct, update total_xp and recalculate level
            // First fetch current XP and level
            String fetchUserSql = "SELECT total_xp, player_level FROM users WHERE id = ? FOR UPDATE";
            try (PreparedStatement stmt = conn.prepareStatement(fetchUserSql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        previousTotalXp = rs.getInt("total_xp");
                        previousLevel = rs.getInt("player_level");
                    }
                }
            }

            if (isCorrect) {
                updatedTotalXp = previousTotalXp + xpEarned;
                updatedLevel = DBConnection.calculateLevel(updatedTotalXp);
                levelUp = (updatedLevel > previousLevel);

                String updateUserSql = "UPDATE users SET total_xp = ?, player_level = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateUserSql)) {
                    stmt.setInt(1, updatedTotalXp);
                    stmt.setInt(2, updatedLevel);
                    stmt.setInt(3, userId);
                    stmt.executeUpdate();
                }
            } else {
                updatedTotalXp = previousTotalXp;
                updatedLevel = previousLevel;
                levelUp = false;
            }

            // Step 12: Commit atomic transaction
            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/MissionServlet?error="
                    + URLEncoder.encode("Transaction failed during answer submission: " + e.getMessage(), StandardCharsets.UTF_8.toString()));
            return;

        } finally {
            if (conn != null) {
                try {
                    // Restore original auto-commit state before closing
                    conn.setAutoCommit(initialAutoCommit);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        // Pass evaluation results to mission-result.jsp
        request.setAttribute("isCorrect", isCorrect);
        request.setAttribute("missionTitle", missionTitle);
        request.setAttribute("submittedAnswer", selectedAnswer);
        request.setAttribute("correctAnswer", correctAnswer);
        request.setAttribute("xpEarned", xpEarned);
        request.setAttribute("updatedTotalXp", updatedTotalXp);
        request.setAttribute("updatedLevel", updatedLevel);
        request.setAttribute("rankTitle", DBConnection.getRankTitle(updatedLevel));
        request.setAttribute("levelUp", levelUp);

        request.getRequestDispatcher("mission-result.jsp").forward(request, response);
    }
}
