document.addEventListener("DOMContentLoaded",function(){

		//필요한 html 요소들을 가져옵니다.
		const isseuSearchInput = document.getElementById("issueSearchInput");
		const issueListBody = document.getElementById("issueListBody");	
		const versionId = document.getElementById("versionId");
		const selectedIssueIdsInput = document.getElementById("selectedIssueIds");
		const form = document.getElementById("milestoneForm");

		const startDateInput = document.getElementById("startDate");
		const endDateInput = document.getElementById("endDate");
		const dateErrorDiv = document.getElementById("dateError");

		// 사용자가 체크한 일감의 ID들을 담아둘 장바구니
		// 현재 고정된 버전 ID를 담아둘 변수
		let selectedIssueIds = new Set();
		let lockedVersionId = null;

		//일감 검색 기능
		isseuSearchInput.addEventListener("keyup", function() {
			const keyword = isseuSearchInput.value;
			
			//백엔드 API 호출을 통해 일감 검색 결과를 가져옵니다.
			//만약 고정된 버전이 있다면 파라미터에 같이 붙여서 보냅니다
			let apiUrl = `/milestones/api/issues/search?keyword=${keyword}`;
			if (lockedVersionId !== null) {
				apiUrl += `&versionId=${lockedVersionId}`;
			}

			//fetch api를 사용해 서버에 요청을 보냄
			fetch(apiUrl)
				.then(response => response.json())
				.then(data => {
					issueListBody.innerHTML = '';  // 기존에 표시되어 있던 목록을 지우고 깨끗하게 만듭니다.

					data.forEach(issue =>{ // 서버에서 받아온 일감 리스트(data)를 하나씩 꺼내서 HTML(tr 태그)로 만들어 화면에 붙입니다.
						const tr = document.createElement("tr");
						const isChecked = selectedIssueIds.has(issue.issueId) ? 'checked' : ''; // 체크박스 생성: 만약 이미 장바구니에 있는 일감이라면 체크된 상태로 만듭니다.

						tr.innerHTML = `
							<td>
								<input type="checkbox" class="issue-checkbox"
								value="${issue.issueId}"
								data-version-id="${issue.versionId}" ${isChecked}>
							</td>
							<td>${issue.issueId} ${issue.title}</td>`;
							
							issueListBody.appendChild(tr);
					});

					attachCheckboxEvents();
            })
					.catch(error => console.error("일감 검색중 에러 :", error));
		});

		//일감 체크박스 클릭 이벤트
		function attachCheckboxEvent(){
			const checkboxes = document.querySelectorAll(".issue-checkbox"); // 모든 체크박스들을 가져옵니다.

			checkboxes.forEach(checkbox => {
				checkbox.addEventListener('change', function(){ 
					const clickedIssueId = this.value; //클릭된 일감
					const clickedVersionId = this.getAttribute("data-version-id"); //클릭된 일감의 버전 ID

					if(this.checked){ //만약 체크를 했다면
						selectedIssuesSet.add(clickedIssueId); //장바구니에 일감 ID 추가

						if(lockedVersionId == null){
							lockedVersionId = clickedVersionId; //버전 고정
							versionId.value = lockedVersionId; //폼의 숨겨진 input에 값을 넣어 서버로 보낼 준비

							issueSearchInput.dispatchEvent(new Event('keyup')); // 버전이 고정되었으니, 검색창을 한 번 초기화시켜서 해당 버전의 일감만 다시 불러옵니다.

						}
					}else{
						selectedIssuesSet.delete(clieckedIssueId); //체크를 해제했을 때 장바구니에서 뻅니다

						if(selectedIssuesSet.size == 0){ //만약 장바구니가 비었다면?
						    lockedVersionId = null; //버전 고정 해제
							versionId.value = ''; //폼의 숨겨진 input도 초기화
							issueSearchInput.dispatchEvent(new Event('keyup')); //버전 고정이 해제되었으니, 검색창을 초기화시켜서 모든 버전의 일감을 다시 불러옵니다.
						}
					}
					selectedIssueIdsInput.value = Array.from(selectedIssueIds).join(","); //장바구니에 담긴 일감 ID들을 폼의 숨겨진 input에 콤마로 구분된 문자열로 만들어서 넣어줍니다.
					});
				});
			}
		// 저장 버튼 클릭시 검증
		form.addEventListener("submit", function(event){
			let isvalid = true; //통과 여부를 체크하는 변수

			//필수 입력값 검증
			const requiredInputs = document.querySelectorAll(".required-input");//필수 입력값들을 가져옵니다.
			requiredInputs.forEach(input => {
				if(!input.value.trim() == ''){ //만약 값이 비어있다면?
					input.classList.add("error-border"); //붉은 테두리 표시
					isValid = false; //통과 여부를 false로

				}else{
					input.classList.remove("error-border"); //값이 있다면 붉은 테두리 제거
				}
			});
		//2단계 : 날짜 비교 로직
		if(startDateInput.value && endDateInput.value){ //둘 다 값이 있다면
			const startDate = new Date(startDateInput.value);
			const endDate = new Date(endDateInput.value);

			if(startDate > endDate){ //만약 시작일이 목표일자보다 늦다면?
				startDateInput.classList.add('error-border');
                endDateInput.classList.add('error-border');
				dateErrorDiv.style.display = "block"; //에러 메시지 표시
				isValid = false; //통과 여부를 false로
			}else{
				startDateInput.classList.remove('error-border');
                endDateInput.classList.remove('error-border');
				dateErrorDiv.style.display = "none"; //에러 메시지 숨김
			}
		}
		if(!isValid){ //만약 통과하지 못했다면?
			event.preventDefault(); //폼 제출을 막습니다.
		}
		});

		// 날짜를 다시 선택하거나 입력창을 수정하면 붉은 선을 지워주는 센스있는 기능
    	document.querySelectorAll('.required-input').forEach(input => {
        input.addEventListener('change', function() {
            this.classList.remove('error-border');
        });
    });
});