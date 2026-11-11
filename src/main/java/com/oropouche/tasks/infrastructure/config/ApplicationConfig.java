package com.oropouche.tasks.infrastructure.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import com.oropouche.tasks.application.services.TaskServices;
import com.oropouche.tasks.application.usecases.*;
import com.oropouche.tasks.domain.ports.in.GetAddicionalTaskInfoUseCase;
import com.oropouche.tasks.application.services.paciente.PacienteService;
import com.oropouche.tasks.domain.ports.in.AreaSaludUseCase;
import com.oropouche.tasks.domain.ports.in.PacienteUseCase;
import com.oropouche.tasks.domain.ports.out.PacienteRepositoryPort;
import com.oropouche.tasks.domain.ports.in.GetUserUseCase;
import com.oropouche.tasks.domain.ports.in.LoginUseCase;
import com.oropouche.tasks.domain.ports.in.RegisterUserUseCase;
import com.oropouche.tasks.domain.ports.in.UpdateUserUseCase;
import com.oropouche.tasks.infrastructure.transaction.TransactionalUpdateUserUseCase;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.oropouche.tasks.domain.ports.out.AuthenticationPort;
import com.oropouche.tasks.domain.ports.out.PasswordEncoderPort;
import com.oropouche.tasks.domain.ports.out.TokenProviderPort;
import com.oropouche.tasks.domain.ports.out.ExternalServiceTaskPort;
import com.oropouche.tasks.domain.ports.out.TaskRepositotyPort;
import com.oropouche.tasks.domain.ports.out.UserRepositoryPort;
import com.oropouche.tasks.infrastructure.adapters.ExternalServiceTaskAdapter;
import com.oropouche.tasks.infrastructure.repositories.JpaTaskRepositoryAdapter;
import com.oropouche.tasks.infrastructure.security.UserDetailsAdapter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.oropouche.tasks.application.services.area_salud.AreaSaludService;
import com.oropouche.tasks.domain.ports.out.AreaSaludRepositoryPort;

@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = "com.oropouche.tasks.infrastructure.repositories")
public class ApplicationConfig {

    @Bean
    public TaskServices taskServices(
            TaskRepositotyPort taskRepositotyPort,
            GetAddicionalTaskInfoUseCase getAddicionalTaskInfoUseCase) {

        return new TaskServices(
                new CreateTaskUseCaseImpl(taskRepositotyPort),
                new RetrieveTaskUseCaseImpl(taskRepositotyPort),
                new UpdateTaskUseCaseImpl(taskRepositotyPort),
                new DeleteTaskUseCaseImpl(taskRepositotyPort),
                getAddicionalTaskInfoUseCase
        );
    }

    @Bean
    public TaskRepositotyPort taskRepositotyPort(
            JpaTaskRepositoryAdapter jpaTaskRepositoryAdapter) {

        return jpaTaskRepositoryAdapter;
    }

    @Bean
    public GetAddicionalTaskInfoUseCase getAddicionalTaskInfoUseCase(
            ExternalServiceTaskPort externalServicePort) {

        return new GetAddicionalTaskInfoUseCaseImpl(externalServicePort);
    }

    @Bean
    public ExternalServiceTaskPort externalServicePort(
            RestTemplate restTemplate,
            @Value("${external.services.jsonplaceholder.base-url}") String baseUrl) {

        return new ExternalServiceTaskAdapter(restTemplate, baseUrl);
    }

    // Áreas de salud y pacientes

    @Bean
    public AreaSaludUseCase areaSaludUseCase(AreaSaludRepositoryPort areaSaludRepositoryPort) {

        return new AreaSaludService(areaSaludRepositoryPort);
    }

    @Bean
    public PacienteUseCase pacienteUseCase(
            PacienteRepositoryPort pacienteRepositoryPort,
            AreaSaludRepositoryPort areaSaludRepositoryPort) {

        return new PacienteService(pacienteRepositoryPort, areaSaludRepositoryPort);
    }

    // Casos de uso de autenticación (Java puro, se conectan a sus puertos aquí)

    @Bean
    public LoginUseCase loginUseCase(
            AuthenticationPort authenticationPort,
            UserRepositoryPort userRepositoryPort,
            TokenProviderPort tokenProviderPort) {

        return new LoginUseCaseImpl(authenticationPort, userRepositoryPort, tokenProviderPort);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoderPort passwordEncoderPort,
            TokenProviderPort tokenProviderPort) {

        return new RegisterUserUseCaseImpl(userRepositoryPort, passwordEncoderPort, tokenProviderPort);
    }

    // Casos de uso de usuarios

    @Bean
    public GetUserUseCase getUserUseCase(UserRepositoryPort userRepositoryPort) {

        return new GetUserUseCaseImpl(userRepositoryPort);
    }

    @Bean
    public UpdateUserUseCase updateUserUseCase(
            UserRepositoryPort userRepositoryPort,
            PlatformTransactionManager transactionManager) {

        return new TransactionalUpdateUserUseCase(
                new UpdateUserUseCaseImpl(userRepositoryPort),
                new TransactionTemplate(transactionManager));
    }



    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider();

        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            UserRepositoryPort userRepositoryPort) {

        return username -> userRepositoryPort.findByUsername(username)
                .map(UserDetailsAdapter::new)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not Found"));
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}