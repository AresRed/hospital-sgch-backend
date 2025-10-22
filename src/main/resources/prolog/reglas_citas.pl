% =========================================================
% DECLARACIÓN DE DINÁMICOS
% =========================================================
% Declaramos que estos predicados pueden ser modificados (assertz/retractall) 
% por el código Java en tiempo de ejecución.

:- dynamic cita_temporal/4.
:- dynamic doctor_temporal/5.

% doctor_temporal(ID, Especialidad, DuracionCita_Minutos, HoraInicio_Str, HoraFin_Str).
% cita_temporal(DoctorID, Fecha_Str, HoraInicio_Str, Duracion_Minutos).


% =========================================================
% REGLAS DE TIEMPO (Auxiliares)
% =========================================================

% time_to_minutes(+TimeStr, -Minutes)
% Convierte una hora HH:MM a minutos desde medianoche.
time_to_minutes(Time, Minutes) :- 
    atom_chars(Time, [H1, H2, ':', M1, M2]), 
    number_chars(Hours, [H1, H2]), 
    number_chars(Mins, [M1, M2]), 
    Minutes is Hours * 60 + Mins.

% minutes_to_time(+Minutes, -TimeStr)
% Convierte minutos desde medianoche a formato HH:MM.
minutes_to_time(Minutes, Time) :- 
    Hours is Minutes // 60, 
    Mins is Minutes mod 60,
    % El formato garantiza que sea de dos dígitos (ej: 09:05)
    format(atom(Time), '~`0t~d~2|:~`0t~d~2|', [Hours, Mins]).


% generar_candidato_en_pasos(+Actual, +Limite, +Paso, -Candidato)
% Predicado recursivo que genera valores desde 'Actual' hasta 'Limite' en incrementos de 'Paso'.
% Es más eficiente que between/3 + mod/2 porque no genera valores intermedios.

% Caso base: El valor actual es un candidato válido si no supera el límite.
generar_candidato_en_pasos(Actual, Limite, _, Actual) :- 
    Actual =< Limite.

% Caso recursivo: Genera el siguiente candidato y continúa la búsqueda.
generar_candidato_en_pasos(Actual, Limite, Paso, Candidato) :-
    Actual =< Limite,
    Siguiente is Actual + Paso,
    generar_candidato_en_pasos(Siguiente, Limite, Paso, Candidato).

% =========================================================
% REGLAS DE INTERVALOS Y OCUPACIÓN
% =========================================================

% intervalo_cita(+DoctorID, +Fecha, +HoraInicio, -HoraFin)
% Calcula la hora de fin de una cita basándose en la duración del doctor.
intervalo_cita(DoctorID, _Fecha, HoraInicio, HoraFin) :-
    % Obtener la duración de la cita del doctor (del hecho temporal)
    doctor_temporal(DoctorID, _, DuracionMinutos, _, _),
    
    time_to_minutes(HoraInicio, MinutosInicio),
    
    MinutosFin is MinutosInicio + DuracionMinutos,
    
    minutes_to_time(MinutosFin, HoraFin).


% horario_ocupado(+DoctorID, +FechaCandidata, +HoraCandidata, +DuracionCita)
% Verifica si una hora candidata se solapa con una cita ya reservada.
horario_ocupado(DoctorID, Fecha, HoraCandidata, DuracionCita) :-
    % 1. Buscar una cita ya reservada en esa Fecha (usando el predicado dinámico)
    cita_temporal(DoctorID, Fecha, CitaInicio, _DuracionReservada),
    
    % 2. Calcular el intervalo de la cita reservada
    intervalo_cita(DoctorID, Fecha, CitaInicio, CitaFin),
    
    % 3. Determinar los límites de tiempo en minutos
    time_to_minutes(HoraCandidata, CandidataMinInicio),
    CandidataMinFin is CandidataMinInicio + DuracionCita,
    
    time_to_minutes(CitaInicio, ReservadaMinInicio),
    time_to_minutes(CitaFin, ReservadaMinFin),
    
    % 4. Comprobar la superposición (intersección de intervalos)
    (
        % El inicio de la candidata está antes del fin de la reservada
        CandidataMinInicio < ReservadaMinFin,
        % Y el fin de la candidata está después del inicio de la reservada
        CandidataMinFin > ReservadaMinInicio
    ).


% =========================================================
% REGLA PRINCIPAL: BÚSQUEDA DE HORARIO
% =========================================================

% horario_optimo(+DoctorID, +Especialidad, +FechaBase, -HorarioDisponible)
% Usa backtracking para encontrar el primer horario disponible que satisfaga todas las restricciones.
horario_optimo(DoctorID, Especialidad, FechaBase, HorarioDisponible) :-
    
    % 1. Obtener límites y duración del doctor (hecho inyectado por Java)
    doctor_temporal(DoctorID, Especialidad, Duracion, HoraInicioStr, HoraFinStr),
    
    % 2. Convertir límites a minutos
    time_to_minutes(HoraInicioStr, MinInicio),
    time_to_minutes(HoraFinStr, MinFin),
    
    % 3. Generar una secuencia de horas candidatas
    % Se debe calcular el límite superior ANTES de llamar a between/3.
    LimiteSuperior is MinFin - Duracion,
    generar_candidato_en_pasos(MinInicio, LimiteSuperior, Duracion, MinCandidato),
    
    % 4. Convertir el candidato a formato HH:MM
    minutes_to_time(MinCandidato, HoraCandidata),

    % 5. Verificar que el horario NO esté ocupado (¡Uso de Negación por Falla: \+!)
    % Si 'horario_ocupado' falla (no hay hechos que lo hagan verdadero), el horario es libre.
    \+ horario_ocupado(DoctorID, FechaBase, HoraCandidata, Duracion),
    
    % 6. Unificar la variable de salida (HorarioDisponible) con la HoraCandidata encontrada
    HorarioDisponible = HoraCandidata.