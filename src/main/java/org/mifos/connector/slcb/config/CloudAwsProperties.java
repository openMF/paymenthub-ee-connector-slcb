package org.mifos.connector.slcb.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.validation.annotation.Validated;

/**
 * The S3 bucket and credentials: {@code cloud.aws.*}.
 *
 * <p>
 * {@code cloud.aws.enabled} is not here: only {@code @ConditionalOnProperty} reads it.
 * </p>
 *
 * @param bucketName
 *            bucket the payment files are downloaded from
 * @param credentials
 *            access key pair
 * @param region
 *            bucket region
 */
@Validated
@ConfigurationProperties(prefix = "cloud.aws")
public record CloudAwsProperties(@NotNull String bucketName, @NotNull @Valid Credentials credentials,
        @NotNull @Valid Region region) {

    /**
     * {@code cloud.aws.credentials.*}.
     *
     * @param accessKey
     *            access key id
     * @param secretKey
     *            secret access key
     */
    public record Credentials(@NotNull String accessKey, @NotNull String secretKey) {
    }

    /**
     * {@code cloud.aws.region.*}.
     *
     * @param staticRegion
     *            {@code cloud.aws.region.static}, renamed because {@code static} is a Java keyword
     */
    public record Region(@Name("static") @NotNull String staticRegion) {
    }
}
