package com.cac.iam.runner;

import com.cac.iam.service.apply.ApplyService;
import com.cac.iam.util.CommandLineFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplyRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ApplyRunner.class);
    private static final String ARG_APPLY = "--apply";

    private final ApplyService applyService;

    public ApplyRunner(ApplyService applyService) {
        this.applyService = applyService;
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
            applyService.applyMasterPlan();
        } catch (Exception e) {
            log.error("Master plan application failed: {}", e.getMessage(), e);
        }
    }
}
