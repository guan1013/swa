package de.shop.util.exceptions;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
@ApplicationScoped
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
	@Override
	public Response toResponse(ConstraintViolationException e) {
		final String msg = e.getMessage();
		final Response response = Response.status(NOT_FOUND)
		                                  .type(TEXT_PLAIN)
		                                  .entity(msg)
		                                  .build();
		return response;
	}

}
