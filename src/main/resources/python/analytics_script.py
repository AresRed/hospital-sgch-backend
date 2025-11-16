import sys
import pandas as pd
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

# Configuración
plt.rcParams['figure.facecolor'] = 'white'
plt.rcParams['axes.facecolor'] = '#f8f9fa'

# Leer argumentos
try:
    input_csv = sys.argv[1]
    output_png = sys.argv[2]
    metric = sys.argv[3]
except IndexError:
    print("Error: Se requieren 3 argumentos", file=sys.stderr)
    sys.exit(1)

try:
    # Cargar datos
    df = pd.read_csv(input_csv)
    
    if df.empty:
        print("Error: CSV vacío", file=sys.stderr)
        sys.exit(2)
    
    plt.figure(figsize=(12, 7))
    
    if metric == 'cancelaciones':
        # Cancelaciones por doctor
        df['cancelada'] = df['estado'].apply(lambda x: 1 if x == 'CANCELADA' else 0)
        resumen = df.groupby('doctor_id').agg(
            total=('id_cita', 'count'),
            canceladas=('cancelada', 'sum')
        )
        resumen = resumen[resumen['total'] > 0]
        
        if resumen.empty:
            print("Error: No hay datos", file=sys.stderr)
            sys.exit(2)
        
        resumen['tasa'] = (resumen['canceladas'] / resumen['total']) * 100
        resumen_sorted = resumen['tasa'].sort_values(ascending=False)
        
        ax = resumen_sorted.plot(kind='bar', color='#e74c3c', edgecolor='white')
        plt.title('Tasa de Cancelación por Doctor', fontsize=16, fontweight='bold', pad=20)
        plt.xlabel('ID del Doctor', fontsize=12)
        plt.ylabel('Tasa de Cancelación (%)', fontsize=12)
        plt.xticks(rotation=45)
        
        for i, v in enumerate(resumen_sorted):
            ax.text(i, v + 1, f'{v:.1f}%', ha='center', va='bottom', fontweight='bold')
        
        plt.grid(axis='y', alpha=0.3)
        plt.tight_layout()
        
    elif metric == 'citas_por_especialidad':
        # Citas por especialidad
        df_realizadas = df[df['estado'] == 'REALIZADA']
        
        if df_realizadas.empty:
            print("Error: No hay citas realizadas", file=sys.stderr)
            sys.exit(2)
        
        conteo = df_realizadas.groupby('especialidad')['id_cita'].count().sort_values(ascending=True)
        
        colors = ['#3498db', '#2ecc71', '#f39c12', '#e74c3c', '#9b59b6', '#1abc9c', '#34495e', '#16a085']
        colors_final = (colors * 10)[:len(conteo)]
        
        ax = conteo.plot(kind='barh', color=colors_final, edgecolor='white')
        plt.title('Citas Finalizadas por Especialidad', fontsize=16, fontweight='bold', pad=20)
        plt.xlabel('Número de Citas', fontsize=12)
        plt.ylabel('Especialidad', fontsize=12)
        
        for i, v in enumerate(conteo):
            ax.text(v + 0.5, i, str(v), ha='left', va='center', fontweight='bold')
        
        plt.grid(axis='x', alpha=0.3)
        plt.tight_layout()
    else:
        print(f"Error: Métrica '{metric}' desconocida", file=sys.stderr)
        sys.exit(3)
    
    plt.savefig(output_png, dpi=150, bbox_inches='tight', facecolor='white')
    print("Grafico generado: " + output_png)
    
except Exception as e:
    print(f"Error: {e}", file=sys.stderr)
    import traceback
    traceback.print_exc(file=sys.stderr)
    sys.exit(2)