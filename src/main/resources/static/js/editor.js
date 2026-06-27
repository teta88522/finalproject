import * as Y from 'yjs'
import { WebsocketProvider } from 'y-websocket'
import { Editor } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'
import { Collaboration } from '@tiptap/extension-collaboration'
import { CollaborationCaret } from '@tiptap/extension-collaboration-caret'

const editorEl = document.getElementById('editor')
console.log('에디터 요소:', editorEl)

if (editorEl) {

  const wikiId = editorEl.dataset.wikiId
  console.log('wikiId:', wikiId)

  const ydoc = new Y.Doc()

  const provider = new WebsocketProvider(
    'ws://localhost:8080/ws/wiki',
    wikiId,
    ydoc
  )

  provider.on('status', event => {
    console.log('WebSocket 상태:', event.status)
  })

  // 랜덤 색상 생성
  const colors = ['#F98181', '#FBBC88', '#FAF594', '#70CFF8', '#94FADB', '#B9F18D']
  const randomColor = colors[Math.floor(Math.random() * colors.length)]
  const randomName = '사용자' + Math.floor(Math.random() * 100)  // 나중에 세션으로 교체

  const editor = new Editor({
    element: editorEl,
    extensions: [
      StarterKit.configure({ history: false }),
      Collaboration.configure({ document: ydoc }),
      CollaborationCaret.configure({
        provider: provider,
        user: {
          name: randomName,
          color: randomColor,
        },
      }),
    ],
  })
  window.editor = editor
  console.log('에디터 초기화 완료:', editor)

  // 페이지 로드시 최신 내용 불러오기
  fetch(`/wiki/load/${wikiId}`)
    .then(res => res.json())
    .then(data => {
      if (data && data.content) {
        editor.commands.setContent(JSON.parse(data.content))
        console.log('내용 불러오기 완료')
      }
    })
    .catch(() => console.log('저장된 내용 없음'))

  // 5초마다 자동저장
  setInterval(() => {
    console.log('자동저장 시도중...')
    const content = JSON.stringify(editor.getJSON())
    const title = document.getElementById('wikiTitle')?.value || '제목없음'

    fetch('/wiki/save', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        wikiId: wikiId,
        title: title,
        content: content
      })
    })
    .then(res => res.text())
    .then(text => console.log('자동저장 완료:', text))
    .catch(err => console.error('저장 실패:', err))
  }, 60000)

}