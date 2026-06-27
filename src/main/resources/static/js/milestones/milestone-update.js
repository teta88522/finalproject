document.addEventListener("DOMContentLoaded", function() {

    const issueSearchInput = document.getElementById("issueSearchInput");
    const searchDropdown = document.getElementById("searchDropdown"); 
    const issueListBody = document.getElementById("issueListBody");    
    const versionIdInput = document.getElementById("versionId");
    const selectedIssueIdsInput = document.getElementById("selectedIssueIds");
    const form = document.getElementById("milestoneForm");
    const startDateInput = document.getElementById("startDate");
    const endDateInput = document.getElementById("endDate");
    const dateErrorDiv = document.getElementById("dateError");
    const startDatePastError = document.getElementById("startDatePastError"); // 추가된 에러 메시지 요소
	const projectId = document.getElementById('projectId').value;
    // [상태 관리 변수]
    let selectedIssueIds = new Set();
    let addedHistoryIds = new Set();  
    let lockedVersionId = versionIdInput.value || null; 
    let searchTimeout = null; 

    // --- [오늘 날짜 구하기 및 달력 과거 날짜 차단] ---
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    const todayStr = `${yyyy}-${mm}-${dd}`;

    if (startDateInput) {
        startDateInput.setAttribute('min', todayStr); // 달력 UI에서 오늘 이전 날짜 비활성화
    }
    // ------------------------------------------------
	if (endDateInput) {
	      endDateInput.setAttribute('min', todayStr); // 목표일자 달력도 기본적으로 과거 차단
	  }

	  // 시작일을 선택(변경)했을 때, 목표일자의 최소 선택 가능 날짜를 시작일로 동기화
	  if (startDateInput && endDateInput) {
	      startDateInput.addEventListener('change', function() {
	          endDateInput.setAttribute('min', this.value); 
	          
	          // 만약 이미 선택해둔 목표일자가 새로 바꾼 시작일보다 빠르다면, 목표일자 초기화
	          if (endDateInput.value && endDateInput.value < this.value) {
	              endDateInput.value = '';
	          }
	      });
	  }
	  
	  const statusSelect = document.getElementById("statusCode");

	      function toggleStartDateLock() {
	          if (statusSelect && startDateInput) {
	              // 'L002' (진행 중) 인 경우
	              if (statusSelect.value === 'L002') { 
	                  startDateInput.readOnly = true; // 읽기 전용으로 변경
	                  startDateInput.style.backgroundColor = '#e9ecef'; // 회색 배경으로 변경 (비활성화 느낌)
	                  startDateInput.style.pointerEvents = 'none'; // 달력 아이콘 클릭 원천 차단
	                  startDateInput.title = '진행 중인 마일스톤의 시작일은 변경할 수 없습니다.';
	              } else {
	                  // 그 외 상태 (진행 예정 등) 일 경우 잠금 해제
	                  startDateInput.readOnly = false;
	                  startDateInput.style.backgroundColor = '';
	                  startDateInput.style.pointerEvents = 'auto';
	                  startDateInput.title = '';
	              }
	          }
	      }

	      // 1. 페이지가 처음 열렸을 때 기존 상태를 읽어서 즉시 적용
	      toggleStartDateLock();

	      // 2. 사용자가 드롭다운에서 상태를 변경할 때마다 실시간 적용
	      if (statusSelect) {
	          statusSelect.addEventListener('change', toggleStartDateLock);
	      }
	      // ------------------------------------------------
    // 기존 연결 일감 불러오기
    function initExistingIssues() {
        const existingCheckboxes = document.querySelectorAll(".issue-checkbox");
        existingCheckboxes.forEach(checkbox => {
            if (checkbox.checked) {
                const issueId = parseInt(checkbox.value) || checkbox.value;
                selectedIssueIds.add(issueId); 
                addedHistoryIds.add(issueId); 
            }
            checkbox.addEventListener('change', handleCheckboxChange);
        });
        updateHiddenInput();
        updateCheckboxUI(); 
    }

    // 일감 검색 API 호출 함수
    function fetchAndShowIssues(keyword) {
		let apiUrl = `/project/${projectId}/milestones/api/issues/search?keyword=${keyword}`;
        
        if (lockedVersionId !== null && lockedVersionId !== '') {
            apiUrl += `&versionId=${lockedVersionId}`;
        }

        fetch(apiUrl)
            .then(response => response.json())
            .then(data => renderDropdown(data))
            .catch(error => console.error("일감 조회 에러 :", error));
    }

    // 검색창 타이핑 시
    issueSearchInput.addEventListener("input", function() {
        clearTimeout(searchTimeout); 
        const keyword = this.value.trim();
        searchTimeout = setTimeout(() => { fetchAndShowIssues(keyword); }, 300); 
    });

    // 검색창 클릭(포커스) 시
    issueSearchInput.addEventListener("focus", function() {
        const keyword = this.value.trim();
        fetchAndShowIssues(keyword);
    });

    // [드롭다운 렌더링 함수]
    function renderDropdown(data) {
        searchDropdown.innerHTML = ''; 

        if (data.length === 0) {
            searchDropdown.innerHTML = `<li class="search-dropdown-item" style="color:#999; cursor:default;">조회된 일감이 없습니다.</li>`;
            searchDropdown.style.display = "block";
            return;
        }

        data.forEach(issue => {
            const li = document.createElement("li");
            li.className = "search-dropdown-item";

            const isUsed = addedHistoryIds.has(issue.issueId) || addedHistoryIds.has(String(issue.issueId));
            const isDifferentVersion = (lockedVersionId !== null && lockedVersionId !== "" && String(issue.versionId) !== String(lockedVersionId));

            if (isUsed || isDifferentVersion) {
                li.style.color = "#ccc";
                li.style.cursor = "not-allowed";
                
                if (isUsed) {
                    li.innerHTML = `<strong>${issue.versionName || '버전 없음'}</strong> - ${issue.title} <span style="font-size:0.85em; color:#e74c3c; margin-left:5px;">(이미 추가됨)</span>`;
                } else {
                    li.innerHTML = `<strong>${issue.versionName || '버전 없음'}</strong> - ${issue.title} <span style="font-size:0.85em; color:#e74c3c; margin-left:5px;">(버전 불일치)</span>`;
                }
            } else {
                li.innerHTML = `
                    <strong>${issue.versionName || '버전 없음'}</strong> - ${issue.title}
                    <span class="badge badge-right" style="color: #0056b3;">${issue.issueStatusName || '미정'}</span>
                `;
                
                li.addEventListener("click", function() {
                    addIssueToTable(issue); 
                    searchDropdown.style.display = "none"; 
                    issueSearchInput.value = ""; 
                });
            }

            searchDropdown.appendChild(li);
        });

        searchDropdown.style.display = (searchDropdown.children.length > 0) ? "block" : "none";
    }

    // [표에 일감 추가 함수]
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
            <td>${issue.title} (${issue.versionName})</td>
            <td style="text-align: center;"><span class="status-badge">${issue.issueStatusName || '미정'}</span></td>
        `;
        
        issueListBody.prepend(tr);

        const newCheckbox = tr.querySelector(".issue-checkbox");
        
        selectedIssueIds.add(issue.issueId);
        addedHistoryIds.add(issue.issueId);
        
        if (!lockedVersionId) {
            lockedVersionId = issue.versionId;
            if (versionIdInput) versionIdInput.value = lockedVersionId;
        }
        
        newCheckbox.addEventListener('change', handleCheckboxChange);
        updateHiddenInput(); 
        updateCheckboxUI();
    }

    // [체크박스 상태 변경 로직]
    function handleCheckboxChange() {
        const clickedIssueId = parseInt(this.value) || this.value; 
        const clickedVersionId = this.getAttribute("data-version-id");

        if (this.checked) {
            selectedIssueIds.add(clickedIssueId);
            if (!lockedVersionId) {
                lockedVersionId = clickedVersionId;
                if (versionIdInput) versionIdInput.value = lockedVersionId;
            }
        } else {
            selectedIssueIds.delete(clickedIssueId);
            
            if (selectedIssueIds.size === 0) {
                lockedVersionId = null;
                if (versionIdInput) versionIdInput.value = '';
            }
        }
        
        updateHiddenInput();
        updateCheckboxUI(); 
    }

    // 표에 있는 모든 체크박스의 활성/비활성 상태 갱신 함수
    function updateCheckboxUI() {
        const allCheckboxes = document.querySelectorAll(".issue-checkbox");
        
        allCheckboxes.forEach(cb => {
            const cbVersionId = cb.getAttribute("data-version-id");
            const tr = cb.closest("tr"); 
            
            if (lockedVersionId !== null && String(cbVersionId) !== String(lockedVersionId)) {
                cb.disabled = true;                
                tr.style.color = "#ccc";           
                tr.style.backgroundColor = "#f9f9f9"; 
                tr.title = "현재 선택된 일감들과 버전이 달라 추가할 수 없습니다."; 
            } else {
                cb.disabled = false;
                tr.style.color = "";
                tr.style.backgroundColor = "";
                tr.title = "";
            }
        });
    }

    // hidden input 태그에 값 동기화
    function updateHiddenInput() {
        if (selectedIssueIdsInput) {
            selectedIssueIdsInput.value = Array.from(selectedIssueIds).join(",");
        }
    }

    // 드롭다운 바깥 클릭 시 닫기
    document.addEventListener("click", function(event) {
        if (!issueSearchInput.contains(event.target) && searchDropdown && !searchDropdown.contains(event.target)) {
            searchDropdown.style.display = "none";
        }
    });

    // --- [폼 전송 시 최종 검증] ---
    form.addEventListener("submit", function(event) {
        let isValid = true; 

        // 1. 필수 입력값 체크
        const requiredInputs = document.querySelectorAll(".required-input");
        requiredInputs.forEach(input => {
            if (input.value.trim() === '') { 
                input.classList.add("error-border"); 
                isValid = false; 
            } else {
                input.classList.remove("error-border"); 
            }
        });

        // 2. 날짜 논리 검증
        if (startDateInput.value) {
            // 과거 날짜 입력 차단
            if (startDateInput.value < todayStr) {
                startDateInput.classList.add('error-border');
                if (startDatePastError) startDatePastError.style.display = "block";
                isValid = false;
            } else {
                startDateInput.classList.remove('error-border');
                if (startDatePastError) startDatePastError.style.display = "none";
            }
            
            // 목표 일자와의 선후 관계 검증
            if (endDateInput.value) {
                if (startDateInput.value > endDateInput.value) { 
                    startDateInput.classList.add('error-border');
                    endDateInput.classList.add('error-border');
                    dateErrorDiv.style.display = "block"; 
                    isValid = false; 
                } else {
                    endDateInput.classList.remove('error-border');
                    dateErrorDiv.style.display = "none"; 
                }
            }
        }

        if (!isValid) { event.preventDefault(); }
    });

    document.querySelectorAll('.required-input').forEach(input => {
        input.addEventListener('change', function() {
            this.classList.remove('error-border');
        });
    });

    // 수정 화면 전용: 열리자마자 기존 세팅 호출
    initExistingIssues();
});