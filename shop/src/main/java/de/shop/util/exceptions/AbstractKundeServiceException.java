package de.shop.util.exceptions;

import de.shop.util.AbstractShopException;

//@form:off
public abstract class AbstractKundeServiceException extends
AbstractShopException {
//@form:on

	private static final long serialVersionUID = 2391278710510634328L;

	public AbstractKundeServiceException(String msg) {
		super(msg);
	}

	public AbstractKundeServiceException(String msg, Throwable t) {
		super(msg, t);
	}
}
