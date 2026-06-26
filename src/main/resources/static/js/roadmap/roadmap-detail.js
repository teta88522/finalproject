function toggleMilestone(element) {
    const icon = element.querySelector('.toggle-icon');
    const content = element.nextElementSibling;
    content.style.display = (content.style.display === "none") ? "block" : "none";
    icon.classList.toggle("open");
}

document.addEventListener("DOMContentLoaded", function() {
    if (!roadmapData || !roadmapData.milestoneList) return;

    let totalIssues = 0;
    let totalProgressSum = 0;
    const statusStats = { '완료': 0, '진행중': 0, '진행예정': 0, '기타': 0 };

    roadmapData.milestoneList.forEach(milestone => {
        if(milestone.issueList) {
            milestone.issueList.forEach(issue => {
                totalIssues++;
                totalProgressSum += (issue.progressRate || 0);
                const statusName = issue.issueStatusName || '미지정';
                if (statusName.includes('완료') || issue.progressRate === 100) statusStats['완료']++;
                else if (statusName.includes('진행') || statusName.includes('진행중')) statusStats['진행중']++;
                else if (statusName.includes('예정') || statusName.includes('대기')) statusStats['진행예정']++;
                else statusStats['기타']++;
            });
        }
    });

    const avgProgress = totalIssues === 0 ? 0 : Math.round(totalProgressSum / totalIssues);
    const btnComplete = document.getElementById('btnCompleteRoadmap');
    if (btnComplete && avgProgress === 100) btnComplete.disabled = false;

    document.getElementById('mainProgressText').innerText = avgProgress + '%';
    document.getElementById('progressBreakdownText').innerHTML = 
        `완료 ${statusStats['완료']}개 | 진행중 ${statusStats['진행중']}개 | 예정 ${statusStats['진행예정']}개`;
    document.getElementById('totalIssueCount').innerText = totalIssues;

    new Chart(document.getElementById('progressChart').getContext('2d'), {
        type: 'doughnut',
        data: { labels: ['완료', '잔여'], datasets: [{ data: [avgProgress, 100 - avgProgress], backgroundColor: ['#3B82F6', '#E2E8F0'], borderWidth: 0 }] },
        options: { responsive: true, maintainAspectRatio: false, cutout: '80%', plugins: { legend: { display: false }, tooltip: { enabled: false } } }
    });

    const statusLabels = ['진행예정', '진행중', '완료', '기타'];
    const statusData = [statusStats['진행예정'], statusStats['진행중'], statusStats['완료'], statusStats['기타']];
    const colors = ['#9CA3AF', '#3B82F6', '#22C55E', '#A855F7'];

    new Chart(document.getElementById('statusChart').getContext('2d'), {
        type: 'doughnut',
        data: { labels: statusLabels, datasets: [{ data: statusData, backgroundColor: colors, borderWidth: 0 }] },
        options: { responsive: true, maintainAspectRatio: false, cutout: '70%', plugins: { legend: { display: false } } }
    });

    statusLabels.forEach((label, index) => {
        document.getElementById('customLegend').insertAdjacentHTML('beforeend', 
            `<div class="legend-item"><span class="legend-color" style="background-color: ${colors[index]}"></span>${label} (${statusData[index]}개)</div>`);
    });
});