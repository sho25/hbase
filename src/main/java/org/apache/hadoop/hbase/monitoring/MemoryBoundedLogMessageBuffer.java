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
name|monitoring
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|SimpleDateFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Charsets
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * A size-bounded repository of alerts, which are kept  * in a linked list. Alerts can be added, and they will  * automatically be removed one by one when the specified heap  * usage is exhausted.  */
end_comment

begin_class
specifier|public
class|class
name|MemoryBoundedLogMessageBuffer
block|{
specifier|private
specifier|final
name|long
name|maxSizeBytes
decl_stmt|;
specifier|private
name|long
name|usage
init|=
literal|0
decl_stmt|;
specifier|private
name|LinkedList
argument_list|<
name|LogMessage
argument_list|>
name|messages
decl_stmt|;
specifier|public
name|MemoryBoundedLogMessageBuffer
parameter_list|(
name|long
name|maxSizeBytes
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|maxSizeBytes
operator|>
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxSizeBytes
operator|=
name|maxSizeBytes
expr_stmt|;
name|this
operator|.
name|messages
operator|=
name|Lists
operator|.
name|newLinkedList
argument_list|()
expr_stmt|;
block|}
comment|/**    * Append the given message to this buffer, automatically evicting    * older messages until the desired memory limit is achieved.    */
specifier|public
specifier|synchronized
name|void
name|add
parameter_list|(
name|String
name|messageText
parameter_list|)
block|{
name|LogMessage
name|message
init|=
operator|new
name|LogMessage
argument_list|(
name|messageText
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|usage
operator|+=
name|message
operator|.
name|estimateHeapUsage
argument_list|()
expr_stmt|;
name|messages
operator|.
name|add
argument_list|(
name|message
argument_list|)
expr_stmt|;
while|while
condition|(
name|usage
operator|>
name|maxSizeBytes
condition|)
block|{
name|LogMessage
name|removed
init|=
name|messages
operator|.
name|remove
argument_list|()
decl_stmt|;
name|usage
operator|-=
name|removed
operator|.
name|estimateHeapUsage
argument_list|()
expr_stmt|;
assert|assert
name|usage
operator|>=
literal|0
assert|;
block|}
block|}
comment|/**    * Dump the contents of the buffer to the given stream.    */
specifier|public
specifier|synchronized
name|void
name|dumpTo
parameter_list|(
name|PrintWriter
name|out
parameter_list|)
block|{
name|SimpleDateFormat
name|df
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd'T'HH:mm:ss"
argument_list|)
decl_stmt|;
for|for
control|(
name|LogMessage
name|msg
range|:
name|messages
control|)
block|{
name|out
operator|.
name|write
argument_list|(
name|df
operator|.
name|format
argument_list|(
operator|new
name|Date
argument_list|(
name|msg
operator|.
name|timestamp
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
operator|new
name|String
argument_list|(
name|msg
operator|.
name|message
argument_list|,
name|Charsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|synchronized
name|List
argument_list|<
name|LogMessage
argument_list|>
name|getMessages
parameter_list|()
block|{
comment|// defensive copy
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
name|messages
argument_list|)
return|;
block|}
comment|/**    * Estimate the number of bytes this buffer is currently    * using.    */
specifier|synchronized
name|long
name|estimateHeapUsage
parameter_list|()
block|{
return|return
name|usage
return|;
block|}
specifier|private
specifier|static
class|class
name|LogMessage
block|{
comment|/** the error text, encoded in bytes to save memory */
specifier|public
specifier|final
name|byte
index|[]
name|message
decl_stmt|;
specifier|public
specifier|final
name|long
name|timestamp
decl_stmt|;
comment|/**      * Completely non-scientific estimate of how much one of these      * objects takes, along with the LinkedList overhead. This doesn't      * need to be exact, since we don't expect a ton of these alerts.      */
specifier|private
specifier|static
specifier|final
name|long
name|BASE_USAGE
init|=
literal|100
decl_stmt|;
specifier|public
name|LogMessage
parameter_list|(
name|String
name|message
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|message
operator|=
name|message
operator|.
name|getBytes
argument_list|(
name|Charsets
operator|.
name|UTF_8
argument_list|)
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
block|}
specifier|public
name|long
name|estimateHeapUsage
parameter_list|()
block|{
return|return
name|message
operator|.
name|length
operator|+
name|BASE_USAGE
return|;
block|}
block|}
block|}
end_class

end_unit

