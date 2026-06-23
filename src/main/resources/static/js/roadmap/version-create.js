// version-create.js

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('versionCreateForm');
    const checkbox = document.getElementById('defaultYn');
    const hiddenCheckbox = document.getElementById('defaultYnHidden');

    form.addEventListener('submit', function(e) {
        // 1. 필수 값 검증 (버전명)
        const versionName = document.getElementById('versionName').value.trim();
        
        if (!versionName) {
            e.preventDefault(); // 폼 전송 막기
            alert('버전명을 입력해주세요.');
            document.getElementById('versionName').focus();
            return;
        }

        // 2. 체크박스 처리 로직
        // HTML form은 체크박스가 해제되어 있으면 아예 파라미터를 안 보냅니다.
        // 체크되어 있으면 hidden 태그를 비활성화하여 'Y'만 넘어가게 하고,
        // 체크 해제되어 있으면 hidden 태그가 작동하여 'N'이 넘어가게 합니다.
        if (checkbox.checked) {
            hiddenCheckbox.disabled = true; 
        } else {
            hiddenCheckbox.disabled = false;
        }

        // 통과 시 자연스럽게 submit 됩니다.
    });
});