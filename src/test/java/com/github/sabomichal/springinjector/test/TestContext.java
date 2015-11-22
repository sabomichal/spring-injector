package com.github.sabomichal.springinjector.test;

import com.github.sabomichal.springinjector.SpringInjector;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Michal Sabo
 */
@Configuration
@ComponentScan(basePackageClasses = SpringInjector.class)
public class TestContext {
}
