package com.qa.mobile.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/** Adds the {@link RetryAnalyzer} to every @Test, so no test has to declare it. */
@SuppressWarnings("rawtypes")
public class RetryTransformer implements IAnnotationTransformer {
  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    annotation.setRetryAnalyzer(RetryAnalyzer.class);
  }
}
