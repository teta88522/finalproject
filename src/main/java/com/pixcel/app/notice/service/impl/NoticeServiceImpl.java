package com.pixcel.app.notice.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.notice.entity.NoticeEntity;
import com.pixcel.app.notice.repository.NoticeRepository;
import com.pixcel.app.notice.repository.PostRepository; // 📝 PostRepository 임포트 추가
import com.pixcel.app.notice.service.NoticeRequestDTO;
import com.pixcel.app.notice.service.NoticeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {
	
	private final NoticeRepository noticeRepository;
	private final PostRepository postRepository; // 📝 PostRepository 필드 주입 추가
	
	@Override
	@Transactional
	public String createNoticeBoard(NoticeRequestDTO noticeRequestDto) {
		
		String currentYymm = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
		String prefix = "BOARD_" + currentYymm + "_";
		
		String nextBoardId = noticeRepository.findLatestBoardIdByPrefix(prefix)
		.map(notice -> {
			String lastSeqStr = notice.getBoardId().substring(prefix.length());
			int nextSeq = Integer.parseInt(lastSeqStr) + 1;
			return prefix + String.format("%04d", nextSeq);
		})
		.orElse(prefix + "0001");
		
		NoticeEntity notice = NoticeEntity.builder()
	            .boardId(nextBoardId)
	            .projectId(noticeRequestDto.getProjectId())
	            .boardName(noticeRequestDto.getBoardName())
	            .createdBy(noticeRequestDto.getCreatedBy())
	            .description(noticeRequestDto.getDescription())
	            .build();
		
		noticeRepository.save(notice);

	    return notice.getBoardId();
	}
	
	@Override
	public List<NoticeRequestDTO> getBoardList(String projectId){
		List<NoticeEntity>noticeList = noticeRepository.findByProjectId(projectId);
		List<NoticeRequestDTO> dtoList = new ArrayList<>();
		
		for (NoticeEntity notice : noticeList) {
			long postCount = postRepository.countByBoardId(notice.getBoardId());
			NoticeRequestDTO dto = NoticeRequestDTO.builder()
                    .boardId(notice.getBoardId())        // DB 값 -> DTO로 복사
                    .boardName(notice.getBoardName())
                    .description(notice.getDescription())
                    .createdBy(notice.getCreatedBy())
                    .userName(notice.getUser() != null ? notice.getUser().getUserName() : "알 수 없음")
                    .postCount(postCount) // 📝 실시간 게시글 개수 주입
                    .build();
            
            // 완성된 하나를 리스트에 추가합니다.
            dtoList.add(dto);
        }
		return dtoList;
	}
	
	@Override 
	public NoticeRequestDTO getBoardDetail(String boardId) { // 리턴 타입 통일!
	    NoticeEntity notice = noticeRepository.findById(boardId)
	            .orElseThrow(() -> new RuntimeException("해당 게시판을 찾을 수 없습니다."));
	    
	    return NoticeRequestDTO.builder()
	            .boardId(notice.getBoardId())
	            .boardName(notice.getBoardName())
	            .description(notice.getDescription())
	            .userName(notice.getUser() != null ? notice.getUser().getUserName() : "알 수 없음")
	            .build();

				
	}
	
	@Override
	@Transactional
	public void updateBoard(NoticeRequestDTO noticeRequestDto){
		NoticeEntity board = noticeRepository.findById(noticeRequestDto.getBoardId()).orElse(null);
		board.updateBoard(noticeRequestDto.getBoardName(), noticeRequestDto.getDescription());
	}

	@Override
	@Transactional
	public boolean deleteBoard(String boardId){
		long postCount = postRepository.countByBoardId(boardId);
	
		if (postCount > 0) {
			return false;
	}
	noticeRepository.deleteById(boardId);
	return true; // 삭제 성공 반환
	}
}
