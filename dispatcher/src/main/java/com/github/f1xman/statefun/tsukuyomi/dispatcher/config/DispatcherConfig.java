package com.github.f1xman.statefun.tsukuyomi.dispatcher.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class DispatcherConfig {

    @Getter
    StatefunModule module;

}
