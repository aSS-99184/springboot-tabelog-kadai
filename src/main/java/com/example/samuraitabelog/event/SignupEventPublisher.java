package com.example.samuraitabelog.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.samuraitabelog.entity.User;

// AuthControllerのsignup()メソッドの中で呼び出す
@Component
public class SignupEventPublisher {
	private final ApplicationEventPublisher applicationEventPublisher;
	
    public SignupEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;                
    }
    
    public void publishSignupEvent(User user, String requestUrl) {
    	// SignupEventクラスのインスタンスを渡す
        applicationEventPublisher.publishEvent(new SignupEvent(this, user, requestUrl));
    }

}
