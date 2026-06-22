document.addEventListener('DOMContentLoaded', function() {
    
    // 요소 선택
    const form = document.getElementById('milestoneForm');
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    const dateError = document.getElementById('dateError');
    const btnUpdate = document.getElementById('btnUpdate');
    const milestoneId = document.getElementById('milestoneId').value;
    
    // === 1. 초기 데이터 로드 (기존 연결된 일감 불러오기) ===
    loadConnectedIssues(milestoneId);

    // === 2. 이벤트 리스너 등록 ===
    
    // 날짜 유효성 검사
    startDateInput.addEventListener('change', validateDates);
    endDateInput.addEventListener('change', validateDates);
    
    // 검색 버튼
    document.getElementById('btnSearchIssue').addEventListener('click', searchIssues);
    
    // 전체 선택 체크박스
    const checkAll = document.getElementById('checkAllIssues');
    if(checkAll) {
        checkAll.addEventListener('change', function() {
            const checkboxes = document.querySelectorAll('.issue-checkbox');
            checkboxes.forEach(cb => cb.checked = checkAll.checked);
        });
    }

    // 폼 제출 (저장)
    btnUpdate.addEventListener('click', function(e) {
        e.preventDefault();
        
        if(validateForm()) {
            // 선택된 일감 ID 수집
            const selectedCheckboxes = document.querySelectorAll('.issue-checkbox:checked');
            const selectedIds = Array.from(selectedCheckboxes).map(cb => cb.value);
            
            // hidden 필드에 값 설정
            document.getElementById('selectedIssueIds').value = selectedIds.join(',');
            
            // 수정이 필요한 부분 6: 비동기(AJAX)로 전송할지, Form 일반 Submit으로 처리할지에 따라 로직 변경
            // 현재는 일반 폼 Submit
            form.submit();
        }
    });

    // === 3. 함수 정의 ===
    
    function validateDates() {
        const start = startDateInput.value;
        const end = endDateInput.value;
        
        if(start && end) {
            if(start > end) {
                dateError.style.display = 'block';
                return false;
            } else {
                dateError.style.display = 'none';
                return true;
            }
        }
        return true;
    }

    function validateForm() {
        // 필수 값 체크
        let isValid = true;
        const requiredInputs = document.querySelectorAll('.required-input');
        
        requiredInputs.forEach(input => {
            if(!input.value.trim()) {
                input.style.borderColor = '#dc3545';
                isValid = false;
            } else {
                input.style.borderColor = '#ccc';
            }
        });

        if(!isValid) {
            alert("필수 항목을 모두 입력해주세요.");
            return false;
        }

        if(!validateDates()) {
            alert("시작일은 목표일자보다 늦을 수 없습니다.");
            return false;
        }

        return true;
    }
    
    // 수정이 필요한 부분 7: 기존 연결된 일감을 가져오는 실제 API URL로 수정
    function loadConnectedIssues(id) {
        // 예시: fetch(`/api/milestones/${id}/issues`)
        console.log("Loading issues for milestone: " + id);
        
        // Mock 데이터 (실제 프로젝트에서는 API 응답 데이터로 대체)
        const mockIssues = [
            { issueId: 'ISSUE-1', title: '로그인 UI 수정', status: '진행 중' },
            { issueId: 'ISSUE-2', title: 'DB 마이그레이션', status: '완료' }
        ];
        
        renderIssueList(mockIssues, true);
    }
    
    // 수정이 필요한 부분 8: 일감을 검색하는 실제 API URL로 수정
    function searchIssues() {
        const keyword = document.getElementById('issueSearchInput').value;
        console.log("Searching issues with keyword: " + keyword);
        
        // Mock 데이터 (검색 결과)
        const searchResults = [
            { issueId: 'ISSUE-3', title: '메인 페이지 배너 수정', status: '진행 예정' }
        ];
        
        // 기존 목록에 추가할지 덮어쓸지 기획에 따라 처리 (여기서는 추가 렌더링 예시)
        renderIssueList(searchResults, false);
    }

    function renderIssueList(issues, isChecked) {
        const tbody = document.getElementById('issueListBody');
        
        issues.forEach(issue => {
            // 중복 방지 로직 (이미 리스트에 있는지 확인)
            if(document.querySelector(`input.issue-checkbox[value="${issue.issueId}"]`)) {
                return;
            }
            
            const tr = document.createElement('tr');
            
            const checkedAttr = isChecked ? 'checked' : '';
            
            tr.innerHTML = `
                <td style="text-align: center;">
                    <input type="checkbox" class="issue-checkbox" value="${issue.issueId}" ${checkedAttr}>
                </td>
                <td>${issue.title}</td>
                <td><span class="status-badge">${issue.status}</span></td>
            `;
            
            tbody.appendChild(tr);
        });
    }
});