<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Đăng ký - NPP PDF2DOC</title>

    <!-- Shared theme (centralized CSS) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/theme.css" />
    <style>
        /* Two-column register card (left gradient marketing, right white form) */
        .page {
            display: flex;
            justify-content: center;
            align-items: center;
            /* ensure page centers content vertically; subtract approximate header height so it looks centered on most screens */
            min-height: calc(100vh - 120px);
            padding: 32px 16px;
            box-sizing: border-box;
        }

        .reg-card {
            width: 900px;
            max-width: 96%;
            display: flex;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 10px 30px rgba(0,0,0,0.08);
            background: transparent;
            /* panels should appear as two adjacent rectangles and match height */
            align-items: stretch;
        }

        .reg-card .panel-left {
            flex: 0 0 360px;
            padding: 32px;
            color: #fff;
            background: linear-gradient(135deg, #f72d23 0%, #ff6a52 100%);
            display: flex;
            flex-direction: column;
            justify-content: center;
            gap: 12px;
            min-width: 240px;
        }

        .reg-card .panel-left .logo {
            width: 56px;
            height: 56px;
            border-radius: 10px;
            background: rgba(255,255,255,0.15);
            display:flex;
            align-items:center;
            justify-content:center;
            font-weight:800;
            color:#fff;
            font-size:20px;
        }

        .reg-card .panel-left h2 {
            margin: 0;
            font-size: 22px;
            font-weight: 800;
        }

        .reg-card .panel-left p {
            margin: 0;
            opacity: 0.95;
            line-height: 1.35;
        }

        .reg-card .panel-right {
            flex: 1 1 540px;
            background: #fff;
            padding: 28px;
            box-sizing: border-box;
        }

        .reg-card .reg-form .field {
            margin-bottom: 12px;
        }

        .reg-card .reg-form label {
            display: block;
            margin-bottom: 6px;
            color: #333;
            font-size: 13px;
        }

        .reg-card .reg-form input {
            width: 100%;
            padding: 10px 12px;
            border: 1px solid #e6e6e6;
            border-radius: 8px;
            box-sizing: border-box;
            transition: box-shadow .16s ease, border-color .16s ease;
            background: #fff;
            font-size: 14px;
        }

        .reg-card .reg-form input:focus {
            outline: none;
            border-color: rgba(247,45,35,0.95);
            box-shadow: 0 0 0 6px rgba(247,45,35,0.08);
        }

        .reg-card .primary-btn {
            padding: 10px 14px;
            border-radius: 8px;
            background: #f72d23;
            color: #fff;
            border: none;
            cursor: pointer;
            font-weight: 700;
        }

        .reg-card .link-container {
            color: #444;
            font-size: 14px;
        }

        .card.process {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 18px;
        }

        @media (max-width: 760px) {
            .reg-card {
                flex-direction: column;
            }
            .reg-card .panel-left {
                padding: 20px;
                text-align: center;
            }
            .reg-card .panel-right {
                padding: 18px;
            }
        }
    </style>
</head>
<body>
    <%-- shared header (contains UTF-8 directive and fixed header) --%>
    <jsp:include page="/WEB-INF/jsp/partials/header.jsp" />

    <main class="page" role="main" aria-labelledby="register-heading">
        <div class="reg-card" role="region" aria-label="Đăng ký tài khoản">
            <!-- Left: Get started marketing panel -->
            <div class="panel-left" aria-hidden="false">
                <h2 id="register-heading" style="margin-top:0;margin-bottom:6px;">Get started</h2>
                <p class="subtitle" style="margin-top:4px;">Tạo tài khoản để tải lên file PDF, theo dõi tiến trình và lưu lịch sử chuyển đổi trong Dashboard.</p>
            </div>

            <!-- Right: registration form panel -->
            <div class="panel-right">
                <c:if test="${not empty infoMessage}">
                    <div class="muted-small" style="margin-top:12px;">${infoMessage}</div>
                </c:if>
                <form class="reg-form" action="${pageContext.request.contextPath}/register" method="post" novalidate>
                    <c:if test="${not empty errorMessage}">
                        <div class="reg-form error-message" style="padding:8px;border-radius:8px;background:rgba(247,45,35,0.04);color:#a22017;font-weight:600;margin-bottom:8px;">
                            ${errorMessage}
                        </div>
                    </c:if>

                    <div class="field">
                        <label for="username">Tên đăng nhập</label>
                        <input id="username" name="username" type="text" required autocomplete="username" />
                    </div>

                    <div class="field">
                        <label for="email">Email</label>
                        <input id="email" name="email" type="email" required autocomplete="email" />
                    </div>

                    <div class="field">
                        <label for="password">Mật khẩu</label>
                        <input id="password" name="password" type="password" required autocomplete="new-password" />
                    </div>

                    <div class="field">
                        <label for="confirmPassword">Xác nhận mật khẩu</label>
                        <input id="confirmPassword" name="confirmPassword" type="password" required autocomplete="new-password" />
                    </div>

                    <div style="margin-top:6px;">
                        <button type="submit" class="primary-btn">Tạo tài khoản</button>
                    </div>

                    <div class="link-container" style="margin-top:12px;">
                        Đã có tài khoản?
                        <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <!-- Small inline script: focus the first field on page load for convenience -->
    <script>
        (function(){
            try {
                var el = document.getElementById('username');
                if (el) el.focus();
            } catch(e) {
                // non-critical
                console && console.log('register page script error', e);
            }
        })();
    </script>
</body>
</html>
