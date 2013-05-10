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

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.util.Log;

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

		// TODO: Es sollte keine Liste sein, sondern nur ein einziger Kunde
		// gefunden werden
		user = ks.findKundeByMail(
				KundeService.FetchType.JUST_KUNDE, username, null);
		
		System.out.println("LOGIN:" + user);

		String path = facesCtx.getViewRoot().getViewId();
		return path;
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
		try
		{
			request.logout();
		} catch(ServletException e)
		{
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
