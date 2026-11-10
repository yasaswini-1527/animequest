<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, firstphase.LeaderboardUser" %>
<%
    @SuppressWarnings("unchecked")
    List<LeaderboardUser> leaderboardList = (List<LeaderboardUser>) request.getAttribute("leaderboardList");
    Integer currentUserId = (Integer) request.getAttribute("currentUserId");
    if (currentUserId == null && session.getAttribute("userId") != null) {
        currentUserId = (Integer) session.getAttribute("userId");
    }

    LeaderboardUser firstPlace = (leaderboardList != null && leaderboardList.size() > 0) ? leaderboardList.get(0) : null;
    LeaderboardUser secondPlace = (leaderboardList != null && leaderboardList.size() > 1) ? leaderboardList.get(1) : null;
    LeaderboardUser thirdPlace = (leaderboardList != null && leaderboardList.size() > 2) ? leaderboardList.get(2) : null;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Global Warrior Rankings – AnimeQuest</title>
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
                    <li><a href="index.html" class="nav-link">Home</a></li>
                    <% if (session.getAttribute("userId") != null) { %>
                        <li><a href="DashboardServlet" class="nav-link">Dashboard</a></li>
                    <% } %>
                    <li><a href="MissionServlet" class="nav-link">Mission Board</a></li>
                    <li><a href="LeaderboardServlet" class="nav-link active">Leaderboard</a></li>
                </ul>
            </nav>

            <div class="nav-cta">
                <% if (session.getAttribute("userId") != null) { %>
                    <div class="user-pill">
                        <div class="user-avatar-small"><%= ((String)session.getAttribute("username")).substring(0, 1).toUpperCase() %></div>
                        <span class="user-pill-text"><%= session.getAttribute("username") %></span>
                    </div>
                    <a href="LogoutServlet" class="btn btn-outline-danger btn-sm">Logout</a>
                <% } else { %>
                    <a href="login.jsp" class="btn btn-secondary btn-sm">Warrior Login</a>
                    <a href="register.jsp" class="btn btn-primary btn-sm">Join the Quest</a>
                <% } %>
            </div>
        </div>
    </header>

    <!-- Main Container -->
    <main class="main-wrapper">
        <div style="text-align: center; margin-bottom: 2.5rem;">
            <div class="hero-badge">
                <span>🏆</span> Hall of Champions
            </div>
            <h1 style="font-size: 2.4rem; font-weight: 800; color: var(--text-primary);">
                Global Warrior <span class="brand-text">Rankings</span>
            </h1>
            <p style="color: var(--text-secondary); max-width: 600px; margin: 0.5rem auto 0;">
                The elite coders of AnimeQuest ranked by total XP and mission triumphs. Strive for glory!
            </p>
        </div>

        <% if (leaderboardList == null || leaderboardList.isEmpty()) { %>
            <div class="card" style="text-align: center; padding: 3rem;">
                <div style="font-size: 3rem; margin-bottom: 0.75rem;">⚔️</div>
                <h3 style="font-size: 1.35rem; font-weight: 800;">No Registered Warriors Yet</h3>
                <p style="color: var(--text-secondary); margin-top: 0.35rem;">Be the first adventurer to claim the #1 Grandmaster spot!</p>
                <a href="register.jsp" class="btn btn-primary btn-md" style="margin-top: 1rem;">Register Now ⚔️</a>
            </div>
        <% } else { %>

            <!-- Top 3 Podium Display -->
            <div class="podium-container">
                <!-- 2nd Place Silver -->
                <% if (secondPlace != null) { %>
                    <div class="podium-card podium-silver">
                        <div class="podium-medal">🥈</div>
                        <span class="badge" style="background: #f1f5f9; color: #475569; margin-bottom: 0.5rem;">Rank #2</span>
                        <div class="podium-username"><%= secondPlace.getUsername() %></div>
                        <div style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.5rem;"><%= secondPlace.getRankTitle() %></div>
                        <div class="podium-xp"><%= secondPlace.getTotalXp() %> XP</div>
                        <div style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 0.25rem;"><%= secondPlace.getCompletedMissions() %> Missions Won</div>
                    </div>
                <% } %>

                <!-- 1st Place Gold -->
                <% if (firstPlace != null) { %>
                    <div class="podium-card podium-gold">
                        <div class="podium-medal">🥇</div>
                        <span class="badge badge-xp" style="margin-bottom: 0.5rem;">Champion #1</span>
                        <div class="podium-username" style="font-size: 1.45rem;"><%= firstPlace.getUsername() %></div>
                        <div style="font-size: 0.9rem; color: var(--primary-purple); font-weight: 700; margin-bottom: 0.5rem;"><%= firstPlace.getRankTitle() %></div>
                        <div class="podium-xp" style="font-size: 1.4rem; color: var(--accent-gold);"><%= firstPlace.getTotalXp() %> XP</div>
                        <div style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 0.25rem;"><%= firstPlace.getCompletedMissions() %> Missions Won</div>
                    </div>
                <% } %>

                <!-- 3rd Place Bronze -->
                <% if (thirdPlace != null) { %>
                    <div class="podium-card podium-bronze">
                        <div class="podium-medal">🥉</div>
                        <span class="badge" style="background: #fee2e2; color: #b91c1c; margin-bottom: 0.5rem;">Rank #3</span>
                        <div class="podium-username"><%= thirdPlace.getUsername() %></div>
                        <div style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.5rem;"><%= thirdPlace.getRankTitle() %></div>
                        <div class="podium-xp"><%= thirdPlace.getTotalXp() %> XP</div>
                        <div style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 0.25rem;"><%= thirdPlace.getCompletedMissions() %> Missions Won</div>
                    </div>
                <% } %>
            </div>

            <!-- Complete Leaderboard Table -->
            <div class="card" style="padding: 1.5rem;">
                <div class="table-responsive">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th style="width: 80px; text-align: center;">Rank</th>
                                <th>Warrior Player</th>
                                <th>Warrior Level</th>
                                <th>Rank Title</th>
                                <th>Missions Won</th>
                                <th style="text-align: right;">Total XP</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (LeaderboardUser u : leaderboardList) { 
                                boolean isSelf = (currentUserId != null && currentUserId == u.getId());
                                String rankNumClass = "";
                                if (u.getRank() == 1) rankNumClass = "gold";
                                else if (u.getRank() == 2) rankNumClass = "silver";
                                else if (u.getRank() == 3) rankNumClass = "bronze";
                            %>
                                <tr class="<%= isSelf ? "highlight-user" : "" %>">
                                    <td style="text-align: center;">
                                        <div class="rank-badge-num <%= rankNumClass %>">
                                            <%= u.getRank() %>
                                        </div>
                                    </td>
                                    <td>
                                        <div style="display: flex; align-items: center; gap: 0.5rem;">
                                            <strong><%= u.getUsername() %></strong>
                                            <% if (isSelf) { %>
                                                <span class="badge badge-category" style="font-size: 0.65rem;">YOU</span>
                                            <% } %>
                                        </div>
                                    </td>
                                    <td>Level <%= u.getPlayerLevel() %></td>
                                    <td>
                                        <span class="rank-pill" style="font-size: 0.78rem;">
                                            <%= u.getRankTitle() %>
                                        </span>
                                    </td>
                                    <td><%= u.getCompletedMissions() %></td>
                                    <td style="text-align: right;">
                                        <strong style="color: var(--primary-purple); font-size: 1.05rem;">
                                            <%= u.getTotalXp() %> XP
                                        </strong>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        <% } %>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <div class="footer-inner">
            <div>AnimeQuest &bull; Gamified Programming Learning Adventure</div>
            <div class="sakura-tag">🌸 Honor, persistence, and algorithmic mastery</div>
        </div>
    </footer>
</body>
</html>
