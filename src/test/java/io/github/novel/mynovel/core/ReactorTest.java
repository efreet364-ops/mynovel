package io.github.novel.mynovel.core;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactorTest {

    @Test
    void doReactor() {
        Flux.just("1", "2", "3").subscribe(System.out::println);
        Mono.just("a").subscribe(System.out::println);
    }

}
