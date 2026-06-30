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
    'ws://localhost:8080/ws/wiki',
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

}