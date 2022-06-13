package com.microsoft.azure.toolkit.ide.guidance.phase;

import com.microsoft.azure.toolkit.ide.guidance.Phase;
import com.microsoft.azure.toolkit.ide.guidance.Guidance;
import com.microsoft.azure.toolkit.ide.guidance.config.PhaseConfig;

import javax.annotation.Nonnull;
import javax.swing.*;

public interface GuidancePhaseProvider {
    Phase createPhase(@Nonnull final PhaseConfig config, @Nonnull final Guidance guidance);

    JPanel createPhasePanel(@Nonnull Phase phase);
}
