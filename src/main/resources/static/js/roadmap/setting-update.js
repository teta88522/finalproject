document.addEventListener('DOMContentLoaded', function() {
    const updateForm = document.getElementById('versionUpdateForm');
    const deleteForm = document.getElementById('versionDeleteForm'); // 삭제 폼
    const btnSubmit = document.getElementById('btnSubmit');
    const btnDelete = document.getElementById('btnDelete'); // 삭제 버튼
    
    // 이 페이지에 폼과 버튼이 없으면 코드를 멈춤
    if (!updateForm || !btnSubmit) return;

    const checkbox = document.getElementById("defaultYn");
    const versionNameInput = document.getElementById('versionName');
    const nameErrorDiv = document.getElementById('nameError');
    const descriptionInput = document.getElementById("description");
    const descriptionErrorDiv = document.getElementById("descError");

    // 1. [수정완료] 버튼 클릭 시 검증 및 제출
    btnSubmit.addEventListener('click', function() {
        let isValid = true;

        // 1-1. 검증: 버전명
        if (versionNameInput.value.trim() === "") {
            versionNameInput.classList.add("border-red");
            if (nameErrorDiv) nameErrorDiv.style.display = "block";
            isValid = false;
        } else {
            versionNameInput.classList.remove("border-red");
            if (nameErrorDiv) nameErrorDiv.style.display = "none";
        }

        // 1-2. 검증: 설명
        if (descriptionInput.value.trim() === "") {
            descriptionInput.classList.add("border-red");
            if (descriptionErrorDiv) descriptionErrorDiv.style.display = "block";
            isValid = false;
        } else {
            descriptionInput.classList.remove("border-red");
            if (descriptionErrorDiv) descriptionErrorDiv.style.display = "none";
        }

        // 1-3. 검증 실패 시: 빈 칸으로 커서 이동하고 전송 중단
        if (!isValid) {
            if (versionNameInput.value.trim() === "") {
                versionNameInput.focus();
            } else {
                descriptionInput.focus();
            }
            return;
        }

        // 1-4. 검증 통과 시: 체크박스 데이터 정리 (N / Y)
        if (checkbox) {
            if (!checkbox.checked) {
                let hiddenInput = updateForm.querySelector('input[name="defaultYn"][type="hidden"]');
                if (!hiddenInput) {
                    hiddenInput = document.createElement("input");
                    hiddenInput.type = "hidden";
                    hiddenInput.name = "defaultYn";
                    hiddenInput.value = "N";
                    updateForm.appendChild(hiddenInput);
                }
            } else {
                const hiddenInput = updateForm.querySelector('input[name="defaultYn"][type="hidden"]');
                if (hiddenInput) {
                    hiddenInput.remove();
                }
            }
        }

        // 1-5. 최종 확인 후 폼 전송
        if(confirm('수정된 내용을 저장하시겠습니까?')) {
            updateForm.submit();
        }
    });

    // 2. [삭제] 버튼 클릭 시 2단계 사전 예방 차단기 구동 및 폼 전송
    if(btnDelete) {
        btnDelete.addEventListener('click', function() {
            const issueCount = parseInt(btnDelete.getAttribute('data-issue-count') || '0', 10);
            const milestoneCount = parseInt(btnDelete.getAttribute('data-milestone-count') || '0', 10);
            const defaultYn = btnDelete.getAttribute('data-default-yn') || 'N';

            // 🛡️ 1차 차단: 기본(Default) 버전 삭제 차단
            if (defaultYn === 'Y') {
                alert("기본(Default) 버전으로 설정된 로드맵은 삭제할 수 없습니다.\n삭제하시려면 다른 로드맵을 기본 버전으로 먼저 변경해 주세요.");
                return;
            }

            // 🛡️ 2차 차단: 하위 항목(일감 또는 마일스톤) 잔존 시 삭제 차단
            if (issueCount > 0 || milestoneCount > 0) {
                alert("하위 항목(일감 또는 마일스톤)이 존재하는 로드맵은 삭제할 수 없습니다.\n(현재 연결된 일감: " + issueCount + "개, 마일스톤: " + milestoneCount + "개)");
                return;
            }

            // 안전 검사 모두 통과 시에만 최종 확인 후 삭제 진행
            if(confirm('정말 이 로드맵을 삭제하시겠습니까?')) {
                deleteForm.submit();
            }
        });
    }

    // 3. 입력창 수정 시 에러 메시지 실시간 숨김
    versionNameInput.addEventListener('input', function() {
        this.classList.remove('border-red');
        if (nameErrorDiv) nameErrorDiv.style.display = 'none';
    });

    descriptionInput.addEventListener("input", function () {
        this.classList.remove("border-red");
        if (descriptionErrorDiv) descriptionErrorDiv.style.display = "none";
    });
});