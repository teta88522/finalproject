document.addEventListener('DOMContentLoaded', function() {
    
    const btnList = document.getElementById('btnList');
    const btnEdit = document.getElementById('btnEdit');
    const btnDelete = document.getElementById('btnDelete');
    
    // 1. '목록으로' 버튼
    if (btnList) {
        btnList.addEventListener('click', function() {
            location.href = '/milestones/list'; 
        });
    }

    // 2. '수정' 버튼
    if (btnEdit) {
        btnEdit.addEventListener('click', function() {
            // this.getAttribute('data-id')를 통해 버튼에 숨겨둔 ID를 꺼내옵니다.
            const milestoneId = this.getAttribute('data-id');
            location.href = `/milestones/update?id=${milestoneId}`;
        });
    }

    // 3. '삭제' 버튼
    if (btnDelete) {
        btnDelete.addEventListener('click', function() {
            const milestoneId = this.getAttribute('data-id');
            
            if (confirm('정말 이 마일스톤을 삭제하시겠습니까?')) {
                // 삭제 요청을 위해 임시 폼을 생성합니다.
                const form = document.createElement('form');
                form.method = 'POST'; 
                form.action = '/milestones/delete'; 

                // 💡 여기서도 서버로 몰래 ID를 보내기 위해 hidden 인풋을 동적으로 만듭니다!
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'milestoneId';
                input.value = milestoneId;

                form.appendChild(input);
                document.body.appendChild(form);
                form.submit();
            }
        });
    }
});