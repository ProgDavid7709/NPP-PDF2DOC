<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
  New index.jsp placed under WEB-INF/jsp
  Purpose: simple landing page (protected by being under WEB-INF) with a header
  and a Login button that opens the /login page.

  Usage: Servlets may forward to "/WEB-INF/jsp/index.jsp" to show this page.
--%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Trang chủ - NPP PDF2DOC</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/theme.css" />
    <style>
        /* Make hero area layout horizontally when space allows.
           Add horizontal padding so content isn't flush to the viewport edges,
           and ensure hero content is vertically centered in the available viewport area. */
        .container { max-width: 1100px; margin: 0 auto; box-sizing: border-box; }

        .container .hero {
            display: flex;
            gap: 28px;
            justify-content: space-between;
            flex-wrap: nowrap;
            /* top/bottom padding kept modest; horizontal padding handled by .container */
            padding: 28px 0;
            box-sizing: border-box;
            /* make the hero fill a large portion of the viewport so its children can be vertically centered */
            min-height: calc(100vh - 64px - 80px); /* header (64px) + some breathing room */
            align-items: center; /* center lead + aside vertically inside the hero */
        }

        .container .hero .lead {
            flex: 1 1 480px;
            min-width: 260px;
            padding: 28px;
        }

        .container .hero .aside {
            flex: 0 0 260px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        /* Ensure SVG scales nicely inside aside */
        .container .hero .aside svg {
            max-width: 100%;
            height: auto;
            display: block;
        }

        /* Responsive: stack on small screens (mobile) */
        @media (max-width: 780px) {
            .container .hero {
                flex-direction: column-reverse;
                align-items: stretch;
                gap: 18px;
                min-height: auto; /* remove forced height on small screens */
            }
            .container .hero .aside {
                width: 100%;
                flex: none;
            }
        }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/partials/header.jsp" />

    <div class="container">
        <div class="hero" role="main" aria-labelledby="welcome-title">
            <div class="lead">
                <h2 id="welcome-title">Chào mừng đến với NPP PDF2DOC</h2>
                <p class="lead-desc" style="padding-bottom:10px;">
                    Chuyển đổi PDF sang DOC một cách nhanh gọn. Đăng nhập để tải lên tài liệu của bạn và theo dõi tiến trình trong Dashboard.
                </p>

                <div class="cta">
                    <a class="btn" href="${pageContext.request.contextPath}/login">Đăng nhập để bắt đầu</a>
                    <a class="btn secondary" href="${pageContext.request.contextPath}/register" style="margin-left:12px;">Đăng ký</a>
                </div>

                <ul class="feature-list" aria-hidden="false" style="list-style: none; padding-left: 0; margin-top: 18px;">
                    <li class="muted-small">• Giao diện đơn giản, dễ sử dụng</li>
                    <li class="muted-small">• Lưu trữ tác vụ và lịch sử trong Dashboard</li>
                    <li class="muted-small">• Hỗ trợ tốc độ xử lý nhanh và an toàn</li>
                </ul>
            </div>

            <div class="aside" aria-hidden="true">
            <svg width="220" height="140" viewBox="0 0 220 140" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                <rect x="0" y="0" width="220" height="140" rx="10" fill="#ecfdf5"/>
                <path d="M38 26h80v12H38z" fill="#fbe7e6"/>
                <path d="M38 48h120v10H38z" fill="#fdecea"/>
                <path d="M38 70h92v10H38z" fill="#fdecea"/>
                <circle cx="172" cy="82" r="26" fill="#f72d23"/>
                <text x="172" y="88" text-anchor="middle" font-size="18" fill="#fff" font-family="Arial">DOC</text>
            </svg>
        </div>
        </div>
    </div>
</body>
</html>
