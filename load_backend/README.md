Divisor de Archivos - Backend

Este proyecto es el backend del sistema para dividir, 
descargar, enviar y reconstruir archivos. Utiliza Java 17, 
Spring Boot y una arquitectura limpia con puertos y adaptadores.

Requisitos previos

    Java 17+
    Gradle 7+
    Git 

 Ejecutar el proyecto
    
    Clona el repositorio:

        git clone https://github.com/tu-usuario/divisor-backend.git
         cd divisor-backend

Ejecuta el proyecto:

    ./gradlew bootRun

    estara activo en http://localhost:8080

Configuración del correo (opcional)

    Si deseas probar el envío de correos, edita src/main/resources/application.
    properties con tus datos:

        spring.mail.host=smtp.gmail.com
        spring.mail.port=587
        spring.mail.username=tu_correo@gmail.com
        spring.mail.password=tu_contraseña_segura_o_app_password
        spring.mail.properties.mail.smtp.auth=true
        spring.mail.properties.mail.smtp.starttls.enable=true