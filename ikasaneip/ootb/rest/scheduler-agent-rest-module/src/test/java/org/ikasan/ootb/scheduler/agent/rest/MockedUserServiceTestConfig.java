package org.ikasan.ootb.scheduler.agent.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.ikasan.ootb.scheduler.agent.rest.dto.ContextParameterDto;
import org.ikasan.spec.scheduled.context.model.ContextParameter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.*;

@TestConfiguration
@EnableWebSecurity
@EnableWebMvc
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MockedUserServiceTestConfig implements WebMvcConfigurer
{
    @Bean
    @Primary
    public UserDetailsService userDetailsService()
    {
        return new InMemoryUserDetailsManager(Arrays.asList(
            User.withUsername("webServiceAdmin")
                .password("password")
                .authorities("WebServiceAdmin")
                .build(),
            User.withUsername("readonly")
                .password("readonly")
                .authorities("readonly")
                .build()
        ));
    }

}
