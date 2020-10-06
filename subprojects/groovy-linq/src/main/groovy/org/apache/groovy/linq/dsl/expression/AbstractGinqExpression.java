/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.linq.dsl.expression;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.NodeMetaDataHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents GINQ expression which could hold meta data
 *
 * @since 4.0.0
 */
public abstract class AbstractGinqExpression implements GinqExpression, NodeMetaDataHandler {
    private Map<?, ?> metaDataMap = new LinkedHashMap<>();
    private int lineNumber = -1;
    private int columnNumber = -1;
    private int lastLineNumber = -1;
    private int lastColumnNumber = -1;

    @Override
    public Map<?, ?> getMetaDataMap() {
        return metaDataMap;
    }

    @Override
    public void setMetaDataMap(Map<?, ?> metaDataMap) {
        this.metaDataMap = metaDataMap;
    }

    public void setSourcePosition(ASTNode node) {
        this.lineNumber = node.getLineNumber();
        this.columnNumber = node.getColumnNumber();
        this.lastLineNumber = node.getLastLineNumber();
        this.lastColumnNumber = node.getLastColumnNumber();
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public int getLastLineNumber() {
        return lastLineNumber;
    }

    public void setLastLineNumber(int lastLineNumber) {
        this.lastLineNumber = lastLineNumber;
    }

    public int getLastColumnNumber() {
        return lastColumnNumber;
    }

    public void setLastColumnNumber(int lastColumnNumber) {
        this.lastColumnNumber = lastColumnNumber;
    }
}
