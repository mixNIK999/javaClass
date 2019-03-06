package task;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Injector {

    /**
     * Create and initialize object of `rootClassName` class using classes from
     * `implementationClassNames` for concrete dependencies.
     */
    public static Object initialize(String rootClassName, List<String> implementationClassNames) throws Exception {
        Class<?> rootClass = Class.forName(rootClassName);
        return initClass(rootClass, implementationClassNames, new HashMap<>());
    }

    private static Object initClass(@NotNull Class<?> rootClass, List<String> implementationClassNames, Map<Class, Object> dependenciesMap)
            throws ClassNotFoundException, ImplementationNotFoundException, AmbiguousImplementationException {
//        var dependenciesMap = new HashMap<Class, Object>();
        List<Object> initList = new ArrayList<>();
        for (Constructor constructor : rootClass.getDeclaredConstructors()) {
            for (Class paramType : constructor.getParameterTypes()) {
                Class implemType = findImplement(paramType, implementationClassNames);
                if (!dependenciesMap.containsKey(implemType)) {
                    dependenciesMap.put(implemType, initClass(implemType, implementationClassNames, dependenciesMap));
                }
                initList.add(dependenciesMap.get(implemType));
            }
        }
        return dependenciesMap;
    }

    private static Class<?> findImplement(Class<?> Param, @NotNull List<String> implementationClassNames)
            throws ClassNotFoundException, ImplementationNotFoundException, AmbiguousImplementationException {
        Class result = null;
        for (String implName : implementationClassNames) {
            if (Class.forName(implName).isInstance(Param)) {
                if (result == null) {
                    result = Class.forName(implName);
                } else {
                    throw new AmbiguousImplementationException();
                }
            }
        }
        if (result == null)
            throw new ImplementationNotFoundException();
        return result;
    }
}