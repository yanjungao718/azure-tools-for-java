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

/**
 * Enums of UI messages.
 */
public enum ErrorUIMessageEnum {

    UNKNOWN_HOST_EXCEPTION("It seems you have an unstable network at the moment, please try again when network is available."),
    SOCKET_TIMEOUT_EXCEPTION("Timeout when accessing azure, please try your operation again."),
    FAILED_TO_GET_ACCESS_TOKEN_BY_CLI("Failed to get access token, please try to login Azure CLI using 'az login' and try again."),
    ;

    private String text;

    public String getText() {
        return text;
    }

    ErrorUIMessageEnum(String text) {
        this.text = text;
    }
}
