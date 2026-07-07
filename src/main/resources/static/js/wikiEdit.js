const wikiId = document.getElementById('markdownInput').dataset.wikiId
const projectId = document.getElementById('markdownInput').dataset.projectId

function switchTab(tab) {
  const tabs = document.querySelectorAll('.wiki-tab')
  const textarea = document.getElementById('markdownInput')
  const previewEl = document.getElementById('wikiPreview')
  const toolbar = document.getElementById('wikiToolbar')

  if (tab === 'edit') {
    tabs[0].classList.add('active')
    tabs[1].classList.remove('active')
    textarea.style.display = 'block'
    toolbar.style.display = 'flex'
    previewEl.classList.remove('active')
  } else {
    tabs[0].classList.remove('active')
    tabs[1].classList.add('active')
    textarea.style.display = 'none'
    toolbar.style.display = 'none'
    previewEl.classList.add('active')

    const md = textarea.value
    let html = window.marked.parse(md)
    convertWikiLinks(html).then(convertedHtml => {
      previewEl.innerHTML = generateToc(convertedHtml) + convertedHtml
    })
  }
}

function generateToc(html) {
  const parser = new DOMParser()
  const doc = parser.parseFromString(html, 'text/html')
  const headings = doc.querySelectorAll('h2, h3, h4')
  if (headings.length === 0) return ''
  let toc = '<div class="wiki-toc"><div class="wiki-toc-title">목차</div><ol>'
  headings.forEach((h, i) => {
    const level = parseInt(h.tagName[1])
    const text = h.textContent
    const indent = (level - 2) * 16
    toc += `<li style="margin-left:${indent}px"><a href="#heading-${i}">${text}</a></li>`
  })
  toc += '</ol></div>'
  return toc
}

async function convertWikiLinks(html) {
  const pattern = /\[\[(.+?)\]\]/g
  const matches = [...html.matchAll(pattern)]
  if (matches.length === 0) return html

  for (const match of matches) {
    const title = match[1]
    try {
      const res = await fetch(`/project/${projectId}/wiki/find?title=${encodeURIComponent(title)}`)
      const page = await res.json()
      if (page && page.wikiId) {
        html = html.replace(
          match[0],
          `<a href="/project/${projectId}/wiki/${page.wikiId}"
              style="color:#3d6ef5; text-decoration:none; border-bottom:1px solid #3d6ef5;">${title}</a>`
        )
      } else {
        html = html.replace(
          match[0],
          `<a href="javascript:createWiki('${title}')"
              style="color:#e53e3e; text-decoration:none; border-bottom:1px solid #e53e3e;">${title}</a>`
        )
      }
    } catch(e) {
      html = html.replace(match[0], title)
    }
  }
  return html
}

function createWiki(title) {
  if (!confirm(`"${title}" 위키를 새로 만드시겠습니까?`)) return
  fetch(`/project/${projectId}/wiki/create`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title: title, projectId: projectId })
  })
  .then(res => res.json())
  .then(data => {
    location.href = `/project/${projectId}/wiki/${data.wikiId}/edit`
  })
  .catch(err => console.error('위키 생성 실패:', err))
}

function insertMd(before, after) {
  const ta = document.getElementById('markdownInput')
  const start = ta.selectionStart
  const end = ta.selectionEnd
  const selected = ta.value.substring(start, end)
  const newText = before + (selected || '텍스트') + after
  ta.value = ta.value.substring(0, start) + newText + ta.value.substring(end)
  ta.focus({ preventScroll: true })
  ta.dispatchEvent(new Event('input'))
}

function insertLine(prefix) {
  const ta = document.getElementById('markdownInput')
  const start = ta.selectionStart
  const lineStart = ta.value.lastIndexOf('\n', start - 1) + 1
  ta.value = ta.value.substring(0, lineStart) + prefix + ta.value.substring(lineStart)
  ta.focus({ preventScroll: true })
  ta.dispatchEvent(new Event('input'))
}

function insertCodeBlock() {
  const ta = document.getElementById('markdownInput')
  const scrollY = window.scrollY
  const start = ta.selectionStart
  const insert = '\n```\n코드 입력\n```\n'
  ta.value = ta.value.substring(0, start) + insert + ta.value.substring(start)
  ta.focus({ preventScroll: true })
  window.scrollTo(0, scrollY)
  ta.dispatchEvent(new Event('input'))
}

function insertTable() {
  const ta = document.getElementById('markdownInput')
  const scrollY = window.scrollY
  const start = ta.selectionStart
  const insert = '\n| 제목1 | 제목2 | 제목3 |\n|---|---|---|\n| 내용 | 내용 | 내용 |\n'
  ta.value = ta.value.substring(0, start) + insert + ta.value.substring(start)
  ta.focus({ preventScroll: true })
  window.scrollTo(0, scrollY)
  ta.dispatchEvent(new Event('input'))
}

function insertLink() {
  const ta = document.getElementById('markdownInput')
  const start = ta.selectionStart
  const end = ta.selectionEnd
  const selected = ta.value.substring(start, end)
  document.getElementById('linkText').value = selected || ''
  document.getElementById('linkUrl').value = ''
  $('#insertLinkModal').modal('show')
}

function confirmInsertLink() {
  const url = document.getElementById('linkUrl').value.trim()
  if (!url) return

  const text = document.getElementById('linkText').value.trim() || '링크텍스트'
  $('#insertLinkModal').modal('hide')

  const ta = document.getElementById('markdownInput')
  const start = ta.selectionStart
  const end = ta.selectionEnd
  const insert = `[${text}](${url})`
  ta.value = ta.value.substring(0, start) + insert + ta.value.substring(end)
  ta.focus({ preventScroll: true })
  ta.dispatchEvent(new Event('input'))

  document.getElementById('linkText').value = ''
  document.getElementById('linkUrl').value = ''
}
function insertWikiLink() {
  document.getElementById('wikiLinkTitle').value = ''
  document.getElementById('wikiLinkSelect').value = ''

  // 위키 목록 불러오기
  fetch(`/project/${projectId}/wiki/listJson`)
    .then(res => res.json())
    .then(list => {
      const select = document.getElementById('wikiLinkSelect')
      select.innerHTML = '<option value="">-- 위키 선택 --</option>'
      if (Array.isArray(list)) {
        list.forEach(w => {
          const option = document.createElement('option')
          option.value = w.title
          option.textContent = w.title
          select.appendChild(option)
        })
      }
    })
    .catch(err => console.error('위키 목록 로드 실패:', err))

  // 셀렉트 선택시 입력창에 자동 반영
  document.getElementById('wikiLinkSelect').onchange = function() {
    if (this.value) {
      document.getElementById('wikiLinkTitle').value = this.value
    }
  }

  $('#insertWikiLinkModal').modal('show')
}

function confirmInsertWikiLink() {
  const title = document.getElementById('wikiLinkTitle').value.trim()
  if (!title) return

  $('#insertWikiLinkModal').modal('hide')

  // 위키 존재 여부 확인 후 없으면 생성
  fetch(`/project/${projectId}/wiki/find?title=${encodeURIComponent(title)}`)
    .then(res => res.json())
    .then(page => {
      if (!page || !page.wikiId) {
        // 없으면 자동 생성
        return fetch(`/project/${projectId}/wiki/createJson`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ title: title, projectId: projectId })
        }).then(res => res.json())
      }
      return page
    })
    .then(() => {
      // 생성 여부 상관없이 [[제목]] 삽입
      const ta = document.getElementById('markdownInput')
      const start = ta.selectionStart
      const insert = `[[${title}]]`
      ta.value = ta.value.substring(0, start) + insert + ta.value.substring(start)
      ta.focus({ preventScroll: true })
      ta.setSelectionRange(start + insert.length, start + insert.length)
      ta.dispatchEvent(new Event('input'))
      document.getElementById('wikiLinkTitle').value = ''
      document.getElementById('wikiLinkSelect').value = ''
    })
    .catch(err => console.error('위키 링크 삽입 실패:', err))
}

function loadVersionList() {
  fetch(`/project/${projectId}/wiki/versions/${wikiId}`)
    .then(res => res.json())
    .then(list => {
      const container = document.getElementById('versionList')
      container.innerHTML = ''
      if (!Array.isArray(list) || list.length === 0) {
        container.innerHTML = '<p style="color:#aaa; font-size:13px;">저장된 버전이 없습니다.</p>'
        return
      }
      list.forEach(v => {
        const div = document.createElement('div')
        div.className = 'version-item'
        div.innerHTML = `
          <div style="font-size:13px; font-weight:500;">${v.title || '제목없음'}</div>
          <div class="version-no">${v.versionNo}</div>
          <div class="version-date">By.${v.userName}</div>
        `
        div.onclick = () => restoreVersion(v.versionId)
        container.appendChild(div)
      })
    })
    .catch(err => console.error('버전 목록 로드 실패:', err))
}


let pendingRestoreVersionId = null

function restoreVersion(versionId) {
  pendingRestoreVersionId = versionId
  $('#restoreModal').modal('show')
}

function confirmRestore() {
  if (!pendingRestoreVersionId) return
  $('#restoreModal').modal('hide')

  fetch(`/project/${projectId}/wiki/version/${pendingRestoreVersionId}`)
    .then(res => res.json())
    .then(data => {
      if (data && data.content) {
        const content = JSON.parse(data.content)
        const text = typeof content === 'string' ? content : (content.markdown || '')
        const ta = document.getElementById('markdownInput')
        ta.value = text
        ta.dispatchEvent(new Event('input'))
      }
      pendingRestoreVersionId = null
    })
    .catch(err => console.error('복원 실패:', err))
}

// 복원 버튼에 이벤트 연결
document.getElementById('confirmRestoreBtn').addEventListener('click', confirmRestore)


function saveAndView() {
  const textarea = document.getElementById('markdownInput')
  const title = document.getElementById('wikiTitle')?.value || '제목없음'
  const content = JSON.stringify({ markdown: textarea.value })

  fetch(`/project/${projectId}/wiki/save`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ wikiId, title, content })
  })
  .then(res => res.text())
  .then(() => {
    location.href = `/project/${projectId}/wiki/${wikiId}`
  })
  .catch(err => {
    console.error('저장 실패:', err)
    alert('저장에 실패했습니다.')
  })
}