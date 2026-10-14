USE inmuebles;

INSERT INTO propietario (id, nombre, correo_electronico) VALUES
(1, 'Carlos Pérez', 'carlos.perez@email.com'),
(2, 'María Gómez', 'maria.gomez@email.com'),
(3, 'Juan Rodríguez', 'juan.rodriguez@email.com'),
(4, 'Ana Martínez', 'ana.martinez@email.com'),
(5, 'Luis Fernández', 'luis.fernandez@email.com')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), correo_electronico = VALUES(correo_electronico);
