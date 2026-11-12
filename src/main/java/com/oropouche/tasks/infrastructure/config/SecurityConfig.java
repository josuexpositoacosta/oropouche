package com.oropouche.tasks.infrastructure.config;

import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.infrastructure.jwt.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationProvider authProvider) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authProvider = authProvider;
    }

    private static final String ADMIN = Role.ADMIN.name();
    private static final String MEDICO = Role.MEDICO.name();
    private static final String EPIDEMIOLOGO = Role.EPIDEMIOLOGO.name();

    /**
     * Reglas por rol (primer nivel). El acceso por área de salud se aplica después, en la capa de
     * aplicación (PoliticaAcceso). Todo requiere JWT excepto el login y las peticiones CORS preflight.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authRequest -> authRequest
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers("/auth/**").permitAll()
                // Administración de usuarios
                .antMatchers("/api/v1/admin/**").hasAuthority(ADMIN)
                // Catálogos: los lee cualquier usuario; los gestiona el ADMIN
                .antMatchers(HttpMethod.GET, "/api/v1/areas-salud/**", "/api/v1/desenlaces/**").authenticated()
                .antMatchers("/api/v1/areas-salud/**", "/api/v1/desenlaces/**").hasAuthority(ADMIN)
                // Datos clínicos: los leen médico y epidemiólogo; los escribe el médico
                .antMatchers(HttpMethod.PATCH, "/api/v1/casos/*/clasificacion/validacion").hasAuthority(EPIDEMIOLOGO)
                .antMatchers(HttpMethod.PATCH, "/api/v1/casos/*/clasificacion").hasAnyAuthority(MEDICO, EPIDEMIOLOGO)
                .antMatchers(HttpMethod.GET, "/api/v1/pacientes/**", "/api/v1/casos/**")
                    .hasAnyAuthority(MEDICO, EPIDEMIOLOGO)
                .antMatchers("/api/v1/pacientes/**", "/api/v1/casos/**").hasAuthority(MEDICO)
                // Estadísticas de vigilancia
                .antMatchers("/api/v1/estadisticas/**").hasAuthority(EPIDEMIOLOGO)
                // Datos personales propios (la regla "solo los suyos" está en la capa de aplicación)
                .antMatchers("/api/v1/user/**").authenticated()
                .anyRequest().denyAll())
            .exceptionHandling(exceptions -> exceptions
                // Sin token o con token inválido: 401 (no autenticado) en lugar de 403
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .sessionManagement(sessionManager -> sessionManager
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    /**
     * El filtro JWT es un {@code @Component}; sin esto Spring Boot también lo registraría en el
     * contenedor de servlets y se ejecutaría dos veces por petición. Solo debe vivir en la
     * cadena de Spring Security.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
