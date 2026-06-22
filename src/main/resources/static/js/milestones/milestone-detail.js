document.addEventListener('DOMContentLoaded', function() {
    
    // 버튼을 직접 찾지 않고, 문서 전체에서 클릭을 감시합니다.
    document.addEventListener('click', function(e) {
        
        // 1. '목록으로' 버튼 클릭 시
        if (e.target && e.target.id === 'btnList') {
            location.href = '/milestones/list';
        }

        // 2. '수정' 버튼 클릭 시
        if (e.target && e.target.id === 'btnEdit') {
            const milestoneId = e.target.getAttribute('data-id');
            location.href = `/milestones/update?id=${milestoneId}`;
        }

        // 3. '삭제' 버튼 클릭 시
        if (e.target && e.target.id === 'btnDelete') {
            const milestoneId = e.target.getAttribute('data-id');
            
            if (confirm('정말 이 마일스톤을 삭제하시겠습니까?')) {
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '/milestones/delete';

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