package com.cryptomorin.xseries.reflection.jvm;

import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class MethodMemberHandle extends NamedMemberHandle {
    protected Class<?>[] parameterTypes = new Class[0];

    public MethodMemberHandle(ClassHandle clazz) {
        super(clazz);
    }

    public MethodMemberHandle parameters(ClassHandle... parameterTypes) {
        this.parameterTypes = Arrays.stream(parameterTypes).map(ClassHandle::unreflect).toArray(Class[]::new);
        return this;
    }

    public MethodMemberHandle returns(Class<?> clazz) {
        super.returns(clazz);
        return this;
    }

    public MethodMemberHandle returns(ClassHandle clazz) {
        super.returns(clazz);
        return this;
    }

    public MethodMemberHandle asStatic() {
        super.asStatic();
        return this;
    }

    public MethodMemberHandle parameters(Class<?>... parameterTypes) {
        this.parameterTypes = parameterTypes;
        return this;
    }

    @Override
    public MethodHandle reflect() throws ReflectiveOperationException {
        return lookup.unreflect(reflectJvm());
    }

    public MethodMemberHandle named(String... names) {
        super.named(names);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Method reflectJvm() throws ReflectiveOperationException {
        Objects.requireNonNull(returnType, "Return type not specified");
        if (names.isEmpty()) throw new IllegalStateException("No names specified");

        NoSuchMethodException errors = null;
        Method method = null;

        Class<?> clazz = this.clazz.unreflect();
        for (String name : this.names) {
            if (method != null) break;
            try {
                method = clazz.getDeclaredMethod(name, parameterTypes);
                if (method.getReturnType() != this.returnType) {
                    throw new NoSuchMethodException("Method named '" + name + "' was found but the types don't match: " + this.returnType + " != " + method);
                }
            } catch (NoSuchMethodException ex) {
                method = null;
                if (errors == null) errors = new NoSuchMethodException("None of the methods were found for " + this);
                errors.addSuppressed(ex);
            }
        }

        if (method == null) throw errors;
        return handleAccessible(method);
    }

    @Override
    public String toString() {
        String str = this.getClass().getSimpleName() + '{';
        if (makeAccessible) str += "protected/private ";
        if (isFinal) str += "final ";
        if (returnType != null) str += returnType.getSimpleName() + ' ';
        str += String.join("/", names);
        str += '(' + Arrays.stream(parameterTypes).map(Class::getSimpleName).collect(Collectors.joining(", ")) + ')';
        return str + '}';
    }
}
