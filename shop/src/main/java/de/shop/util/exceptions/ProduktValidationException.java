package de.shop.util.exceptions;

public class ProduktValidationException extends RuntimeException {
	
	private static final long serialVersionUID = -579963016845566102L;

	public ProduktValidationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ProduktValidationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
}