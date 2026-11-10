<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Verify admin authentication
    Integer adminId = (Integer) session.getAttribute("adminId");
    if (adminId == null) {
        response.sendRedirect(request.getContextPath() + "/admin-login.jsp?error=Mission+Master+access+required.");
        return;
    }

    String adminUsername = (String) session.getAttribute("adminUsername");
    if (adminUsername == null) adminUsername = "admin";

    String errorMessage = (String) request.getAttribute("errorMessage");
    String fieldTitle = (String) request.getAttribute("fieldTitle");
    String fieldDescription = (String) request.getAttribute("fieldDescription");
    String fieldCategory = (String) request.getAttribute("fieldCategory");
    String fieldDifficulty = (String) request.getAttribute("fieldDifficulty");
    String fieldOptionA = (String) request.getAttribute("fieldOptionA");
    String fieldOptionB = (String) request.getAttribute("fieldOptionB");
    String fieldOptionC = (String) request.getAttribute("fieldOptionC");
    String fieldOptionD = (String) request.getAttribute("fieldOptionD");
    String fieldCorrectAnswer = (String) request.getAttribute("fieldCorrectAnswer");
    String fieldXpReward = (String) request.getAttribute("fieldXpReward");

    if (fieldTitle == null) fieldTitle = "";
    if (fieldDescription == null) fieldDescription = "";
    if (fieldCategory == null) fieldCategory = "Java";
    if (fieldDifficulty == null) fieldDifficulty = "Easy";
    if (fieldOptionA == null) fieldOptionA = "";
    if (fieldOptionB == null) fieldOptionB = "";
    if (fieldOptionC == null) fieldOptionC = "";
    if (fieldOptionD == null) fieldOptionD = "";
    if (fieldCorrectAnswer == null) fieldCorrectAnswer = "";
    if (fieldXpReward == null) fieldXpReward = "50";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Deploy New Mission – AnimeQuest</title>
    <link rel="stylesheet" href="css/style.css">
    <script>
        function syncCorrectAnswerOptions() {
            var optA = document.getElementById("option_a").value;
            var optB = document.getElementById("option_b").value;
            var optC = document.getElementById("option_c").value;
            var optD = document.getElementById("option_d").value;
            
            var sel = document.getElementById("correct_answer_select");
            var currentVal = sel.value;
            
            sel.innerHTML = "";
            
            var createOpt = function(val, label) {
                var opt = document.createElement("option");
                opt.value = val;
                opt.textContent = label + ": " + (val ? val : "(Empty)");
                if (val && val === currentVal) opt.selected = true;
                sel.appendChild(opt);
            };
            
            createOpt(optA, "Option A");
            createOpt(optB, "Option B");
            createOpt(optC, "Option C");
            createOpt(optD, "Option D");
        }
    </script>
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
                    <li><a href="add-mission.jsp" class="nav-link active">Deploy Mission</a></li>
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
        <div class="card" style="max-width: 780px; margin: 1.5rem auto;">
            <div style="text-align: center; margin-bottom: 2rem;">
                <div class="feature-icon-box icon-purple" style="margin: 0 auto 0.75rem;">➕</div>
                <h1 style="font-size: 2rem; font-weight: 800; color: var(--text-primary);">Deploy Programming Mission</h1>
                <p style="color: var(--text-secondary); font-size: 0.95rem;">
                    Configure and publish a new programming challenge for the student warrior community.
                </p>
            </div>

            <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
                <div class="alert alert-error">
                    <span>⚠️</span>
                    <div><%= errorMessage %></div>
                </div>
            <% } %>

            <form action="AddMissionServlet" method="post" autocomplete="off">
                <!-- Mission Title -->
                <div class="form-group">
                    <label class="form-label" for="title">Mission Title *</label>
                    <input type="text" id="title" name="title" class="form-input" 
                           placeholder="e.g. Java Polymorphism Mechanics" required value="<%= fieldTitle %>">
                </div>

                <!-- Mission Description -->
                <div class="form-group">
                    <label class="form-label" for="description">Mission Description / Question Text *</label>
                    <textarea id="description" name="description" class="form-textarea" rows="4" 
                              placeholder="Provide the complete programming problem, code snippet, or theoretical query..." required><%= fieldDescription %></textarea>
                </div>

                <!-- Category and Difficulty -->
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                    <div class="form-group">
                        <label class="form-label" for="category">Category *</label>
                        <select id="category" name="category" class="form-select" required>
                            <option value="Java" <%= "Java".equalsIgnoreCase(fieldCategory) ? "selected" : "" %>>Java (Core & OOP)</option>
                            <option value="SQL" <%= "SQL".equalsIgnoreCase(fieldCategory) ? "selected" : "" %>>SQL (Relational DB)</option>
                            <option value="DSA" <%= "DSA".equalsIgnoreCase(fieldCategory) ? "selected" : "" %>>DSA (Data Structures &amp; Algorithms)</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="difficulty">Difficulty *</label>
                        <select id="difficulty" name="difficulty" class="form-select" required>
                            <option value="Easy" <%= "Easy".equalsIgnoreCase(fieldDifficulty) ? "selected" : "" %>>Easy</option>
                            <option value="Medium" <%= "Medium".equalsIgnoreCase(fieldDifficulty) ? "selected" : "" %>>Medium</option>
                            <option value="Hard" <%= "Hard".equalsIgnoreCase(fieldDifficulty) ? "selected" : "" %>>Hard</option>
                        </select>
                    </div>
                </div>

                <!-- Answer Options -->
                <div style="margin: 1.5rem 0 1rem; border-top: 1px solid var(--border-color); padding-top: 1.25rem;">
                    <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 1rem; color: var(--primary-purple);">
                        Answer Options (4 Choices)
                    </h3>

                    <div class="form-group">
                        <label class="form-label" for="option_a">Option A *</label>
                        <input type="text" id="option_a" name="option_a" class="form-input" 
                               placeholder="Candidate answer A" required value="<%= fieldOptionA %>" oninput="syncCorrectAnswerOptions()">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="option_b">Option B *</label>
                        <input type="text" id="option_b" name="option_b" class="form-input" 
                               placeholder="Candidate answer B" required value="<%= fieldOptionB %>" oninput="syncCorrectAnswerOptions()">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="option_c">Option C *</label>
                        <input type="text" id="option_c" name="option_c" class="form-input" 
                               placeholder="Candidate answer C" required value="<%= fieldOptionC %>" oninput="syncCorrectAnswerOptions()">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="option_d">Option D *</label>
                        <input type="text" id="option_d" name="option_d" class="form-input" 
                               placeholder="Candidate answer D" required value="<%= fieldOptionD %>" oninput="syncCorrectAnswerOptions()">
                    </div>
                </div>

                <!-- Correct Answer Selection & XP Reward -->
                <div style="display: grid; grid-template-columns: 1.5fr 1fr; gap: 1rem; margin-top: 1rem;">
                    <div class="form-group">
                        <label class="form-label" for="correct_answer">Correct Answer (Must match an option above) *</label>
                        <input type="text" id="correct_answer" name="correct_answer" class="form-input" 
                               placeholder="Paste exact correct option value" required value="<%= fieldCorrectAnswer %>">
                        <div style="font-size: 0.75rem; color: var(--text-secondary); margin-top: 0.35rem;">
                            Hint: Ensure the text matches Option A, B, C, or D exactly.
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="xp_reward">XP Reward (Points) *</label>
                        <input type="number" id="xp_reward" name="xp_reward" class="form-input" 
                               min="10" max="1000" step="5" required value="<%= fieldXpReward %>">
                    </div>
                </div>

                <!-- Submit Button -->
                <div style="display: flex; gap: 1rem; margin-top: 2rem;">
                    <button type="submit" class="btn btn-primary btn-lg" style="flex: 1;">
                        Publish Mission to Board 🚀
                    </button>
                    <a href="admin-dashboard.jsp" class="btn btn-secondary btn-lg">Cancel</a>
                </div>
            </form>
        </div>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <div class="footer-inner">
            <div>AnimeQuest &bull; Mission Master Administrative Console</div>
            <div class="sakura-tag">🌸 Crafting challenges that forge true mastery</div>
        </div>
    </footer>
</body>
</html>
