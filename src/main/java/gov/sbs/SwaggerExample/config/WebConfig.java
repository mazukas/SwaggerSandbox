package gov.sbs.SwaggerExample.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class WebConfig implements WebApplicationInitializer, ApplicationContextAware {

	private ApplicationContext context;
	
	public void onStartup(ServletContext sc) throws ServletException {
		System.out.println("\n\n\n\n\nSwaggerConfig : 1");
		
		AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
		ctx.register(AppConfig.class);
		ctx.setServletContext(sc);
		
		DispatcherServlet ds = new DispatcherServlet(ctx);
		Dynamic servlet = sc.addServlet("dispatcher", ds);
		servlet.addMapping("/services/*");
		servlet.setLoadOnStartup(1);

	}

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		System.out.println("\n\n\n\n\nSwaggerConfig : 2");
		this.context = context;
	}
}
