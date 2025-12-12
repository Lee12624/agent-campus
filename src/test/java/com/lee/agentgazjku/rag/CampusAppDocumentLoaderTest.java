package com.lee.agentgazjku.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class CampusAppDocumentLoaderTest {


    @Resource
    private CampusAppDocumentLoader campusAppDocumentLoader;

    @Test
    void testLoad() {
        campusAppDocumentLoader.loadDocuments();
    }


}