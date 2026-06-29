document.addEventListener("DOMContentLoaded", function () {
    
    // ==========================================================
    // 1. 개인정보 수정 모드 토글 기능
    // ==========================================================
    const btnEdit = document.getElementById("btnEdit");
    const btnSave = document.getElementById("btnSave");
    const profileForm = document.getElementById("profileForm");
    
    // form 내부에 readonly 속성을 토글할 타겟 input들 지정
    const editableInputs = profileForm.querySelectorAll("input[name='userName'], input[name='email'], input[name='phone']");

    if (btnEdit && btnSave) {
        btnEdit.addEventListener("click", function () {
            // 수정 버튼 클릭 시 input들의 readonly를 제거해 편집 모드로 바꿉니다.
            editableInputs.forEach(input => {
                input.removeAttribute("readonly");
                input.style.border = "1px solid #4e73df"; // 입력 활성화 시각적 표현
            });
            
            // 버튼 상태 전환
            btnEdit.style.display = "none";
            btnSave.style.display = "inline-block";
        });
    }

    if (profileForm) {
        profileForm.addEventListener("submit", function (e) {
            e.preventDefault(); // 브라우저가 주소창을 이동시키는 기본 submit 동작 중단

            const formData = new FormData(profileForm);

            fetch("/updateUser", {
                method: "POST",
                body: formData
            })
            .then(res => res.json())
            .then(data => {
                if (data.update === true) {
                    alert(data.message);
                    location.href = "/mypage"; // 성공 시 마이페이지로 새로고침 이동
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
            e.preventDefault(); // 기본 submit 동작 중단

            // 1차 비밀번호 일치 검증
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
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: params
            })
            .then(res => res.json())
            .then(data => {
                if (data.result === true) {
                    alert(data.message);
                    location.href = "/mypage"; // 성공 시 마이페이지로 새로고침 이동
                } else {
                    alert(data.message); // 실패 시 에러 알림 (모달은 그대로 유지)
                }
            })
            .catch(err => {
                console.error(err);
                alert("비밀번호 변경 중 오류가 발생했습니다.");
            });
        });
    }

    // ==========================================================
    // 3. 프로젝트 실시간 클라이언트 필터링 기능 (비동기 초고속 필터)
    // ==========================================================
    const filterProjInput = document.getElementById("filterProjectNameInput");
    const filterProjSelect = document.getElementById("filterProjectNameSelect");
    const filterStart = document.getElementById("filterStartDate");
    const filterEnd = document.getElementById("filterEndDate");
    const filterOwner = document.getElementById("filterOwner");
    const filterStatus = document.getElementById("filterStatus");
    const btnReset = document.getElementById("btnFilterReset");
    const projectRows = document.querySelectorAll(".project-row");

    // 필터 실행 함수
    function applyFilter() {
        const projInputVal = filterProjInput.value.toLowerCase();
        const projSelectVal = filterProjSelect.value;
        const startDateVal = filterStart.value; // yyyy-mm-dd
        const endDateVal = filterEnd.value;     // yyyy-mm-dd
        const ownerVal = filterOwner.value.toLowerCase();
        const statusVal = filterStatus.value;

        projectRows.forEach(row => {
            // HTML data-* 속성으로부터 파싱
            const rowProjName = (row.getAttribute("data-project-name") || "").toLowerCase();
            const rowStartDate = row.getAttribute("data-start-date") || "";
            const rowEndDate = row.getAttribute("data-end-date") || "";
            const rowOwner = (row.getAttribute("data-owner-name") || "").toLowerCase();
            const rowStatusCode = row.getAttribute("data-status-code") || "";

            let isMatch = true;

            // 1) 프로젝트 명 검색창 필터
            if (projInputVal && !rowProjName.includes(projInputVal)) {
                isMatch = false;
            }
            // 2) 프로젝트 명 셀렉트박스 필터
            if (projSelectVal && rowProjName !== projSelectVal.toLowerCase()) {
                isMatch = false;
            }
            // 3) 시작일 범위 필터 (시작일이 검색 범위 내에 있는지 비교)
            if (startDateVal && rowStartDate < startDateVal) {
                isMatch = false;
            }
            if (endDateVal && rowStartDate > endDateVal) {
                isMatch = false;
            }
            // 4) 담당자 필터
            if (ownerVal && !rowOwner.includes(ownerVal)) {
                isMatch = false;
            }
            // 5) 상태 코드 필터
            if (statusVal && rowStatusCode !== statusVal) {
                isMatch = false;
            }

            // 조건에 맞으면 보이고, 맞지 않으면 숨김
            if (isMatch) {
                row.style.display = "";
            } else {
                row.style.display = "none";
            }
        });
    }

    // 각 필터 요소에 이벤트 연결 (값이 바뀔 때마다 즉시 반영)
    if (filterProjInput) filterProjInput.addEventListener("input", applyFilter);
    if (filterProjSelect) filterProjSelect.addEventListener("change", applyFilter);
    if (filterStart) filterStart.addEventListener("change", applyFilter);
    if (filterEnd) filterEnd.addEventListener("change", applyFilter);
    if (filterOwner) filterOwner.addEventListener("input", applyFilter);
    if (filterStatus) filterStatus.addEventListener("change", applyFilter);

    // 필터 초기화
    if (btnReset) {
        btnReset.addEventListener("click", function () {
            filterProjInput.value = "";
            filterProjSelect.value = "";
            filterStart.value = "";
            filterEnd.value = "";
            filterOwner.value = "";
            filterStatus.value = "";
            
            // 모든 행 원상복구
            projectRows.forEach(row => {
                row.style.display = "";
            });
        });
    }

    // ==========================================================
    // 4. 테이블 체크박스 전체 선택/해제 기능
    // ==========================================================
    const checkAll = document.getElementById("checkAll");
    const projectChecks = document.querySelectorAll(".project-check");

    if (checkAll) {
        checkAll.addEventListener("change", function () {
            projectChecks.forEach(check => {
                // 부모 row가 display: 'none'인 것은 제외하고 체크 처리
                const row = check.closest("tr");
                if (row && row.style.display !== "none") {
                    check.checked = checkAll.checked;
                }
            });
        });
    }
     // ==========================================================
    // 5. 구독 신청 이메일 인증 발송 및 타이머 연동 (신규 추가)
    // ==========================================================
    const btnSendAuthCode = document.getElementById("btnSendAuthCode");
    const subscribeEmail = document.getElementById("subscribeEmail");
    const authCode = document.getElementById("authCode");
    const authTimer = document.getElementById("authTimer");
    const btnSubmitSubscribe = document.getElementById("btnSubmitSubscribe");
    let timerInterval = null;
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
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: new URLSearchParams({ email: emailVal })
            })
            .then(res => res.json())
            .then(data => {
                if (data.result === true) {
                    alert("입력하신 이메일로 인증코드가 발송되었습니다. 3분 내에 입력해 주세요.");
                    
                    // 1. 인증번호 입력창 및 완료 버튼 활성화
                    authCode.disabled = false;
                    btnSubmitSubscribe.disabled = false;
                    authCode.focus();
                    // 2. 타이머 가동 (기존 타이머가 돌고 있다면 초기화 후 재시작)
                    startAuthTimer(180); // 3분 = 180초
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
        clearInterval(timerInterval); // 돌고 있던 이전 타이머 해제
        authTimer.style.display = "inline"; // 타이머 텍스트 노출
        let timeRemaining = durationSeconds;
        timerInterval = setInterval(function() {
            let minutes = Math.floor(timeRemaining / 60);
            let seconds = timeRemaining % 60;
            // 한 자리 숫자일 때 앞에 0을 붙여 02:05 형식 유지
            minutes = minutes < 10 ? "0" + minutes : minutes;
            seconds = seconds < 10 ? "0" + seconds : seconds;
            authTimer.textContent = `${minutes}:${seconds}`;
            if (--timeRemaining < 0) {
                clearInterval(timerInterval);
                authTimer.textContent = "만료됨";
                authCode.disabled = true; // 시간 만료 시 입력창 다시 잠금
                btnSubmitSubscribe.disabled = true;
                alert("인증 시간이 만료되었습니다. 인증코드를 다시 발송해 주세요.");
            }
        }, 1000);
    }
});