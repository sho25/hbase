begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
operator|.
name|compactions
package|;
end_package

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
name|List
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
name|ConcurrentHashMap
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
name|RejectedExecutionHandler
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
name|ThreadPoolExecutor
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
name|atomic
operator|.
name|AtomicInteger
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
name|RemoteExceptionHandler
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|GetRegionInfoResponse
operator|.
name|CompactionState
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|HRegionServer
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
name|regionserver
operator|.
name|HStore
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
name|regionserver
operator|.
name|StoreFile
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
name|EnvironmentEdgeManager
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
name|util
operator|.
name|StringUtils
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
name|Function
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
name|Joiner
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
name|base
operator|.
name|Predicate
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
name|Collections2
import|;
end_import

begin_comment
comment|/**  * This class holds all details necessary to run a compaction.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CompactionRequest
implements|implements
name|Comparable
argument_list|<
name|CompactionRequest
argument_list|>
implements|,
name|Runnable
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CompactionRequest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HRegion
name|r
decl_stmt|;
specifier|private
specifier|final
name|HStore
name|s
decl_stmt|;
specifier|private
specifier|final
name|CompactSelection
name|compactSelection
decl_stmt|;
specifier|private
specifier|final
name|long
name|totalSize
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isMajor
decl_stmt|;
specifier|private
name|int
name|p
decl_stmt|;
specifier|private
specifier|final
name|Long
name|timeInNanos
decl_stmt|;
specifier|private
name|HRegionServer
name|server
init|=
literal|null
decl_stmt|;
comment|/**      * Map to track the number of compactions requested per region (id)      */
specifier|private
specifier|static
specifier|final
name|ConcurrentHashMap
argument_list|<
name|Long
argument_list|,
name|AtomicInteger
argument_list|>
name|majorCompactions
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|Long
argument_list|,
name|AtomicInteger
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ConcurrentHashMap
argument_list|<
name|Long
argument_list|,
name|AtomicInteger
argument_list|>
name|minorCompactions
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|Long
argument_list|,
name|AtomicInteger
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|CompactionRequest
parameter_list|(
name|HRegion
name|r
parameter_list|,
name|HStore
name|s
parameter_list|,
name|CompactSelection
name|files
parameter_list|,
name|boolean
name|isMajor
parameter_list|,
name|int
name|p
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|files
argument_list|)
expr_stmt|;
name|this
operator|.
name|r
operator|=
name|r
expr_stmt|;
name|this
operator|.
name|s
operator|=
name|s
expr_stmt|;
name|this
operator|.
name|compactSelection
operator|=
name|files
expr_stmt|;
name|long
name|sz
init|=
literal|0
decl_stmt|;
for|for
control|(
name|StoreFile
name|sf
range|:
name|files
operator|.
name|getFilesToCompact
argument_list|()
control|)
block|{
name|sz
operator|+=
name|sf
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|totalSize
operator|=
name|sz
expr_stmt|;
name|this
operator|.
name|isMajor
operator|=
name|isMajor
expr_stmt|;
name|this
operator|.
name|p
operator|=
name|p
expr_stmt|;
name|this
operator|.
name|timeInNanos
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
block|}
comment|/**      * Find out if a given region is in compaction now.      *      * @param regionId      * @return      */
specifier|public
specifier|static
name|CompactionState
name|getCompactionState
parameter_list|(
specifier|final
name|long
name|regionId
parameter_list|)
block|{
name|Long
name|key
init|=
name|Long
operator|.
name|valueOf
argument_list|(
name|regionId
argument_list|)
decl_stmt|;
name|AtomicInteger
name|major
init|=
name|majorCompactions
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|AtomicInteger
name|minor
init|=
name|minorCompactions
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|int
name|state
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|minor
operator|!=
literal|null
operator|&&
name|minor
operator|.
name|get
argument_list|()
operator|>
literal|0
condition|)
block|{
name|state
operator|+=
literal|1
expr_stmt|;
comment|// use 1 to indicate minor here
block|}
if|if
condition|(
name|major
operator|!=
literal|null
operator|&&
name|major
operator|.
name|get
argument_list|()
operator|>
literal|0
condition|)
block|{
name|state
operator|+=
literal|2
expr_stmt|;
comment|// use 2 to indicate major here
block|}
switch|switch
condition|(
name|state
condition|)
block|{
case|case
literal|3
case|:
comment|// 3 = 2 + 1, so both major and minor
return|return
name|CompactionState
operator|.
name|MAJOR_AND_MINOR
return|;
case|case
literal|2
case|:
return|return
name|CompactionState
operator|.
name|MAJOR
return|;
case|case
literal|1
case|:
return|return
name|CompactionState
operator|.
name|MINOR
return|;
default|default:
return|return
name|CompactionState
operator|.
name|NONE
return|;
block|}
block|}
specifier|public
specifier|static
name|void
name|preRequest
parameter_list|(
specifier|final
name|CompactionRequest
name|cr
parameter_list|)
block|{
name|Long
name|key
init|=
name|Long
operator|.
name|valueOf
argument_list|(
name|cr
operator|.
name|getHRegion
argument_list|()
operator|.
name|getRegionId
argument_list|()
argument_list|)
decl_stmt|;
name|ConcurrentHashMap
argument_list|<
name|Long
argument_list|,
name|AtomicInteger
argument_list|>
name|compactions
init|=
name|cr
operator|.
name|isMajor
argument_list|()
condition|?
name|majorCompactions
else|:
name|minorCompactions
decl_stmt|;
name|AtomicInteger
name|count
init|=
name|compactions
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|count
operator|==
literal|null
condition|)
block|{
name|compactions
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|count
operator|=
name|compactions
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
name|count
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|postRequest
parameter_list|(
specifier|final
name|CompactionRequest
name|cr
parameter_list|)
block|{
name|Long
name|key
init|=
name|Long
operator|.
name|valueOf
argument_list|(
name|cr
operator|.
name|getHRegion
argument_list|()
operator|.
name|getRegionId
argument_list|()
argument_list|)
decl_stmt|;
name|ConcurrentHashMap
argument_list|<
name|Long
argument_list|,
name|AtomicInteger
argument_list|>
name|compactions
init|=
name|cr
operator|.
name|isMajor
argument_list|()
condition|?
name|majorCompactions
else|:
name|minorCompactions
decl_stmt|;
name|AtomicInteger
name|count
init|=
name|compactions
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|count
operator|!=
literal|null
condition|)
block|{
name|count
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|finishRequest
parameter_list|()
block|{
name|this
operator|.
name|compactSelection
operator|.
name|finishRequest
argument_list|()
expr_stmt|;
block|}
comment|/**      * This function will define where in the priority queue the request will      * end up.  Those with the highest priorities will be first.  When the      * priorities are the same it will first compare priority then date      * to maintain a FIFO functionality.      *      *<p>Note: The date is only accurate to the millisecond which means it is      * possible that two requests were inserted into the queue within a      * millisecond.  When that is the case this function will break the tie      * arbitrarily.      */
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|CompactionRequest
name|request
parameter_list|)
block|{
comment|//NOTE: The head of the priority queue is the least element
if|if
condition|(
name|this
operator|.
name|equals
argument_list|(
name|request
argument_list|)
condition|)
block|{
return|return
literal|0
return|;
comment|//they are the same request
block|}
name|int
name|compareVal
decl_stmt|;
name|compareVal
operator|=
name|p
operator|-
name|request
operator|.
name|p
expr_stmt|;
comment|//compare priority
if|if
condition|(
name|compareVal
operator|!=
literal|0
condition|)
block|{
return|return
name|compareVal
return|;
block|}
name|compareVal
operator|=
name|timeInNanos
operator|.
name|compareTo
argument_list|(
name|request
operator|.
name|timeInNanos
argument_list|)
expr_stmt|;
if|if
condition|(
name|compareVal
operator|!=
literal|0
condition|)
block|{
return|return
name|compareVal
return|;
block|}
comment|// break the tie based on hash code
return|return
name|this
operator|.
name|hashCode
argument_list|()
operator|-
name|request
operator|.
name|hashCode
argument_list|()
return|;
block|}
comment|/** Gets the HRegion for the request */
specifier|public
name|HRegion
name|getHRegion
parameter_list|()
block|{
return|return
name|r
return|;
block|}
comment|/** Gets the Store for the request */
specifier|public
name|HStore
name|getStore
parameter_list|()
block|{
return|return
name|s
return|;
block|}
comment|/** Gets the compact selection object for the request */
specifier|public
name|CompactSelection
name|getCompactSelection
parameter_list|()
block|{
return|return
name|compactSelection
return|;
block|}
comment|/** Gets the StoreFiles for the request */
specifier|public
name|List
argument_list|<
name|StoreFile
argument_list|>
name|getFiles
parameter_list|()
block|{
return|return
name|compactSelection
operator|.
name|getFilesToCompact
argument_list|()
return|;
block|}
comment|/** Gets the total size of all StoreFiles in compaction */
specifier|public
name|long
name|getSize
parameter_list|()
block|{
return|return
name|totalSize
return|;
block|}
specifier|public
name|boolean
name|isMajor
parameter_list|()
block|{
return|return
name|this
operator|.
name|isMajor
return|;
block|}
comment|/** Gets the priority for the request */
specifier|public
name|int
name|getPriority
parameter_list|()
block|{
return|return
name|p
return|;
block|}
comment|/** Gets the priority for the request */
specifier|public
name|void
name|setPriority
parameter_list|(
name|int
name|p
parameter_list|)
block|{
name|this
operator|.
name|p
operator|=
name|p
expr_stmt|;
block|}
specifier|public
name|void
name|setServer
parameter_list|(
name|HRegionServer
name|hrs
parameter_list|)
block|{
name|this
operator|.
name|server
operator|=
name|hrs
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|fsList
init|=
name|Joiner
operator|.
name|on
argument_list|(
literal|", "
argument_list|)
operator|.
name|join
argument_list|(
name|Collections2
operator|.
name|transform
argument_list|(
name|Collections2
operator|.
name|filter
argument_list|(
name|compactSelection
operator|.
name|getFilesToCompact
argument_list|()
argument_list|,
operator|new
name|Predicate
argument_list|<
name|StoreFile
argument_list|>
argument_list|()
block|{
specifier|public
name|boolean
name|apply
parameter_list|(
name|StoreFile
name|sf
parameter_list|)
block|{
return|return
name|sf
operator|.
name|getReader
argument_list|()
operator|!=
literal|null
return|;
block|}
block|}
argument_list|)
argument_list|,
operator|new
name|Function
argument_list|<
name|StoreFile
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|apply
parameter_list|(
name|StoreFile
name|sf
parameter_list|)
block|{
return|return
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|sf
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
decl_stmt|;
return|return
literal|"regionName="
operator|+
name|r
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|", storeName="
operator|+
operator|new
name|String
argument_list|(
name|s
operator|.
name|getFamily
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|+
literal|", fileCount="
operator|+
name|compactSelection
operator|.
name|getFilesToCompact
argument_list|()
operator|.
name|size
argument_list|()
operator|+
literal|", fileSize="
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|totalSize
argument_list|)
operator|+
operator|(
operator|(
name|fsList
operator|.
name|isEmpty
argument_list|()
operator|)
condition|?
literal|""
else|:
literal|" ("
operator|+
name|fsList
operator|+
literal|")"
operator|)
operator|+
literal|", priority="
operator|+
name|p
operator|+
literal|", time="
operator|+
name|timeInNanos
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|server
argument_list|)
expr_stmt|;
if|if
condition|(
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
return|return;
block|}
try|try
block|{
name|long
name|start
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|boolean
name|completed
init|=
name|r
operator|.
name|compact
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
operator|(
operator|(
name|completed
operator|)
condition|?
literal|"completed"
else|:
literal|"aborted"
operator|)
operator|+
literal|" compaction: "
operator|+
name|this
operator|+
literal|"; duration="
operator|+
name|StringUtils
operator|.
name|formatTimeDiff
argument_list|(
name|now
argument_list|,
name|start
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|completed
condition|)
block|{
comment|// degenerate case: blocked regions require recursive enqueues
if|if
condition|(
name|s
operator|.
name|getCompactPriority
argument_list|()
operator|<=
literal|0
condition|)
block|{
name|server
operator|.
name|compactSplitThread
operator|.
name|requestCompaction
argument_list|(
name|r
argument_list|,
name|s
argument_list|,
literal|"Recursive enqueue"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// see if the compaction has caused us to exceed max region size
name|server
operator|.
name|compactSplitThread
operator|.
name|requestSplit
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Compaction failed "
operator|+
name|this
argument_list|,
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|ex
argument_list|)
argument_list|)
expr_stmt|;
name|server
operator|.
name|checkFileSystem
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Compaction failed "
operator|+
name|this
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|server
operator|.
name|checkFileSystem
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|s
operator|.
name|finishRequest
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"CompactSplitThread status: "
operator|+
name|server
operator|.
name|compactSplitThread
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Cleanup class to use when rejecting a compaction request from the queue.      */
specifier|public
specifier|static
class|class
name|Rejection
implements|implements
name|RejectedExecutionHandler
block|{
annotation|@
name|Override
specifier|public
name|void
name|rejectedExecution
parameter_list|(
name|Runnable
name|request
parameter_list|,
name|ThreadPoolExecutor
name|pool
parameter_list|)
block|{
if|if
condition|(
name|request
operator|instanceof
name|CompactionRequest
condition|)
block|{
name|CompactionRequest
name|cr
init|=
operator|(
name|CompactionRequest
operator|)
name|request
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Compaction Rejected: "
operator|+
name|cr
argument_list|)
expr_stmt|;
name|cr
operator|.
name|getStore
argument_list|()
operator|.
name|finishRequest
argument_list|(
name|cr
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

