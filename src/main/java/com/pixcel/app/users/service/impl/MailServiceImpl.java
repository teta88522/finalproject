package com.pixcel.app.users.service.impl;

import java.util.Random;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class MailServiceImpl {
	private final JavaMailSender mailSender;
	
	String authCode = String.format("%06d",new Random().nextInt(1000000));
	
	@Async
	public void sendAuthEmail(String toEmail, String authCode) {
	
	try {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");
		
		helper.setFrom("kdeok201@gmail.com");
		helper.setTo(toEmail);
		helper.setSubject("[Pixcel 구독 신청 이메일 인증코드입니다]");
		
		String mailContent="<h3>Pixcel 서비스 구독 신청 인증코드</h3>"
		+ "<p>아래 인증코드를 마이페이지 모달창에 입력해주세요.</p>"
		+ "<h2 style='color:#1f2d6c;'>"+ authCode + "</h2>"
		+ "<p>인증 유효 시간은 3분입니다.</p>";
		helper.setText(mailContent, true);
		mailSender.send(message);
	}catch(Exception e) {
		e.printStackTrace();
		throw new RuntimeException("이메일 발송에 실패했습니다");
	}

}
}
