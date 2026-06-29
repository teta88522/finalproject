// 1. 필터 상태 관리 객체 초기화
let filterValues = {
    milestone: 'all',
    status: 'all',
    assignee: 'all',
    text: '',
    start: '',
    end: ''
};

// 2. 필터 적용 함수 (HTML에서 onchange / onkeyup 시 호출됨)
function filterGantt() {
    filterValues.milestone = document.getElementById('f_milestone').value;
    filterValues.status = document.getElementById('f_status').value;
    filterValues.assignee = document.getElementById('f_assignee').value;
    filterValues.text = document.getElementById('f_text').value.toLowerCase();
    filterValues.start = document.getElementById('f_start').value;
    filterValues.end = document.getElementById('f_end').value;

    gantt.render(); // 필터 적용 후 간트차트 다시 그리기
}

document.addEventListener("DOMContentLoaded", function () {

    // 데이터가 로드되고 파싱된 직후에 드롭다운 리스트 자동 생성
    gantt.attachEvent("onParse", function() {
        populateFilterDropdowns();
    });

    // 화면에 태스크를 그리기 전 필터링 로직 적용
    gantt.attachEvent("onBeforeTaskDisplay", function(id, task) {
        
        // 개별 태스크가 필터 조건에 맞는지 확인하는 내부 함수
        function isMatch(t) {
            // 텍스트 검색 필터
            if (filterValues.text && t.text.toLowerCase().indexOf(filterValues.text) === -1) return false;
            
            // 상태 필터
            if (filterValues.status !== 'all' && t.issueStatusName !== filterValues.status) return false;
            
            // 담당자 필터
            if (filterValues.assignee !== 'all' && t.assigneeName !== filterValues.assignee) return false;
            
            // 마일스톤 필터
            if (filterValues.milestone !== 'all') {
                if (t.id != filterValues.milestone && t.parent != filterValues.milestone) return false;
            }

            // 기간 검색 필터
            if (filterValues.start && filterValues.end) {
                let filterStart = new Date(filterValues.start);
                let filterEnd = new Date(filterValues.end);
                if (t.end_date <= filterStart || t.start_date >= filterEnd) return false;
            }

            return true;
        }

        // 현재 태스크가 조건에 맞으면 그대로 표시
        if (isMatch(task)) return true;

        // 🚨 핵심 해결 포인트: 
        // 현재 태스크(마일스톤)가 조건에 안 맞더라도, 자식(일감)이 하나라도 조건에 맞으면 부모를 화면에 표시합니다.
        if (gantt.hasChild(id)) {
            let children = gantt.getChildren(id);
            for (let i = 0; i < children.length; i++) {
                let childTask = gantt.getTask(children[i]);
                if (isMatch(childTask)) {
                    return true;
                }
            }
        }

        return false; // 본인도 조건에 안 맞고, 조건에 맞는 자식도 없으면 최종적으로 숨김
    });
});

// 3. 드롭다운 옵션 자동 생성 함수
function populateFilterDropdowns() {
    const tasks = gantt.getTaskByTime();
    
    // 중복 방지를 위한 Set 사용
    const milestones = new Set();
    const statuses = new Set();
    const assignees = new Set();

    tasks.forEach(task => {
        // 부모 값이 없거나 0이면 최상위 태스크(마일스톤)로 취급
        if (!task.parent || task.parent == 0) {
            milestones.add(JSON.stringify({ id: task.id, text: task.text }));
        }
        
        if (task.issueStatusName) statuses.add(task.issueStatusName);
        if (task.assigneeName) assignees.add(task.assigneeName);
    });

    const msSelect = document.getElementById('f_milestone');
    const stSelect = document.getElementById('f_status');
    const asSelect = document.getElementById('f_assignee');

    if (!msSelect || !stSelect || !asSelect) return;

    // 기존 리스트 초기화 (첫 번째 '전체' 옵션만 남김)
    msSelect.options.length = 1;
    stSelect.options.length = 1;
    asSelect.options.length = 1;

    // 추출된 데이터 꽂아넣기
    milestones.forEach(item => {
        let ms = JSON.parse(item);
        msSelect.add(new Option(ms.text, ms.id));
    });
    statuses.forEach(status => stSelect.add(new Option(status, status)));
    assignees.forEach(assignee => asSelect.add(new Option(assignee, assignee)));
}

gantt.attachEvent("onParse", function() {
    gantt.sort("start_date", true);
});