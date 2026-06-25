(function () {
    document.querySelectorAll('.common-file-upload').forEach(function (box) {
        var fileInput = document.getElementById(box.dataset.fileInputId);
        var fileList = document.getElementById(box.dataset.fileListId);
        var fileCount = document.getElementById(box.dataset.fileCountId);
        var form = document.getElementById(box.dataset.fileFormId);
        var selectedFiles = [];

        if (!fileInput || !fileList || !fileCount || !form) {
            return;
        }

        fileInput.addEventListener('change', function (event) {
            selectedFiles = selectedFiles.concat(Array.from(event.target.files));
            renderFiles();
            fileInput.value = '';
        });

        fileList.addEventListener('click', function (event) {
            if (event.target.tagName !== 'BUTTON') {
                return;
            }

            var index = Number(event.target.getAttribute('data-index'));
            selectedFiles.splice(index, 1);
            renderFiles();
        });

        form.addEventListener('submit', function () {
            var dataTransfer = new DataTransfer();

            selectedFiles.forEach(function (file) {
                dataTransfer.items.add(file);
            });

            fileInput.files = dataTransfer.files;
        });

        function renderFiles() {
            fileList.innerHTML = '';
            fileCount.innerText = '파일 ' + selectedFiles.length + '개';

            selectedFiles.forEach(function (file, index) {
                var sizeKB = (file.size / 1024).toFixed(1);
                var item = document.createElement('li');

                item.className = 'common-file-item issues-file-item';
                item.innerHTML =
                    '<span class="common-file-name issues-file-name">' + escapeHtml(file.name) +
                    ' <small>(' + sizeKB + ' KB)</small></span>' +
                    '<button type="button" data-index="' + index + '">삭제</button>';

                fileList.appendChild(item);
            });
        }

        function escapeHtml(value) {
            return value
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }
    });
})();
