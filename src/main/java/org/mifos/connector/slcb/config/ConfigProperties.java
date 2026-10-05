package org.mifos.connector.slcb.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The {@code config.*} settings: date format and reconciliation switch.
 *
 * @param dateFormat
 *            {@link java.text.SimpleDateFormat} pattern for the dates sent to SLCB
 * @param reconciliation
 *            reconciliation settings
 */
@Validated
@ConfigurationProperties(prefix = "config")
public record ConfigProperties(@NotNull String dateFormat, @NotNull @Valid Reconciliation reconciliation) {

    /**
     * {@code config.reconciliation.*}.
     *
     * @param enable
     *            whether a reconciliation call follows each transfer
     */
    public record Reconciliation(@NotNull Boolean enable) {
    }
}
