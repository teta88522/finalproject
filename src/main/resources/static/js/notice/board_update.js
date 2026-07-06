// board-update.js
document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("boardUpdateForm");
    const btnSubmit = document.getElementById("btnSubmit");
  
    if (!form || !btnSubmit) return;
  
    const boardNameInput = document.getElementById("boardName");
    const boardNameErrorDiv = document.getElementById("boardNameError");
    
    const descInput = document.getElementById("description");
    const descErrorDiv = document.getElementById("descError");

    // 1. 에러 메시지 처리
    const errorMessage = form.getAttribute("data-error-message");
    if (errorMessage && errorMessage !== 'null' && errorMessage.trim() !== '') {
        window.PFDialog.alert(errorMessage, {
            title: '에러',
            icon: 'error'
        });
    }
  
    // 2. 수정 제출 버튼 처리
    btnSubmit.addEventListener("click", function () {
        let isValid = true;
  
        // 게시판 이름 검증
        if (boardNameInput.value.trim() === "") {
            boardNameInput.classList.add("border-red");
            if (boardNameErrorDiv) boardNameErrorDiv.style.display = "block";
            isValid = false;
        } else {
            boardNameInput.classList.remove("border-red");
            if (boardNameErrorDiv) boardNameErrorDiv.style.display = "none";
        }
  
        // 설명 검증
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
  
        // 성공 시 제출 (SweetAlert 수정 컨펌 적용)
        window.PFDialog.confirm({
            title: '수정 확인',
            message: '게시판 수정 내용을 저장하시겠습니까?',
            confirmText: '저장',
            icon: 'question'
        }).then(function(confirmed) {
            if (confirmed) {
                btnSubmit.disabled = true;
                form.submit();
            }
        });
    });
  
    // 3. 게시판 삭제 전 글 개수 안전 차단기 및 SweetAlert 컨펌
    const btnDeleteBoard = document.getElementById('btnDeleteBoard');
    if (btnDeleteBoard) {
        btnDeleteBoard.addEventListener('click', function(e) {
            e.preventDefault();
            const postCount = parseInt(this.getAttribute('data-post-count') || '0', 10);
            const deleteUrl = this.getAttribute('href');
            
            if (postCount > 0) {
                window.PFDialog.alert("게시글이 존재하는 게시판은 삭제할 수 없습니다. (현재 등록된 글: " + postCount + "개)", {
                    title: '삭제 불가',
                    icon: 'warning'
                });
            } else {
                window.PFDialog.confirm({
                    title: '삭제 확인',
                    message: '정말로 이 게시판을 삭제하시겠습니까?\n삭제 시 복구할 수 없습니다.',
                    confirmText: '삭제',
                    icon: 'warning'
                }).then(function(confirmed) {
                    if (confirmed) {
                        location.href = deleteUrl;
                    }
                });
            }
        });
    }
  
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
