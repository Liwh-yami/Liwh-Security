package com.liwh.config;

import com.liwh.authentication.mobile.config.SmsAuthenticationSecurityConfig;
import com.liwh.constants.SecurityConstants;
import com.liwh.properties.SecurityProperties;
import com.liwh.validate.filter.ValidateCodeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

/**
 * @author: Liwh
 * @ClassName: WebSecurityConfig
 * @Description:
 * @version: 1.0.0
 * @date: 2018-12-07 11:15 AM
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SecurityProperties securityProperties;
    @Autowired//用我们的实现类
    private AuthenticationSuccessHandler defaultAuthenticationSuccessHandle;
    @Autowired
    private AuthenticationFailureHandler defaultAuthenticationFailureHandler;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    SmsAuthenticationSecurityConfig smsAuthenticationSecurityConfig;
    @Autowired
    private ValidateCodeFilter validateCodeFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder;
    }

    @Bean//持久化的TokenRepository
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        //开启：启动服务时，spring创建表
//        jdbcTokenRepository.setCreateTableOnStartup(true);
        return jdbcTokenRepository;
    }

    //重写web http security 配置
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        //afterPropertiesSet()
        validateCodeFilter.afterPropertiesSet();


        //加在前面
        http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin()
                //访问自定义首页
                .loginPage(SecurityConstants.DEFAULT_LOGIN_PAGE_URL)  /*html请求*/
//                .loginPage("/authentication/require")  /*资源访问请求*/
                //自定义表单的请求，使用UsernamePasswordAuthenticationFilter进行处理
                .loginProcessingUrl(SecurityConstants.DEFAULT_LOGIN_PROCESSING_URL_FORM)
                //加入我们的成功处理器
                .successHandler(defaultAuthenticationSuccessHandle)
                .failureHandler(defaultAuthenticationFailureHandler)
                .and()
                .rememberMe()
                .tokenRepository(persistentTokenRepository())
                .tokenValiditySeconds(securityProperties.getWebSecurity().getRememberMeSeconds())
                //回调userDetails，进行记住后操作
                .userDetailsService(userDetailsService)
                .and()
                .authorizeRequests()
                //匹配器放过测试的controller请求 + 首页访问
//                .antMatchers("/default-security.html").permitAll()
                .antMatchers(SecurityConstants.DEFAULT_UN_AUTHENTICATION_URL, SecurityConstants.DEFAULT_LOGIN_PROCESSING_URL_MOBILE
                        , SecurityConstants.DEFAULT_LOGIN_PAGE_URL
                        , SecurityConstants.DEFAULT_VALIDATE_CODE_URL_PREFIX + SecurityConstants.DEFAULT_WILDCARD_URL).permitAll()
                .anyRequest()
                .authenticated()
                .and()
                //关闭csrf跨站攻击保护
                .csrf().disable()
                //追加短信验证的HTTP配置
                .apply(smsAuthenticationSecurityConfig);
    }
}
