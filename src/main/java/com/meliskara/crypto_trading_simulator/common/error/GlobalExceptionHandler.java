package com.meliskara.crypto_trading_simulator.common.error;

import com.meliskara.crypto_trading_simulator.auth.EmailAlreadyTakenException;
import com.meliskara.crypto_trading_simulator.auth.InvalidCredentialsException;
import com.meliskara.crypto_trading_simulator.auth.InvalidRefreshTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(EmailAlreadyTakenException.class)
	ProblemDetail handleEmailTaken(EmailAlreadyTakenException ex){
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Email already registered");
		return problem;
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex){
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED
		, ex.getMessage());
		problem.setTitle("Authentication failed");
		return problem;
	}

	@ExceptionHandler(InvalidRefreshTokenException.class)
	ProblemDetail handleInvalidRefreshToken(InvalidRefreshTokenException ex){
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.UNAUTHORIZED,
				ex.getMessage());
		problem.setTitle("Authentication failed");
		return problem;
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpected(Exception ex){
		log.error("Unexpected error", ex);
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"An unexpected error occurred");
		problem.setTitle("Unexpected server error");
		return problem;
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request) {

		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}

		ProblemDetail problem = ex.getBody();
		problem.setTitle("Validation failed");
		problem.setDetail("One or more fields are invalid");
		problem.setProperty("errors", errors);

		return handleExceptionInternal(ex, problem, headers, status, request);
	}
}
