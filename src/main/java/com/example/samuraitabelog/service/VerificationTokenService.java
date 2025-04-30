package com.example.samuraitabelog.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.entity.VerificationToken;
import com.example.samuraitabelog.repository.VerificationTokenRepository;

@Service
public class VerificationTokenService {
private final VerificationTokenRepository verificationTokenRepository;
    
    // コンストラクタ
    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository) {        
        this.verificationTokenRepository = verificationTokenRepository;
    } 
    
    @Transactional
    public void create(User user, String token) {
    	// 新しいトークンオブジェクト
        VerificationToken verificationToken = new VerificationToken();
        
     // トークンに対応するユーザーをセット
        verificationToken.setUser(user);
     // トークンの文字列をセット
        verificationToken.setToken(token);        
     // データベースにトークン情報を保存
        verificationTokenRepository.save(verificationToken);
    }    
    
 // トークン文字列を使って、対応するトークン情報を探す処理
    public VerificationToken getVerificationToken(String token) {
    	// トークンが一致するレコードをデータベースから取得
        return verificationTokenRepository.findByToken(token);
    }    
}
