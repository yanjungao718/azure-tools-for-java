package com.microsoft.azure.toolkit.ide.guidance.config;

import lombok.Data;

import java.util.Map;

@Data
public class InputConfig {
    String name;
    String description;
    Map<String, String> paramMapping;
    Map<String, String> resultMapping;
}
