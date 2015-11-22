package com.github.sabomichal.springinjector.test.it;

import com.github.sabomichal.springinjector.SpringInjector;

/**
 * @author Michal Sabo
 */
public class InjectableClass {

	public InjectableClass() {
		SpringInjector.get().inject(this);
	}
}
