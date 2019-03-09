package uk.dioxic.mongotakeaway;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.CommonsPool2TargetSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

@SpringBootApplication
public class TakeawayApplication {

	public static void main(String[] args) {
		SpringApplication.run(TakeawayApplication.class, args);
	}

//	@Bean
//	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
//	public ProxyFactoryBean stateMachine() {
//		ProxyFactoryBean pfb = new ProxyFactoryBean();
//		pfb.setTargetSource(poolTargetSource());
//		return pfb;
//	}
//
//	@Bean
//	public CommonsPool2TargetSource poolTargetSource() {
//		CommonsPool2TargetSource pool = new CommonsPool2TargetSource();
//		pool.setMaxSize(3);
//		pool.setTargetBeanName("stateMachineTarget");
//		return pool;
//	}

}
