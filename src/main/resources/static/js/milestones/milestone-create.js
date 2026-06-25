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

  // [상태 관리 변수]
  let selectedIssueIds = new Set(); // 현재 체크되어 서버로 전송될 일감 ID 목록
  let addedHistoryIds = new Set();  // 💡 한 번이라도 추가된 적 있는 일감 ID 목록 (체크 해제되어도 유지되어 중복 방지)
  let lockedVersionId = null;       // 처음 추가된 일감에 의해 고정되는 버전 ID
  let searchTimeout = null;

  // 일감 검색 API 호출 함수
  function fetchAndShowIssues(keyword) {
    let apiUrl = `/milestones/api/issues/search?keyword=${keyword}`;

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

      // 💡 조건 1: 한 번이라도 장바구니에 담긴 적이 있는가? (체크를 풀었어도 true)
      const isUsed = addedHistoryIds.has(issue.issueId);
      
      // 💡 조건 2: 잠긴 버전이 있고, 현재 검색된 일감의 버전이 그와 다른가?
      const isDifferentVersion = (lockedVersionId !== null && lockedVersionId !== "" && issue.versionId !== lockedVersionId);

      if (isUsed || isDifferentVersion) {
        // [선택 불가 상태 스타일 스타일 및 문구 처리]
        li.style.color = "#ccc";
        li.style.cursor = "not-allowed";
        
        if (isUsed) {
          li.innerHTML = `<strong>${issue.versionName}</strong> - ${issue.title} <span style="font-size:0.85em; color:#e74c3c; margin-left:5px;">(이미 추가됨)</span>`;
        } else {
          li.innerHTML = `<strong>${issue.versionName}</strong> - ${issue.title} <span style="font-size:0.85em; color:#e74c3c; margin-left:5px;">(버전 불일치)</span>`;
        }
      } else {
        // [선택 가능 상태]
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
    
    // 현재 선택 목록과 중복 방지 히스토리 목록에 각각 추가
    selectedIssueIds.add(issue.issueId);
    addedHistoryIds.add(issue.issueId);

    // 첫 번째 일감 등록 시 버전 필드 잠금 설정
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
        
        // 체크가 모두 풀리면 버전 잠금 해제
        if (selectedIssueIds.size === 0) {
          lockedVersionId = null;
          if (versionIdInput) versionIdInput.value = "";
        }
      }
      
      updateHiddenInput();
      updateCheckboxUI(); // 💡 상태가 변할 때마다 전체 체크박스 UI 갱신
    }

    // 💡 [신규 추가] 표에 있는 모든 체크박스의 활성/비활성 상태를 갱신하는 함수
    function updateCheckboxUI() {
      const allCheckboxes = document.querySelectorAll(".issue-checkbox");
      
      allCheckboxes.forEach(cb => {
        const cbVersionId = cb.getAttribute("data-version-id");
        const tr = cb.closest("tr"); // 체크박스가 포함된 행(행 전체 색상 변경용)
        
        // 잠긴 버전이 존재하고, 현재 체크박스의 버전이 그 잠긴 버전과 다를 때
        if (lockedVersionId !== null && cbVersionId !== lockedVersionId) {
          cb.disabled = true;                // 체크박스 클릭 차단
          tr.style.color = "#ccc";           // 글씨를 회색으로 변경
          tr.style.backgroundColor = "#f9f9f9"; // 배경을 살짝 어둡게 (선택 사항)
          tr.title = "현재 선택된 일감들과 버전이 달라 추가할 수 없습니다."; // 마우스 호버 시 툴팁
        } else {
          // 조건에 맞거나 아무것도 잠기지 않았을 때는 원상복구
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

  // 폼 서브밋 검증
  form.addEventListener("submit", function (event) {
    let isValid = true;

    const requiredInputs = document.querySelectorAll(".required-input");
    requiredInputs.forEach((input) => {
      if (input.value.trim() === "") {
        input.classList.add("error-border");
        isValid = false;
      } else {
        input.classList.remove("error-border");
      }
    });

    if (startDateInput.value && endDateInput.value) {
      const startDate = new Date(startDateInput.value);
      const endDate = new Date(endDateInput.value);

      if (startDate > endDate) {
        startDateInput.classList.add("error-border");
        endDateInput.classList.add("error-border");
        dateErrorDiv.style.display = "block";
        isValid = false;
      } else {
        startDateInput.classList.remove("error-border");
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
