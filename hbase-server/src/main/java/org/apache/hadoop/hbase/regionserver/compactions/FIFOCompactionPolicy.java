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
name|hbase
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
name|regionserver
operator|.
name|StoreConfigInformation
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
name|regionserver
operator|.
name|StoreUtils
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

begin_comment
comment|/**  *   * FIFO compaction policy selects only files which have all cells expired.   * The column family MUST have non-default TTL. One of the use cases for this   * policy is when we need to store raw data which will be post-processed later   * and discarded completely after quite short period of time. Raw time-series vs.   * time-based roll up aggregates and compacted time-series. We collect raw time-series  * and store them into CF with FIFO compaction policy, periodically we run task   * which creates roll up aggregates and compacts time-series, the original raw data   * can be discarded after that.  *   */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FIFOCompactionPolicy
extends|extends
name|ExploringCompactionPolicy
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
name|FIFOCompactionPolicy
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|FIFOCompactionPolicy
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|StoreConfigInformation
name|storeConfigInfo
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|storeConfigInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|CompactionRequest
name|selectCompaction
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|candidateFiles
parameter_list|,
name|List
argument_list|<
name|StoreFile
argument_list|>
name|filesCompacting
parameter_list|,
name|boolean
name|isUserCompaction
parameter_list|,
name|boolean
name|mayUseOffPeak
parameter_list|,
name|boolean
name|forceMajor
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|forceMajor
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Major compaction is not supported for FIFO compaction policy. Ignore the flag."
argument_list|)
expr_stmt|;
block|}
name|boolean
name|isAfterSplit
init|=
name|StoreUtils
operator|.
name|hasReferences
argument_list|(
name|candidateFiles
argument_list|)
decl_stmt|;
if|if
condition|(
name|isAfterSplit
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Split detected, delegate selection to the parent policy."
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|selectCompaction
argument_list|(
name|candidateFiles
argument_list|,
name|filesCompacting
argument_list|,
name|isUserCompaction
argument_list|,
name|mayUseOffPeak
argument_list|,
name|forceMajor
argument_list|)
return|;
block|}
comment|// Nothing to compact
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|toCompact
init|=
name|getExpiredStores
argument_list|(
name|candidateFiles
argument_list|,
name|filesCompacting
argument_list|)
decl_stmt|;
name|CompactionRequest
name|result
init|=
operator|new
name|CompactionRequest
argument_list|(
name|toCompact
argument_list|)
decl_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldPerformMajorCompaction
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|isAfterSplit
init|=
name|StoreUtils
operator|.
name|hasReferences
argument_list|(
name|filesToCompact
argument_list|)
decl_stmt|;
if|if
condition|(
name|isAfterSplit
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Split detected, delegate to the parent policy."
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|shouldPerformMajorCompaction
argument_list|(
name|filesToCompact
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
name|boolean
name|needsCompaction
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|storeFiles
parameter_list|,
name|List
argument_list|<
name|StoreFile
argument_list|>
name|filesCompacting
parameter_list|)
block|{
name|boolean
name|isAfterSplit
init|=
name|StoreUtils
operator|.
name|hasReferences
argument_list|(
name|storeFiles
argument_list|)
decl_stmt|;
if|if
condition|(
name|isAfterSplit
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Split detected, delegate to the parent policy."
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|needsCompaction
argument_list|(
name|storeFiles
argument_list|,
name|filesCompacting
argument_list|)
return|;
block|}
return|return
name|hasExpiredStores
argument_list|(
name|storeFiles
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|hasExpiredStores
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|files
parameter_list|)
block|{
name|long
name|currentTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFile
name|sf
range|:
name|files
control|)
block|{
comment|// Check MIN_VERSIONS is in HStore removeUnneededFiles
name|Long
name|maxTs
init|=
name|sf
operator|.
name|getReader
argument_list|()
operator|.
name|getMaxTimestamp
argument_list|()
decl_stmt|;
name|long
name|maxTtl
init|=
name|storeConfigInfo
operator|.
name|getStoreFileTtl
argument_list|()
decl_stmt|;
if|if
condition|(
name|maxTs
operator|==
literal|null
operator|||
name|maxTtl
operator|==
name|Long
operator|.
name|MAX_VALUE
operator|||
operator|(
name|currentTime
operator|-
name|maxTtl
operator|<
name|maxTs
operator|)
condition|)
block|{
continue|continue;
block|}
else|else
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|getExpiredStores
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|files
parameter_list|,
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesCompacting
parameter_list|)
block|{
name|long
name|currentTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|expiredStores
init|=
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFile
name|sf
range|:
name|files
control|)
block|{
comment|// Check MIN_VERSIONS is in HStore removeUnneededFiles
name|Long
name|maxTs
init|=
name|sf
operator|.
name|getReader
argument_list|()
operator|.
name|getMaxTimestamp
argument_list|()
decl_stmt|;
name|long
name|maxTtl
init|=
name|storeConfigInfo
operator|.
name|getStoreFileTtl
argument_list|()
decl_stmt|;
if|if
condition|(
name|maxTs
operator|==
literal|null
operator|||
name|maxTtl
operator|==
name|Long
operator|.
name|MAX_VALUE
operator|||
operator|(
name|currentTime
operator|-
name|maxTtl
operator|<
name|maxTs
operator|)
condition|)
block|{
continue|continue;
block|}
elseif|else
if|if
condition|(
name|filesCompacting
operator|==
literal|null
operator|||
name|filesCompacting
operator|.
name|contains
argument_list|(
name|sf
argument_list|)
operator|==
literal|false
condition|)
block|{
name|expiredStores
operator|.
name|add
argument_list|(
name|sf
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|expiredStores
return|;
block|}
block|}
end_class

end_unit

