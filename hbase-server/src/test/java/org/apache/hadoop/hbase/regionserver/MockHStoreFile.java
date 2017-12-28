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
name|regionserver
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
name|Arrays
import|;
end_import

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
name|Optional
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|OptionalLong
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|Cell
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
name|CellBuilderFactory
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
name|CellBuilderType
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
name|HBaseTestingUtility
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
name|HDFSBlocksDistribution
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
name|io
operator|.
name|hfile
operator|.
name|CacheConfig
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
name|Bytes
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/** A mock used so our tests don't deal with actual StoreFiles */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MockHStoreFile
extends|extends
name|HStoreFile
block|{
name|long
name|length
init|=
literal|0
decl_stmt|;
name|boolean
name|isRef
init|=
literal|false
decl_stmt|;
name|long
name|ageInDisk
decl_stmt|;
name|long
name|sequenceid
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|metadata
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|byte
index|[]
name|splitPoint
init|=
literal|null
decl_stmt|;
name|TimeRangeTracker
name|timeRangeTracker
decl_stmt|;
name|long
name|entryCount
decl_stmt|;
name|boolean
name|isMajor
decl_stmt|;
name|HDFSBlocksDistribution
name|hdfsBlocksDistribution
decl_stmt|;
name|long
name|modificationTime
decl_stmt|;
name|boolean
name|compactedAway
decl_stmt|;
name|MockHStoreFile
parameter_list|(
name|HBaseTestingUtility
name|testUtil
parameter_list|,
name|Path
name|testPath
parameter_list|,
name|long
name|length
parameter_list|,
name|long
name|ageInDisk
parameter_list|,
name|boolean
name|isRef
parameter_list|,
name|long
name|sequenceid
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|testUtil
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|testPath
argument_list|,
name|testUtil
operator|.
name|getConfiguration
argument_list|()
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|testUtil
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|BloomType
operator|.
name|NONE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
name|this
operator|.
name|isRef
operator|=
name|isRef
expr_stmt|;
name|this
operator|.
name|ageInDisk
operator|=
name|ageInDisk
expr_stmt|;
name|this
operator|.
name|sequenceid
operator|=
name|sequenceid
expr_stmt|;
name|this
operator|.
name|isMajor
operator|=
literal|false
expr_stmt|;
name|hdfsBlocksDistribution
operator|=
operator|new
name|HDFSBlocksDistribution
argument_list|()
expr_stmt|;
name|hdfsBlocksDistribution
operator|.
name|addHostsAndBlockWeight
argument_list|(
operator|new
name|String
index|[]
block|{
name|RSRpcServices
operator|.
name|getHostname
argument_list|(
name|testUtil
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|false
argument_list|)
block|}
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|modificationTime
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
block|}
name|void
name|setLength
parameter_list|(
name|long
name|newLen
parameter_list|)
block|{
name|this
operator|.
name|length
operator|=
name|newLen
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMaxSequenceId
parameter_list|()
block|{
return|return
name|sequenceid
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isMajorCompactionResult
parameter_list|()
block|{
return|return
name|isMajor
return|;
block|}
specifier|public
name|void
name|setIsMajor
parameter_list|(
name|boolean
name|isMajor
parameter_list|)
block|{
name|this
operator|.
name|isMajor
operator|=
name|isMajor
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isReference
parameter_list|()
block|{
return|return
name|this
operator|.
name|isRef
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isBulkLoadResult
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getMetadataValue
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
block|{
return|return
name|this
operator|.
name|metadata
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
specifier|public
name|void
name|setMetadataValue
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
operator|.
name|metadata
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|void
name|setTimeRangeTracker
parameter_list|(
name|TimeRangeTracker
name|timeRangeTracker
parameter_list|)
block|{
name|this
operator|.
name|timeRangeTracker
operator|=
name|timeRangeTracker
expr_stmt|;
block|}
name|void
name|setEntries
parameter_list|(
name|long
name|entryCount
parameter_list|)
block|{
name|this
operator|.
name|entryCount
operator|=
name|entryCount
expr_stmt|;
block|}
specifier|public
name|OptionalLong
name|getMinimumTimestamp
parameter_list|()
block|{
return|return
name|timeRangeTracker
operator|==
literal|null
condition|?
name|OptionalLong
operator|.
name|empty
argument_list|()
else|:
name|OptionalLong
operator|.
name|of
argument_list|(
name|timeRangeTracker
operator|.
name|getMin
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|OptionalLong
name|getMaximumTimestamp
parameter_list|()
block|{
return|return
name|timeRangeTracker
operator|==
literal|null
condition|?
name|OptionalLong
operator|.
name|empty
argument_list|()
else|:
name|OptionalLong
operator|.
name|of
argument_list|(
name|timeRangeTracker
operator|.
name|getMax
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|markCompactedAway
parameter_list|()
block|{
name|this
operator|.
name|compactedAway
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCompactedAway
parameter_list|()
block|{
return|return
name|compactedAway
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getModificationTimeStamp
parameter_list|()
block|{
return|return
name|modificationTime
return|;
block|}
annotation|@
name|Override
specifier|public
name|HDFSBlocksDistribution
name|getHDFSBlockDistribution
parameter_list|()
block|{
return|return
name|hdfsBlocksDistribution
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initReader
parameter_list|()
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|StoreFileScanner
name|getPreadScanner
parameter_list|(
name|boolean
name|cacheBlocks
parameter_list|,
name|long
name|readPt
parameter_list|,
name|long
name|scannerOrder
parameter_list|,
name|boolean
name|canOptimizeForNonNullColumn
parameter_list|)
block|{
return|return
name|getReader
argument_list|()
operator|.
name|getStoreFileScanner
argument_list|(
name|cacheBlocks
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
name|readPt
argument_list|,
name|scannerOrder
argument_list|,
name|canOptimizeForNonNullColumn
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|StoreFileScanner
name|getStreamScanner
parameter_list|(
name|boolean
name|canUseDropBehind
parameter_list|,
name|boolean
name|cacheBlocks
parameter_list|,
name|boolean
name|isCompaction
parameter_list|,
name|long
name|readPt
parameter_list|,
name|long
name|scannerOrder
parameter_list|,
name|boolean
name|canOptimizeForNonNullColumn
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getReader
argument_list|()
operator|.
name|getStoreFileScanner
argument_list|(
name|cacheBlocks
argument_list|,
literal|false
argument_list|,
name|isCompaction
argument_list|,
name|readPt
argument_list|,
name|scannerOrder
argument_list|,
name|canOptimizeForNonNullColumn
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|StoreFileReader
name|getReader
parameter_list|()
block|{
specifier|final
name|long
name|len
init|=
name|this
operator|.
name|length
decl_stmt|;
specifier|final
name|TimeRangeTracker
name|timeRangeTracker
init|=
name|this
operator|.
name|timeRangeTracker
decl_stmt|;
specifier|final
name|long
name|entries
init|=
name|this
operator|.
name|entryCount
decl_stmt|;
return|return
operator|new
name|StoreFileReader
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|length
parameter_list|()
block|{
return|return
name|len
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMaxTimestamp
parameter_list|()
block|{
return|return
name|timeRange
operator|==
literal|null
condition|?
name|Long
operator|.
name|MAX_VALUE
else|:
name|timeRangeTracker
operator|.
name|getMax
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getEntries
parameter_list|()
block|{
return|return
name|entries
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|evictOnClose
parameter_list|)
throws|throws
name|IOException
block|{
comment|// no-op
block|}
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|Cell
argument_list|>
name|getLastKey
parameter_list|()
block|{
if|if
condition|(
name|splitPoint
operator|!=
literal|null
condition|)
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|CellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|DEEP_COPY
argument_list|)
operator|.
name|setType
argument_list|(
name|Cell
operator|.
name|Type
operator|.
name|Put
argument_list|)
operator|.
name|setRow
argument_list|(
name|Arrays
operator|.
name|copyOf
argument_list|(
name|splitPoint
argument_list|,
name|splitPoint
operator|.
name|length
operator|+
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|Cell
argument_list|>
name|midKey
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|splitPoint
operator|!=
literal|null
condition|)
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|CellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|DEEP_COPY
argument_list|)
operator|.
name|setType
argument_list|(
name|Cell
operator|.
name|Type
operator|.
name|Put
argument_list|)
operator|.
name|setRow
argument_list|(
name|splitPoint
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|Cell
argument_list|>
name|getFirstKey
parameter_list|()
block|{
if|if
condition|(
name|splitPoint
operator|!=
literal|null
condition|)
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|CellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|DEEP_COPY
argument_list|)
operator|.
name|setType
argument_list|(
name|Cell
operator|.
name|Type
operator|.
name|Put
argument_list|)
operator|.
name|setRow
argument_list|(
name|splitPoint
argument_list|,
literal|0
argument_list|,
name|splitPoint
operator|.
name|length
operator|-
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|OptionalLong
name|getBulkLoadTimestamp
parameter_list|()
block|{
comment|// we always return false for isBulkLoadResult so we do not have a bulk load timestamp
return|return
name|OptionalLong
operator|.
name|empty
argument_list|()
return|;
block|}
block|}
end_class

end_unit

