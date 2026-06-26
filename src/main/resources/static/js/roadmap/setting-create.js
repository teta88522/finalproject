document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("versionCreateForm");
  const btnSubmit = document.getElementById("btnSubmit");

  // 이 페이지에 폼과 제출 버튼이 없으면 코드를 멈춤
  if (!form || !btnSubmit) return;

  const checkbox = document.getElementById("defaultYn");
  const versionNameInput = document.getElementById("versionName");
  const nameErrorDiv = document.getElementById("nameError");
  const descriptionInput = document.getElementById("description");
  const descriptionErrorDiv = document.getElementById("descError");

  // "버튼 클릭" 이벤트 감지
  btnSubmit.addEventListener("click", function () {
    let isValid = true;

    // 1. 검증: 버전명
    if (versionNameInput.value.trim() === "") {
      versionNameInput.classList.add("border-red");
      if (nameErrorDiv) nameErrorDiv.style.display = "block";
      isValid = false;
    } else {
      versionNameInput.classList.remove("border-red");
      if (nameErrorDiv) nameErrorDiv.style.display = "none";
    }

    // 2. 검증 : 설명
    if (descriptionInput.value.trim() === "") {
      descriptionInput.classList.add("border-red");
      if (descriptionErrorDiv) descriptionErrorDiv.style.display = "block";
      isValid = false;
    } else {
      descriptionInput.classList.remove("border-red");
      if (descriptionErrorDiv) descriptionErrorDiv.style.display = "none";
    }

    // 2-1. 검증 실패 시: 빈 칸으로 커서 이동하고 전송 안 함!
    if (!isValid) {
      // 💡 수정됨: 만약 이름이 비었으면 이름으로, 이름은 있는데 설명이 비었으면 설명으로 포커스 이동
      if (versionNameInput.value.trim() === "") {
        versionNameInput.focus();
      } else {
        descriptionInput.focus();
      }
      return;
    }

    // 3. 검증 통과 시: 체크박스 데이터 정리 (N / Y)
    if (!checkbox.checked) {
      let hiddenInput = form.querySelector(
        'input[name="defaultYn"][type="hidden"]',
      );
      if (!hiddenInput) {
        hiddenInput = document.createElement("input");
        hiddenInput.type = "hidden";
        hiddenInput.name = "defaultYn";
        hiddenInput.value = "N";
        form.appendChild(hiddenInput);
      }
    } else {
      const hiddenInput = form.querySelector(
        'input[name="defaultYn"][type="hidden"]',
      );
      if (hiddenInput) {
        hiddenInput.remove();
      }
    }

    // 4. 모든 것이 완벽할 때 자바스크립트가 직접 전송!
    form.submit();
  });

  // 5. 사용자가 글자를 지우고 새로 쓰기 시작하면 빨간 줄 바로 없애주기
  versionNameInput.addEventListener("input", function () {
    this.classList.remove("border-red");
    if (nameErrorDiv) nameErrorDiv.style.display = "none";
  });

  // 💡 수정됨: 설명창도 입력 시 실시간으로 빨간 줄 지워주기 추가
  descriptionInput.addEventListener("input", function () {
    this.classList.remove("border-red");
    if (descriptionErrorDiv) descriptionErrorDiv.style.display = "none";
  });
});
