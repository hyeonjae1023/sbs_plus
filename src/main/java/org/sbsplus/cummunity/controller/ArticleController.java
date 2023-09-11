package org.sbsplus.cummunity.controller;


import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.sbsplus.cummunity.dto.ArticleDto;
import org.sbsplus.cummunity.service.ArticleService;
import org.sbsplus.type.Category;
import org.sbsplus.util.Pager;
import org.sbsplus.util.Rq;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/article")
@RequiredArgsConstructor
@ControllerAdvice
public class ArticleController {
    
    private final ArticleService articleService;
    private final Rq rq;
    
    // 게시글 리스트
    @GetMapping("")
    public String articleList(
              @RequestParam(defaultValue = "1") Integer page
            , @RequestParam(defaultValue = "ALL", name = "category") String category_
            , Model model
            ){
        Category category = Category.convertStringToEnum(category_);
        
        
        Page<ArticleDto> articles = articleService.findByCategory(page-1, category);
        
        if(articles == null || page > articles.getTotalPages()) {
            return rq.unexpectedRequestForWardUri("존재하지 않는 페이지입니다.");
        }
        
        Integer totalPage = articles.getTotalPages();
        
        
        model.addAttribute("articles", articles);
        
        List<Category> categories = Category.getCategories();
        model.addAttribute("categories", categories);
        
        List<Integer> pageRange = Pager.getPageRange(page, totalPage);
        model.addAttribute("pageRange", pageRange);
        
        return "/article/articleList";
    }
    
    // 게시글 디테일 조회
    @GetMapping("/{articleId}")
    public String articleDetail(@PathVariable Integer articleId, Model model){
        
        ArticleDto article = articleService.findById(articleId);
        
        model.addAttribute("article", article);
        
        // 중복 조회수 카운팅 방지
        Cookie oldCookie = rq.getCookie("viewedArticles");
        
        if (oldCookie != null) {
            if (!oldCookie.getValue().contains("[" + articleId.toString() + "]")) {
                articleService.increaseHit(articleId);
                oldCookie.setValue(oldCookie.getValue() + "_[" + articleId + "]");
                oldCookie.setPath("/article");
                oldCookie.setMaxAge(60 * 60 * 24);
                rq.getResponse().addCookie(oldCookie);
            }
        } else {
            articleService.increaseHit(articleId);
            Cookie newCookie = new Cookie("viewedArticles","[" + articleId + "]");
            newCookie.setPath("/article");
            newCookie.setMaxAge(60 * 60 * 24);
            rq.getResponse().addCookie(newCookie);
        }
        
        return "/article/articleDetail";
    }


    



}
