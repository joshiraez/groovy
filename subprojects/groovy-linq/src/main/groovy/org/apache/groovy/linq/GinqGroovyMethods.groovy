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
package org.apache.groovy.linq

import groovy.transform.CompileStatic
import org.apache.groovy.linq.dsl.GinqAstBuilder
import org.apache.groovy.linq.dsl.GinqAstWalker
import org.apache.groovy.linq.dsl.expression.SimpleGinqExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.macro.runtime.Macro
import org.codehaus.groovy.macro.runtime.MacroContext

@CompileStatic
class GinqGroovyMethods {
    @Macro
    static Expression GINQ(MacroContext ctx, final ClosureExpression closureExpression) {
        Statement code = closureExpression.getCode()

        GinqAstBuilder ginqAstBuilder = new GinqAstBuilder(ctx.getSourceUnit())
        code.visit(ginqAstBuilder)
        SimpleGinqExpression simpleGinqExpression = ginqAstBuilder.getSimpleGinqExpression()

        GinqAstWalker ginqBuilder = new GinqAstWalker(ctx.getSourceUnit())
        MethodCallExpression selectMethodCallExpression = ginqBuilder.visitSimpleGinqExpression(simpleGinqExpression)

        return selectMethodCallExpression
    }
}
