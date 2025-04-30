package com.example.samuraitabelog.event;

import org.springframework.context.ApplicationEvent;

import com.example.samuraitabelog.entity.User;

import lombok.Getter;

// Listenerクラスから情報を取得
@Getter
// ApplicationEventクラスを継承
public class SignupEvent extends ApplicationEvent {
		
		// 会員登録したユーザー情報を保持
	    private User user;
	    // リクエストを受けたURL（https://ドメイン名/signup）を保持
	    private String requestUrl;        

	    // スーパークラスのコンストラクタを呼び出し、Publisherクラスのインスタンスを渡す
	    public SignupEvent(Object source, User user, String requestUrl) {
	        super(source);
	        
	        this.user = user;        
	        this.requestUrl = requestUrl;
	    }

}
