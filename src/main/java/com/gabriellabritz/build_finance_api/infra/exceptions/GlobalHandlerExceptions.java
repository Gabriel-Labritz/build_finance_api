package com.gabriellabritz.build_finance_api.infra.exceptions;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.EmailAlreadyUsedException;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.InvalidVerificationTokenException;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.UserAlreadyVerifiedException;
import com.gabriellabritz.build_finance_api.infra.exceptions.categories.CategoryAlreadyExistsException;
import com.gabriellabritz.build_finance_api.infra.exceptions.categories.CategoryNotFoundException;
import com.gabriellabritz.build_finance_api.infra.exceptions.categories.DefaultCategoryException;
import com.gabriellabritz.build_finance_api.infra.exceptions.jwt.InvalidPreAuthTokenException;
import com.gabriellabritz.build_finance_api.infra.exceptions.jwt.InvalidRefreshTokenException;
import com.gabriellabritz.build_finance_api.infra.exceptions.jwt.JwtGenerationException;
import com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions.InvalidRecurringTransactionDateException;
import com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions.RecurringTransactionAlreadyActiveException;
import com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions.RecurringTransactionAlreadyInactiveException;
import com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions.RecurringTransactionNotFoundException;
import com.gabriellabritz.build_finance_api.infra.exceptions.transactions.TransactionNotFoundException;
import com.gabriellabritz.build_finance_api.infra.exceptions.transactions.TransactionTypeMismatchException;
import com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth.InvalidA2FCodeException;
import com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth.TwoFactorAuthAlreadyEnabledException;
import com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth.TwoFactorAuthNotEnabledException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalHandlerExceptions {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Erro de validação dos campos");
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        problemDetail.setTitle("Dados inválidos");
        problemDetail.setProperty("errors_field", errors);

        return problemDetail;
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ProblemDetail handleEmailAlreadyUsedException(EmailAlreadyUsedException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Email já cadastrado.");

        return problemDetail;
    }

    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ProblemDetail handleInvalidVerificationTokenException(InvalidVerificationTokenException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Token de verificação.");

        return problemDetail;
    }

    @ExceptionHandler(UserAlreadyVerifiedException.class)
    public ProblemDetail handleUserAlreadyVerifiedException(UserAlreadyVerifiedException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Usuário verificado.");

        return problemDetail;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(BadCredentialsException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        problemDetail.setTitle("Não autorizado");

        return problemDetail;
    }

    @ExceptionHandler(DisabledException.class)
    public ProblemDetail handleDisabledException(DisabledException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Conta inativa, verifique seu email para ativar sua conta.");
        problemDetail.setTitle("Conta inativa");

        return problemDetail;
    }

    @ExceptionHandler(JwtGenerationException.class)
    public ProblemDetail handleJwtGenerationException(JwtGenerationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno ao processar a autenticação. Tente novamente.");
        problemDetail.setTitle("Erro interno");

        return problemDetail;
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleInvalidRefreshTokenException(InvalidRefreshTokenException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        problemDetail.setTitle("Refresh token inválido.");

        return problemDetail;
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ProblemDetail handleJWTVerificationException(JWTVerificationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        problemDetail.setTitle("Token JWT inválido.");

        return problemDetail;
    }

    @ExceptionHandler(TwoFactorAuthAlreadyEnabledException.class)
    public ProblemDetail handleTwoFactorAuthAlreadyEnabled(TwoFactorAuthAlreadyEnabledException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Configuração de autenticação de dois fatores.");

        return problemDetail;
    }

    @ExceptionHandler(InvalidA2FCodeException.class)
    public ProblemDetail handleInvalidA2FCodeException(InvalidA2FCodeException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Código inválido.");

        return problemDetail;
    }

    @ExceptionHandler(TwoFactorAuthNotEnabledException.class)
    public ProblemDetail handleTwoFactorAuthNotEnabled(TwoFactorAuthNotEnabledException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Autenticação de dois fatores desativada.");

        return problemDetail;
    }

    @ExceptionHandler(InvalidPreAuthTokenException.class)
    public ProblemDetail handleInvalidPreAuthTokenException(InvalidPreAuthTokenException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        problemDetail.setTitle("Tipo do token inválido.");

        return problemDetail;
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ProblemDetail handleCategoryAlreadyExistsException(CategoryAlreadyExistsException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Categoria já existe.");

        return problemDetail;
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ProblemDetail handleCategoryNotFoundException(CategoryNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Categoria não encontrada.");

        return problemDetail;
    }

    @ExceptionHandler(DefaultCategoryException.class)
    public ProblemDetail handleDefaultCategoryException(DefaultCategoryException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Categoria padrão.");

        return problemDetail;
    }

    @ExceptionHandler(TransactionTypeMismatchException.class)
    public ProblemDetail handleTransactionTypeMismatchException(TransactionTypeMismatchException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
        problemDetail.setTitle("Tipo de transação.");

        return problemDetail;
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ProblemDetail handleTransactionNotFoundException(TransactionNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Transação não encontrada.");

        return problemDetail;
    }

    @ExceptionHandler(InvalidRecurringTransactionDateException.class)
    public ProblemDetail handleInvalidRecurringTransactionDateException(InvalidRecurringTransactionDateException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Data da transação recorrente inválida.");

        return problemDetail;
    }

    @ExceptionHandler(RecurringTransactionNotFoundException.class)
    public ProblemDetail handleRecurringTransactionNotFoundException(RecurringTransactionNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Transação não encontrada.");

        return problemDetail;
    }

    @ExceptionHandler(RecurringTransactionAlreadyInactiveException.class)
    public ProblemDetail handleRecurringTransactionAlreadyInactiveException(RecurringTransactionAlreadyInactiveException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Transação já desativada.");

        return problemDetail;
    }

    @ExceptionHandler(RecurringTransactionAlreadyActiveException.class)
    public ProblemDetail handleRecurringTransactionAlreadyActiveException(RecurringTransactionAlreadyActiveException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Transação já ativa.");

        return problemDetail;
    }
}
