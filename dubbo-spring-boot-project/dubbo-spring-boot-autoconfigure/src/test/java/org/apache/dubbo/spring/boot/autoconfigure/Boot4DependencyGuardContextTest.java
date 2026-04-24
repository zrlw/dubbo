package org.apache.dubbo.spring.boot.autoconfigure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

class Boot4DependencyGuardContextTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DubboSpringBoot4DependencyCheckAutoConfiguration.class));

    private boolean oldBoot4Flag;

    @BeforeEach
    void forceBoot4ConditionOn() throws Exception {
        Field f = SpringBoot4Condition.class.getDeclaredField("IS_SPRING_BOOT_4");
        f.setAccessible(true);
        oldBoot4Flag = (boolean) f.get(null);

        Field modField = Field.class.getDeclaredField("modifiers");
        modField.setAccessible(true);
        modField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

        f.set(null, true);
    }

    @AfterEach
    void restoreBoot4Condition() throws Exception {
        Field f = SpringBoot4Condition.class.getDeclaredField("IS_SPRING_BOOT_4");
        f.setAccessible(true);

        Field modField = Field.class.getDeclaredField("modifiers");
        modField.setAccessible(true);
        modField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

        f.set(null, oldBoot4Flag);
    }

    @Test
    void servletEnabled_withoutBoot4Autoconfigure_failsFast() {
        runner.withPropertyValues("dubbo.protocol.triple.servlet.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("Missing dubbo-spring-boot-4-autoconfigure dependency");
                });
    }

    @Test
    void websocketEnabled_withoutBoot4Autoconfigure_failsFast() {
        runner.withPropertyValues("dubbo.protocol.triple.websocket.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("Missing dubbo-spring-boot-4-autoconfigure dependency");
                });
    }
}
