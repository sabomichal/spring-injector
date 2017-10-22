package com.github.sabomichal.springinjector;


import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class is usually used by the {@link SpringInjector} to inject spring beans. This class will also
 * utilize caching mechanism (caching singleton beans and bean names) to improve inject performance.
 *
 * @author Igor Vaynberg (ivaynberg)
 * @author Istvan Devai
 * @author Michal Sabo
 * @see SpringBeanLocator
 * @see Inject
 */
public class AnnotFieldValueFactory implements IFieldValueFactory {

    private final ConcurrentMap<SpringBeanLocator, Object> cache = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, String> beanNameCache = new ConcurrentHashMap<>();

    private ISpringContextLocator springContextLocator;

    AnnotFieldValueFactory(ISpringContextLocator springContextLocator) {
        this.springContextLocator = springContextLocator;
    }

    @Override
    public Object getFieldValue(final Field field) {
        if (supportsField(field)) {

            Named named = field.getAnnotation(Named.class);
            String name = named != null ? named.value() : "";

            Class<?> generic = ResolvableType.forField(field).resolveGeneric(0);
            String beanName = getBeanName(field, name, generic);

            SpringBeanLocator locator = new SpringBeanLocator(beanName, field.getType(), field, springContextLocator);

            // only check the cache if the bean is a singleton
            Object cachedValue = cache.get(locator);
            if (cachedValue != null) {
                return cachedValue;
            }

            Object target = LazyInitProxyFactory.createProxy(field.getType(), locator);

            // only put the proxy into the cache if the bean is a singleton
            if (locator.isSingletonBean()) {
                Object tmpTarget = cache.putIfAbsent(locator, target);
                if (tmpTarget != null) {
                    target = tmpTarget;
                }
            }
            return target;
        }
        return null;
    }

    /**
     * @param field
     * @return bean name
     */
    private String getBeanName(final Field field, String name, Class<?> generic) {
        if (StringUtils.isEmpty(name)) {
            Class<?> fieldType = field.getType();

            name = beanNameCache.get(fieldType);
            if (name == null) {
                name = getBeanNameOfClass(getSpringContext(), fieldType, generic);

                if (name != null) {
                    String tmpName = beanNameCache.putIfAbsent(fieldType, name);
                    if (tmpName != null) {
                        name = tmpName;
                    }
                }
            }
        }

        return name;
    }

    /**
     * Returns the name of the Bean as registered to Spring. Throws IllegalState exception if none
     * or more than one beans are found.
     *
     * @param ctx   spring application context
     * @param clazz bean class
     * @return spring name of the bean
     */
    private String getBeanNameOfClass(final ApplicationContext ctx, final Class<?> clazz,
                                      final Class<?> generic) {
        // get the list of all possible matching beans
        List<String> names = new ArrayList<>(
                Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(ctx, clazz)));

        // filter out beans that are not candidates for autowiring
        if (ctx instanceof AbstractApplicationContext) {
            Iterator<String> it = names.iterator();
            while (it.hasNext()) {
                final String possibility = it.next();
                BeanDefinition beanDef = getBeanDefinition(
                        ((AbstractApplicationContext) ctx).getBeanFactory(), possibility);
                if (BeanFactoryUtils.isFactoryDereference(possibility) ||
                        possibility.startsWith("scopedTarget.") ||
                        (beanDef != null && !beanDef.isAutowireCandidate())) {
                    it.remove();
                }
            }
        }

        if (names.size() > 1) {
            if (ctx instanceof AbstractApplicationContext) {
                List<String> primaries = new ArrayList<>();
                for (String name : names) {
                    BeanDefinition beanDef = getBeanDefinition(
                            ((AbstractApplicationContext) ctx).getBeanFactory(), name);
                    if (beanDef instanceof AbstractBeanDefinition) {
                        if (beanDef.isPrimary()) {
                            primaries.add(name);
                        }
                    }
                }
                if (primaries.size() == 1) {
                    return primaries.get(0);
                }
            }

            if (generic != null) {
                return null;
            }
            StringJoiner joiner = new StringJoiner(",");
            names.forEach(joiner::add);
            throw new IllegalStateException("More than one bean of type [" + clazz.getName() + "] found, you have to specify the name of the bean " + "(@Inject(name=\"foo\")) or (@Named(\"foo\") if using @javax.inject classes) in order to resolve this conflict. " + "Matched beans: " +  joiner.toString());
        } else if (!names.isEmpty()) {
            return names.get(0);
        }

        return null;
    }

    private BeanDefinition getBeanDefinition(final ConfigurableListableBeanFactory beanFactory,
                                             final String name) {
        if (beanFactory.containsBeanDefinition(name)) {
            return beanFactory.getBeanDefinition(name);
        } else {
            BeanFactory parent = beanFactory.getParentBeanFactory();
            if ((parent != null) && (parent instanceof ConfigurableListableBeanFactory)) {
                return getBeanDefinition((ConfigurableListableBeanFactory) parent, name);
            } else {
                return null;
            }
        }
    }

    public boolean supportsField(final Field field) {
        return field.isAnnotationPresent(Inject.class);
    }

    private ApplicationContext getSpringContext() {
        return springContextLocator.getSpringContext();
    }
}
