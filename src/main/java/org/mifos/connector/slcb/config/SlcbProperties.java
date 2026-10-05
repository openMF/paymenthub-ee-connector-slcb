package org.mifos.connector.slcb.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * How this connector reaches the SLCB API: {@code slcb.*}.
 *
 * @param auth
 *            the authentication endpoint and credentials
 * @param api
 *            the API host and its endpoints
 * @param signature
 *            the key requests are signed with
 * @param account
 *            the source account payments are made from
 * @param institutionCode
 *            institution code sent with every payment request
 */
@Validated
@ConfigurationProperties(prefix = "slcb")
public record SlcbProperties(@NotNull @Valid Auth auth, @NotNull @Valid Api api, @NotNull @Valid Signature signature,
        @NotNull @Valid Account account, @NotNull String institutionCode) {

    /**
     * {@code slcb.auth.*}.
     *
     * @param host
     *            scheme, host and port of the authentication server
     * @param authEndpoint
     *            path of the token endpoint
     * @param username
     *            user the connector authenticates as
     * @param password
     *            password of that user
     */
    public record Auth(@NotNull String host, @NotNull String authEndpoint, @NotNull String username,
            @NotNull String password) {
    }

    /**
     * {@code slcb.api.*}.
     *
     * @param host
     *            scheme, host and port of the API
     * @param transactionRequestEndpoint
     *            path payments are sent to
     * @param reconciliationEndpoint
     *            path of the reconciliation call
     * @param accountBalanceEndpoint
     *            path of the account balance call
     */
    public record Api(@NotNull String host, @NotNull String transactionRequestEndpoint,
            @NotNull String reconciliationEndpoint, @NotNull String accountBalanceEndpoint) {
    }

    /**
     * {@code slcb.signature.*}.
     *
     * @param key
     *            key used to sign the authorisation code of each request
     */
    public record Signature(@NotNull String key) {
    }

    /**
     * {@code slcb.account.*}.
     *
     * @param number
     *            source account number
     * @param type
     *            source account type, sent as a number
     */
    public record Account(@NotNull String number, @NotNull Integer type) {
    }
}
