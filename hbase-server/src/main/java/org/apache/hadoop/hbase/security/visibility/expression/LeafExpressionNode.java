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
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
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
name|LeafExpressionNode
implements|implements
name|ExpressionNode
block|{
specifier|public
specifier|static
specifier|final
name|LeafExpressionNode
name|OPEN_PARAN_NODE
init|=
operator|new
name|LeafExpressionNode
argument_list|(
literal|"("
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|LeafExpressionNode
name|CLOSE_PARAN_NODE
init|=
operator|new
name|LeafExpressionNode
argument_list|(
literal|")"
argument_list|)
decl_stmt|;
specifier|private
name|String
name|identifier
decl_stmt|;
specifier|public
name|LeafExpressionNode
parameter_list|(
name|String
name|identifier
parameter_list|)
block|{
name|this
operator|.
name|identifier
operator|=
name|identifier
expr_stmt|;
block|}
specifier|public
name|String
name|getIdentifier
parameter_list|()
block|{
return|return
name|this
operator|.
name|identifier
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|this
operator|.
name|identifier
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|instanceof
name|LeafExpressionNode
condition|)
block|{
name|LeafExpressionNode
name|that
init|=
operator|(
name|LeafExpressionNode
operator|)
name|obj
decl_stmt|;
return|return
name|this
operator|.
name|identifier
operator|.
name|equals
argument_list|(
name|that
operator|.
name|identifier
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|identifier
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
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|LeafExpressionNode
name|deepClone
parameter_list|()
block|{
name|LeafExpressionNode
name|clone
init|=
operator|new
name|LeafExpressionNode
argument_list|(
name|this
operator|.
name|identifier
argument_list|)
decl_stmt|;
return|return
name|clone
return|;
block|}
block|}
end_class

end_unit

