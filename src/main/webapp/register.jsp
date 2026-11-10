<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String errorMessage = (String) request.getAttribute("errorMessage");
    String enteredUsername = (String) request.getAttribute("enteredUsername");
    String enteredEmail = (String) request.getAttribute("enteredEmail");
    
    if (enteredUsername == null) enteredUsername = "";
    if (enteredEmail == null) enteredEmail = "";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Warrior Registration – AnimeQuest</title>
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
                <a href="login.jsp" class="btn btn-secondary btn-sm">Warrior Login</a>
                <a href="index.html" class="btn btn-secondary btn-sm">Home</a>
            </div>
        </div>
    </header>

    <!-- Main Container -->
    <main class="main-wrapper">
        <div class="form-card">
            <div class="form-header">
                <div class="form-avatar">🥷</div>
                <h1 class="form-title">Join AnimeQuest</h1>
                <p class="form-subtitle">Forge your warrior identity and start at Level 1 Rookie Ninja</p>
            </div>

            <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
                <div class="alert alert-error">
                    <span>⚠️</span>
                    <div><%= errorMessage %></div>
                </div>
            <% } %>

            <form action="RegisterServlet" method="post" autocomplete="off">
                <div class="form-group">
                    <label class="form-label" for="username">Warrior Username</label>
                    <input type="text" id="username" name="username" class="form-input" 
                           placeholder="e.g. ShadowCoder" required value="<%= enteredUsername %>">
                </div>

                <div class="form-group">
                    <label class="form-label" for="email">Student Email Address</label>
                    <input type="email" id="email" name="email" class="form-input" 
                           placeholder="warrior@academy.edu" required value="<%= enteredEmail %>">
                </div>

                <div class="form-group">
                    <label class="form-label" for="password">Warrior Password</label>
                    <input type="password" id="password" name="password" class="form-input" 
                           placeholder="Minimum 6 characters" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="confirmPassword">Confirm Password</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" class="form-input" 
                           placeholder="Re-enter password" required>
                </div>

                <button type="submit" class="btn btn-primary btn-block btn-lg" style="margin-top: 1.5rem;">
                    Register as Rookie Ninja ⚔️
                </button>
            </form>

            <div class="form-footer-link">
                Already registered as a warrior? <a href="login.jsp">Enter the Arena (Login)</a>
            </div>
        </div>
    </main>

    <footer class="footer">
        <div class="footer-inner">
            <div>AnimeQuest &bull; Gamified Programming Learning Adventure</div>
            <div class="sakura-tag">🌸 Every master was once a rookie ninja</div>
        </div>
    </footer>
</body>
</html>
