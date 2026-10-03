package com.assignment.utils;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.opentest4j.TestAbortedException;

/** Behaviour for {@link KnownDefect}. See that annotation for the rules. */
public class KnownDefectExtension implements TestExecutionExceptionHandler, AfterTestExecutionCallback {

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        KnownDefect defect = defectOf(context);
        if (defect == null || !(throwable instanceof AssertionError)) {
            throw throwable; // only assertion failures count as "the known bug reproduced"
        }
        String message = "KNOWN DEFECT [" + defect.id() + "] " + defect.value()
                + System.lineSeparator() + "Observed: " + throwable.getMessage();
        Allure.label("tag", defect.id());
        throw new TestAbortedException(message, throwable);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        KnownDefect defect = defectOf(context);
        if (defect != null && context.getExecutionException().isEmpty()) {
            throw new AssertionError("Known defect [" + defect.id() + "] no longer reproduces. "
                    + "Verify the fix and remove @KnownDefect from " + context.getDisplayName());
        }
    }

    private static KnownDefect defectOf(ExtensionContext context) {
        return context.getTestMethod().map(m -> m.getAnnotation(KnownDefect.class)).orElse(null);
    }
}

