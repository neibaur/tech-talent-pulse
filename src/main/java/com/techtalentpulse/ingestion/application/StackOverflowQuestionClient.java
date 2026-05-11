package com.techtalentpulse.ingestion.application;

import java.util.List;

public interface StackOverflowQuestionClient {

  List<StackOverflowQuestionPayload> fetchRecentQuestions(String tag, int pageSize);
}
