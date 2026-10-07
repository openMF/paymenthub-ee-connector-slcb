package org.mifos.connector.slcb.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Every property these records ask for has to be there, or the connector must refuse to start and say which one is
 * missing. That is what the plain {@code @Value} declarations did before they were replaced, so these tests hold the
 * replacement to the same promise, and they read the real application.yml rather than a copy of it.
 *
 * <p>
 * The missing-section check runs with no configuration file at all. Loading only this module's application.yml is not
 * enough to remove a section, because paymenthub-ee-core puts its own application.yaml on the classpath and that one
 * also sets {@code zeebe.*}.
 * </p>
 */
class ShippedConfigBindsTest {

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({ ConnectorCamelProperties.class, ZeebeProperties.class, SlcbProperties.class,
            ConfigProperties.class, CloudAwsProperties.class })
    static class AllRecords {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ConnectorCamelProperties.class)
    static class OnlyCamel {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ZeebeProperties.class)
    static class OnlyZeebe {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(SlcbProperties.class)
    static class OnlySlcb {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ConfigProperties.class)
    static class OnlyConfig {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(CloudAwsProperties.class)
    static class OnlyCloudAws {}

    /** One configuration class per record, keyed by the prefix that record binds. */
    private static final Map<String, Class<?>> ONE_RECORD_EACH = Map.of("camel", OnlyCamel.class, "zeebe", OnlyZeebe.class, "slcb",
            OnlySlcb.class, "config", OnlyConfig.class, "cloud.aws", OnlyCloudAws.class);

    private ApplicationContextRunner runner() {
        return new ApplicationContextRunner().withConfiguration(
                AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class, ValidationAutoConfiguration.class));
    }

    private ApplicationContextRunner withShippedYaml() {
        return runner().withInitializer(new ConfigDataApplicationContextInitializer());
    }

    @Test
    void theShippedApplicationYmlFillsEveryField() {
        withShippedYaml().withUserConfiguration(AllRecords.class).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(ConnectorCamelProperties.class).serverPort()).isNotNull();
            ZeebeProperties zeebe = context.getBean(ZeebeProperties.class);
            assertThat(zeebe.broker().contactpoint()).isEqualTo("127.0.0.1:26500");
            assertThat(zeebe.client().maxExecutionThreads()).isEqualTo(100);
            SlcbProperties slcb = context.getBean(SlcbProperties.class);
            assertThat(slcb.auth().authEndpoint()).isEqualTo("/api/auth");
            assertThat(slcb.auth().host()).isNotEmpty();
            assertThat(slcb.auth().username()).isNotEmpty();
            assertThat(slcb.auth().password()).isNotEmpty();
            assertThat(slcb.api().transactionRequestEndpoint()).isEqualTo("/api/transactionRequest");
            assertThat(slcb.api().reconciliationEndpoint()).isEqualTo("/reconciliation");
            assertThat(slcb.api().accountBalanceEndpoint()).isEqualTo("/accountBalance");
            assertThat(slcb.signature().key()).isNotEmpty();
            assertThat(slcb.account().number()).isNotEmpty();
            assertThat(slcb.account().type()).isZero();
            // application.yml spells this one in camel case: slcb.institutionCode
            assertThat(slcb.institutionCode()).isEqualTo("SLCB");
            ConfigProperties config = context.getBean(ConfigProperties.class);
            assertThat(config.dateFormat()).isEqualTo("yyyy-MM-dd'T'hh:mm:ssXXX");
            assertThat(config.reconciliation().enable()).isFalse();
            CloudAwsProperties aws = context.getBean(CloudAwsProperties.class);
            assertThat(aws.bucketName()).isNotEmpty();
            assertThat(aws.credentials().accessKey()).isNotEmpty();
            assertThat(aws.credentials().secretKey()).isNotEmpty();
            // cloud.aws.region.static: "static" is a Java keyword, so the component is named staticRegion
            assertThat(aws.region().staticRegion()).isEqualTo("us-east-2");
        });
    }

    @Test
    void everyRecordRefusesToStartWhenItsSectionIsMissing() {
        ONE_RECORD_EACH.forEach((prefix, configuration) -> runner().withUserConfiguration(configuration).run(context -> {
            assertThat(context).as("context with nothing configured under '%s'", prefix).hasFailed();
            assertThat(context.getStartupFailure()).as("failure for '%s'", prefix).hasStackTraceContaining("BindValidationException")
                    .hasStackTraceContaining("Binding validation errors on " + prefix);
        }));
    }

    @Test
    void aMissingKeyInsideAGroupStopsStartup() {
        // only slcb.auth.username is missing, so this checks that @Valid reaches the nested record
        runner().withUserConfiguration(OnlySlcb.class)
                .withPropertyValues("slcb.auth.host=https://auth", "slcb.auth.auth-endpoint=/api/auth", "slcb.auth.password=p",
                        "slcb.api.host=https://api", "slcb.api.transaction-request-endpoint=/t", "slcb.api.reconciliation-endpoint=/r",
                        "slcb.api.account-balance-endpoint=/b", "slcb.signature.key=k", "slcb.account.number=1", "slcb.account.type=0",
                        "slcb.institutionCode=SLCB")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasStackTraceContaining("Binding validation errors on slcb")
                            .hasStackTraceContaining("auth.username");
                });
    }

    @Test
    void aValueSetToNothingOnANumberFieldStopsStartup() {
        withShippedYaml().withUserConfiguration(OnlySlcb.class).withPropertyValues("slcb.account.type=").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining("Binding validation errors on slcb");
        });
    }

    @Test
    void aValueSetToNothingOnABooleanFieldStopsStartup() {
        withShippedYaml().withUserConfiguration(OnlyConfig.class).withPropertyValues("config.reconciliation.enable=").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining("Binding validation errors on config");
        });
    }

    @Test
    void aValueSetToNothingOnAStringFieldIsAcceptedJustAsItWasBefore() {
        withShippedYaml().withUserConfiguration(OnlySlcb.class).withPropertyValues("slcb.institutionCode=").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(SlcbProperties.class).institutionCode()).isEmpty();
        });
    }
}
