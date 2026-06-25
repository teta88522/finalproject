document.addEventListener('DOMContentLoaded', function() {
    
    // 1. 마일스톤 생성 버튼 이동
    const btnCreateMilestone = document.getElementById('btnCreateMilestone');
    if(btnCreateMilestone) {
        btnCreateMilestone.addEventListener('click', () => window.location.href = '/milestones/create');
    }
    
    // 2. 아코디언 기능 (마일스톤 헤더 클릭 시)
    const headers = document.querySelectorAll('.toggle-accordion');
    headers.forEach(header => {
        header.addEventListener('click', function(e) {
            // "마일스톤 제목" 링크 자체를 클릭했을 때는 아코디언 동작 대신 페이지 이동 허용
            if(e.target.classList.contains('no-accordion') || e.target.closest('.no-accordion')) {
                return; 
            }
            
            const item = this.closest('.milestone-item');
            const body = item.querySelector('.milestone-body');
            
            // Toggle 로직
            if(item.classList.contains('open')) {
                body.style.display = 'none';
                item.classList.remove('open');
            } else {
                body.style.display = 'block';
                item.classList.add('open');
            }
        });
    });

    // 3. 🌟 동적 상태 색상 배정기 (Hash Palette)
    // 보기 편안하고 디자인과 어울리는 색상들을 미리 정의해 둡니다.
    const colorPalette = [
        '#007bff', // Blue
        '#28a745', // Green
        '#e83e8c', // Pink
        '#fd7e14', // Orange
        '#6f42c1', // Purple
        '#17a2b8', // Teal
        '#dc3545', // Red
        '#20c997', // Mint
        '#6610f2'  // Indigo
    ];

    // 문자열을 해시(숫자)로 변환하는 함수
    function getHashFromString(str) {
        let hash = 0;
        for (let i = 0; i < str.length; i++) {
            hash += str.charCodeAt(i);
        }
        return hash;
    }

    // HTML에 렌더링된 모든 뱃지를 찾아서 색상을 칠합니다.
    const badges = document.querySelectorAll('.dynamic-badge');
    badges.forEach(badge => {
        const statusName = badge.getAttribute('data-status');
        if(statusName) {
            // 상태 이름의 해시값을 구한 뒤, 팔레트 길이로 나눈 나머지를 인덱스로 사용
            const colorIndex = getHashFromString(statusName) % colorPalette.length;
            badge.style.backgroundColor = colorPalette[colorIndex];
        } else {
            badge.style.backgroundColor = '#6c757d'; // 상태가 없을 때 기본 회색
        }
    });
});