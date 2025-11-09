-- INSERTS SIMPLES (Garante que cada linha seja executada individualmente)
INSERT INTO TB_HDL_ZONA (NOME, TIPO) VALUES ('MOTTUPOP', 'Combustao');
INSERT INTO TB_HDL_ZONA (NOME, TIPO) VALUES ('MOTTUE', 'Eletrica');
INSERT INTO TB_HDL_ZONA (NOME, TIPO) VALUES ('MOTTUSPORT', 'Combustao');

-- O Flyway do Spring Boot geralmente faz COMMIT automático, mas é mais seguro incluir:
COMMIT;