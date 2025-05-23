package com.tienda.tienda.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class RedirectController {
    @GetMapping("/")
    public void redirectToProductos(HttpServletResponse response) throws IOException {
        response.sendRedirect("/api/v1/productos");
    }
}
