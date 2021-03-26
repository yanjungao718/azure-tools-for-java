package com.microsoft.azure.toolkit.intellij.azuresdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class AzureJavaSdkEntity {
    @JsonProperty("Package")
    private String packageName;
    @JsonProperty("GroupId")
    private String groupId;
    @JsonProperty("VersionGA")
    private String versionGA;
    @JsonProperty("VersionPreview")
    private String versionPreview;
    @JsonProperty("DisplayName")
    private String displayName;
    @JsonProperty("ServiceName")
    private String serviceName;
    @JsonProperty("RepoPath")
    private String repoPath;
    @JsonProperty("MSDocs")
    private String msDocs;
    @JsonProperty("GHDocs")
    private String ghDocs;
    @JsonProperty("Type")
    private String type;
    @JsonProperty("New")
    private Boolean isNew; // New
    @JsonProperty("PlannedVersions")
    private String plannedVersions;
    @JsonProperty(value = "Hide")
    private Boolean isHide;
    @JsonProperty("Notes")
    private String notes;
}