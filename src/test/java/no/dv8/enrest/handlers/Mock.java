package no.dv8.enrest.handlers;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Mock<T> {
    T t;
    InvocationHandler handler;
    Map<String, Object> vals = new HashMap<>();
    boolean throwIfUnset = false;

    public Mock(Class<T> clz) {
        handler = (proxy, method, args) -> {
            String key = method.getName();
            if (vals.containsKey(key)) {
                return vals.get(key);
            } else {
                log.info("INVOKE {}.{} => N/A", clz.getSimpleName(), method.getName());
                // maybe?
                // return null;
                if (throwIfUnset)
                    throw new IllegalStateException("INVOKED: " + method.getName());
                else
                    return null;
            }
        };
        t = (T) Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, handler);
    }

    public T instance() {
        return t;
    }


    public void set(String methodName, Object returnValue) {
        vals.put(methodName, returnValue);
    }

    public Mock<T> throwIfUnset() {
        throwIfUnset = true;
        return this;
    }
}
