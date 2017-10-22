package com.github.sabomichal.springinjector;

import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Convenience class to extract information about the type and generics of a field to inject.
 * The field is a List, a Map or a Set and the generic type of its elements is extracted as well
 *
 * @author Tobias Soloschenko
 * @author Andrea Del Bene
 */
class FieldBeansCollector {
    private final FieldType fieldType;

    private final Map<Object, Object> beansToInjectMap;

    private final Collection<Object> beansToInjectColl;

    public enum FieldType {
        LIST, SET, MAP, NONE
    }

    FieldBeansCollector(final ResolvableType fieldResolvableType) {
        Class<?> clazz = fieldResolvableType.resolve();

        // The required code starts here which replaces
        if (clazz == Map.class) {
            fieldType = FieldType.MAP;
            // the getGeneric has to be called with 1 because the map contains the typified
            // information in the value generic
            beansToInjectColl = null;
            beansToInjectMap = new HashMap<>();
        } else if (clazz == Set.class) {
            fieldType = FieldType.SET;
            beansToInjectColl = new HashSet<>();
            beansToInjectMap = null;
        } else if (clazz == List.class) {
            fieldType = FieldType.LIST;
            beansToInjectColl = new ArrayList<>();
            beansToInjectMap = null;
        } else {
            fieldType = FieldType.NONE;
            beansToInjectColl = null;
            beansToInjectMap = null;
        }
    }

    /**
     * Returns an instance containing all the beans collected for the field and
     * compatible with the type of the field.
     *
     * @return the instance to inject into the field.
     */
    Object getBeansToInject() {
        if (beansToInjectMap != null && beansToInjectMap.size() > 0) {
            return beansToInjectMap;
        }

        if (beansToInjectColl != null && beansToInjectColl.size() > 0) {
            return beansToInjectColl;
        }

        return null;
    }

    /**
     * Adds compatible bean to the field. This means that the field type is Map, a List or a Set
     * and that the given bean is compatible with its elements type.
     *
     * @param beanName the name of the bean to inject
     * @param bean     the bean to inject
     */
    void addBean(String beanName, Object bean) {
        switch (fieldType) {
            case LIST:
            case SET:
                beansToInjectColl.add(bean);
                break;
            case MAP:
                beansToInjectMap.put(beanName, bean);
                break;
            default:
                break;
        }
    }
}
