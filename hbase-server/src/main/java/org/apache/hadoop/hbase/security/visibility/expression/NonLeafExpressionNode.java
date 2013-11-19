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
operator|.
name|expression
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|NonLeafExpressionNode
implements|implements
name|ExpressionNode
block|{
specifier|private
name|Operator
name|op
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ExpressionNode
argument_list|>
name|childExps
init|=
operator|new
name|ArrayList
argument_list|<
name|ExpressionNode
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
specifier|public
name|NonLeafExpressionNode
parameter_list|()
block|{    }
specifier|public
name|NonLeafExpressionNode
parameter_list|(
name|Operator
name|op
parameter_list|)
block|{
name|this
operator|.
name|op
operator|=
name|op
expr_stmt|;
block|}
specifier|public
name|NonLeafExpressionNode
parameter_list|(
name|Operator
name|op
parameter_list|,
name|List
argument_list|<
name|ExpressionNode
argument_list|>
name|exps
parameter_list|)
block|{
name|this
operator|.
name|op
operator|=
name|op
expr_stmt|;
if|if
condition|(
name|op
operator|==
name|Operator
operator|.
name|NOT
operator|&&
name|exps
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|Operator
operator|.
name|NOT
operator|+
literal|" should be on 1 child expression"
argument_list|)
throw|;
block|}
name|this
operator|.
name|childExps
operator|=
name|exps
expr_stmt|;
block|}
specifier|public
name|NonLeafExpressionNode
parameter_list|(
name|Operator
name|op
parameter_list|,
name|ExpressionNode
modifier|...
name|exps
parameter_list|)
block|{
name|this
operator|.
name|op
operator|=
name|op
expr_stmt|;
name|List
argument_list|<
name|ExpressionNode
argument_list|>
name|expLst
init|=
operator|new
name|ArrayList
argument_list|<
name|ExpressionNode
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ExpressionNode
name|exp
range|:
name|exps
control|)
block|{
name|expLst
operator|.
name|add
argument_list|(
name|exp
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|childExps
operator|=
name|expLst
expr_stmt|;
block|}
specifier|public
name|Operator
name|getOperator
parameter_list|()
block|{
return|return
name|op
return|;
block|}
specifier|public
name|List
argument_list|<
name|ExpressionNode
argument_list|>
name|getChildExps
parameter_list|()
block|{
return|return
name|childExps
return|;
block|}
specifier|public
name|void
name|addChildExp
parameter_list|(
name|ExpressionNode
name|exp
parameter_list|)
block|{
if|if
condition|(
name|op
operator|==
name|Operator
operator|.
name|NOT
operator|&&
name|this
operator|.
name|childExps
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|Operator
operator|.
name|NOT
operator|+
literal|" should be on 1 child expression"
argument_list|)
throw|;
block|}
name|this
operator|.
name|childExps
operator|.
name|add
argument_list|(
name|exp
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addChildExps
parameter_list|(
name|List
argument_list|<
name|ExpressionNode
argument_list|>
name|exps
parameter_list|)
block|{
name|this
operator|.
name|childExps
operator|.
name|addAll
argument_list|(
name|exps
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"("
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|op
operator|==
name|Operator
operator|.
name|NOT
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|op
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|this
operator|.
name|childExps
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|childExps
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|<
name|this
operator|.
name|childExps
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" "
operator|+
name|this
operator|.
name|op
operator|+
literal|" "
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSingleNode
parameter_list|()
block|{
return|return
name|this
operator|.
name|op
operator|==
name|Operator
operator|.
name|NOT
return|;
block|}
specifier|public
name|NonLeafExpressionNode
name|deepClone
parameter_list|()
block|{
name|NonLeafExpressionNode
name|clone
init|=
operator|new
name|NonLeafExpressionNode
argument_list|(
name|this
operator|.
name|op
argument_list|)
decl_stmt|;
for|for
control|(
name|ExpressionNode
name|exp
range|:
name|this
operator|.
name|childExps
control|)
block|{
name|clone
operator|.
name|addChildExp
argument_list|(
name|exp
operator|.
name|deepClone
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|clone
return|;
block|}
block|}
end_class

end_unit

