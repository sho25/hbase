begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|filter
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|InterfaceStability
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
name|KeyValue
import|;
end_import

begin_comment
comment|/**  * A filter that includes rows based on a chance.  *   */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|RandomRowFilter
extends|extends
name|FilterBase
block|{
specifier|protected
specifier|static
specifier|final
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|protected
name|float
name|chance
decl_stmt|;
specifier|protected
name|boolean
name|filterOutRow
decl_stmt|;
comment|/**    * Writable constructor, do not use.    */
specifier|public
name|RandomRowFilter
parameter_list|()
block|{   }
comment|/**    * Create a new filter with a specified chance for a row to be included.    *     * @param chance    */
specifier|public
name|RandomRowFilter
parameter_list|(
name|float
name|chance
parameter_list|)
block|{
name|this
operator|.
name|chance
operator|=
name|chance
expr_stmt|;
block|}
comment|/**    * @return The chance that a row gets included.    */
specifier|public
name|float
name|getChance
parameter_list|()
block|{
return|return
name|chance
return|;
block|}
comment|/**    * Set the chance that a row is included.    *     * @param chance    */
specifier|public
name|void
name|setChance
parameter_list|(
name|float
name|chance
parameter_list|)
block|{
name|this
operator|.
name|chance
operator|=
name|chance
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|KeyValue
name|v
parameter_list|)
block|{
if|if
condition|(
name|filterOutRow
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRow
parameter_list|()
block|{
return|return
name|filterOutRow
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|chance
operator|<
literal|0
condition|)
block|{
comment|// with a zero chance, the rows is always excluded
name|filterOutRow
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|chance
operator|>
literal|1
condition|)
block|{
comment|// always included
name|filterOutRow
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
comment|// roll the dice
name|filterOutRow
operator|=
operator|!
operator|(
name|random
operator|.
name|nextFloat
argument_list|()
operator|<
name|chance
operator|)
expr_stmt|;
block|}
return|return
name|filterOutRow
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|filterOutRow
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|chance
operator|=
name|in
operator|.
name|readFloat
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeFloat
argument_list|(
name|chance
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

