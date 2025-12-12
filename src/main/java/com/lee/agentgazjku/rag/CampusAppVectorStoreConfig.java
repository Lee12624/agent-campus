package com.lee.agentgazjku.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CampusAppVectorStoreConfig {

    /**
     * 配置校园应用向量存储
     * @param dashscopeEmbeddingModel
     * @return 校园应用向量存储 基于内存
     */


    @Resource
    private CampusAppDocumentLoader campusAppDocumentLoader;

    @Bean
    VectorStore campusAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        List<Document> documents = campusAppDocumentLoader.loadDocuments();
        simpleVectorStore.add(documents);
        return simpleVectorStore;

    }

}
