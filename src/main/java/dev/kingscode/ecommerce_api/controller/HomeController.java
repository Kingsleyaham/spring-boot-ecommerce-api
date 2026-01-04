package dev.kingscode.ecommerce_api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String redirectToDocs() {
        return "redirect:/docs";
    }

}
