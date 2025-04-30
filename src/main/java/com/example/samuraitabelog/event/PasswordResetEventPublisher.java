package com.example.samuraitabelog.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.samuraitabelog.entity.User;

// イベントを発行する
@Component
public class PasswordResetEventPublisher {
	private final ApplicationEventPublisher applicationEventPublisher;
	
	public PasswordResetEventPublisher (ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;                
    }
    
	// パスワードリセット用のイベントを発行する
    public void publishPasswordResetEvent(User user, String requestUrl) {
    	applicationEventPublisher.publishEvent(new PasswordResetEvent(this, user, requestUrl));
    }

}
