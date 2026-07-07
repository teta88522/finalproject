// 1. 필터 상태 관리 객체 초기화
let filterValues = {
    milestone: 'all',
    status: 'all',
    assignee: 'all',
    text: '',
    start: '',
    end: ''
};

// 2. 필터 적용 함수
function filterGantt() {
    filterValues.milestone = document.getElementById('f_milestone').value;
    filterValues.status = document.getElementById('f_status').value;
    filterValues.assignee = document.getElementById('f_assignee').value;
    filterValues.text = document.getElementById('f_text').value.toLowerCase();
    filterValues.start = document.getElementById('f_start').value;
    filterValues.end = document.getElementById('f_end').value;

    // 📅 입력창의 날짜에 맞추어 간트차트 가로 타임라인 범위를 칼각 동기화
    if (filterValues.start) {
        gantt.config.start_date = new Date(filterValues.start);
    } else {
        gantt.config.start_date = null;
    }
    
    if (filterValues.end) {
        gantt.config.end_date = new Date(filterValues.end);
    } else {
        gantt.config.end_date = null;
    }

    gantt.render(); // 필터 적용 후 간트차트 다시 그리기
}

document.addEventListener("DOMContentLoaded", function () {
    // HTML에서 window.projectId에 담아 전달해 준 프로젝트 ID 취득
    const projectId = window.projectId;
    if (!projectId) return;



    // --- [Gantt 기본 환경 설정] ---
    gantt.config.autosize = "y";
    gantt.config.date_format = "%Y-%m-%d";
    gantt.config.readonly = true;

    // [기술적 이유]: 최상위 부모(마일스톤) 태스크의 날짜 범위가 자식 일감들의 기간에 의해 
    // 자동으로 뭉개지고 재계산(Rollup)되는 라이브러리 기본 스펙을 차단하고, DB 고유 날짜를 유지합니다.
    gantt.config.project_start_date = true;
    gantt.config.project_end_date = true;
    gantt.config.work_time = false;
    
    gantt.config.columns = [
        { name : "text", label: "마일스톤 / 일감명", tree : true, width: 200},
        { name : "assigneeName", label : "담당자", align : "center", width: 80 },
        { name : "issueStatusName", label : "상태", align : "center", width: 80 },
        { name : "start_date", label :"시작일" ,align : "center", width: 120},
        { name : "end_date", label :"종료일" ,align : "center", width: 120},
        { name : "duration", label : "기간(일)", align: "center", width: 80},
        { name : "progress", label: "진행률", align: "center", width: 80 , template: function(obj){
            return Math.round((obj.progress || 0) * 100) +"%";
        }}
    ];

    gantt.config.scales = [
        { unit: "month", step: 1, format: "%Y년 %m월" }, 
        { 
            unit: "day", 
            step: 1, 
            format: function(date) {
                const days = ["일", "월", "화", "수", "목", "금", "토"];
                const dayName = days[date.getDay()]; // 요일 가져오기
                const dayNumber = gantt.date.date_to_str("%d")(date); 
                return dayNumber + " (" + dayName + ")"; 
            } 
        }
    ];
    
    gantt.config.smart_rendering = true;

    // 🚨 [기술적 이유]: 과거/미래의 모든 마일스톤과 일감 텍스트가 왼쪽 표(그리드)에 
    // 항상 누락 없이 100% 노출되도록 데이터 기간에 맞춤 자동 확장을 활성화합니다.
    gantt.config.fit_tasks = true;

    // 📅 [초기 default 세팅]: 오늘날짜와 오늘+15일 날짜 계산
    const today = new Date();
    const future = new Date(today);
    future.setDate(today.getDate() + 15);
    
    // YYYY-MM-DD 규격 변환기 (오라클/인풋 바인딩용)
    const formatDate = (date) => {
        const yyyy = date.getFullYear();
        const mm = String(date.getMonth() + 1).padStart(2, '0');
        const dd = String(date.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}`;
    };

    const inputStart = document.getElementById('f_start');
    const inputEnd = document.getElementById('f_end');
    
    if (inputStart && inputEnd) {
        inputStart.value = formatDate(today);
        inputEnd.value = formatDate(future);
        
        // 필터 상태값에도 즉시 꽂아넣기
        filterValues.start = inputStart.value;
        filterValues.end = inputEnd.value;
    }

    // 기본 축 지정 후 간트차트 기동
    if (filterValues.start) gantt.config.start_date = new Date(filterValues.start);
    if (filterValues.end) gantt.config.end_date = new Date(filterValues.end);

    gantt.init("gantt_container");

    // 데이터가 로드되고 파싱된 직후에 드롭다운 리스트 자동 생성 및 시작일 기준 정렬
    gantt.attachEvent("onParse", function() {
        populateFilterDropdowns();
        gantt.sort("start_date", false); 
    });

    // [기술적 이유] 최초 데이터 로드 시점에 '오늘' 날짜가 속한 가로 픽셀 좌표(x)를 역계산하여,
    // 스크롤바가 오늘 날짜 눈금을 정면 맨 왼쪽에 딱 정렬해서 띄워주도록 제어합니다.
    gantt.attachEvent("onLoadEnd", function() {
        const todayX = gantt.posFromDate(new Date());
        gantt.scrollTo(todayX, null);
        
        // 🌟 [추가]: 데이터 로딩과 차트 그리기가 완벽히 다 끝났으므로 필터와 차트를 짠! 하고 동시에 노출합니다.
        const allWrapper = document.getElementById('ganttAllWrapper');
        if (allWrapper) {
            allWrapper.style.opacity = '1';
        }
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

        // 부모(마일스톤)가 조건에 안 맞더라도, 자식(일감)이 하나라도 조건에 맞으면 부모를 화면에 노출
        if (gantt.hasChild(id)) {
            let children = gantt.getChildren(id);
            for (let i = 0; i < children.length; i++) {
                let childTask = gantt.getTask(children[i]);
                if (isMatch(childTask)) {
                    return true;
                }
            }
        }

        return false; 
    });

    gantt.load(`/project/${projectId}/gantt/api/data`);

    // ➕/➖ 버튼 클릭 시 기간 검색창의 날짜를 15일씩 가변 연산 연동
    const btnZoomIn = document.getElementById("btnZoomIn");   // 미래 +15일 버튼
    const btnZoomOut = document.getElementById("btnZoomOut"); // 과거 -15일 버튼

    if (btnZoomIn && btnZoomOut) {
        // ➕ 버튼 클릭 시: [종료일] 입력창의 날짜를 미래로 +15일 연장 후 자동 검색
        btnZoomIn.addEventListener("click", function() {
            const inputEnd = document.getElementById('f_end');
            if (inputEnd && inputEnd.value) {
                let currEnd = new Date(inputEnd.value);
                currEnd.setDate(currEnd.getDate() + 15);
                inputEnd.value = formatDate(currEnd);
                filterGantt(); // 동기화 및 렌더링 호출
            }
        });

        // ➖ 버튼 클릭 시: [시작일] 입력창의 날짜를 과거로 -15일 연장 후 자동 검색
        btnZoomOut.addEventListener("click", function() {
            const inputStart = document.getElementById('f_start');
            if (inputStart && inputStart.value) {
                let currStart = new Date(inputStart.value);
                currStart.setDate(currStart.getDate() - 15);
                inputStart.value = formatDate(currStart);
                filterGantt(); // 동기화 및 렌더링 호출
            }
        });
    }

    // --- [일/주/월 스케일 동적 전환 로직 추가] ---
    const btnScaleDay = document.getElementById("btnScaleDay");
    const btnScaleWeek = document.getElementById("btnScaleWeek");
    const btnScaleMonth = document.getElementById("btnScaleMonth");
    const scaleButtons = [btnScaleDay, btnScaleWeek, btnScaleMonth];

    function setScaleActive(activeBtn) {
        scaleButtons.forEach(btn => {
            if (btn) {
                btn.classList.remove("active-scale");
                btn.style.fontWeight = "normal";
                btn.style.backgroundColor = "";
                btn.style.color = "";
                btn.style.borderColor = "";
            }
        });
        if (activeBtn) {
            activeBtn.classList.add("active-scale");
            activeBtn.style.fontWeight = "bold";
            activeBtn.style.backgroundColor = "#006DF0";
            activeBtn.style.color = "#fff";
            activeBtn.style.borderColor = "#006DF0";
        }
    }

    if (btnScaleDay) {
        btnScaleDay.addEventListener("click", function() {
            setScaleActive(btnScaleDay);
            
            // 📅 날짜 검색 필터 자동 연동: 오늘 ~ 오늘 + 15일
            const start = new Date();
            const end = new Date();
            end.setDate(end.getDate() + 15);
            if (inputStart && inputEnd) {
                inputStart.value = formatDate(start);
                inputEnd.value = formatDate(end);
            }
            
            gantt.config.scales = [
                { unit: "month", step: 1, format: "%Y년 %m월" }, 
                { 
                    unit: "day", 
                    step: 1, 
                    format: function(date) {
                        const days = ["일", "월", "화", "수", "목", "금", "토"];
                        const dayName = days[date.getDay()];
                        const dayNumber = gantt.date.date_to_str("%d")(date); 
                        return dayNumber + " (" + dayName + ")"; 
                    } 
                }
            ];
            gantt.config.scale_height = 50;
            filterGantt(); // 날짜 필터링 범위 동기화 및 렌더링 호출
        });
    }

    if (btnScaleWeek) {
        btnScaleWeek.addEventListener("click", function() {
            setScaleActive(btnScaleWeek);
            
            // 📅 날짜 검색 필터 자동 연동: 오늘 ~ 오늘 + 45일 (1.5개월)
            const start = new Date();
            const end = new Date();
            end.setDate(end.getDate() + 45);
            if (inputStart && inputEnd) {
                inputStart.value = formatDate(start);
                inputEnd.value = formatDate(end);
            }
            
            gantt.config.scales = [
                { unit: "month", step: 1, format: "%Y년 %m월" }, 
                { 
                    unit: "week", 
                    step: 1, 
                    format: function(date) {
                        const dateToStr = gantt.date.date_to_str("%W");
                        return dateToStr(date) + "주차";
                    } 
                }
            ];
            gantt.config.scale_height = 50;
            filterGantt(); // 날짜 필터링 범위 동기화 및 렌더링 호출
        });
    }

    if (btnScaleMonth) {
        btnScaleMonth.addEventListener("click", function() {
            setScaleActive(btnScaleMonth);
            
            // 📅 날짜 검색 필터 자동 연동: 오늘 ~ 오늘 + 3개월
            const start = new Date();
            const end = new Date();
            end.setMonth(end.getMonth() + 3);
            if (inputStart && inputEnd) {
                inputStart.value = formatDate(start);
                inputEnd.value = formatDate(end);
            }
            
            gantt.config.scales = [
                { unit: "year", step: 1, format: "%Y년" }, 
                { unit: "month", step: 1, format: "%m월" }
            ];
            gantt.config.scale_height = 50;
            filterGantt(); // 날짜 필터링 범위 동기화 및 렌더링 호출
        });
    }

    // 📝 [검색] 버튼을 누를 때만 필터링 기능이 동작하도록 리스너 바인딩
    const btnFilterSearch = document.getElementById('btnFilterSearch');
    const btnFilterReset = document.getElementById('btnFilterReset');

    if (btnFilterSearch) {
        btnFilterSearch.addEventListener('click', filterGantt);
    }

    // 📝 [초기화] 버튼 누를 시 모든 값을 공백화하되, 시작/종료일은 기본 [오늘 / 오늘+15일]로 세팅 원복
    if (btnFilterReset) {
        btnFilterReset.addEventListener('click', function() {
            const msSelect = document.getElementById('f_milestone');
            const stSelect = document.getElementById('f_status');
            const asSelect = document.getElementById('f_assignee');
            const inputTxt = document.getElementById('f_text');

            if (msSelect) msSelect.value = "all";
            if (stSelect) stSelect.value = "all";
            if (asSelect) asSelect.value = "all";
            if (inputTxt) inputTxt.value = "";

            // 날짜는 다시 오늘 / 오늘+15일 초기 기본 세팅으로 리셋
            if (inputStart && inputEnd) {
                inputStart.value = formatDate(today);
                inputEnd.value = formatDate(future);
            }
            filterGantt(); // 리셋 상태 렌더링 호출
        });
    }
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