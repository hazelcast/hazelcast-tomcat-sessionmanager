package com.hazelcast.session;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static org.junit.Assert.assertNotNull;

public class Java6ExcludeRule implements MethodRule {

    private static final boolean EXCLUDED;

    static {
        String propertyName = "java.specification.version";
        String versionProperty = System.getProperty(propertyName);
        assertNotNull(propertyName + " should be set!", versionProperty);

        int version = Integer.parseInt(versionProperty.split("\\.")[1]);
        EXCLUDED = version < 7;
    }

    @Override
    public Statement apply(Statement statement, FrameworkMethod frameworkMethod, Object o) {
        if (EXCLUDED) {
            return new ExcludedStatement(frameworkMethod);
        } else {
            return statement;
        }
    }

    private static final class ExcludedStatement extends Statement {

        private final FrameworkMethod method;

        private ExcludedStatement(FrameworkMethod method) {
            this.method = method;
        }

        @Override
        public void evaluate() throws Throwable {
            String className = method.getMethod().getDeclaringClass().getName();
            String methodName = method.getName();

            System.out.println("EXCLUDING '" + className + "#" + methodName + "()' ...");
        }
    }
}
