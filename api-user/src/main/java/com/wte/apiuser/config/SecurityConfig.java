package com.wte.apiuser.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wt.wte.wtebase.base.BaseConstants;
import com.wt.wte.wtebase.common.dto.UmsUserDetails;
import com.wt.wte.wtebase.modules.ums.model.UmsUser;
import com.wt.wte.wtebase.modules.ums.service.UmsUserService;
import com.wte.apiuser.component.JwtAuthenticationTokenFilter;
import com.wte.apiuser.component.RestAuthenticationEntryPoint;
import com.wte.apiuser.component.RestfulAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SpringSecurity的配置
 * Create by chenglong on 2021/1/22
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UmsUserService umsUserService;
    @Autowired
    private RestfulAccessDeniedHandler restfulAccessDeniedHandler;
    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable() //使用JWT，不需要csrf
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) //基于token，不适用session
            .and().authorizeRequests()
            .antMatchers(HttpMethod.GET,"/",
                    "/*.html",
                    "/favicon.ico",
                    "/**/*.html",
                    "/**/*.css",
                    "/**/*.js",
                    "/doc.html",
                    "/swagger-resources/**",
                    "/v2/api-docs/**").permitAll()
            .antMatchers("/api/account/**").permitAll() // 对AccountController的api要允许匿名访问
            .antMatchers(HttpMethod.OPTIONS).permitAll() //跨域请求会先进行一次options请求
            .anyRequest().authenticated();
        //禁用缓存
        http.headers().cacheControl();
        //添加JWT filter
        http.addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling()
                .accessDeniedHandler(restfulAccessDeniedHandler)
                .authenticationEntryPoint(restAuthenticationEntryPoint);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        //获取登录用户信息
        return username -> {
            QueryWrapper<UmsUser> wrapper = new QueryWrapper();
            wrapper.lambda()
                    .eq(UmsUser::getIsDelete, BaseConstants.DEL_NO)
                    .eq(UmsUser::getUsername,username);
            UmsUser user = umsUserService.getOne(wrapper);
            if (user != null) {
                return new UmsUserDetails(user);
            }
            throw new UsernameNotFoundException("用户名或密码错误");
        };
    }

    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter(){
        return new JwtAuthenticationTokenFilter();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
