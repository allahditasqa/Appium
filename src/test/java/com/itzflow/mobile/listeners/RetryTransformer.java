package com.itzflow.mobile.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/** Har @Test par RetryAnalyzer lagata hai (taake har test mein alag se likhna na pade). */
@SuppressWarnings("rawtypes")
public class RetryTransformer implements IAnnotationTransformer {
  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    annotation.setRetryAnalyzer(RetryAnalyzer.class);
  }
}
