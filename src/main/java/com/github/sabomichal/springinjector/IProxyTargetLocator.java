package com.github.sabomichal.springinjector;

import java.io.Serializable;

/**
 * Represents a service locator for lazy init proxies. When the first method invocation occurs on
 * the lazy init proxy this locator will be used to retrieve the proxy target object that will
 * receive the method invocation.
 * <p>
 * Generally implementations should be small when serialized because the main purpose of lazy init
 * proxies is to be stored in session when the wicket pages are serialized, and when deserialized to
 * be able to lookup the dependency again. The smaller the implementation of IProxyTargetLocator the
 * less the drain on session size.
 * <p>
 * A small implementation may use a static lookup to retrieve the target object.
 * <p>
 * Example:
 * <p>
 * <pre>
 * class UserServiceLocator implements IProxyTargetLocator
 * {
 * 	Object locateProxyObject()
 *    {
 * 		MyApplication app = (MyApplication)Application.get();
 * 		return app.getUserService();
 *    }
 * }
 * </pre>
 *
 * @author Igor Vaynberg (ivaynberg)
 * @see LazyInitProxyFactory#createProxy(Class, IProxyTargetLocator)
 */
public interface IProxyTargetLocator extends Serializable {
    /**
     * Returns the object that will be used as target object for a lazy init proxy.
     *
     * @return retrieved object
     */
    Object locateProxyTarget();
}
