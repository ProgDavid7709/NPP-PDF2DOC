<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Process - NPP PDF2DOC</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/theme.css" />
    <style>
        /* Fade animation for uploadArea and fileList */
        .fade-in {
            opacity: 1;
            transform: translateY(0);
            transition: opacity 220ms ease, transform 220ms ease, max-height 220ms ease;
        }
        .fade-out {
            opacity: 0;
            transform: translateY(-6px);
            transition: opacity 180ms ease, transform 180ms ease, max-height 180ms ease;
        }
        #fileList, #uploadArea {
            overflow: hidden;
            will-change: opacity, transform, max-height;
        }

        /* Button muted / active swap classes */
        .btn-muted {
            background: #bdbdbd !important;
            border-color: #bdbdbd !important;
            color: #fff !important;
        }

        /* ensure convert button looks the same even when disabled */
        #convertBtn {
            background: #f72d23 !important;
            border-color: #f72d23 !important;
            color: #fff !important;
        }
    </style>
</head>
<body>
    <%-- Include header that was created as partial --%>
    <jsp:include page="/WEB-INF/jsp/partials/header.jsp" />

    <div class="page">
        <c:choose>
            <c:when test="${not empty sessionScope.userId}">
                <div class="card" style="display:flex; flex-direction:column; align-items:center; gap:18px;">
                    <h2>Process — Tải file & theo dõi tiến trình</h2>

                    <div style="margin-top:14px;">
                        <!-- Process UI: empty state and file list -->
                        <form class="process-form" id="processForm" action="${pageContext.request.contextPath}/process" method="POST" enctype="multipart/form-data" novalidate style="width:100%; max-width:920px; box-sizing:border-box; margin:0 auto;">
                            <!-- Hidden native input (we will handle files client-side) -->
                            <input id="pdf-file" name="pdfFile" type="file" accept=".pdf" multiple style="display:none" />

                            <!-- Upload area / empty state -->
                            <div id="uploadArea" style="border:1px dashed #e6e6e6; border-radius:8px; padding:36px; cursor:pointer; background:#fff; display:flex; flex-direction:column; align-items:center; justify-content:center; gap:10px; min-height:140px;">
                                <!-- simple inline cloud-upload SVG centered and preserving aspect ratio -->
                                <svg id="cloudIcon"
                                     width="120"
                                     height="120"
                                     viewBox="0 0 32 32"
                                     fill="none"
                                     xmlns="http://www.w3.org/2000/svg"
                                     preserveAspectRatio="xMidYMid meet"
                                     style="display:block;">

                                    <g transform="translate(4,4)">
                                        <path d="M19.35 10.04A7 7 0 1 0 6.9 8.12 5.5 5.5 0 0 0 7 19h11.35a4.5 4.5 0 0 0 0-8.96z" fill="#f44336"/>
                                        <path d="M12 12v6m0-6l-2 2m2-2 2 2"
                                              stroke="#fff"
                                              stroke-width="1.2"
                                              stroke-linecap="round"
                                              stroke-linejoin="round"/>
                                    </g>
                                </svg>

                                <div style="font-weight:700; font-size:16px; color:#333;">Chọn file để bắt đầu</div>
                                <div style="margin-top:8px; color:#666; font-size:13px; text-align:center; max-width:560px;">Kéo thả hoặc nhấp vào đây để chọn file PDF (hỗ trợ nhiều file)</div>
                            </div>

                            <!-- File list (hidden when empty) -->
                            <div id="fileList" style="margin-top:14px; display:none;">
                                <h3 style="margin-top:18px; margin-bottom:8px;">Danh sách file</h3>
                                <!-- filesContainer has fixed width so each row is fixed width -->
                                <div id="filesContainer" style="display:flex;flex-direction:column; gap:8px; width:800px; box-sizing:border-box; margin:0 auto;"></div>

                                <!-- Controls -->
                                <div id="fileListControls" style="display:flex; align-items:center; gap:12px; margin-top:14px; width:800px; box-sizing:border-box; margin-left:auto; margin-right:auto;">
                                    <button type="button" id="addMoreBtn" class="btn" style="display:inline-flex;align-items:center; gap:8px;">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M12 5v14M5 12h14" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                                        Add more Files
                                    </button>

                                    <button type="button" id="convertBtn" class="btn primary" disabled style="margin-left:auto; background:#f72d23; border-color:#f72d23; color:#fff; display:inline-flex; align-items:center; gap:8px;">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M12 5v10M8 9l4-4 4 4" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                                        Convert
                                    </button>
                                </div>
                                <div id="uploadNotice" style="margin-top:8px; color:#666; font-size:13px; width:800px; margin:8px auto 0 auto; text-align:left;">PDF → DOC (mặc định)</div>
                            </div>

                            <!-- Insert processingTask here (hidden by default) -->
                            <div id="processingTask" style="display:none; width:800px; box-sizing:border-box; margin:0 auto;">
                              <div style="font-weight:700; font-size:16px; color:#333; margin-bottom:8px;">
                                Task đang được xử lí, đến Dashboard để xem tiến độ
                              </div>
                              <div style="display:flex; gap:12px; margin-top:12px;">
                                <button type="button" id="processingBackBtn" class="btn" style="background:#707070; border:1px solid #ccc;">Quay lại</button>
                                <button type="button" id="processingToDashboardBtn" class="btn primary" style="background:#f72d23; border-color:#f72d23; color:#fff;">Đến Dashboard</button>
                              </div>
                            </div>
                        </form>
                    </div>

                    <div id="localTasks" aria-live="polite"></div>
                </div>

                <script>
                    (function(){
                        // Enhanced client-side process UI
                        var form = document.getElementById('processForm');
                        var fileInput = document.getElementById('pdf-file');
                        var uploadArea = document.getElementById('uploadArea');
                        var fileList = document.getElementById('fileList');
                        var filesContainer = document.getElementById('filesContainer');
                        var addMoreBtn = document.getElementById('addMoreBtn');
                        var convertBtn = document.getElementById('convertBtn');
                        var localTasks = document.getElementById('localTasks');
                        var processingTask = document.getElementById('processingTask');
                        var processingBackBtn = document.getElementById('processingBackBtn');
                        var processingToDashboardBtn = document.getElementById('processingToDashboardBtn');

                        var selectedFiles = []; // array of File objects

                        // store original button styles so we can swap and restore them
                        var addBtnOrig = null;
                        var convertBtnOrig = null;
                        function captureButtonStyles() {
                            try {
                                if (addMoreBtn && !addBtnOrig) {
                                    var s = window.getComputedStyle(addMoreBtn);
                                    addBtnOrig = { background: s.backgroundColor, borderColor: s.borderColor, color: s.color };
                                }
                                if (convertBtn && !convertBtnOrig) {
                                    var s2 = window.getComputedStyle(convertBtn);
                                    convertBtnOrig = { background: s2.backgroundColor, borderColor: s2.borderColor, color: s2.color };
                                }
                            } catch (e) {
                                // non-critical
                            }
                        }
                        // capture after a small delay to ensure computed styles available
                        setTimeout(captureButtonStyles, 80);

                        function bytesToSize(bytes) {
                            if (bytes === 0) return '0 B';
                            var k = 1024, sizes = ['B', 'KB', 'MB', 'GB', 'TB'], i = Math.floor(Math.log(bytes) / Math.log(k));
                            return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
                        }

                        // weak map to track pending hide timers per element so overlapping calls are resilient
                        var __fadeTimers = (window.__fadeTimers = window.__fadeTimers || new WeakMap());

                        function fadeIn(el) {
                            if (!el) return;
                            try { console.debug('[UI] fadeIn', el.id, new Date().toISOString()); } catch(e){}
                            // cancel any pending hide for this element
                            var pending = __fadeTimers.get(el);
                            if (pending) {
                                clearTimeout(pending);
                                __fadeTimers.delete(el);
                            }
                            el.classList.remove('fade-out');
                            // when restoring the upload area we need it to be flex so its contents remain centered;
                            // otherwise use block as a safe default for other elements.
                            el.style.display = (el === uploadArea ? 'flex' : 'block');
                            // force reflow then add fade-in
                            void el.offsetWidth;
                            // ensure fade-in class is applied after clearing state
                            el.classList.add('fade-in');
                        }

                        function fadeOut(el) {
                            if (!el) return;
                            try { console.debug('[UI] fadeOut', el.id, new Date().toISOString()); } catch(e){}
                            // clear any previous pending timer for this element
                            var prev = __fadeTimers.get(el);
                            if (prev) {
                                clearTimeout(prev);
                                __fadeTimers.delete(el);
                            }
                            el.classList.remove('fade-in');
                            el.classList.add('fade-out');
                            // schedule hide after animation, but keep reference so it can be cancelled
                            var to = setTimeout(function() {
                                try {
                                    el.style.display = 'none';
                                } catch (e) {
                                    // ignore if element removed from DOM
                                }
                                __fadeTimers.delete(el);
                            }, 220);
                            __fadeTimers.set(el, to);
                        }

                        function applyButtonSwap(toFileListVisible) {
                            try {
                                // ensure we have captured original styles
                                captureButtonStyles();
                                if (!addMoreBtn || !convertBtn) return;

                                // Keep convert button color fixed at #f72d23 always
                                convertBtn.style.background = '#f72d23';
                                convertBtn.style.borderColor = '#f72d23';
                                convertBtn.style.color = '#fff';

                                // Only toggle visual state of the "Add more" button
                                if (toFileListVisible) {
                                    addMoreBtn.classList.add('btn-muted');
                                } else {
                                    addMoreBtn.classList.remove('btn-muted');
                                }
                            } catch (e) {
                                // non-critical
                            }
                        }

                        function renderFileList() {
                            // Ensure filesContainer exists in the live DOM
                            filesContainer = document.getElementById('filesContainer') || filesContainer;

                            filesContainer.innerHTML = '';
                            if (selectedFiles.length === 0) {
                                // hide file list with animation and restore upload area
                                fadeOut(fileList);
                                // show upload area
                                fadeIn(uploadArea);
                                // restore localTasks panel (if present)
                                try { if (localTasks) { fadeIn(localTasks); } } catch(e) {}
                                applyButtonSwap(false);
                                return;
                            }

                            // hide upload area with animation and show file list
                            fadeOut(uploadArea);
                            // hide live localTasks while user reviews file list
                            try { if (localTasks) { fadeOut(localTasks); } } catch(e) {}
                            // small delay to let uploadArea begin hiding before showing list
                            setTimeout(function() {
                                fadeIn(fileList);
                            }, 80);

                            // when file list visible, swap button colors
                            applyButtonSwap(true);

                            // For each selected file create a fixed-width row (800px)
                            selectedFiles.forEach(function(f, idx) {
                                var row = document.createElement('div');
                                row.style.display = 'flex';
                                row.style.alignItems = 'center';
                                row.style.justifyContent = 'space-between';
                                row.style.padding = '10px';
                                row.style.border = '1px solid #f0f0f0';
                                row.style.borderRadius = '6px';
                                row.style.background = '#fff';
                                row.style.height = '56px';           // fixed row height to keep list compact
                                row.style.boxSizing = 'border-box';
                                row.style.overflow = 'hidden';
                                // FIXED width according to requirement
                                row.style.width = '800px';
                                row.style.margin = '0 auto';

                                var left = document.createElement('div');
                                left.style.display = 'flex';
                                left.style.alignItems = 'center';
                                left.style.gap = '12px';
                                // left takes remaining space inside the fixed-width row but won't grow the row
                                left.style.flex = '1 1 auto';
                                left.style.minWidth = '0';

                                var fileIcon = document.createElement('div');
                                // keep icon at fixed size so it doesn't affect text width
                                fileIcon.style.flex = '0 0 28px';
                                fileIcon.style.width = '28px';
                                fileIcon.style.display = 'flex';
                                fileIcon.style.alignItems = 'center';
                                fileIcon.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M14 3H6a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z" stroke="#444" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>';
                                left.appendChild(fileIcon);

                                var nameWrap = document.createElement('div');
                                nameWrap.style.display = 'block';
                                nameWrap.style.overflow = 'hidden';
                                nameWrap.style.textOverflow = 'ellipsis';
                                nameWrap.style.whiteSpace = 'nowrap';
                                // take remaining space inside left and truncate if too long
                                nameWrap.style.flex = '1 1 auto';
                                nameWrap.style.minWidth = '0';
                                nameWrap.style.maxWidth = 'calc(100% - 28px)'; // leave room for icon
                                nameWrap.innerText = f.name + ' • ' + bytesToSize(f.size);

                                left.appendChild(nameWrap);

                                var actions = document.createElement('div');
                                actions.style.display = 'flex';
                                actions.style.gap = '8px';
                                actions.style.alignItems = 'center';
                                // fixed width for actions so it cannot expand the row
                                actions.style.flex = '0 0 64px';
                                actions.style.width = '64px';
                                actions.style.minWidth = '64px';
                                actions.style.justifyContent = 'center';

                                // default is pdf -> doc, no convert select required
                                var removeBtn = document.createElement('button');
                                removeBtn.type = 'button';
                                removeBtn.title = 'Remove';
                                removeBtn.style.background = 'transparent';
                                removeBtn.style.border = 'none';
                                removeBtn.style.color = '#c23';
                                removeBtn.style.cursor = 'pointer';
                                removeBtn.style.fontWeight = 700;
                                removeBtn.innerText = '✕';
                                removeBtn.addEventListener('click', function () {
                                    selectedFiles.splice(idx, 1);
                                    renderFileList();
                                    updateConvertState();
                                });

                                actions.appendChild(removeBtn);

                                row.appendChild(left);
                                row.appendChild(actions);
                                filesContainer.appendChild(row);
                            });
                        }

                        function updateConvertState() {
                            convertBtn = document.getElementById('convertBtn') || convertBtn;
                            convertBtn.disabled = selectedFiles.length === 0;
                            // Always ensure the convert button keeps the desired color even when disabled/enabled
                            try {
                                convertBtn.style.background = '#f72d23';
                                convertBtn.style.borderColor = '#f72d23';
                                convertBtn.style.color = '#fff';
                            } catch (e) {}
                        }

                        function handleFiles(list) {
                            // convert FileList to array and append (prevent duplicates by name/size)
                            var arr = Array.prototype.slice.call(list || []);
                            arr.forEach(function (f) {
                                // basic dedupe by name+size
                                var exists = selectedFiles.some(function (sf) { return sf.name === f.name && sf.size === f.size; });
                                if (!exists) selectedFiles.push(f);
                            });
                            renderFileList();
                            updateConvertState();
                        }

                        // click upload area to open file dialog
                        uploadArea.addEventListener('click', function () {
                            fileInput.click();
                        });

                        // support drag & drop
                        uploadArea.addEventListener('dragover', function (ev) {
                            ev.preventDefault();
                            uploadArea.style.borderColor = '#ddd';
                            uploadArea.style.background = '#fafafa';
                        });
                        uploadArea.addEventListener('dragleave', function (ev) {
                            uploadArea.style.borderColor = '#e6e6e6';
                            uploadArea.style.background = '#fff';
                        });
                        uploadArea.addEventListener('drop', function (ev) {
                            ev.preventDefault();
                            uploadArea.style.borderColor = '#e6e6e6';
                            uploadArea.style.background = '#fff';
                            if (ev.dataTransfer && ev.dataTransfer.files) {
                                handleFiles(ev.dataTransfer.files);
                            }
                        });

                        // native input change
                        fileInput.addEventListener('change', function (ev) {
                            if (fileInput.files && fileInput.files.length) {
                                handleFiles(fileInput.files);
                                // clear the file input so same file can be re-added if removed
                                fileInput.value = '';
                            }
                        });

                        if (addMoreBtn) {
                            addMoreBtn.addEventListener('click', function () {
                                fileInput.click();
                            });
                        }

                        // Bind processing task buttons once (since they are static in HTML)
                        var dashboardUrl = '${pageContext.request.contextPath}/dashboard';
                        if (processingToDashboardBtn) {
                            processingToDashboardBtn.addEventListener('click', function () {
                                window.location.href = dashboardUrl;
                            });
                        }
                        if (processingBackBtn) {
                            processingBackBtn.addEventListener('click', function () {
                                fadeOut(processingTask);
                                selectedFiles = [];
                                renderFileList();
                                updateConvertState();
                            });
                        }

                        // Convert: send selected files via fetch as FormData
                        if (convertBtn) {
                            // We'll wrap the convert behavior in a function so we can rebind later if needed
                            var doConvert = function () {
                                if (selectedFiles.length === 0) return;

                                // Update button state before hiding
                                convertBtn.disabled = true;
                                convertBtn.innerText = 'Converting...';

                                // Hide fileList and show processingTask immediately
                                fadeOut(fileList);
                                try { if (localTasks) { fadeOut(localTasks); } } catch(e) {}
                                setTimeout(function() {
                                    fadeIn(processingTask);
                                }, 80);

                                var fd = new FormData();
                                // append multiple fields with same name 'pdfFile' (server should accept multiple parts)
                                selectedFiles.forEach(function (f) {
                                    fd.append('pdfFile', f, f.name);
                                });

                                // send to server in background
                                fetch(form.action, {
                                    method: 'POST',
                                    body: fd
                                }).then(function (resp) {
                                    if (!resp.ok) {
                                        // Nếu server trả về 500, ta vẫn cố gắng đọc JSON lỗi từ server gửi về
                                        return resp.json().then(function(errData) {
                                            throw new Error(errData.message || 'Server Error ' + resp.status);
                                        }).catch(function() {
                                            // Nếu không đọc được JSON (ví dụ lỗi Tomcat mặc định), ném lỗi HTTP
                                            throw new Error('Upload failed: ' + resp.statusText);
                                        });
                                    }
                                    // Nếu OK, parse JSON
                                    return resp.json();
                                }).then(function (data) {
                                    // Xử lý logic nghiệp vụ dựa trên JSON server trả về
                                    if (data.status === 'queued') {
                                        // THÀNH CÔNG:
                                        console.log('Task IDs:', data.taskIds);

                                        // Hiển thị thông báo hoặc chuyển hướng (nếu cần)
                                        // Hiện tại UI đang ở trạng thái "Processing", bạn có thể giữ nguyên
                                        // hoặc cập nhật text báo "Đã đẩy vào hàng đợi thành công!"

                                        if (data.errors && data.errors.length > 0) {
                                            alert('Một số file bị lỗi: \n' + data.errors.join('\n'));
                                        }
                                    } else {
                                        // Server trả về 200 nhưng logic bị lỗi (ví dụ không có file nào hợp lệ)
                                        throw new Error(data.message || 'Lỗi không xác định từ server');
                                    }
                                }).catch(function(err){
                                    console.error('Upload error', err);
                                    // Hiển thị lỗi chi tiết từ Server gửi về
                                    alert('Có lỗi xảy ra: ' + err.message);

                                    // Ẩn giao diện loading, hiện lại danh sách file
                                    fadeOut(processingTask);
                                    setTimeout(function() {
                                        fadeIn(fileList);
                                    }, 80);
                                }).finally(function(){
                                    // Reset nút convert
                                    convertBtn = document.getElementById('convertBtn') || convertBtn;
                                    try {
                                        convertBtn.disabled = false;
                                        convertBtn.innerText = 'Convert';
                                    } catch (e) {}
                                });
                            };

                            // initial bind
                            convertBtn.addEventListener('click', function () {
                                // re-query convertBtn to ensure we have the live element
                                convertBtn = document.getElementById('convertBtn') || convertBtn;
                                doConvert();
                            });
                        }

                        // Diagnostics: monitor display/class/DOM changes for uploadArea and fileList
                        try {
                            function __diagDump(tag) {
                                try {
                                    console.debug('[DIAG] ' + tag, {
                                        uploadArea: uploadArea ? {
                                            id: uploadArea.id || '(no-id)',
                                            styleDisplay: uploadArea.style.display,
                                            className: uploadArea.className,
                                            computedDisplay: window.getComputedStyle ? window.getComputedStyle(uploadArea).display : '(no-computed)'
                                        } : '(missing)',
                                        fileList: fileList ? {
                                            id: fileList.id || '(no-id)',
                                            styleDisplay: fileList.style.display,
                                            className: fileList.className,
                                            computedDisplay: window.getComputedStyle ? window.getComputedStyle(fileList).display : '(no-computed)'
                                        } : '(missing)'
                                    });
                                } catch (e) {
                                    try { console.log('[DIAG] dump failed', e); } catch(_) {}
                                }
                            }

                            __diagDump('initial');

                            var __diagObserver = new MutationObserver(function(mutations) {
                                try {
                                    mutations.forEach(function(m) {
                                        var targetId = (m.target && m.target.id) || '(no-id)';
                                        console.debug('[DIAG] mutation', {
                                            type: m.type,
                                            targetId: targetId,
                                            attributeName: m.attributeName,
                                            addedNodes: (m.addedNodes && m.addedNodes.length) || 0,
                                            removedNodes: (m.removedNodes && m.removedNodes.length) || 0
                                        });
                                    });
                                    // small async delay to allow style/computed changes to settle before taking snapshot
                                    setTimeout(function() { __diagDump('after-mutation'); }, 8);
                                } catch (e) {
                                    console.error('[DIAG] observer handler error', e);
                                }
                            });

                            if (uploadArea && uploadArea.nodeType === 1) {
                                __diagObserver.observe(uploadArea, { attributes: true, attributeFilter: ['style', 'class'] });
                            }
                            if (fileList && fileList.nodeType === 1) {
                                // observe attribute changes (style/class) and also DOM children changes inside fileList
                                __diagObserver.observe(fileList, { attributes: true, attributeFilter: ['style', 'class'] });
                                __diagObserver.observe(fileList, { childList: true, subtree: true });
                            }

                            // expose diagnosic controls for manual use in console
                            window.__processUI_diag = {
                                observer: __diagObserver,
                                dump: __diagDump,
                                disconnect: function(){ try { __diagObserver.disconnect(); } catch(e){} }
                            };

                            console.debug('[DIAG] setup complete - watching uploadArea and fileList');
                        } catch (e) {
                            console.error('[DIAG] setup failed', e);
                        }

                    })();
                </script>

            </c:when>

            <c:otherwise>
                <div class="card hero">
                    <h2>Chưa đăng nhập</h2>
                    <p class="small">Bạn cần đăng nhập để sử dụng chức năng upload / process file.</p>
                    <a class="btn" href="${pageContext.request.contextPath}/login">Đăng nhập</a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
