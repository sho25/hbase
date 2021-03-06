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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
import|;
end_import

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
comment|/**  * Utilities related to atomic operations.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|AtomicUtils
block|{
specifier|private
name|AtomicUtils
parameter_list|()
block|{   }
comment|/**    * Updates a AtomicLong which is supposed to maintain the minimum values. This method is not    * synchronized but is thread-safe.    */
specifier|public
specifier|static
name|void
name|updateMin
parameter_list|(
name|AtomicLong
name|min
parameter_list|,
name|long
name|value
parameter_list|)
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|long
name|cur
init|=
name|min
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|>=
name|cur
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|min
operator|.
name|compareAndSet
argument_list|(
name|cur
argument_list|,
name|value
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
block|}
comment|/**    * Updates a AtomicLong which is supposed to maintain the maximum values. This method is not    * synchronized but is thread-safe.    */
specifier|public
specifier|static
name|void
name|updateMax
parameter_list|(
name|AtomicLong
name|max
parameter_list|,
name|long
name|value
parameter_list|)
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|long
name|cur
init|=
name|max
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|<=
name|cur
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|max
operator|.
name|compareAndSet
argument_list|(
name|cur
argument_list|,
name|value
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
end_class

end_unit

