/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.applib.services.exceprecog;

import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.Programmatic;

/**
 * Abstract implementation of {@link ExceptionRecognizer} that looks 
 * exceptions meeting the {@link Predicate} supplied in the constructor
 * and, if found anywhere in the {@link Throwables#getCausalChain(Throwable) causal chain},
 * then returns a non-null message indicating that the exception has been recognized.
 * 
 * <p>
 * If a messaging-parsing {@link Function} is provided through the constructor,
 * then the message can be altered.  Otherwise the exception's {@link Throwable#getMessage() message} is returned as-is.
 */
public abstract class ExceptionRecognizerAbstract implements ExceptionRecognizer2 {

    public static final Logger LOG = LoggerFactory.getLogger(ExceptionRecognizerAbstract.class);

    /**
     * Normally recognized exceptions are not logged (because they are expected and handled).  
     * 
     * <p>
     * This key is primarily for diagnostic purposes, to log the exception regardless.
     */
    private static final String KEY_LOG_RECOGNIZED_EXCEPTIONS = "isis.services.exceprecog.logRecognizedExceptions";


    /**
     * Convenience for subclass implementations that always return a fixed message.
     */
    protected static Function<String, String> constant(final String message) {
        return new Function<String, String>() {

            @Override
            public String apply(String input) {
                return message;
            }
        };
    }

    /**
     * Convenience for subclass implementations that always prefixes the exception message
     * with the supplied text
     */
    protected static Function<String, String> prefix(final String prefix) {
        return new Function<String, String>() {

            @Override
            public String apply(String input) {
                return prefix + ": " + input;
            }
        };
    }
    
    // //////////////////////////////////////


    private final Category category;
    private final Predicate<Throwable> predicate;
    private final Function<String,String> messageParser;
    
    private boolean logRecognizedExceptions;

    // //////////////////////////////////////

    public ExceptionRecognizerAbstract(final Category category, Predicate<Throwable> predicate, final Function<String,String> messageParser) {
        this.category = category;
        this.predicate = predicate;
        this.messageParser = messageParser != null? messageParser: Functions.<String>identity();
    }

    public ExceptionRecognizerAbstract(Predicate<Throwable> predicate, final Function<String,String> messageParser) {
        this(Category.OTHER, predicate, messageParser);
    }

    public ExceptionRecognizerAbstract(Category category, Predicate<Throwable> predicate) {
        this(category, predicate, null);
    }

    public ExceptionRecognizerAbstract(Predicate<Throwable> predicate) {
        this(Category.OTHER, predicate);
    }


    @PostConstruct
    public void init(Map<String, String> properties) {
        final String prop = properties.get(KEY_LOG_RECOGNIZED_EXCEPTIONS);
        this.logRecognizedExceptions = Boolean.parseBoolean(prop);
    }

    @PreDestroy
    public void shutdown() {
    }

    // //////////////////////////////////////

    @Programmatic
    public String recognize(Throwable ex) {
        List<Throwable> causalChain = Throwables.getCausalChain(ex);
        for (Throwable throwable : causalChain) {
            if(predicate.apply(throwable)) {
                if(logRecognizedExceptions) {
                    LOG.info("Recognized exception, stacktrace : ", throwable);
                }
                final Throwable rootCause = Throwables.getRootCause(throwable);
                final String rootCauseMessage = rootCause.getMessage();
                final String parsedMessage = messageParser.apply(rootCauseMessage);
                return parsedMessage;
            }
        }
        return null;
    }

    @Programmatic
    @Override
    public Recognition recognize2(Throwable ex) {
        return Recognition.of(category, recognize(ex));
    }

}
