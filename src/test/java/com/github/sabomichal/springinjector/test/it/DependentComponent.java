package com.github.sabomichal.springinjector.test.it;

import com.github.sabomichal.springinjector.SpringInjector;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author Michal Sabo
 * 
 */
public class DependentComponent implements Serializable {
	private static final long serialVersionUID = 1L;

	@Inject
	private InjectedComponent injectedComponent;

	public DependentComponent() {
		SpringInjector.get().inject(this);
	}

	public int answer() {
		return injectedComponent.answer();
	}
}