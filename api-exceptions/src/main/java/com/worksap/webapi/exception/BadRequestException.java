/*
 *   Copyright 2016 Works Applications Co.,Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.worksap.webapi.exception;

/**
 * TODO: Write Javadoc
 */
public class BadRequestException extends ApiException {
    private static final String MESSAGE_PREFIX = "400: ";
    private static final String DEFAULT_MESSAGE = MESSAGE_PREFIX + "Bad Request";

    public BadRequestException(String message, Throwable t) {
        super(MESSAGE_PREFIX + message, t);
    }

    public BadRequestException(String message) {
        super(MESSAGE_PREFIX + message);
    }

    public BadRequestException() {
        super(DEFAULT_MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}