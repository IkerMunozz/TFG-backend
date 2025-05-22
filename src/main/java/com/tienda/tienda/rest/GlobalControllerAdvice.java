package com.tienda.tienda.rest;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void agregarEstadoSesion(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        model.addAttribute("logueado", token != null);
    }
}

