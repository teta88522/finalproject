import { Editor } from "@tiptap/core";
import StarterKit from "@tiptap/starter-kit";

new Editor({
  element: document.querySelector("#editor"),
  extensions: [StarterKit],
  content: "<p>Hello Wiki!</p>"
});

const ws = new WebSocket("ws://localhost:8080/ws/wiki?wikiId=123");

ws.onopen = () => {
    console.log("WebSocket Connected");
};

ws.onmessage = (event) => {
    console.log("받음:", event.data);
};

ws.onclose = () => {
    console.log("WebSocket Closed");
};