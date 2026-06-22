package com.pixcel.app.document.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/document")
public class DocumentController {
	@GetMapping("/list")
    public String accordion() {
        return "sample/documentList";
    }

}
