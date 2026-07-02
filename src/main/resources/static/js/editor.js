import * as Y from 'yjs'
import { WebsocketProvider } from 'y-websocket'
import { marked } from 'marked'

window.marked = marked
marked.setOptions({ breaks: true, gfm: true })

const textarea = document.getElementById('markdownInput')

if (textarea) {

  const wikiId = textarea.dataset.wikiId
  const projectId = textarea.dataset.projectId
  const ydoc = new Y.Doc()

  const provider = new WebsocketProvider(
    'ws://localhost/ws/wiki',
    wikiId,
    ydoc
  )

  provider.on('status', event => {
    console.log('WebSocket 상태:', event.status)
  })

  const ytext = ydoc.getText('markdown')

  // Yjs 변경 → textarea 반영
  ytext.observe(() => {
    const newText = ytext.toString()
    if (textarea.value !== newText) {
      textarea.value = newText
    }
  })

  // textarea 입력 → Yjs 반영
  textarea.addEventListener('input', () => {
    const newText = textarea.value
    const oldText = ytext.toString()
    if (newText !== oldText) {
      ydoc.transact(() => {
        ytext.delete(0, ytext.length)
        ytext.insert(0, newText)
      })
    }
  })

  // 페이지 로드시 내용 불러오기
  fetch(`/project/${projectId}/wiki/load/${wikiId}`)
    .then(res => res.json())
    .then(data => {
      if (data && data.content) {
        const content = JSON.parse(data.content)
        const text = typeof content === 'string' ? content : (content.markdown || '')
        ydoc.transact(() => {
          ytext.delete(0, ytext.length)
          ytext.insert(0, text)
        })
        textarea.value = text
      }
    })
    .catch(() => console.log('저장된 내용 없음'))

  // 60초마다 자동저장
  setInterval(() => {
    const content = JSON.stringify({ markdown: textarea.value })
    const title = document.getElementById('wikiTitle')?.value || '제목없음'
    fetch(`/project/${projectId}/wiki/save`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ wikiId, title, content })
    })
    .then(() => console.log('자동저장 완료'))
    .catch(err => console.error('저장 실패:', err))
  }, 60000)

  // ===== 이미지 업로드 =====
  const imageInput = document.getElementById('wikiImageInput')

  window.triggerImageUpload = function () {
    imageInput.click()
  }

  if (imageInput) {
    imageInput.addEventListener('change', (e) => {
      const file = e.target.files[0]
      if (file) uploadWikiImage(file)
      e.target.value = ''
    })
  }

  function replaceInYjs(oldStr, newStr) {
    const text = ytext.toString()
    const idx = text.indexOf(oldStr)
    if (idx === -1) return
    ydoc.transact(() => {
      ytext.delete(idx, oldStr.length)
      ytext.insert(idx, newStr)
    })
  }

  function uploadWikiImage(file) {
    const placeholder = `![업로드중...](uploading)`
    const cursorPos = textarea.selectionStart

    textarea.value =
      textarea.value.substring(0, cursorPos) +
      placeholder +
      textarea.value.substring(cursorPos)
    textarea.dispatchEvent(new Event('input')) // → ytext에도 반영됨

    const formData = new FormData()
    formData.append('file', file)

    fetch(`/project/${projectId}/wiki/${wikiId}/image/upload`, {
      method: 'POST',
      body: formData
    })
      .then(res => res.json())
      .then(data => {
        if (data.error) throw new Error(data.error)
        const markdown = `![${file.name}](${data.url})`
        replaceInYjs(placeholder, markdown)
        textarea.value = ytext.toString()
      })
      .catch(err => {
        console.error('이미지 업로드 실패:', err)
        replaceInYjs(placeholder, '')
        textarea.value = ytext.toString()
        alert('이미지 업로드에 실패했습니다.')
      })
  }

  // 드래그앤드롭
  textarea.addEventListener('dragover', e => e.preventDefault())
  textarea.addEventListener('drop', e => {
    e.preventDefault()
    const file = e.dataTransfer.files[0]
    if (file && file.type.startsWith('image/')) {
      uploadWikiImage(file)
    }
  })

  // 클립보드 붙여넣기
  textarea.addEventListener('paste', e => {
    const items = e.clipboardData?.items
    if (!items) return
    for (const item of items) {
      if (item.type.startsWith('image/')) {
        e.preventDefault()
        const file = item.getAsFile()
        uploadWikiImage(file)
        break
      }
    }
  })

}