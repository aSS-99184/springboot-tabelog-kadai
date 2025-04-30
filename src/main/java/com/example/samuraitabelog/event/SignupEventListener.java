package com.example.samuraitabelog.event;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.service.VerificationTokenService;

@Component
public class SignupEventListener {
	// トークンを作成・管理するサービスを使うための変数
    private final VerificationTokenService verificationTokenService; 
 // メール送信用のライブラリ（Springが用意してる）を使うための変数
    private final JavaMailSender javaMailSender;
    
    public SignupEventListener(VerificationTokenService verificationTokenService, JavaMailSender mailSender) {
    	// トークンサービスを使えるように変数にセット
    	this.verificationTokenService = verificationTokenService;        
    	// メール送信用の機能を使えるように変数にセット
    	this.javaMailSender = mailSender;
    }

    // イベント発生時に実行したいメソッド
    @EventListener
    // どのイベント発生時か→SignupEventクラスから通知を受けたときにonSignupEventメソッドを実行する
    private void onSignupEvent(SignupEvent signupEvent) { 
    	// ユーザー情報
        User user = signupEvent.getUser();
        // ランダムなトークンを生成
        String token = UUID.randomUUID().toString();
        // VerificationTokenServiceを使って、トークンをユーザーに関連付けて保存
        verificationTokenService.create(user, token);
        
        // メールの送信者
        String senderAddress = "springboot.samuraitabelog@example.com";
        // メールの受信者（ユーザー）のメールアドレスを取得
        String recipientAddress = user.getEmail();
        // メールの件名
        String subject = "メール認証";
        // // 確認用URLを作成（ユーザーがクリックすると、トークンを使って認証)
        String confirmationUrl = signupEvent.getRequestUrl() + "/verify?token=" + token;
        // メールの本文
        String message = "以下のリンクをクリックして会員登録を完了してください。";
        
        SimpleMailMessage mailMessage = new SimpleMailMessage(); 
        mailMessage.setFrom(senderAddress);
        mailMessage.setTo(recipientAddress);
        mailMessage.setSubject(subject);
        mailMessage.setText(message + "\n" + confirmationUrl);
        
        // 作成したメールメッセージを送信
        javaMailSender.send(mailMessage);

    }
}
