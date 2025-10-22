package com.sgch.hospital.util.mail;

public interface EmailService {
    void enviarCorreo(String to, String subject, String text);
}
