# Proyecto-Programacion

## Descripción del Proyecto
Sistema de tienda online desarrollado íntegramente en Java puro, utilizando una arquitectura Cliente-Servidor mediante Sockets. El proyecto cumple con todos los requisitos del Hito 2, sin utilizar librerías externas ni bases de datos de terceros (persistencia mediante archivos CSV).

Backend

-javiwisiddi -->  Javiera Ortega

-JoseMIaau --> José Maureira

Frontend

-Straciastellarossa --> Antonia Medina

-Mauricio-S-Rojas --> Mauricio Rojas

Cooevaluación:

-José Maureira: 7.0

-Javiera Ortega: 6.9

-Antonia Medina: 6.8

-Mauri: 6.7

Requisitos del sistema

-Java Development Kit (JDK):** Versión 17 o superior (desarrollado y probado en JDK 21).

-Sistema Operativo:** Windows, Linux o macOS.

-Este programa no posee librerias externas, por lo que con las librerias nativas de java basta para ejecutarlo.

## Instrucciones de Ejecución 

### 1. Configuración de Red 
Dado que el sistema utiliza Sockets para conectar múltiples clientes, es necesario configurar la IP:
1. Abra el código fuente en su editor (ej. VS Code).
2. Diríjase al archivo `MainCliente.java`.
3. Busque la línea `Cliente cliente = new Cliente("192.168.0.236", 65432);` y reemplace la IP por la dirección IPv4 actual del computador que ejecutará el programa del Administrador(Por defecto si se ejecuta 'MainAdmin.java', en la terminal aparecera la ip).

### 2. Orden de Inicialización
Para evitar errores de conexión (Connection Refused), los programas deben iniciarse estrictamente en este orden:
1. **Ejecutar Primero:** `MainAdmin.java` (Servidor). La consola mostrará la confirmación de que el servidor está escuchando.
2. **Ejecutar Segundo:** `MainCliente.java` (Cliente). Puede ejecutar este archivo múltiples veces en distintos computadores para simular varios clientes armando carritos simultáneamente.

### Credenciales por defecto(para administracion): 

-Usuario: admin@supercurico.cl

-Contraseña: admin123
