<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String errorMessage = (String) request.getAttribute("errorMessage");
    if (errorMessage == null) {
        errorMessage = request.getParameter("error");
    }

    String successMessage = request.getParameter("success");
    String infoMessage = request.getParameter("message");

    String enteredUser = (String) request.getAttribute("enteredUser");

    // Look for Remembered Username cookie
    String rememberedUsername = "";
    jakarta.servlet.http.Cookie[] cookies = request.getCookies();
    if (cookies != null) {
        for (jakarta.servlet.http.Cookie c : cookies) {
            if ("rememberedUser".equals(c.getName())) {
                rememberedUsername = c.getValue();
                break;
            }
        }
    }

    String defaultUserVal = "";
    if (enteredUser != null && !enteredUser.isEmpty()) {
        defaultUserVal = enteredUser;
    } else if (rememberedUsername != null && !rememberedUsername.isEmpty()) {
        defaultUserVal = rememberedUsername;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Warrior Login – AnimeQuest</title>
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
                <a href="register.jsp" class="btn btn-primary btn-sm">Register Warrior</a>
                <a href="admin-login.jsp" class="btn btn-secondary btn-sm">Mission Master</a>
            </div>
        </div>
    </header>

    <!-- Main Container -->
    <main class="main-wrapper">
        <div class="form-card">
            <div class="form-header">
                <div class="form-avatar">⚡</div>
                <h1 class="form-title">Enter the Arena</h1>
                <p class="form-subtitle">Log in to your Warrior Dashboard to resume your quest</p>
            </div>

            <% if (successMessage != null && !successMessage.isEmpty()) { %>
                <div class="alert alert-success">
                    <span>✨</span>
                    <div><%= successMessage %></div>
                </div>
            <% } %>

            <% if (infoMessage != null && !infoMessage.isEmpty()) { %>
                <div class="alert alert-success">
                    <span>🌸</span>
                    <div><%= infoMessage %></div>
                </div>
            <% } %>

            <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
                <div class="alert alert-error">
                    <span>⚠️</span>
                    <div><%= errorMessage %></div>
                </div>
            <% } %>

            <form action="LoginServlet" method="post" autocomplete="off">
                <div class="form-group">
                    <label class="form-label" for="usernameOrEmail">Warrior Username or Email</label>
                    <input type="text" id="usernameOrEmail" name="usernameOrEmail" class="form-input" 
                           placeholder="Enter username or registered email" required value="<%= defaultUserVal %>">
                </div>

                <div class="form-group">
                    <label class="form-label" for="password">Password</label>
                    <input type="password" id="password" name="password" class="form-input" 
                           placeholder="Enter your warrior password" required>
                </div>

                <div class="form-group" style="display: flex; justify-content: space-between; align-items: center; margin-top: 0.5rem;">
                    <label class="form-checkbox-label">
                        <input type="checkbox" name="rememberMe" value="on" 
                               <%= (rememberedUsername != null && !rememberedUsername.isEmpty()) ? "checked" : "" %>>
                        Remember my username
                    </label>
                </div>

                <button type="submit" class="btn btn-primary btn-block btn-lg" style="margin-top: 1.25rem;">
                    Enter Warrior Dashboard 🚀
                </button>
            </form>

            <div class="form-footer-link">
                New adventurer? <a href="register.jsp">Create your player account</a>
            </div>
            
            <div class="form-footer-link" style="margin-top: 0.5rem; font-size: 0.8rem;">
                Are you an administrator? <a href="admin-login.jsp">Mission Master Portal</a>
            </div>
        </div>
    </main>

    <footer class="footer">
        <div class="footer-inner">
            <div>AnimeQuest &bull; Gamified Programming Learning Adventure</div>
            <div class="sakura-tag">🌸 Sharpen your mind like a legendary blade</div>
        </div>
    </footer>
</body>
</html>
