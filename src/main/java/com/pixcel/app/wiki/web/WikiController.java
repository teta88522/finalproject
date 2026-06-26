package com.pixcel.app.wiki.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/wiki")
@Controller
public class WikiController {

//    @GetMapping("/")
//    public String wikiPage(Model model) {
//
//        model.addAttribute("wikiId", "123");
//
//        return "wiki/webTest";
//    }
//    
    @GetMapping("/")
    public String wikiPage() {


        return "wiki/wiki";
    }
    
    
    @GetMapping("/editor")
    public String editor() {
        return "wiki/editor";
    }


}