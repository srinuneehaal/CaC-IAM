package com.cac.iam.runner;

import com.cac.iam.service.PlanApplyService;
import com.cac.iam.util.CommandLineFlags;
import com.cac.iam.util.LoggerProvider;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplyRunner implements CommandLineRunner {

    private final Logger log;
    private static final String ARG_APPLY = "--apply";

    private final PlanApplyService applyService;

    @org.springframework.beans.factory.annotation.Autowired
    public ApplyRunner(PlanApplyService applyService, LoggerProvider loggerProvider) {
        this.applyService = applyService;
        this.log = loggerProvider.getLogger(getClass());
    }

    @Override
    public void run(String... args) {
        if (!CommandLineFlags.hasFlag(args, ARG_APPLY)) {
            return;
        }
        if (args != null && args.length > 1) {
            log.warn("Extra arguments detected alongside {}: {}", ARG_APPLY, List.of(args));
        }
        try {
            applyService.applyPlan();
        } catch (Exception e) {
            log.error("Master plan application failed: {}", e.getMessage(), e);
        }
    }
}
