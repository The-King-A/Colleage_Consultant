package com.isoft.consultant.config;

import com.isoft.consultant.aiservice.ConsultantService;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Configuration
public class CommonConfig {
    /** OpenAI对话模型实例，由外部配置注入 */
    @Autowired
    private OpenAiChatModel model;
    
    /** Redis会话存储实例，用于持久化用户对话历史 */
    @Autowired
    private ChatMemoryStore redisChatMemoryStore;
    
    /** 向量化模型，用于将文本转换为向量表示 */
    @Autowired
    private EmbeddingModel embeddingModel;
    
    /** Redis向量存储实例，用于存储文档向量 */
    @Autowired
    private RedisEmbeddingStore redisEmbeddingStore;
    @Bean
    public ChatMemoryProvider chatMemoryProvider(){
        ChatMemoryProvider chatMemoryProvider=new ChatMemoryProvider(){
            
            @Override
            public ChatMemory get(Object memoryId){
               return MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)
                       .chatMemoryStore(redisChatMemoryStore)
                        .build();
            }
        };
        return chatMemoryProvider;
    }


    @Bean
    public EmbeddingStore store(){
        // 1.加载文档进内存
        // List<Document> documents = ClassPathDocumentLoader.loadDocuments("content");
        // 加载文档的时候指定解析器，支持PDF文档解析
        List<Document> documents = ClassPathDocumentLoader.loadDocuments("content",new ApachePdfBoxDocumentParser());
        
        // 3.构建文档分割器对象
        DocumentSplitter ds = DocumentSplitters.recursive(500,100);
        
        // 4.构建一个EmbeddingStoreIngestor对象，完成文本数据切割、向量化和存储
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                // 使用Redis向量存储替代内存存储
                .embeddingStore(redisEmbeddingStore)
                // 设置文档分割器
                .documentSplitter(ds)
                // 设置向量化模型
                .embeddingModel(embeddingModel)
                .build();
                
        // 执行文档摄入流程：解析->分割->向量化->存储
        ingestor.ingest(documents);
        return redisEmbeddingStore;
    }
    @Bean
    public ContentRetriever contentRetriever(/*EmbeddingStore store*/) {
        return EmbeddingStoreContentRetriever.builder()
                // 指定向量存储实例
                .embeddingStore(redisEmbeddingStore)
                // 设置相似度阈值为0.5，只返回相关性较高的结果
                .minScore(0.5)
                // 最多返回3个相关结果，避免信息过载
                .maxResults(3)
                // 指定向量化模型，需与文档处理时保持一致
                .embeddingModel(embeddingModel)
                .build();
    }
}