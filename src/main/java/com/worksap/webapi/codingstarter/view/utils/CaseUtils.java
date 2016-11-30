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

package com.worksap.webapi.codingstarter.view.utils;

import org.jboss.dna.common.text.Inflector;

import java.util.regex.Pattern;

/**
 * TODO: Write Javadoc
 */
public class CaseUtils {
    private static Pattern WORD_SPLIT_PATTERN = Pattern.compile("[^a-zA-Z]");

    public String pluralize(Object word) {
        return inflector.pluralize(word);
    }

    public String pluralize(Object word, int count) {
        return inflector.pluralize(word, count);
    }

    public String singularize(Object word) {
        return inflector.singularize(word);
    }

    public String lowerCamelCase(String lowerCaseAndUnderscoredWord, char... delimiterChars) {
        return inflector.lowerCamelCase(lowerCaseAndUnderscoredWord, delimiterChars);
    }

    public String upperCamelCase(String lowerCaseAndUnderscoredWord, char... delimiterChars) {
        return inflector.upperCamelCase(lowerCaseAndUnderscoredWord, delimiterChars);
    }

    public String underscore(String camelCaseWord, char... delimiterChars) {
        return inflector.underscore(camelCaseWord, delimiterChars);
    }

    public String capitalize(String words) {
        return inflector.capitalize(words);
    }

    public String humanize(String lowerCaseAndUnderscoredWords, String... removableTokens) {
        return inflector.humanize(lowerCaseAndUnderscoredWords, removableTokens);
    }

    public String titleCase(String words, String... removableTokens) {
        return inflector.titleCase(words, removableTokens);
    }

    public String ordinalize(int number) {
        return inflector.ordinalize(number);
    }

    private final Inflector inflector;

    public CaseUtils(Inflector inflector) {
        this.inflector = inflector;
    }

    public String chainCase(String words) {
        if (words == null) return null;
        return inflector.underscore(words, ' ').replace('_', '-');
    }
}
