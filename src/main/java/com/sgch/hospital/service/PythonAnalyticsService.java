package com.sgch.hospital.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.sgch.hospital.model.entity.Cita;

@Service
public class PythonAnalyticsService {

    private static final String SCRIPT_RESOURCE_PATH = "python/analytics_script.py";
    private static final String TEMP_DATA_PATH = "temp_citas_data.csv";

    /**
     * Genera un gráfico usando Python a partir de los datos de citas.
     * 
     * @param citas Lista de citas a analizar
     * @param outputFileName Nombre del archivo PNG de salida
     * @param metric Tipo de métrica a generar (cancelaciones, citas_por_especialidad)
     * @return Ruta absoluta del archivo PNG generado
     * @throws IOException Si hay error de lectura/escritura
     * @throws InterruptedException Si el proceso Python es interrumpido
     */
    public String generarGrafico(List<Cita> citas, String outputFileName, String metric) 
            throws IOException, InterruptedException {
        
        // 1. Escribir los datos de Java a un archivo CSV temporal
        escribirDatosACsv(citas, TEMP_DATA_PATH);

        // 2. Extraer el script Python del classpath a un archivo temporal
        File scriptFile = extraerScriptPython();

        try {
            // 3. Detectar el comando Python correcto (Windows vs Linux/Mac)
            String pythonCommand = detectarComandoPython();
            System.out.println("Usando comando Python: " + pythonCommand);
            
            // 4. Construir el comando de ejecución para Python
            ProcessBuilder pb = new ProcessBuilder(
                pythonCommand,
                scriptFile.getAbsolutePath(),
                TEMP_DATA_PATH,
                outputFileName,
                metric
            );
            
            // Redirigir errores para debugging
            pb.redirectErrorStream(false);
            
            // 5. Ejecutar el script y esperar a que termine
            Process p = pb.start();
            int exitCode = p.waitFor();

            // 6. Capturar salida estándar y errores
            String output = new String(p.getInputStream().readAllBytes());
            String errorOutput = new String(p.getErrorStream().readAllBytes());

            if (exitCode != 0) {
                System.err.println("Salida estándar de Python:\n" + output);
                System.err.println("Errores de Python:\n" + errorOutput);
                throw new RuntimeException(
                    "Error al ejecutar el script de Python. Código: " + exitCode + 
                    ". Error: " + errorOutput
                );
            }
            
            System.out.println("Python output: " + output);
            
            // 7. Retornar la ruta de la imagen generada
            return new File(outputFileName).getAbsolutePath();

        } finally {
            // 8. Limpiar archivos temporales
            limpiarArchivosTemporal(scriptFile);
        }
    }

    /**
     * Detecta el comando Python correcto según el sistema operativo
     */
    private String detectarComandoPython() {
        String os = System.getProperty("os.name").toLowerCase();
        
        // En Windows, normalmente es "python"
        if (os.contains("win")) {
            return "python";
        }
        
        // En Linux/Mac, intentar python3 primero
        return "python3";
    }

    /**
     * Extrae el script Python del classpath a un archivo temporal.
     * Esto es necesario porque Python necesita leer el archivo desde el sistema de archivos.
     */
    private File extraerScriptPython() throws IOException {
        ClassPathResource resource = new ClassPathResource(SCRIPT_RESOURCE_PATH);
        
        if (!resource.exists()) {
            throw new IOException("No se encontró el script Python en: " + SCRIPT_RESOURCE_PATH);
        }

        // Crear archivo temporal
        Path tempScript = Files.createTempFile("analytics_script_", ".py");
        
        // Copiar contenido del recurso al archivo temporal
        try (InputStream is = resource.getInputStream()) {
            Files.copy(is, tempScript, StandardCopyOption.REPLACE_EXISTING);
        }

        File scriptFile = tempScript.toFile();
        scriptFile.deleteOnExit(); // Eliminar al cerrar la JVM
        
        return scriptFile;
    }

    /**
     * Escribe la lista de Entidades Cita a un archivo CSV.
     * 
     * @param citas Lista de citas (datos de la DB)
     * @param filename Nombre del archivo CSV temporal
     */
    private void escribirDatosACsv(List<Cita> citas, String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            // Cabecera del CSV
            pw.println("id_cita,estado,doctor_id,especialidad"); 
            
            // Escribir los datos
            for (Cita cita : citas) {
                String especialidadNombre = (cita.getDoctor() != null && cita.getDoctor().getEspecialidad() != null)
                    ? cita.getDoctor().getEspecialidad().getNombre()
                    : "Desconocida";
                    
                pw.printf("%d,%s,%d,%s%n", 
                    cita.getId(), 
                    cita.getEstado().name(), 
                    cita.getDoctor().getId(), 
                    especialidadNombre
                );
            }
        }
    }

    /**
     * Limpia archivos temporales generados
     */
    private void limpiarArchivosTemporal(File scriptFile) {
        try {
            // Eliminar script temporal
            if (scriptFile != null && scriptFile.exists()) {
                Files.deleteIfExists(scriptFile.toPath());
            }
            
            // Eliminar CSV temporal
            Files.deleteIfExists(Path.of(TEMP_DATA_PATH));
            
        } catch (IOException e) {
            System.err.println("No se pudo eliminar archivos temporales: " + e.getMessage());
        }
    }
}