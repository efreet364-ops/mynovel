package io.github.novel.mynovel.service.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import io.github.novel.mynovel.core.common.resp.PageRespDto;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dto.es.EsBookDto;
import io.github.novel.mynovel.dto.req.BookSearchReqDto;
import io.github.novel.mynovel.dto.resp.BookInfoRespDto;
import io.github.novel.mynovel.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ConditionalOnProperty(prefix = "spring.elasticsearch", name = "enabled", havingValue = "true")
@Service
@RequiredArgsConstructor
public class EsSearchServiceImpl implements SearchService {

    private static final IndexCoordinates BOOK_INDEX = IndexCoordinates.of("book");

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public RestResp<PageRespDto<BookInfoRespDto>> searchBooks(BookSearchReqDto condition) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(buildQuery(condition))
                .withPageable(PageRequest.of(condition.getPageNum() - 1, condition.getPageSize()))
                .withSort(s -> s.field(f -> f.field(resolveSortField(condition.getSort()))
                        .order(resolveSortOrder(condition.getOrder()))))
                .build();

        SearchHits<EsBookDto> searchHits = elasticsearchOperations.search(query, EsBookDto.class, BOOK_INDEX);
        List<BookInfoRespDto> books = searchHits.stream()
                .map(SearchHit::getContent)
                .map(this::toBookInfoRespDto)
                .toList();

        return RestResp.ok(PageRespDto.of(
                condition.getPageNum(),
                condition.getPageSize(),
                searchHits.getTotalHits(),
                books));
    }

    private Query buildQuery(BookSearchReqDto condition) {
        List<Query> filters = buildFilters(condition);

        return Query.of(q -> q.bool(b -> {
            if (StringUtils.hasText(condition.getKeyword())) {
                b.must(m -> m.multiMatch(mm -> mm
                        .query(condition.getKeyword())
                        .fields("bookName", "authorName", "bookDesc", "categoryName", "lastChapterName")));
            } else {
                b.must(m -> m.matchAll(ma -> ma));
            }
            filters.forEach(b::filter);
            return b;
        }));
    }

    private List<Query> buildFilters(BookSearchReqDto condition) {
        List<Query> filters = new ArrayList<>();
        filters.add(Query.of(q -> q.range(r -> r.number(n -> n.field("wordCount").gt(0.0)))));

        if (condition.getWorkDirection() != null) {
            filters.add(termQuery("workDirection", condition.getWorkDirection()));
        }
        if (condition.getCategoryId() != null) {
            filters.add(termQuery("categoryId", condition.getCategoryId()));
        }
        if (condition.getIsVip() != null) {
            filters.add(termQuery("isVip", condition.getIsVip()));
        }
        if (condition.getBookStatus() != null) {
            filters.add(termQuery("bookStatus", condition.getBookStatus()));
        }
        if (condition.getWordCountMin() != null || condition.getWordCountMax() != null) {
            filters.add(buildWordCountRangeQuery(condition));
        }
        if (condition.getUpdateTimeMin() != null) {
            filters.add(Query.of(q -> q.range(r -> r.number(n -> n
                    .field("lastChapterUpdateTime")
                    .gte(toShanghaiStartOfDayMillis(condition.getUpdateTimeMin()).doubleValue())))));
        }

        return filters;
    }

    private Query buildWordCountRangeQuery(BookSearchReqDto condition) {
        return Query.of(q -> q.range(r -> r.number(n -> {
            n.field("wordCount");
            if (condition.getWordCountMin() != null) {
                n.gte(condition.getWordCountMin().doubleValue());
            }
            if (condition.getWordCountMax() != null) {
                n.lt(condition.getWordCountMax().doubleValue());
            }
            return n;
        })));
    }

    private Query termQuery(String field, Number value) {
        return Query.of(q -> q.term(t -> t.field(field).value(FieldValue.of(value.longValue()))));
    }

    private Long toShanghaiStartOfDayMillis(Date date) {
        return date.toInstant()
                .atZone(ZONE_ID)
                .toLocalDate()
                .atStartOfDay(ZONE_ID)
                .toInstant()
                .toEpochMilli();
    }

    private String resolveSortField(String sort) {
        if ("last_chapter_update_time".equals(sort)) {
            return "lastChapterUpdateTime";
        }
        if ("word_count".equals(sort)) {
            return "wordCount";
        }
        if ("visit_count".equals(sort)) {
            return "visitCount";
        }
        return "score";
    }

    private SortOrder resolveSortOrder(String order) {
        if ("asc".equalsIgnoreCase(order)) {
            return SortOrder.Asc;
        }
        return SortOrder.Desc;
    }

    private BookInfoRespDto toBookInfoRespDto(EsBookDto esBook) {
        return BookInfoRespDto.builder()
                .id(esBook.getId())
                .categoryId(esBook.getCategoryId())
                .categoryName(esBook.getCategoryName())
                .bookName(esBook.getBookName())
                .authorId(esBook.getAuthorId())
                .authorName(esBook.getAuthorName())
                .bookDesc(esBook.getBookDesc())
                .bookStatus(esBook.getBookStatus())
                .visitCount(esBook.getVisitCount())
                .wordCount(esBook.getWordCount())
                .commentCount(esBook.getCommentCount())
                .lastChapterId(esBook.getLastChapterId())
                .lastChapterName(esBook.getLastChapterName())
                .updateTime(toLocalDateTime(esBook.getLastChapterUpdateTime()))
                .build();
    }

    private LocalDateTime toLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZONE_ID);
    }
}
