package com.meliskara.crypto_trading_simulator.common.error;

import com.meliskara.crypto_trading_simulator.auth.EmailAlreadyTakenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EmailAlreadyTakenException.class)
	ProblemDetail handleEmailTaken(EmailAlreadyTakenException ex){
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Email already registered");
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleValidation(MethodArgumentNotValidException ex){
		Map<String,String> errors = new LinkedHashMap<>();
		for (FieldError fieldError :
		ex.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}

		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST, "One or more fields are invalid");
		problem.setTitle("Validation failed");
		problem.setProperty("errors", errors);
		return problem;
	}
}
