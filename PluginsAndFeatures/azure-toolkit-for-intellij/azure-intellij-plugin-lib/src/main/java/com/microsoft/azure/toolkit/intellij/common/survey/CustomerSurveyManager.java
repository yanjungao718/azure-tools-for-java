/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.common.survey;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomerSurveyManager {
    private static final String AZURE_TOOLS_FOLDER = ".AzureToolsForIntelliJ";
    private static final String BASE_FOLDER = Paths.get(System.getProperty("user.home"), AZURE_TOOLS_FOLDER).toString();
    private static final String FILE_NAME_SURVEY_CONFIG = "SurveyConfig.json";

    private static final int INIT_SURVEY_DELAY_BY_DAY = 10;
    private static final int PUT_OFF_DELAY_BY_DAY = 30;
    private static final int TAKE_SURVEY_DELAY_BY_DAY = 180;

    private final CustomerSurveyConfiguration customerSurveyConfiguration;
    private final AtomicBoolean aBoolean = new AtomicBoolean(true);

    public static CustomerSurveyManager getInstance() {
        return HOLDER.INSTANCE;
    }

    private CustomerSurveyManager(CustomerSurveyConfiguration customerSurveyConfiguration) {
        this.customerSurveyConfiguration = customerSurveyConfiguration;
    }

    public synchronized void takeSurvey(final Project project, final ICustomerSurvey survey) {
        if (!customerSurveyConfiguration.isAcceptSurvey()) {
            return;
        }
        final CustomerSurveyConfiguration.CustomerSurveyStatus status = getSurveyStatus(survey);
        if (status == null) {
            // init status for new survey
            customerSurveyConfiguration.getSurveyStatus().add(createNewStatus(survey));
            saveSurveyStatus();
            return;
        }
        if (LocalDateTime.now().isAfter(status.getNextSurveyDate()) && aBoolean.get()) {
            // show survey pop up
            aBoolean.set(false);
            ApplicationManager.getApplication().invokeLater(() -> showSurveyPopup(project, survey));
        }
    }

    private void showSurveyPopup(final Project project, final ICustomerSurvey survey) {
        final SurveyPopUpDialog popUpDialog = new SurveyPopUpDialog(project, survey, result -> {
            try {
                if (result == CustomerSurveyResponse.ACCEPT) {
                    // redirect to browser
                    BrowserUtil.browse(survey.getLink());
                }
                updateSurveyStatus(survey, result);
            } finally {
                aBoolean.set(true);
            }
        });
        popUpDialog.setVisible(true);
    }

    private CustomerSurveyConfiguration.CustomerSurveyStatus getSurveyStatus(ICustomerSurvey survey) {
        return customerSurveyConfiguration.getSurveyStatus().stream()
                .filter(status -> StringUtils.equalsIgnoreCase(status.getType(), survey.getType()))
                .findFirst().orElse(null);
    }

    private CustomerSurveyConfiguration.CustomerSurveyStatus createNewStatus(ICustomerSurvey customerSurvey) {
        return CustomerSurveyConfiguration.CustomerSurveyStatus.builder()
                .type(customerSurvey.getType())
                .surveyTimes(0)
                .nextSurveyDate(LocalDateTime.now().plusDays(INIT_SURVEY_DELAY_BY_DAY)).build();
    }

    private void updateSurveyStatus(ICustomerSurvey survey, CustomerSurveyResponse response) {
        if (response == CustomerSurveyResponse.NEVER_SHOW_AGAIN) {
            customerSurveyConfiguration.setAcceptSurvey(false);
        } else {
            final int delay = response == CustomerSurveyResponse.ACCEPT ? TAKE_SURVEY_DELAY_BY_DAY : PUT_OFF_DELAY_BY_DAY;
            final CustomerSurveyConfiguration.CustomerSurveyStatus surveyStatus = getSurveyStatus(survey);
            surveyStatus.setSurveyTimes(surveyStatus.getSurveyTimes() + 1);
            surveyStatus.setLastSurveyDate(LocalDateTime.now());
            surveyStatus.setNextSurveyDate(LocalDateTime.now().plusDays(delay * 2));
            customerSurveyConfiguration.getSurveyStatus().stream()
                    .filter(status -> !StringUtils.equalsIgnoreCase(status.getType(), survey.getType()))
                    .forEach(customerSurveyStatus -> customerSurveyStatus.setNextSurveyDate(LocalDateTime.now().plusDays(delay)));
        }
        saveSurveyStatus();
    }

    private void saveSurveyStatus() {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        final File file = getConfigurationFile();
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            writer.writeValue(file, customerSurveyConfiguration);
        } catch (final IOException e) {
            e.printStackTrace();
            // swallow exceptions for survey
        }
    }

    private static File getConfigurationFile() {
        return new File(BASE_FOLDER, FILE_NAME_SURVEY_CONFIG);
    }

    private static final class HOLDER {
        private static final CustomerSurveyManager INSTANCE;

        static {
            CustomerSurveyConfiguration surveyConfiguration;
            final ObjectMapper mapper = new ObjectMapper();
            final File configurationFile = getConfigurationFile();
            try {
                surveyConfiguration = mapper.readValue(configurationFile, CustomerSurveyConfiguration.class);
            } catch (final JsonProcessingException jsonException) {
                try {
                    final SurveyConfig config = mapper.readValue(configurationFile, SurveyConfig.class);
                    final CustomerSurveyConfiguration.CustomerSurveyStatus surveyStatus = CustomerSurveyConfiguration.CustomerSurveyStatus.builder()
                            .type(CustomerSurvey.AZURE_INTELLIJ_TOOLKIT.name())
                            .surveyTimes(config.surveyTimes)
                            .lastSurveyDate(config.lastSurveyDate)
                            .nextSurveyDate(config.nextSurveyDate).build();
                    surveyConfiguration = new CustomerSurveyConfiguration();
                    surveyConfiguration.getSurveyStatus().add(surveyStatus);
                } catch (final IOException e) {
                    surveyConfiguration = new CustomerSurveyConfiguration();
                }
            } catch (final IOException e) {
                surveyConfiguration = new CustomerSurveyConfiguration();
            }
            INSTANCE = new CustomerSurveyManager(surveyConfiguration);
        }
    }

    @Deprecated
    static class SurveyConfig {
        @JsonProperty("surveyTimes")
        private final int surveyTimes = 0;
        @JsonProperty("isAcceptSurvey")
        private final boolean isAcceptSurvey = true;
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private final LocalDateTime lastSurveyDate = null;
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private final LocalDateTime nextSurveyDate = LocalDateTime.now().plusDays(INIT_SURVEY_DELAY_BY_DAY);
    }
}
