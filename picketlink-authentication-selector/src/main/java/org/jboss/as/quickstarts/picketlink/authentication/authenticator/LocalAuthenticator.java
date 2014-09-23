
package org.jboss.as.quickstarts.picketlink.authentication.authenticator;

import static org.picketlink.log.BaseLog.AUTHENTICATION_LOGGER;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.picketlink.authentication.LockedAccountException;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.social.auth.AbstractSocialAuthenticator;

@Named
@RequestScoped
public class LocalAuthenticator extends AbstractSocialAuthenticator {

	@Inject
	IdentityManager identityManager;

	@Inject
	DefaultLoginCredentials credentials;

	@Override
	public void authenticate() {
		credentials = (DefaultLoginCredentials) httpServletRequest.getAttribute("Vamos");
		if (credentials.getCredential() == null) {
			return;
		}
		Credentials creds;

		creds = new UsernamePasswordCredentials(credentials.getUserId(), (Password) credentials.getCredential());

		if (AUTHENTICATION_LOGGER.isDebugEnabled()) {
			AUTHENTICATION_LOGGER.debugf("Validating credentials [%s] using PicketLink IDM.", creds);
		}

		identityManager.validateCredentials(creds);

		this.credentials.setStatus(creds.getStatus());
		this.credentials.setValidatedAccount(creds.getValidatedAccount());

		if (AUTHENTICATION_LOGGER.isDebugEnabled()) {
			AUTHENTICATION_LOGGER.debugf("Credential status is [%s] and validated account [%s]",
					this.credentials.getStatus(), this.credentials.getValidatedAccount());
		}

		if (Credentials.Status.VALID.equals(creds.getStatus())) {
			setStatus(AuthenticationStatus.SUCCESS);
			setAccount(creds.getValidatedAccount());
		} else if (Credentials.Status.ACCOUNT_DISABLED.equals(creds.getStatus())) {
			throw new LockedAccountException("Account [" + this.credentials.getUserId() + "] is disabled.");
		}

		System.out.println(credentials);
	}
}
