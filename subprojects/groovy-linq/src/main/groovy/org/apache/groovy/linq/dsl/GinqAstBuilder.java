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
package org.apache.groovy.linq.dsl;

import org.apache.groovy.linq.dsl.expression.FilterExpression;
import org.apache.groovy.linq.dsl.expression.FilterableExpression;
import org.apache.groovy.linq.dsl.expression.FromExpression;
import org.apache.groovy.linq.dsl.expression.GinqExpression;
import org.apache.groovy.linq.dsl.expression.InnerJoinExpression;
import org.apache.groovy.linq.dsl.expression.JoinExpression;
import org.apache.groovy.linq.dsl.expression.OnExpression;
import org.apache.groovy.linq.dsl.expression.SelectExpression;
import org.apache.groovy.linq.dsl.expression.SimpleGinqExpression;
import org.apache.groovy.linq.dsl.expression.WhereExpression;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;

/**
 * Build the AST for GINQ
 *
 * @since 4.0.0
 */
public class GinqAstBuilder extends CodeVisitorSupport implements SyntaxErrorReportable {
    private SimpleGinqExpression simpleGinqExpression = new SimpleGinqExpression(); // store the result
    private GinqExpression ginqExpression; // store the return value
    private final SourceUnit sourceUnit;

    public GinqAstBuilder(SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit;
    }

    public SimpleGinqExpression getSimpleGinqExpression() {
        return simpleGinqExpression;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        super.visitMethodCallExpression(call);
        final String methodName = call.getMethodAsString();

        if ("from".equals(methodName)  || "innerJoin".equals(methodName)) {
            ArgumentListExpression arguments = (ArgumentListExpression) call.getArguments();
            if (arguments.getExpressions().size() != 1) {
                this.collectSyntaxError(
                        new GinqSyntaxError(
                                "Only 1 argument expected for `" + methodName + "`, e.g. `" + methodName + " n in nums`",
                                call.getLineNumber(), call.getColumnNumber()
                        )
                );
            }
            final Expression expression = arguments.getExpression(0);
            if (!(expression instanceof BinaryExpression
                    && ((BinaryExpression) expression).getOperation().getType() == Types.KEYWORD_IN)) {
                this.collectSyntaxError(
                        new GinqSyntaxError(
                                "`in` is expected for `" + methodName + "`, e.g. `" + methodName + " n in nums`",
                                call.getLineNumber(), call.getColumnNumber()
                        )
                );
            }
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            Expression aliasExpr = binaryExpression.getLeftExpression();
            Expression dataSourceExpr = binaryExpression.getRightExpression();

            FilterableExpression filterableExpression = null;
            if ("from".equals(methodName)) {
                filterableExpression = new FromExpression(aliasExpr, dataSourceExpr);
                simpleGinqExpression.setFromExpression((FromExpression) filterableExpression);
            } else if ("innerJoin".equals(methodName)) {
                filterableExpression = new InnerJoinExpression(aliasExpr, dataSourceExpr);
                simpleGinqExpression.addJoinExpression((JoinExpression) filterableExpression);
            }
            filterableExpression.setSourcePosition(call);
            ginqExpression = filterableExpression;

            return;
        }

        if ("where".equals(methodName) || "on".equals(methodName)) {
            Expression filterExpr = ((ArgumentListExpression) call.getArguments()).getExpression(0);

            FilterExpression filterExpression = null;
            if ("where".equals(methodName)) {
                filterExpression = new WhereExpression(filterExpr);
            } else if ("on".equals(methodName)) {
                filterExpression = new OnExpression(filterExpr);
            }

            if (null == filterExpression) {
                throw new GroovyBugError("Unknown method: " + methodName);
            }

            filterExpression.setSourcePosition(call);

            if (ginqExpression instanceof FilterableExpression) { // TODO more strict check
                ((FilterableExpression) ginqExpression).addFilterExpression(filterExpression);
            } else {
                throw new GroovyBugError("The preceding expression is not a FilterableExpression: " + ginqExpression);
            }

            return;
        }

        if ("select".equals(methodName)) {
            SelectExpression selectExpression = new SelectExpression(call.getArguments());
            selectExpression.setSourcePosition(call);

            simpleGinqExpression.setSelectExpression(selectExpression);
            ginqExpression = selectExpression;

            return;
        }
    }

    @Override
    public SourceUnit getSourceUnit() {
        return sourceUnit;
    }
}
