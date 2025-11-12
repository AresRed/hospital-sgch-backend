import sys
import pandas as pd
import matplotlib.pyplot as plt

# ----------------------------------------------------
# 1. PARTE IMPERATIVA: Lectura de Argumentos
# El script espera 3 argumentos de Java: [CSV_IN], [PNG_OUT], [METRIC]
# ----------------------------------------------------
try:
    input_csv_path = sys.argv[1]
    output_png_path = sys.argv[2]
    metric = sys.argv[3]
except IndexError:
    print("Error: Se requieren las rutas del archivo de entrada, salida y la métrica.", file=sys.stderr)
    sys.exit(1)

# ----------------------------------------------------
# 2. CARGA Y PREPARACIÓN DE DATOS
# ----------------------------------------------------
try:
    # Cargar el archivo CSV en un DataFrame (Operación Vectorial)
    df = pd.read_csv(input_csv_path)
    
    # 3. LÓGICA CONDICIONAL Y ANÁLISIS (PARTE FUNCIONAL/VECTORIAL)
    plt.figure(figsize=(10, 6))
    
    # --- MÉTRICA 1: Tasa de Cancelación por Doctor ---
    if metric == 'cancelaciones':
        
        # 1. Función para mapear estados (Uso de lambda: Programación Funcional)
        df['cancelada'] = df['estado'].apply(lambda x: 1 if x == 'CANCELADA' else 0)
        
        # 2. Agrupación y agregación (Operaciones Vectoriales/Funcionales)
        resumen = df.groupby('doctor_id').agg(
            total_citas=('id_cita', 'count'),
            total_canceladas=('cancelada', 'sum')
        )
        
        # 3. Cálculo de la tasa
        resumen['tasa'] = (resumen['total_canceladas'] / resumen['total_citas']) * 100
        
        # 4. Generación del gráfico
        resumen['tasa'].sort_values(ascending=False).plot(kind='bar', color='salmon')
        plt.title('Tasa de Cancelación de Citas por Doctor (%)')
        plt.xlabel('ID del Doctor')
        plt.ylabel('Tasa de Cancelación (%)')
        plt.xticks(rotation=0)

    # --- MÉTRICA 2: Citas Atendidas por Especialidad ---
    elif metric == 'citas_por_especialidad':
        
        # 1. Filtrar solo citas REALIZADAS (Estado Confirmada/Realizada, etc.)
        df_realizadas = df[df['estado'] == 'REALIZADA']
        
        # 2. Agrupación por especialidad y conteo
        conteo = df_realizadas.groupby('especialidad')['id_cita'].count().sort_values(ascending=False)
        
        # 3. Generación del gráfico (Ejemplo: Gráfico de Barras)
        conteo.plot(kind='barh', color='teal')
        plt.title('Citas Finalizadas por Especialidad')
        plt.xlabel('Número de Citas')
        plt.ylabel('Especialidad')

    else:
        print(f"Métrica desconocida: {metric}", file=sys.stderr)
        sys.exit(3)

    # 4. GUARDAR EL GRÁFICO (El backend leerá este archivo)
    plt.tight_layout()
    plt.savefig(output_png_path)
    
    print(f"Gráfico {metric} generado en: {output_png_path}")

except Exception as e:
    print(f"Error fatal durante el análisis de datos: {e}", file=sys.stderr)
    sys.exit(2)