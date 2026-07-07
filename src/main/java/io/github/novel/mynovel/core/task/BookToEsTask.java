package io.github.novel.mynovel.core.task;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.novel.mynovel.dao.entity.BookInfo;
import io.github.novel.mynovel.dao.mapper.BookInfoMapper;
import io.github.novel.mynovel.dto.es.EsBookDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
//@ConditionalOnProperty(prefix = "spring.elasticsearch", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class BookToEsTask {

    private static final IndexCoordinates BOOK_INDEX = IndexCoordinates.of("book");

    private static final int PAGE_SIZE = 1000;

    private final BookInfoMapper bookInfoMapper;

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 每天凌晨 3 点将 MySQL 中 word_count > 0 的小说全量同步到 Elasticsearch。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void syncBookToEs() {
        log.info("开始全量同步小说信息到 Elasticsearch");

        deleteAllBookDocuments();

        long current = 1;
        long total = 0;

        while (true) {
            Page<BookInfo> page = new Page<>(current, PAGE_SIZE);
            LambdaQueryWrapper<BookInfo> queryWrapper = new LambdaQueryWrapper<BookInfo>()
                    .gt(BookInfo::getWordCount, 0)
                    .orderByAsc(BookInfo::getId);

            IPage<BookInfo> bookPage = bookInfoMapper.selectPage(page, queryWrapper);
            List<EsBookDto> esBooks = bookPage.getRecords().stream()
                    .map(EsBookDto::build)
                    .toList();

            if (esBooks.isEmpty()) {
                break;
            }

            elasticsearchOperations.save(esBooks, BOOK_INDEX);
            total += esBooks.size();

            if (current >= bookPage.getPages()) {
                break;
            }
            current++;
        }

        elasticsearchOperations.indexOps(BOOK_INDEX).refresh();
        log.info("完成全量同步小说信息到 Elasticsearch，同步数量：{}", total);
    }

    private void deleteAllBookDocuments() {
        NativeQuery query = NativeQuery.builder()
                .withQuery(Query.of(q -> q.matchAll(m -> m)))
                .build();

        DeleteQuery deleteQuery = DeleteQuery.builder(query).build();
        elasticsearchOperations.delete(deleteQuery, EsBookDto.class, BOOK_INDEX);
        elasticsearchOperations.indexOps(BOOK_INDEX).refresh();
    }
}
