package com.sgch.hospital.util.mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{

    private final JavaMailSender mailSender;

    // Aquí definirás el correo del remitente que debe ser configurado en application.properties/yml
    // @Value("${spring.mail.username}") 
    private String fromEmail = "sgch.noreply@gmail.com"; 
    
    @Override
    public void enviarCorreo(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        
        // El envío de correo es una OPERACIÓN IMPERATIVA
        mailSender.send(message); 
    }
}
