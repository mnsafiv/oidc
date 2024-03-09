package ru.safonoviv.oidclmsboi.boa.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SqlRollback extends RuntimeException{
    private final HttpStatus status;

    public SqlRollback(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }


}
