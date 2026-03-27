package net.hwongu.nexus.ingreso.be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuracion minima para disponer de un constructor de clientes HTTP.
 *
 * <p>Se centraliza este bean para mantener la creacion de clientes remotos
 * simple, reutilizable y visible para fines didacticos.</p>
 *
 * @author Henry Wong
 */
@Configuration
public class RestClientConfig {

    /**
     * Expone un {@link RestClient.Builder} reutilizable para las integraciones
     * con otros microservicios.
     *
     * @return builder base de clientes HTTP.
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
