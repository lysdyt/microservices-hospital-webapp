package com.baeldung.config;

import com.baeldung.model.Authority;
import com.baeldung.model.User;
import com.baeldung.service.HospitalUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@PropertySource({ "classpath:persistence.properties" })
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    /*@Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public void globalUserDetails(final AuthenticationManagerBuilder auth) throws Exception {
        // @formatter:off
	auth.inMemoryAuthentication()
	  .withUser("john").password(passwordEncoder.encode("123")).roles("USER").and()
	  .withUser("tom").password(passwordEncoder.encode("111")).roles("ADMIN").and()
	  .withUser("user1").password(passwordEncoder.encode("pass")).roles("USER").and()
	  .withUser("admin").password(passwordEncoder.encode("nimda")).roles("ADMIN");
    }// @formatter:on

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        // @formatter:off
		http.authorizeRequests().antMatchers("/login").permitAll()
		.antMatchers("/oauth/token/revokeById/**").permitAll()
		.antMatchers("/tokens/**").permitAll()
		.anyRequest().authenticated()
		.and().formLogin().permitAll()
		.and().csrf().disable();
		// @formatter:on
    }*/

    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;
    private final HospitalUserDetailsService userDetailsService;

    @Autowired
    public WebSecurityConfig(DataSource dataSource, PasswordEncoder passwordEncoder, HospitalUserDetailsService userDetailsService) {
        this.dataSource = dataSource;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .authenticationProvider(authenticationProvider())
                .jdbcAuthentication()
                .dataSource(dataSource);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        User user = new User("john", "123");

        List<Authority> s = new ArrayList<>();
        s.add(new Authority("ADMIN"));
        user.setRoles(s);
        user.setEnabled(true);
        userDetailsService.registerUser(user);

        User doctor = new User("doctor", "doctor");
        List<Authority> roles = new ArrayList<>();
        roles.add(new Authority("DOCTOR"));
        doctor.setRoles(roles);
        doctor.setEnabled(true);
        userDetailsService.registerUser(doctor);

        User normalUser = new User("user", "user");
        List<Authority> noRoles = new ArrayList<>();
        noRoles.add(new Authority("USER"));
        normalUser.setRoles(noRoles);
        normalUser.setEnabled(true);
        userDetailsService.registerUser(normalUser);

        DaoAuthenticationProvider authProvider
                = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        // @formatter:off
        http.authorizeRequests().antMatchers("/login").permitAll()
                .antMatchers("/register/user").permitAll()
                .antMatchers("/oauth/token/revokeById/**").permitAll()
                .antMatchers("/tokens/**").permitAll()
                .anyRequest().authenticated()
                .and().formLogin().permitAll()
                .and().csrf().disable();
        // @formatter:on
    }
}
