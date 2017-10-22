package com.github.sabomichal.springinjector.test.it;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author Michal Sabo
 * 
 */
@Component
public class InjectedComponent {

	public int answer() {
		return 42;
	}

}