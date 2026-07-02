document.addEventListener("DOMContentLoaded", function () {
    
    // 타이머 변수를 최상단에 선언하여 공유 선언
    let timerInterval = null;

    // ==========================================================
    // 1. 개인정보 수정 모드 토글 기능
    // ==========================================================
    const btnEdit = document.getElementById("btnEdit");
    const btnSave = document.getElementById("btnSave");
    const profileForm = document.getElementById("profileForm");
    
    if (profileForm) {
        const editableInputs = profileForm.querySelectorAll("input[name='userName'], input[name='email'], input[name='phone']");

        if (btnEdit && btnSave) {
            btnEdit.addEventListener("click", function () {
                editableInputs.forEach(input => {
                    input.removeAttribute("readonly");
                    input.style.border = "1px solid #4e73df"; 
                });
                btnEdit.style.display = "none";
                btnSave.style.display = "inline-block";
            });
        }

        profileForm.addEventListener("submit", function (e) {
            e.preventDefault(); 
            const formData = new FormData(profileForm);

            fetch("/updateUser", {
                method: "POST",
                body: formData
            })
            .then(res => res.json())
            .then(data => {
                if (data.update === true) {
                    alert(data.message);
                    location.href = "/mypage"; 
                } else {
                    alert(data.message);
                }
            })
            .catch(err => {
                console.error(err);
                alert("프로필 수정 중 오류가 발생했습니다.");
            });
        });
    }

    // ==========================================================
    // 2. 비밀번호 변경 모달 폼 검증 및 비동기 전송
    // ==========================================================
    const passwordForm = document.getElementById("passwordForm");
    const newPassword = document.getElementById("newPassword");
    const confirmPassword = document.getElementById("confirmPassword");

    if (passwordForm) {
        passwordForm.addEventListener("submit", function (e) {
            e.preventDefault(); 

            if (newPassword.value !== confirmPassword.value) {
                alert("새로 입력하신 비밀번호가 일치하지 않습니다. 다시 확인해 주세요.");
                confirmPassword.focus();
                return;
            }

            const params = new URLSearchParams();
            params.append("currentPassword", passwordForm.querySelector("[name='currentPassword']").value);
            params.append("newPassword", newPassword.value);

            fetch("/updatePassword", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: params
            })
            .then(res => res.json())
            .then(data => {
                if (data.result === true) {
                    alert(data.message);
                    location.href = "/mypage"; 
                } else {
                    alert(data.message); 
                }
            })
            .catch(err => {
                console.error(err);
                alert("비밀번호 변경 중 오류가 발생했습니다.");
            });
        });
    }

    // ==========================================================
    // 3. 프로젝트 MyBatis 서버 비동기 검색 기능 (ID 및 URL 매칭 완료)
    // ==========================================================
    const btnSearch = document.getElementById("btnSearch"); 
    const tableBody = document.getElementById("projectTableBody");

    function searchProjects() {
        // HTML 구조와 일치하도록 ID 명칭 전체 수정
        const params = {
            projectName: document.getElementById("searchProjectName").value,
            startDate: document.getElementById("searchStartDate").value,
            endDate: document.getElementById("searchEndDate").value,
            ownerName: document.getElementById("searchOwner").value,
            statusCode: document.getElementById("searchStatus").value
        };

        // UsersController의 @PostMapping("/search") 주소와 일치하도록 수정
        fetch("/search", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(params)
        })
        .then(res => res.json())
        .then(list => {
            tableBody.innerHTML = "";

            if (list.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="8" class="no-data">검색 결과가 없습니다.</td></tr>';
                return;
            }

            // 결과 리스트 렌더링 (날짜 데이터 포맷팅 안전 처리 추가)
            list.forEach(p => {
                const formattedCreateDate = p.createdAt ? p.createdAt.split('T')[0] : '';
                const formattedEndDate = p.endDate ? p.endDate.split('T')[0] : '';
                
                const row = `
                    <tr>
                        <td><input type="checkbox" class="project-check" /></td>
                        <td>${p.projectName || ''}</td>
                        <td>${p.description || ''}</td>
                        <td>${p.ownerName || ''}</td>
                        <td><a href="${p.projectUrl || '#'}" target="_blank">${p.projectUrl || ''}</a></td>
                        <td>
                            <span class="status-badge ${p.statusCode == 'a001' ? 'status-ready' : (p.statusCode == 'a002' ? 'status-ing' : 'status-done')}">
                                ${p.statusCode == 'a001' ? '예정' : (p.statusCode == 'a002' ? '진행중' : '종료')}
                            </span>
                        </td>
                        <td>${formattedCreateDate}</td>
                        <td>${formattedEndDate}</td>
                    </tr>
                `;
                tableBody.insertAdjacentHTML('beforeend', row);
            });
        })
        .catch(err => console.error("검색 오류:", err));
    }

    // 검색 버튼 클릭 시 실행
    btnSearch?.addEventListener("click", searchProjects);

    // 초기화 버튼 이벤트 바인딩
    document.getElementById("btnFilterReset")?.addEventListener("click", function() {
        document.getElementById("searchProjectName").value = "";
        document.getElementById("searchStartDate").value = "";
        document.getElementById("searchEndDate").value = "";
        document.getElementById("searchOwner").value = "";
        document.getElementById("searchStatus").value = "";
        searchProjects(); 
    });

    // ==========================================================
    // 4. 테이블 체크박스 전체 선택/해제 기능
    // ==========================================================
    const checkAll = document.getElementById("checkAll");

    if (checkAll) {
        checkAll.addEventListener("change", function () {
            const projectChecks = document.querySelectorAll(".project-check");
            projectChecks.forEach(check => {
                const row = check.closest("tr");
                if (row && row.style.display !== "none") {
                    check.checked = checkAll.checked;
                }
            });
        });
    }

    // ==========================================================
    // 5. 구독 신청 이메일 인증 발송 및 타이머 연동
    // ==========================================================
    const btnSendAuthCode = document.getElementById("btnSendAuthCode");
    const subscribeEmail = document.getElementById("subscribeEmail");
    const authCode = document.getElementById("authCode");
    const authTimer = document.getElementById("authTimer");
    const btnSubmitSubscribe = document.getElementById("btnSubmitSubscribe");

    if (btnSendAuthCode) {
        btnSendAuthCode.addEventListener("click", function() {
            const emailVal = subscribeEmail.value.trim();
            
            if (!emailVal) {
                alert("이메일 주소를 입력해 주세요.");
                subscribeEmail.focus();
                return;
            }
   
            fetch("/subscribe/sendCode", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ email: emailVal })
            })
            .then(res => res.json())
            .then(data => {
                if (data.result === true) {
                    alert("입력하신 이메일로 인증코드가 발송되었습니다. 3분 내에 입력해 주세요.");
                    authCode.disabled = false;
                    btnSubmitSubscribe.disabled = false;
                    authCode.focus();
                    startAuthTimer(180); 
                } else {
                    alert(data.message || "인증코드 발송에 실패했습니다.");
                }
            })
            .catch(err => {
                console.error(err);
                alert("메일 전송 요청 중 오류가 발생했습니다.");
            });
        });
    }
   
    function startAuthTimer(durationSeconds) {
        clearInterval(timerInterval); 
        authTimer.style.display = "inline"; 
        let timeRemaining = durationSeconds;
        timerInterval = setInterval(function() {
            let minutes = Math.floor(timeRemaining / 60);
            let seconds = timeRemaining % 60;
            minutes = minutes < 10 ? "0" + minutes : minutes;
            seconds = seconds < 10 ? "0" + seconds : seconds;
            authTimer.textContent = `${minutes}:${seconds}`;
            if (--timeRemaining < 0) {
                clearInterval(timerInterval);
                authTimer.textContent = "만료됨";
                authCode.disabled = true; 
                btnSubmitSubscribe.disabled = true;
                alert("인증 시간이 만료되었습니다. 인증코드를 다시 발송해 주세요.");
            }
        }, 1000);
    }

    // ==========================================================
    // 6. 인증코드 검증 및 구독 완료 처리 (안전하게 스코프 내부로 이동)
    // ==========================================================
    const subscribeForm = document.getElementById("subscribeForm");

    if (subscribeForm) {
        subscribeForm.addEventListener("submit", function(e) {
            e.preventDefault(); 

            const inputCode = authCode.value.trim();
            if (!inputCode) {
                alert("인증코드를 입력해 주세요.");
                authCode.focus();
                return;
            }

            fetch("/subscribe/verify", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ authCode: inputCode })
            })
            .then(res => res.json())
            .then(data => {
                if (data.result === true) {
                    alert(data.message);
                    if (timerInterval) clearInterval(timerInterval);
                    $('#subscribeModal').modal('hide'); 
                    location.reload(); 
                } else {
                    alert(data.message); 
                    authCode.focus();
                }
            })
            .catch(err => {
                console.error(err);
                alert("인증 처리 중 오류가 발생했습니다.");
            });
        });
    }
});