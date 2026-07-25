package net.nyana.reflection.proxy;

import net.nyana.reflection.condition.CustomCondition;
import net.nyana.reflection.condition.annotation.Condition;
import net.nyana.reflection.condition.ConditionChecker;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ConditionCheckerTest {
    @Test
    void should_Match_When_AllConditionsPass() throws Exception {
        Condition[] conditions = conditionsOn("allPass");

        assertTrue(ConditionChecker.matches(conditions));
    }

    @Test
    void should_NotMatch_When_AnyConditionFails() throws Exception {
        Condition[] conditions = conditionsOn("oneFails");

        assertFalse(ConditionChecker.matches(conditions));
    }

    private static Condition[] conditionsOn(String methodName) throws NoSuchMethodException {
        Method method = ConditionCheckerTest.class.getDeclaredMethod(methodName);
        return method.getAnnotation(Conditional.class).value();
    }

    @Conditional({
            @Condition(type = EqualsCondition.class, value = "expected"),
            @Condition(type = EqualsCondition.class, value = "expected")
    })
    private static void allPass() {
    }

    @Conditional({
            @Condition(type = EqualsCondition.class, value = "expected"),
            @Condition(type = EqualsCondition.class, value = "different")
    })
    private static void oneFails() {
    }

    @java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    private @interface Conditional {
        Condition[] value();
    }

    public static final class EqualsCondition extends CustomCondition {
        @Override
        public boolean check(@NonNull String[] value) {
            return "expected".equals(value[0]);
        }
    }
}
