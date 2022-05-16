package group.insyde.statefun.tsukuyomi.core.validation;

import group.insyde.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;

public interface Matcher {
    void match(TsukuyomiApi tsukuyomi);
}
