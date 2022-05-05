package com.github.f1xman.statefun.tsukuyomi.core.validation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(staticName = "of")
@Getter
public class EnvelopeSummary {

    List<EnvelopeMeta> envelopeMetas;
    int totalReceived;

}
