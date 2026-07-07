package io.github.novel.mynovel;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import io.github.novel.mynovel.dto.es.EsBookDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Elasticsearch 测试用例
 * <p>
 * 前置条件：本地运行 Elasticsearch 8.x（默认连接 localhost:9200），
 * 且已通过 {@code doc/es/book.http} 创建了 {@code book} 索引及其 mapping。
 * <p>
 * 测试覆盖：
 * <ul>
 *   <li>连接检查</li>
 *   <li>文档 CRUD（增删改查）</li>
 *   <li>批量操作</li>
 *   <li>各类搜索查询（term / match / multi_match / range / fuzzy / wildcard / bool）</li>
 *   <li>分页与排序</li>
 *   <li>高亮查询</li>
 *   <li>聚合查询</li>
 * </ul>
 *
 * @author efreet233
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
@DisplayName("Elasticsearch 测试")
class ESTest {

    private static final IndexCoordinates BOOK_INDEX = IndexCoordinates.of("book");

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    // ==================== 测试数据 ====================

    private static final List<EsBookDto> TEST_BOOKS = Arrays.asList(
            EsBookDto.builder()
                    .id(1L).workDirection(0).categoryId(1L).categoryName("玄幻")
                    .bookName("斗破苍穹").authorId(101L).authorName("天蚕土豆")
                    .bookDesc("天才少年萧炎在创造了家族空前绝后的修炼纪录后突然成了废人").score(85)
                    .bookStatus(1).visitCount(1000000L).wordCount(5320000)
                    .commentCount(5000).lastChapterId(10001L).lastChapterName("大结局")
                    .lastChapterUpdateTime(1719100800000L).isVip(1)
                    .build(),
            EsBookDto.builder()
                    .id(2L).workDirection(0).categoryId(1L).categoryName("玄幻")
                    .bookName("完美世界").authorId(102L).authorName("辰东")
                    .bookDesc("一粒尘可填海，一根草斩尽日月星辰，弹指间天翻地覆").score(90)
                    .bookStatus(1).visitCount(800000L).wordCount(6500000)
                    .commentCount(4000).lastChapterId(20001L).lastChapterName("终章")
                    .lastChapterUpdateTime(1719100800000L).isVip(1)
                    .build(),
            EsBookDto.builder()
                    .id(3L).workDirection(0).categoryId(2L).categoryName("都市")
                    .bookName("全职高手").authorId(103L).authorName("蝴蝶蓝")
                    .bookDesc("网游荣耀中被誉为教科书级别的顶尖高手叶修").score(95)
                    .bookStatus(1).visitCount(1200000L).wordCount(5300000)
                    .commentCount(6000).lastChapterId(30001L).lastChapterName("荣耀永不散场")
                    .lastChapterUpdateTime(1719100800000L).isVip(1)
                    .build(),
            EsBookDto.builder()
                    .id(4L).workDirection(1).categoryId(3L).categoryName("言情")
                    .bookName("何以笙箫默").authorId(104L).authorName("顾漫")
                    .bookDesc("一段年少时的爱恋，牵出一生的纠缠").score(80)
                    .bookStatus(1).visitCount(500000L).wordCount(250000)
                    .commentCount(3000).lastChapterId(40001L).lastChapterName("尾声")
                    .lastChapterUpdateTime(1716508800000L).isVip(0)
                    .build(),
            EsBookDto.builder()
                    .id(5L).workDirection(0).categoryId(4L).categoryName("科幻")
                    .bookName("三体").authorId(105L).authorName("刘慈欣")
                    .bookDesc("文化大革命如火如荼进行的同时，军方探寻外星文明的绝秘计划取得了突破性进展").score(98)
                    .bookStatus(1).visitCount(2000000L).wordCount(900000)
                    .commentCount(10000).lastChapterId(50001L).lastChapterName("死神永生")
                    .lastChapterUpdateTime(1704067200000L).isVip(1)
                    .build(),
            EsBookDto.builder()
                    .id(6L).workDirection(0).categoryId(2L).categoryName("都市")
                    .bookName("都市之最强狂兵").authorId(106L).authorName("沧海残阳")
                    .bookDesc("一代兵王回归都市，掀起滔天巨浪").score(55)
                    .bookStatus(0).visitCount(300000L).wordCount(3200000)
                    .commentCount(1500).lastChapterId(60001L).lastChapterName("第一千二百章")
                    .lastChapterUpdateTime(1720800000000L).isVip(1)
                    .build(),
            EsBookDto.builder()
                    .id(7L).workDirection(0).categoryId(1L).categoryName("玄幻")
                    .bookName("凡人修仙传").authorId(107L).authorName("忘语")
                    .bookDesc("一个普通山村小子，偶然下进入到当地江湖小门派，成了一名记名弟子").score(88)
                    .bookStatus(1).visitCount(1500000L).wordCount(7700000)
                    .commentCount(8000).lastChapterId(70001L).lastChapterName("仙界篇终章")
                    .lastChapterUpdateTime(1716508800000L).isVip(1)
                    .build()
    );

    // ==================== 连接检查 ====================

    @Test
    @DisplayName("01 - 检查 ES 连接")
    void test01_checkConnection() {
        assertNotNull(elasticsearchOperations, "ElasticsearchOperations 未注入");
        assertDoesNotThrow(() -> {
            var clusterHealth = elasticsearchOperations.cluster().health();
            System.out.println("ES 集群名称: " + clusterHealth.getClusterName());
            System.out.println("ES 集群状态: " + clusterHealth.getStatus());
        }, "无法连接到 Elasticsearch，请确保 ES 服务已启动");
    }

    // ==================== 文档 CRUD ====================

    @Test
    @DisplayName("02 - 保存单个文档")
    void test02_saveDocument() {
        EsBookDto book = TEST_BOOKS.getFirst();
        EsBookDto saved = elasticsearchOperations.save(book, BOOK_INDEX);

        assertNotNull(saved, "保存后的文档不应为 null");
        assertEquals(book.getId(), saved.getId());

        // 刷新使文档立即可搜索
        elasticsearchOperations.indexOps(BOOK_INDEX).refresh();
        System.out.println("保存文档成功: " + saved.getBookName());
    }

    @Test
    @DisplayName("03 - 根据 ID 查找文档")
    void test03_findById() {
        ensureDataLoaded();

        EsBookDto doc = elasticsearchOperations.get("1", EsBookDto.class, BOOK_INDEX);
        assertNotNull(doc, "根据 ID=1 应能查到文档");
        assertEquals("斗破苍穹", doc.getBookName());
        assertEquals("天蚕土豆", doc.getAuthorName());
        assertEquals("玄幻", doc.getCategoryName());
        System.out.println("查找到文档: " + doc.getBookName() + " - " + doc.getAuthorName());
    }

    @Test
    @DisplayName("04 - 更新文档")
    void test04_updateDocument() {
        ensureDataLoaded();

        EsBookDto doc = elasticsearchOperations.get("1", EsBookDto.class, BOOK_INDEX);
        assertNotNull(doc, "更新前文档应存在");

        doc.setScore(90);
        doc.setVisitCount(2000000L);
        doc.setBookName("斗破苍穹（修订版）");

        EsBookDto updated = elasticsearchOperations.save(doc, BOOK_INDEX);
        elasticsearchOperations.indexOps(BOOK_INDEX).refresh();

        assertEquals(90, updated.getScore());
        assertEquals("斗破苍穹（修订版）", updated.getBookName());
        System.out.println("更新文档成功: " + updated.getBookName() + " 评分: " + updated.getScore());

        // 恢复原始数据
        doc.setBookName("斗破苍穹");
        doc.setScore(85);
        doc.setVisitCount(1000000L);
        elasticsearchOperations.save(doc, BOOK_INDEX);
        elasticsearchOperations.indexOps(BOOK_INDEX).refresh();
    }

    @Test
    @DisplayName("05 - 批量保存文档")
    void test05_batchSave() {
        elasticsearchOperations.save(TEST_BOOKS, BOOK_INDEX);
        elasticsearchOperations.indexOps(BOOK_INDEX).refresh();

        long count = elasticsearchOperations.count(
                NativeQuery.builder().withQuery(Query.of(q -> q.matchAll(m -> m))).build(),
                EsBookDto.class, BOOK_INDEX);

        assertEquals(TEST_BOOKS.size(), count, "批量保存后文档数应匹配");
        System.out.println("批量保存 " + TEST_BOOKS.size() + " 条文档成功");
    }

    // ==================== 查询操作 ====================

    @Test
    @DisplayName("06 - 统计文档总数")
    void test06_count() {
        ensureDataLoaded();

        long count = elasticsearchOperations.count(
                NativeQuery.builder().withQuery(Query.of(q -> q.matchAll(m -> m))).build(),
                EsBookDto.class, BOOK_INDEX);

        assertEquals(TEST_BOOKS.size(), count);
        System.out.println("索引 book 文档总数: " + count);
    }

    @Test
    @DisplayName("07 - term 精确词条查询")
    void test07_termQuery() {
        ensureDataLoaded();

        CriteriaQuery query = new CriteriaQuery(new Criteria("categoryName").is("玄幻"));

        SearchHits<EsBookDto> hits = elasticsearchOperations.search(query, EsBookDto.class, BOOK_INDEX);
        assertTrue(hits.getTotalHits() > 0, "应有玄幻类书籍");
        System.out.println("=== term 查询: categoryName=玄幻 (共 " + hits.getTotalHits() + " 条) ===");
        hits.forEach(hit -> System.out.println("  " + hit.getContent().getBookName()
                + " | 评分: " + hit.getContent().getScore()));
    }

    @Test
    @DisplayName("08 - match 全文匹配查询")
    void test08_matchQuery() {
        ensureDataLoaded();

        NativeQuery query = NativeQuery.builder()
                .withQuery(Query.of(q -> q.match(m -> m.field("bookDesc").query("天才"))))
                .build();

        SearchHits<EsBookDto> hits = elasticsearchOperations.search(query, EsBookDto.class, BOOK_INDEX);
        assertTrue(hits.getTotalHits() > 0, "应有描述中包含'天才'的书籍");
        System.out.println("=== match 查询: bookDesc 匹配 '天才' (共 " + hits.getTotalHits() + " 条) ===");
        hits.forEach(hit -> System.out.println("  " + hit.getContent().getBookName()
                + " | " + hit.getContent().getBookDesc()
                + " | 评分: " + hit.getScore()));
    }

    @Test
    @DisplayName("09 - multi_match 多字段匹配查询")
    void test09_multiMatchQuery() {
        ensureDataLoaded();

        NativeQuery query = NativeQuery.builder()
                .withQuery(Query.of(q -> q.multiMatch(mm -> mm
                        .fields("bookName", "bookDesc").query("都市"))))
                .build();

        SearchHits<EsBookDto> hits = elasticsearchOperations.search(query, EsBookDto.class, BOOK_INDEX);
        assertTrue(hits.getTotalHits() > 0, "应有匹配'都市'的书籍");
        System.out.println("=== multi_match 查询: bookName + bookDesc 匹配 '都市' (共 " + hits.getTotalHits() + " 条) ===");
        hits.forEach(hit -> System.out.println("  " + hit.getContent().getBookName()
                + " | " + hit.getContent().getCategoryName()));
    }

    @Test
    @DisplayName("10 - range 范围查询")
    void test10_rangeQuery() {
        ensureDataLoaded();

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.range(r -> r.number(n -> n
                        .field("score")
                        .gte(80.0)
                        .lte(100.0)
                )))
                .build();

        SearchHits<EsBookDto> hits = elasticsearchOperations.search(query, EsBookDto.class, BOOK_INDEX);
        assertTrue(hits.getTotalHits() > 0, "应有评分在 80-100 之间的书籍");
        System.out.println("=== range 查询: 80 <= score <= 100 (共 " + hits.getTotalHits() + " 条) ===");
        hits.forEach(hit -> System.out.println("  " + hit.getContent().getBookName()
                + " | 评分: " + hit.getContent().getScore()
                + " | 字数: " + hit.getContent().getWordCount()));
    }

    @Test
    @DisplayName("11 - bool 布尔组合查询")
    void test11_boolQuery() {
        ensureDataLoaded();

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t
                                .field("bookStatus")
                                .value(1)
                        ))
                        .must(m -> m.range(r -> r
                                .number(n -> n
                                        .field("score")
                                        .gte(85.0)
                                )
                        ))
                        .should(s -> s.term(t -> t
                                .field("categoryName")
                                .value("玄幻")
                        ))
                        .should(s -> s.term(t -> t
                                .field("categoryName")
                                .value("科幻")
                        ))
                        .minimumShouldMatch("1")
                ))
                .build();

        SearchHits<EsBookDto> hits = elasticsearchOperations.search(query, EsBookDto.class, BOOK_INDEX);
        assertTrue(hits.getTotalHits() > 0, "应有满足 bool 条件的书籍");
        System.out.println("=== bool 查询: (玄幻 OR 科幻) AND 已完结 AND 评分>=85 (共 " + hits.getTotalHits() + " 条) ===");
        hits.forEach(hit -> System.out.println("  " + hit.getContent().getBookName()
                + " | " + hit.getContent().getCategoryName()
                + " | 评分: " + hit.getContent().getScore()));
    }

    @Test
    @DisplayName("12 - match 查询书名")
    void test12_matchBookNameQuery() {
        ensureDataLoaded();

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.match(m -> m
                        .field("bookName")
                        .query("斗破")
                ))
                .build();

        SearchHits<EsBookDto> hits =
                elasticsearchOperations.search(query, EsBookDto.class, BOOK_INDEX);

        assertTrue(hits.getTotalHits() > 0, "书名匹配查询应有结果");

        hits.forEach(hit -> System.out.println(
                hit.getContent().getBookName() + " | score: " + hit.getScore()
        ));
    }

    @Test
    @DisplayName("13 - wildcard 通配符查询")
    void test13_wildcardQuery() {
        ensureDataLoaded();

        NativeQuery query = NativeQuery.builder()
                .withQuery(Query.of(q -> q.wildcard(w -> w.field("bookName").value("*之*"))))
                .build();

        SearchHits<EsBookDto> hits = elasticsearchOperations.search(query, EsBookDto.class, BOOK_INDEX);
        assertTrue(hits.getTotalHits() > 0, "通配符查询应有结果");
        System.out.println("=== wildcard 查询: bookName 包含 '*之*' (共 " + hits.getTotalHits() + " 条) ===");
        hits.forEach(hit -> System.out.println("  " + hit.getContent().getBookName()));
    }

    // ==================== 分页与排序 ====================

    @Test
    @DisplayName("14 - 分页与排序")
    void test14_paginationAndSort() {
        ensureDataLoaded();

        PageRequest page1Req = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "score"));
        NativeQuery page1Query = NativeQuery.builder()
                .withQuery(Query.of(q -> q.matchAll(m -> m)))
                .withPageable(page1Req)
                .build();

        SearchHits<EsBookDto> page1 = elasticsearchOperations.search(page1Query, EsBookDto.class, BOOK_INDEX);
        assertEquals(3, page1.getSearchHits().size(), "第1页应有3条记录");
        System.out.println("=== 分页: 第1页 (每页3条，按评分降序) ===");
        page1.forEach(hit -> System.out.println("  " + hit.getContent().getBookName()
                + " | 评分: " + hit.getContent().getScore()));

        PageRequest page2Req = PageRequest.of(1, 3, Sort.by(Sort.Direction.DESC, "score"));
        NativeQuery page2Query = NativeQuery.builder()
                .withQuery(Query.of(q -> q.matchAll(m -> m)))
                .withPageable(page2Req)
                .build();

        SearchHits<EsBookDto> page2 = elasticsearchOperations.search(page2Query, EsBookDto.class, BOOK_INDEX);
        System.out.println("=== 分页: 第2页 ===");
        page2.forEach(hit -> System.out.println("  " + hit.getContent().getBookName()
                + " | 评分: " + hit.getContent().getScore()));

        // 验证分页不重叠
        List<Long> page1Ids = page1.getSearchHits().stream()
                .map(h -> h.getContent().getId()).collect(Collectors.toList());
        List<Long> page2Ids = page2.getSearchHits().stream()
                .map(h -> h.getContent().getId()).collect(Collectors.toList());
        assertTrue(page1Ids.stream().noneMatch(page2Ids::contains),
                "第1页和第2页的文档不应重叠");
    }

    // ==================== 高亮查询 ====================

    @Test
    @DisplayName("15 - 高亮查询")
    void test15_highlightQuery() {
        ensureDataLoaded();

        // 字段级别参数
        HighlightFieldParameters fieldParams = HighlightFieldParameters.builder()
                .withFragmentSize(200)
                .withNumberOfFragments(1)
                .build();

        HighlightField highlightBookName = new HighlightField("bookName", fieldParams);
        HighlightField highlightBookDesc = new HighlightField("bookDesc", fieldParams);

        // 全局参数
        HighlightParameters highlightParams = HighlightParameters.builder()
                .withPreTags("<em style='color:red'>")
                .withPostTags("</em>")
                .build();

        Highlight highlight = new Highlight(highlightParams,
                List.of(highlightBookName, highlightBookDesc));
        HighlightQuery highlightQuery = new HighlightQuery(highlight, EsBookDto.class);

        NativeQuery query = NativeQuery.builder()
                .withQuery(Query.of(q -> q.multiMatch(mm -> mm
                        .fields("bookName", "bookDesc").query("天才"))))
                .withHighlightQuery(highlightQuery)
                .build();

        SearchHits<EsBookDto> hits = elasticsearchOperations.search(query, EsBookDto.class, BOOK_INDEX);
        assertTrue(hits.getTotalHits() > 0, "高亮查询应有结果");

        System.out.println("=== 高亮查询: '天才' (共 " + hits.getTotalHits() + " 条) ===");
        hits.forEach(hit -> {
            System.out.println("  文档: " + hit.getContent().getBookName());
            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            highlightFields.forEach((field, values) ->
                    System.out.println("    高亮[" + field + "]: " + values.get(0)));
        });
    }

    // ==================== 聚合查询 ====================

    @Test
    @DisplayName("16 - 聚合查询")
    void test16_aggregation() {
        ensureDataLoaded();

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.matchAll(m -> m))
                .withAggregation("by_category", Aggregation.of(a -> a
                        .terms(t -> t.field("categoryName.keyword").size(10))))
                .withAggregation("avg_score", Aggregation.of(a -> a
                        .avg(avg -> avg.field("score"))))
                .withAggregation("max_visit", Aggregation.of(a -> a
                        .max(max -> max.field("visitCount"))))
                .withMaxResults(0)
                .build();

        SearchHits<EsBookDto> hits = elasticsearchOperations.search(query, EsBookDto.class, BOOK_INDEX);

        Object aggs = hits.getAggregations();
        assertNotNull(aggs, "聚合结果不应为 null");

        System.out.println("=== 聚合查询 ===");
        if (aggs instanceof ElasticsearchAggregations eas) {
            eas.aggregationsAsMap().forEach((name, agg) ->
                    System.out.println("  " + name + ": " + agg.aggregation()));
        } else {
            System.out.println("  聚合结果: " + aggs);
        }
    }

    // ==================== 删除文档 ====================

    @Test
    @DisplayName("17 - 删除文档")
    void test17_deleteDocument() {
        ensureDataLoaded();

        // 创建测试文档
        EsBookDto temp = EsBookDto.builder()
                .id(999L)
                .bookName("临时测试小说")
                .authorName("测试作者")
                .categoryName("测试分类")
                .workDirection(0)
                .score(60)
                .bookStatus(0)
                .build();

        // 保存
        elasticsearchOperations.save(temp, BOOK_INDEX);
        elasticsearchOperations.indexOps(BOOK_INDEX).refresh();

        // 验证存在
        EsBookDto beforeDelete =
                elasticsearchOperations.get("999", EsBookDto.class, BOOK_INDEX);

        assertNotNull(beforeDelete);

        // 删除
        elasticsearchOperations.delete("999", BOOK_INDEX);
        elasticsearchOperations.indexOps(BOOK_INDEX).refresh();

        // 再次查询，应不存在
        EsBookDto afterDelete =
                elasticsearchOperations.get("999", EsBookDto.class, BOOK_INDEX);

        assertNull(afterDelete);

        System.out.println("删除文档 999 成功");
    }

    @Test
    @DisplayName("18 - 删除所有文档")
    void test18_deleteAllDocument() {
        ensureDataLoaded();
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.matchAll(m -> m))
                .build();

        DeleteQuery deleteQuery = DeleteQuery.builder(query).build();
        elasticsearchOperations.delete(deleteQuery, EsBookDto.class, BOOK_INDEX);
        elasticsearchOperations.indexOps(BOOK_INDEX).refresh();

        long count = elasticsearchOperations.count(
                NativeQuery.builder()
                        .withQuery(q -> q.matchAll(m -> m))
                        .build(),
                EsBookDto.class,
                BOOK_INDEX
        );

        assertEquals(0, count, "删除所有文档后，索引中文档数应为 0");
        System.out.println("删除所有文档成功，当前文档数: " + count);
    }

    // ==================== 辅助方法 ====================

    private void ensureDataLoaded() {
        long count = elasticsearchOperations.count(
                NativeQuery.builder().withQuery(Query.of(q -> q.matchAll(m -> m))).build(),
                EsBookDto.class, BOOK_INDEX);
        if (count < TEST_BOOKS.size()) {
            elasticsearchOperations.save(TEST_BOOKS, BOOK_INDEX);
            elasticsearchOperations.indexOps(BOOK_INDEX).refresh();
        }
    }
}
