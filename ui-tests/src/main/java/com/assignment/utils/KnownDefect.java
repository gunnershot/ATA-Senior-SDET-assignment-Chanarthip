package com.assignment.utils;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test whose assertions describe the <b>correct</b> behaviour but which currently fails
 * because of a known application defect.
 * <ul>
 *   <li>Assertion failure &rarr; reported as <i>skipped</i> (aborted) with the defect id and the observed failure,
 *       so CI stays green and the Allure report shows the bug clearly.</li>
 *   <li>Test passes &rarr; reported as <i>failed</i>: the defect no longer reproduces, so remove this annotation.</li>
 *   <li>Any non-assertion error (timeouts, broken locators) still fails normally and isn't hidden.</li>
 * </ul>
 * Tagged {@code known-defect}, so these tests can be filtered: {@code mvn test -DexcludedGroups=known-defect}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Tag("known-defect")
@ExtendWith(KnownDefectExtension.class)
public @interface KnownDefect {

    /** Short defect id, e.g. "BUG-UI-18". */
    String id();

    /** What is wrong, in one sentence. */
    String value();
}

