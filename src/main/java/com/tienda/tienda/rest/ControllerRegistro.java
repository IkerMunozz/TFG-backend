package com.tienda.tienda.rest;

import com.tienda.tienda.dao.DaoGrupo;
import com.tienda.tienda.dao.DaoSocio;
import com.tienda.tienda.dao.DaoToken;
import com.tienda.tienda.dao.DaoUsuario;
import com.tienda.tienda.encriptacion.Sha256;
import com.tienda.tienda.entities.*;
import com.tienda.tienda.service.TiendaServicio;
import com.tienda.tienda.service.TiendaServicioImpl;
import com.tienda.tienda.tools.Tools;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@RestController
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

    @PostMapping("/registrarsocio")
    @Transactional
    public String registrarSocio(@RequestParam String email,
                                 @RequestParam String nombre,
                                 @RequestParam String direccion,
                                 @RequestParam String password,
                                 @RequestParam String telefono) {
        try {
            if (daoSocio.findByEmail(email).isPresent()) {
                return "El email ya está registrado como socio.";
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

            return "Socio registrado correctamente. Revisa tu correo para validar tu cuenta.";
        } catch (Exception e) {
                e.printStackTrace();
                System.err.println("ERROR DE REGISTRO: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                return "Error al registrar socio: " + e.getMessage();
            }
    }





    @Transactional
    @RequestMapping(value = "/validar", method = {RequestMethod.GET, RequestMethod.POST})
    public String validarUsuario(@RequestParam String token) {
        System.out.println("Solicitud recibida con el token: " + token);

        try {
            System.out.println("Token recibido: " + token);

            // Buscar el token
            Optional<Token> tokenOpt = daoToken.findByValue(token);
            if (tokenOpt.isEmpty()) {
                return "Token inválido o expirado.";
            }

            Token tokenEntity = tokenOpt.get();
            String email = tokenEntity.getEmail();
            String clave = tokenEntity.getClave();

            System.out.println("Email asociado al token: " + email);

            if (email == null || email.isEmpty()) {
                return "El email no es válido.";
            }

            // Bloqueo pesimista para buscar usuario existente
            Optional<Usuario> usuarioExistenteOpt = daoUsuario.findByEmailWithLock(email);
            if (usuarioExistenteOpt.isPresent()) {
                return "El usuario ya ha sido validado previamente.";
            }

            // Buscar el socio (forzando que esté en sesión)
            Optional<Socio> socioOpt = daoSocio.findByEmail(email);
            if (socioOpt.isEmpty()) {
                return "No existe un socio asociado a este email.";
            }

            Socio socio = socioOpt.get();

            // IMPORTANTE: asegurar que Socio esté "attached" a la sesión
            socio = entityManager.find(Socio.class, socio.getId());
            if (socio == null) {
                return "Error interno: no se pudo asociar el socio.";
            }

            // Crear el nuevo usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setClave(clave);  // Se recomienda cifrar la clave aquí si aún no está cifrada

            daoUsuario.save(nuevoUsuario);
            tiendaServicio.insertarGrupo(email);

            return "Usuario validado y creado correctamente.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error al validar el usuario: " + e.getMessage();
        }
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String clave) throws Exception {

        Optional<Usuario> usuarioOpt = daoUsuario.findById(email);

        if (usuarioOpt.isEmpty()) {

            return "El usuario no existe";
        }

        String claveencriptada=Sha256.getSha256(clave);

        if (claveencriptada.equals(usuarioOpt.get().getClave())) {

            return "Login exitoso";
        }else{
            return "Contraseña incorrecta";
        }

    }

   





}





