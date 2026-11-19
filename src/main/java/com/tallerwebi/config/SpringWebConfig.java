package com.tallerwebi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

@EnableWebMvc
@Configuration
@ComponentScan(
  { "com.tallerwebi.presentacion", "com.tallerwebi.dominio", "com.tallerwebi.infraestructura" }
)
public class SpringWebConfig implements WebMvcConfigurer {

  // Spring + Thymeleaf need this
  @Autowired
  private ApplicationContext applicationContext;

  @Override
  public void addResourceHandlers(final ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/css/**").addResourceLocations("/resources/core/css/");
    registry.addResourceHandler("/js/**").addResourceLocations("/resources/core/js/");
    registry.addResourceHandler("/imagenes/**").addResourceLocations("/resources/core/imagenes/");
    registry.addResourceHandler("/webjars/**").addResourceLocations("/webjars/");

    java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads").toAbsolutePath();
    uploadDir.toFile().mkdirs();
    String uploadPath = uploadDir.toUri().toString();
    if (!uploadPath.endsWith("/")) {
      uploadPath += "/";
    }
    registry
            .addResourceHandler("/resources/uploads/**")
            .addResourceLocations(uploadPath);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
      .addInterceptor(new InterceptorSeguridad())
      // Acá le decimos qué rutas DEBEN estar protegidas (requieren login)
      .addPathPatterns(
        "/home/**",
        "/tecnico/**",
        "/admin/**",
        "/cierre-orden/**",
        "/presupuesto/**",
        "/ordenes/**",
        "/maquinas/**",
        "/plan-canje/**",
        "/admin/maquinas/**",
        "/api/alertas/**",
        "/recepcion-home"
      )
      // Acá le decimos qué rutas son PÚBLICAS (cualquiera puede entrar sin login)
      .excludePathPatterns(
        "/login",
        "/validar-login",
        "/nuevo-usuario",
        "/guardar-usuario",
        "/css/**",
        "/js/**",
        "/imagenes/**",
        // NUEVO: Excluimos la carpeta de uploads para que no rebote los requests GET de las imágenes
        "/resources/uploads/**"
      );
  }

  // https://www.thymeleaf.org/doc/tutorials/3.0/thymeleafspring.html
  // Spring + Thymeleaf
  @Bean
  public SpringResourceTemplateResolver templateResolver() {
    SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
    templateResolver.setApplicationContext(this.applicationContext);
    templateResolver.setPrefix("/WEB-INF/views/thymeleaf/");
    templateResolver.setSuffix(".html");
    templateResolver.setTemplateMode(TemplateMode.HTML);
    templateResolver.setCharacterEncoding("UTF-8"); //para solucionar problemmas de caracteres y tildes
    templateResolver.setCacheable(true);
    return templateResolver;
  }

  @Bean(name = "multipartResolver")
  public StandardServletMultipartResolver multipartResolver() {
    return new StandardServletMultipartResolver();
  }

  // Spring + Thymeleaf
  @Bean
  public SpringTemplateEngine templateEngine() {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.setTemplateResolver(templateResolver());
    templateEngine.setEnableSpringELCompiler(true);
    return templateEngine;
  }

  // Spring + Thymeleaf
  @Bean
  public ThymeleafViewResolver viewResolver() {
    ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
    viewResolver.setTemplateEngine(templateEngine());
    viewResolver.setCharacterEncoding("UTF-8"); //para solucionar problemmas de caracteres y tildes
    return viewResolver;
  }
}
