document.addEventListener("DOMContentLoaded", () => {

    const fileInput = document.getElementById("fileInput");
    const fileList = document.getElementById("fileList");
    const fileCount = document.getElementById("fileCount");
	
	// ✅ 이 페이지에 파일 요소가 없으면 그냥 종료
	if (!fileInput || !fileList || !fileCount) return;

	
    let selectedFiles = [];

    fileInput.addEventListener("change", (e) => {
        console.log("파일 감지:", e.target.files);
        const newFiles = Array.from(e.target.files);
        selectedFiles = selectedFiles.concat(newFiles);
        renderFiles();
        fileInput.value = "";
    });

    function renderFiles() {
        fileList.innerHTML = "";
        fileCount.innerText = `파일 ${selectedFiles.length}개`;

        selectedFiles.forEach((file, index) => {
            const sizeKB = (file.size / 1024).toFixed(1);
            const ext = file.name.split('.').pop().toLowerCase();

            const li = document.createElement("li");
            li.style.cssText = "display:flex; justify-content:space-between; padding:6px 0; border-bottom:1px solid #eee;";
            li.innerHTML = `
                <span>📎 ${file.name} <small style="color:#888;">(.${ext}, ${sizeKB} KB)</small></span>
                <button type="button" data-index="${index}">삭제</button>
            `;
            fileList.appendChild(li);
        });
    }

    fileList.addEventListener("click", (e) => {
        if (e.target.tagName === "BUTTON") {
            const index = parseInt(e.target.getAttribute("data-index"));
            selectedFiles.splice(index, 1);
            renderFiles();
        }
    });

    document.getElementById("documentEditForm").addEventListener("submit", () => {
        const dt = new DataTransfer();
        selectedFiles.forEach(file => dt.items.add(file));
        fileInput.files = dt.files;
		
		console.log("selectedFiles =", selectedFiles);
		    console.log("fileInput.files =", fileInput.files);
		    console.log("fileInput.files.length =", fileInput.files.length);
    });

});