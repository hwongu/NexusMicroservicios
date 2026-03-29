# 🧾 Proyecto Nexus – Sistema de Gestión de Almacén (Arquitectura de Microservicios)

Este repositorio contiene el código fuente, la configuración de la base de datos y la infraestructura para el sistema **Nexus** en su versión de una **Arquitectura de Microservicios**. Este proyecto ha sido diseñado como material educativo y de ejemplo para clases universitarias, demostrando cómo construir una aplicación completa (Full-Stack) utilizando tecnologías puras y estándares de la industria, sin depender inicialmente de frameworks pesados en el backend.

El sistema implementa:
* **Mantenimientos (CRUD) Desacoplados:** Gestión de Usuarios (Seguridad) y Catálogo (Productos y Categorías) en servicios independientes.
* **Transacciones:** Registro de ingresos a almacén consumiendo servicios de catálogo y seguridad de forma interna.
* **Enrutamiento Centralizado:** Un API Gateway que actúa como punto de entrada único para las peticiones del frontend.

---

## 📁 Estructura del Repositorio

El proyecto está dividido en cinco grandes módulos principales:

### 🗄️ 1_DataBase (Persistencia Efímera)
Contiene la infraestructura de datos dockerizada para el ambiente de desarrollo.
* Aloja un archivo `docker-compose.yml` diseñado con el patrón *Database per Service*. Levanta tres instancias aisladas de **PostgreSQL 18** (Seguridad, Catálogo e Ingresos) y ejecuta los scripts SQL de inicialización. 

### ☕ 2_BackEnd (Lógica de Negocio y Ecosistema Spring)
Contiene el núcleo del sistema dividido en servicios independientes construidos con **Java 21** y **Spring Boot 4.0.5**.
* **NexusSeguridadMs:** Microservicio encargado del IAM y gestión de usuarios.
* **NexusCatalogoMs:** Microservicio encargado de los productos y categorías.
* **NexusIngresoMs:** Microservicio de operaciones transaccionales.
* **NexusGatewayMs:** API Gateway construido con Spring Cloud Gateway para centralizar el enrutamiento.

### 💻 3_FrontEnd (Interfaz de Usuario)
Contiene la aplicación cliente con la que interactúan los usuarios finales.
* **NexusFrontEnd:** Proyecto Single Page Application (SPA) desarrollado en **Angular 19**. Incluye scripts `.bat` personalizados para facilitar la compilación y generación de instaladores en entornos Windows.

### 🐳 4_Infraestructure (Despliegue Full-Stack)
Contiene los directorios de destino donde se unifican todos los artefactos para levantar la infraestructura completa (Bases de Datos, Backend y Frontend) mediante Docker Compose.
* **NexusInfraestructura\backend:** Contiene subcarpetas específicas (`deploy_catalogo`, `deploy_seguridad`, `deploy_ingresos`, `deploy_gateway`) para alojar los `.jar` de cada servicio.
* **NexusInfraestructura\frontend:** Carpeta de destino para los archivos estáticos compilados del frontend en Angular.

### 🧪 5_Test (Pruebas de API)
Contiene las colecciones y entornos preconfigurados para validar los endpoints del ecosistema de manera independiente.
* **Postman:** Directorio que aloja los archivos JSON exportados listos para ser importados en tu cliente HTTP.

---

## ⚠️ Reglas de Oro (Para despliegue)

Para garantizar que el entorno local funcione correctamente, el orden de ejecución es fundamental:

1.  **La Base de Datos manda (en desarrollo):** Los contenedores de las bases de datos deben estar encendidos antes de intentar levantar o probar los microservicios individualmente desde tu IDE.
2.  **Aislamiento de Entornos:** No puedes tener levantada la base de datos de la carpeta `1_DataBase` al mismo tiempo que intentas levantar toda la infraestructura desde `4_Infraestructure`, ya que habrá colisión de puertos.
3.  **Construcción independiente:** Asegúrate de compilar cada uno de los 4 proyectos del backend (generando sus respectivos `.jar`) y el frontend (generando el directorio `dist`) antes de intentar el despliegue final.

---

## 🚀 Guía de Ejecución Paso a Paso

### 🗄️ Fase 0: Levantar las Bases de Datos (Ambiente de Desarrollo)
El entorno de datos debe ser lo primero en inicializarse para probar tu código localmente.
1. Navega a la ruta: `1_DataBase`
2. Levanta las bases de datos y ejecuta los scripts con el comando:
   `docker-compose up -d`
3. Cuando termines de trabajar en desarrollo, y **especialmente antes de pasar a la Fase 3**, asegúrate de detener y destruir este entorno ejecutando:
   `docker-compose down -v`

### ☕ Fase 1: Compilar el Ecosistema Backend (Java 21 + Spring Boot)
Preparamos los microservicios y el Gateway.
1. Navega a la ruta: `2_BackEnd`
2. Entra a la carpeta de cada uno de los 4 proyectos (`NexusCatalogoMs`, `NexusSeguridadMs`, `NexusIngresoMs` y `NexusGatewayMs`) y genera el empaquetado ejecutando en cada uno:
   `mvn clean install`
3. Esto generará un archivo `.jar` dentro de la carpeta `target/` de cada proyecto (ej. `NexusCatalogoMs-0.0.1-SNAPSHOT.jar`).

### 💻 Fase 2: Compilar el Frontend (Angular 19)
Preparamos la interfaz de usuario para producción.
1. Navega a la ruta: `3_FrontEnd\NexusFrontEnd`
2. Para desarrollo o pruebas de compilación rápida, puedes usar:
   `compilar.bat`
3. Para generar la versión final optimizada, ejecuta:
   `generar-instalador-produccion.bat`
4. Esto creará una carpeta `dist/`. Entra allí e identifica el contenido dentro de la subcarpeta `NexusFrontEnd/browser`.

### 🐳 Fase 3: Preparar la Infraestructura Completa (Despliegue Total)
**⚠️ Importante:** Este paso es únicamente si deseas levantar *todo* el ecosistema (DBs + Microservicios + Frontend) en conjunto. La base de datos levantada en la **Fase 0** debe estar **detenida y destruida** (`docker-compose down -v`).

1. **Para el Backend:** Copia los archivos `.jar` obtenidos en la Fase 1 y pégalos en sus rutas respectivas dentro de `4_Infraestructure\NexusInfraestructura\backend`:
   * El `.jar` de Catálogo en la carpeta `deploy_catalogo`
   * El `.jar` de Seguridad en la carpeta `deploy_seguridad`
   * El `.jar` de Ingresos en la carpeta `deploy_ingresos`
   * El `.jar` del Gateway en la carpeta `deploy_gateway`
2. **Para el Frontend:** Copia **todo el contenido** que está dentro de `dist/NexusFrontEnd/browser` (obtenido en la Fase 2) y pégalo dentro de la carpeta `4_Infraestructure\NexusInfraestructura\frontend`.
3. **Levantar el Sistema:** Navega a la carpeta raíz de la infraestructura y ejecuta `docker-compose up -d`. Todo el sistema estará orquestado e interconectado.

---

## 🧪 Pruebas del API con Postman

Para facilitar la validación del ecosistema backend de forma aislada, se han incluido scripts de prueba parametrizados. La colección está dividida en dos partes: acceso directo a los microservicios y acceso unificado a través del API Gateway.

1. Navega a la ruta: `5_Test\Postman`.
2. Abre tu herramienta **Postman** (o Insomnia).
3. Utiliza la opción **Import** y selecciona los dos archivos incluidos:
   * `NexusMicroservicios.postman_collection.json` (Contiene las colecciones de peticiones).
   * `NexusMicroservicios_Enviroment.postman_environment.json` (Contiene las variables dinámicas).
4. **¡Muy importante!** En la esquina superior derecha de Postman, asegúrate de seleccionar el entorno `NexusMicroservicios_Enviroment`.
5. Ve a la vista de configuración del entorno (el ícono del "ojito" o "Environment quick look") e ingresa los valores base en la columna **Current Value** según los puertos que estés utilizando (ya sea desde tu IDE o en Docker). Deberás llenar:
   * `baseUrlCatalogo`
   * `baseUrlSeguridad`
   * `baseUrlIngresoMs`
   * `baseUrlGateway`
6. Guarda los cambios del entorno. Ahora todas las peticiones se enrutarán dinámicamente.

---

## 🛠️ Stack Tecnológico

* **Backend:** Java 21, Spring Boot 4.0.5, Spring Cloud Gateway, Spring Data JPA, Lombok
* **Testing:** JUnit 5, Mockito, Postman (API Testing)
* **Gestor de Dependencias:** Maven
* **Frontend:** Angular 19
* **Base de Datos:** PostgreSQL 18 (Patrón Database per Service)
* **Infraestructura:** Docker & Docker Compose

---

## 👤 Autor
**Autor:** [Henry Wong](https://github.com/hwongu)  

---

## 📜 Licencia

Este proyecto está protegido por copyright © 2026 **Henry Wong**.  
Está permitido su uso únicamente con fines **educativos y académicos** en el marco de cursos universitarios.  
**Queda prohibido su uso en entornos de producción o con fines comerciales.**

---

## ⚠️ Nota

Este repositorio es un recurso de ejemplo para prácticas en clase. No está optimizado para ambientes reales ni cumple con todas las medidas de seguridad y escalabilidad requeridas en aplicaciones comerciales.

---

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-Build-C71A22?style=for-the-badge&logo=apachemaven)
![Angular](https://img.shields.io/badge/Angular-19-DD0031?style=for-the-badge&logo=angular)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-336791?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?style=for-the-badge&logo=docker)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)