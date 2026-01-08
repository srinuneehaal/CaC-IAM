package com.cac.iam.runner;

import com.cac.iam.service.PlanApplyService;
import com.cac.iam.util.CommandLineFlags;
import com.cac.iam.util.LoggerProvider;
import com.cac.iam.util.ShutdownManager;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplyRunner implements CommandLineRunner {

    private final Logger log;
    private static final String ARG_APPLY = "--apply";

    private final PlanApplyService applyService;
    private final ShutdownManager shutdownManager;

    @org.springframework.beans.factory.annotation.Autowired
    public ApplyRunner(PlanApplyService applyService, LoggerProvider loggerProvider, ShutdownManager shutdownManager) {
        this.applyService = applyService;
        this.shutdownManager = shutdownManager;
        this.log = loggerProvider.getLogger(getClass());
    }

    ApplyRunner(PlanApplyService applyService, LoggerProvider loggerProvider) {
        this(applyService, loggerProvider, ShutdownManager.noOp());
    }

    @Override
    public void run(String... args) {
        if (!CommandLineFlags.hasFlag(args, ARG_APPLY)) {
            return;
        }
        if (args != null && args.length > 1) {
            log.warn("Extra arguments detected alongside {}: {}", ARG_APPLY, List.of(args));
        }
        int exitCode = 0;
        try {
            applyService.applyPlan();
        } catch (Exception e) {
            log.error("Master plan application failed: {}", e.getMessage(), e);
            exitCode = 1;
        } finally {
            shutdownManager.shutdown(exitCode);
        }
    }
}
