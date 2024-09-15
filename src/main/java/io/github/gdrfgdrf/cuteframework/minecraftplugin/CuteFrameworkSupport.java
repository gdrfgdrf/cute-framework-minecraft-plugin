package io.github.gdrfgdrf.cuteframework.minecraftplugin;

import io.github.gdrfgdrf.cuteframework.CuteFramework;
import io.github.gdrfgdrf.cuteframework.bean.BeanManager;
import io.github.gdrfgdrf.cuteframework.bean.annotation.Component;
import io.github.gdrfgdrf.cuteframework.bean.compare.OrderComparator;
import io.github.gdrfgdrf.cuteframework.utils.ClassUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author gdrfgdrf
 */
public class CuteFrameworkSupport extends JavaPlugin {
    private static JavaPlugin mainJavaPlugin;

    @Override
    public void onLoad() {
        super.onLoad();
        load_();
        try {
            CuteFramework.getInstance().run();
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while running CuteFramework", e);
        }

        getLogger().info("CuteFramework is running");

        mainJavaPlugin = this;
    }

    public void load_() {
        try {
            CuteFramework.initialize();
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while initializing CuteFramework", e);
        }
    }

    @Override
    public void onEnable() {
        getLogger().info("CuteFramework is enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("CuteFramework is disabled");
    }

    public static void load(JavaPlugin javaPlugin) {
        if (mainJavaPlugin == null) {
            return;
        }
        mainJavaPlugin.getLogger().info("Starting loading the plugin " + javaPlugin.getName());

        ClassLoader classLoader = javaPlugin.getClass().getClassLoader();
        String mainClassPackage = javaPlugin.getClass().getPackageName();
        String mainClassLastPackage = mainClassPackage.substring(0, mainClassPackage.lastIndexOf("."));

        Set<Class<?>> components = new LinkedHashSet<>();
        ClassUtils.searchJar(
                classLoader,
                mainClassLastPackage,
                clazz -> !clazz.isAnnotation() && ClassUtils.hasAnnotation(clazz, Component.class),
                components
        );
        components = components.stream()
                .sorted(OrderComparator.getInstance())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        components.forEach(component -> {
            try {
                BeanManager.getInstance().create(component);
            } catch (Exception e) {
                throw new RuntimeException(
                        "An error occurred while loading the beans for plugin " + javaPlugin.getName(),
                        e
                );
            }
        });
    }
}
