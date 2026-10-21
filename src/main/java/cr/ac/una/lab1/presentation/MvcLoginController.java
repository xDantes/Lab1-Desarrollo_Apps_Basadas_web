package cr.ac.una.lab1.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Página de login del módulo MVC (sesión + CSRF, separado del login JWT de la API). */
@Controller
class MvcLoginController {

    /** GET /mvc/login — Spring Security procesa el POST automáticamente (ver SecurityConfig). */
    @GetMapping("/mvc/login")
    String login() {
        return "login";
    }
}
