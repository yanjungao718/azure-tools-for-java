package com.microsoft.azure.toolkit.ide.guidance.phase;

import com.microsoft.azure.toolkit.ide.guidance.Phase;
import com.microsoft.azure.toolkit.ide.guidance.Guidance;
import com.microsoft.azure.toolkit.ide.guidance.config.PhaseConfig;
import com.microsoft.azure.toolkit.ide.guidance.view.components.SummaryPhasePanel;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.swing.*;

public class SummaryPhaseProvider implements GuidancePhaseProvider {
    private static final String SUMMARY = "summary";

    @Override
    public Phase createPhase(@Nonnull PhaseConfig config, @Nonnull Guidance guidance) {
        return StringUtils.equals(config.getType(), SUMMARY) ? new Phase(config, guidance) : null;
    }

    @Override
    public JPanel createPhasePanel(@Nonnull Phase phase) {
        return StringUtils.equals(phase.getType(), SUMMARY) ? new SummaryPhasePanel(phase) : null;
    }
}
