package de.shop.auth.controller;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import de.shop.kundenverwaltung.controller.KundeController;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.util.Log;
import de.shop.util.Transactional;

@Named("auth")
@SessionScoped
@Log
public class AuthController implements Serializable {

	// //////////////////////////////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final long serialVersionUID = -958228038511437676L;

	private String username;

	private String password;

	@Produces
	@SessionScoped
	@KundeLoggedIn
	private Kunde user;

	@Inject
	private KundeService ks;

	@Inject
	private KundeController kc;
	@Inject
	private transient HttpServletRequest request;

	@Inject
	private transient FacesContext facesCtx;

	@Inject
	private transient HttpSession session;

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS

	public String login() {

		try {
			request.login(username, password);
		}
		catch (ServletException e) {
			reset();
			return null;
		}

		user = ks.findKundeByMail(KundeService.FetchType.JUST_KUNDE, username,
				null);
		kc.setKundeId(user.getKundeID());
		kc.findKundeById();
		String path = facesCtx.getViewRoot().getViewId();
		return path;
	}

	/**
	 * Nachtraegliche Einloggen eines registrierten Kunden mit Benutzername und
	 * Password.
	 */
	@Transactional
	public void preserveLogin() {
		if (username != null && user != null) {
			return;
		}

		// Benutzername beim Login ermitteln
		username = request.getRemoteUser();

		user = ks.findKundeByUsername(username);
		if (user == null) {
			// Darf nicht passieren, wenn unmittelbar zuvor das Login
			// erfolgreich war
			logout();
			throw new InternalError("Kein Kunde mit dem Loginnamen \""
					+ username + "\" gefunden");
		}
	}

	public boolean isLoggedIn() {
		return user != null;
	}

	private void reset() {
		user = null;
		username = null;
		password = null;
	}

	public String getUsername() {
		return username;
	}

	public String logout() {
		try {
			request.logout();

		}
		catch (ServletException e) {
			user = null;
			username = null;
			password = null;
			session.invalidate();
			reset();
			return null;
		}

		user = null;
		username = null;
		password = null;
		session.invalidate();
		return "/index?faces-redirect=true";
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// GETTER & SETTER

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Kunde getUser() {
		return user;
	}
}
