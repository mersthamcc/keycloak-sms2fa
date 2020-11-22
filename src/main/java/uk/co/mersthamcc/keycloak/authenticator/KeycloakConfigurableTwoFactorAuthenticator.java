package uk.co.mersthamcc.keycloak.authenticator;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.*;
import uk.co.mersthamcc.keycloak.actions.MccOtpConfigureSmsAction;
import uk.co.mersthamcc.keycloak.helpers.MccOtpSmsHelper;
import uk.co.mersthamcc.keycloak.smsprovider.SmsProvider;
import uk.co.mersthamcc.keycloak.smsprovider.SmsProviderFactory;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.keycloak.models.utils.KeycloakModelUtils.getRoleFromString;
import static uk.co.mersthamcc.keycloak.authenticator.KeycloakConfigurableTwoFactorAuthenticatorFactory.CONFIG_PROPERTY_FORCE_OTP_ROLE;

public class KeycloakConfigurableTwoFactorAuthenticator implements Authenticator {

    public static final String TOKEN_ATTR = "OTP_VERIFY_TOKEN";
    public static final String MOBILE_PHONE_ATTR = "OTP_MOBILE_PHONE_NUMBER";
    public static final String TEMPLATE_OTP_CONFIG = "configure-sms.ftl";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        if (otpRequired(context)) {
            if (user.getAttributes().containsKey(MOBILE_PHONE_ATTR)) {
                SmsProvider provider = getSmsProvider(context);

                String token = provider.send(user.getFirstAttribute(MOBILE_PHONE_ATTR));
                user.setAttribute(TOKEN_ATTR, List.of(token));

                context.challenge(context.form().createLoginTotp());
            } else {
                setRequiredActions(context.getSession(), context.getRealm(), user);
                context.success();
            }
        } else {
            context.success();
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        SmsProvider provider = getSmsProvider(context);
        UserModel user = context.getUser();
        MultivaluedMap<String, String> form = context.getHttpRequest().getDecodedFormParameters();
        String otp = form.getFirst("otp");
        String token = user.getFirstAttribute(TOKEN_ATTR);

        user.removeAttribute(TOKEN_ATTR);
        if (provider.validate(token, otp)) {
            context.success();
        } else {
            context.failure(AuthenticationFlowError.EXPIRED_CODE);
        }
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        user.addRequiredAction(MccOtpConfigureSmsAction.PROVIDER_ID);
    }

    @Override
    public void close() {

    }

    public SmsProvider getSmsProvider(AuthenticationFlowContext context) {
        return SmsProviderFactory.create();
    }

    private boolean otpRequired(AuthenticationFlowContext context) {
        AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();
        UserModel user = context.getUser();

        return userHasRole(context.getRealm(), user, authenticatorConfig.getConfig().get(CONFIG_PROPERTY_FORCE_OTP_ROLE));
    }

    private boolean userHasRole(RealmModel realm, UserModel user, String roleName) {
        if (roleName == null) {
            return false;
        }

        RoleModel role = getRoleFromString(realm, roleName);
        if (role != null) {
            return user.hasRole(role);
        }
        return false;
    }
}
