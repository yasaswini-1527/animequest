<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, firstphase.Mission" %>
<%
    // Verify admin authentication
    Integer adminId = (Integer) session.getAttribute("adminId");
    if (adminId == null) {
        response.sendRedirect(request.getContextPath() + "/admin-login.jsp?error=Mission+Master+access+required.");
        return;
    }

    String adminUsername = (String) session.getAttribute("adminUsername");
    if (adminUsername == null) adminUsername = "admin";

    @SuppressWarnings("unchecked")
    List<Mission> missionList = (List<Mission>) request.getAttribute("missionList");

    // If accessed directly without going through ViewMissionsServlet, redirect to servlet
    if (missionList == null) {
        response.sendRedirect(request.getContextPath() + "/ViewMissionsServlet");
        return;
    }

    String successMessage = request.getParameter("success");
    String errorMessage = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mission Inventory – AnimeQuest</title>
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
                    <li><a href="admin-dashboard.jsp" class="nav-link">Admin Dashboard</a></li>
                    <li><a href="add-mission.jsp" class="nav-link">Deploy Mission</a></li>
                    <li><a href="ViewMissionsServlet" class="nav-link active">Mission Inventory</a></li>
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
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
            <div>
                <h1 style="font-size: 2.2rem; font-weight: 800; color: var(--text-primary); margin-bottom: 0.35rem;">
                    Mission <span class="brand-text">Inventory Management</span>
                </h1>
                <p style="color: var(--text-secondary); font-size: 1rem;">
                    Catalog of all currently active programming missions across Java, SQL, and DSA tracks.
                </p>
            </div>
            <div>
                <a href="add-mission.jsp" class="btn btn-primary btn-md">+ Deploy New Mission</a>
            </div>
        </div>

        <% if (successMessage != null && !successMessage.isEmpty()) { %>
            <div class="alert alert-success">
                <span>✨</span>
                <div><%= successMessage %></div>
            </div>
        <% } %>

        <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
            <div class="alert alert-error">
                <span>⚠️</span>
                <div><%= errorMessage %></div>
            </div>
        <% } %>

        <!-- Dynamic Missions Table -->
        <div class="card" style="padding: 1.5rem;">
            <% if (missionList == null || missionList.isEmpty()) { %>
                <div style="text-align: center; padding: 3rem 1rem;">
                    <div style="font-size: 3rem; margin-bottom: 0.5rem;">📭</div>
                    <h3 style="font-size: 1.25rem; font-weight: 800;">No Missions Found</h3>
                    <p style="color: var(--text-secondary); margin-top: 0.35rem;">Deploy your first programming mission to populate the student board.</p>
                    <a href="add-mission.jsp" class="btn btn-primary btn-sm" style="margin-top: 1rem;">Create Mission &rarr;</a>
                </div>
            <% } else { %>
                <div class="table-responsive">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th style="width: 70px;">ID</th>
                                <th>Mission Title</th>
                                <th>Description</th>
                                <th>Category</th>
                                <th>Difficulty</th>
                                <th style="text-align: right;">XP Reward</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Mission m : missionList) { 
                                String diffClass = "badge-easy";
                                if ("Medium".equalsIgnoreCase(m.getDifficulty())) diffClass = "badge-medium";
                                else if ("Hard".equalsIgnoreCase(m.getDifficulty())) diffClass = "badge-hard";
                            %>
                                <tr>
                                    <td style="font-weight: 800; color: var(--text-secondary);">#<%= m.getId() %></td>
                                    <td><strong style="color: var(--text-primary);"><%= m.getTitle() %></strong></td>
                                    <td style="max-width: 380px; font-size: 0.9rem; color: var(--text-secondary);">
                                        <%= m.getDescription() %>
                                    </td>
                                    <td><span class="badge badge-category"><%= m.getCategory() %></span></td>
                                    <td><span class="badge <%= diffClass %>"><%= m.getDifficulty() %></span></td>
                                    <td style="text-align: right;">
                                        <strong style="color: var(--accent-gold); font-size: 1.05rem;">
                                            +<%= m.getXpReward() %> XP
                                        </strong>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>

                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 1.5rem; color: var(--text-secondary); font-size: 0.85rem;">
                    <div>Total Active Missions: <strong><%= missionList.size() %></strong></div>
                    <div style="font-style: italic;">* Correct answer solutions are secured and hidden from public directory views.</div>
                </div>
            <% } %>
        </div>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <div class="footer-inner">
            <div>AnimeQuest &bull; Mission Master Administrative Console</div>
            <div class="sakura-tag">🌸 Maintaining pedagogical rigor and gamified balance</div>
        </div>
    </footer>
</body>
</html>
