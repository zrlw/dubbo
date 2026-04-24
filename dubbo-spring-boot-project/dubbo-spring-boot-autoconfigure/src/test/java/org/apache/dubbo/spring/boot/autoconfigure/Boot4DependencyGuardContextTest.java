import org.apache.dubbo.spring.boot.autoconfigure.DubboSpringBoot4DependencyCheckAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class Boot4DependencyGuardContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(DubboSpringBoot4DependencyCheckAutoConfiguration.class);

    @Test
    void testTripleServletEnabled() {
        contextRunner
            .properties("dubbo.protocol.triple.servlet.enabled=true")
            .run(context -> assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> context.getBean(DubboSpringBoot4DependencyCheckAutoConfiguration.class))
                .withMessageContaining("Missing dubbo-spring-boot-4-autoconfigure dependency"));
    }

    @Test
    void testTripleWebSocketEnabled() {
        contextRunner
            .properties("dubbo.protocol.triple.websocket.enabled=true")
            .run(context -> assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> context.getBean(DubboSpringBoot4DependencyCheckAutoConfiguration.class))
                .withMessageContaining("Missing dubbo-spring-boot-4-autoconfigure dependency"));
    }
}