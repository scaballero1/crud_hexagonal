CREATE DATABASE IF NOT EXISTS inmuebles;
USE inmuebles;

CREATE TABLE IF NOT EXISTS propietario (
    id INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo_electronico VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS inmueble (
    chip VARCHAR(50) PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    fecha_compra DATE NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    id_propietario INT NOT NULL,
    numero_piso INT NULL,
    cantidad_pisos INT NULL,
    CONSTRAINT fk_inmueble_propietario FOREIGN KEY (id_propietario) REFERENCES propietario(id)
);

CREATE TABLE IF NOT EXISTS sotano (
    id INT AUTO_INCREMENT PRIMARY KEY,
    chip_casa VARCHAR(50) NOT NULL UNIQUE,
    area_m2 DOUBLE NOT NULL,
    tipo_uso VARCHAR(20) NOT NULL,
    CONSTRAINT fk_sotano_casa FOREIGN KEY (chip_casa) REFERENCES inmueble(chip) ON DELETE CASCADE
);
