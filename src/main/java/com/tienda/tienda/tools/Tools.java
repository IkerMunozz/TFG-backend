package com.tienda.tienda.tools;

import java.security.SecureRandom;
import java.util.Properties;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class Tools {

    public static String generarToken() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int longitud = 80;

        // Creamos un objeto SecureRandom para generar números aleatorios de forma segura
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(longitud);

        // Generamos el token con caracteres aleatorios
        for (int i = 0; i < longitud; i++) {
            int index = random.nextInt(caracteres.length()); // Selecciona un índice aleatorio
            token.append(caracteres.charAt(index)); // Añade el carácter correspondiente al índice
        }

        // Retornamos el token generado
        return token.toString();
    }



    public static void enviarConGMail(String destinatario, String asunto, String cuerpo) {
        String remitente = "proyecto.swappy@gmail.com"; // El correo que envía
        String clave = "enbi gocw nhsh rsrh"; // Contraseña de aplicación

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, null); // Mejor control

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remitente)); // Muy importante poner remitente aquí
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
            message.setSubject(asunto);
            message.setContent(cuerpo, "text/html; charset=utf-8");

            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", remitente, clave);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (MessagingException me) {
            me.printStackTrace(); // Mejor imprimir o lanzar excepción
        }
    }


    public static String creaCuerpoCorreo(String token) {
        String ref = "http://tfg-backend-production.up.railway.app/api/registro/validar?token=" + token;
        StringBuilder st = new StringBuilder();
        st.append("<!DOCTYPE html>");
        st.append("<html lang='es'>");
        st.append("<head>");
        st.append("    <meta charset='UTF-8'>");
        st.append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        st.append("    <title>Confirmación de correo - Swappy</title>");
        st.append("    <style>");
        st.append("        body { background-color: #e5e5e5; margin: 0; font-family: 'Roboto', 'Helvetica Neue', Arial, sans-serif; }");
        st.append("        .email-wrapper { width: 100%; min-height: 100vh; display: flex; justify-content: center; align-items: center; padding: 40px 0; }");
        st.append("        .email-content { background: #ffffff; width: 100%; max-width: 600px; padding: 40px; border-radius: 12px; box-shadow: 0px 8px 24px rgba(0, 0, 0, 0.15); text-align: center; }");
        st.append("        .logo { font-size: 32px; font-weight: bold; color: #00bfa6; margin-bottom: 30px; }");
        st.append("        .title { font-size: 26px; font-weight: 700; color: #333333; margin-bottom: 20px; }");
        st.append("        .text { font-size: 16px; color: #666666; line-height: 1.6; margin-bottom: 30px; }");
        st.append("        .btn { display: inline-block; padding: 16px 32px; background-color: #00bfa6; color: white; text-decoration: none; border-radius: 8px; font-size: 18px; font-weight: bold; transition: background-color 0.3s ease; }");
        st.append("        .btn:hover { background-color: #009688; }");
        st.append("        .footer { font-size: 12px; color: #999999; margin-top: 40px; }");
        st.append("        a { word-break: break-word; }");
        st.append("    </style>");
        st.append("</head>");
        st.append("<body>");
        st.append("    <div class='email-wrapper'>");
        st.append("        <div class='email-content'>");
        st.append("            <div class='logo'>Swappy</div>");
        st.append("            <div class='title'>Confirma tu correo electrónico</div>");
        st.append("            <div class='text'>");
        st.append("                ¡Gracias por registrarte en <strong>Swappy</strong>!<br><br>");
        st.append("                Para activar tu cuenta, por favor confirma tu dirección de correo pulsando el siguiente botón:");
        st.append("            </div>");
        st.append("            <a href='" + ref + "' class='btn'>Confirmar correo</a>");
        st.append("            <div class='footer'>Este es un mensaje automático. No respondas a este correo.</div>");
        st.append("        </div>");
        st.append("    </div>");
        st.append("</body>");
        st.append("</html>");
        return st.toString();
    }



}
