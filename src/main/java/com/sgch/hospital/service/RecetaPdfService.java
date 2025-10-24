package com.sgch.hospital.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.itextpdf.html2pdf.HtmlConverter;
import com.sgch.hospital.model.entity.Receta;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecetaPdfService {

    private final TemplateEngine templateEngine;

    public byte[] generarPdfReceta(Receta receta) throws IOException {
        // Preparar datos para el template
        Map<String, Object> variables = new HashMap<>();
        variables.put("receta", receta);
        variables.put("paciente", receta.getNotaMedica().getExpediente().getPaciente());
        variables.put("doctor", receta.getNotaMedica().getDoctor());
        variables.put("notaMedica", receta.getNotaMedica());
        variables.put("fechaFormateada", receta.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        variables.put("hospital", "Hospital SGCH");

        // Crear contexto de Thymeleaf
        Context context = new Context();
        context.setVariables(variables);

        // Generar HTML desde el template
        String html = templateEngine.process("receta-template", context);

        // Convertir HTML a PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, outputStream);

        return outputStream.toByteArray();
    }

    public String generarHtmlReceta(Receta receta) {
        // Preparar datos para el template
        Map<String, Object> variables = new HashMap<>();
        variables.put("receta", receta);
        variables.put("paciente", receta.getNotaMedica().getExpediente().getPaciente());
        variables.put("doctor", receta.getNotaMedica().getDoctor());
        variables.put("notaMedica", receta.getNotaMedica());
        variables.put("fechaFormateada", receta.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        variables.put("hospital", "Hospital SGCH");

        // Crear contexto de Thymeleaf
        Context context = new Context();
        context.setVariables(variables);

        // Generar HTML desde el template
        return templateEngine.process("receta-template", context);
    }
}
