package de.shop.util.exceptions;

import de.shop.util.AbstractShopException;

//@form:off
public abstract class AbstractBestellpostenServiceException extends
AbstractShopException {
//@form:on


	/**
	 * 
	 */
	private static final long serialVersionUID = -6414473484587120998L;

	public AbstractBestellpostenServiceException(String msg) {
		super(msg);
	}

	public AbstractBestellpostenServiceException(String msg, Throwable t) {
		super(msg, t);
	}
}
