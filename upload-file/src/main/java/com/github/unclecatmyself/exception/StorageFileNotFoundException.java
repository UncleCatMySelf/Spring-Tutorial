package com.github.unclecatmyself.exception;

/**
 * Created by MySelf on 2018/11/22.
 */
public class StorageFileNotFoundException extends StorageException {

    public StorageFileNotFoundException(String message){
        super(message);
    }

    public StorageFileNotFoundException(String message,Throwable cause){
        super(message, cause);
    }

}
