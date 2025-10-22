package com.sgch.hospital.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sgch.hospital.model.entity.Cita;

@Service
public class PythonAnalyticsService {

    public String generarGraficoCancelaciones(List<Cita> citas, String outputFileName) throws IOException, InterruptedException {
        
        String dataPath = "temp_citas_data.csv";
        String scriptPath = "src/main/resources/python/analytics_script.py";
        
        // 1. Escribir los datos de Java a un archivo CSV para que Python pueda leerlos
        escribirDatosACsv(citas, dataPath);

        // 2. Construir el comando de ejecución para Python
        // NOTA: 'python3' o 'python' depende de tu entorno
        ProcessBuilder pb = new ProcessBuilder("python3", scriptPath, dataPath, outputFileName);
        
        // 3. Ejecutar el script y esperar a que termine
        Process p = pb.start();
        int exitCode = p.waitFor();

        if (exitCode != 0) {
            // Imprimir errores de Python para debug
            p.getErrorStream().transferTo(System.err);
            throw new RuntimeException("Error al ejecutar el script de Python. Código: " + exitCode);
        }
        
        // 4. Retornar la ruta de la imagen generada
        return new File(outputFileName).getAbsolutePath();
    }

    private void escribirDatosACsv(List<Cita> citas, String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            // Cabecera del CSV
            pw.println("id_cita,estado,doctor_id,paciente_id"); 
            
            // Escribir los datos
            for (Cita cita : citas) {
                pw.printf("%d,%s,%d,%d\n", 
                    cita.getId(), 
                    cita.getEstado().name(), 
                    cita.getDoctor().getId(), 
                    cita.getPaciente().getId()
                );
            }
        }
    }
}
