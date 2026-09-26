# Proyecto-Progamacion
SuperCuricó - Sistema de Gestión de Supermercado
Backend
-javiwisiddi -->  Javiera Ortega
-JoseMIaau --> José Maureira
Frontend
-Straciastellarossa --> Antonia Medina
-Mauricio-S-Rojas --> Mauricio Rojas
Requisitos del sistema
-Java Development Kit (JDK):** Versión 17 o superior (desarrollado y probado en JDK 21).
-Sistema Operativo:** Windows, Linux o macOS.
-Este programa no posee librerias externas, por lo que con las librerias nativas de java basta para ejecutarlo.
Instrucciones de ejecucion:
-Doble clic sobre el archivo `SuperCurico.jar` o ejecutar desde la terminal escribiendo: java -jar SuperCurico.jar
-Si se desea recompilar el proyecto manualmente, entonces realizar los siguientes pasos en la terminal:
mkdir bin
javac -encoding UTF-8 -d bin src/modelo/*.java src/persistencia/*.java src/vista/*.java src/Main.java
Copy-Item -Recurse -Force src\imagenes bin\       esto es en linux/macos cp -r src/imagenes bin/
java -cp bin Main
Credenciales por defecto(para administracion): 
-Usuario: admin@supercurico.cl
-Contraseña: admin123