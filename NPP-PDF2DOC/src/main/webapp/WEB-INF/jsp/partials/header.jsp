<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<style>
    /* Ensure a reliable Vietnamese-friendly font stack and make header full-width fixed
       so it always sits at the top and is not affected by page-level flex centering (login page). */
    :root {
        --header-height: 64px;
        /* Use a warm gradient based on the requested brand color */
        --header-bg: linear-gradient(90deg, #f72d23, #ff6a52);
        --header-color: #ffffff;
        --header-font-stack: "Noto Sans", "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    }

    /* Basic page padding so fixed header does not overlap content.
       Force the body to use block layout (override any page-level flex centering)
       so the header stays fixed at the top and content (e.g. login form) remains centered below it. */
    body {
        display: block !important;
        margin: 0;
        font-family: var(--header-font-stack);
        padding-top: var(--header-height);
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
    }

    /* Header layout - fixed to top, full width */
    .site-header {
        background: var(--header-bg);
        color: var(--header-color);
        padding: 10px 20px;
        box-shadow: 0 2px 6px rgba(0,0,0,0.08);
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        height: var(--header-height);
        box-sizing: border-box;
        z-index: 999;
        font-family: var(--header-font-stack);
    }

    /* Keep content centered but the header occupies full width */
    .site-header .header-inner {
        max-width: 1100px;
        margin: 0 auto;
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: 100%;
    }
    .site-header .brand a {
        color: #fff;
        text-decoration: none;
        font-weight: 700;
        font-size: 18px;
        letter-spacing: 0.2px;
        font-family: var(--header-font-stack);
    }

    /* Nav */
    .site-nav {
        display: flex;
        gap: 12px;
        align-items: center;
    }
    .site-nav a {
        color: #ffffff;
        text-decoration: none;
        padding: 8px 10px;
        border-radius: 6px;
        font-size: 14px;
        font-family: inherit;
    }
    .site-nav a:hover {
        background: rgba(255,255,255,0.05);
    }

    /* User menu */
    .user-menu {
        position: relative;
        display: inline-block;
    }
    .user-button {
        display: flex;
        gap: 8px;
        align-items: center;
        background: transparent;
        border: none;
        color: #fff;
        cursor: pointer;
        padding: 6px 8px;
        border-radius: 6px;
        font-size: 14px;
        font-family: inherit;
    }
    .user-button:hover {
        background: rgba(255,255,255,0.03);
    }
    .user-avatar {
        width: 34px;
        height: 34px;
        border-radius: 50%;
        background: #34495e;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        color: #fff;
        font-size: 14px;
        font-family: inherit;
    }
    .user-dropdown {
        position: absolute;
        right: 0;
        top: calc(100% + 8px);
        background: #fff;
        color: #333;
        min-width: 160px;
        border-radius: 6px;
        box-shadow: 0 6px 18px rgba(0,0,0,0.12);
        display: none;
        overflow: hidden;
        font-family: inherit;
    }
    .user-dropdown a {
        display: block;
        padding: 10px 12px;
        color: #333;
        text-decoration: none;
        font-size: 14px;
        font-family: inherit;
    }
    .user-dropdown a:hover {
        background: #f4f6f9;
    }

    /* small responsive tweaks */
    @media (max-width: 640px) {
        .site-header .header-inner {
            padding: 0 8px;
        }
        .site-nav {
            gap: 8px;
        }
        .user-avatar { width: 30px; height: 30px; font-size: 13px; }
    }
</style>

<header class="site-header" role="banner">
    <div class="header-inner">
        <div class="brand">
            <a href="${pageContext.request.contextPath}/index">NPP PDF2DOC</a>
        </div>

        <div style="display:flex; align-items:center; gap:12px;">
            <nav class="site-nav" role="navigation" aria-label="Main navigation">
                <c:choose>
                    <c:when test="${not empty sessionScope.userId}">
                        <%-- No top navigation links for logged-in users; use the user menu at the right --%>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
                        <a href="${pageContext.request.contextPath}/register">Đăng ký</a>
                    </c:otherwise>
                </c:choose>
            </nav>

            <c:if test="${not empty sessionScope.userId}">
                <div class="user-menu" id="userMenuRoot">
                    <button
                        type="button"
                        class="user-button"
                        id="userMenuToggle"
                        aria-haspopup="true"
                        aria-expanded="false"
                    >
                        <span class="user-avatar" aria-hidden="true">
                            <c:choose>
                                <c:when test="${not empty sessionScope.username}">
                                    ${fn:substring(sessionScope.username, 0, 1)}
                                </c:when>
                                <c:otherwise>U</c:otherwise>
                            </c:choose>
                        </span>
                        <span style="margin-left:6px; color:#ecf0f1;">
                            ${sessionScope.username}
                        </span>
                        <span style="margin-left:6px; font-size:12px; color:#ecf0f1;">▾</span>
                    </button>

                    <div class="user-dropdown" id="userDropdown" role="menu" aria-label="User menu">
                        <%-- Dashboard opens the page that lists tasks and dates (StatusServlet -> /dashboard) --%>
                        <a href="${pageContext.request.contextPath}/dashboard" role="menuitem">Dashboard</a>
                        <a href="${pageContext.request.contextPath}/logout" role="menuitem">Đăng xuất</a>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
</header>

<script>
(function () {
    try {
        var toggle = document.getElementById('userMenuToggle');
        var dropdown = document.getElementById('userDropdown');
        var root = document.getElementById('userMenuRoot');

        if (!toggle || !dropdown) return;

        function openMenu() {
            dropdown.style.display = 'block';
            toggle.setAttribute('aria-expanded', 'true');
        }
        function closeMenu() {
            dropdown.style.display = 'none';
            toggle.setAttribute('aria-expanded', 'false');
        }

        toggle.addEventListener('click', function (ev) {
            ev.stopPropagation();
            if (dropdown.style.display === 'block') {
                closeMenu();
            } else {
                openMenu();
            }
        });

        // Close when clicking outside
        document.addEventListener('click', function (ev) {
            if (!root.contains(ev.target)) {
                closeMenu();
            }
        });

        // Close on ESC
        document.addEventListener('keydown', function (ev) {
            if (ev.key === 'Escape' || ev.key === 'Esc') {
                closeMenu();
            }
        });
    } catch (e) {
        // keep silent — header must not break the page on errors
        console.error('Header partial script error:', e);
    }
})();
</script>
