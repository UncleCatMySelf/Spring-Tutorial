package com.github.unclecatmyself;

import com.github.unclecatmyself.service.StorageService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Created by MySelf on 2018/11/22.
 */
@AutoConfigureMockMvc
public class FileUploadTests extends UploadFileApplicationTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StorageService storageService;


}
