package com.warrantyportal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaWebController {

    /**
     * Forwards all frontend single-page application routes to /index.html
     * Excludes /api/**, /h2-console/**, and static resource paths with file extensions.
     */
    @GetMapping(value = {
            "/",
            "/login",
            "/register",
            "/dashboard",
            "/products",
            "/products/**",
            "/warranties",
            "/warranties/**",
            "/claims",
            "/claims/**",
            "/invoices",
            "/invoices/**",
            "/admin",
            "/admin/**"
    })
    public String forwardFrontendRoutes() {
        return "forward:/index.html";
    }
}
