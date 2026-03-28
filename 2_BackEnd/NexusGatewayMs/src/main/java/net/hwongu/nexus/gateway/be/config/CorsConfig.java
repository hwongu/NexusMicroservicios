package net.hwongu.nexus.gateway.be.config;

import org.springframework.context.annotation.Configuration;

/**
 * Marcador de configuracion del gateway.
 *
 * <p>El manejo de CORS se realiza mediante {@link GatewayCorsFilter} para
 * garantizar que la cabecera Access-Control-Allow-Origin se escriba una sola
 * vez despues del paso por el proxy.</p>
 *
 * @author Henry Wong
 */
@Configuration
public class CorsConfig {
}