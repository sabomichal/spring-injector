package com.github.sabomichal.springinjector;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation that can locate beans within a spring application
 * context. Beans are looked up by the combination of name and type, if name is omitted only type is
 * used.
 *
 * @author Igor Vaynberg (ivaynberg)
 * @author Istvan Devai
 */
public class SpringBeanLocator implements IProxyTargetLocator {
    private static final long serialVersionUID = 1L;

    // Weak reference so we don't hold up WebApp classloader garbage collection.
    private transient WeakReference<Class<?>> beanTypeCache;
    private final ISpringContextLocator springContextLocator;
    private final String beanTypeName;
    private String beanName;
    private Boolean singletonCache = null;

    /**
     * Resolvable type for field to inject
     */
    private ResolvableType fieldResolvableType;

    /**
     * If the field to inject is a list this is the resolvable type of its elements
     */
    private ResolvableType fieldElementsResolvableType;

    /**
     * Constructor
     *
     * @param beanName bean name
     * @param beanType bean class
     * @param locator  spring context locator
     */
    SpringBeanLocator(final String beanName, final Class<?> beanType, Field beanField, final ISpringContextLocator locator) {
        Assert.notNull(locator, "Argument locator can not be null.");
        Assert.notNull(beanType, "Argument beanType can not be null.");

        this.beanName = beanName;
        this.beanTypeCache = new WeakReference<>(beanType);
        this.beanTypeName = beanType.getName();
        this.springContextLocator = locator;

        if (beanField != null) {
            fieldResolvableType = ResolvableType.forField(beanField);
            fieldElementsResolvableType = extractElementGeneric(fieldResolvableType);
        }
    }

    /**
     * If the field type is a collection (Map, Set or List) extracts type
     * information about its elements.
     *
     * @param fieldResolvableType the resolvable type of the field
     * @return the resolvable type of elements of the field, if any.
     */
    private ResolvableType extractElementGeneric(ResolvableType fieldResolvableType) {
        Class<?> clazz = fieldResolvableType.resolve();

        if (Set.class.isAssignableFrom(clazz) || List.class.isAssignableFrom(clazz)) {
            return fieldResolvableType.getGeneric();
        } else if (Map.class.isAssignableFrom(clazz)) {
            return fieldResolvableType.getGeneric(1);
        }

        return null;
    }

    /**
     * @return returns whether the bean (the locator is supposed to istantiate) is a singleton or
     * not
     */
    boolean isSingletonBean() {
        if (singletonCache == null) {
            singletonCache = getBeanName() != null && getSpringContext().isSingleton(getBeanName());
        }
        return singletonCache;
    }

    /**
     * @return bean class this locator is configured with
     */
    private Class<?> getBeanType() {
        Class<?> clazz = beanTypeCache == null ? null : beanTypeCache.get();
        if (clazz == null) {
            beanTypeCache = new WeakReference<>(
                    clazz = resolveClass(beanTypeName));
            if (clazz == null) {
                throw new RuntimeException("SpringBeanLocator could not find class [" +
                        beanTypeName + "] needed to locate the [" +
                        ((beanName != null) ? (beanName) : ("bean name not specified")) + "] bean");
            }
        }
        return clazz;
    }

    /**
     * @param <T>       class type
     * @param className Class to resolve
     * @return Resolved class
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> resolveClass(final String className) {
        Class<T> resolved = null;
        try {
            resolved = (Class<T>) Class.forName(className, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException cnfx) {
            // ignore, resolved class stays null
        }
        return resolved;
    }

    public Object locateProxyTarget() {
        return lookupSpringBean(getSpringContext(), beanName, getBeanType());
    }

    /**
     * @return ApplicationContext
     */
    private ApplicationContext getSpringContext() {
        final ApplicationContext context = springContextLocator.getSpringContext();
        if (context == null) {
            throw new IllegalStateException("spring application context locator returned null");
        }
        return context;
    }

    /**
     * @return bean name this locator is configured with
     */
    private String getBeanName() {
        return beanName;
    }

    /**
     * Looks up a bean by its name and class. Throws IllegalState exception if bean not found.
     *
     * @param ctx   spring application context
     * @param name  bean name
     * @param clazz bean class
     * @return found bean
     */
    private Object lookupSpringBean(ApplicationContext ctx, String name, Class<?> clazz) {
        try {
            // If the name is set the lookup is clear
            if (name != null) {
                return ctx.getBean(name, clazz);
            }

            // If the beanField information is null the clazz is going to be used
            if (fieldResolvableType == null) {
                return ctx.getBean(clazz);
            }

            // If the given class is a list try to get the generic of the list
            Class<?> lookupClass = fieldElementsResolvableType != null ?
                    fieldElementsResolvableType.resolve() : clazz;

            // Else the lookup is done via Generic
            List<String> names = loadBeanNames(ctx, lookupClass);

            Object foundBeans = getBeansByName(ctx, names);

            if (foundBeans != null) {
                return foundBeans;
            }

            throw new IllegalStateException("Concrete bean could not be received from the application context for class: " + clazz.getName() + ".");
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalStateException("bean with name [" + name + "] and class [" + clazz.getName() + "] not found", e);
        }
    }

    /**
     * Returns a list of candidate names for the given class.
     *
     * @param ctx         spring application context
     * @param lookupClass the class to lookup
     * @return a list of candidate names
     */
    private List<String> loadBeanNames(ApplicationContext ctx, Class<?> lookupClass) {
        List<String> beanNames = new ArrayList<>();
        Class<?> fieldType = getBeanType();
        String[] beanNamesArr = ctx.getBeanNamesForType(fieldType);

        //add names for field class
        beanNames.addAll(Arrays.asList(beanNamesArr));

        //add names for lookup class
        if (lookupClass != fieldType) {
            beanNamesArr = ctx.getBeanNamesForType(lookupClass);
            beanNames.addAll(Arrays.asList(beanNamesArr));
        }

        //filter those beans who don't have a definition (used internally by Spring)
        beanNames.removeIf(s -> !ctx.containsBeanDefinition(s));

        return beanNames;
    }

    /**
     * Retrieves a list of beans or a single bean for the given list of names and assignable to the
     * current field to inject.
     *
     * @param ctx   spring application context.
     * @param names the list of candidate names
     * @return a list of matching beans or a single one.
     */
    private Object getBeansByName(ApplicationContext ctx, List<String> names) {
        FieldBeansCollector beansCollector = new FieldBeansCollector(fieldResolvableType);

        for (String beanName : names) {
            RootBeanDefinition beanDef = getBeanDefinition(ctx, beanName);

            if (beanDef == null) {
                continue;
            }

            ResolvableType candidateResolvableType = null;

            //check if we have the class of the bean or the factory method.
            //Usually if use XML as config file we have the class while we
            //have the factory method if we use Java-based configuration.
            if (beanDef.hasBeanClass()) {
                candidateResolvableType = ResolvableType.forClass(beanDef.getBeanClass());
            } else if (beanDef.getResolvedFactoryMethod() != null) {
                candidateResolvableType = ResolvableType.forMethodReturnType(
                        beanDef.getResolvedFactoryMethod());
            }

            if (candidateResolvableType == null) {
                continue;
            }

            boolean exactMatch = fieldResolvableType.isAssignableFrom(candidateResolvableType);
            boolean elementMatch = fieldElementsResolvableType != null && fieldElementsResolvableType.isAssignableFrom(candidateResolvableType);

            if (exactMatch) {
                this.beanName = beanName;
                return ctx.getBean(beanName);
            }

            if (elementMatch) {
                beansCollector.addBean(beanName, ctx.getBean(beanName));
            }

        }

        return beansCollector.getBeansToInject();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof SpringBeanLocator) {
            SpringBeanLocator other = (SpringBeanLocator) obj;
            return beanTypeName.equals(other.beanTypeName) &&
                    Objects.equals(beanName, other.beanName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashcode = beanTypeName.hashCode();
        if (getBeanName() != null) {
            hashcode = hashcode + (127 * beanName.hashCode());
        }
        return hashcode;
    }

    /**
     * Gets the root bean definition for the given name.
     *
     * @param ctx  spring application context.
     * @param name bean name
     * @return bean definition for the current name, null if such a definition is not found.
     */
    private RootBeanDefinition getBeanDefinition(final ApplicationContext ctx, final String name) {
        ConfigurableListableBeanFactory beanFactory = ((AbstractApplicationContext) ctx).getBeanFactory();

        BeanDefinition beanDef = beanFactory.containsBean(name) ?
                beanFactory.getMergedBeanDefinition(name) : null;

        if (beanDef instanceof RootBeanDefinition) {
            return (RootBeanDefinition) beanDef;
        }

        return null;
    }
}
