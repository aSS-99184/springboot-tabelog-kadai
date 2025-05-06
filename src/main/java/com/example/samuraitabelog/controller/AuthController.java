package com.example.samuraitabelog.controller;

import java.util.UUID;

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

import com.example.samuraitabelog.entity.PasswordResetToken;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.entity.VerificationToken;
import com.example.samuraitabelog.event.PasswordResetEventPublisher;
import com.example.samuraitabelog.event.SignupEventPublisher;
import com.example.samuraitabelog.form.PasswordEditForm;
import com.example.samuraitabelog.form.PasswordRequestForm;
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
	public String login() {
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
            String successMessage = "会員登録が完了しました。";
            model.addAttribute("successMessage", successMessage);            
        } else {
            String errorMessage = "認証に失敗しました。もう一度登録をお願いします。";
            model.addAttribute("errorMessage", errorMessage);
        }
        
        return "auth/verify";         
    }
    
    
    
    // パスワードリセットリクエスト画面の表示
    @GetMapping("/password/password_reset_request")
    public String showPasswordResetRequestForm(Model model) {
    	model.addAttribute("passwordRequestForm", new PasswordRequestForm());
    	return "password/password_reset_request";
    }
    
    // リセット用のリンクをユーザーに送信する
    @PostMapping("/password/password_reset_request")
    public String processPasswordResetRequest(@ModelAttribute @Validated PasswordRequestForm passwordRequestForm,BindingResult bindingResult, Model model,  HttpServletRequest httpServletRequest) {	
    	
    	// 入力されたメールアドレスが既に登録されていなければエラー表示
    	String email = passwordRequestForm.getEmail();
    	if (!userService.isEmailRegistered(email)) {
    		FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "このメールアドレスは登録されていません。");
    		bindingResult.addError(fieldError);
    	}
    	
    	if (bindingResult.hasErrors()) {
    		model.addAttribute("passwordRequestForm", passwordRequestForm);
    		return "password/password_reset_request";
    	}
    	
    	// ユーザーを取得
        User user = userService.findUserByEmail(email);
        // パスワードリセット用のランダムなトークンを生成
        String token = UUID.randomUUID().toString();
        // トークンを保存する
        passwordResetTokenService.create(user, token);     
        // リクエストURLを取る
        String requestUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() + ":" + httpServletRequest.getServerPort() + "/password/password_reset";
        
        
        // イベントを発行してリセットメールを送信
        passwordResetEventPublisher.publishPasswordResetEvent(user, requestUrl);
        
        model.addAttribute("passwordRequestForm", new PasswordRequestForm());
    	model.addAttribute("successMessage", "パスワード再設定用のリンクをメールで送信しました。メールをご確認ください。");
    	
    	return "password/password_reset_request";
    	
    }
    
    // パスワードを再設定するページを表示する
    @GetMapping("/password/password_reset")
    public String showPasswordResetFormByToken(@RequestParam(name = "token") String token, Model model) {
    	PasswordResetToken passwordResetToken = passwordResetTokenService.getPasswordResetToken(token);
    	
    	if (passwordResetToken != null) {
    		User user = passwordResetToken.getUser();
    		model.addAttribute("user", user);
    		model.addAttribute("resetToken", token);
    		PasswordEditForm form = new PasswordEditForm();
            form.setToken(token);
    		model.addAttribute("passwordEditForm", form);
    		return "password/password_reset";
    	} else {
    		String errorMessage = "このリンクは無効です。もう一度パスワード再設定画面よりメールアドレスを入力してください。";
    		model.addAttribute("errorMessage", errorMessage);
    		return "redirect:/password/password_reset";
    	}
    }
    
    // パスワード再設定を実際に処理する
    @PostMapping("/password/password_reset")
    public String handlePasswordResetByToken(@ModelAttribute @Validated PasswordEditForm passwordEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
    	
    	String token = passwordEditForm.getToken();   	
    	PasswordResetToken resetToken = passwordResetTokenService.getPasswordResetToken(token);
    	
    	// 万が一トークンが無効な場合の時のエラー表示
    	if (resetToken == null) {
    		redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました。");
    		return "redirect:/password/password_reset_request";
    	}
    	
    	// 入力パスワードと確認パスワードが一致していなければエラー
    	if (!passwordEditForm.getPassword().equals(passwordEditForm.getPasswordConfirm())) {
    		bindingResult.rejectValue("passwordConfirm", "error.passwordConfirm", "パスワードが一致しません。再度入力してください。");
    	}
    	
    	if (bindingResult.hasErrors()) {
    		return "password/password_reset";
    	}
    	
    	// ユーザーのパスワードを更新
    	User user = resetToken.getUser();
    	userService.newpassword(passwordEditForm, user);
    	
    	// パスワードリセットトークンの削除
    	passwordResetTokenService.delete(resetToken);

    	// 成功メッセージ
    	redirectAttributes.addFlashAttribute("successMessage", "パスワードを変更しました。ログインしてください。");
    	
    	return "redirect:/auth/login";
    }
   
}


