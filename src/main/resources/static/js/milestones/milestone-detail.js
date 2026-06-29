document.addEventListener('DOMContentLoaded', function() {
    
    // 1. 이벤트 리스너가 'e'를 인자로 받을 수 있도록 함수 구문을 확인하세요.
    document.addEventListener('click', function(e) {
        
        // 2. e.target이 존재하는지 먼저 확인
        if (!e.target) return;

        // 목록 버튼
        if (e.target.id === 'btnList') {
            const url = e.target.getAttribute('data-url');
            if(url) location.href = url;
        }

        // 수정 버튼
        if (e.target.id === 'btnEdit') {
            const milestoneId = e.target.getAttribute('data-id');
            const baseUrl = e.target.getAttribute('data-url');
            if(baseUrl) location.href = `${baseUrl}?id=${milestoneId}`;
        }

        // 삭제 버튼
        if (e.target.id === 'btnDelete') {
            const milestoneId = e.target.getAttribute('data-id');
            const actionUrl = e.target.getAttribute('data-url');
            
            if (confirm('정말 이 마일스톤을 삭제하시겠습니까?')) {
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = actionUrl;

                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'milestoneId';
                input.value = milestoneId;

                form.appendChild(input);
                document.body.appendChild(form);
                form.submit();
            }
        }
    });
});