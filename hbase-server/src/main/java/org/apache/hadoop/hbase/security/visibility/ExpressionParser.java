begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|security
operator|.
name|visibility
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Stack
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|security
operator|.
name|visibility
operator|.
name|expression
operator|.
name|ExpressionNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|security
operator|.
name|visibility
operator|.
name|expression
operator|.
name|LeafExpressionNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|security
operator|.
name|visibility
operator|.
name|expression
operator|.
name|NonLeafExpressionNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|security
operator|.
name|visibility
operator|.
name|expression
operator|.
name|Operator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ExpressionParser
block|{
specifier|private
specifier|static
specifier|final
name|char
name|CLOSE_PARAN
init|=
literal|')'
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|char
name|OPEN_PARAN
init|=
literal|'('
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|char
name|OR
init|=
literal|'|'
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|char
name|AND
init|=
literal|'&'
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|char
name|NOT
init|=
literal|'!'
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|char
name|SPACE
init|=
literal|' '
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|char
name|DOUBLE_QUOTES
init|=
literal|'"'
decl_stmt|;
specifier|public
name|ExpressionNode
name|parse
parameter_list|(
name|String
name|expS
parameter_list|)
throws|throws
name|ParseException
block|{
name|expS
operator|=
name|expS
operator|.
name|trim
argument_list|()
expr_stmt|;
name|Stack
argument_list|<
name|ExpressionNode
argument_list|>
name|expStack
init|=
operator|new
name|Stack
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
name|byte
index|[]
name|exp
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|expS
argument_list|)
decl_stmt|;
name|int
name|endPos
init|=
name|exp
operator|.
name|length
decl_stmt|;
while|while
condition|(
name|index
operator|<
name|endPos
condition|)
block|{
name|byte
name|b
init|=
name|exp
index|[
name|index
index|]
decl_stmt|;
switch|switch
condition|(
name|b
condition|)
block|{
case|case
name|OPEN_PARAN
case|:
name|processOpenParan
argument_list|(
name|expStack
argument_list|,
name|expS
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|index
operator|=
name|skipSpaces
argument_list|(
name|exp
argument_list|,
name|index
argument_list|)
expr_stmt|;
break|break;
case|case
name|CLOSE_PARAN
case|:
name|processCloseParan
argument_list|(
name|expStack
argument_list|,
name|expS
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|index
operator|=
name|skipSpaces
argument_list|(
name|exp
argument_list|,
name|index
argument_list|)
expr_stmt|;
break|break;
case|case
name|AND
case|:
case|case
name|OR
case|:
name|processANDorOROp
argument_list|(
name|getOperator
argument_list|(
name|b
argument_list|)
argument_list|,
name|expStack
argument_list|,
name|expS
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|index
operator|=
name|skipSpaces
argument_list|(
name|exp
argument_list|,
name|index
argument_list|)
expr_stmt|;
break|break;
case|case
name|NOT
case|:
name|processNOTOp
argument_list|(
name|expStack
argument_list|,
name|expS
argument_list|,
name|index
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE_QUOTES
case|:
name|int
name|labelOffset
init|=
operator|++
name|index
decl_stmt|;
comment|// We have to rewrite the expression within double quotes as incase of expressions
comment|// with escape characters we may have to avoid them as the original expression did
comment|// not have them
name|List
argument_list|<
name|Byte
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|index
operator|<
name|endPos
operator|&&
operator|!
name|endDoubleQuotesFound
argument_list|(
name|exp
index|[
name|index
index|]
argument_list|)
condition|)
block|{
if|if
condition|(
name|exp
index|[
name|index
index|]
operator|==
literal|'\\'
condition|)
block|{
name|index
operator|++
expr_stmt|;
if|if
condition|(
name|exp
index|[
name|index
index|]
operator|!=
literal|'\\'
operator|&&
name|exp
index|[
name|index
index|]
operator|!=
literal|'"'
condition|)
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"invalid escaping with quotes "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
name|list
operator|.
name|add
argument_list|(
name|exp
index|[
name|index
index|]
argument_list|)
expr_stmt|;
name|index
operator|++
expr_stmt|;
block|}
comment|// The expression has come to the end. still no double quotes found
if|if
condition|(
name|index
operator|==
name|endPos
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"No terminating quotes "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
comment|// This could be costly. but do we have any alternative?
comment|// If we don't do this way then we may have to handle while checking the authorizations.
comment|// Better to do it here.
name|byte
index|[]
name|array
init|=
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|primitives
operator|.
name|Bytes
operator|.
name|toArray
argument_list|(
name|list
argument_list|)
decl_stmt|;
name|String
name|leafExp
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|array
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|leafExp
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
name|processLabelExpNode
argument_list|(
operator|new
name|LeafExpressionNode
argument_list|(
name|leafExp
argument_list|)
argument_list|,
name|expStack
argument_list|,
name|expS
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|index
operator|=
name|skipSpaces
argument_list|(
name|exp
argument_list|,
name|index
argument_list|)
expr_stmt|;
break|break;
default|default:
name|labelOffset
operator|=
name|index
expr_stmt|;
do|do
block|{
if|if
condition|(
operator|!
name|VisibilityLabelsValidator
operator|.
name|isValidAuthChar
argument_list|(
name|exp
index|[
name|index
index|]
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
name|index
operator|++
expr_stmt|;
block|}
do|while
condition|(
name|index
operator|<
name|endPos
operator|&&
operator|!
name|isEndOfLabel
argument_list|(
name|exp
index|[
name|index
index|]
argument_list|)
condition|)
do|;
name|leafExp
operator|=
operator|new
name|String
argument_list|(
name|exp
argument_list|,
name|labelOffset
argument_list|,
name|index
operator|-
name|labelOffset
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
if|if
condition|(
name|leafExp
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
name|processLabelExpNode
argument_list|(
operator|new
name|LeafExpressionNode
argument_list|(
name|leafExp
argument_list|)
argument_list|,
name|expStack
argument_list|,
name|expS
argument_list|,
name|index
argument_list|)
expr_stmt|;
comment|// We already crossed the label node index. So need to reduce 1 here.
name|index
operator|--
expr_stmt|;
name|index
operator|=
name|skipSpaces
argument_list|(
name|exp
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
name|index
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|expStack
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
argument_list|)
throw|;
block|}
name|ExpressionNode
name|top
init|=
name|expStack
operator|.
name|pop
argument_list|()
decl_stmt|;
if|if
condition|(
name|top
operator|==
name|LeafExpressionNode
operator|.
name|OPEN_PARAN_NODE
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
argument_list|)
throw|;
block|}
if|if
condition|(
name|top
operator|instanceof
name|NonLeafExpressionNode
condition|)
block|{
name|NonLeafExpressionNode
name|nlTop
init|=
operator|(
name|NonLeafExpressionNode
operator|)
name|top
decl_stmt|;
if|if
condition|(
name|nlTop
operator|.
name|getOperator
argument_list|()
operator|==
name|Operator
operator|.
name|NOT
condition|)
block|{
if|if
condition|(
name|nlTop
operator|.
name|getChildExps
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|nlTop
operator|.
name|getChildExps
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
argument_list|)
throw|;
block|}
block|}
return|return
name|top
return|;
block|}
specifier|private
name|int
name|skipSpaces
parameter_list|(
name|byte
index|[]
name|exp
parameter_list|,
name|int
name|index
parameter_list|)
block|{
while|while
condition|(
name|index
operator|<
name|exp
operator|.
name|length
operator|-
literal|1
operator|&&
name|exp
index|[
name|index
operator|+
literal|1
index|]
operator|==
name|SPACE
condition|)
block|{
name|index
operator|++
expr_stmt|;
block|}
return|return
name|index
return|;
block|}
specifier|private
name|void
name|processCloseParan
parameter_list|(
name|Stack
argument_list|<
name|ExpressionNode
argument_list|>
name|expStack
parameter_list|,
name|String
name|expS
parameter_list|,
name|int
name|index
parameter_list|)
throws|throws
name|ParseException
block|{
if|if
condition|(
name|expStack
operator|.
name|size
argument_list|()
operator|<
literal|2
condition|)
block|{
comment|// When ) comes we expect atleast a ( node and another leaf/non leaf node
comment|// in stack.
throw|throw
operator|new
name|ParseException
argument_list|()
throw|;
block|}
else|else
block|{
name|ExpressionNode
name|top
init|=
name|expStack
operator|.
name|pop
argument_list|()
decl_stmt|;
name|ExpressionNode
name|secondTop
init|=
name|expStack
operator|.
name|pop
argument_list|()
decl_stmt|;
comment|// The second top must be a ( node and top should not be a ). Top can be
comment|// any thing else
if|if
condition|(
name|top
operator|==
name|LeafExpressionNode
operator|.
name|OPEN_PARAN_NODE
operator|||
name|secondTop
operator|!=
name|LeafExpressionNode
operator|.
name|OPEN_PARAN_NODE
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
comment|// a&(b|) is not valid.
comment|// The top can be a ! node but with exactly child nodes. !).. is invalid
comment|// Other NonLeafExpressionNode , then there should be exactly 2 child.
comment|// (a&) is not valid.
if|if
condition|(
name|top
operator|instanceof
name|NonLeafExpressionNode
condition|)
block|{
name|NonLeafExpressionNode
name|nlTop
init|=
operator|(
name|NonLeafExpressionNode
operator|)
name|top
decl_stmt|;
if|if
condition|(
operator|(
name|nlTop
operator|.
name|getOperator
argument_list|()
operator|==
name|Operator
operator|.
name|NOT
operator|&&
name|nlTop
operator|.
name|getChildExps
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
operator|)
operator|||
operator|(
name|nlTop
operator|.
name|getOperator
argument_list|()
operator|!=
name|Operator
operator|.
name|NOT
operator|&&
name|nlTop
operator|.
name|getChildExps
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|2
operator|)
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
block|}
comment|// When (a|b)&(c|d) comes while processing the second ) there will be
comment|// already (a|b)& node
comment|// avail in the stack. The top will be c|d node. We need to take it out
comment|// and combine as one
comment|// node.
if|if
condition|(
operator|!
name|expStack
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ExpressionNode
name|thirdTop
init|=
name|expStack
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|thirdTop
operator|instanceof
name|NonLeafExpressionNode
condition|)
block|{
name|NonLeafExpressionNode
name|nlThirdTop
init|=
operator|(
name|NonLeafExpressionNode
operator|)
name|expStack
operator|.
name|pop
argument_list|()
decl_stmt|;
name|nlThirdTop
operator|.
name|addChildExp
argument_list|(
name|top
argument_list|)
expr_stmt|;
if|if
condition|(
name|nlThirdTop
operator|.
name|getOperator
argument_list|()
operator|==
name|Operator
operator|.
name|NOT
condition|)
block|{
comment|// It is a NOT node. So there may be a NonLeafExpressionNode below
comment|// it to which the
comment|// completed NOT can be added now.
if|if
condition|(
operator|!
name|expStack
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ExpressionNode
name|fourthTop
init|=
name|expStack
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|fourthTop
operator|instanceof
name|NonLeafExpressionNode
condition|)
block|{
comment|// Its Operator will be OR or AND
name|NonLeafExpressionNode
name|nlFourthTop
init|=
operator|(
name|NonLeafExpressionNode
operator|)
name|fourthTop
decl_stmt|;
assert|assert
name|nlFourthTop
operator|.
name|getOperator
argument_list|()
operator|!=
name|Operator
operator|.
name|NOT
assert|;
comment|// Also for sure its number of children will be 1
assert|assert
name|nlFourthTop
operator|.
name|getChildExps
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|nlFourthTop
operator|.
name|addChildExp
argument_list|(
name|nlThirdTop
argument_list|)
expr_stmt|;
return|return;
comment|// This case no need to add back the nlThirdTop.
block|}
block|}
block|}
name|top
operator|=
name|nlThirdTop
expr_stmt|;
block|}
block|}
name|expStack
operator|.
name|push
argument_list|(
name|top
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|processOpenParan
parameter_list|(
name|Stack
argument_list|<
name|ExpressionNode
argument_list|>
name|expStack
parameter_list|,
name|String
name|expS
parameter_list|,
name|int
name|index
parameter_list|)
throws|throws
name|ParseException
block|{
if|if
condition|(
operator|!
name|expStack
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ExpressionNode
name|top
init|=
name|expStack
operator|.
name|peek
argument_list|()
decl_stmt|;
comment|// Top can not be a Label Node. a(.. is not valid. but ((a.. is fine.
if|if
condition|(
name|top
operator|instanceof
name|LeafExpressionNode
operator|&&
name|top
operator|!=
name|LeafExpressionNode
operator|.
name|OPEN_PARAN_NODE
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|top
operator|instanceof
name|NonLeafExpressionNode
condition|)
block|{
comment|// Top is non leaf.
comment|// It can be ! node but with out any child nodes. !a(.. is invalid
comment|// Other NonLeafExpressionNode , then there should be exactly 1 child.
comment|// a&b( is not valid.
comment|// a&( is valid though. Also !( is valid
name|NonLeafExpressionNode
name|nlTop
init|=
operator|(
name|NonLeafExpressionNode
operator|)
name|top
decl_stmt|;
if|if
condition|(
operator|(
name|nlTop
operator|.
name|getOperator
argument_list|()
operator|==
name|Operator
operator|.
name|NOT
operator|&&
name|nlTop
operator|.
name|getChildExps
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|0
operator|)
operator|||
operator|(
name|nlTop
operator|.
name|getOperator
argument_list|()
operator|!=
name|Operator
operator|.
name|NOT
operator|&&
name|nlTop
operator|.
name|getChildExps
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
operator|)
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
block|}
block|}
name|expStack
operator|.
name|push
argument_list|(
name|LeafExpressionNode
operator|.
name|OPEN_PARAN_NODE
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|processLabelExpNode
parameter_list|(
name|LeafExpressionNode
name|node
parameter_list|,
name|Stack
argument_list|<
name|ExpressionNode
argument_list|>
name|expStack
parameter_list|,
name|String
name|expS
parameter_list|,
name|int
name|index
parameter_list|)
throws|throws
name|ParseException
block|{
if|if
condition|(
name|expStack
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|expStack
operator|.
name|push
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ExpressionNode
name|top
init|=
name|expStack
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|top
operator|==
name|LeafExpressionNode
operator|.
name|OPEN_PARAN_NODE
condition|)
block|{
name|expStack
operator|.
name|push
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|top
operator|instanceof
name|NonLeafExpressionNode
condition|)
block|{
name|NonLeafExpressionNode
name|nlTop
init|=
operator|(
name|NonLeafExpressionNode
operator|)
name|expStack
operator|.
name|pop
argument_list|()
decl_stmt|;
name|nlTop
operator|.
name|addChildExp
argument_list|(
name|node
argument_list|)
expr_stmt|;
if|if
condition|(
name|nlTop
operator|.
name|getOperator
argument_list|()
operator|==
name|Operator
operator|.
name|NOT
operator|&&
operator|!
name|expStack
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ExpressionNode
name|secondTop
init|=
name|expStack
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|secondTop
operator|==
name|LeafExpressionNode
operator|.
name|OPEN_PARAN_NODE
condition|)
block|{
name|expStack
operator|.
name|push
argument_list|(
name|nlTop
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|secondTop
operator|instanceof
name|NonLeafExpressionNode
condition|)
block|{
operator|(
operator|(
name|NonLeafExpressionNode
operator|)
name|secondTop
operator|)
operator|.
name|addChildExp
argument_list|(
name|nlTop
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|expStack
operator|.
name|push
argument_list|(
name|nlTop
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|void
name|processANDorOROp
parameter_list|(
name|Operator
name|op
parameter_list|,
name|Stack
argument_list|<
name|ExpressionNode
argument_list|>
name|expStack
parameter_list|,
name|String
name|expS
parameter_list|,
name|int
name|index
parameter_list|)
throws|throws
name|ParseException
block|{
if|if
condition|(
name|expStack
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
name|ExpressionNode
name|top
init|=
name|expStack
operator|.
name|pop
argument_list|()
decl_stmt|;
if|if
condition|(
name|top
operator|.
name|isSingleNode
argument_list|()
condition|)
block|{
if|if
condition|(
name|top
operator|==
name|LeafExpressionNode
operator|.
name|OPEN_PARAN_NODE
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
name|expStack
operator|.
name|push
argument_list|(
operator|new
name|NonLeafExpressionNode
argument_list|(
name|op
argument_list|,
name|top
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|NonLeafExpressionNode
name|nlTop
init|=
operator|(
name|NonLeafExpressionNode
operator|)
name|top
decl_stmt|;
if|if
condition|(
name|nlTop
operator|.
name|getChildExps
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
name|expStack
operator|.
name|push
argument_list|(
operator|new
name|NonLeafExpressionNode
argument_list|(
name|op
argument_list|,
name|nlTop
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|processNOTOp
parameter_list|(
name|Stack
argument_list|<
name|ExpressionNode
argument_list|>
name|expStack
parameter_list|,
name|String
name|expS
parameter_list|,
name|int
name|index
parameter_list|)
throws|throws
name|ParseException
block|{
comment|// When ! comes, the stack can be empty or top ( or top can be some exp like
comment|// a&
comment|// !!.., a!, a&b!, !a! are invalid
if|if
condition|(
operator|!
name|expStack
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ExpressionNode
name|top
init|=
name|expStack
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|top
operator|.
name|isSingleNode
argument_list|()
operator|&&
name|top
operator|!=
name|LeafExpressionNode
operator|.
name|OPEN_PARAN_NODE
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|top
operator|.
name|isSingleNode
argument_list|()
operator|&&
operator|(
operator|(
name|NonLeafExpressionNode
operator|)
name|top
operator|)
operator|.
name|getChildExps
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Error parsing expression "
operator|+
name|expS
operator|+
literal|" at column : "
operator|+
name|index
argument_list|)
throw|;
block|}
block|}
name|expStack
operator|.
name|push
argument_list|(
operator|new
name|NonLeafExpressionNode
argument_list|(
name|Operator
operator|.
name|NOT
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|boolean
name|endDoubleQuotesFound
parameter_list|(
name|byte
name|b
parameter_list|)
block|{
return|return
operator|(
name|b
operator|==
name|DOUBLE_QUOTES
operator|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isEndOfLabel
parameter_list|(
name|byte
name|b
parameter_list|)
block|{
return|return
operator|(
name|b
operator|==
name|OPEN_PARAN
operator|||
name|b
operator|==
name|CLOSE_PARAN
operator|||
name|b
operator|==
name|OR
operator|||
name|b
operator|==
name|AND
operator|||
name|b
operator|==
name|NOT
operator|||
name|b
operator|==
name|SPACE
operator|)
return|;
block|}
specifier|private
specifier|static
name|Operator
name|getOperator
parameter_list|(
name|byte
name|op
parameter_list|)
block|{
switch|switch
condition|(
name|op
condition|)
block|{
case|case
name|AND
case|:
return|return
name|Operator
operator|.
name|AND
return|;
case|case
name|OR
case|:
return|return
name|Operator
operator|.
name|OR
return|;
case|case
name|NOT
case|:
return|return
name|Operator
operator|.
name|NOT
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

