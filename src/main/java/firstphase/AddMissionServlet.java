package firstphase;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * AddMissionServlet - Administrator mission creation handler.
 * Only authenticated Mission Masters can access and create missions.
 */
@WebServlet("/AddMissionServlet")
public class AddMissionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect(request.getContextPath() + "/admin-login.jsp?error="
                    + URLEncoder.encode("Please log in as Mission Master to access this area.", StandardCharsets.UTF_8.toString()));
            return;
        }

        request.getRequestDispatcher("add-mission.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect(request.getContextPath() + "/admin-login.jsp?error="
                    + URLEncoder.encode("Unauthorized access. Admin login required.", StandardCharsets.UTF_8.toString()));
            return;
        }

        int adminId = (Integer) session.getAttribute("adminId");

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String category = request.getParameter("category");
        String difficulty = request.getParameter("difficulty");
        String optionA = request.getParameter("option_a");
        String optionB = request.getParameter("option_b");
        String optionC = request.getParameter("option_c");
        String optionD = request.getParameter("option_d");
        String correctAnswer = request.getParameter("correct_answer");
        String xpRewardStr = request.getParameter("xp_reward");

        // Trim values
        if (title != null) title = title.trim();
        if (description != null) description = description.trim();
        if (category != null) category = category.trim();
        if (difficulty != null) difficulty = difficulty.trim();
        if (optionA != null) optionA = optionA.trim();
        if (optionB != null) optionB = optionB.trim();
        if (optionC != null) optionC = optionC.trim();
        if (optionD != null) optionD = optionD.trim();
        if (correctAnswer != null) correctAnswer = correctAnswer.trim();
        if (xpRewardStr != null) xpRewardStr = xpRewardStr.trim();

        // Validation: Required fields
        if (title == null || title.isEmpty() ||
            description == null || description.isEmpty() ||
            category == null || category.isEmpty() ||
            difficulty == null || difficulty.isEmpty() ||
            optionA == null || optionA.isEmpty() ||
            optionB == null || optionB.isEmpty() ||
            optionC == null || optionC.isEmpty() ||
            optionD == null || optionD.isEmpty() ||
            correctAnswer == null || correctAnswer.isEmpty() ||
            xpRewardStr == null || xpRewardStr.isEmpty()) {

            request.setAttribute("errorMessage", "All mission parameters are mandatory.");
            preserveFormFields(request, title, description, category, difficulty, optionA, optionB, optionC, optionD, correctAnswer, xpRewardStr);
            request.getRequestDispatcher("add-mission.jsp").forward(request, response);
            return;
        }

        // Validate XP reward
        int xpReward = 50;
        try {
            xpReward = Integer.parseInt(xpRewardStr);
            if (xpReward <= 0 || xpReward > 1000) {
                request.setAttribute("errorMessage", "XP reward must be a positive number between 1 and 1000.");
                preserveFormFields(request, title, description, category, difficulty, optionA, optionB, optionC, optionD, correctAnswer, xpRewardStr);
                request.getRequestDispatcher("add-mission.jsp").forward(request, response);
                return;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Invalid XP reward value. Please enter a valid integer.");
            preserveFormFields(request, title, description, category, difficulty, optionA, optionB, optionC, optionD, correctAnswer, xpRewardStr);
            request.getRequestDispatcher("add-mission.jsp").forward(request, response);
            return;
        }

        // Validate that correct answer belongs to the four options
        if (!correctAnswer.equals(optionA) &&
            !correctAnswer.equals(optionB) &&
            !correctAnswer.equals(optionC) &&
            !correctAnswer.equals(optionD)) {
            
            request.setAttribute("errorMessage", "The specified correct answer must exactly match one of the four options (A, B, C, or D).");
            preserveFormFields(request, title, description, category, difficulty, optionA, optionB, optionC, optionD, correctAnswer, xpRewardStr);
            request.getRequestDispatcher("add-mission.jsp").forward(request, response);
            return;
        }

        // Insert mission into database
        String insertSql = "INSERT INTO missions (title, description, category, difficulty, "
                + "option_a, option_b, option_c, option_d, correct_answer, xp_reward, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, category);
            stmt.setString(4, difficulty);
            stmt.setString(5, optionA);
            stmt.setString(6, optionB);
            stmt.setString(7, optionC);
            stmt.setString(8, optionD);
            stmt.setString(9, correctAnswer);
            stmt.setInt(10, xpReward);
            stmt.setInt(11, adminId);

            stmt.executeUpdate();

            String successMsg = URLEncoder.encode("Mission '" + title + "' was successfully deployed to the Mission Board!", StandardCharsets.UTF_8.toString());
            response.sendRedirect(request.getContextPath() + "/ViewMissionsServlet?success=" + successMsg);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Database error occurred while adding the mission: " + e.getMessage());
            preserveFormFields(request, title, description, category, difficulty, optionA, optionB, optionC, optionD, correctAnswer, xpRewardStr);
            request.getRequestDispatcher("add-mission.jsp").forward(request, response);
        }
    }

    private void preserveFormFields(HttpServletRequest request, String title, String description,
                                    String category, String difficulty, String optionA, String optionB,
                                    String optionC, String optionD, String correctAnswer, String xpReward) {
        request.setAttribute("fieldTitle", title);
        request.setAttribute("fieldDescription", description);
        request.setAttribute("fieldCategory", category);
        request.setAttribute("fieldDifficulty", difficulty);
        request.setAttribute("fieldOptionA", optionA);
        request.setAttribute("fieldOptionB", optionB);
        request.setAttribute("fieldOptionC", optionC);
        request.setAttribute("fieldOptionD", optionD);
        request.setAttribute("fieldCorrectAnswer", correctAnswer);
        request.setAttribute("fieldXpReward", xpReward);
    }
}
