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

    private static final String SCRIPT_PATH = "src/main/resources/python/analytics_script.py";
    private static final String TEMP_DATA_PATH = "temp_citas_data.csv";

    public String generarGrafico(List<Cita> citas, String outputFileName, String metric) 
            throws IOException, InterruptedException {
        
        // 1. Escribir los datos de Java a un archivo CSV
        escribirDatosACsv(citas, TEMP_DATA_PATH);

        // 2. Construir el comando de ejecución para Python
        // Pasamos 3 argumentos al script: [CSV_IN], [PNG_OUT], [METRIC]
        ProcessBuilder pb = new ProcessBuilder(
            "python3", // O 'python', dependiendo de tu instalación
            SCRIPT_PATH, 
            TEMP_DATA_PATH, 
            outputFileName,
            metric // <-- El parámetro que le dice a Python qué hacer
        );
        
        // 3. Ejecutar el script y esperar a que termine
        Process p = pb.start();
        int exitCode = p.waitFor();

        if (exitCode != 0) {
            // Capturar y mostrar errores de ejecución de Python
            String errorOutput = new String(p.getErrorStream().readAllBytes());
            System.err.println("Error en la salida del script Python:\n" + errorOutput);
            throw new RuntimeException("Error al ejecutar el script de Python. Código: " + exitCode);
        }
        
        // 4. Retornar la ruta de la imagen generada
        return new File(outputFileName).getAbsolutePath();
    }

    /**
     * Método auxiliar: Escribe la lista de Entidades Cita a un archivo CSV.
     * @param citas Lista de citas (datos de la DB).
     * @param filename Nombre del archivo CSV temporal.
     */
    private void escribirDatosACsv(List<Cita> citas, String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            // Cabecera del CSV: Es crucial para Pandas
            pw.println("id_cita,estado,doctor_id,especialidad"); 
            
            // Escribir los datos
            for (Cita cita : citas) {
                // Asegurarse de que el objeto Doctor no sea null y obtener la especialidad de la entidad.
                String especialidadNombre = (cita.getDoctor() != null && cita.getDoctor().getEspecialidad() != null)
                    ? cita.getDoctor().getEspecialidad().getNombre() // Asumiendo que Doctor ya usa la entidad Especialidad
                    : "Desconocida";
                    
                pw.printf("%d,%s,%d,%s\n", 
                    cita.getId(), 
                    cita.getEstado().name(), 
                    cita.getDoctor().getId(), 
                    especialidadNombre
                );
            }
        }
    }
    

}
