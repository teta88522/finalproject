document.addEventListener('DOMContentLoaded', function() {
    
    // 1. 아코디언 토글 로직
    const listPanel = document.querySelector('.list-panel');
    if (listPanel) {
        listPanel.addEventListener('click', function(e) {
            const roadmapHeader = e.target.closest('.roadmap-header');
            if (roadmapHeader) {
                // 버튼(관리 등)을 누를 때는 아코디언이 작동하지 않도록
                if (e.target.closest('.btn') || e.target.tagName === 'A') return;

                roadmapHeader.classList.toggle('active');
                const body = roadmapHeader.nextElementSibling;
                if (body && body.classList.contains('roadmap-body')) {
                    body.style.display = body.style.display === 'none' ? 'block' : 'none';
                }
            }
        });
    }

    // 2. Select 필터 옵션 자동 생성 (화면에 있는 데이터 긁어오기)
    const roadmapFilter = document.getElementById('roadmapFilter');
    const milestoneFilter = document.getElementById('milestoneFilter');
    const issueSearch = document.getElementById('issueSearch');

    if (roadmapFilter && milestoneFilter) {
        const roadmaps = document.querySelectorAll('.roadmap-item');
        roadmaps.forEach(roadmap => {
            // 로드맵 드롭다운 채우기
            const rTitle = roadmap.querySelector('.title-area h3').innerText.trim();
            const rId = roadmap.getAttribute('data-roadmap-id');
            roadmapFilter.insertAdjacentHTML('beforeend', `<option value="${rId}">${rTitle}</option>`);

            // 마일스톤 드롭다운 채우기
            const milestones = roadmap.querySelectorAll('.milestone-item');
            milestones.forEach(milestone => {
                const mTitle = milestone.querySelector('.m-title').innerText.trim();
                const mId = milestone.getAttribute('data-milestone-id');
                milestoneFilter.insertAdjacentHTML('beforeend', `<option value="${mId}">${mTitle}</option>`);
            });
        });
    }

    // 3. 다중 연동 필터링 기능
    function applyFilters() {
        const selectedRoadmap = roadmapFilter.value;
        const selectedMilestone = milestoneFilter.value;
        const keyword = issueSearch.value.toLowerCase().trim();

        const roadmaps = document.querySelectorAll('.roadmap-item');

        roadmaps.forEach(roadmap => {
            const rId = roadmap.getAttribute('data-roadmap-id');
            let roadmapHasVisibleContent = false;

            // 3-1. 로드맵 필터 불일치 시 전체 숨김
            if (selectedRoadmap && selectedRoadmap !== rId) {
                roadmap.style.display = 'none';
                return; 
            }

            const milestones = roadmap.querySelectorAll('.milestone-item');
            milestones.forEach(milestone => {
                const mId = milestone.getAttribute('data-milestone-id');
                let milestoneHasVisibleContent = false;

                // 3-2. 마일스톤 필터 불일치 시 마일스톤 숨김
                if (selectedMilestone && selectedMilestone !== mId) {
                    milestone.style.display = 'none';
                    return; 
                }

                // 3-3. 일감 검색 처리
                const issues = milestone.querySelectorAll('.issue-item');
                let issueMatchCount = 0;

                issues.forEach(issue => {
                    const issueTitle = issue.querySelector('.issue-info').innerText.toLowerCase();
                    if (keyword && !issueTitle.includes(keyword)) {
                        issue.style.display = 'none';
                    } else {
                        issue.style.display = 'flex';
                        issueMatchCount++;
                    }
                });

                // 키워드가 있는데 일감이 하나도 안 맞으면 마일스톤 숨김
                if (keyword && issueMatchCount === 0) {
                    milestone.style.display = 'none';
                } else {
                    milestone.style.display = 'block';
                    roadmapHasVisibleContent = true;
                }
            });

            // 내부 마일스톤이 하나라도 보이거나, 아무 검색 조건이 없을 때 로드맵 표시
            if (roadmapHasVisibleContent || (!selectedMilestone && !keyword)) {
                roadmap.style.display = 'block';
            } else {
                roadmap.style.display = 'none';
            }
        });
    }

    // 필터 이벤트 리스너 등록
    if (roadmapFilter) roadmapFilter.addEventListener('change', applyFilters);
    if (milestoneFilter) milestoneFilter.addEventListener('change', applyFilters);
    if (issueSearch) issueSearch.addEventListener('input', applyFilters);
});
