package com.github.sabomichal.springinjector;

import org.springframework.context.ApplicationContext;

import java.io.Serializable;

/**
 * Interface representing object that can locate a spring context. The implementation should take up
 * little room when serialized.
 * <p>
 * SpringObjectLocator uses this interface to locate the spring context so that it in turn can
 * locate a bean.
 * <p>
 * Ideal implementations use a static lookup to locate the context.
 * <p>
 * For Example:
 * <p>
 * <pre>
 * class SpringContextLocator implements ISpringContextLocator
 * {
 * 	public ApplicationContext getSpringContext()
 *    {
 * 		// MyApplication is the subclass of WebApplication used by the application
 * 		return ((MyApplication)Application.get()).getContext();
 *    }
 * }
 * </pre>
 *
 * @author Igor Vaynberg (ivaynberg)
 * @see org.apache.wicket.spring.SpringBeanLocator
 */
public interface ISpringContextLocator extends Serializable {
    /**
     * Getter for spring application context
     *
     * @return spring application context
     */
    ApplicationContext getSpringContext();
}
