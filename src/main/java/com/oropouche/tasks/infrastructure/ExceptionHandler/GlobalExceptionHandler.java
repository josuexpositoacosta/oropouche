
package com.oropouche.tasks.infrastructure.ExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.oropouche.tasks.domain.exceptions.InvalidCredentialsException;
import com.oropouche.tasks.domain.exceptions.RecursoNoEncontradoException;
import com.oropouche.tasks.domain.exceptions.RecursoDuplicadoException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handlerArgumentException(IllegalArgumentException ex)
    {
        return new ResponseEntity<String>(ex.getMessage(),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<String> handlerRecursoNoEncontrado(RecursoNoEncontradoException ex)
    {
        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handlerInvalidCredentials(InvalidCredentialsException ex)
    {
        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /** Cubre también {@code UserAlreadyExistsException}, que extiende de esta. */
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<String> handlerRecursoDuplicado(RecursoDuplicadoException ex)
    {
        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handlerAuthenticationException(AuthenticationException ex)
    {
        return new ResponseEntity<String>("Usuario o contraseña incorrectos", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handlerDataIntegrityViolation(DataIntegrityViolationException ex)
    {
        LOGGER.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<String>("El recurso ya existe o viola una restricción de datos",
                HttpStatus.CONFLICT);
    }

    /** JSON mal formado o con valores no válidos (por ejemplo, una clasificación inexistente). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handlerMensajeIlegible(HttpMessageNotReadableException ex)
    {
        return new ResponseEntity<String>(
                "El cuerpo de la petición no es válido: revise el formato y los valores permitidos",
                HttpStatus.BAD_REQUEST);
    }

    /** Parámetro de URL con un tipo o valor no válido (por ejemplo, ?clasificacion=XYZ). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handlerParametroInvalido(MethodArgumentTypeMismatchException ex)
    {
        return new ResponseEntity<String>("Valor no válido para el parámetro: " + ex.getName(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handlerRuntimeException(RuntimeException ex)
    {
        LOGGER.error("Error no controlado", ex);
        return new ResponseEntity<String>("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
