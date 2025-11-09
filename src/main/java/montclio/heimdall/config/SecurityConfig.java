package montclio.heimdall.config;

import montclio.heimdall.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod; // Importe o HttpMethod

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/login", "/register/**", "/error").permitAll()

                        .requestMatchers("/users/**").hasAuthority("ADMIN")

                        .requestMatchers(
                                "/motorcycles/delete/**",
                                "/tags/delete/**",
                                "/zonas/delete/**",
                                "/vagas/delete/**"
                        ).hasAuthority("ADMIN")


                        .requestMatchers(HttpMethod.POST, "/zonas/**", "/vagas/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/zonas/edit/**", "/vagas/edit/**", "/zonas/new", "/vagas/new").hasAuthority("ADMIN")

                        //operações do Dia a Dia (Motos e Tags): Operador pode Criar/Editar
                        .requestMatchers(HttpMethod.GET, "/motorcycles/new", "/tags/new", "/motorcycles/edit/**", "/tags/edit/**").hasAnyAuthority("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/motorcycles/save", "/tags/save").hasAnyAuthority("ADMIN", "USER")

                        //visualização Geral (Dashboard e Listagens) - Aberto a todos logados
                        .requestMatchers(HttpMethod.GET, "/", "/dashboard", "/motorcycles/**", "/tags/**", "/zonas/**", "/vagas/**").hasAnyAuthority("ADMIN", "USER")

                        .requestMatchers("/motorcycles/**", "/tags/**", "/zonas/**", "/vagas/**", "/", "/dashboard", "/profile/**") // <-- ADICIONE /profile/**
                        .hasAnyAuthority("ADMIN", "USER")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}