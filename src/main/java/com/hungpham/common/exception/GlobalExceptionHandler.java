package com.hungpham.common.exception;

import com.hungpham.common.enums.ErrorCodeEnum;
import com.hungpham.dtos.ErrorMessageResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorMessageResponseDto> handleBadRequestException(BadRequestException ex) {
        logger.debug("BadRequestException", ex);
        ErrorMessageResponseDto errorResponseDto = new ErrorMessageResponseDto(ErrorCodeEnum.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorMessageResponseDto> handleEntityNotFoundException(EntityNotFoundException ex) {
        logger.error("EntityNotFoundException", ex);
        ErrorMessageResponseDto errorResponseDto = new ErrorMessageResponseDto(ErrorCodeEnum.ENTITY_NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessageResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.error("ResourceNotFoundException", ex);
        ErrorMessageResponseDto errorResponseDto = new ErrorMessageResponseDto(ErrorCodeEnum.UNPROCESSABLE_ENTITY, ex.getMessage());
        return new ResponseEntity<>(errorResponseDto, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
