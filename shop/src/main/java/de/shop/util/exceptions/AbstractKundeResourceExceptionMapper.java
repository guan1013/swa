package de.shop.util.exceptions;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class AbstractKundeResourceExceptionMapper implements
		ExceptionMapper<AbstractKundeServiceException> {
	@Override
	public Response toResponse(AbstractKundeServiceException e) {
		final String msg = e.getMessage();
		final Response response = Response.status(CONFLICT).type(TEXT_PLAIN)
				.entity(msg).build();
		return response;
	}

}
