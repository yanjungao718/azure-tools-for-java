/*
 * Copyright (c) Microsoft Corporation
 *
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azuretools.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * Enums of the Map of backend errors to UI messages for azure tools.
 * All reminder, dialog, etc will use these contents of this class to show whatever.
 */
public enum ErrorUIMapEnum {
    UNKNOWN_HOST_EXCEPTION(ErrorEnum.UNKNOWN_HOST_EXCEPTION, ErrorUIMessageEnum.UNKNOWN_HOST_EXCEPTION),
    SOCKET_TIMEOUT_EXCEPTION(ErrorEnum.SOCKET_TIMEOUT_EXCEPTION, ErrorUIMessageEnum.SOCKET_TIMEOUT_EXCEPTION),
    FAILED_TO_GET_ACCESS_TOKEN_BY_CLI(ErrorEnum.FAILED_TO_GET_ACCESS_TOKEN_BY_CLI, ErrorUIMessageEnum.FAILED_TO_GET_ACCESS_TOKEN_BY_CLI),
    ;

    private ErrorEnum error;
    private ErrorUIMessageEnum viewMessage;

    ErrorUIMapEnum(ErrorEnum error, ErrorUIMessageEnum viewMessage) {
        this.error = error;
        this.viewMessage = viewMessage;
    }

    public ErrorEnum getError() {
        return error;
    }

    public ErrorUIMessageEnum getViewMessage() {
        return viewMessage;
    }

    public static ErrorUIMessageEnum getViewMessageEnumByCode(int code) {
        for (ErrorUIMapEnum e : ErrorUIMapEnum.values()) {
            if (e.getError() != null && e.getError().getErrorCode() == code) {
                return e.getViewMessage();
            }
        }
        return null;
    }

    public static String getViewMessageByCode(int code) {
        ErrorUIMessageEnum viewMessage = getViewMessageEnumByCode(code);
        return viewMessage != null ? viewMessage.getText() : StringUtils.EMPTY;
    }
}
