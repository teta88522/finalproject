function deletePost(projectId, boardId, postId) {
    if(confirm("정말로 삭제하시겠습니까?")) {
        window.location.href = `/project/${projectId}/notice/PostDelete?boardId=${boardId}&postId=${postId}`;
    }
}