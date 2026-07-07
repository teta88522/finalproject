const projectId = document.getElementById('hiddenProjectId').value
const wikiId = document.getElementById('hiddenWikiId').value

// 링크 설정
document.getElementById('btnBack').href = `/project/${projectId}/wiki/list`
document.getElementById('btnEdit').href = `/project/${projectId}/wiki/${wikiId}/edit`
document.getElementById('editLink').href = `/project/${projectId}/wiki/${wikiId}/edit`

let currentContent = ''
let selectedVersionId = null

marked.setOptions({ breaks: true, gfm: true })

function renderMarkdown(raw) {
  try {
    const parsed = JSON.parse(raw)
    return typeof parsed === 'string' ? parsed : (parsed.markdown || '')
  } catch(e) {
    return ''
  }
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

async function displayContent(markdown, isOldVersion) {
  let html = marked.parse(markdown)
  html = await convertWikiLinks(html)

  const parser = new DOMParser()
  const doc = parser.parseFromString(html, 'text/html')
  const headings = doc.querySelectorAll('h2, h3, h4')

  let tocHtml = ''
  if (headings.length > 0) {
    tocHtml = '<div class="wiki-toc"><div class="wiki-toc-title">목차</div><ol>'
    headings.forEach((h, i) => {
      const level = parseInt(h.tagName[1])
      const text = h.textContent
      const indent = (level - 2) * 16
      tocHtml += `<li style="margin-left:${indent}px"><a href="#heading-${i}">${text}</a></li>`
    })
    tocHtml += '</ol></div>'
  }

  let idx = 0
  const finalHtml = html.replace(/<h([234])>/g, (match, level) => {
    return `<h${level} id="heading-${idx++}">`
  })

  document.getElementById('wikiContent').innerHTML = tocHtml + finalHtml
  document.getElementById('versionNotice').style.display = isOldVersion ? 'block' : 'none'
}

// 초기 렌더링
fetch(`/project/${projectId}/wiki/load/${wikiId}`)
  .then(res => res.json())
  .then(data => {
    if (data && data.content) {
      currentContent = data.content
      const md = renderMarkdown(data.content)
      if (md) {
        displayContent(md, false)
      } else {
        document.getElementById('wikiContent').innerHTML = '<p style="color:#aaa;">내용이 없습니다.</p>'
      }
    } else {
      document.getElementById('wikiContent').innerHTML = '<p style="color:#aaa;">내용이 없습니다.</p>'
    }
  })
  .catch(() => {
    document.getElementById('wikiContent').innerHTML = '<p style="color:#aaa;">내용이 없습니다.</p>'
  })

function toggleHistory() {
  const panel = document.getElementById('historyPanel')
  panel.classList.toggle('open')
  if (panel.classList.contains('open')) loadHistory()
}

function loadHistory() {
  fetch(`/project/${projectId}/wiki/versions/${wikiId}`)
    .then(res => res.json())
    .then(list => {
      const container = document.getElementById('historyList')
      if (!Array.isArray(list) || list.length === 0) {
        container.innerHTML = '<p style="padding:16px; color:#aaa; font-size:13px;">버전 없음</p>'
        return
      }
      container.innerHTML = ''
      list.forEach((v, i) => {
        const div = document.createElement('div')
        div.className = 'history-item' + (i === 0 ? ' active' : '')
        div.innerHTML = `
          <div style="font-size:13px; font-weight:500;">${v.title || '제목없음'}</div>
          <div class="history-item-version">${v.versionNo}</div>
          <div class="history-item-date">${v.createdAt}</div>
          <div class="history-item-author">By.${v.userName || ''}</div>
        `
        div.onclick = () => previewVersion(v.versionId, div)
        container.appendChild(div)
      })
    })
    .catch(err => console.error('히스토리 로드 실패:', err))
}

function previewVersion(versionId, el) {
  document.querySelectorAll('.history-item').forEach(e => e.classList.remove('active'))
  el.classList.add('active')
  selectedVersionId = versionId
  document.getElementById('backToCurrentBtn').style.display = 'block'

  fetch(`/project/${projectId}/wiki/version/${versionId}`)
    .then(res => res.json())
    .then(data => {
      if (data && data.content) {
        const md = renderMarkdown(data.content)
        displayContent(md, true)
      }
    })
    .catch(err => console.error('버전 로드 실패:', err))
}

function backToCurrent() {
  selectedVersionId = null
  document.getElementById('backToCurrentBtn').style.display = 'none'
  document.querySelectorAll('.history-item').forEach((e, i) => {
    if (i === 0) e.classList.add('active')
    else e.classList.remove('active')
  })
  const md = renderMarkdown(currentContent)
  displayContent(md, false)
}