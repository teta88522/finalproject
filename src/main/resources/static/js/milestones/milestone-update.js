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
    const startDatePastError = document.getElementById("startDatePastError"); 
	const projectId = document.getElementById('projectId').value;
    // [상태 관리 변수]
    let selectedIssueIds = new Set();
    let addedHistoryIds = new Set();  
    let lockedVersionId = versionIdInput ? versionIdInput.value : null; 
    let searchTimeout = null; 

    // 📝 연결할 버전(로드맵) 수동 선택 변경 시 필터 갱신 및 무결성 제어 리스너
    if (versionIdInput) {
        versionIdInput.addEventListener('change', function() {
            if (selectedIssueIds.size > 0) {
                window.PFDialog.confirm({
                    title: '버전 변경 확인',
                    message: '버전을 변경하시면 기존에 선택해 둔 일감 목록이 모두 초기화됩니다. 계속하시겠습니까?',
                    confirmText: '변경',
                    icon: 'warning'
                }).then((confirmed) => {
                    if (confirmed) {
                        // 일감 목록 테이블 비우기
                        issueListBody.innerHTML = `<tr><td class="empty-row" style="text-align: center; color: #999; padding: 30px 10px;">상단 검색창을 통해 연결할 일감을 찾아보세요.</td></tr>`;
                        selectedIssueIds.clear();
                        addedHistoryIds.clear();
                        updateHiddenInput();
                        lockedVersionId = versionIdInput.value;
                        
                        // 검색창 내용이 적혀있었다면 즉각 리프레시 검색
                        const keyword = issueSearchInput.value.trim();
                        fetchAndShowIssues(keyword);
                    } else {
                        // 취소 시 이전 버전으로 select 값 원복
                        versionIdInput.value = lockedVersionId || "";
                    }
                });
            } else {
                lockedVersionId = this.value;
                // 검색창 내용이 적혀있었다면 즉각 리프레시 검색
                const keyword = issueSearchInput.value.trim();
                fetchAndShowIssues(keyword);
            }
        });
    }

    // --- [날짜 선후 관계 설정] ---
    if (startDateInput && endDateInput) {
 
        if (startDateInput.value) {
            endDateInput.setAttribute('min', startDateInput.value);
        }
        
        // 시작일을 선택(변경)했을 때, 목표일자의 최소 선택 가능 날짜를 시작일로 동기화
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
	          if (statusSelect && startDateInput && endDateInput) {
	              const status = statusSelect.value;
	              const allCheckboxes = document.querySelectorAll(".issue-checkbox");
 
	              if (status === 'L003') { // [완료 상태일 때]
	                  // A. 날짜 필드 전체 락 (시작일 + 목표일)
	                  startDateInput.readOnly = true;
	                  startDateInput.style.backgroundColor = '#e9ecef';
	                  startDateInput.style.pointerEvents = 'none';
	                  
	                  endDateInput.readOnly = true;
	                  endDateInput.style.backgroundColor = '#e9ecef';
	                  endDateInput.style.pointerEvents = 'none';
 
	                  // B. 일감 추가 검색창 잠금
	                  if (issueSearchInput) {
	                      issueSearchInput.disabled = true;
	                      issueSearchInput.style.backgroundColor = '#e9ecef';
	                      issueSearchInput.placeholder = '완료된 마일스톤에는 일감을 추가할 수 없습니다.';
	                  }
 
	                  // C. 기존 연결된 일감 체크박스 해제 잠금 (연동 해제 방지)
	                  allCheckboxes.forEach(cb => {
	                      cb.disabled = true;
	                  });
 
	              } else if (status === 'L002') { // [진행 중 상태일 때]
	                  // 시작일만 락, 목표일은 변경 허용
	                  startDateInput.readOnly = true;
	                  startDateInput.style.backgroundColor = '#e9ecef';
	                  startDateInput.style.pointerEvents = 'none';
	                  
	                  endDateInput.readOnly = false;
	                  endDateInput.style.backgroundColor = '';
	                  endDateInput.style.pointerEvents = 'auto';
 
	                  if (issueSearchInput) {
	                      issueSearchInput.disabled = false;
	                      issueSearchInput.style.backgroundColor = '';
	                      issueSearchInput.placeholder = '일감 제목 또는 버전 입력...';
	                  }
	                  updateCheckboxUI(); // 일반적인 버전 체크박스 UI 잠금 상태로 갱신
 
	              } else { // [진행 예정(L001) 등 대기 상태일 때]
	                  // 모든 락 해제
	                  startDateInput.readOnly = false;
	                  startDateInput.style.backgroundColor = '';
	                  startDateInput.style.pointerEvents = 'auto';
	                  
	                  endDateInput.readOnly = false;
	                  endDateInput.style.backgroundColor = '';
	                  endDateInput.style.pointerEvents = 'auto';
 
	                  if (issueSearchInput) {
	                      issueSearchInput.disabled = false;
	                      issueSearchInput.style.backgroundColor = '';
	                      issueSearchInput.placeholder = '일감 제목 또는 버전 입력...';
	                  }
	                  updateCheckboxUI(); // 일반적인 버전 체크박스 UI 잠금 상태로 갱신
	              }
	          }
	      }
	      
	      // [신규 추가]: 현재 체크박스 선택된 일감들의 평균 진행률 계산
	      function getCheckedIssuesProgressAverage() {
	          const checked = document.querySelectorAll(".issue-checkbox:checked");
	          if (checked.length === 0) return 0;
	          
	          let sum = 0;
	          checked.forEach(cb => {
	              const rate = parseFloat(cb.getAttribute("data-progress-rate")) || 0;
	              sum += rate;
	          });
	          return Math.round(sum / checked.length);
	      }
 
	      // 1. 페이지가 처음 열렸을 때 기존 상태를 읽어서 즉시 적용
	      toggleStartDateLock();
 
	      // 2. 사용자가 드롭다운에서 상태를 변경할 때마다 실시간 적용
	      if (statusSelect) {
	          statusSelect.addEventListener('change', function() {
	              if (this.value === 'L003') {
	                  const avgProgress = getCheckedIssuesProgressAverage();
	                  const checkedCount = document.querySelectorAll(".issue-checkbox:checked").length;
	                  if (checkedCount === 0 || avgProgress < 100) {
	                      window.PFDialog.alert(
	                          '연결된 일감이 없거나, 모든 일감의 평균 진행률이 100%가 아닌 마일스톤은 완료할 수 없습니다. (현재 평균 진행률: ' + avgProgress + '%)',
	                          {
	                              title: '상태 변경 불가',
	                              icon: 'warning',
	                              confirmText: '확인'
	                          }
	                      );
	                      // 완료 불가시 진행 중(L002)으로 임시 복원
	                      this.value = 'L002';
	                  }
	              }
	              toggleStartDateLock();
	          });
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
        
        const currentVersion = versionIdInput ? versionIdInput.value : "";
        if (currentVersion !== null && currentVersion !== '') {
            apiUrl += `&versionId=${currentVersion}`;
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

        // 📝 [추가]: 버전이 선택되지 않은 상태에서 일감을 추가하면, 해당 일감의 버전으로 자동 연동 및 고정
        if (versionIdInput && (versionIdInput.value === "" || versionIdInput.value === null)) {
            versionIdInput.value = issue.versionId;
            lockedVersionId = issue.versionId;
        }

        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td style="text-align: center;">
                <input type="checkbox" class="issue-checkbox" 
                       value="${issue.issueId}" 
                       data-version-id="${issue.versionId}" 
                       data-progress-rate="${issue.progressRate || 0}" checked>
            </td>
            <td>${issue.title} (${issue.versionName})</td>
            <td style="text-align: center;"><span class="status-badge">${issue.issueStatusName || '미정'}</span></td>
        `;
        
        issueListBody.prepend(tr);

        const newCheckbox = tr.querySelector(".issue-checkbox");
        
        selectedIssueIds.add(issue.issueId);
        addedHistoryIds.add(issue.issueId);
        
        newCheckbox.addEventListener('change', handleCheckboxChange);
        updateHiddenInput(); 
        updateCheckboxUI();
    }

    // [체크박스 상태 변경 로직]
    function handleCheckboxChange() {
        const clickedIssueId = parseInt(this.value) || this.value; 

        if (this.checked) {
            selectedIssueIds.add(clickedIssueId);
        } else {
            selectedIssueIds.delete(clickedIssueId);
        }
        
        updateHiddenInput();
        updateCheckboxUI(); 
    }

    // 표에 있는 모든 체크박스의 활성/비활성 상태 갱신 함수
    function updateCheckboxUI() {
        const allCheckboxes = document.querySelectorAll(".issue-checkbox");
        const currentVersion = versionIdInput ? versionIdInput.value : "";
        
        allCheckboxes.forEach(cb => {
            const cbVersionId = cb.getAttribute("data-version-id");
            const tr = cb.closest("tr"); 
            
            if (currentVersion !== "" && String(cbVersionId) !== String(currentVersion)) {
                cb.disabled = true;                
                tr.style.color = "#ccc";           
                tr.style.backgroundColor = "#f9f9f9"; 
                tr.title = "현재 마일스톤에 선택된 버전과 일치하지 않는 일감입니다."; 
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

        // 2. 날짜 논리 검증 (시작일이 목표일보다 늦을 수 없음)
        if (startDateInput.value && endDateInput.value) {
            if (startDateInput.value > endDateInput.value) { 
                startDateInput.classList.add('error-border');
                endDateInput.classList.add('error-border');
                dateErrorDiv.style.display = "block"; 
                isValid = false; 
            } else {
                startDateInput.classList.remove('error-border');
                endDateInput.classList.remove('error-border');
                dateErrorDiv.style.display = "none"; 
            }
        }

        // 3. 상태가 '완료(L003)'인데 진척도가 100%가 아니거나 일감이 없는 경우 전송 방지
        if (statusSelect && statusSelect.value === 'L003') {
            const avgProgress = getCheckedIssuesProgressAverage();
            const checkedCount = document.querySelectorAll(".issue-checkbox:checked").length;
            if (checkedCount === 0 || avgProgress < 100) {
                window.PFDialog.alert(
                    '연결된 일감이 없거나, 모든 일감의 평균 진행률이 100%가 아닌 마일스톤은 완료할 수 없습니다. (현재 평균 진행률: ' + avgProgress + '%)',
                    {
                        title: '제출 불가능',
                        icon: 'warning',
                        confirmText: '확인',
						iconColor: '#fc0303'
                    }
                );
                isValid = false;
            }
        }

        if (!isValid) { 
            event.preventDefault(); 
            return;
        }

        // 📝 SweetAlert 수정 컨펌 연동 (제출 일시 정지 후 컨펌 승인 시 실제 submit)
        if (form.dataset.pfUpdateApproved !== 'true') {
            event.preventDefault(); // 일단 전송 중단

            window.PFDialog.confirm({
                title: '수정 확인',
                message: '수정된 내용을 저장하시겠습니까?',
                confirmText: '저장',
                icon: 'question'
            }).then(function(confirmed) {
                if (confirmed) {
                    form.dataset.pfUpdateApproved = 'true';
                    const btn = document.getElementById('btnUpdate');
                    if (btn) btn.disabled = true;
                    form.submit();
                }
            });
        }
    });

    document.querySelectorAll('.required-input').forEach(input => {
        input.addEventListener('change', function() {
            this.classList.remove('error-border');
        });
    });

    // 수정 화면 전용: 열리자마자 기존 세팅 호출
    initExistingIssues();
});