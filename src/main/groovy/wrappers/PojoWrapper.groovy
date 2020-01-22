package wrappers

import net.sf.cglib.proxy.Enhancer
import net.sf.cglib.proxy.MethodInterceptor
import traits.Undoable

import java.beans.Introspector
import java.beans.PropertyDescriptor

class PojoWrapper {

    // TODO support for non-blocks? (supply a config setting when calling wrap())

    static <T> T wrap(Class<T> clazz) {
        if (!clazz.getInterfaces().contains(Undoable.class)) {
            throw new IllegalArgumentException("Classes passed to wrap() must implement pack.Undoable")
        }
        Enhancer enhancer = new Enhancer()
        enhancer.setSuperclass(clazz)
        enhancer.setCallback({ obj, method, args, proxy ->
            if (!method.isSynthetic()) {
                for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                    if (propertyDescriptor.getWriteMethod() == method) {
                        if (!clazz.cast(obj).freeze) {
                            if (!clazz.cast(obj).inProgress) {
                                throw new IllegalStateException("Did you forget to call start() before setting values?")
                            }
                            (clazz.cast(obj).tuples << new Tuple2(propertyDescriptor.getWriteMethod(), propertyDescriptor.getReadMethod().invoke(obj)))
                        }
                    }
                }
            }
            return proxy.invokeSuper(obj, args)
        } as MethodInterceptor)
        return (T) enhancer.create()
    }

}