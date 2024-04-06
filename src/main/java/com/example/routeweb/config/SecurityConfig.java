package com.example.routeweb.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private DataSource dataSource;

    @Override
    protected void configure(final AuthenticationManagerBuilder auth)
            throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder())
                .usersByUsernameQuery("select username, " +
                        "password, 1 from users where username = ?")
                .authoritiesByUsernameQuery("select u.username, " +
                        "ur.roles from users u inner join user_role " +
                        "ur on u.id_user = ur.user_id where u.username = ?");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/authorization").permitAll()
                .antMatchers("/printDocument", "/addUser", "/download/{fileName}").hasAnyAuthority("ADMIN")
                .antMatchers("/printDocument").hasAnyAuthority("EMPLOYEE")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/authorization")
                .defaultSuccessUrl("/fileData")
                .failureUrl("/authorization?error=true")
                .successHandler((request, response, authentication) -> {
                    for (GrantedAuthority authority : authentication.getAuthorities()) {
                        if ("ADMIN".equals(authority.getAuthority())) {
                            response.sendRedirect("/fileData");
                            return;
                        }
                    }
                    for (GrantedAuthority authority : authentication.getAuthorities()) {
                        if ("EMPLOYEE".equals(authority.getAuthority())) {
                            response.sendRedirect("/fileDataFromEmployee");
                            return;
                        }
                    }
                    response.sendRedirect("/fileData");
                })


                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/authorization")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
                .and()
                .cors().disable();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
