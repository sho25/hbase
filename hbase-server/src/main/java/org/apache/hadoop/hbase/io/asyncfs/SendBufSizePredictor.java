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
name|io
operator|.
name|asyncfs
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

begin_comment
comment|/**  * Used to predict the next send buffer size.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|SendBufSizePredictor
block|{
comment|// LIMIT is 128MB
specifier|private
specifier|static
specifier|final
name|int
name|LIMIT
init|=
literal|128
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
comment|// buf's initial capacity - 4KB
specifier|private
name|int
name|capacity
init|=
literal|4
operator|*
literal|1024
decl_stmt|;
name|int
name|initialSize
parameter_list|()
block|{
return|return
name|capacity
return|;
block|}
name|int
name|guess
parameter_list|(
name|int
name|bytesWritten
parameter_list|)
block|{
comment|// if the bytesWritten is greater than the current capacity
comment|// always increase the capacity in powers of 2.
if|if
condition|(
name|bytesWritten
operator|>
name|this
operator|.
name|capacity
condition|)
block|{
comment|// Ensure we don't cross the LIMIT
if|if
condition|(
operator|(
name|this
operator|.
name|capacity
operator|<<
literal|1
operator|)
operator|<=
name|LIMIT
condition|)
block|{
comment|// increase the capacity in the range of power of 2
name|this
operator|.
name|capacity
operator|=
name|this
operator|.
name|capacity
operator|<<
literal|1
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// if we see that the bytesWritten is lesser we could again decrease
comment|// the capacity by dividing it by 2 if the bytesWritten is satisfied by
comment|// that reduction
if|if
condition|(
operator|(
name|this
operator|.
name|capacity
operator|>>
literal|1
operator|)
operator|>=
name|bytesWritten
condition|)
block|{
name|this
operator|.
name|capacity
operator|=
name|this
operator|.
name|capacity
operator|>>
literal|1
expr_stmt|;
block|}
block|}
return|return
name|this
operator|.
name|capacity
return|;
block|}
block|}
end_class

end_unit

