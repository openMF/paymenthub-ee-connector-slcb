package org.mifos.connector.slcb.file.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.mifos.connector.slcb.config.CloudAwsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsStorageConfig {

    private final String accessKey;

    private final String accessSecret;

    private final String region;

    public AwsStorageConfig(CloudAwsProperties awsProperties) {
        this.accessKey = awsProperties.credentials().accessKey();
        this.accessSecret = awsProperties.credentials().secretKey();
        this.region = awsProperties.region().staticRegion();
    }

    @Bean
    @ConditionalOnProperty(
            value="cloud.aws.enabled",
            havingValue = "true")
    public AmazonS3 s3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, accessSecret);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();
    }

}
