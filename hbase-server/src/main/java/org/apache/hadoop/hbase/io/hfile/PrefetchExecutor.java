begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hfile
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentSkipListMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|RejectedExecutionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ScheduledExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ScheduledThreadPoolExecutor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configuration
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
name|fs
operator|.
name|Path
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
name|HBaseConfiguration
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
name|HConstants
import|;
end_import

begin_class
specifier|public
class|class
name|PrefetchExecutor
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|PrefetchExecutor
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Futures for tracking block prefetch activity */
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|Path
argument_list|,
name|Future
argument_list|<
name|?
argument_list|>
argument_list|>
name|prefetchFutures
init|=
operator|new
name|ConcurrentSkipListMap
argument_list|<
name|Path
argument_list|,
name|Future
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|/** Executor pool shared among all HFiles for block prefetch */
specifier|private
specifier|static
specifier|final
name|ScheduledExecutorService
name|prefetchExecutorPool
decl_stmt|;
comment|/** Delay before beginning prefetch */
specifier|private
specifier|static
specifier|final
name|int
name|prefetchDelayMillis
decl_stmt|;
comment|/** Variation in prefetch delay times, to mitigate stampedes */
specifier|private
specifier|static
specifier|final
name|float
name|prefetchDelayVariation
decl_stmt|;
static|static
block|{
comment|// Consider doing this on demand with a configuration passed in rather
comment|// than in a static initializer.
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// 1s here for tests, consider 30s in hbase-default.xml
comment|// Set to 0 for no delay
name|prefetchDelayMillis
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.hfile.prefetch.delay"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|prefetchDelayVariation
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
literal|"hbase.hfile.prefetch.delay.variation"
argument_list|,
literal|0.2f
argument_list|)
expr_stmt|;
name|int
name|prefetchThreads
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.hfile.thread.prefetch"
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|prefetchExecutorPool
operator|=
operator|new
name|ScheduledThreadPoolExecutor
argument_list|(
name|prefetchThreads
argument_list|,
operator|new
name|ThreadFactory
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Thread
name|newThread
parameter_list|(
name|Runnable
name|r
parameter_list|)
block|{
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|t
operator|.
name|setName
argument_list|(
literal|"hfile-prefetch-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|Random
name|RNG
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
comment|// TODO: We want HFile, which is where the blockcache lives, to handle
comment|// prefetching of file blocks but the Store level is where path convention
comment|// knowledge should be contained
specifier|private
specifier|static
specifier|final
name|Pattern
name|prefetchPathExclude
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"("
operator|+
name|Path
operator|.
name|SEPARATOR_CHAR
operator|+
name|HConstants
operator|.
name|HBASE_TEMP_DIRECTORY
operator|.
name|replace
argument_list|(
literal|"."
argument_list|,
literal|"\\."
argument_list|)
operator|+
name|Path
operator|.
name|SEPARATOR_CHAR
operator|+
literal|")|("
operator|+
name|Path
operator|.
name|SEPARATOR_CHAR
operator|+
name|HConstants
operator|.
name|HREGION_COMPACTIONDIR_NAME
operator|.
name|replace
argument_list|(
literal|"."
argument_list|,
literal|"\\."
argument_list|)
operator|+
name|Path
operator|.
name|SEPARATOR_CHAR
operator|+
literal|")"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|void
name|request
parameter_list|(
name|Path
name|path
parameter_list|,
name|Runnable
name|runnable
parameter_list|)
block|{
if|if
condition|(
operator|!
name|prefetchPathExclude
operator|.
name|matcher
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|find
argument_list|()
condition|)
block|{
name|long
name|delay
decl_stmt|;
if|if
condition|(
name|prefetchDelayMillis
operator|>
literal|0
condition|)
block|{
name|delay
operator|=
call|(
name|long
call|)
argument_list|(
operator|(
name|prefetchDelayMillis
operator|*
operator|(
literal|1.0f
operator|-
operator|(
name|prefetchDelayVariation
operator|/
literal|2
operator|)
operator|)
operator|)
operator|+
operator|(
name|prefetchDelayMillis
operator|*
operator|(
name|prefetchDelayVariation
operator|/
literal|2
operator|)
operator|*
name|RNG
operator|.
name|nextFloat
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|delay
operator|=
literal|0
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Prefetch requested for "
operator|+
name|path
operator|+
literal|", delay="
operator|+
name|delay
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
block|}
name|prefetchFutures
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|prefetchExecutorPool
operator|.
name|schedule
argument_list|(
name|runnable
argument_list|,
name|delay
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RejectedExecutionException
name|e
parameter_list|)
block|{
name|prefetchFutures
operator|.
name|remove
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Prefetch request rejected for "
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|void
name|complete
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|prefetchFutures
operator|.
name|remove
argument_list|(
name|path
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Prefetch completed for "
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|cancel
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|Future
argument_list|<
name|?
argument_list|>
name|future
init|=
name|prefetchFutures
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|future
operator|!=
literal|null
condition|)
block|{
comment|// ok to race with other cancellation attempts
name|future
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|prefetchFutures
operator|.
name|remove
argument_list|(
name|path
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Prefetch cancelled for "
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|boolean
name|isCompleted
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|Future
argument_list|<
name|?
argument_list|>
name|future
init|=
name|prefetchFutures
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|future
operator|!=
literal|null
condition|)
block|{
return|return
name|future
operator|.
name|isDone
argument_list|()
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

