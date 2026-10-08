package mcp.rag.llm.tools;

import java.time.LocalDateTime;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * LLM이 호출할 수 있는 도구(Tool) 모음
 * - LLM이 질문을 보고 필요하다고 판단하면 자동으로 메서드를 호출함
 */
public class DateTimeTools {

    @Tool(description = "현재 날짜와 시간을 조회한다")
    public String getCurrentDateTime() {
        return LocalDateTime.now().toString();
    }

    @Tool(description = "주문번호로 주문 상태를 조회한다")
    public String getOrderStatus(@ToolParam(description = "주문번호") String orderId) {
        // 실제로는 DB 조회. 지금은 가짜 데이터
        return "주문 " + orderId + " 상태: 배송중";
    }
}
