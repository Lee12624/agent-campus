package com.lee.agentgazjku.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

public class CampusAppContextualQueryAugmenterFactory {

    public static ContextualQueryAugmenter create() {
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate(
                """
                你应该输出下面的内容：
                抱歉，我只能回答校园相关的问题，别的没办法帮到您哦
                """);


        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .build();
    }
}
