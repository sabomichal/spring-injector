package com.github.sabomichal.springinjector.test.it;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author Michal Sabo
 * 
 */
public class DependentComponent extends InjectableClass implements Serializable {
	private static final long serialVersionUID = 500751252338115791L;

	@Inject
	private InjectedComponent injectedComponent;

	@Inject
	private transient InjectedComponent injectedTransientComponent;

	public int answer() {
		return injectedComponent.answer();
	}

	public int answerTransient() {
		return injectedTransientComponent.answer();
	}
}