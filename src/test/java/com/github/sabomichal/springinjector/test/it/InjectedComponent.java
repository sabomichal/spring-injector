package com.github.sabomichal.springinjector.test.it;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author Michal Sabo
 * 
 */
@Component
public class InjectedComponent implements Serializable {
	private static final long serialVersionUID = -9219303463386257841L;

	public int answer() {
		return 42;
	}

}