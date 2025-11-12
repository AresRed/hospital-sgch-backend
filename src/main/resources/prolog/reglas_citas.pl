% =========================================================
% DECLARACIÓN DE DINÁMICOS (Hechos inyectados por Java)
% =========================================================
:- dynamic cita_reservada_temp/4.   % (ID_Doctor, Fecha_Str, HoraInicio_Str, Duracion_Min)
:- dynamic info_doctor/5.           % (ID_Doctor, Especialidad, Duracion_Min, HoraInicio_Str, HoraFin_Str)
:- dynamic bloqueo_horario_temp/5.  % (ID_Doctor, TipoBloqueo, Fecha_Str, HoraInicio_Str, HoraFin_Str)
                                    % TipoBloqueo = 'por_dia' | 'recurrente'

% =========================================================
% CONVERSIÓN DE TIEMPOS
% =========================================================

% time_to_minutes(+TiempoStr, -Minutos)
time_to_minutes(Tiempo, Minutos) :-
    ( atomic_list_concat([HorasStr, MinsStr], ':', Tiempo)
    -> true
    ;  split_string(Tiempo, ":", "", [HorasStrS, MinsStrS]),
       atom_string(HorasStr, HorasStrS),
       atom_string(MinsStr, MinsStrS)
    ),
    atom_number(HorasStr, Horas),
    atom_number(MinsStr, Mins),
    Minutos is Horas * 60 + Mins.

% minutes_to_time(+Minutos, -TiempoStr)
minutes_to_time(Minutos, Tiempo) :-
    Horas is Minutos // 60,
    Mins is Minutos mod 60,
    format(atom(Tiempo), '~|~`0t~d~2|:~|~`0t~d~2|', [Horas, Mins]).

% =========================================================
% REGLAS DE INTERVALOS
% =========================================================

% solapamiento(+Inicio1, +Fin1, +Inicio2, +Fin2)
solapamiento(MinInicio1, MinFin1, MinInicio2, MinFin2) :-
    MinInicio1 < MinFin2,
    MinFin1 > MinInicio2.

% =========================================================
% DETECCIÓN DE HORARIO OCUPADO
% =========================================================

horario_ocupado(ID_Doctor, Fecha, HoraCandidata, DuracionCita) :-
    time_to_minutes(HoraCandidata, CandidataMinInicio),
    CandidataMinFin is CandidataMinInicio + DuracionCita,
    (
        horario_ocupado_por_cita(ID_Doctor, Fecha, CandidataMinInicio, CandidataMinFin)
    ;
        horario_ocupado_por_bloqueo(ID_Doctor, Fecha, CandidataMinInicio, CandidataMinFin)
    ).

% ---------------------------------------------------------
% Citas reservadas
% ---------------------------------------------------------
horario_ocupado_por_cita(ID_Doctor, Fecha, CandidataMinInicio, CandidataMinFin) :-
    cita_reservada_temp(ID_Doctor, Fecha, CitaInicio, _),
    info_doctor(ID_Doctor, _, DuracionMinutos, _, _),
    time_to_minutes(CitaInicio, ReservadaMinInicio),
    ReservadaMinFin is ReservadaMinInicio + DuracionMinutos,
    solapamiento(CandidataMinInicio, CandidataMinFin, ReservadaMinInicio, ReservadaMinFin).

% ---------------------------------------------------------
% Bloqueos de horario (por día o recurrentes)
% ---------------------------------------------------------
horario_ocupado_por_bloqueo(ID_Doctor, Fecha, CandidataMinInicio, CandidataMinFin) :-
    bloqueo_horario_temp(ID_Doctor, TipoBloqueo, FechaBloqueo, HoraInicioBloqueo, HoraFinBloqueo),
    (TipoBloqueo = 'recurrente' ; (TipoBloqueo = 'por_dia', Fecha = FechaBloqueo)),
    time_to_minutes(HoraInicioBloqueo, BloqueoMinInicio),
    time_to_minutes(HoraFinBloqueo, BloqueoMinFin),
    solapamiento(CandidataMinInicio, CandidataMinFin, BloqueoMinInicio, BloqueoMinFin).

% =========================================================
% HORARIO DISPONIBLE (Regla principal)
% =========================================================

horario_optimo(ID_Doctor, _Especialidad, Fecha, HorarioDisponible) :-
    info_doctor(ID_Doctor, _, Duracion, HoraInicioStr, HoraFinStr),
    time_to_minutes(HoraInicioStr, MinInicio),
    time_to_minutes(HoraFinStr, MinFin),
    LimiteSuperior is MinFin - Duracion,
    between(MinInicio, LimiteSuperior, MinCandidato),
    0 is (MinCandidato - MinInicio) mod Duracion,
    minutes_to_time(MinCandidato, HoraCandidata),
    \+ horario_ocupado(ID_Doctor, Fecha, HoraCandidata, Duracion),
    HorarioDisponible = HoraCandidata.

% =========================================================
% EJEMPLOS DE HECHOS
% =========================================================
/*
% Info del doctor
info_doctor(1, 'Cardiología', 30, '08:00', '12:00').

% Citas reservadas
cita_reservada_temp(1, '2025-11-12', '09:00', 30).

% Bloqueos:
%   Caso 1: solo el 2025-11-13
bloqueo_horario_temp(1, 'por_dia', '2025-11-13', '10:00', '11:00').

%   Caso 2: recurrente todos los días
bloqueo_horario_temp(1, 'recurrente', '-', '08:00', '08:30').
*/
