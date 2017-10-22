package com.github.sabomichal.springinjector;

import java.io.Serializable;

public interface ILazyInitProxy extends Serializable {
    /**
     * @return object locator the proxy carries
     */
    IProxyTargetLocator getObjectLocator();
}