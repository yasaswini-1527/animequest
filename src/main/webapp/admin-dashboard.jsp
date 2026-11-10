<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.Connection, java.sql.PreparedStatement, java.sql.ResultSet, firstphase.DBConnection" %>
<%
    // Verify admin authentication
    Integer adminId = (Integer) session.getAttribute("adminId");
    if (adminId == null) {
        response.sendRedirect(request.getContextPath() + "/admin-login.jsp?error=Admin+session+required.+Please+log+in.");
        return;
    }

    String adminUsername = (String) session.getAttribute("adminUsername");
    if (adminUsername == null) adminUsername = "admin";

    // Retrieve system overview metrics
    int totalMissions = 0;
    int totalWarriors = 0;
    int totalSubmissions = 0;

    try (Connection conn = DBConnection.getConnection()) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM missions");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) totalMissions = rs.getInt(1);
        }
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) totalWarriors = rs.getInt(1);
        }
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM mission_submissions");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) totalSubmissions = rs.getInt(1);
        }
    } catch (Exception e) {
        // Fallback gracefully
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mission Master Command – AnimeQuest</title>
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
                <span class="brand-badge" style="background: #fef3c7; color: #b45309;">Mission Master</span>
            </a>

            <nav>
                <ul class="nav-links">
                    <li><a href="admin-dashboard.jsp" class="nav-link active">Admin Dashboard</a></li>
                    <li><a href="add-mission.jsp" class="nav-link">Deploy Mission</a></li>
                    <li><a href="ViewMissionsServlet" class="nav-link">Mission Inventory</a></li>
                </ul>
            </nav>

            <div class="nav-cta">
                <div class="user-pill" style="border-color: #fde68a; background: #fffbeb;">
                    <div class="user-avatar-small" style="background: var(--gradient-gold);">👑</div>
                    <span class="user-pill-text" style="color: #b45309;"><%= adminUsername %></span>
                </div>
                <a href="AdminLogoutServlet" class="btn btn-outline-danger btn-sm">Admin Logout</a>
            </div>
        </div>
    </header>

    <!-- Main Container -->
    <main class="main-wrapper">
        <!-- Welcome Section -->
        <section class="dashboard-header" style="background: linear-gradient(135deg, #ffffff 0%, #fffbeb 100%); border-color: #fde68a;">
            <div class="dashboard-user-info">
                <div class="warrior-avatar-large" style="background: var(--gradient-gold);">👑</div>
                <div class="dashboard-user-meta">
                    <h1>Welcome, <span style="color: var(--accent-gold);">Mission Master</span> <%= adminUsername %></h1>
                    <div class="rank-pill" style="background: #fef3c7; color: #b45309; border-color: #fde68a;">
                        <span>⚙️</span> AnimeQuest Realm Administrator
                    </div>
                </div>
            </div>

            <div class="dashboard-actions">
                <a href="add-mission.jsp" class="btn btn-primary btn-md">+ Deploy New Mission</a>
                <a href="ViewMissionsServlet" class="btn btn-secondary btn-md">Manage Missions 📜</a>
            </div>
        </section>

        <!-- Metrics Overview -->
        <section class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon icon-purple">📜</div>
                <div>
                    <div class="stat-value"><%= totalMissions %></div>
                    <div class="stat-title">Deployed Missions</div>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon icon-pink">🥷</div>
                <div>
                    <div class="stat-value"><%= totalWarriors %></div>
                    <div class="stat-title">Registered Warriors</div>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon icon-gold">⚡</div>
                <div>
                    <div class="stat-value"><%= totalSubmissions %></div>
                    <div class="stat-title">Total Submissions Evaluated</div>
                </div>
            </div>
        </section>

        <!-- Administrative Quick Action Cards -->
        <section class="feature-grid">
            <div class="feature-card" style="display: flex; flex-direction: column; justify-content: space-between;">
                <div>
                    <div class="feature-icon-box icon-purple">➕</div>
                    <h3 class="feature-card-title">Deploy New Mission</h3>
                    <p class="feature-card-desc">
                        Author and release new Java, SQL, or DSA programming missions with customized XP rewards, difficulties, and four candidate answer options.
                    </p>
                </div>
                <div style="margin-top: 1.5rem;">
                    <a href="add-mission.jsp" class="btn btn-primary btn-block">Create Mission Form &rarr;</a>
                </div>
            </div>

            <div class="feature-card" style="display: flex; flex-direction: column; justify-content: space-between;">
                <div>
                    <div class="feature-icon-box icon-gold">📋</div>
                    <h3 class="feature-card-title">Manage Mission Inventory</h3>
                    <p class="feature-card-desc">
                        Review the entire catalog of deployed programming challenges, inspect difficulty distributions, and monitor learning materials.
                    </p>
                </div>
                <div style="margin-top: 1.5rem;">
                    <a href="ViewMissionsServlet" class="btn btn-secondary btn-block">Open Mission Directory &rarr;</a>
                </div>
            </div>

            <div class="feature-card" style="display: flex; flex-direction: column; justify-content: space-between;">
                <div>
                    <div class="feature-icon-box icon-pink">🏆</div>
                    <h3 class="feature-card-title">Global Warrior Rankings</h3>
                    <p class="feature-card-desc">
                        Inspect the live public student leaderboard to monitor player performance, XP distribution, and level advancement across the realm.
                    </p>
                </div>
                <div style="margin-top: 1.5rem;">
                    <a href="LeaderboardServlet" class="btn btn-gold btn-block">Inspect Rankings &rarr;</a>
                </div>
            </div>
        </section>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <div class="footer-inner">
            <div>AnimeQuest &bull; Mission Master Administrative Console</div>
            <div class="sakura-tag">🌸 Guiding student warriors to programming excellence</div>
        </div>
    </footer>
</body>
</html>
