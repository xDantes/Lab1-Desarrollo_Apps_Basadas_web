ADR-001 · Elección de Stack para Proyecto

Estado: Aceptada.  · Fecha: 04/08/2026. · Responsables: Derrek Adrián Ureña Solis y José Arrieta Sancho 

Contexto
Para realizar el proyecto de curso se recomienda utilizar un Stack (Java 21 · Spring Boot 3 · Gradle · PostgreSQL · MongoDB · JPA/Hibernate · React) 
la idea es analizar si utilizar estas herramientas o escoger otras equivalentes a las que se mencionan para la confección del proyecto.

Decisión
Aceptamos el Stack recomendado como la arquitectura para el desarrollo del sistema LescoCR.

Específicamente, utilizamos Spring Boot 3.3.2 con Gradle para construir una API REST, usamos una combinación de PostgreSQL para organizar la información 
estructurada (como usuarios, cursos, matrículas y pagos) junto con MongoDB para guardar las imágenes y categorías del diccionario de señas LESCO 
de forma más flexible; y finalmente usamos React para diseñar la pantalla e interfaz interactiva con la que interactúan los usuarios de la aplicación.

Alternativas consideradas
No se consideraron otras alternativas posibles.

Consecuencias
Positivas: Nuevos conocimientos en las tecnologías utilizadas y reforzar lo aprendido, por ejemplo, con JAVA  

Negativas: Una curva de aprendizaje mientras se trabaja en el proyecto que nos puede llevar a retrabajo en código por bugs en el desarrollo.

Referencias
