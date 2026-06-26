document.addEventListener('DOMContentLoaded', function() {
    
    // 1. 설정 버튼 클릭 이벤트 (원하는 링크로 수정하세요)
    const btnSettings = document.getElementById('btnSettings');
    if(btnSettings) {
        btnSettings.addEventListener('click', () => {
            console.log('설정 페이지로 이동합니다.');
            window.location.href = '/roadmap/setting_list';
        });
    }

    // 2. 아코디언 토글 로직
    const headers = document.querySelectorAll('.toggle-accordion');
    headers.forEach(header => {
        header.addEventListener('click', function(e) {
            // 텍스트 링크 클릭 시 펴지지 않고 페이지 이동 허용
            if(e.target.classList.contains('no-accordion') || e.target.closest('.no-accordion')) return;
            
            const item = this.closest('.roadmap-item');
            const body = item.querySelector('.roadmap-body');
            
            if(item.classList.contains('open')) {
                body.style.display = 'none';
                item.classList.remove('open');
            } else {
                body.style.display = 'block';
                item.classList.add('open');
            }
        });
    });

    // 3. 동적 상태 색상 배정기 (Hash Palette)
    const colorPalette = ['#007bff', '#28a745', '#e83e8c', '#fd7e14', '#6f42c1', '#17a2b8', '#dc3545', '#20c997'];
    
    function getHashFromString(str) {
        if (!str) return 0;
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
    // 4. 실시간 프론트엔드 필터링 로직
    // ==========================================
    const roadmapItems = document.querySelectorAll('.roadmap-item');
    const selRoadmap = document.getElementById('roadmapFilter');
    const selMilestone = document.getElementById('milestoneFilter');
    const inputIssue = document.getElementById('issueSearch');

    // 화면 데이터를 읽어 드롭다운(로드맵, 마일스톤) 옵션 자동 세팅
    const uniqueRoadmaps = new Set();
    const uniqueMilestones = new Set();

    roadmapItems.forEach(item => {
        const rName = item.querySelector('.title-link').innerText.trim();
        uniqueRoadmaps.add(rName);

        const mLinks = item.querySelectorAll('.m-title-area .title-link');
        mLinks.forEach(m => uniqueMilestones.add(m.innerText.trim()));
    });

    uniqueRoadmaps.forEach(v => selRoadmap.add(new Option(v, v)));
    uniqueMilestones.forEach(m => selMilestone.add(new Option(m, m)));

    // 필터 실행 함수
    function applyFilters() {
        const valRoadmap = selRoadmap.value;
        const valMilestone = selMilestone.value;
        const valSearch = inputIssue.value.toLowerCase().trim();

        roadmapItems.forEach(item => {
            const rName = item.querySelector('.title-link').innerText.trim();
            
            // 1차 필터: 로드맵 드롭다운 매칭
            let isRoadmapMatch = true;
            if(valRoadmap && rName !== valRoadmap) isRoadmapMatch = false;

            let hasMatchingMilestoneOrIssue = false;
            
            // 2차 필터: 하위 마일스톤 & 일감 검색 매칭
            const milestones = item.querySelectorAll('.milestone-block');
            milestones.forEach(mBlock => {
                const mName = mBlock.querySelector('.m-title-area .title-link').innerText.trim();
                let isMilestoneMatch = true;
                if (valMilestone && mName !== valMilestone) isMilestoneMatch = false;

                const issues = mBlock.querySelectorAll('.issue-item');
                let hasMatchingIssue = false;

                issues.forEach(issue => {
                    const iTitle = issue.querySelector('.js-issue-title').innerText.toLowerCase();
                    if(valSearch === "" || iTitle.includes(valSearch)) {
                        issue.style.display = 'flex';
                        hasMatchingIssue = true;
                    } else {
                        issue.style.display = 'none';
                    }
                });

                // 마일스톤 매칭 여부 최종 결정
                if (isMilestoneMatch && (valSearch === "" || hasMatchingIssue)) {
                    mBlock.style.display = 'block';
                    hasMatchingMilestoneOrIssue = true;
                } else {
                    mBlock.style.display = 'none';
                }
            });

            // 검색어 혹은 마일스톤 필터가 활성화된 경우, 하위에 일치하는 게 없으면 로드맵도 숨김
            if(valMilestone !== "" || valSearch !== "") {
                if (!hasMatchingMilestoneOrIssue) isRoadmapMatch = false;
            }

            // 최종 렌더링
            if(isRoadmapMatch) {
                item.style.display = 'block';
                // 검색어나 마일스톤 필터가 작동 중이면 아코디언 자동으로 열기
                if((valMilestone !== "" || valSearch !== "") && !item.classList.contains('open')) {
                    item.querySelector('.roadmap-body').style.display = 'block';
                    item.classList.add('open');
                }
            } else {
                item.style.display = 'none';
            }
        });
    }

    // 이벤트 리스너 등록
    selRoadmap.addEventListener('change', applyFilters);
    selMilestone.addEventListener('change', applyFilters);
    inputIssue.addEventListener('input', applyFilters); // 타이핑 즉시 필터링
});