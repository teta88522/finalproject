document.addEventListener("DOMContentLoaded", function() {

    // =======================================================================
    // 1. 공통 HTML 요소 가져오기
    // =======================================================================
    const issueSearchInput = document.getElementById("issueSearchInput");
    const searchDropdown = document.getElementById("searchDropdown"); 
    const issueListBody = document.getElementById("issueListBody");    
    
    const versionIdInput = document.getElementById("versionId");
    const selectedIssueIdsInput = document.getElementById("selectedIssueIds");
    const form = document.getElementById("milestoneForm");

    const startDateInput = document.getElementById("startDate");
    const endDateInput = document.getElementById("endDate");
    const dateErrorDiv = document.getElementById("dateError");

    // 장바구니 및 버전 관리 변수
    let selectedIssueIds = new Set();
    // 💡 [수정/생성 호환]: 기존 버전 값이 있으면(수정) 가져오고, 없으면(생성) null로 세팅
    let lockedVersionId = (versionIdInput && versionIdInput.value) ? versionIdInput.value : null; 
    let searchTimeout = null; 

    // =======================================================================
    // 2. 초기화 함수 (수정 페이지일 때만 기존 일감들을 자동으로 장바구니에 세팅)
    // =======================================================================
    function initExistingIssues() {
        const existingCheckboxes = document.querySelectorAll(".issue-checkbox");
        
        // 생성 페이지라면 기존 체크박스가 0개이므로 이 반복문은 자연스럽게 건너뜁니다 (안전)
        existingCheckboxes.forEach(checkbox => {
            if (checkbox.checked) {
                selectedIssueIds.add(checkbox.value); 
            }
            // 기존에 그려진 체크박스들에 이벤트 달아주기
            checkbox.addEventListener('change', handleCheckboxChange);
        });
        updateHiddenInput();
    }

    // =======================================================================
    // 3. 일감 자동완성 검색 기능 (클릭 시 & 타이핑 시 실행)
    // =======================================================================
    function fetchAndShowIssues(keyword) {
        let apiUrl = `/milestones/api/issues/search?keyword=${keyword}`;
        
        // 버전 고정 로직 반영
        if (lockedVersionId !== null && lockedVersionId !== '') {
            apiUrl += `&versionId=${lockedVersionId}`;
        }

        fetch(apiUrl)
            .then(response => response.json())
            .then(data => renderDropdown(data))
            .catch(error => console.error("일감 조회 에러 :", error));
    }

    // 입력창에 타이핑 할 때
    issueSearchInput.addEventListener("input", function() {
        clearTimeout(searchTimeout); 
        const keyword = this.value.trim();

        searchTimeout = setTimeout(() => {
            fetchAndShowIssues(keyword);
        }, 300); 
    });

    // 입력창을 마우스로 클릭(포커스) 했을 때
    issueSearchInput.addEventListener("focus", function() {
        const keyword = this.value.trim();
        fetchAndShowIssues(keyword);
    });

    // 드롭다운 그려주기
    function renderDropdown(data) {
        searchDropdown.innerHTML = ''; 

        if (data.length === 0) {
            searchDropdown.innerHTML = `<li class="search-dropdown-item" style="color:#999; cursor:default;">조회된 일감이 없습니다.</li>`;
            searchDropdown.style.display = "block";
            return;
        }

        data.forEach(issue => {
            if(selectedIssueIds.has(issue.issueId)) return; // 장바구니에 이미 있으면 제외

            const li = document.createElement("li");
            li.className = "search-dropdown-item";
            li.innerHTML = `
                <strong>${issue.issueId}</strong> - ${issue.title}
                <span class="badge badge-right" style="color: #0056b3;">${issue.statusName || '상태'}</span>
            `;
            
            li.addEventListener("click", function() {
                addIssueToTable(issue); 
                searchDropdown.style.display = "none"; 
                issueSearchInput.value = ""; 
            });

            searchDropdown.appendChild(li);
        });

        if(searchDropdown.children.length > 0) {
            searchDropdown.style.display = "block"; 
        } else {
            searchDropdown.style.display = "none";
        }
    }

    // =======================================================================
    // 4. 선택한 일감을 하단 표(장바구니)에 추가 및 제어
    // =======================================================================
    function addIssueToTable(issue) {
        const emptyRow = document.querySelector("#issueListBody .empty-row");
        if (emptyRow) emptyRow.closest("tr").remove();

        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td style="text-align: center;">
                <input type="checkbox" class="issue-checkbox" 
                       value="${issue.issueId}" 
                       data-version-id="${issue.versionId}" checked>
            </td>
            <td>${issue.title} (${issue.issueId})</td>
            <td style="text-align: center;"><span class="status-badge">${issue.statusName || '상태'}</span></td>
        `;
        
        issueListBody.prepend(tr);

        const newCheckbox = tr.querySelector(".issue-checkbox");
        selectedIssueIds.add(issue.issueId);
        
        // 첫 번째 일감 담을 때 버전 고정
        if (!lockedVersionId) {
            lockedVersionId = issue.versionId;
            if(versionIdInput) versionIdInput.value = lockedVersionId;
        }
        
        newCheckbox.addEventListener('change', handleCheckboxChange);
        updateHiddenInput(); 
    }

    // 체크박스 상태 변경 처리
    function handleCheckboxChange() {
        const clickedIssueId = this.value;
        const clickedVersionId = this.getAttribute("data-version-id");

        if (this.checked) {
            selectedIssueIds.add(clickedIssueId);
            if (!lockedVersionId) {
                lockedVersionId = clickedVersionId;
                if(versionIdInput) versionIdInput.value = lockedVersionId;
            }
        } else {
            selectedIssueIds.delete(clickedIssueId);
            // 장바구니가 완전히 비면 버전 잠금 해제
            if (selectedIssueIds.size === 0) {
                lockedVersionId = null;
                if(versionIdInput) versionIdInput.value = '';
            }
        }
        updateHiddenInput();
    }

    function updateHiddenInput() {
        if(selectedIssueIdsInput) {
            selectedIssueIdsInput.value = Array.from(selectedIssueIds).join(",");
        }
    }

    // 바깥 영역 클릭 시 드롭다운 닫기
    document.addEventListener("click", function(event) {
        if (issueSearchInput && !issueSearchInput.contains(event.target) && searchDropdown && !searchDropdown.contains(event.target)) {
            searchDropdown.style.display = "none";
        }
    });

    // =======================================================================
    // 5. 공통 폼 전송 시 유효성 검사
    // =======================================================================
    if (form) {
        form.addEventListener("submit", function(event) {
            let isValid = true; 

            // 필수 입력값 검증
            const requiredInputs = document.querySelectorAll(".required-input");
            requiredInputs.forEach(input => {
                if (input.value.trim() === '') { 
                    input.classList.add("error-border"); 
                    isValid = false; 
                } else {
                    input.classList.remove("error-border"); 
                }
            });

            // 날짜 비교 로직
            if (startDateInput && endDateInput && startDateInput.value && endDateInput.value) { 
                const startDate = new Date(startDateInput.value);
                const endDate = new Date(endDateInput.value);

                if (startDate > endDate) { 
                    startDateInput.classList.add('error-border');
                    endDateInput.classList.add('error-border');
                    if(dateErrorDiv) dateErrorDiv.style.display = "block"; 
                    isValid = false; 
                } else {
                    startDateInput.classList.remove('error-border');
                    endDateInput.classList.remove('error-border');
                    if(dateErrorDiv) dateErrorDiv.style.display = "none"; 
                }
            }

            if (!isValid) { 
                event.preventDefault(); 
            }
        });
    }

    // 에러 테두리 실시간 제거
    document.querySelectorAll('.required-input').forEach(input => {
        input.addEventListener('change', function() {
            this.classList.remove('error-border');
        });
    });

    // =======================================================================
    // 6. 스크립트 실행 (수정 페이지라면 데이터 세팅 함수가 작동함)
    // =======================================================================
    initExistingIssues();
});