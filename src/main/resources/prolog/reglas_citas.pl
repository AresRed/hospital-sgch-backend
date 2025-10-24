% =========================================================
% DECLARACIÓN DE DINÁMICOS (Hechos inyectados por Java)
% =========================================================

% Declaramos que estos predicados serán modificados por Java (assertz/retractall).

:- dynamic cita_reservada_temp/4.  % (ID_Doctor, Fecha_Str, HoraInicio_Str, Duracion_Min)
:- dynamic info_doctor/5.          % (ID_Doctor, Especialidad, Duracion_Min, HoraInicio_Str, HoraFin_Str)


% =========================================================
% REGLAS DE TIEMPO (Auxiliares para la conversión de HH:MM)
% =========================================================

% time_to_minutes(+TiempoStr, -Minutos)
time_to_minutes(Tiempo, Minutos) :- 
    atom_chars(Tiempo, [H1, H2, ':', M1, M2]), 
    number_chars(Horas, [H1, H2]), 
    number_chars(Mins, [M1, M2]), 
    Minutos is Horas * 60 + Mins.

% minutes_to_time(+Minutos, -TiempoStr)
minutes_to_time(Minutos, Tiempo) :- 
    Horas is Minutos // 60, 
    Mins is Minutos mod 60,
    % Formatea a dos dígitos (ej: 09:05)
    format(atom(Tiempo), '~`0t~d~2|:~`0t~d~2|', [Horas, Mins]).


% =========================================================
% REGLAS DE INTERVALOS Y OCUPACIÓN (Restricciones)
% =========================================================

% horario_ocupado(+ID_Doctor, +FechaCandidata, +HoraCandidata, +DuracionCita)
% Verifica si una hora candidata se solapa con cualquier cita ya reservada (hecho dinámico).
horario_ocupado(ID_Doctor, Fecha, HoraCandidata, DuracionCita) :-
    % 1. Buscar una cita ya reservada en esa Fecha (usando el predicado dinámico)
    cita_reservada_temp(ID_Doctor, Fecha, CitaInicio, _DuracionReservada),
    
    % 2. Calcular el Fin de la cita reservada (usando la duración del doctor)
    time_to_minutes(CitaInicio, ReservadaMinInicio),
    info_doctor(ID_Doctor, _, DuracionMinutos, _, _),
    ReservadaMinFin is ReservadaMinInicio + DuracionMinutos,
    
    % 3. Determinar el intervalo de la hora candidata
    time_to_minutes(HoraCandidata, CandidataMinInicio),
    CandidataMinFin is CandidataMinInicio + DuracionCita,
    
    % 4. Comprobar la superposición (Lógica de Intersección de Intervalos)
    (
        CandidataMinInicio < ReservadaMinFin,  % El inicio de la nueva cita choca con el fin de la reservada
        CandidataMinFin > ReservadaMinInicio  % El fin de la nueva cita choca con el inicio de la reservada
    ).


% =========================================================
% REGLA PRINCIPAL: BÚSQUEDA DE HUECOS LIBRES
% =========================================================

% horario_optimo(+ID_Doctor, +Especialidad, +Fecha, -HorarioDisponible)
% Genera y verifica todos los candidatos usando el incremento de la Duración.
horario_optimo(ID_Doctor, _Especialidad, Fecha, HorarioDisponible) :-
    
    % 1. Obtener límites y duración del doctor (hecho inyectado por Java)
    info_doctor(ID_Doctor, _, Duracion, HoraInicioStr, HoraFinStr),
    
    % 2. Convertir límites a minutos
    time_to_minutes(HoraInicioStr, MinInicio),
    time_to_minutes(HoraFinStr, MinFin),
    
    % 3. Generar una secuencia de horas candidatas en pasos de "Duracion"
    % El rango superior garantiza que la cita quepa completamente.
    LimiteSuperior is MinFin - Duracion, 
    
    % Generamos el candidato en minutos con el incremento de Duracion
    between(MinInicio, LimiteSuperior, MinCandidato),
    0 is (MinCandidato - MinInicio) mod Duracion, % Asegura que el candidato cae en un paso múltiplo de Duracion

    % 4. Convertir el candidato a formato HH:MM
    minutes_to_time(MinCandidato, HoraCandidata),

    % 5. Verificar que el horario NO esté ocupado (Negación por Falla: \+!)
    \+ horario_ocupado(ID_Doctor, Fecha, HoraCandidata, Duracion),
    
    % 6. Unificar la salida
    HorarioDisponible = HoraCandidata.---