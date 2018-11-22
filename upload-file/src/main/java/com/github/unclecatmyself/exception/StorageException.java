package com.github.unclecatmyself.exception;

import com.github.unclecatmyself.service.StorageService;

/**
 * Created by MySelf on 2018/11/22.
 */
public class StorageException extends RuntimeException {

    public StorageException(String message){
        super(message);
    }

    public StorageException(String message,Throwable cause){
        super(message, cause);
    }

}
