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
name|client
package|;
end_package

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
name|Iterator
import|;
end_import

begin_comment
comment|/**  * Helper class for custom client scanners.  */
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
specifier|abstract
class|class
name|AbstractClientScanner
implements|implements
name|ResultScanner
block|{
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Result
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|Iterator
argument_list|<
name|Result
argument_list|>
argument_list|()
block|{
comment|// The next RowResult, possibly pre-read
name|Result
name|next
init|=
literal|null
decl_stmt|;
comment|// return true if there is another item pending, false if there isn't.
comment|// this method is where the actual advancing takes place, but you need
comment|// to call next() to consume it. hasNext() will only advance if there
comment|// isn't a pending next().
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
if|if
condition|(
name|next
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|next
operator|=
name|AbstractClientScanner
operator|.
name|this
operator|.
name|next
argument_list|()
expr_stmt|;
return|return
name|next
operator|!=
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|// get the pending next item and advance the iterator. returns null if
comment|// there is no next item.
specifier|public
name|Result
name|next
parameter_list|()
block|{
comment|// since hasNext() does the real advancing, we call this to determine
comment|// if there is a next before proceeding.
if|if
condition|(
operator|!
name|hasNext
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// if we get to here, then hasNext() has given us an item to return.
comment|// we want to return the item and then null out the next pointer, so
comment|// we use a temporary variable.
name|Result
name|temp
init|=
name|next
decl_stmt|;
name|next
operator|=
literal|null
expr_stmt|;
return|return
name|temp
return|;
block|}
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

