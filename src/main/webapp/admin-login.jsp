<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String errorMessage = (String) request.getAttribute("errorMessage");
    if (errorMessage == null) {
        errorMessage = request.getParameter("error");
    }
    String message = request.getParameter("message");
    String enteredAdminUser = (String) request.getAttribute("enteredAdminUser");
    if (enteredAdminUser == null) enteredAdminUser = "";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mission Master Portal – AnimeQuest</title>
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
                <span class="brand-badge" style="background: #fef3c7; color: #b45309;">Admin Portal</span>
            </a>
            <div class="nav-cta">
                <a href="login.jsp" class="btn btn-secondary btn-sm">Warrior Login</a>
                <a href="index.html" class="btn btn-secondary btn-sm">Home</a>
            </div>
        </div>
    </header>

    <!-- Main Container -->
    <main class="main-wrapper">
        <div class="form-card">
            <div class="form-header">
                <div class="form-avatar" style="background: var(--gradient-gold);">👑</div>
                <h1 class="form-title">Mission Master</h1>
                <p class="form-subtitle">Administrative control portal for mission deployment and management</p>
            </div>

            <!-- Credentials Note -->
            <div style="background-color: #faf5ff; border: 1px solid #ede9fe; border-radius: var(--radius-md); padding: 0.85rem 1rem; margin-bottom: 1.25rem; font-size: 0.85rem; color: var(--primary-purple);">
                <strong>Default Administrator Credentials:</strong><br>
                Username: <code>admin</code> &bull; Password: <code>admin123</code>
            </div>

            <% if (message != null && !message.isEmpty()) { %>
                <div class="alert alert-success">
                    <span>🌸</span>
                    <div><%= message %></div>
                </div>
            <% } %>

            <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
                <div class="alert alert-error">
                    <span>⚠️</span>
                    <div><%= errorMessage %></div>
                </div>
            <% } %>

            <form action="AdminLoginServlet" method="post" autocomplete="off">
                <div class="form-group">
                    <label class="form-label" for="username">Administrator Username</label>
                    <input type="text" id="username" name="username" class="form-input" 
                           placeholder="Enter admin username" required value="<%= enteredAdminUser %>">
                </div>

                <div class="form-group">
                    <label class="form-label" for="password">Administrator Password</label>
                    <input type="password" id="password" name="password" class="form-input" 
                           placeholder="Enter admin password" required>
                </div>

                <button type="submit" class="btn btn-gold btn-block btn-lg" style="margin-top: 1.25rem;">
                    Access Mission Command 👑
                </button>
            </form>

            <div class="form-footer-link">
                Looking for the student portal? <a href="login.jsp">Go to Warrior Login</a>
            </div>
        </div>
    </main>

    <footer class="footer">
        <div class="footer-inner">
            <div>AnimeQuest &bull; Mission Master Administrative Console</div>
            <div class="sakura-tag">🌸 Orchestrating challenges for the next generation of coders</div>
        </div>
    </footer>
</body>
</html>
