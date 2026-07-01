// board-update.js
document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("boardUpdateForm");
    const btnSubmit = document.getElementById("btnSubmit");
  
    if (!form || !btnSubmit) return;
  
    const boardNameInput = document.getElementById("boardName");
    const boardNameErrorDiv = document.getElementById("boardNameError");
    
    const descInput = document.getElementById("description");
    const descErrorDiv = document.getElementById("descError");
  
    btnSubmit.addEventListener("click", function () {
        let isValid = true;
  
        // 1. 게시판 이름 검증
        if (boardNameInput.value.trim() === "") {
            boardNameInput.classList.add("border-red");
            if (boardNameErrorDiv) boardNameErrorDiv.style.display = "block";
            isValid = false;
        } else {
            boardNameInput.classList.remove("border-red");
            if (boardNameErrorDiv) boardNameErrorDiv.style.display = "none";
        }
  
        // 2. 설명 검증
        if (descInput.value.trim() === "") {
            descInput.classList.add("border-red");
            if (descErrorDiv) descErrorDiv.style.display = "block";
            isValid = false;
        } else {
            descInput.classList.remove("border-red");
            if (descErrorDiv) descErrorDiv.style.display = "none";
        }
  
        // 검증 실패 시 포커스 이동
        if (!isValid) {
            if (boardNameInput.value.trim() === "") {
                boardNameInput.focus();
            } else {
                descInput.focus();
            }
            return;
        }
  
        // 성공 시 제출
        form.submit();
    });
  
    // 실시간 에러 테두리 지우기
    boardNameInput.addEventListener("input", function () {
        this.classList.remove("border-red");
        if (boardNameErrorDiv) boardNameErrorDiv.style.display = "none";
    });
  
    descInput.addEventListener("input", function () {
        this.classList.remove("border-red");
        if (descErrorDiv) descErrorDiv.style.display = "none";
    });
});
