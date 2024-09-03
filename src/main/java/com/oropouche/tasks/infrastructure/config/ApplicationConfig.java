package com.oropouche.tasks.infrastructure.config;


import com.oropouche.tasks.application.services.TaskServices;
import com.oropouche.tasks.application.usecases.*;
import com.oropouche.tasks.infrastructure.repositories.user.UserRepository;
import com.oropouche.tasks.domain.ports.in.GetAddicionalTaskInfoUseCase;
import com.oropouche.tasks.domain.ports.out.ExternalServiceTaskPort;
import com.oropouche.tasks.domain.ports.out.TaskRepositotyPort;
import com.oropouche.tasks.infrastructure.adapters.ExternalServiceTaskAdapter;
import com.oropouche.tasks.infrastructure.repositories.JpaTaskRepositoryAdapter;
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

@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = "com.oropouche.tasks.infrastructure.repositories")
public class ApplicationConfig {

   private final UserRepository userRepository;

    @Bean
    public TaskServices taskServices(TaskRepositotyPort taskRepositotyPort, GetAddicionalTaskInfoUseCase getAddicionalTaskInfoUseCase ){
        return new TaskServices(
                new CreateTaskUseCaseImpl(taskRepositotyPort),
                new RetrieveTaskUseCaseImpl(taskRepositotyPort),
                new UpdateTaskUseCaseImpl(taskRepositotyPort),
                new DeleteTaskUseCaseImpl(taskRepositotyPort),
                getAddicionalTaskInfoUseCase
        );
    }
    @Bean
    public TaskRepositotyPort taskRepositotyPort(JpaTaskRepositoryAdapter jpaTaskRepositoryAdapter){
        return jpaTaskRepositoryAdapter;
    }

    @Bean
    public GetAddicionalTaskInfoUseCase getAddicionalTaskInfoUseCase(ExternalServiceTaskPort externalServicePort){
    return new GetAddicionalTaskInfoUseCaseImpl(externalServicePort);
    }
    @Bean
    public ExternalServiceTaskPort externalServicePort(){
        return new ExternalServiceTaskAdapter();
    }

    //jwt
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
      return username -> userRepository.findByUsername(username)
              .orElseThrow(() -> new UsernameNotFoundException("User not Found"));
    }

}
