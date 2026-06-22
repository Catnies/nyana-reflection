# Nyana Reflection
Nyana Reflection 是一个面向 Java 17 的反射辅助库，提供类、字段、方法、构造器查找封装，以及基于 `MethodHandle`、`VarHandle` 和 ASM hidden class 的高性能访问入口。项目还提供注解式反射代理，可以用接口方法绑定目标类的字段、方法和构造器，适合需要跨版本、跨映射名访问内部 API 的场景。

## 快速开始
### 成员查找与字段访问

```java
import net.nyana.reflection.clazz.NyanaClass;
import net.nyana.reflection.field.ASMFieldAccessor;
import net.nyana.reflection.field.NyanaField;

import static net.nyana.reflection.field.matcher.FieldMatchers.*;

final class Target {
    private int value = 1;
}

NyanaField field = NyanaClass.of(Target.class)
        .getDeclaredNyanaField(fAllOf(fNamed("value"), fType(int.class)));

ASMFieldAccessor accessor = field.asm();
Target target = new Target();

accessor.setInt(target, 7);
int value = accessor.getInt(target);
```

### 构造器调用

```java
import net.nyana.reflection.clazz.NyanaClass;
import net.nyana.reflection.constructor.NyanaConstructor;
import net.nyana.reflection.constructor.arity.ConstructorInvoker1;

import static net.nyana.reflection.constructor.matcher.ConstructorMatchers.*;

final class Target {
    private final String name;

    private Target(String name) {
        this.name = name;
    }
}

NyanaConstructor<Target> constructor = NyanaClass.of(Target.class)
        .getDeclaredSparrowConstructor(cTakeArguments(String.class));

ConstructorInvoker1 invoker = constructor.asm$1();
Target target = (Target) invoker.newInstance("nyana");
```

### 注解式代理

```java
import net.nyana.reflection.proxy.ASMProxyFactory;
import net.nyana.reflection.proxy.annotation.ConstructorInvoker;
import net.nyana.reflection.proxy.annotation.FieldGetter;
import net.nyana.reflection.proxy.annotation.FieldSetter;
import net.nyana.reflection.proxy.annotation.MethodInvoker;
import net.nyana.reflection.proxy.annotation.ReflectionProxy;

final class Target {
    private static String label = "unset";
    private int value = 1;

    private Target(String name) {
    }

    private int add(int value) {
        return this.value + value;
    }

    private static String join(String suffix) {
        return label + suffix;
    }
}

@ReflectionProxy(clazz = Target.class)
interface TargetProxy {
    @ConstructorInvoker
    Target create(String name);

    @FieldGetter(name = "value")
    int value(Target target);

    @FieldSetter(name = "value")
    void value(Target target, int value);

    @FieldSetter(name = "label", isStatic = true)
    void label(String label);

    @MethodInvoker(name = "add")
    int add(Target target, int value);

    @MethodInvoker(name = "join", isStatic = true)
    String join(String suffix);
}

TargetProxy proxy = ASMProxyFactory.create(TargetProxy.class);
Target target = proxy.create("created");

proxy.value(target, 7);
proxy.label("nyana");

int sum = proxy.add(target, 3);
String label = proxy.join("-reflection");
```

代理接口支持：

- `@ReflectionProxy(clazz = ...)` 或 `@ReflectionProxy(name = ...)` 指定目标类。
- `@FieldGetter` / `@FieldSetter` 绑定字段读写。
- `@MethodInvoker` 绑定方法调用。
- `@ConstructorInvoker` 绑定构造器调用。
- `activeIf` 配合 `NyanaReflection.setActivePredicate(...)` 过滤版本或环境条件。
- `optional = true` 允许缺失成员时跳过绑定。
- `@Type` 覆盖代理方法参数对应的真实目标类型。

当代理接口与目标类位于同一个 `ClassLoader` 时，代理实现会以目标类 nestmate hidden class 的形式生成；跨 `ClassLoader` 时会回退到 MethodHandle writer，并将 generated class 定义到代理接口侧，避免父子加载器无法解析接口的问题。

### 重映射

默认情况下，名称映射是 no-op：

```java
import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.remapper.Remapper;

NyanaReflection.setRemapper(Remapper.noOp());
```

可以用 Mapping IO 文件创建 remapper：

```java
import java.nio.file.Path;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.remapper.Remapper;

NyanaReflection.setRemapper(
        Remapper.loadMappingIo(Path.of("mappings.tiny"), "named", "intermediary")
);
```

`NyanaClass.find(...)`、`@ReflectionProxy(name = ...)`、字段名和方法名匹配都会经过当前 `Remapper`。

## Gradle

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.catnies.top/releases")
}

dependencies {
    // 运行环境没有 ASM 时需要显式提供。
    implementation("org.ow2.asm:asm:9.9.1")
    implementation("net.nyana:nyana-reflection:{latest_version}")
}
```
