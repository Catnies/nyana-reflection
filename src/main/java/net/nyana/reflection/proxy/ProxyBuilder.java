package net.nyana.reflection.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

interface ProxyBuilder {

    void writeFieldGetter(Method method, Field field);

    void writeFieldSetter(Method method, Field field);

    void writeMethod(Method proxyMethod, Method targetMethod);

    void writeConstructor(Method method, Constructor<?> constructor);
}
