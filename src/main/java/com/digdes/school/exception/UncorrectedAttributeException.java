package com.digdes.school.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UncorrectedAttributeException extends RuntimeException{
    public UncorrectedAttributeException(String message){
        super(message);
    }
}
