document.addEventListener("DOMContentLoaded", function () {
  const btnToggleCompleted = document.getElementById("btnToggleCompleted");
  const statusFilter = document.getElementById("statusFilter");
  const versionSearch = document.getElementById("versionSearch");
  const tableRows = document.querySelectorAll(".roadmap-table tbody tr");

  // 1. 완료된 버전 닫기/보기 토글 기능
  let isCompletedHidden = false;

  if (btnToggleCompleted) {
    btnToggleCompleted.addEventListener("click", function () {
      isCompletedHidden = !isCompletedHidden; // 상태 뒤집기

      // 버튼 텍스트 변경
      this.innerHTML = isCompletedHidden
        ? "🔓 완료된 버전 보기"
        : "🔒 완료된 버전 닫기";

      applyFilters(); // 필터 재적용
    });
  }

  // 2. 상태 선택(Select) 및 검색어(Input) 입력 시 즉시 필터링
  if (statusFilter) {
    statusFilter.addEventListener("change", applyFilters);
  }
  if (versionSearch) {
    // 타이핑할 때마다 즉시 검색되도록 input 이벤트 사용
    versionSearch.addEventListener("input", applyFilters);
  }

  // 3. 필터링 핵심 로직
  function applyFilters() {
    // 데이터가 없다는 빈 행(empty-state)이 있으면 로직 실행 안 함
    if (tableRows.length === 1 && tableRows[0].querySelector(".empty-state"))
      return;

    const selectedStatusValue = statusFilter.value; // "k001", "k002", "k003"
    const searchKeyword = versionSearch.value.trim().toLowerCase();

    tableRows.forEach((row) => {
      // 각 행(Row)의 데이터 가져오기
      const versionName = row
        .querySelector(".version-link")
        .innerText.toLowerCase();

      // 상태 뱃지 확인 (th:if 로직에 의해 생성된 뱃지 텍스트로 판단)
      const statusBadge = row.querySelector(".status-badge");
      const statusText = statusBadge ? statusBadge.innerText : "";

      let rowStatusCode = "";
      if (statusText === "진행 예정") rowStatusCode = "a001";
      else if (statusText === "진행 중") rowStatusCode = "a002";
      else if (statusText === "완료") rowStatusCode = "a003";

      // 조건 검사
      // 1) 완료된 버전 닫기 버튼이 켜져 있는데, 이 행이 완료(a003) 상태인가?
      const isHiddenByToggle = isCompletedHidden && rowStatusCode === "a003";

      // 2) 선택한 상태 필터와 일치하는가? (전체면 통과)
      const isStatusMatch =
        selectedStatusValue === "" || selectedStatusValue === rowStatusCode;

      // 3) 검색어가 버전에 포함되어 있는가? (빈칸이면 통과)
      const isSearchMatch =
        searchKeyword === "" || versionName.includes(searchKeyword);

      // 3가지 조건 중 하나라도 숨겨야 할 조건이면 숨김, 아니면 보여줌
      if (isHiddenByToggle || !isStatusMatch || !isSearchMatch) {
        row.style.display = "none";
      } else {
        row.style.display = "";
      }
    });
  }
});
