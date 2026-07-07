// 1. 아코디언 토글 기능
function toggleMilestone(element) {
    const icon = element.querySelector('.toggle-icon');
    const content = element.nextElementSibling;
    
    if (content.style.display === "none") {
        content.style.display = "block";
        icon.classList.add("open");
    } else {
        content.style.display = "none";
        icon.classList.remove("open");
    }
}

document.addEventListener("DOMContentLoaded", function() {
    if (!roadmapData || !roadmapData.milestoneList) return;

    // 데이터 집계용 변수 세팅
    let totalIssues = 0;           // 전체 일감 갯수
    let totalProgressSum = 0;      // 진행률 총합
    let totalEstimateSum = 0;      // 총 예상 소요시간
    let totalHoursSum = 0;         // 총 실제 소요시간
    const statusStats = { '완료': 0, '진행중': 0, '진행예정': 0, '기타': 0 };

    // 로드맵 데이터 루프 돌면서 계산
    roadmapData.milestoneList.forEach(milestone => {
        if(milestone.issueList) {
            milestone.issueList.forEach(issue => {
                if (!issue.issueId) return; // 빈 일감 객체 필터링
                totalIssues++; // 일감 갯수 +1
                totalProgressSum += (issue.progressRate || 0); // 진행률 누적
                totalEstimateSum += (issue.estimatedHours || 0); // 예상시간 누적
                totalHoursSum += (issue.hours || 0);             // 실제시간 누적
                
                // 상태별 갯수 누적
                const statusName = issue.issueStatusName || '미지정';
                if (statusName.includes('완료') || issue.progressRate === 100) {
                    statusStats['완료']++;
                } else if (statusName.includes('진행') || statusName.includes('진행중')) {
                    statusStats['진행중']++;
                } else if (statusName.includes('예정') || statusName.includes('대기')) {
                    statusStats['진행예정']++;
                } else {
                    statusStats['기타']++;
                }
            });
        }
    });

    // --- [1] 최상단 헤더 요약 정보 업데이트 ---
    const headerTotalIssues = document.getElementById('headerTotalIssues');
    if (headerTotalIssues) {
        headerTotalIssues.innerText = totalIssues + '개'; // 전체 일감 갯수 입력
        document.getElementById('headerTotalEstimate').innerText = totalEstimateSum;
        document.getElementById('headerTotalHours').innerText = totalHoursSum;
    }

    // --- [2] 로드맵 총 진행률 계산 및 버튼 활성화 ---
    const avgProgress = totalIssues === 0 ? 0 : Math.round(totalProgressSum / totalIssues);
    
    // 📝 모든 마일스톤이 완료(L003) 상태인지 체크 (미지정 마일스톤 제외)
    let allMilestonesCompleted = true;
    if (roadmapData.milestoneList) {
        roadmapData.milestoneList.forEach(milestone => {
            if (milestone.milestoneId && !milestone.milestoneId.startsWith("UNASSIGNED_")) {
                if (milestone.statusCode !== 'L003') {
                    allMilestonesCompleted = false;
                }
            }
        });
    }

    const btnComplete = document.getElementById('btnCompleteRoadmap');
    if (btnComplete) {
        if (avgProgress === 100 && allMilestonesCompleted && roadmapData.statusCode !== 'k003') {
            btnComplete.disabled = false; // 100% 달성 및 모든 마일스톤 완료 시 완료 버튼 활성화
            btnComplete.classList.remove('btn-disabled');
        } else {
            btnComplete.disabled = true; // 조건 미달 혹은 완료상태면 비활성화
            btnComplete.classList.add('btn-disabled');
        }
    }
    
    // 📝 완료 버튼 클릭 시 확인 알림창 작동 및 폼 서브밋 연동 (PFDialog.confirm 적용 및 중복클릭 방지)
    if (btnComplete) {
        btnComplete.addEventListener("click", function() {
            window.PFDialog.confirm({
                title: '로드맵 완료',
                message: '완료를 누르시면 더 이상 수정이 안됩니다. 계속하시겠습니까?',
                confirmText: '완료',
                icon: 'warning'
            }).then(function(confirmed) {
                if (confirmed) {
                    btnComplete.disabled = true;
                    
                    const completeForm = document.getElementById('completeRoadmapForm');
                    if (completeForm) {
                        completeForm.submit();
                    }
                }
            });
        });
    }
    // --- [3] 우측 차트 텍스트 업데이트 ---
    document.getElementById('mainProgressText').innerText = avgProgress + '%';
    document.getElementById('progressBreakdownText').innerHTML = 
        `완료 ${statusStats['완료']}개 | 진행중 ${statusStats['진행중']}개 | 예정 ${statusStats['진행예정']}개`;
    document.getElementById('totalIssueCount').innerText = totalIssues;

    // --- [4] 첫 번째 차트 (로드맵 총 진행률) ---
    const ctxProgress = document.getElementById('progressChart').getContext('2d');
    new Chart(ctxProgress, {
        type: 'doughnut',
        data: {
            labels: ['완료', '잔여'],
            datasets: [{
                data: [avgProgress, 100 - avgProgress],
                backgroundColor: ['#6366f1', '#e2e8f0'], // 남색 -> Indigo 테마 통일
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '80%',
            plugins: { legend: { display: false }, tooltip: { enabled: false } }
        }
    });

    // --- [5] 두 번째 차트 (일감 상태별 분포) ---
    const statusLabels = ['진행예정', '진행중', '완료', '기타'];
    const statusData = [statusStats['진행예정'], statusStats['진행중'], statusStats['완료'], statusStats['기타']];
    const colors = ['#94a3b8', '#6366f1', '#22c55e', '#a855f7']; // 파스텔 뱃지색에 일치하도록 보정

    const ctxStatus = document.getElementById('statusChart').getContext('2d');
    new Chart(ctxStatus, {
        type: 'doughnut',
        data: {
            labels: statusLabels,
            datasets: [{
                data: statusData,
                backgroundColor: colors,
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '70%',
            plugins: { legend: { display: false } }
        }
    });

    // --- [6] 커스텀 범례 생성 ---
    const legendContainer = document.getElementById('customLegend');
    statusLabels.forEach((label, index) => {
        const count = statusData[index];
        const pct = totalIssues === 0 ? 0 : Math.round((count / totalIssues) * 100);
        
        const itemHtml = `
            <div class="legend-item">
                <span class="legend-color" style="background-color: ${colors[index]}"></span>
                <span class="legend-label">${label}</span>
                <span class="legend-value">${count} (${pct}%)</span>
            </div>
        `;
        legendContainer.insertAdjacentHTML('beforeend', itemHtml);
    });
	
	// ==========================================
	    // [신규 추가] 마감 임박 일감 (Upcoming Deadlines) 로직
	    // ==========================================
	    const upcomingContainer = document.getElementById('upcomingIssuesContainer');
	    if (upcomingContainer && roadmapData.milestoneList) {
	        
	        let pendingIssues = [];
	        
	        // 시간(시/분/초)을 제외하고 오늘 날짜를 00:00:00으로 세팅 (순수 날짜 비교용)
	        const todayDate = new Date();
	        todayDate.setHours(0, 0, 0, 0);

	        // 1. 완료되지 않은 일감 수집 및 D-Day 계산
	        roadmapData.milestoneList.forEach(milestone => {
	            if(milestone.issueList) {
	                milestone.issueList.forEach(issue => {
	                    if (!issue.issueId) return; // 📝 수정: 빈 일감 객체 필터링
	                    const statusName = issue.issueStatusName || '';
	                    
	                    // 완료 상태거나 진척도가 100%면 제외, 목표일자가 없으면 제외
	                    if (!statusName.includes('완료') && issue.progressRate !== 100 && issue.dueDate) {
	                        
	                        const targetDate = new Date(issue.dueDate);
	                        targetDate.setHours(0, 0, 0, 0);

	                        // 밀리초(ms) 단위 차이를 일(day) 단위로 변환
	                        const diffTime = targetDate.getTime() - todayDate.getTime();
	                        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

	                        pendingIssues.push({
	                            title: issue.title,
	                            dueDate: issue.dueDate,
	                            status: statusName,
	                            dDay: diffDays
	                        });
	                    }
	                });
	            }
	        });

	        // 2. D-Day 오름차순 정렬 (마감일이 제일 가까운 순, 지연된 과거 날짜 포함)
	        pendingIssues.sort((a, b) => a.dDay - b.dDay);

	        // 3. 상위 3개만 추출 (Top 3)
	        const topIssues = pendingIssues.slice(0, 3);

	        // 4. 화면에 그리기 (렌더링)
	        if (topIssues.length === 0) {
	            upcomingContainer.innerHTML = '<div style="text-align:center; color:#999; padding: 30px 0;">진행 중인 일감이 없습니다.</div>';
	        } else {
	            let htmlStr = '';
	            topIssues.forEach(item => {
	                let dDayText = '';
	                let colorClass = '';

	                if (item.dDay < 0) {
	                    dDayText = `지연 (D+${Math.abs(item.dDay)})`; 
	                    colorClass = 'red'; // 이미 지남 (위험)
	                } else if (item.dDay === 0) {
	                    dDayText = 'D-Day';
	                    colorClass = 'red'; // 오늘 당장 (위험)
	                } else if (item.dDay <= 3) {
	                    dDayText = `D-${item.dDay}`;
	                    colorClass = 'red'; // 3일 이내도 빨간색으로 통일하여 위험 강조
	                } else {
	                    dDayText = `D-${item.dDay}`;
	                    colorClass = 'orange'; // 그 외 (안전/주의)
	                }

	                htmlStr += `
	                    <div class="upcoming-item border-${colorClass}">
	                        <div class="upcoming-info">
	                            <span class="upcoming-title" title="${item.title}">${item.title}</span>
	                            <span class="upcoming-date">${item.dueDate} (${item.status})</span>
	                        </div>
	                        <div class="d-day-badge badge-${colorClass}">${dDayText}</div>
	                    </div>
	                `;
	            });
	            upcomingContainer.innerHTML = htmlStr;
	        }
	    }
	
	const colorPalette = [
        { bg: '#e0e7ff', text: '#4f46e5', border: '#c7d2fe' }, // Indigo
        { bg: '#dcfce7', text: '#166534', border: '#bbf7d0' }, // Green
        { bg: '#fef3c7', text: '#92400e', border: '#fde68a' }, // Amber
        { bg: '#fee2e2', text: '#991b1b', border: '#fca5a5' }, // Red
        { bg: '#e0f2fe', text: '#0369a1', border: '#bae6fd' }, // Sky
        { bg: '#fae8ff', text: '#86198f', border: '#f5d0fe' }, // Fuchsia
        { bg: '#f3e8ff', text: '#6b21a8', border: '#e9d5ff' }, // Purple
        { bg: '#ffedd5', text: '#c2410c', border: '#fed7aa' }  // Orange
    ];
	    
	    function getHashFromString(str) {
	        if (!str) return 0;
	        let hash = 0;
	        for (let i = 0; i < str.length; i++) hash += str.charCodeAt(i);
	        return hash;
	    }
	    
	    // HTML에서 'dynamic-badge' 클래스를 가진 모든 요소를 찾아서 색상 입히기
	    document.querySelectorAll('.dynamic-badge').forEach(badge => {
	        const statusName = badge.getAttribute('data-status');
	        if(statusName) {
	            const colorIndex = getHashFromString(statusName) % colorPalette.length;
	            const colors = colorPalette[colorIndex];
	            badge.style.backgroundColor = colors.bg;
	            badge.style.color = colors.text;
	            badge.style.border = `1px solid ${colors.border}`;
	        } else {
	            badge.style.backgroundColor = '#f1f5f9';
	            badge.style.color = '#64748b';
	            badge.style.border = '1px solid #cbd5e1';
	        }
	    });
});