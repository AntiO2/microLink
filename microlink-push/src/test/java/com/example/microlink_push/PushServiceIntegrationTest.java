package com.example.microlink_push;

import com.example.microlink_push.dto.ContentDTO;
import com.example.microlink_push.dto.PaginatedResponse;
import com.example.microlink_push.dto.StatisticsDTO;
import com.example.microlink_push.service.ContentServiceClient;
import com.example.microlink_push.service.PushService;
import com.example.microlink_push.service.StatisticsServiceClient;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.flowable.engine.HistoryService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration test for the PushService.
 * This test loads the full Spring context and verifies the end-to-end execution
 * of the "hotContentPushProcess" Flowable workflow.
 */
@SpringBootTest
public class PushServiceIntegrationTest {

    // The service we want to test
    @Autowired
    private PushService pushService;

    // We mock the Feign clients to isolate our service from external dependencies.
    @MockBean
    private ContentServiceClient contentServiceClient;

    @MockBean
    private StatisticsServiceClient statisticsServiceClient;

    @Autowired
    private HistoryService historyService;

    private List<ContentDTO> mockContentList;
    private List<StatisticsDTO> mockStatsList;

    @BeforeEach
    void setUp() {
        // --- ARRANGE (GIVEN) ---
        // Create 15 mock content items.
        mockContentList = LongStream.rangeClosed(1, 15)
                .mapToObj(id -> {
                    ContentDTO dto = new ContentDTO(); // 1. 使用无参构造函数创建对象
                    // 2. 调用 setter 方法设置所有必需的属性
                    dto.setId(id);
                    dto.setTitle("Title " + id);
                    dto.setText("Body " + id);
                    dto.setStatus("PUBLISHED");
                    return dto; // 3. 返回完全配置好的对象
                })
                .collect(Collectors.toList());

        // Create mock statistics for each content item.
        // We will give them scores that are easy to predict for our test assertion.
        mockStatsList = new ArrayList<>();
        for (ContentDTO content : mockContentList) {
            StatisticsDTO stats = getStatisticsDTO(content);
            mockStatsList.add(stats);
        }
    }

    private static @NonNull StatisticsDTO getStatisticsDTO(ContentDTO content) {
        long id = content.getId();
        // Let's create a predictable scoring pattern.
        // A higher ID will have more likes, comments, and shares, thus a higher score.
        // contentId=15 will be the hottest, contentId=1 will be the coolest.
        StatisticsDTO stats = new StatisticsDTO();
        stats.setContentId(id);
        stats.setViews(id * 100);
        stats.setLikes(id * 10);
        stats.setComments(id *5);
        stats.setShares(id * 2);
        return stats;
    }

    @Test
    void getHotFeed_should_returnTop10RankedContent_when_processExecutesSuccessfully() {
        // --- ARRANGE (GIVEN) ---
        // 1. Mock the ContentServiceClient response
        PaginatedResponse<ContentDTO> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setContent(mockContentList);
        paginatedResponse.setPageNumber(0);
        paginatedResponse.setPageSize(20);
        paginatedResponse.setTotalElements(15);
        paginatedResponse.setTotalPages(1);
        when(contentServiceClient.listPublishedContent(anyInt(), anyInt(), anyString()))
                .thenReturn(paginatedResponse);

        // 2. Mock the StatisticsServiceClient response for each content ID
        for (StatisticsDTO stats : mockStatsList) {
            when(statisticsServiceClient.getStatisticsForContent(stats.getContentId()))
                    .thenReturn(stats);
        }

        // --- ACT (WHEN) ---
        // Execute the method under test. This will trigger the entire Flowable process.
        String processInstanceId = pushService.startHotFeedProcess();
        List<ContentDTO> hotFeed = pushService.getHotFeed();

        // --- ASSERT (THEN) ---
        assertNotNull(processInstanceId, "A process instance ID should have been returned.");
        // Assertions on the final result retrieved from the history service
        assertNotNull(hotFeed, "Ranked content list should not be null.");
        assertEquals(10, hotFeed.size(), "Should have selected exactly top 10 items.");
        // Verify the sorting logic was correct
        assertEquals(15L, hotFeed.get(0).getId(), "Content with ID 15 should be ranked first.");
        assertEquals(6L, hotFeed.get(9).getId(), "Content with ID 6 should be ranked tenth.");

        // 2. Verify that the items are correctly ranked in descending order of popularity.
        // The content with ID 15 should be first, 14 should be second, and so on.
        assertThat(hotFeed.get(0).getId()).isEqualTo(15L);
        assertThat(hotFeed.get(1).getId()).isEqualTo(14L);
        assertThat(hotFeed.get(2).getId()).isEqualTo(13L);
        assertThat(hotFeed.get(3).getId()).isEqualTo(12L);
        assertThat(hotFeed.get(4).getId()).isEqualTo(11L);
        assertThat(hotFeed.get(5).getId()).isEqualTo(10L);
        assertThat(hotFeed.get(6).getId()).isEqualTo(9L);
        assertThat(hotFeed.get(7).getId()).isEqualTo(8L);
        assertThat(hotFeed.get(8).getId()).isEqualTo(7L);
        assertThat(hotFeed.get(9).getId()).isEqualTo(6L);

        // 3. Verify that the least popular items (IDs 1-5) are not in the list.
        List<Long> hotFeedIds = hotFeed.stream().map(ContentDTO::getId).collect(Collectors.toList());
        assertThat(hotFeedIds).doesNotContain(1L, 2L, 3L, 4L, 5L);

        System.out.println("Test successful! The hot feed is correctly ranked.");
        hotFeed.forEach(content -> System.out.println("  - Content ID: " + content.getId()));
    }
}

