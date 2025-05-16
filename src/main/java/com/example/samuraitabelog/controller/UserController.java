package com.example.samuraitabelog.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.form.PasswordEditForm;
import com.example.samuraitabelog.form.UserEditForm;
import com.example.samuraitabelog.repository.UserRepository;
import com.example.samuraitabelog.security.UserDetailsImpl;
import com.example.samuraitabelog.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    
    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository; 
        this.userService = userService; 

    }    
    
    @GetMapping
    public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {         
    	if (userDetailsImpl == null) {
    		return "redirect:/auth/login";
    	}
    	
    	
    	User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());  
        
        model.addAttribute("user", user);
        
        return "user/index";
    }
    
    @GetMapping("/edit")
    public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {        
        User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());  
        UserEditForm userEditForm = new UserEditForm(user.getId(), user.getName(), user.getFurigana(), user.getPostalCode(), user.getAddress(), user.getPhoneNumber(), user.getEmail());
        
        model.addAttribute("userEditForm", userEditForm);
        
        return "user/edit";
    } 
    
    @PostMapping("/update")
    public String update(@ModelAttribute @Validated UserEditForm userEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        
        if (userService.isEmailChanged(userEditForm) && userService.isEmailRegistered(userEditForm.getEmail())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
            bindingResult.addError(fieldError);                       
        }
        
        if (bindingResult.hasErrors()) {
            return "user/edit";
        }
        
        userService.update(userEditForm);
        redirectAttributes.addFlashAttribute("successMessage", "会員情報を編集しました。");
        
        return "redirect:/index";
    }   
    
    // パスワード再設定画面を表示する
    @GetMapping("/password/reset")
    public String showResetPassword(Model model) {
        model.addAttribute("passwordEditForm", new PasswordEditForm());
        return "password/password_reset";
    }
    
    // パスワード再設定を実行する
    @PostMapping("/password/reset")
    public String resetPassword(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,@ModelAttribute @Validated PasswordEditForm passwordEditForm,BindingResult bindingResult,RedirectAttributes redirectAttributes) {
    	
    	// パスワードと確認用パスワードが違ったらエラー
    	if (!passwordEditForm.getPassword().equals(passwordEditForm.getPasswordConfirm())) {
    		bindingResult.rejectValue("passwordConfirm", "error.passwordConfirm", "パスワードが一致しません。");
    	}
    	
    	// エラーがあれば、もう一度パスワード再設定画面に戻す
    	if (bindingResult.hasErrors()) {
            return "password/password_reset";
    	}
    	
    	User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
    	userService.newpassword(passwordEditForm, user);
    	redirectAttributes.addFlashAttribute("successMessage", "パスワードを変更しました。新しいパスワードでログインしてください。");
    	return "redirect:/auth/login";
    }
    
    // 無料会員退会
    @PostMapping("/delete")
    public String deleteAccount(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes) {
    	// ユーザー情報を取得
    	Integer userId = userDetailsImpl.getUser().getId();
    	userService.deleteAccount(userId);

    	try {
    	redirectAttributes.addFlashAttribute("successMessage", "会員情報を削除しました。再度利用したい場合は、会員登録を行ってください。");
    	return "redirect:/login?logout";
    	
    	} catch (Exception e) {
    		redirectAttributes.addFlashAttribute("errorMessage", "退会処理中にエラーが発生しました。");
    		return "redirect:/user";
    	}
    }
}
