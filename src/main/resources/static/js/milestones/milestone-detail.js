document.addEventListener("DOMContentLoaded", function () {
  // 1. 이벤트 리스너가 'e'를 인자로 받을 수 있도록 함수 구문을 확인하세요.
  document.addEventListener("click", function (e) {
    // 2. e.target이 존재하는지 먼저 확인
    if (!e.target) return;

    // 목록 버튼
    if (e.target.id === "btnList") {
      const url = e.target.getAttribute("data-url");
      if (url) location.href = url;
    }

    // 수정 버튼
    if (e.target.id === "btnEdit") {
      const milestoneId = e.target.getAttribute("data-id");
      const baseUrl = e.target.getAttribute("data-url");
      if (baseUrl) location.href = `${baseUrl}?id=${milestoneId}`;
    }
  });

  // 📝 [최종 삭제 승인 시] 버튼을 비활성화하여 중복 클릭 방지
  const deleteForm = document.querySelector(".js-pf-confirm-submit");
  if (deleteForm) {
    deleteForm.addEventListener("pf:confirmed-submit", function () {
      const btn = deleteForm.querySelector('button[type="submit"]');
      if (btn) {
        btn.disabled = true;
      }
    });
  }
});
