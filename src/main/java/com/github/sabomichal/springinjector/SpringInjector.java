package com.github.sabomichal.springinjector;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * SpringInjector scans fields of an object instance and checks if the specified
 * {@link AnnotFieldValueFactory} can provide a value for a field; if it can, the field is set to that
 * value. SpringInjector will ignore all non-null fields.
 *
 * @author Igor Vaynberg (ivaynberg)
 * @author Michal Sabo
 */
@Component
public class SpringInjector {

    @Inject
    private ApplicationContext applicationContext;

    private static SpringInjector instance;

    private final ClassMetaCache<Field[]> cache = new ClassMetaCache<>();
    private IFieldValueFactory fieldValueFactory = new AnnotFieldValueFactory(new ContextLocator());

    private SpringInjector() {
        instance = this;
    }

    public static SpringInjector get() {
        return instance;
    }

    /**
     * Injects the specified object. This method is usually implemented by delegating to
     * {@link #inject(Object, IFieldValueFactory)} with some {@link AnnotFieldValueFactory}
     *
     * @param object object to inject
     * @see #inject(Object, IFieldValueFactory)
     */
    public void inject(final Object object) {
        inject(object, fieldValueFactory);
    }

    /**
     * traverse fields in the class hierarchy of the object and set their value with a locator
     * provided by the locator factory.
     *
     * @param object object to traverse
     * @param factory locator factory
     */
    protected void inject(final Object object, final IFieldValueFactory factory) {
        final Class<?> clazz = object.getClass();
        Field[] fields;

        // try cache
        fields = cache.get(clazz);
        if (fields == null) {
            // cache miss, discover fields
            fields = findFields(clazz, factory);
            // write to cache
            cache.put(clazz, fields);
        }

        for (final Field field : fields) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            try {
                if (field.get(object) == null) {
                    Object value = factory.getFieldValue(field);

                    if (value != null) {
                        field.set(object, value);
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException("error while injecting object [" + object.toString() + "] of type [" + object.getClass().getName() + "]", e);
            }
        }
    }

    /**
     * Returns an array of fields that can be injected using the given field value factory
     *
     * @param clazz
     * @param factory
     * @return an array of fields that can be injected using the given field value factory
     */
    private Field[] findFields(Class<?> clazz, final IFieldValueFactory factory) {
        List<Field> matched = new ArrayList<>();

        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (final Field field : fields) {
                if (factory.supportsField(field)) {
                    matched.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }

        return matched.toArray(new Field[matched.size()]);
    }

    ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    private static class ContextLocator implements ISpringContextLocator {
        private static final long serialVersionUID = 1L;

        @Override
        public ApplicationContext getSpringContext() {
            return SpringInjector.get().getApplicationContext();
        }
    }
}
