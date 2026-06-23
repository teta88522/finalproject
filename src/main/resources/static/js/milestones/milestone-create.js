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

  // 장바구니와 버전 관리 (생성은 무조건 비어있는 상태로 시작)
  let selectedIssueIds = new Set();
  let lockedVersionId = null;
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

  // 검색창 타이핑 시
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

  // 드롭다운 렌더링
  function renderDropdown(data) {
    searchDropdown.innerHTML = "";

    if (data.length === 0) {
      searchDropdown.innerHTML = `<li class="search-dropdown-item" style="color:#999; cursor:default;">조회된 일감이 없습니다.</li>`;
      searchDropdown.style.display = "block";
      return;
    }

    data.forEach((issue) => {
      // 이미 장바구니에 있으면 드롭다운에서 안 보이게
      if (selectedIssueIds.has(issue.issueId)) return;

      const li = document.createElement("li");
      li.className = "search-dropdown-item";
      li.innerHTML = `
                <strong>${issue.issueId}</strong> - ${issue.title}
                <span class="badge badge-right" style="color: #0056b3;">${issue.statusName || "상태"}</span>
            `;

      li.addEventListener("click", function () {
        addIssueToTable(issue);
        searchDropdown.style.display = "none";
        issueSearchInput.value = "";
      });

      searchDropdown.appendChild(li);
    });

    searchDropdown.style.display =
      searchDropdown.children.length > 0 ? "block" : "none";
  }

  // 표에 일감 추가 및 버전 고정
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
            <td style="text-align: center;"><span class="status-badge">${issue.statusName || "상태"}</span></td>
        `;

    issueListBody.prepend(tr);

    const newCheckbox = tr.querySelector(".issue-checkbox");
    selectedIssueIds.add(issue.issueId);

    if (!lockedVersionId) {
      lockedVersionId = issue.versionId;
      versionIdInput.value = lockedVersionId;
    }

    newCheckbox.addEventListener("change", handleCheckboxChange);
    updateHiddenInput();
  }

  // 체크박스 해제 시 로직
  function handleCheckboxChange() {
    const clickedIssueId = this.value;
    const clickedVersionId = this.getAttribute("data-version-id");

    if (this.checked) {
      selectedIssueIds.add(clickedIssueId);
      if (!lockedVersionId) {
        lockedVersionId = clickedVersionId;
        versionIdInput.value = lockedVersionId;
      }
    } else {
      selectedIssueIds.delete(clickedIssueId);
      if (selectedIssueIds.size === 0) {
        lockedVersionId = null;
        versionIdInput.value = "";
      }
    }
    updateHiddenInput();
  }

  function updateHiddenInput() {
    selectedIssueIdsInput.value = Array.from(selectedIssueIds).join(",");
  }

  // 드롭다운 바깥 클릭 시 닫기
  document.addEventListener("click", function (event) {
    if (
      !issueSearchInput.contains(event.target) &&
      searchDropdown &&
      !searchDropdown.contains(event.target)
    ) {
      searchDropdown.style.display = "none";
    }
  });

  // 폼 검증
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
