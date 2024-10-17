package com.alexwang.analysis.controller.admin;

import com.alexwang.analysis.service.TagService;
import com.alexwang.analysis.po.Analysis;
import com.alexwang.analysis.po.User;
import com.alexwang.analysis.service.BlogService;
import com.alexwang.analysis.service.TypeService;
import com.alexwang.analysis.vo.BlogQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class BlogController {

    private static final String INPUT = "admin/blogs-input";
    private static final String LIST = "admin/blogs";
    private static final String REDIRECT_LIST = "redirect:/admin/blogs";


    @Autowired
    private BlogService blogService;
    @Autowired
    private TypeService typeService;
    @Autowired
    private TagService tagService;

    @GetMapping("/blogs")
    public String blogs(@PageableDefault(size = 8, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                        BlogQuery blog, Model model) {
        model.addAttribute("types", typeService.listType());
        model.addAttribute("page", blogService.listBlog(pageable, blog));
        return LIST;
    }

    @PostMapping("/blogs/search")
    public String search(@PageableDefault(size = 8, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                         BlogQuery blog, Model model) {
        model.addAttribute("page", blogService.listBlog(pageable, blog));
        return "admin/blogs :: blogList";
    }


    @GetMapping("/blogs/input")
    public String input(Model model) {
        setTypeAndTag(model);
        model.addAttribute("analysis", new Analysis());
        return INPUT;
    }

    private void setTypeAndTag(Model model) {
        model.addAttribute("types", typeService.listType());
        model.addAttribute("tags", tagService.listTag());
    }


    @GetMapping("/blogs/{id}/input")
    public String editInput(@PathVariable Long id, Model model) {
        setTypeAndTag(model);
        Analysis analysis = blogService.getBlog(id);
        analysis.init();
        model.addAttribute("analysis", analysis);
        return INPUT;
    }



    @PostMapping("/blogs")
    public String post(Analysis analysis, RedirectAttributes attributes, HttpSession session) {
        analysis.setUser((User) session.getAttribute("user"));
        analysis.setType(typeService.getType(analysis.getType().getId()));
        analysis.setTags(tagService.listTag(analysis.getTagIds()));
        Analysis b;
        if (analysis.getId() == null) {
            b =  blogService.saveBlog(analysis);
        } else {
            b = blogService.updateBlog(analysis.getId(), analysis);
        }

        if (b == null ) {
            attributes.addFlashAttribute("message", "Failure");
        } else {
            attributes.addFlashAttribute("message", "Successful");
        }
        return REDIRECT_LIST;
    }


    @GetMapping("/blogs/{id}/delete")
    public String delete(@PathVariable Long id,RedirectAttributes attributes) {
        blogService.deleteBlog(id);
        attributes.addFlashAttribute("message", "Successfully deleted");
        return REDIRECT_LIST;
    }



}