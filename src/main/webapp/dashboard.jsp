<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>
<%
    // Verify session
    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+log+in+to+view+your+Warrior+Dashboard.");
        return;
    }

    String username = (String) request.getAttribute("username");
    if (username == null) username = (String) session.getAttribute("username");

    Integer totalXp = (Integer) request.getAttribute("totalXp");
    Integer playerLevel = (Integer) request.getAttribute("playerLevel");
    String rankTitle = (String) request.getAttribute("rankTitle");
    Integer totalAttempted = (Integer) request.getAttribute("totalAttempted");
    Integer totalCorrect = (Integer) request.getAttribute("totalCorrect");
    Integer totalAvailableMissions = (Integer) request.getAttribute("totalAvailableMissions");
    Integer currentLevelFloor = (Integer) request.getAttribute("currentLevelFloor");
    Integer nextLevelThreshold = (Integer) request.getAttribute("nextLevelThreshold");
    Integer xpInCurrentLevel = (Integer) request.getAttribute("xpInCurrentLevel");
    Integer progressPercent = (Integer) request.getAttribute("progressPercent");

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> recentActivities = (List<Map<String, Object>>) request.getAttribute("recentActivities");

    if (totalXp == null) totalXp = 0;
    if (playerLevel == null) playerLevel = 1;
    if (rankTitle == null) rankTitle = "Rookie Ninja";
    if (totalAttempted == null) totalAttempted = 0;
    if (totalCorrect == null) totalCorrect = 0;
    if (totalAvailableMissions == null) totalAvailableMissions = 0;
    if (xpInCurrentLevel == null) xpInCurrentLevel = 0;
    if (progressPercent == null) progressPercent = 0;
    if (nextLevelThreshold == null) nextLevelThreshold = 500;

    int successRate = 0;
    if (totalAttempted > 0) {
        successRate = (int) Math.round(((double) totalCorrect / totalAttempted) * 100);
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Warrior Dashboard – AnimeQuest</title>
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
                    <li><a href="DashboardServlet" class="nav-link active">Dashboard</a></li>
                    <li><a href="MissionServlet" class="nav-link">Mission Board</a></li>
                    <li><a href="LeaderboardServlet" class="nav-link">Leaderboard</a></li>
                </ul>
            </nav>

            <div class="nav-cta">
                <div class="user-pill">
                    <div class="user-avatar-small"><%= username.substring(0, 1).toUpperCase() %></div>
                    <span class="user-pill-text"><%= username %></span>
                </div>
                <a href="LogoutServlet" class="btn btn-outline-danger btn-sm">Logout 🚪</a>
            </div>
        </div>
    </header>

    <!-- Main Container -->
    <main class="main-wrapper">
        <!-- Dashboard Header / Welcome Profile -->
        <section class="dashboard-header">
            <div class="dashboard-user-info">
                <div class="warrior-avatar-large">
                    <% if (playerLevel >= 7) { %>👑<% } else if (playerLevel >= 4) { %>⚔️<% } else { %>🥷<% } %>
                </div>
                <div class="dashboard-user-meta">
                    <h1>Welcome back, <span class="brand-text"><%= username %></span>!</h1>
                    <div class="rank-pill">
                        <span>⭐</span> Level <%= playerLevel %> &bull; <%= rankTitle %>
                    </div>
                </div>
            </div>

            <div class="dashboard-actions">
                <a href="MissionServlet" class="btn btn-primary btn-md">Explore Missions 📜</a>
                <a href="LeaderboardServlet" class="btn btn-gold btn-md">Leaderboard 🏆</a>
            </div>
        </section>

        <!-- Stats Grid Cards -->
        <section class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon icon-purple">⚡</div>
                <div>
                    <div class="stat-value"><%= totalXp %></div>
                    <div class="stat-title">Total Experience (XP)</div>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon icon-pink">🎯</div>
                <div>
                    <div class="stat-value"><%= playerLevel %> / 8</div>
                    <div class="stat-title">Warrior Level</div>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon icon-purple">✅</div>
                <div>
                    <div class="stat-value"><%= totalCorrect %> / <%= totalAttempted %></div>
                    <div class="stat-title">Missions Succeeded</div>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon icon-gold">📊</div>
                <div>
                    <div class="stat-value"><%= successRate %>%</div>
                    <div class="stat-title">Mission Success Rate</div>
                </div>
            </div>
        </section>

        <!-- RPG Level & XP Progression Card -->
        <section class="progression-card">
            <div class="progress-header">
                <div>
                    <h2 class="progress-title">Level Progression &bull; <%= rankTitle %></h2>
                    <p style="font-size: 0.85rem; color: var(--text-secondary);">
                        <% if (playerLevel >= 8) { %>
                            Supreme Rank Achieved: You have attained the peak rank of Grandmaster!
                        <% } else { %>
                            Earn <%= (nextLevelThreshold - totalXp) %> more XP to unlock Level <%= (playerLevel + 1) %>!
                        <% } %>
                    </p>
                </div>
                <div class="progress-numbers">
                    <%= totalXp %> / <%= (playerLevel >= 8 ? totalXp : nextLevelThreshold) %> XP (<%= progressPercent %>%)
                </div>
            </div>

            <div class="progress-bar-bg">
                <div class="progress-bar-fill" style="width: <%= progressPercent %>%;"></div>
            </div>

            <!-- Anime Rank Roadmap -->
            <div class="rank-roadmap-grid">
                <% 
                   String[] allRanks = {
                       "Rookie Ninja", "Genin Warrior", "Chunin Warrior", "Elite Shinobi",
                       "Shadow Warrior", "Legendary Warrior", "Anime Legend", "Grandmaster"
                   };
                   for (int i = 0; i < 8; i++) {
                       int lvl = i + 1;
                       boolean isActive = (lvl == playerLevel);
                       boolean isUnlocked = (lvl < playerLevel);
                %>
                    <div class="roadmap-item <%= isActive ? "active" : (isUnlocked ? "unlocked" : "") %>">
                        <div class="roadmap-lvl">LVL <%= lvl %></div>
                        <div class="roadmap-title"><%= allRanks[i] %></div>
                    </div>
                <% } %>
            </div>
        </section>

        <!-- Recent Mission Activity -->
        <section class="card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
                <h2 style="font-size: 1.35rem; font-weight: 800;">Recent Mission Activity</h2>
                <a href="MissionServlet" style="font-size: 0.9rem; font-weight: 700; color: var(--primary-purple); text-decoration: none;">View All Missions &rarr;</a>
            </div>

            <% if (recentActivities == null || recentActivities.isEmpty()) { %>
                <div style="text-align: center; padding: 2.5rem 1rem; color: var(--text-secondary);">
                    <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">📜</div>
                    <div style="font-weight: 700; font-size: 1.1rem; color: var(--text-primary);">No Missions Attempted Yet</div>
                    <p style="margin-top: 0.25rem;">Enter the Mission Board to take on your first programming challenge!</p>
                    <a href="MissionServlet" class="btn btn-primary btn-sm" style="margin-top: 1rem;">Go to Mission Board ⚔️</a>
                </div>
            <% } else { %>
                <div class="table-responsive">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>Mission Title</th>
                                <th>Category</th>
                                <th>Result</th>
                                <th>XP Earned</th>
                                <th>Timestamp</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Map<String, Object> act : recentActivities) { 
                                boolean isCorrect = Boolean.TRUE.equals(act.get("isCorrect"));
                                int xp = (Integer) act.get("xpReward");
                            %>
                                <tr>
                                    <td style="font-weight: 700;"><%= act.get("title") %></td>
                                    <td><span class="badge badge-category"><%= act.get("category") %></span></td>
                                    <td>
                                        <% if (isCorrect) { %>
                                            <span class="badge badge-easy">Success</span>
                                        <% } else { %>
                                            <span class="badge badge-hard">Failed</span>
                                        <% } %>
                                    </td>
                                    <td>
                                        <% if (isCorrect) { %>
                                            <strong style="color: var(--accent-gold);">+<%= xp %> XP</strong>
                                        <% } else { %>
                                            <span style="color: var(--text-secondary);">0 XP</span>
                                        <% } %>
                                    </td>
                                    <td style="color: var(--text-secondary); font-size: 0.85rem;"><%= act.get("completedAt") %></td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } %>
        </section>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <div class="footer-inner">
            <div>AnimeQuest &bull; Gamified Programming Learning Adventure</div>
            <div class="sakura-tag">🌸 Advance your rank with each mission victory</div>
        </div>
    </footer>
</body>
</html>
