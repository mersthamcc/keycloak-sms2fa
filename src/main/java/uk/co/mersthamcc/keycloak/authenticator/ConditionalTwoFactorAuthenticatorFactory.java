package uk.co.mersthamcc.keycloak.authenticator;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

import static org.keycloak.provider.ProviderConfigProperty.ROLE_TYPE;
import static uk.co.mersthamcc.keycloak.ConditionalOtpConstants.CONFIG_PROPERTY_FORCE_OTP_ROLE;

public class ConditionalTwoFactorAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "mcc-two-factor-authentication";
    private static final ConditionalTwoFactorAuthenticator SINGLETON_INSTANCE =
            new ConditionalTwoFactorAuthenticator();

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
        AuthenticationExecutionModel.Requirement.REQUIRED,
        AuthenticationExecutionModel.Requirement.ALTERNATIVE,
        AuthenticationExecutionModel.Requirement.DISABLED
    };

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        ProviderConfigProperty property;

        property = new ProviderConfigProperty();
        property.setType(ROLE_TYPE);
        property.setName(CONFIG_PROPERTY_FORCE_OTP_ROLE);
        property.setLabel("Force OTP for Role");
        property.setHelpText("OTP is always required if user has the given Role.");
        configProperties.add(property);
    }

    @Override
    public String getDisplayType() {
        return "MCC 2FA Authentication with SMS";
    }

    @Override
    public String getReferenceCategory() {
        return "mcc-2fa";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public String getHelpText() {
        return "Validates an OTP sent by SMS or an authenticator app";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON_INSTANCE;
    }

    @Override
    public void init(Config.Scope config) {
        // Not used
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Not used
    }

    @Override
    public void close() {
        // Not used
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
