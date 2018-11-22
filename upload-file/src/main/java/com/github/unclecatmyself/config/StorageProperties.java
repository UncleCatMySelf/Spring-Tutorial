package com.github.unclecatmyself.config;


import org.springframework.stereotype.Component;

/**
 * Created by MySelf on 2018/11/22.
 */
@Component
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    private String location = "G:\\uploaddir";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


}
