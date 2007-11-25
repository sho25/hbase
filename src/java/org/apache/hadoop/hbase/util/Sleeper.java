begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|AtomicBoolean
import|;
end_import

begin_comment
comment|/**  * Sleeper for current thread.  * Sleeps for passed period.  Also checks passed boolean and if interrupted,  * will return if the flag is set (rather than go back to sleep until its   * sleep time is up).  */
end_comment

begin_class
specifier|public
class|class
name|Sleeper
block|{
specifier|private
specifier|final
name|int
name|period
decl_stmt|;
specifier|private
name|AtomicBoolean
name|stop
decl_stmt|;
comment|/**    * @param sleep    * @param stop    */
specifier|public
name|Sleeper
parameter_list|(
specifier|final
name|int
name|sleep
parameter_list|,
specifier|final
name|AtomicBoolean
name|stop
parameter_list|)
block|{
name|this
operator|.
name|period
operator|=
name|sleep
expr_stmt|;
name|this
operator|.
name|stop
operator|=
name|stop
expr_stmt|;
block|}
comment|/**    * Sleep for period.    */
specifier|public
name|void
name|sleep
parameter_list|()
block|{
name|sleep
argument_list|(
name|period
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sleep for period adjusted by passed<code>startTime<code>    * @param startTime Time some task started previous to now.  Time to sleep    * will be docked current time minus passed<code>startTime<code>.    */
specifier|public
name|void
name|sleep
parameter_list|(
specifier|final
name|long
name|startTime
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
return|return;
block|}
name|long
name|waitTime
init|=
name|this
operator|.
name|period
operator|-
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
decl_stmt|;
if|if
condition|(
name|waitTime
operator|>
literal|0
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|waitTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|iex
parameter_list|)
block|{
comment|// We we interrupted because we're meant to stop?  If not, just
comment|// continue ignoring the interruption
if|if
condition|(
name|this
operator|.
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
return|return;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

