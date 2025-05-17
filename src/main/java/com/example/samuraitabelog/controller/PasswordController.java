package com.example.samuraitabelog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitabelog.entity.PasswordResetToken;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.form.PasswordEditForm;
import com.example.samuraitabelog.form.PasswordRequestForm;
import com.example.samuraitabelog.service.PasswordResetTokenService;
import com.example.samuraitabelog.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/password")
public class PasswordController {
	private final UserService userService;
	private final PasswordResetTokenService passwordResetTokenService;
	
	public PasswordController(UserService userService, PasswordResetTokenService passwordResetTokenService) {
		this.userService = userService;
		this.passwordResetTokenService = passwordResetTokenService;
		
	}
	
	// 1.パスワード再設定メール送信用の画面
	@GetMapping("/password_reset_request")
	public String showPasswordResetRequestForm(Model model) {
		model.addAttribute("passwordRequestForm", new PasswordRequestForm());
		return "password/password_reset_request";
	}
	
	// 2.リセット用のリンクをユーザーに送信する
    @PostMapping("/password_reset_request")
    public String processPasswordResetRequest(@ModelAttribute @Validated PasswordRequestForm passwordRequestForm,BindingResult bindingResult, Model model,  HttpServletRequest httpServletRequest) {	
    	
    	// 入力されたメールアドレスが登録されていなければエラー表示
    	String email = passwordRequestForm.getEmail();
    	if (!userService.isEmailRegistered(email)) {
    		bindingResult.rejectValue("email", "error.email", "このメールアドレスは登録されていません。");
    	}
    	
    	if (bindingResult.hasErrors()) {
    		model.addAttribute("passwordRequestForm", passwordRequestForm);
    		return "password/password_reset_request";
    	}
    	
    	// ユーザーを取得
        User user = userService.findUserByEmail(email);
       
        // リクエストURLを取る
        String requestUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() + ":" + httpServletRequest.getServerPort() + "/password/password_reset";
        
        
        // イベントを発行してリセットメールを送信
        userService.sendPasswordResetEmail(user, requestUrl);
    
    	model.addAttribute("successMessage", "パスワード再設定用のリンクをメールで送信しました。メールの内容に沿って操作してください。");
    	
    	return "auth/verify";
    }
    
    // 3.パスワードを再設定するページを表示する
    @GetMapping("/password_reset")
    public String showPasswordResetForm(@RequestParam(name = "token") String token, Model model,RedirectAttributes redirectAttributes) {
    
    	if (token == null) {
    		redirectAttributes.addFlashAttribute("errorMessage", "無効なリンクです。メールアドレス入力画面からやり直してください。");
    		return "redirect:/password/password_reset_request";
    	}
    	
    	PasswordResetToken resetToken = passwordResetTokenService.getPasswordResetToken(token);
    	if (resetToken == null) {
    		redirectAttributes.addFlashAttribute("errorMessage", "このリンクは無効です。");
    		return "redirect:/password/password_reset_request";
    	}
    	
    	PasswordEditForm form = new PasswordEditForm();
    	form.setToken(token);
    	model.addAttribute("passwordEditForm", form);
    	return "password/password_reset";
    }
	

    // 4.パスワード再設定を実際に処理する
    @PostMapping("/password_reset")
    public String handlePasswordResetByToken(@ModelAttribute @Validated PasswordEditForm passwordEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
    	
    	String token = passwordEditForm.getToken();   	
    	PasswordResetToken resetToken = passwordResetTokenService.getPasswordResetToken(token);
    	
    	// 万が一トークンが無効な場合の時のエラー表示
    	if (resetToken == null) {
    		redirectAttributes.addFlashAttribute("errorMessage", "無効なリンクです。再度メールアドレスを入力してください。");
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
    	redirectAttributes.addFlashAttribute("successMessage", "パスワードを変更しました。新しいパスワードでログインしてください。");
    	
    	// 5. 完了画面にリダイレクト
    	return "redirect:/password/password_reset_message";
    }
    
    // 6. パスワード再設定完了画面
    @GetMapping("/password_reset_message")
    public String showPasswordResetComplete() {
    	return "password/password_reset_message";
    }
	

}
