package de.shop.util.exceptions;

import de.shop.util.AbstractShopException;

//@form:off

public class AbstractBestellungServiceException extends 
AbstractShopException {
//@form:on

	private static final long serialVersionUID = 8382146394942021969L;

	public AbstractBestellungServiceException(String msg) {
		super(msg);
	}

	public AbstractBestellungServiceException(String msg, Throwable t) {
		super(msg, t);
	}
}
