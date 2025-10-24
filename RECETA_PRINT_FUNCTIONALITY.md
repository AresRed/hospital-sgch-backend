# 📋 **FUNCIONALIDAD DE IMPRESIÓN DE RECETAS**

## 🎯 **Descripción**
Se ha implementado la funcionalidad completa para que tanto doctores como pacientes puedan imprimir y visualizar recetas médicas en formato PDF y HTML.

## 🚀 **Nuevos Endpoints**

### **Para Doctores** (`/api/doctor/`)

#### 1. **Descargar PDF de Receta**
```
GET /api/doctor/receta/{recetaId}/pdf
```
- **Descripción**: Genera y descarga un PDF de la receta médica
- **Autenticación**: Requiere token JWT de DOCTOR o ADMINISTRADOR
- **Respuesta**: Archivo PDF para descarga
- **Headers**: `Content-Type: application/pdf`, `Content-Disposition: attachment`

#### 2. **Ver Receta en HTML**
```
GET /api/doctor/receta/{recetaId}/html
```
- **Descripción**: Muestra la receta en formato HTML en el navegador
- **Autenticación**: Requiere token JWT de DOCTOR o ADMINISTRADOR
- **Respuesta**: HTML renderizado de la receta

### **Para Pacientes** (`/api/paciente/`)

#### 1. **Descargar Mi Receta PDF**
```
GET /api/paciente/receta/{recetaId}/pdf
```
- **Descripción**: Descarga un PDF de la receta del paciente autenticado
- **Autenticación**: Requiere token JWT de PACIENTE
- **Respuesta**: Archivo PDF para descarga
- **Validación**: Solo puede acceder a sus propias recetas

#### 2. **Ver Mi Receta en HTML**
```
GET /api/paciente/receta/{recetaId}/html
```
- **Descripción**: Visualiza la receta en formato HTML
- **Autenticación**: Requiere token JWT de PACIENTE
- **Respuesta**: HTML renderizado de la receta

#### 3. **Listar Todas Mis Recetas**
```
GET /api/paciente/recetas
```
- **Descripción**: Obtiene todas las recetas del paciente autenticado
- **Autenticación**: Requiere token JWT de PACIENTE
- **Respuesta**: Lista de recetas ordenadas por fecha (más reciente primero)

## 🎨 **Características del Diseño**

### **Template de Receta**
- **Header**: Logo del hospital, título del documento, fecha
- **Datos del Paciente**: Nombre, DNI, teléfono, seguro médico
- **Datos del Doctor**: Nombre, especialidad, CMP
- **Diagnóstico**: Campo destacado con fondo amarillo
- **Medicamentos**: Tabla con medicamento, dosis e instrucciones
- **Recomendaciones**: Campo destacado con fondo azul
- **Firma**: Espacio para firma del médico
- **Footer**: Información del hospital y validez de la receta

### **Estilos CSS**
- Diseño profesional y limpio
- Colores corporativos del hospital
- Responsive para impresión
- Tablas con bordes y colores alternados
- Secciones destacadas con colores de fondo

## 🔧 **Tecnologías Utilizadas**

### **Dependencias Agregadas**
```xml
<!-- Generación de PDFs -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
    <type>pom</type>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>html2pdf</artifactId>
    <version>4.0.5</version>
</dependency>

<!-- Templates HTML -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

### **Servicios Creados**
- **`RecetaPdfService`**: Genera PDFs y HTML desde templates
- **`ThymeleafConfig`**: Configuración de templates

## 📝 **Ejemplos de Uso**

### **1. Doctor descarga PDF de receta**
```bash
curl -X GET \
  "http://localhost:8080/api/doctor/receta/1/pdf" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: application/pdf" \
  --output receta.pdf
```

### **2. Paciente ve su receta en HTML**
```bash
curl -X GET \
  "http://localhost:8080/api/paciente/receta/1/html" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: text/html"
```

### **3. Paciente lista todas sus recetas**
```bash
curl -X GET \
  "http://localhost:8080/api/paciente/recetas" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: application/json"
```

## 🔒 **Seguridad**

### **Validaciones Implementadas**
- **Autenticación JWT**: Todos los endpoints requieren token válido
- **Autorización por Roles**: Doctores y pacientes solo acceden a sus datos
- **Validación de Propiedad**: 
  - Doctores solo pueden imprimir recetas que ellos crearon
  - Pacientes solo pueden acceder a sus propias recetas
- **Validación de Existencia**: Verificación de que la receta existe antes de procesar

### **Headers de Seguridad**
- `Content-Type` apropiado para cada tipo de respuesta
- `Content-Disposition` para descargas seguras
- Manejo de errores sin exposición de información sensible

## 🎯 **Casos de Uso**

### **Flujo Completo**
1. **Doctor finaliza cita** → Crea nota médica y receta
2. **Doctor puede imprimir** → Descarga PDF o ve HTML de la receta
3. **Paciente consulta plataforma** → Ve lista de sus recetas
4. **Paciente descarga receta** → Obtiene PDF para farmacia
5. **Paciente imprime desde casa** → Ve HTML y usa "Imprimir" del navegador

### **Beneficios**
- ✅ **Acceso inmediato**: Recetas disponibles al instante
- ✅ **Formato profesional**: Diseño médico estándar
- ✅ **Seguridad**: Solo acceso autorizado
- ✅ **Flexibilidad**: PDF para farmacia, HTML para visualización
- ✅ **Historial completo**: Pacientes pueden ver todas sus recetas

## 🚀 **Próximos Pasos Sugeridos**

1. **Notificaciones por Email**: Enviar receta por correo al paciente
2. **Código QR**: Agregar código QR para validación en farmacia
3. **Firma Digital**: Implementar firma digital del médico
4. **Plantillas Personalizables**: Permitir personalizar el diseño por especialidad
5. **Integración con Farmacias**: API para validar recetas

---

**¡La funcionalidad está lista para usar!** 🎉
