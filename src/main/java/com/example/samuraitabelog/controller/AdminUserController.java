package com.example.samuraitabelog.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.repository.UserRepository;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
	private final UserRepository userRepository;
	
	public AdminUserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	// 管理者用の会員一覧
	@GetMapping
	// 名前のクエリパラメータを受け取る falseで任意。
	public String index (@RequestParam(name = "keyword", required = false) String keyword, 
						// ページング情報を受け取る（初期値：1ページ目、1ページに10件、idで昇順順)
						@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, Model model) {
		
		// ユーザーデータをページごとに分けて管理する
		Page<User> userPage;
		
		// keywordがnullではなく、かつ空文字ではないなら
		if (keyword != null && !keyword.isEmpty()) {
		// 名前かフリガナに指定したキーワードが部分的に含まれているユーザー情報をリポジトリデータベースから検索して、userPage格納する
            userPage = userRepository.findByNameLikeOrFuriganaLike("%" + keyword + "%", "%" + keyword + "%", pageable);                   
        } else {
        	// 逆にキーワードがなければ、ページネーション(pageable)を適用した全ユーザーを取得
            userPage = userRepository.findAll(pageable);
        }        
        
        model.addAttribute("userPage", userPage);        
        model.addAttribute("keyword", keyword);                
        
        return "admin/users/index";
	}
	
	// 管理者用の会員詳細ページ
	@GetMapping("/{id}")
	// URLでIDを渡す＝特定の会員のページを見る
    public String show(@PathVariable(name = "id") Integer id, Model model) {
		// userRepositoryを使ってデータベースからユーザー情報を取得してuserに入れる。IDは存在している前提。参照だけして、実際に使うときに読み込む。
        User user = userRepository.getReferenceById(id);
        // "user"という変数名で上記のuserオブジェクトをHTMLに渡す。
        model.addAttribute("user", user);
        
        return "admin/users/show";
    }    
}
