# src/main/resources/python/analytics_script.py

import sys
import pandas as pd
import matplotlib.pyplot as plt

# ----------------------------------------------------
# 1. PARTE IMPERATIVA: Leer argumentos de la línea de comandos
# ----------------------------------------------------
try:
    # El script espera dos argumentos: la ruta del CSV y el nombre del archivo de salida
    input_csv_path = sys.argv[1]
    output_png_path = sys.argv[2]
except IndexError:
    print("Error: Se requieren las rutas del archivo de entrada y salida.")
    sys.exit(1)

# ----------------------------------------------------
# 2. PARTE VECTORIAL/FUNCIONAL: Análisis de Datos con Pandas
# ----------------------------------------------------
try:
    # Cargar el archivo CSV en un DataFrame de Pandas (Transformación de Datos)
    df = pd.read_csv(input_csv_path)

    # ------------------------------------------------
    # EJEMPLO: Calcular la Tasa de Cancelación por Doctor
    # Usando operaciones funcionales/vectoriales (map, groupby)
    # ------------------------------------------------
    
    # 1. Creación de una columna binaria: Cancelada (Funcional/Vectorial)
    df['cancelada'] = df['estado'].apply(lambda x: 1 if x == 'CANCELADA' else 0)
    
    # 2. Agrupar y sumar (Vectorial/Funcional)
    # df.groupby().agg() es un ejemplo de función de orden superior.
    tasa_cancelacion = df.groupby('doctor_id').agg(
        total_citas=('id_cita', 'count'),
        total_canceladas=('cancelada', 'sum')
    )
    
    # 3. Cálculo de la tasa (Operación Vectorial)
    tasa_cancelacion['tasa'] = (tasa_cancelacion['total_canceladas'] / tasa_cancelacion['total_citas']) * 100
    
    # 4. Generar el gráfico
    plt.figure(figsize=(10, 6))
    tasa_cancelacion['tasa'].sort_values(ascending=False).plot(
        kind='bar', 
        color='skyblue'
    )
    plt.title('Tasa de Cancelación de Citas por Doctor (%)')
    plt.xlabel('ID del Doctor')
    plt.ylabel('Tasa de Cancelación (%)')
    plt.xticks(rotation=0)
    plt.tight_layout()
    
    # 5. Guardar el gráfico en la ruta especificada por Java
    plt.savefig(output_png_path)
    print(f"Gráfico guardado en: {output_png_path}")

except Exception as e:
    # Imprimir errores de Python a la salida estándar para que Java los capture
    print(f"Error en el script de análisis: {e}", file=sys.stderr)
    sys.exit(2)