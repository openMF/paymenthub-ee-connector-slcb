package org.mifos.connector.slcb.config;

import org.springframework.stereotype.Component;

/**
 * The SLCB settings the routes read, in one place, built from {@link SlcbProperties} and {@link ConfigProperties}.
 *
 * <p>
 * The fields keep the names and types the routes already use, so no route changes.
 * </p>
 */
@Component
public class SLCBConfig {

    public final String authHost;
    public final String username;
    public final String password;
    public final String authEndpoint;
    public final String apiHost;
    public final String transferRequestEndpoint;
    public final String reconciliationEndpoint;
    public final String accountBalanceEndpoint;
    public final String signatureKey;
    public final String sourceAccount;
    public final int accountType;
    public final String institutionCode;
    public final boolean isReconciliationEnabled;
    public final String dateFormat;

    public final String authUrl;
    public final String transactionRequestUrl;
    public final String reconciliationUrl;
    public final String accountBalanceUrl;

    public SLCBConfig(SlcbProperties slcb, ConfigProperties config) {
        authHost = slcb.auth().host();
        username = slcb.auth().username();
        password = slcb.auth().password();
        authEndpoint = slcb.auth().authEndpoint();
        apiHost = slcb.api().host();
        transferRequestEndpoint = slcb.api().transactionRequestEndpoint();
        reconciliationEndpoint = slcb.api().reconciliationEndpoint();
        accountBalanceEndpoint = slcb.api().accountBalanceEndpoint();
        signatureKey = slcb.signature().key();
        sourceAccount = slcb.account().number();
        accountType = slcb.account().type();
        institutionCode = slcb.institutionCode();
        isReconciliationEnabled = config.reconciliation().enable();
        dateFormat = config.dateFormat();

        authUrl = authHost + authEndpoint;
        transactionRequestUrl = apiHost + transferRequestEndpoint;
        reconciliationUrl = apiHost + reconciliationEndpoint;
        accountBalanceUrl = apiHost + accountBalanceEndpoint;
    }
}
