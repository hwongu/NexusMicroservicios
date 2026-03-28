package net.hwongu.nexus.ingreso.be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracion global y didactica de CORS para el microservicio.
 *
 * <p>En este entorno academico se permite cualquier origen para facilitar las
 * pruebas desde Angular, Postman u otros clientes sin agregar complejidad de
 * seguridad adicional.</p>
 *
 * @author Henry Wong
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Registra reglas CORS globales para todas las rutas del microservicio.
     *
     * @param registry registro de configuraciones CORS provisto por Spring MVC.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*");
    }
}
