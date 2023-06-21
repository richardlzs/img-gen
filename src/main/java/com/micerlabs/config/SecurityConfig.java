package com.micerlabs.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //authorizeRequests开启权限配置
        http.authorizeRequests()
                //anyRequest().authenticated()所有请求都要认证
                .requestMatchers(new RegexRequestMatcher(".*","GET")).authenticated()
                .requestMatchers(new RegexRequestMatcher(".*","POST")).authenticated()
                //.anyRequest().authenticated()
                //and()和http同意
                .and()
                //formLogin开启表单登陆的配置
                .formLogin()
                //loginProcessingUrl登录接口
                .loginProcessingUrl("/doLogin")
//                //defaultSuccessUrl登录成功跳转的页面,如果直接到Login页面登陆成功跳转的地址
                .defaultSuccessUrl("/h5.html")
                .successHandler((httpServletRequest, httpServletResponse, authentication) -> httpServletResponse.sendRedirect("h5.html"))
                //登录的用户名
                .usernameParameter("uname")
                //登录的密码
                .passwordParameter("passwd")
                //表示跟登录相关的接口不拦截
                .permitAll()
                .and()
                //关闭csrf跨域攻击
                .csrf().disable();
    }
}