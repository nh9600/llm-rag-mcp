package mcp.rag.llm.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;


/**
 * RAG(Retrieval-Augmented Generation) 설정 클래스
 *
 * <p>역할
 * <ul>
 *   <li>VectorStore 빈 등록: 문서 임베딩 벡터를 저장/검색하는 인메모리 저장소(SimpleVectorStore)</li>
 *   <li>서버 기동 시 문서 적재: classpath:docs/ 하위 문서를 읽어 VectorStore에 저장</li>
 * </ul>
 *
 * <p>적재 흐름
 * <pre>
 * 문서 읽기(TextReader) → 청크 분할(TokenTextSplitter)
 *   → 임베딩(EmbeddingModel, Gemini) → VectorStore 저장
 * </pre>
 *
 * <p>참고
 * <ul>
 *   <li>인메모리 저장소이므로 서버 재시작 시 임베딩을 다시 수행함</li>
 *   <li>운영 환경에서는 pgvector 등 영속 VectorStore로 교체 예정</li>
 * </ul>
 */

@Configuration
public class RagConfig {

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    ApplicationRunner loadDocs(VectorStore vectorStore) {
        return args -> {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:docs/*.*");

            List<Document> docs = new ArrayList<>();
            for (Resource r : resources) {
                TextReader reader = new TextReader(r);
                reader.getCustomMetadata().put("filename", r.getFilename());
                docs.addAll(reader.get());
            }

            List<Document> chunks = new TokenTextSplitter().apply(docs);
            vectorStore.add(chunks);
            System.out.println("[RAG] 문서 " + resources.length + "개, 청크 " + chunks.size() + "개 적재");
        };
    }
}
