package com.tx.outsourcingmis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@Tag(name = "页面管理", description = "前端页面跳转")
public class PageController {

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/user-management")
    @Operation(summary = "用户管理", description = "返回用户管理页面")
    public String userManagement() {
        return "user-management";
    }

    @GetMapping("/applications")
    public String applications() {
        return "applications";
    }

    @GetMapping("/approval")
    public String approval() {
        return "approval";
    }

    @GetMapping("/performance")
    public String performance() {
        return "performance";
    }

    @GetMapping("/logs")
    public String logs() {
        return "logs";
    }
}