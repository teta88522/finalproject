document.addEventListener('DOMContentLoaded', function() {
    
	// 3. 마일스톤 생성 버튼 클릭 시 등록 페이지로 이동
	const btnCreateMilestone = document.getElementById('btnCreateMilestone');
	if(btnCreateMilestone) {
	    btnCreateMilestone.addEventListener('click', function() {
	        console.log('마일스톤 생성 페이지로 이동합니다.');
	        // 실제 프로젝트의 등록 화면 URL로 수정해 주세요. (예: /milestones/write 등)
	        window.location.href = '/milestones/create'; 
	    });
	}
	
    // 1. 마일스톤 제목/헤더 클릭 시 상세 페이지로 이동
    const milestoneHeaders = document.querySelectorAll('.clickable-milestone');
    milestoneHeaders.forEach(header => {
        header.addEventListener('click', function(e) {
            // 클릭된 곳이 상태 뱃지 같은 버튼류가 아닐 때만 이동
            if(!e.target.classList.contains('status-badge')) {
                // 부모 컨테이너(.milestone-item)에서 ID 추출
                const milestoneItem = this.closest('.milestone-item');
                const milestoneId = milestoneItem.getAttribute('data-milestone-id');
                
                if(milestoneId) {
                    console.log(`마일스톤 ${milestoneId} 상세 페이지로 이동합니다.`);
                    // 실제 프로젝트 경로에 맞게 수정하세요.
                    window.location.href = `/milestones/detail?id=${milestoneId}`;
                }
            }
        });
    });

    // 2. 하위 일감 항목 클릭 시 해당 일감 상세 페이지로 이동
    const issueItems = document.querySelectorAll('.clickable-issue');
    issueItems.forEach(issue => {
        issue.addEventListener('click', function(e) {
            // 맨 우측의 옵션 점 3개 아이콘 클릭 시 상세 페이지 이동 방지
            if(!e.target.classList.contains('fa-ellipsis-v')) {
                const issueId = this.getAttribute('data-issue-id');
                
                if(issueId) {
                    console.log(`일감 ${issueId} 상세 페이지로 이동합니다.`);
                    // 실제 프로젝트 경로에 맞게 수정하세요.
                    window.location.href = `/issues/detail?id=${issueId}`;
                }
            } else {
                // 점 3개 아이콘 클릭 시 옵션 메뉴 열기 등의 로직
                console.log('옵션 메뉴 열기');
                e.stopPropagation(); // 부모(일감 항목) 클릭 이벤트 전파 차단
            }
        });
    });

});