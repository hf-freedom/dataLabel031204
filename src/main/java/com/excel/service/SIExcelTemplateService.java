package com.excel.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.io.File;
import java.nio.file.Paths;

import static com.excel.config.ExcelTemplateConfig.*;

@Service
public class SIExcelTemplateService {

    public File getUserTemplate() throws Exception {
        Resource resource = new ClassPathResource(TEMPLATE_DIR + File.separator + USER_TEMPLATE);
        if (resource.exists()) {
            return resource.getFile();
        }
        return Paths.get("src/main/resources", TEMPLATE_DIR, USER_TEMPLATE).toFile();
    }

    public File getOrgTemplate() throws Exception {
        Resource resource = new ClassPathResource(TEMPLATE_DIR + File.separator + ORG_TEMPLATE);
        if (resource.exists()) {
            return resource.getFile();
        }
        return Paths.get("src/main/resources", TEMPLATE_DIR, ORG_TEMPLATE).toFile();
    }
}
