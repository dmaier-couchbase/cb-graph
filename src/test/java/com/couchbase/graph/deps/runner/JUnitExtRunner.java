/*
 * Copyright 2015 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.graph.deps.runner;

import com.couchbase.graph.deps.annotation.Context;
import com.couchbase.graph.deps.IChecker;
import com.couchbase.graph.deps.IPrecondition;
import com.couchbase.graph.deps.annotation.Preconditions;
import com.couchbase.graph.deps.annotation.RunIf;
import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.internal.runners.TestMethod;
import org.junit.internal.runners.MethodRoadie;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.Failure;
import org.junit.runner.Description;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;

public final class JUnitExtRunner extends JUnit4ClassRunner {
    private final boolean shouldRunTest4Class;

    public JUnitExtRunner(Class<?> klass) throws InitializationError {
        super(klass);
        shouldRunTest4Class = isPrerequisiteSatisfiedForClass(klass);
    }

    @Override
    protected void invokeTestMethod(Method method, RunNotifier notifier) {
        if (shouldRunTest4Class && isPrereuisitSatisfied(method)) {
            Description description = methodDescription(method);
            Object test = createTest(notifier, description);
            if (test == null) {
                return;
            }
            
            List<IPrecondition> list = createPrecondtions(method, test);
            List<Exception> possibleExceptions = new ArrayList<>();
            int failedAt = invokeSetupForPreconditions(list, possibleExceptions);
            try {
                if (arePreconditionsSetUpSucceed(failedAt)) {
                    TestMethod testMethod = wrapMethod(method);
                    new MethodRoadie(test, testMethod, notifier, description).run();
                } else {
                    notifier.fireTestStarted(description);
                    notifier.fireTestStarted(description);
                }
            } finally {
                failedAt = arePreconditionsSetUpSucceed(failedAt) ? list.size() : failedAt;
                for (int i = 0; i < failedAt; i++) {
                    try {
                        IPrecondition precondition = list.get(i);
                        precondition.teardown();
                    } catch (Exception e) {
                        possibleExceptions.add(e);
                    }
                }
            }
            if (!possibleExceptions.isEmpty()) {
                for (Exception e : possibleExceptions) {
                    notifier.fireTestFailure(new Failure(description, e));
                }
            }
        } else {
            Description testDescription = Description.createTestDescription(this.getTestClass().getJavaClass(),
                    method.getName());
            notifier.fireTestIgnored(testDescription);
        }
    }

    private boolean arePreconditionsSetUpSucceed(int failedAt) {
        return failedAt == -1;
    }

    private int invokeSetupForPreconditions(List<IPrecondition> list, List<Exception> possibleExceptions) {
        int failedAt = -1;
        int currentIndex = 0;
        for (IPrecondition precondition : list) {
            currentIndex++;
            try {
                precondition.setup();
            } catch (Exception e) {
                possibleExceptions.add(e);
                failedAt = currentIndex;
                break;
            }
        }
        return failedAt;
    }

    private List<IPrecondition> createPrecondtions(Method method, Object test) {
        Class<?> declaringClass = method.getDeclaringClass();
        Field[] declaredFields = declaringClass.getDeclaredFields();
        Object context = null;
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Context.class)) {
                try {
                    declaredField.setAccessible(true);
                    context = declaredField.get(test);
                } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        Preconditions preconditions = method.getAnnotation(Preconditions.class);
        ArrayList<IPrecondition> preconditionsAsList = new ArrayList<>();
        if (preconditions == null) {
            return preconditionsAsList;
        }
        Class<? extends IPrecondition>[] classes = preconditions.value();
        for (Class<? extends IPrecondition> aClass : classes) {
            IPrecondition precondition;
            try {
                if (context != null) {
                    Constructor<? extends IPrecondition> constructor = aClass.getConstructor(Object.class);
                    precondition = constructor.newInstance(context);
                } else {
                    precondition = aClass.newInstance();
                }
                preconditionsAsList.add(precondition);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return preconditionsAsList;
    }

    public boolean isPrereuisitSatisfied(Method method) {
        RunIf resource = method.getAnnotation(RunIf.class);
        if (resource == null) {
            return true;
        }
        Class<? extends IChecker> prerequisiteChecker = resource.value();
        try {
            IChecker checker = instantiateChecker(resource, prerequisiteChecker);
            return checker.satisfy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isArgumentNotProvided(String[] argument) {
        return argument == null || argument.length == 0;
    }

    public Object createTest(RunNotifier notifier, Description description) {
        Object test = null;
        try {
            try {
                test = createTest();
            } catch (InvocationTargetException e) {
                testAborted(notifier, description, e.getCause());
                return null;
            } catch (Exception e) {
                testAborted(notifier, description, e);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return test;
    }


    private void testAborted(RunNotifier notifier, Description description,
                             Throwable e) {
        notifier.fireTestStarted(description);
        notifier.fireTestFailure(new Failure(description, e));
        notifier.fireTestFinished(description);
    }

    public boolean isPrerequisiteSatisfiedForClass(Class<?> klass) {
        RunIf resource = klass.getAnnotation(RunIf.class);
        if (resource == null) {
            return true;

        }
        Class<? extends IChecker> prerequisiteChecker = resource.value();
        try {
            IChecker checker = instantiateChecker(resource, prerequisiteChecker);
            return checker.satisfy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private IChecker instantiateChecker(RunIf resource, Class<? extends IChecker> prerequisiteChecker) throws Exception {
        String[] arguments = resource.arguments();
        IChecker checker;
        if (isArgumentNotProvided(arguments)) {
            checker = prerequisiteChecker.newInstance();
        } else {
            if (arguments.length == 1) {
                Constructor<? extends IChecker> constructor = prerequisiteChecker.getConstructor(String.class);
                checker = constructor.newInstance(arguments[0]);
            } else {
                Constructor<? extends IChecker> constructor = prerequisiteChecker.getConstructor(String[].class);
                checker = constructor.newInstance(new Object[]{arguments});
            }
        }
        return checker;
    }

}
