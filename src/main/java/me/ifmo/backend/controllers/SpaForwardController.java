package me.ifmo.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping("/reset-password")
    public String forwardReset() { return "forward:/index.html"; }
}
