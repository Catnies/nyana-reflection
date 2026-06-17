package net.nyana.reflection.type.matcher;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * 通配符类型匹配器: 匹配 ? extends/super 形式的通配符, 可分别校验其上界与下界.
 * ----------------------------------------------------------------
 * 通配符	            getUpperBounds()	getLowerBounds()
 * ?	                [Object]	        []（空）
 * ? extends Number	    [Number]	        []（空）
 * ? super Integer	    [Object]	        [Integer]
 */
final class WildCardMatcher implements TypeMatcher {
    public static final WildCardMatcher SIMPLE = new WildCardMatcher(null, null); // 不约束上下界, 只判定是否为通配符
    private final TypeMatcher[] upper; // 上界(extends) 匹配器, null 表示不校验
    private final TypeMatcher[] lower; // 下界(super) 匹配器, null 表示不校验

    WildCardMatcher(@Nullable TypeMatcher[] upper, @Nullable TypeMatcher[] lower) {
        this.upper = upper;
        this.lower = lower;
    }

    @Override
    public boolean matches(Type type) {
        if (!(type instanceof WildcardType wildcardType)) {
            return false;
        }

        // 逐个校验上界(? extends ...), 个数需一致
        if (this.upper != null && this.upper.length != 0) {
            Type[] upperBounds = wildcardType.getUpperBounds();
            if (upperBounds.length != this.upper.length) {
                return false;
            }
            for (int i = 0; i < upperBounds.length; i++) {
                if (!this.upper[i].matches(upperBounds[i])) {
                    return false;
                }
            }
        }

        // 逐个校验下界(? super ...), 个数需一致
        if (this.lower != null && this.lower.length != 0) {
            Type[] lowerBounds = wildcardType.getLowerBounds();
            if (lowerBounds.length != this.lower.length) {
                return false;
            }
            for (int i = 0; i < lowerBounds.length; i++) {
                if (!this.lower[i].matches(lowerBounds[i])) {
                    return false;
                }
            }
        }

        return true;
    }
}
