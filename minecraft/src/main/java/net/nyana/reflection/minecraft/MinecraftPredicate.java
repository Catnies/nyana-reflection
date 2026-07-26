package net.nyana.reflection.minecraft;

import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minecraft 版本与补丁条件表达式解析器。
 * <p>
 * 支持以下原子条件：
 * <ul>
 *   <li>{@code min_version=X.Y.Z}  —— 当前版本 ≥ 指定版本</li>
 *   <li>{@code max_version=X.Y.Z}  —— 当前版本 ≤ 指定版本</li>
 *   <li>{@code version=X.Y.Z}      —— 当前版本精确等于指定版本</li>
 *   <li>{@code has_patch=名称}      —— 补丁列表中包含指定补丁</li>
 * </ul>
 * 支持逻辑运算符：
 * <ul>
 *   <li>{@code !}   —— 逻辑非</li>
 *   <li>{@code &&}  —— 逻辑与</li>
 *   <li>{@code ||}  —— 逻辑或</li>
 * </ul>
 * 支持 {@code ( )} 括号改变优先级。运算符优先级从高到低：{@code !} &gt; {@code &&} &gt; {@code ||}。
 * <p>
 * 示例：
 * <pre>{@code
 * min_version=1.19.0 && max_version=1.20.4
 * version=1.19.2 || version=1.19.4
 * !has_patch=deprecated && (min_version=1.19.0 || has_patch=legacy)
 * }</pre>
 * 空表达式或 null 视为 {@code true}。格式不符合 {@code type=value} 的原子条件视为 {@code false}。
 */
public final class MinecraftPredicate implements Predicate<String> {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\s*(\\(|\\)|&&|\\|\\||!|[^\\s()&|!]+)\\s*");
    private final Context context;

    public MinecraftPredicate(String version, List<String> patches) {
        this.context = new Context(parseVersionToInteger(version), patches);
    }

    @Override
    public boolean test(String expression) {
        if (expression == null || expression.isEmpty()) return true;
        return compile(expression).test(this.context);
    }

    private Condition compile(String expression) {
        Matcher matcher = TOKEN_PATTERN.matcher(expression);
        Stack<Condition> nodes = new Stack<>();
        Stack<String> ops = new Stack<>();
        while (matcher.find()) {
            String token = matcher.group(1);
            if (token.isEmpty()) continue;
            switch (token) {
                case "(", "!" -> ops.push(token);
                case ")" -> {
                    while (!ops.isEmpty() && !ops.peek().equals("(")) {
                        processOperator(nodes, ops.pop());
                    }
                    if (!ops.isEmpty()) ops.pop(); // 弹出 "("
                }
                case "&&", "||" -> {
                    while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(token)) {
                        processOperator(nodes, ops.pop());
                    }
                    ops.push(token);
                }
                default -> nodes.push(compileLeaf(token));
            }
        }
        while (!ops.isEmpty()) {
            processOperator(nodes, ops.pop());
        }
        return nodes.isEmpty() ? ctx -> true : nodes.pop();
    }

    private static void processOperator(Stack<Condition> nodes, String op) {
        if ("!".equals(op)) {
            if (nodes.isEmpty()) throw new IllegalArgumentException("Invalid syntax: '!' used without operand");
            Condition node = nodes.pop();
            nodes.push(ctx -> !node.test(ctx));
        } else {
            if (nodes.size() < 2) throw new IllegalArgumentException("Invalid syntax: missing operands for " + op);
            Condition right = nodes.pop();
            Condition left = nodes.pop();
            if ("&&".equals(op)) {
                nodes.push(ctx -> left.test(ctx) && right.test(ctx));
            } else if ("||".equals(op)) {
                nodes.push(ctx -> left.test(ctx) || right.test(ctx));
            }
        }
    }

    private static int precedence(String op) {
        if ("!".equals(op)) return 3;
        if ("&&".equals(op)) return 2;
        if ("||".equals(op)) return 1;
        return 0;
    }

    private static Condition compileLeaf(String token) {
        String[] parts = token.split("=", 2);
        if (parts.length != 2) return ctx -> false;
        String type = parts[0].trim();
        String param = parts[1].trim();
        return switch (type) {
            case "min_version" -> new VersionCheck(param, true);
            case "max_version" -> new VersionCheck(param, false);
            case "version" -> new ExactVersionCheck(param);
            case "has_patch" -> new PatchCheck(param);
            default -> throw new IllegalArgumentException("Invalid predicate: " + token);
        };
    }

    public static int parseVersionToInteger(String versionString) {
        int v1 = 0;
        int v2 = 0;
        int v3 = 0;
        int currentNumber = 0;
        int part = 0;
        for (int i = 0; i < versionString.length(); i++) {
            char c = versionString.charAt(i);
            if (c >= '0' && c <= '9') {
                currentNumber = currentNumber * 10 + (c - '0');
            } else if (c == '.') {
                if (part == 0) {
                    v1 = currentNumber;
                }
                if (part == 1) {
                    v2 = currentNumber;
                }
                part++;
                currentNumber = 0;
                if (part > 2) {
                    break;
                }
            }
        }
        if (part == 0) {
            v1 = currentNumber;
        } else if (part == 1) {
            v2 = currentNumber;
        } else if (part == 2) {
            v3 = currentNumber;
        }
        return v1 * 10000 + v2 * 100 + v3;
    }

    public interface Condition {
        boolean test(Context predicate);
    }

    public record Context(int version, List<String> patches) {
    }

    private static class ExactVersionCheck implements Condition {
        private final int targetVersion;

        public ExactVersionCheck(String version) {
            this.targetVersion = parseVersionToInteger(version);
        }

        @Override
        public boolean test(Context predicate) {
            return predicate.version == targetVersion;
        }
    }

    private static class VersionCheck implements Condition {
        private final int targetVersion;
        private final boolean minOrMax;

        public VersionCheck(String targetVersion, boolean minOrMax) {
            this.targetVersion = parseVersionToInteger(targetVersion);
            this.minOrMax = minOrMax;
        }

        @Override
        public boolean test(Context context) {
            if (this.minOrMax) {
                return context.version >= this.targetVersion;
            } else {
                return context.version <= this.targetVersion;
            }
        }
    }

    private record PatchCheck(String patch) implements Condition {

        @Override
        public boolean test(Context predicate) {
            return predicate.patches().contains(this.patch);
        }
    }
}