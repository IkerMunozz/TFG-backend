package com.tienda.tienda.rest;

import com.tienda.tienda.dao.DaoGrupo;
import com.tienda.tienda.dao.DaoSocio;
import com.tienda.tienda.dao.DaoToken;
import com.tienda.tienda.dao.DaoUsuario;
import com.tienda.tienda.dto.ProductoDTO;
import com.tienda.tienda.encriptacion.Sha256;
import com.tienda.tienda.entities.*;
import com.tienda.tienda.service.TiendaServicio;
import com.tienda.tienda.service.TiendaServicioImpl;
import com.tienda.tienda.tools.Tools;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/registro")
public class ControllerRegistro {

    @Autowired
    private DaoSocio daoSocio;
    @Autowired
    private DaoToken daoToken;
    @Autowired
    private DaoGrupo daoGrupo;
    @Autowired
    private DaoUsuario daoUsuario;
    @Autowired
    private TiendaServicio tiendaServicio;
    @PersistenceContext
    private EntityManager entityManager;



    public ControllerRegistro(DaoSocio daoSocio, DaoToken daoToken, DaoGrupo daoGrupo, DaoUsuario daoUsuario, TiendaServicio tiendaServicio) {
        this.daoSocio = daoSocio;
        this.daoToken = daoToken;
        this.daoGrupo = daoGrupo;
        this.daoUsuario = daoUsuario;
        this.tiendaServicio = tiendaServicio;
    }

    @GetMapping("/registro")
    public String mostrarFormulario() {
        return "registro"; // Esto busca "templates/registro.html"
    }
    @PostMapping("/registrarsocio")
    @Transactional
    public String registrarSocio(@RequestParam String email,
                                 @RequestParam String nombre,
                                 @RequestParam String direccion,
                                 @RequestParam String password,
                                 @RequestParam String telefono,
                                 Model model) {
        try {
            if (daoSocio.findByEmail(email).isPresent()) {
                model.addAttribute("error", "El email ya está registrado como socio.");
                return "registro";
            }

            // Crear socio
            Socio nuevoSocio = new Socio();
            nuevoSocio.setEmail(email);
            nuevoSocio.setNombre(nombre);
            nuevoSocio.setDireccion(direccion);
            nuevoSocio.setSaldo(BigDecimal.ZERO);
            nuevoSocio.setVersion(1);

            daoSocio.save(nuevoSocio);
            daoSocio.flush(); // <<---- CLAVE para evitar el error

            // Crear token
            String token = Tools.generarToken();
            String claveEncriptada = Sha256.getSha256(password);

            Token tokenEntity = new Token();
            tokenEntity.setEmail(email);
            tokenEntity.setClave(claveEncriptada);
            tokenEntity.setValue(token);
            tokenEntity.setTelefono(telefono);
            tokenEntity.setFechaInicio(Instant.now());

            daoToken.save(tokenEntity);

            // Enviar correo
            String cuerpoCorreo = Tools.creaCuerpoCorreo(token);
            Tools.enviarConGMail(email, "Validación de tu cuenta en Swappy", cuerpoCorreo);

            model.addAttribute("mensaje", "Socio registrado correctamente. Revisa tu correo para validar tu cuenta.") ;
            return "registro_resultado";
        } catch (Exception e) {
                e.printStackTrace();
                System.err.println("ERROR DE REGISTRO: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                model.addAttribute("error", "Error al registrar socio: " + e.getMessage());
                return "registrp";
            }
    }





    @Transactional
    @RequestMapping(value = "/validar", method = {RequestMethod.GET, RequestMethod.POST})
    public String validarUsuario(@RequestParam String token, Model model) {
        System.out.println("Solicitud recibida con el token: " + token);

        try {
            System.out.println("Token recibido: " + token);

            // Buscar el token
            Optional<Token> tokenOpt = daoToken.findByValue(token);
            if (tokenOpt.isEmpty()) {
                model.addAttribute("mensaje", "Token inválido o expirado.");
                return "registro_resultado";
            }

            Token tokenEntity = tokenOpt.get();
            String email = tokenEntity.getEmail();
            String clave = tokenEntity.getClave();

            System.out.println("Email asociado al token: " + email);

            if (email == null || email.isEmpty()) {
                model.addAttribute("mensaje",  "El email no es válido.");
            }

            // Bloqueo pesimista para buscar usuario existente
            Optional<Usuario> usuarioExistenteOpt = daoUsuario.findByEmailWithLock(email);
            if (usuarioExistenteOpt.isPresent()) {
                model.addAttribute("mensaje", "El usuario ya ha sido validado previamente.");
                return "registro_resultado";
            }

            // Buscar el socio (forzando que esté en sesión)
            Optional<Socio> socioOpt = daoSocio.findByEmail(email);
            if (socioOpt.isEmpty()) {
                model.addAttribute("mensaje", "No existe un socio asociado a este email.");
                return "registro_resultado";
            }

            Socio socio = socioOpt.get();

            // IMPORTANTE: asegurar que Socio esté "attached" a la sesión
            socio = entityManager.find(Socio.class, socio.getId());
            if (socio == null) {
                model.addAttribute("mensaje", "Error interno: no se pudo asociar el socio.");
                return "registro_resultado";
            }

            // Crear el nuevo usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setClave(clave);  // Se recomienda cifrar la clave aquí si aún no está cifrada

            daoUsuario.save(nuevoUsuario);
            tiendaServicio.insertarGrupo(email);

            model.addAttribute("mensaje", "Usuario validado y creado correctamente.");
            return "login";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("mensaje", "Error al validar el usuario: " + e.getMessage());
            return "registro_resultado";
        }
    }

    @GetMapping("/index")
    public String mostrarIndex(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");

        if (token == null) {
            return "redirect:/login";
        }

        List<ProductoDTO> productos = tiendaServicio.getAllProductosDTO();
        model.addAttribute("productos", productos);
        return "index";
    }


    @GetMapping("/login")
    public String mostrarFormularioLogin() {
        return "login"; 
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String clave,
                        HttpSession session,
                        Model model) throws Exception {

        Optional<Usuario> usuarioOpt = daoUsuario.findById(email);

        if (usuarioOpt.isEmpty()) {
            model.addAttribute("error", "El usuario no existe");
            return "login";
        }

        String claveencriptada = Sha256.getSha256(clave);

        if (claveencriptada.equals(usuarioOpt.get().getClave())) {
            // Buscar token y guardarlo en sesión
            Optional<Token> tokenOpt = daoToken.findById(email);
            if (tokenOpt.isPresent()) {
                session.setAttribute("token", tokenOpt.get().getValue());
            }

             List<ProductoDTO> productos = tiendaServicio.getAllProductosDTO();
            
            model.addAttribute("productos", productos); 
            model.addAttribute("mensaje", "Login exitoso");
            return "redirect:/api/v1/productos"; 
        } else {
            model.addAttribute("error", "Contraseña incorrecta");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, Model model) {
        session.invalidate();       
             List<ProductoDTO> productos = tiendaServicio.getAllProductosDTO();          
            model.addAttribute("productos", productos); 
        return "redirect:/api/v1/productos";  
    }



   





}





