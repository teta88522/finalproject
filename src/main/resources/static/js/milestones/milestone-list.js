document.addEventListener('DOMContentLoaded', function() {
    
    // 1. 마일스톤 생성 버튼 이동
	const btnCreateMilestone = document.getElementById('btnCreateMilestone');
	if (btnCreateMilestone) {
	    btnCreateMilestone.addEventListener('click', (e) => {
	        // HTML에 심어둔 경로를 가져와서 이동
	        const url = e.target.getAttribute('data-url');
	        location.assign(url);
	    });
	}
    
    // 2. 아코디언 토글
    const headers = document.querySelectorAll('.toggle-accordion');
    headers.forEach(header => {
        header.addEventListener('click', function(e) {
            if(e.target.classList.contains('no-accordion') || e.target.closest('.no-accordion')) return;
            
            const item = this.closest('.milestone-item');
            const body = item.querySelector('.milestone-body');
            
            if(item.classList.contains('open')) {
                body.style.display = 'none';
                item.classList.remove('open');
            } else {
                body.style.display = 'block';
                item.classList.add('open');
            }
        });
    });

    // 3. 동적 상태 색상 배정기
    const colorPalette = ['#007bff', '#28a745', '#e83e8c', '#fd7e14', '#6f42c1', '#17a2b8', '#dc3545', '#20c997'];
    function getHashFromString(str) {
        let hash = 0;
        for (let i = 0; i < str.length; i++) hash += str.charCodeAt(i);
        return hash;
    }
    document.querySelectorAll('.dynamic-badge').forEach(badge => {
        const statusName = badge.getAttribute('data-status');
        if(statusName) {
            const colorIndex = getHashFromString(statusName) % colorPalette.length;
            badge.style.backgroundColor = colorPalette[colorIndex];
        } else {
            badge.style.backgroundColor = '#6c757d';
        }
    });

    // ==========================================
    // 4. 실시간 프론트엔드 필터링 (날짜 검색 포함)
    // ==========================================
    const milestoneItems = document.querySelectorAll('.milestone-item');
    const selVersion = document.getElementById('jsVersionFilter');
    const selMilestone = document.getElementById('jsMilestoneFilter');
    const inputIssue = document.getElementById('jsIssueSearch');
    const inputStartDate = document.getElementById('jsStartDate');
    const inputEndDate = document.getElementById('jsEndDate');

    // 드롭다운 옵션 자동 채우기
    const uniqueVersions = new Set();
    const uniqueMilestones = new Set();
    milestoneItems.forEach(item => {
        uniqueVersions.add(item.getAttribute('data-version-name'));
        uniqueMilestones.add(item.getAttribute('data-milestone-title'));
    });
    uniqueVersions.forEach(v => selVersion.add(new Option(v, v)));
    uniqueMilestones.forEach(m => selMilestone.add(new Option(m, m)));

    // 필터 실행 함수
    function applyFilters() {
        const valVersion = selVersion.value;
        const valMilestone = selMilestone.value;
        const valSearch = inputIssue.value.toLowerCase().trim();
        const valStart = inputStartDate.value;
        const valEnd = inputEndDate.value;

        milestoneItems.forEach(item => {
            const itemVersion = item.getAttribute('data-version-name');
            const itemTitle = item.getAttribute('data-milestone-title');
            const itemStartDate = item.getAttribute('data-start-date'); // YYYY-MM-DD
            const itemEndDate = item.getAttribute('data-end-date');     // YYYY-MM-DD
            const issues = item.querySelectorAll('.issue-item');
            
            let isMatch = true;

            // 1. 드롭다운 매칭
            if(valVersion && itemVersion !== valVersion) isMatch = false;
            if(valMilestone && itemTitle !== valMilestone) isMatch = false;

            // 2. 날짜 기간 매칭 (문자열 비교로 날짜 대소 판별 가능)
            if(valStart && itemStartDate < valStart) isMatch = false;
            if(valEnd && itemEndDate > valEnd) isMatch = false;

            // 3. 일감 텍스트 검색 매칭
            let hasMatchingIssue = false;
            issues.forEach(issue => {
                const issueTitle = issue.querySelector('.js-issue-title').innerText.toLowerCase();
                if(valSearch === "" || issueTitle.includes(valSearch)) {
                    issue.style.display = 'flex'; 
                    hasMatchingIssue = true;
                } else {
                    issue.style.display = 'none'; 
                }
            });

            // 검색어가 있는데 일감이 하나도 안 맞으면 숨김
            if (valSearch !== "" && !hasMatchingIssue) {
                isMatch = false;
            }

            // 최종 렌더링 처리
            if(isMatch) {
                item.style.display = 'block';
                if(valSearch !== "" && !item.classList.contains('open')) {
                    item.querySelector('.milestone-body').style.display = 'block';
                    item.classList.add('open');
                }
            } else {
                item.style.display = 'none';
            }
        });
    }

    // 📝 [검색] 버튼을 누를 때만 필터링 기능이 동작하도록 수정
    const btnFilterSearch = document.getElementById('btnFilterSearch');
    const btnFilterReset = document.getElementById('btnFilterReset');

    if (btnFilterSearch) {
        btnFilterSearch.addEventListener('click', applyFilters);
    }

    // 📝 [초기화] 버튼 누를 시 모든 값을 공백화하고 화면을 깨끗이 원복
    if (btnFilterReset) {
        btnFilterReset.addEventListener('click', function() {
            if (selVersion) selVersion.value = "";
            if (selMilestone) selMilestone.value = "";
            if (inputIssue) inputIssue.value = "";
            if (inputStartDate) inputStartDate.value = "";
            if (inputEndDate) inputEndDate.value = "";
            applyFilters(); // 필터 리셋 결과 렌더링
        });
    }
});