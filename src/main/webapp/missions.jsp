<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, firstphase.Mission" %>
<%
    // Verify session
    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+log+in+to+view+the+Mission+Board.");
        return;
    }

    String username = (String) session.getAttribute("username");

    @SuppressWarnings("unchecked")
    List<Mission> missionList = (List<Mission>) request.getAttribute("missionList");
    String selectedCategory = (String) request.getAttribute("selectedCategory");
    if (selectedCategory == null) selectedCategory = "All";

    String errorParam = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mission Board – AnimeQuest</title>
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

            <nav>
                <ul class="nav-links">
                    <li><a href="DashboardServlet" class="nav-link">Dashboard</a></li>
                    <li><a href="MissionServlet" class="nav-link active">Mission Board</a></li>
                    <li><a href="LeaderboardServlet" class="nav-link">Leaderboard</a></li>
                </ul>
            </nav>

            <div class="nav-cta">
                <div class="user-pill">
                    <div class="user-avatar-small"><%= (username != null && !username.isEmpty()) ? username.substring(0, 1).toUpperCase() : "W" %></div>
                    <span class="user-pill-text"><%= username %></span>
                </div>
                <a href="LogoutServlet" class="btn btn-outline-danger btn-sm">Logout</a>
            </div>
        </div>
    </header>

    <!-- Main Container -->
    <main class="main-wrapper">
        <div style="margin-bottom: 2rem;">
            <h1 style="font-size: 2.2rem; font-weight: 800; color: var(--text-primary); margin-bottom: 0.5rem;">
                The AnimeQuest <span class="brand-text">Mission Board</span>
            </h1>
            <p style="color: var(--text-secondary); font-size: 1.05rem;">
                Select your programming mission, choose the correct solution, and earn XP to advance your warrior rank!
            </p>
        </div>

        <% if (errorParam != null && !errorParam.isEmpty()) { %>
            <div class="alert alert-error">
                <span>⚠️</span>
                <div><%= errorParam %></div>
            </div>
        <% } %>

        <!-- Category Filter Bar -->
        <div class="filter-bar">
            <span style="font-weight: 700; font-size: 0.9rem; color: var(--text-secondary); margin-right: 0.5rem;">Filter Category:</span>
            <a href="MissionServlet?category=All" class="filter-pill <%= "All".equalsIgnoreCase(selectedCategory) ? "active" : "" %>">All Missions</a>
            <a href="MissionServlet?category=Java" class="filter-pill <%= "Java".equalsIgnoreCase(selectedCategory) ? "active" : "" %>">☕ Java</a>
            <a href="MissionServlet?category=SQL" class="filter-pill <%= "SQL".equalsIgnoreCase(selectedCategory) ? "active" : "" %>">🗄️ SQL</a>
            <a href="MissionServlet?category=DSA" class="filter-pill <%= "DSA".equalsIgnoreCase(selectedCategory) ? "active" : "" %>">⚡ DSA</a>
        </div>

        <!-- Missions Grid -->
        <% if (missionList == null || missionList.isEmpty()) { %>
            <div class="card" style="text-align: center; padding: 3rem;">
                <div style="font-size: 3rem; margin-bottom: 0.75rem;">📜</div>
                <h3 style="font-size: 1.4rem; font-weight: 800;">No Missions Available</h3>
                <p style="color: var(--text-secondary); margin-top: 0.5rem;">There are currently no programming missions matching category "<%= selectedCategory %>". Check back soon!</p>
            </div>
        <% } else { %>
            <div class="missions-grid">
                <% for (Mission m : missionList) { 
                    boolean attempted = m.isAttempted();
                    Boolean userCorrect = m.getUserCorrect();
                    String cardClass = "";
                    if (attempted) {
                        cardClass = (Boolean.TRUE.equals(userCorrect)) ? "attempted-success" : "attempted-failed";
                    }
                    String diffBadge = "badge-easy";
                    if ("Medium".equalsIgnoreCase(m.getDifficulty())) diffBadge = "badge-medium";
                    else if ("Hard".equalsIgnoreCase(m.getDifficulty())) diffBadge = "badge-hard";
                %>
                    <div class="mission-card <%= cardClass %>">
                        <div>
                            <div class="mission-top-badges">
                                <div>
                                    <span class="badge badge-category"><%= m.getCategory() %></span>
                                    <span class="badge <%= diffBadge %>"><%= m.getDifficulty() %></span>
                                </div>
                                <span class="badge badge-xp">+<%= m.getXpReward() %> XP</span>
                            </div>

                            <h3 class="mission-title"><%= m.getTitle() %></h3>
                            <p class="mission-desc"><%= m.getDescription() %></p>
                        </div>

                        <div>
                            <% if (!attempted) { %>
                                <!-- Active Submission Form -->
                                <form action="SubmitAnswerServlet" method="post">
                                    <input type="hidden" name="mission_id" value="<%= m.getId() %>">

                                    <div class="options-group">
                                        <label class="option-item">
                                            <input type="radio" name="answer" value="<%= m.getOptionA() %>" required>
                                            <span><strong>A.</strong> <%= m.getOptionA() %></span>
                                        </label>
                                        <label class="option-item">
                                            <input type="radio" name="answer" value="<%= m.getOptionB() %>">
                                            <span><strong>B.</strong> <%= m.getOptionB() %></span>
                                        </label>
                                        <label class="option-item">
                                            <input type="radio" name="answer" value="<%= m.getOptionC() %>">
                                            <span><strong>C.</strong> <%= m.getOptionC() %></span>
                                        </label>
                                        <label class="option-item">
                                            <input type="radio" name="answer" value="<%= m.getOptionD() %>">
                                            <span><strong>D.</strong> <%= m.getOptionD() %></span>
                                        </label>
                                    </div>

                                    <button type="submit" class="btn btn-primary btn-block">
                                        Submit Mission Answer ⚔️
                                    </button>
                                </form>
                            <% } else { %>
                                <!-- Attempted Mission Display (Disabled options & status) -->
                                <div class="options-group">
                                    <div class="option-item disabled">
                                        <span><strong>A.</strong> <%= m.getOptionA() %></span>
                                    </div>
                                    <div class="option-item disabled">
                                        <span><strong>B.</strong> <%= m.getOptionB() %></span>
                                    </div>
                                    <div class="option-item disabled">
                                        <span><strong>C.</strong> <%= m.getOptionC() %></span>
                                    </div>
                                    <div class="option-item disabled">
                                        <span><strong>D.</strong> <%= m.getOptionD() %></span>
                                    </div>
                                </div>

                                <div style="margin-bottom: 0.75rem; font-size: 0.85rem; color: var(--text-secondary);">
                                    Your Submitted Answer: <strong style="color: var(--text-primary);"><%= m.getUserAnswer() %></strong>
                                </div>

                                <% if (Boolean.TRUE.equals(userCorrect)) { %>
                                    <div class="mission-status-pill status-completed">
                                        ✅ Mission Success &bull; +<%= m.getXpReward() %> XP Awarded
                                    </div>
                                <% } else { %>
                                    <div class="mission-status-pill status-failed">
                                        ❌ Mission Failed &bull; 0 XP Awarded
                                    </div>
                                <% } %>
                            <% } %>
                        </div>
                    </div>
                <% } %>
            </div>
        <% } %>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <div class="footer-inner">
            <div>AnimeQuest &bull; Gamified Programming Learning Adventure</div>
            <div class="sakura-tag">🌸 Knowledge conquered through disciplined trial</div>
        </div>
    </footer>
</body>
</html>
