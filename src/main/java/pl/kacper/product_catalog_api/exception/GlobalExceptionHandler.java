package pl.kacper.product_catalog_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ProblemDetail handleValidationException(Throwable throwable, BindingResult bindingResult) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "JSON Body validation error");

        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        Map<String, String> mappedErrors = fieldErrors.stream()
                .collect(
                        Collectors.groupingBy(
                                FieldError::getField,
                                Collectors.mapping(
                                        FieldError::getDefaultMessage,
                                        Collectors.joining(" | ")
                                )
                        )
                );

        problemDetail.setProperty("problems", mappedErrors);

        return problemDetail;
    }

    @ExceptionHandler({NoSuchDbRecordException.class})
    public ProblemDetail handleNoSuchDbRecordException(Throwable throwable) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, throwable.getMessage());
        problemDetail.setTitle("Invalid product ID");

        return problemDetail;
    }
}
