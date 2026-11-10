<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Verify session
    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Session+expired.");
        return;
    }

    Boolean isCorrectObj = (Boolean) request.getAttribute("isCorrect");
    boolean isCorrect = Boolean.TRUE.equals(isCorrectObj);

    String missionTitle = (String) request.getAttribute("missionTitle");
    String submittedAnswer = (String) request.getAttribute("submittedAnswer");
    String correctAnswer = (String) request.getAttribute("correctAnswer");
    Integer xpEarned = (Integer) request.getAttribute("xpEarned");
    Integer updatedTotalXp = (Integer) request.getAttribute("updatedTotalXp");
    Integer updatedLevel = (Integer) request.getAttribute("updatedLevel");
    String rankTitle = (String) request.getAttribute("rankTitle");
    Boolean levelUpObj = (Boolean) request.getAttribute("levelUp");
    boolean levelUp = Boolean.TRUE.equals(levelUpObj);

    if (missionTitle == null) missionTitle = "Programming Mission";
    if (xpEarned == null) xpEarned = 0;
    if (updatedTotalXp == null) updatedTotalXp = 0;
    if (updatedLevel == null) updatedLevel = 1;
    if (rankTitle == null) rankTitle = "Rookie Ninja";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mission Outcome – AnimeQuest</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="page-bg-pattern"></div>

    <!-- Navigation Bar -->
    <header class="navbar">
        <div class="nav-container">
            <a href="index.html" class="brand-logo">
                <div class="brand-icon">⚔️</div>
                <div>Anime<span class="brand-text">Quest</span></div>
            </a>
            <div class="nav-cta">
                <a href="DashboardServlet" class="btn btn-secondary btn-sm">Dashboard</a>
                <a href="MissionServlet" class="btn btn-primary btn-sm">Mission Board</a>
            </div>
        </div>
    </header>

    <main class="main-wrapper">
        <div class="result-card <%= isCorrect ? "success" : "failed" %>">
            
            <% if (isCorrect) { %>
                <!-- Celebratory Success Display -->
                <div class="result-badge-icon icon-success">🎉</div>
                <h1 class="result-heading" style="color: var(--success-color);">MISSION SUCCESS!</h1>
                <p class="result-sub">Brilliant execution, Warrior! Your programming logic prevailed.</p>

                <% if (levelUp) { %>
                    <div class="level-up-banner">
                        🎊 RANK ADVANCEMENT! You leveled up to Level <%= updatedLevel %> &bull; <%= rankTitle %>!
                    </div>
                <% } %>

                <table class="result-stats-table">
                    <tr>
                        <td>Mission Completed:</td>
                        <td><%= missionTitle %></td>
                    </tr>
                    <tr>
                        <td>Your Answer:</td>
                        <td><span style="color: var(--success-color);"><%= submittedAnswer %></span></td>
                    </tr>
                    <tr>
                        <td>XP Rewarded:</td>
                        <td><strong style="color: var(--accent-gold); font-size: 1.15rem;">+<%= xpEarned %> XP</strong></td>
                    </tr>
                    <tr>
                        <td>Updated Total XP:</td>
                        <td><%= updatedTotalXp %> XP</td>
                    </tr>
                    <tr>
                        <td>Current Warrior Rank:</td>
                        <td><span class="rank-pill">Level <%= updatedLevel %> &bull; <%= rankTitle %></span></td>
                    </tr>
                </table>

            <% } else { %>
                <!-- Mission Failed Display -->
                <div class="result-badge-icon icon-failed">💔</div>
                <h1 class="result-heading" style="color: var(--error-color);">MISSION FAILED</h1>
                <p class="result-sub">Do not lose heart, Warrior! True ninjas learn from every defeat.</p>

                <table class="result-stats-table">
                    <tr>
                        <td>Mission Attempted:</td>
                        <td><%= missionTitle %></td>
                    </tr>
                    <tr>
                        <td>Your Answer:</td>
                        <td><span style="color: var(--error-color);"><%= submittedAnswer %></span></td>
                    </tr>
                    <tr>
                        <td>Correct Solution:</td>
                        <td><strong style="color: var(--success-color);"><%= correctAnswer %></strong></td>
                    </tr>
                    <tr>
                        <td>XP Earned:</td>
                        <td>0 XP</td>
                    </tr>
                    <tr>
                        <td>Total XP Preserved:</td>
                        <td><%= updatedTotalXp %> XP</td>
                    </tr>
                </table>
            <% } %>

            <div style="display: flex; justify-content: center; gap: 1rem; flex-wrap: wrap; margin-top: 1.5rem;">
                <a href="MissionServlet" class="btn btn-primary btn-lg">Try Another Mission 📜</a>
                <a href="DashboardServlet" class="btn btn-secondary btn-lg">Warrior Dashboard 🏠</a>
                <a href="LeaderboardServlet" class="btn btn-gold btn-lg">Global Rankings 🏆</a>
            </div>
        </div>
    </main>

    <footer class="footer">
        <div class="footer-inner">
            <div>AnimeQuest &bull; Gamified Programming Learning Adventure</div>
            <div class="sakura-tag">🌸 Every completed mission builds your legacy</div>
        </div>
    </footer>
</body>
</html>
