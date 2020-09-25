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
 * Error info enums with view display names for azure tools.
 *
 * @author qianjinshen
 * @date 2020-09-24 16:34:00
 */
public enum ErrorViewEnum {
    UNKNOWN_HOST_EXCEPTION(ErrorEnum.UNKNOWN_HOST_EXCEPTION, "Please check to confirm your network works well."),
    FAILED_TO_GET_ACCESS_TOKEN_BY_CLI(ErrorEnum.FAILED_TO_GET_ACCESS_TOKEN_BY_CLI, "Please check to confirm your network works well."),
    ;

    private ErrorEnum error;
    private String displayMessage;

    ErrorViewEnum(ErrorEnum error) {
        this.error = error;
        this.displayMessage = error.getErrorMessage();
    }

    ErrorViewEnum(ErrorEnum error, String displayMessage) {
        this.error = error;
        this.displayMessage = displayMessage;
    }

    public ErrorEnum getError() {
        return error;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public static String getDisplayMessageByCode(int code) {
        for (ErrorViewEnum e : ErrorViewEnum.values()) {
            if (e.getError() != null && e.getError().getErrorCode() == code) {
                return e.getDisplayMessage();
            }
        }
        return StringUtils.EMPTY;
    }
}
