package net.nyana.reflection.proxy;

import net.nyana.reflection.proxy.annotation.ConstructorInvoker;
import net.nyana.reflection.proxy.annotation.FieldGetter;
import net.nyana.reflection.proxy.annotation.FieldSetter;
import net.nyana.reflection.proxy.annotation.MethodInvoker;
import net.nyana.reflection.proxy.annotation.ReflectionProxy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ASMProxyFactoryTest {

    @TempDir
    Path tempDir;

    @Test
    void should_CreateAsmProxy_When_InterfaceUsesProxyAnnotations() {
        TargetProxy proxy = ASMProxyFactory.create(TargetProxy.class);
        Target target = new Target("initial");

        assertNotNull(proxy);
        assertEquals(1, proxy.value(target));

        proxy.value(target, 7);
        assertEquals(7, proxy.value(target));
        assertEquals(10, proxy.add(target, 3));

        proxy.label("nyana");
        assertEquals("nyana", proxy.label());
        assertEquals("nyana-reflection", proxy.join("-reflection"));

        proxy.rename(target, "renamed");
        assertEquals("renamed", target.name);

        Target constructed = proxy.create("created");
        assertEquals("created", constructed.name);
    }

    @Test
    void should_CreateAsmProxy_When_ProxyInterfaceIsLoadedByChildLoaderAndTargetIsLoadedByParentLoader() throws Exception {
        Path parentClasses = this.tempDir.resolve("parent-classes");
        Path childClasses = this.tempDir.resolve("child-classes");

        // 目标类只放在父 loader, proxy 接口只放在子 loader, 用来复现跨 loader fallback 场景
        compile(
                parentClasses,
                "loaderfixture.ParentLoadedTarget",
                """
                        package loaderfixture;

                        public final class ParentLoadedTarget {
                            public static String ping() {
                                return "parent";
                            }
                        }
                        """
        );
        compile(
                childClasses,
                "loaderfixture.ChildLoadedProxy",
                """
                        package loaderfixture;

                        import net.nyana.reflection.proxy.annotation.MethodInvoker;
                        import net.nyana.reflection.proxy.annotation.ReflectionProxy;

                        @ReflectionProxy(name = "loaderfixture.ParentLoadedTarget")
                        public interface ChildLoadedProxy {
                            @MethodInvoker(name = "ping", isStatic = true)
                            String ping();
                        }
                        """
        );

        try (
                URLClassLoader parentLoader = new URLClassLoader(
                        new URL[]{parentClasses.toUri().toURL()},
                        ASMProxyFactoryTest.class.getClassLoader()
                );
                URLClassLoader childLoader = new URLClassLoader(
                        new URL[]{childClasses.toUri().toURL()},
                        parentLoader
                )
        ) {
            Class.forName("loaderfixture.ParentLoadedTarget", true, parentLoader);
            Class<?> proxyType = Class.forName("loaderfixture.ChildLoadedProxy", true, childLoader);

            // 旧实现会把生成类定义到父 loader, 导致父 loader 解析不到子 loader 的 proxy 接口
            Object proxy = assertDoesNotThrow(() -> ASMProxyFactory.create(proxyType));
            Method ping = proxyType.getMethod("ping");
            assertEquals("parent", ping.invoke(proxy));
        }
    }

    @Test
    void should_UseNestmateWriter_When_ProxyAndTargetUseSameClassLoader() throws Exception {
        ProxyDefinition definition = new ProxyDefinition(TargetProxy.class, Target.class, List.of());
        ProxyClassBytes bytes = writeWith("NestmateProxyClassWriter", definition);

        assertSame(Target.class, bytes.lookupHost());
    }

    @Test
    void should_UseMethodHandleWriter_When_ProxyAndTargetUseDifferentClassLoaders() throws Exception {
        try (LoadedFixture fixture = loadParentTargetAndChildProxy("planner")) {
            ProxyDefinition definition = new ProxyDefinition(fixture.proxyType(), fixture.targetType(), List.of());
            ProxyClassBytes bytes = writeWith("MethodHandleProxyClassWriter", definition);

            assertSame(fixture.proxyType(), bytes.lookupHost());
        }
    }

    @Test
    void should_CreateMethodHandleFallbackProxy_For_AllBindingTypesAcrossLoaders() throws Exception {
        try (LoadedFixture fixture = loadParentTargetAndChildProxy("fallback")) {
            Class<?> proxyType = fixture.proxyType();

            // 同一个 fallback proxy 覆盖字段, 方法, 构造器和 final 字段 setter, 避免只验证静态方法的窄场景
            Object proxy = assertDoesNotThrow(() -> ASMProxyFactory.create(proxyType));

            Method create = proxyType.getMethod("create", String.class);
            Object target = create.invoke(proxy, "created");

            assertEquals(1, proxyType.getMethod("value", Object.class).invoke(proxy, target));
            proxyType.getMethod("value", Object.class, int.class).invoke(proxy, target, 8);
            assertEquals(8, proxyType.getMethod("value", Object.class).invoke(proxy, target));
            assertEquals(10, proxyType.getMethod("add", Object.class, int.class).invoke(proxy, target, 2));

            proxyType.getMethod("label", String.class).invoke(proxy, "nyana");
            assertEquals("nyana", proxyType.getMethod("label").invoke(proxy));
            assertEquals("nyana-reflection", proxyType.getMethod("join", String.class).invoke(proxy, "-reflection"));

            assertEquals("created", proxyType.getMethod("finalName", Object.class).invoke(proxy, target));
            proxyType.getMethod("finalName", Object.class, String.class).invoke(proxy, target, "changed");
            assertEquals("changed", proxyType.getMethod("finalName", Object.class).invoke(proxy, target));

            assertEquals(null, proxyType.getMethod("rename", Object.class, String.class).invoke(proxy, target, "renamed"));
            assertEquals("renamed", proxyType.getMethod("name", Object.class).invoke(proxy, target));
        }
    }

    @Test
    void should_DefineFallbackProxyInProxyPackage_When_TargetAndProxyPackagesDiffer() throws Exception {
        Path parentClasses = this.tempDir.resolve("cross-package-parent-classes");
        Path childClasses = this.tempDir.resolve("cross-package-child-classes");

        compile(
                parentClasses,
                "targetfixture.CrossPackageTarget",
                """
                        package targetfixture;

                        public final class CrossPackageTarget {
                            public static String ping() {
                                return "cross-package";
                            }
                        }
                        """
        );
        compile(
                childClasses,
                "proxyfixture.CrossPackageProxy",
                """
                        package proxyfixture;

                        import net.nyana.reflection.proxy.annotation.MethodInvoker;
                        import net.nyana.reflection.proxy.annotation.ReflectionProxy;

                        @ReflectionProxy(name = "targetfixture.CrossPackageTarget")
                        public interface CrossPackageProxy {
                            @MethodInvoker(name = "ping", isStatic = true)
                            String ping();
                        }
                        """
        );

        try (
                URLClassLoader parentLoader = new URLClassLoader(
                        new URL[]{parentClasses.toUri().toURL()},
                        ASMProxyFactoryTest.class.getClassLoader()
                );
                URLClassLoader childLoader = new URLClassLoader(
                        new URL[]{childClasses.toUri().toURL()},
                        parentLoader
                )
        ) {
            Class.forName("targetfixture.CrossPackageTarget", true, parentLoader);
            Class<?> proxyType = Class.forName("proxyfixture.CrossPackageProxy", true, childLoader);

            Object proxy = assertDoesNotThrow(() -> ASMProxyFactory.create(proxyType));

            assertTrue(proxyType.isInstance(proxy));
            assertEquals("cross-package", proxyType.getMethod("ping").invoke(proxy));
        }
    }

    @Test
    void should_CreateProxyForChildInterface_When_ParentAndChildDefineSameProxyName() throws Exception {
        Path parentClasses = this.tempDir.resolve("shadow-parent-classes");
        Path childClasses = this.tempDir.resolve("shadow-child-classes");

        compile(parentClasses, "loaderfixture.ShadowTarget", targetSource("ShadowTarget", "shadow"));
        compile(childClasses, "loaderfixture.ShadowProxy", proxySource("ShadowProxy", "loaderfixture.ShadowTarget"));
        compile(parentClasses, "loaderfixture.ShadowProxy", proxySource("ShadowProxy", "loaderfixture.ShadowTarget"));

        try (
                URLClassLoader parentLoader = new URLClassLoader(
                        new URL[]{parentClasses.toUri().toURL()},
                        ASMProxyFactoryTest.class.getClassLoader()
                );
                ChildFirstFixtureClassLoader childLoader = new ChildFirstFixtureClassLoader(
                        new URL[]{childClasses.toUri().toURL()},
                        parentLoader
                )
        ) {
            Class<?> targetType = Class.forName("loaderfixture.ShadowTarget", true, parentLoader);
            Class<?> parentProxyType = Class.forName("loaderfixture.ShadowProxy", true, parentLoader);
            Class<?> childProxyType = Class.forName("loaderfixture.ShadowProxy", true, childLoader);

            assertSame(parentLoader, targetType.getClassLoader());
            assertSame(parentLoader, parentProxyType.getClassLoader());
            assertSame(childLoader, childProxyType.getClassLoader());

            // 同名接口在父子 loader 中各有一份时, 必须以调用方传入的 child proxy 为准
            ProxyClassBytes bytes = writeWith("MethodHandleProxyClassWriter", new ProxyDefinition(childProxyType, targetType, List.of()));
            assertSame(childProxyType, bytes.lookupHost());

            Object proxy = ASMProxyFactory.create(childProxyType);

            assertTrue(childProxyType.isInstance(proxy));
            assertEquals("shadow", childProxyType.getMethod("ping").invoke(proxy));
        }
    }

    private LoadedFixture loadParentTargetAndChildProxy(String prefix) throws Exception {
        Path parentClasses = this.tempDir.resolve(prefix + "-parent-classes");
        Path childClasses = this.tempDir.resolve(prefix + "-child-classes");
        String targetName = capitalize(prefix) + "Target";
        String proxyName = capitalize(prefix) + "Proxy";

        // fixture 名字带前缀, 避免同一个测试类中不同 URLClassLoader 复用同名 class 文件
        compile(parentClasses, "loaderfixture." + targetName, targetSource(targetName, prefix));
        compile(childClasses, "loaderfixture." + proxyName, proxySource(proxyName, "loaderfixture." + targetName));

        URLClassLoader parentLoader = new URLClassLoader(
                new URL[]{parentClasses.toUri().toURL()},
                ASMProxyFactoryTest.class.getClassLoader()
        );
        URLClassLoader childLoader = new URLClassLoader(
                new URL[]{childClasses.toUri().toURL()},
                parentLoader
        );
        Class<?> targetType = Class.forName("loaderfixture." + targetName, true, parentLoader);
        Class<?> proxyType = Class.forName("loaderfixture." + proxyName, true, childLoader);
        return new LoadedFixture(targetType, proxyType, parentLoader, childLoader);
    }

    private static String targetSource(String simpleName, String pingValue) {
        return """
                package loaderfixture;

                public final class %s {
                    private static String label = "unset";

                    private int value = 1;
                    private String name;
                    private final String finalName;

                    private %s(String name) {
                        this.name = name;
                        this.finalName = name;
                    }

                    private int add(int value) {
                        return this.value + value;
                    }

                    private static String join(String suffix) {
                        return label + suffix;
                    }

                    private void rename(String name) {
                        this.name = name;
                    }

                    private String name() {
                        return this.name;
                    }

                    private static String ping() {
                        return "%s";
                    }
                }
                """.formatted(simpleName, simpleName, pingValue);
    }

    private static String proxySource(String simpleName, String targetName) {
        return """
                package loaderfixture;

                import net.nyana.reflection.proxy.annotation.ConstructorInvoker;
                import net.nyana.reflection.proxy.annotation.FieldGetter;
                import net.nyana.reflection.proxy.annotation.FieldSetter;
                import net.nyana.reflection.proxy.annotation.MethodInvoker;
                import net.nyana.reflection.proxy.annotation.ReflectionProxy;

                @ReflectionProxy(name = "%s")
                public interface %s {
                    @ConstructorInvoker
                    Object create(String name);

                    @FieldGetter(name = "value")
                    int value(Object target);

                    @FieldSetter(name = "value")
                    void value(Object target, int value);

                    @FieldGetter(name = "label", isStatic = true)
                    String label();

                    @FieldSetter(name = "label", isStatic = true)
                    void label(String label);

                    @FieldGetter(name = "finalName")
                    String finalName(Object target);

                    @FieldSetter(name = "finalName")
                    void finalName(Object target, String name);

                    @MethodInvoker(name = "add")
                    int add(Object target, int value);

                    @MethodInvoker(name = "join", isStatic = true)
                    String join(String suffix);

                    @MethodInvoker(name = "rename")
                    Object rename(Object target, String name);

                    @MethodInvoker(name = "name")
                    String name(Object target);

                    @MethodInvoker(name = "ping", isStatic = true)
                    String ping();
                }
                """.formatted(targetName, simpleName);
    }

    private static String capitalize(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * 通过反射调用内部 writer, 避免测试编译期直接依赖 ASM compileOnly 类型.
     */
    private static ProxyClassBytes writeWith(String writerSimpleName, ProxyDefinition definition) throws Exception {
        String packageName = ASMProxyFactoryTest.class.getPackageName();
        Class<?> writerClass = Class.forName(packageName + "." + writerSimpleName);
        java.lang.reflect.Constructor<?> constructor = writerClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object writer = constructor.newInstance();

        Method write = Class.forName(packageName + ".ProxyClassWriter").getDeclaredMethod("write", ProxyDefinition.class);
        write.setAccessible(true);
        return (ProxyClassBytes) write.invoke(writer, definition);
    }

    /**
     * 编译测试期 Java 源码到指定输出目录, 让每个测试可以精确控制 class 文件属于哪个 loader.
     */
    private static void compile(Path outputDir, String className, String source) throws Exception {
        Path sourceFile = outputDir.resolve(className.replace('.', '/') + ".java");
        Files.createDirectories(sourceFile.getParent());
        Files.writeString(sourceFile, source, StandardCharsets.UTF_8);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "Tests must run on a JDK because this test compiles loader fixtures");

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(List.of(sourceFile.toFile()));
            List<String> options = List.of(
                    "-d", outputDir.toString(),
                    "-classpath", System.getProperty("java.class.path"),
                    "--release", "17"
            );

            boolean compiled = Boolean.TRUE.equals(compiler.getTask(null, fileManager, diagnostics, options, null, units).call());
            assertTrue(compiled, diagnostics.getDiagnostics().toString());
        }
    }

    /**
     * 保存动态 fixture 的两个 URLClassLoader, 便于测试结束时主动关闭文件句柄.
     */
    private record LoadedFixture(
            Class<?> targetType,
            Class<?> proxyType,
            URLClassLoader parentLoader,
            URLClassLoader childLoader
    ) implements AutoCloseable {
        @Override
        public void close() throws Exception {
            this.childLoader.close();
            this.parentLoader.close();
        }
    }

    /**
     * 只对 loaderfixture 包使用 child-first 策略, 用来制造父子 loader 各自定义同名 proxy 的 shadow-class 场景.
     */
    private static final class ChildFirstFixtureClassLoader extends URLClassLoader {
        private ChildFirstFixtureClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.startsWith("loaderfixture.")) {
                return super.loadClass(name, resolve);
            }

            synchronized (getClassLoadingLock(name)) {
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    try {
                        loaded = findClass(name);
                    } catch (ClassNotFoundException ignored) {
                        loaded = super.loadClass(name, false);
                    }
                }
                if (resolve) {
                    resolveClass(loaded);
                }
                return loaded;
            }
        }
    }

    @ReflectionProxy(clazz = Target.class)
    interface TargetProxy {

        @FieldGetter(name = "value")
        int value(Target target);

        @FieldSetter(name = "value")
        void value(Target target, int value);

        @FieldGetter(name = "label", isStatic = true)
        String label();

        @FieldSetter(name = "label", isStatic = true)
        void label(String label);

        @MethodInvoker(name = "add")
        int add(Target target, int value);

        @MethodInvoker(name = "join", isStatic = true)
        String join(String suffix);

        @MethodInvoker(name = "rename")
        void rename(Target target, String name);

        @ConstructorInvoker
        Target create(String name);
    }

    static final class Target {
        private static String label = "unset";

        private int value = 1;
        private String name;

        Target(String name) {
            this.name = name;
        }

        int add(int value) {
            return this.value + value;
        }

        static String join(String suffix) {
            return label + suffix;
        }

        String rename(String name) {
            this.name = name;
            return name;
        }
    }
}
