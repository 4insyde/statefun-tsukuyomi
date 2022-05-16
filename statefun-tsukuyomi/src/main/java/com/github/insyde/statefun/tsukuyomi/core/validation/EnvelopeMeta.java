package com.github.insyde.statefun.tsukuyomi.core.validation;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(staticName = "of")
@Getter
@EqualsAndHashCode
public class EnvelopeMeta {

    int index;

}
