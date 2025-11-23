<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Đăng nhập - NPP PDF2DOC</title>

    <!-- Shared theme (contains .login-compact) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/theme.css" />
    <style>
        /* Small page-specific tweak: keep body/layout for header unchanged; center the main container instead */
        body {
            /* keep body normal so header stays in place */
            display: block;
            min-height: calc(100vh - 64px);
            background: transparent; /* background is handled by theme.css */
        }

        /* Center the main.container (so header positioning is not affected) */
        main.container {
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: calc(100vh - 120px); /* leave room for fixed header; adjust if needed */
            box-sizing: border-box;
            padding-top: 0;
        }

        /* Ensure the compact card sits visually centered inside the main.container */
        .login-compact {
            margin: 0;
        }

        /* Two-column login card: left marketing/welcome + right form */
        .login-card {
            display: flex;
            width: 820px;
            max-width: 96%;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 8px 28px rgba(0,0,0,0.12);
            background: transparent; /* panels provide their own backgrounds */
            align-items: stretch;
        }

        .login-card .panel-left {
            flex: 1 1 360px;
            padding: 36px 28px;
            color: #fff;
            display: flex;
            flex-direction: column;
            justify-content: center;
            gap: 8px;
            background: linear-gradient(180deg, #f72d23 0%, #ff6a52 100%);
            min-width: 260px;
        }

        .login-card .panel-left .welcome {
            margin: 0;
            font-size: 28px;
            line-height: 1.05;
            font-weight: 800;
        }

        .login-card .panel-left .panel-sub {
            margin: 0;
            color: rgba(255,255,255,0.95);
            font-size: 14px;
        }

        .login-card .panel-right {
            flex: 1 1 440px;
            background: #ffffff;
            padding: 28px;
            box-sizing: border-box;
        }

        .login-card .panel-right .login-form h2 {
            margin: 0 0 8px 0;
            font-size: 20px;
            color: #222;
        }
        .login-card .panel-right .lead {
            margin: 0 0 14px 0;
            color: #555;
        }

        /* Form fields style + focus glow using the brand color */
        .login-card .panel-right .field {
            margin-bottom: 12px;
        }
        .login-card input[type="text"],
        .login-card input[type="password"],
        .login-card input[type="email"] {
            width: 100%;
            padding: 10px 12px;
            border: 1px solid #e6e6e6;
            border-radius: 8px;
            box-sizing: border-box;
            transition: box-shadow .18s ease, border-color .18s ease;
            font-size: 14px;
        }

        .login-card input:focus {
            outline: none;
            border-color: rgba(247,45,35,0.9);
            box-shadow: 0 0 0 6px rgba(247,45,35,0.08);
        }

        .submit-row {
            margin-top: 12px;
            display: flex;
            align-items: center;
        }

        /* Button typography & prevent label wrapping so "Đăng nhập" / "Đăng ký" stay on one line */
        .login-card .btn {
            font-family: "Noto Sans", "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            font-weight: 700;
            font-size: 14px;
            white-space: nowrap;        /* keep button text on one line when possible */
            padding: 10px 14px;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            justify-content: center;
        }
        .login-card .btn.secondary {
            padding: 10px 12px;
        }

        .form-footer {
            margin-top: 12px;
            color: #666;
            font-size: 13px;
        }

        /* Preserve previous login-error look but scoped to panel-right */
        .login-error {
            background: rgba(247,45,35,0.06);
            color: #8b1913;
            padding: 10px 12px;
            border-radius: 8px;
            font-weight: 700;
            margin-bottom: 10px;
        }

        /* Responsive: stack vertically on narrow screens */
        @media (max-width: 720px) {
            .login-card {
                flex-direction: column;
                width: 92%;
            }
            .login-card .panel-left {
                padding: 20px;
                text-align: center;
            }
            .login-card .panel-right {
                padding: 18px;
            }
        }
    </style>
</head>
<body>
    <%-- Include shared header partial (fixed header + UTF-8 directive) --%>
    <jsp:include page="/WEB-INF/jsp/partials/header.jsp" />

    <main role="main" aria-labelledby="login-heading" class="container">
        <section class="login-compact" aria-label="Đăng nhập">
            <div class="icon-box" style="display:none"></div>

            <div class="login-card" role="region" aria-labelledby="login-heading">
                <!-- Left panel: marketing/welcome -->
                <div class="panel-left" aria-hidden="false">
                    <h2 class="welcome">Welcome Back</h2>
                    <p class="panel-sub" style="margin-top:4px;">Chúng tôi rất vui khi bạn quay trở lại — đăng nhập để truy cập Process và Dashboard.</p>
                </div>

                <!-- Right panel: actual form -->
                <div class="panel-right">
                    <div class="login-form">
                        <!--<h2 id="login-heading">Đăng nhập</h2>
                        <p class="lead">Nhập tài khoản để truy cập Process và Dashboard.</p>-->

                        <c:if test="${not empty errorMessage}">
                            <div class="login-error" role="alert">${errorMessage}</div>
                        </c:if>

                        <form action="${pageContext.request.contextPath}/login" method="POST" class="login-compact-form" autocomplete="on" novalidate>
                            <div class="field">
                                <label for="username">Tên đăng nhập</label>
                                <input id="username" name="username" type="text" required autofocus autocomplete="username" />
                            </div>

                            <div class="field">
                                <label for="password">Mật khẩu</label>
                                <input id="password" name="password" type="password" required autocomplete="current-password" />
                            </div>

                            <div class="submit-row">
                                <button type="submit" class="btn">Đăng nhập</button>
                                <a class="btn secondary" href="${pageContext.request.contextPath}/register" style="display:inline-block; padding:10px 12px; margin-left:8px;">Đăng ký</a>
                            </div>

                            <div class="form-footer" style="margin-top:12px;">
                                <a href="${pageContext.request.contextPath}/">Quay về trang chủ</a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </section>
    </main>

    <script>
        // Focus convenience: focus username when page loads
        (function () {
            try {
                var u = document.getElementById('username');
                if (u) u.focus();
            } catch (e) {
                // ignore
            }
        })();
    </script>
</body>
</html>
