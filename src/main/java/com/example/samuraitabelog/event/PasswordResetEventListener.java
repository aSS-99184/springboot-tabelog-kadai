package com.example.samuraitabelog.event;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.service.PasswordResetTokenService;

// PasswordResetEventからの通知を受けてパスワード再設定用のメールを送信する
@Component
public class PasswordResetEventListener {
    private final PasswordResetTokenService passwordResetTokenService;    
    private final JavaMailSender javaMailSender;
    
    public PasswordResetEventListener(PasswordResetTokenService passwordResetTokenService, JavaMailSender mailSender) {
        this.passwordResetTokenService = passwordResetTokenService;        
        this.javaMailSender = mailSender;
    }

	@EventListener
	private void onPasswordResetEvent(PasswordResetEvent passwordResetEvent) {
	    User user = passwordResetEvent.getUser();
	    String token = UUID.randomUUID().toString();
	    passwordResetTokenService.create(user, token);
	    
	    String senderAddress = "springboot.samuraitabelog@example.com";
	    String recipientAddress = user.getEmail();
	    String subject = "パスワード再設定のご案内";
	    String confirmationUrl = passwordResetEvent.getRequestUrl() + "?token=" + token;
	    String message = "以下のリンクをクリックしてパスワードを再設定してください。";
	    
	    SimpleMailMessage mailMessage = new SimpleMailMessage(); 
	    mailMessage.setFrom(senderAddress);
	    mailMessage.setTo(recipientAddress);
	    mailMessage.setSubject(subject);
	    mailMessage.setText(message + "\n" + confirmationUrl);
	    javaMailSender.send(mailMessage);
	}

}
