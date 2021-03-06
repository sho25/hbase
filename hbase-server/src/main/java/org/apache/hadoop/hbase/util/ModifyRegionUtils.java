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
name|util
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
name|io
operator|.
name|InterruptedIOException
import|;
end_import

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
name|Collection
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
name|Callable
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
name|CompletionService
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
name|ExecutionException
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
name|ExecutorCompletionService
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
name|TimeUnit
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
name|HConstants
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
name|client
operator|.
name|RegionInfo
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
name|client
operator|.
name|RegionInfoBuilder
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
name|client
operator|.
name|TableDescriptor
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Utility methods for interacting with the regions.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|ModifyRegionUtils
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ModifyRegionUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ModifyRegionUtils
parameter_list|()
block|{   }
specifier|public
interface|interface
name|RegionFillTask
block|{
name|void
name|fillRegion
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|public
interface|interface
name|RegionEditTask
block|{
name|void
name|editRegion
parameter_list|(
specifier|final
name|RegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|public
specifier|static
name|RegionInfo
index|[]
name|createRegionInfos
parameter_list|(
name|TableDescriptor
name|tableDescriptor
parameter_list|,
name|byte
index|[]
index|[]
name|splitKeys
parameter_list|)
block|{
name|long
name|regionId
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|RegionInfo
index|[]
name|hRegionInfos
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|splitKeys
operator|==
literal|null
operator|||
name|splitKeys
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|hRegionInfos
operator|=
operator|new
name|RegionInfo
index|[]
block|{
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|setStartKey
argument_list|(
literal|null
argument_list|)
operator|.
name|setEndKey
argument_list|(
literal|null
argument_list|)
operator|.
name|setSplit
argument_list|(
literal|false
argument_list|)
operator|.
name|setRegionId
argument_list|(
name|regionId
argument_list|)
operator|.
name|build
argument_list|()
block|}
expr_stmt|;
block|}
else|else
block|{
name|int
name|numRegions
init|=
name|splitKeys
operator|.
name|length
operator|+
literal|1
decl_stmt|;
name|hRegionInfos
operator|=
operator|new
name|RegionInfo
index|[
name|numRegions
index|]
expr_stmt|;
name|byte
index|[]
name|startKey
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|endKey
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numRegions
condition|;
name|i
operator|++
control|)
block|{
name|endKey
operator|=
operator|(
name|i
operator|==
name|splitKeys
operator|.
name|length
operator|)
condition|?
literal|null
else|:
name|splitKeys
index|[
name|i
index|]
expr_stmt|;
name|hRegionInfos
index|[
name|i
index|]
operator|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|startKey
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|endKey
argument_list|)
operator|.
name|setSplit
argument_list|(
literal|false
argument_list|)
operator|.
name|setRegionId
argument_list|(
name|regionId
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|startKey
operator|=
name|endKey
expr_stmt|;
block|}
block|}
return|return
name|hRegionInfos
return|;
block|}
comment|/**    * Create new set of regions on the specified file-system.    * NOTE: that you should add the regions to hbase:meta after this operation.    *    * @param conf {@link Configuration}    * @param rootDir Root directory for HBase instance    * @param tableDescriptor description of the table    * @param newRegions {@link RegionInfo} that describes the regions to create    * @param task {@link RegionFillTask} custom code to populate region after creation    * @throws IOException    */
specifier|public
specifier|static
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|createRegions
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|TableDescriptor
name|tableDescriptor
parameter_list|,
specifier|final
name|RegionInfo
index|[]
name|newRegions
parameter_list|,
specifier|final
name|RegionFillTask
name|task
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|newRegions
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|int
name|regionNumber
init|=
name|newRegions
operator|.
name|length
decl_stmt|;
name|ThreadPoolExecutor
name|exec
init|=
name|getRegionOpenAndInitThreadPool
argument_list|(
name|conf
argument_list|,
literal|"RegionOpenAndInit-"
operator|+
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|,
name|regionNumber
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|createRegions
argument_list|(
name|exec
argument_list|,
name|conf
argument_list|,
name|rootDir
argument_list|,
name|tableDescriptor
argument_list|,
name|newRegions
argument_list|,
name|task
argument_list|)
return|;
block|}
finally|finally
block|{
name|exec
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Create new set of regions on the specified file-system.    * NOTE: that you should add the regions to hbase:meta after this operation.    *    * @param exec Thread Pool Executor    * @param conf {@link Configuration}    * @param rootDir Root directory for HBase instance    * @param tableDescriptor description of the table    * @param newRegions {@link RegionInfo} that describes the regions to create    * @param task {@link RegionFillTask} custom code to populate region after creation    * @throws IOException    */
specifier|public
specifier|static
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|createRegions
parameter_list|(
specifier|final
name|ThreadPoolExecutor
name|exec
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|TableDescriptor
name|tableDescriptor
parameter_list|,
specifier|final
name|RegionInfo
index|[]
name|newRegions
parameter_list|,
specifier|final
name|RegionFillTask
name|task
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|newRegions
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|int
name|regionNumber
init|=
name|newRegions
operator|.
name|length
decl_stmt|;
name|CompletionService
argument_list|<
name|RegionInfo
argument_list|>
name|completionService
init|=
operator|new
name|ExecutorCompletionService
argument_list|<>
argument_list|(
name|exec
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionInfos
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|RegionInfo
name|newRegion
range|:
name|newRegions
control|)
block|{
name|completionService
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|RegionInfo
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|RegionInfo
name|call
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|createRegion
argument_list|(
name|conf
argument_list|,
name|rootDir
argument_list|,
name|tableDescriptor
argument_list|,
name|newRegion
argument_list|,
name|task
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
try|try
block|{
comment|// wait for all regions to finish creation
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|regionNumber
condition|;
name|i
operator|++
control|)
block|{
name|regionInfos
operator|.
name|add
argument_list|(
name|completionService
operator|.
name|take
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught "
operator|+
name|e
operator|+
literal|" during region creation"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|regionInfos
return|;
block|}
comment|/**    * Create new set of regions on the specified file-system.    * @param conf {@link Configuration}    * @param rootDir Root directory for HBase instance    * @param tableDescriptor description of the table    * @param newRegion {@link RegionInfo} that describes the region to create    * @param task {@link RegionFillTask} custom code to populate region after creation    * @throws IOException    */
specifier|public
specifier|static
name|RegionInfo
name|createRegion
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|TableDescriptor
name|tableDescriptor
parameter_list|,
specifier|final
name|RegionInfo
name|newRegion
parameter_list|,
specifier|final
name|RegionFillTask
name|task
parameter_list|)
throws|throws
name|IOException
block|{
comment|// 1. Create HRegion
comment|// The WAL subsystem will use the default rootDir rather than the passed in rootDir
comment|// unless I pass along via the conf.
name|Configuration
name|confForWAL
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|confForWAL
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|,
name|rootDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|HRegion
name|region
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|newRegion
argument_list|,
name|rootDir
argument_list|,
name|conf
argument_list|,
name|tableDescriptor
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
try|try
block|{
comment|// 2. Custom user code to interact with the created region
if|if
condition|(
name|task
operator|!=
literal|null
condition|)
block|{
name|task
operator|.
name|fillRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
comment|// 3. Close the new region to flush to disk. Close log file too.
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|region
operator|.
name|getRegionInfo
argument_list|()
return|;
block|}
comment|/**    * Execute the task on the specified set of regions.    *    * @param exec Thread Pool Executor    * @param regions {@link RegionInfo} that describes the regions to edit    * @param task {@link RegionFillTask} custom code to edit the region    * @throws IOException    */
specifier|public
specifier|static
name|void
name|editRegions
parameter_list|(
specifier|final
name|ThreadPoolExecutor
name|exec
parameter_list|,
specifier|final
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
name|regions
parameter_list|,
specifier|final
name|RegionEditTask
name|task
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|ExecutorCompletionService
argument_list|<
name|Void
argument_list|>
name|completionService
init|=
operator|new
name|ExecutorCompletionService
argument_list|<>
argument_list|(
name|exec
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|RegionInfo
name|hri
range|:
name|regions
control|)
block|{
name|completionService
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|IOException
block|{
name|task
operator|.
name|editRegion
argument_list|(
name|hri
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
try|try
block|{
for|for
control|(
name|RegionInfo
name|hri
range|:
name|regions
control|)
block|{
name|completionService
operator|.
name|take
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/*    * used by createRegions() to get the thread pool executor based on the    * "hbase.hregion.open.and.init.threads.max" property.    */
specifier|static
name|ThreadPoolExecutor
name|getRegionOpenAndInitThreadPool
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|String
name|threadNamePrefix
parameter_list|,
name|int
name|regionNumber
parameter_list|)
block|{
name|int
name|maxThreads
init|=
name|Math
operator|.
name|min
argument_list|(
name|regionNumber
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.hregion.open.and.init.threads.max"
argument_list|,
literal|16
argument_list|)
argument_list|)
decl_stmt|;
name|ThreadPoolExecutor
name|regionOpenAndInitThreadPool
init|=
name|Threads
operator|.
name|getBoundedCachedThreadPool
argument_list|(
name|maxThreads
argument_list|,
literal|30L
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
name|threadNamePrefix
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|regionOpenAndInitThreadPool
return|;
block|}
block|}
end_class

end_unit

