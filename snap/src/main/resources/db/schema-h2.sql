-- Eliminazione delle tabelle di Spring Batch con prefisso BATCH_
/*
DROP TABLE BATCH_STEP_EXECUTION_CONTEXT CASCADE CONSTRAINTS;
DROP TABLE BATCH_JOB_EXECUTION_CONTEXT CASCADE CONSTRAINTS;
DROP TABLE BATCH_STEP_EXECUTION CASCADE CONSTRAINTS;
DROP TABLE BATCH_JOB_EXECUTION_PARAMS CASCADE CONSTRAINTS;
DROP TABLE BATCH_JOB_EXECUTION CASCADE CONSTRAINTS;
DROP TABLE BATCH_JOB_INSTANCE CASCADE CONSTRAINTS;

-- Eliminazione delle sequenze
DROP SEQUENCE BATCH_STEP_EXECUTION_SEQ;
DROP SEQUENCE BATCH_JOB_EXECUTION_SEQ;
DROP SEQUENCE BATCH_JOB_SEQ;

-- Eliminazione delle tabelle si SNAP
DROP TABLE table_config;
*/

-- Creazione delle tabelle di Spring Batch con prefisso BATCH_
CREATE TABLE BATCH_JOB_INSTANCE (
    JOB_INSTANCE_ID NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    VERSION NUMBER,
    JOB_NAME VARCHAR2(100) NOT NULL,
    JOB_KEY VARCHAR2(2500),
    CONSTRAINT BATCH_JOB_INST_UN UNIQUE (JOB_NAME, JOB_KEY)
);

CREATE TABLE BATCH_JOB_EXECUTION (
    JOB_EXECUTION_ID NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    VERSION NUMBER,
    JOB_INSTANCE_ID NUMBER NOT NULL,
    CREATE_TIME TIMESTAMP NOT NULL,
    START_TIME TIMESTAMP,
    END_TIME TIMESTAMP,
    STATUS VARCHAR2(10),
    EXIT_CODE VARCHAR2(2500),
    EXIT_MESSAGE VARCHAR2(2500),
    LAST_UPDATED TIMESTAMP,
    JOB_CONFIGURATION_LOCATION VARCHAR2(2500),
    CONSTRAINT BATCH_JOB_INST_EXEC_FK FOREIGN KEY (JOB_INSTANCE_ID)
    REFERENCES BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
);

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS (
    JOB_EXECUTION_ID NUMBER NOT NULL,
    PARAMETER_NAME VARCHAR2(100) NOT NULL,
    PARAMETER_TYPE VARCHAR2(100) NOT NULL,
    PARAMETER_VALUE VARCHAR2(2500),
    IDENTIFYING CHAR(1) NOT NULL,
    CONSTRAINT BATCH_JOB_EXEC_PARAMS_FK FOREIGN KEY (JOB_EXECUTION_ID)
    REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE TABLE BATCH_STEP_EXECUTION (
    STEP_EXECUTION_ID NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    VERSION NUMBER NOT NULL,
    STEP_NAME VARCHAR2(100) NOT NULL,
    JOB_EXECUTION_ID NUMBER NOT NULL,
    START_TIME TIMESTAMP NOT NULL,
    END_TIME TIMESTAMP,
    STATUS VARCHAR2(10),
    COMMIT_COUNT NUMBER,
    READ_COUNT NUMBER,
    FILTER_COUNT NUMBER,
    WRITE_COUNT NUMBER,
    READ_SKIP_COUNT NUMBER,
    WRITE_SKIP_COUNT NUMBER,
    PROCESS_SKIP_COUNT NUMBER,
    ROLLBACK_COUNT NUMBER,
    EXIT_CODE VARCHAR2(2500),
    EXIT_MESSAGE VARCHAR2(2500),
    LAST_UPDATED TIMESTAMP,
    CONSTRAINT BATCH_JOB_EXEC_STEP_FK FOREIGN KEY (JOB_EXECUTION_ID)
    REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);
	
CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT (
    STEP_EXECUTION_ID NUMBER NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR2(2500) NOT NULL,
    SERIALIZED_CONTEXT CLOB,
    CONSTRAINT BATCH_STEP_EXEC_CTX_FK FOREIGN KEY (STEP_EXECUTION_ID)
    REFERENCES BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
);

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT (
    JOB_EXECUTION_ID NUMBER NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR2(2500) NOT NULL,
    SERIALIZED_CONTEXT CLOB,
    CONSTRAINT BATCH_JOB_EXEC_CTX_FK FOREIGN KEY (JOB_EXECUTION_ID)
    REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

-- Creazione delle sequenze
CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ;
CREATE SEQUENCE BATCH_JOB_SEQ;

-- Tabella di configurazione
CREATE TABLE table_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    source_schema VARCHAR(255) NOT NULL,
    source_table VARCHAR(255) NOT NULL,
    destination_schema VARCHAR(255) NOT NULL,
    destination_table VARCHAR(255) NOT NULL
);
