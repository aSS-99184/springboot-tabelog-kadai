package com.example.samuraitabelog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.entity.VerificationToken;
import com.example.samuraitabelog.event.PasswordResetEventPublisher;
import com.example.samuraitabelog.event.SignupEventPublisher;
import com.example.samuraitabelog.form.SignupForm;
import com.example.samuraitabelog.service.PasswordResetTokenService;
import com.example.samuraitabelog.service.UserService;
import com.example.samuraitabelog.service.VerificationTokenService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {
	private final UserService userService;
	private final SignupEventPublisher signupEventPublisher;
	private final VerificationTokenService verificationTokenService;
	private final PasswordResetTokenService passwordResetTokenService;
    private final PasswordResetEventPublisher passwordResetEventPublisher;
	
	public AuthController(UserService userService, SignupEventPublisher signupEventPublisher, VerificationTokenService verificationTokenService, PasswordResetTokenService passwordResetTokenService, PasswordResetEventPublisher passwordResetEventPublisher) {        
        this.userService = userService;     
        this.signupEventPublisher = signupEventPublisher;
        this.verificationTokenService = verificationTokenService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.passwordResetEventPublisher = passwordResetEventPublisher;
 
    }
	
	@GetMapping("/login")
	public String login(Model model) {
			return "auth/login";
	}
	
	// signupにアクセスしたらサインアップ画面に空の入力フォームを開く
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("signupForm", new SignupForm());
		return "auth/signup";
	}
	
	// 入力内容(signupForm)を受け取って、チェックして、問題なければ登録処理する
	@PostMapping("/signup")
	public String signup(@ModelAttribute @Validated SignupForm signupForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
		// 入力フォームに入力されたemailを取り出して、すでに登録されていれば、エラー表示
		if (userService.isEmailRegistered(signupForm.getEmail())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
			bindingResult.addError(fieldError);
		}       
        
		// フォームにエラーがあるとサインアップ画面に戻る
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }
        // データを保存する
        User createdUser = userService.create(signupForm);
        // リクエストURLを取る
        String requestUrl = new String(httpServletRequest.getRequestURL());
        // イベントを発行する
        signupEventPublisher.publishSignupEvent(createdUser, requestUrl);
        
        redirectAttributes.addFlashAttribute("successMessage", "ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、会員登録を完了してください。"); 
        
        return "redirect:/";
    } 
	// メールの中にあるリンクをクリックしたら呼び出される処理
    @GetMapping("/signup/verify")
    public String verify(@RequestParam(name = "token") String token, Model model) {
        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
        
        if (verificationToken != null) {
            User user = verificationToken.getUser();  
            userService.enableUser(user);
            String successMessage = "会員登録が完了しました。登録したメールアドレスとパスワードでログインしてください。";
            model.addAttribute("successMessage", successMessage);            
        } else {
            String errorMessage = "認証に失敗しました。もう一度登録をお願いします。";
            model.addAttribute("errorMessage", errorMessage);
        }
        
        return "auth/verify";         
    }
   
}


