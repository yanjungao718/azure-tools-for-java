/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.link.po;


import com.microsoft.azure.toolkit.intellij.link.base.ResourceType;
import com.microsoft.azure.toolkit.intellij.link.mysql.JdbcUrl;
import com.microsoft.azure.toolkit.intellij.link.mysql.PasswordSaveType;
import lombok.Getter;
import lombok.Setter;

@Getter
public class MySQLResourcePO extends BaseResourcePO {

    private String url;
    private String username;
    private String database;
    @Setter
    private PasswordSaveType passwordSave;

    private MySQLResourcePO(String id, String resourceId, String url, String username, PasswordSaveType passwordSave) {
        super(id, resourceId, ResourceType.AZURE_DATABASE_FOR_MYSQL);
        this.url = url;
        this.username = username;
        this.database = JdbcUrl.from(url).getDatabase();
        this.passwordSave = passwordSave;
    }

    @Override
    public String getBusinessUniqueKey() {
        return super.getBusinessUniqueKey() + "#" + database;
    }

    public static class Builder {
        private String id;
        private String resourceId;
        private String url;
        private String username;
        private PasswordSaveType passwordSave;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder resourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder passwordSave(PasswordSaveType passwordSave) {
            this.passwordSave = passwordSave;
            return this;
        }

        public MySQLResourcePO build() {
            return new MySQLResourcePO(id, resourceId, url, username, passwordSave);
        }

    }
}
