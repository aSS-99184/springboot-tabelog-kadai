package com.example.samuraitabelog.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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
import com.example.samuraitabelog.form.UserEditForm;
import com.example.samuraitabelog.repository.UserRepository;
import com.example.samuraitabelog.security.UserDetailsImpl;
import com.example.samuraitabelog.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
        
        return "redirect:/user";
    }   
    
    
    
    // 無料会員退会
    @PostMapping("/delete")
    public String deleteAccount(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes,HttpServletRequest request, HttpServletResponse response) {
    	try {
    		// ユーザー情報を取得
    		Integer userId = userDetailsImpl.getUser().getId();
        	userService.deleteAccount(userId);
        	// ログアウト処理（認証情報クリア）	
        	SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        	logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        	
	    	redirectAttributes.addFlashAttribute("successMessage", "会員情報を削除しました。再度利用したい場合は、会員登録を行ってください。");
	    	return "redirect:/login?logout";
    	
    	} catch (Exception e) {
    		redirectAttributes.addFlashAttribute("errorMessage", "退会処理中にエラーが発生しました。");
    		return "redirect:/index";
    	}
    }
}
