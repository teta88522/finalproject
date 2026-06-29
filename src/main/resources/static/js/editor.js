import * as Y from 'yjs'
import { WebsocketProvider } from 'y-websocket'
import { Editor } from '@tiptap/core'
import { StarterKit } from '@tiptap/starter-kit'
import { Collaboration } from '@tiptap/extension-collaboration'
import CollaborationCaret from '@tiptap/extension-collaboration-caret'
import { Typography } from '@tiptap/extension-typography'
import { Placeholder } from '@tiptap/extension-placeholder'
import { Heading } from '@tiptap/extension-heading'
import { Blockquote } from '@tiptap/extension-blockquote'
import { CodeBlock } from '@tiptap/extension-code-block'
import { HorizontalRule } from '@tiptap/extension-horizontal-rule'
import { BulletList } from '@tiptap/extension-bullet-list'
import { OrderedList } from '@tiptap/extension-ordered-list'
import { Table } from '@tiptap/extension-table'
import { TableRow } from '@tiptap/extension-table-row'
import { TableCell } from '@tiptap/extension-table-cell'
import { TableHeader } from '@tiptap/extension-table-header'

const editorEl = document.getElementById('editor')

if (editorEl) {

  const wikiId = editorEl.dataset.wikiId
  const ydoc = new Y.Doc()

  const provider = new WebsocketProvider(
    'ws://localhost:8080/ws/wiki',
    wikiId,
    ydoc
  )

  provider.on('status', event => {
    console.log('WebSocket 상태:', event.status)
  })

  const colors = ['#F98181', '#FBBC88', '#FAF594', '#70CFF8', '#94FADB', '#B9F18D']
  const randomColor = colors[Math.floor(Math.random() * colors.length)]
  const randomName = '사용자' + Math.floor(Math.random() * 100)

  const editor = new Editor({
    element: editorEl,
	extensions: [
	  StarterKit.configure({
	    history: false,
	  }),
	  Collaboration.configure({ document: ydoc }),
	  CollaborationCaret.configure({
	    provider: provider,
	    user: { name: randomName, color: randomColor },
	  }),
	  Placeholder.configure({
	    placeholder: '내용을 입력하세요. "/" 를 입력하면 명령어를 사용할 수 있습니다.',
	  }),
	  Table,
	  TableRow,
	  TableCell,
	  TableHeader,
	],
  })

  window.editor = editor

  // 슬래시 명령어 처리
  editor.on('update', ({ editor }) => {
    const { state } = editor
    const { selection } = state
    const { $anchor } = selection
    const text = $anchor.nodeBefore?.text || ''

    if (text.endsWith('/')) {
      showSlashMenu(editor)
    } else {
      hideSlashMenu()
    }
  })

  // 페이지 로드시 최신 내용 불러오기
//  fetch(`/wiki/load/${wikiId}`)
//    .then(res => res.json())
//    .then(data => {
//      if (data && data.content) {
//        editor.commands.setContent(JSON.parse(data.content))
//      }
//    })
//    .catch(() => console.log('저장된 내용 없음'))

  // 60초마다 자동저장
  setInterval(() => {
    const content = JSON.stringify(editor.getJSON())
    const title = document.getElementById('wikiTitle')?.value || '제목없음'

    fetch('/wiki/save', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ wikiId, title, content })
    })
    .then(() => console.log('자동저장 완료'))
    .catch(err => console.error('저장 실패:', err))
  }, 60000)

}

// 슬래시 명령어 메뉴
function showSlashMenu(editor) {
  let menu = document.getElementById('slashMenu')
  if (!menu) {
    menu = document.createElement('div')
    menu.id = 'slashMenu'
    menu.style.cssText = `
      position: fixed;
      background: #fff;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      box-shadow: 0 4px 16px rgba(0,0,0,0.12);
      padding: 8px;
      z-index: 9999;
      min-width: 200px;
    `
    document.body.appendChild(menu)
  }

  const commands = [
    { label: '📝 제목 1', action: () => { editor.chain().focus().deleteRange({ from: editor.state.selection.$anchor.pos - 1, to: editor.state.selection.$anchor.pos }).setHeading({ level: 1 }).run() }},
    { label: '📝 제목 2', action: () => { editor.chain().focus().deleteRange({ from: editor.state.selection.$anchor.pos - 1, to: editor.state.selection.$anchor.pos }).setHeading({ level: 2 }).run() }},
    { label: '📝 제목 3', action: () => { editor.chain().focus().deleteRange({ from: editor.state.selection.$anchor.pos - 1, to: editor.state.selection.$anchor.pos }).setHeading({ level: 3 }).run() }},
    { label: '💬 인용구', action: () => { editor.chain().focus().deleteRange({ from: editor.state.selection.$anchor.pos - 1, to: editor.state.selection.$anchor.pos }).setBlockquote().run() }},
    { label: '💻 코드블록', action: () => { editor.chain().focus().deleteRange({ from: editor.state.selection.$anchor.pos - 1, to: editor.state.selection.$anchor.pos }).setCodeBlock().run() }},
    { label: '• 목록', action: () => { editor.chain().focus().deleteRange({ from: editor.state.selection.$anchor.pos - 1, to: editor.state.selection.$anchor.pos }).toggleBulletList().run() }},
    { label: '1. 번호 목록', action: () => { editor.chain().focus().deleteRange({ from: editor.state.selection.$anchor.pos - 1, to: editor.state.selection.$anchor.pos }).toggleOrderedList().run() }},
    { label: '📊 표', action: () => { editor.chain().focus().deleteRange({ from: editor.state.selection.$anchor.pos - 1, to: editor.state.selection.$anchor.pos }).insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run() }},
    { label: '➖ 구분선', action: () => { editor.chain().focus().deleteRange({ from: editor.state.selection.$anchor.pos - 1, to: editor.state.selection.$anchor.pos }).setHorizontalRule().run() }},
  ]

  menu.innerHTML = ''
  commands.forEach(cmd => {
    const item = document.createElement('div')
    item.style.cssText = 'padding:8px 12px; cursor:pointer; border-radius:6px; font-size:14px;'
    item.textContent = cmd.label
    item.onmouseenter = () => item.style.background = '#f5f5f5'
    item.onmouseleave = () => item.style.background = ''
    item.onclick = () => { cmd.action(); hideSlashMenu() }
    menu.appendChild(item)
  })

  // 커서 위치에 메뉴 표시
  const { from } = editor.state.selection
  const coords = editor.view.coordsAtPos(from)
  menu.style.left = coords.left + 'px'
  menu.style.top = (coords.bottom + 8) + 'px'
  menu.style.display = 'block'

  // 외부 클릭시 닫기
  setTimeout(() => {
    document.addEventListener('click', hideSlashMenu, { once: true })
  }, 100)
}

function hideSlashMenu() {
  const menu = document.getElementById('slashMenu')
  if (menu) menu.style.display = 'none'
}