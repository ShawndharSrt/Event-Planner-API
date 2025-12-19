package com.app.events.service;

public interface SequenceGeneratorService {
    long generateSequence(String seqName);

    void syncSequence(String seqName, long minValue);
}
