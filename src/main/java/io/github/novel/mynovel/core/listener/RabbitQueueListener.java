package io.github.novel.mynovel.core.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import io.github.novel.mynovel.core.constant.AmqpConsts;
import io.github.novel.mynovel.dao.entity.BookInfo;
import io.github.novel.mynovel.dao.mapper.BookInfoMapper;
import io.github.novel.mynovel.dto.es.EsBookDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

/**
 * Rabbit 队列监听器
 */
@Component
@ConditionalOnProperty(prefix = "spring", name = {"elasticsearch.enabled",
        "amqp.enabled"}, havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class RabbitQueueListener {

    private final BookInfoMapper bookInfoMapper;

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 监听小说信息改变的 ES 更新队列，更新最新小说信息到 ES
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = AmqpConsts.BookChangeMq.QUEUE_ES_UPDATE),
            exchange = @Exchange(name = AmqpConsts.BookChangeMq.EXCHANGE_NAME, type = ExchangeTypes.FANOUT)
    ))
    @SneakyThrows
    public void updateEsBook(Long bookId) {
        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (bookInfo == null) {
            log.warn("同步 ES 失败，书籍不存在，bookId={}", bookId);
            return;
        }

        EsBookDto esBook = EsBookDto.build(bookInfo);

        elasticsearchOperations.save(
                esBook,
                IndexCoordinates.of("book")
        );

        log.info("同步 ES 成功，bookId={}", bookId);
    }

}
