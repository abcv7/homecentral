package com.homecentral.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AiClientTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    private AiClient aiClient;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.when(chatClientBuilder.build()).thenReturn(chatClient);
        aiClient = new AiClient(chatClientBuilder);
    }

    static class TestItem {
        public String name;
        public Integer count;
        public TestItem() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    @Test
    void parseJsonArrayShouldReturnMappedList() {
        String raw = "[{\"name\":\"apple\",\"count\":2},{\"name\":\"milk\",\"count\":1}]";

        List<TestItem> result = aiClient.parseJsonArray(raw, TestItem.class);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("apple", result.get(0).getName());
        assertEquals(Integer.valueOf(2), result.get(0).getCount());
        assertEquals("milk", result.get(1).getName());
    }

    @Test
    void parseJsonArrayShouldStripMarkdownCodeBlock() {
        String raw = "```json\n[{\"name\":\"banana\",\"count\":3}]\n```";

        List<TestItem> result = aiClient.parseJsonArray(raw, TestItem.class);

        assertEquals(1, result.size());
        assertEquals("banana", result.get(0).getName());
    }

    @Test
    void parseJsonArrayShouldReturnEmptyListOnInvalidJson() {
        String raw = "not json at all";

        List<TestItem> result = aiClient.parseJsonArray(raw, TestItem.class);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseJsonArrayShouldReturnEmptyListOnBlankInput() {
        List<TestItem> result = aiClient.parseJsonArray("", TestItem.class);
        assertTrue(result.isEmpty());

        result = aiClient.parseJsonArray(null, TestItem.class);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseJsonArrayShouldReturnEmptyListOnNonArrayJson() {
        String raw = "{\"name\":\"not an array\"}";

        List<TestItem> result = aiClient.parseJsonArray(raw, TestItem.class);

        assertTrue(result.isEmpty());
    }
}
