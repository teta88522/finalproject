document.addEventListener("DOMContentLoaded", function () {
  const issueSearchInput = document.getElementById("issueSearchInput");
  const searchDropdown = document.getElementById("searchDropdown");
  const issueListBody = document.getElementById("issueListBody");
  const versionIdInput = document.getElementById("versionId");
  const selectedIssueIdsInput = document.getElementById("selectedIssueIds");
  const form = document.getElementById("milestoneForm");
  const startDateInput = document.getElementById("startDate");
  const endDateInput = document.getElementById("endDate");
  const dateErrorDiv = document.getElementById("dateError");
  const startDatePastError = document.getElementById("startDatePastError"); // [신규] 에러 메시지 변수 추가
  const projectId = document.getElementById('projectId').value;

  // [상태 관리 변수]
  let selectedIssueIds = new Set();
  let addedHistoryIds = new Set();  
  let lockedVersionId = null;       
  let searchTimeout = null;

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
  // 일감 검색 API 호출 함수
  function fetchAndShowIssues(keyword) {
    let apiUrl = `/project/${projectId}/milestones/api/issues/search?keyword=${keyword}`;

    if (lockedVersionId !== null && lockedVersionId !== "") {
      apiUrl += `&versionId=${lockedVersionId}`;
    }

    fetch(apiUrl)
      .then((response) => response.json())
      .then((data) => renderDropdown(data))
      .catch((error) => console.error("일감 조회 에러 :", error));
  }

  // 검색창 타이핑 시 (Debounce 적용)
  issueSearchInput.addEventListener("input", function () {
    clearTimeout(searchTimeout);
    const keyword = this.value.trim();
    searchTimeout = setTimeout(() => {
      fetchAndShowIssues(keyword);
    }, 300);
  });

  // 검색창 클릭(포커스) 시 빈 문자열로 전체 검색
  issueSearchInput.addEventListener("focus", function () {
    const keyword = this.value.trim();
    fetchAndShowIssues(keyword);
  });

  // [드롭다운 렌더링 함수]
  function renderDropdown(data) {
    searchDropdown.innerHTML = "";

    if (data.length === 0) {
      searchDropdown.innerHTML = `<li class="search-dropdown-item" style="color:#999; cursor:default;">조회된 일감이 없습니다.</li>`;
      searchDropdown.style.display = "block";
      return;
    }

    data.forEach((issue) => {
      const li = document.createElement("li");
      li.className = "search-dropdown-item";

      const isUsed = addedHistoryIds.has(issue.issueId);
      const isDifferentVersion = (lockedVersionId !== null && lockedVersionId !== "" && issue.versionId !== lockedVersionId);

      if (isUsed || isDifferentVersion) {
        li.style.color = "#ccc";
        li.style.cursor = "not-allowed";
        
        if (isUsed) {
          li.innerHTML = `<strong>${issue.versionName}</strong> - ${issue.title} <span style="font-size:0.85em; color:#e74c3c; margin-left:5px;">(이미 추가됨)</span>`;
        } else {
          li.innerHTML = `<strong>${issue.versionName}</strong> - ${issue.title} <span style="font-size:0.85em; color:#e74c3c; margin-left:5px;">(버전 불일치)</span>`;
        }
      } else {
        li.innerHTML = `
            <strong>${issue.versionName || '버전 없음'}</strong> - ${issue.title}
            <span class="badge badge-right" style="color: #0056b3;">${issue.issueStatusName || "미정"}</span>
        `;

        li.addEventListener("click", function () {
          addIssueToTable(issue);
          searchDropdown.style.display = "none";
          issueSearchInput.value = "";
        });
      }

      searchDropdown.appendChild(li);
    });

    searchDropdown.style.display = searchDropdown.children.length > 0 ? "block" : "none";
  }

  // [표에 일감 추가 및 버전 고정 함수]
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
        <td style="text-align: center;"><span class="status-badge">${issue.issueStatusName || "상태"}</span></td>
    `;

    issueListBody.prepend(tr);

    const newCheckbox = tr.querySelector(".issue-checkbox");
    
    selectedIssueIds.add(issue.issueId);
    addedHistoryIds.add(issue.issueId);

    if (!lockedVersionId) {
      lockedVersionId = issue.versionId;
      if (versionIdInput) versionIdInput.value = lockedVersionId;
    }

    newCheckbox.addEventListener("change", handleCheckboxChange);
    updateHiddenInput();
	updateCheckboxUI();
  }

  // [체크박스 해제 및 선택 시 로직 함수]
  function handleCheckboxChange() {
    const clickedIssueId = this.value;
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
        if (versionIdInput) versionIdInput.value = "";
      }
    }
    
    updateHiddenInput();
    updateCheckboxUI(); 
  }

  // 표에 있는 모든 체크박스의 활성/비활성 상태를 갱신하는 함수
  function updateCheckboxUI() {
    const allCheckboxes = document.querySelectorAll(".issue-checkbox");
    
    allCheckboxes.forEach(cb => {
      const cbVersionId = cb.getAttribute("data-version-id");
      const tr = cb.closest("tr"); 
      
      if (lockedVersionId !== null && cbVersionId !== lockedVersionId) {
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

  // 드롭다운 바깥 영역 클릭 시 닫기
  document.addEventListener("click", function (event) {
    if (
      !issueSearchInput.contains(event.target) &&
      searchDropdown &&
      !searchDropdown.contains(event.target)
    ) {
      searchDropdown.style.display = "none";
    }
  });

  // --- [폼 서브밋 시 최종 날짜 검증 로직 추가] ---
  form.addEventListener("submit", function (event) {
    let isValid = true;

    // 1. 필수 입력값 체크
    const requiredInputs = document.querySelectorAll(".required-input");
    requiredInputs.forEach((input) => {
      if (input.value.trim() === "") {
        input.classList.add("error-border");
        isValid = false;
      } else {
        input.classList.remove("error-border");
      }
    });

    // 2. 날짜 논리 검증 (시작일이 목표일보다 늦을 수 없음)
    if (startDateInput.value && endDateInput.value) {
        if (startDateInput.value > endDateInput.value) {
            startDateInput.classList.add("error-border");
            endDateInput.classList.add("error-border");
            dateErrorDiv.style.display = "block";
            isValid = false;
        } else {
            endDateInput.classList.remove("error-border");
            dateErrorDiv.style.display = "none";
        }
    }

    if (!isValid) {
      event.preventDefault();
    }
  });

  document.querySelectorAll(".required-input").forEach((input) => {
    input.addEventListener("change", function () {
      this.classList.remove("error-border");
    });
  });
});