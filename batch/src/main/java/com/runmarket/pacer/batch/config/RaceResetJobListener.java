package com.runmarket.pacer.batch.config;

import com.runmarket.pacer.batch.crawler.CrawlStateStore;
import com.runmarket.pacer.domain.port.in.race.DeleteAllRacesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RaceResetJobListener implements JobExecutionListener {

    private final DeleteAllRacesUseCase deleteAllRacesUseCase;
    private final CrawlStateStore stateStore;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        if (stateStore.isFirstRun()) {
            log.info("crawl_state.json 없음 — 기존 대회 전체 삭제 후 재크롤 시작");
            deleteAllRacesUseCase.deleteAll();
        }
    }
}
