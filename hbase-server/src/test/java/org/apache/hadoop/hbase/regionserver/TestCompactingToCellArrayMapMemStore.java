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
name|regionserver
package|;
end_package

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
name|*
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
name|testclassification
operator|.
name|MediumTests
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
name|testclassification
operator|.
name|RegionServerTests
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
name|Threads
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
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
name|List
import|;
end_import

begin_comment
comment|/**  * compacted memstore test case  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCompactingToCellArrayMapMemStore
extends|extends
name|TestCompactingMemStore
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
name|TestCompactingToCellArrayMapMemStore
operator|.
name|class
argument_list|)
decl_stmt|;
comment|//private static MemStoreChunkPool chunkPool;
comment|//private HRegion region;
comment|//private RegionServicesForStores regionServicesForStores;
comment|//private HStore store;
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Helpers
comment|//////////////////////////////////////////////////////////////////////////////
annotation|@
name|Override
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|chunkPool
operator|.
name|clearChunks
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|compactingSetUp
argument_list|()
expr_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.hregion.compacting.memstore.type"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// compact to CellArrayMap
name|this
operator|.
name|memstore
operator|=
operator|new
name|CompactingMemStore
argument_list|(
name|conf
argument_list|,
name|CellComparator
operator|.
name|COMPARATOR
argument_list|,
name|store
argument_list|,
name|regionServicesForStores
argument_list|)
expr_stmt|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Compaction tests
comment|//////////////////////////////////////////////////////////////////////////////
specifier|public
name|void
name|testCompaction1Bucket
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|counter
init|=
literal|0
decl_stmt|;
name|String
index|[]
name|keys1
init|=
block|{
literal|"A"
block|,
literal|"A"
block|,
literal|"B"
block|,
literal|"C"
block|}
decl_stmt|;
comment|//A1, A2, B3, C4
comment|// test 1 bucket
name|addRowsByKeys
argument_list|(
name|memstore
argument_list|,
name|keys1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|496
argument_list|,
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|memstore
operator|.
name|getActive
argument_list|()
operator|.
name|getCellsCount
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|size
init|=
name|memstore
operator|.
name|getFlushableSize
argument_list|()
decl_stmt|;
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|flushInMemory
argument_list|()
expr_stmt|;
comment|// push keys to pipeline and compact
while|while
condition|(
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|isMemStoreFlushingInMemory
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|memstore
operator|.
name|getSnapshot
argument_list|()
operator|.
name|getCellsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|264
argument_list|,
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Segment
name|s
range|:
name|memstore
operator|.
name|getSegments
argument_list|()
control|)
block|{
name|counter
operator|+=
name|s
operator|.
name|getCellsCount
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|counter
argument_list|)
expr_stmt|;
name|size
operator|=
name|memstore
operator|.
name|getFlushableSize
argument_list|()
expr_stmt|;
name|MemStoreSnapshot
name|snapshot
init|=
name|memstore
operator|.
name|snapshot
argument_list|()
decl_stmt|;
comment|// push keys to snapshot
name|region
operator|.
name|addAndGetGlobalMemstoreSize
argument_list|(
operator|-
name|size
argument_list|)
expr_stmt|;
comment|// simulate flusher
name|ImmutableSegment
name|s
init|=
name|memstore
operator|.
name|getSnapshot
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|s
operator|.
name|getCellsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|clearSnapshot
argument_list|(
name|snapshot
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testCompaction2Buckets
parameter_list|()
throws|throws
name|IOException
block|{
name|String
index|[]
name|keys1
init|=
block|{
literal|"A"
block|,
literal|"A"
block|,
literal|"B"
block|,
literal|"C"
block|}
decl_stmt|;
name|String
index|[]
name|keys2
init|=
block|{
literal|"A"
block|,
literal|"B"
block|,
literal|"D"
block|}
decl_stmt|;
name|addRowsByKeys
argument_list|(
name|memstore
argument_list|,
name|keys1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|496
argument_list|,
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|size
init|=
name|memstore
operator|.
name|getFlushableSize
argument_list|()
decl_stmt|;
comment|//    assertTrue(
comment|//        "\n\n<<< This is the active size with 4 keys - " + memstore.getActive().getSize()
comment|//            + ". This is the memstore flushable size - " + size + "\n",false);
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|flushInMemory
argument_list|()
expr_stmt|;
comment|// push keys to pipeline and compact
while|while
condition|(
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|isMemStoreFlushingInMemory
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
name|int
name|counter
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Segment
name|s
range|:
name|memstore
operator|.
name|getSegments
argument_list|()
control|)
block|{
name|counter
operator|+=
name|s
operator|.
name|getCellsCount
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|counter
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|memstore
operator|.
name|getSnapshot
argument_list|()
operator|.
name|getCellsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|264
argument_list|,
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
argument_list|)
expr_stmt|;
name|addRowsByKeys
argument_list|(
name|memstore
argument_list|,
name|keys2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|640
argument_list|,
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
argument_list|)
expr_stmt|;
name|size
operator|=
name|memstore
operator|.
name|getFlushableSize
argument_list|()
expr_stmt|;
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|flushInMemory
argument_list|()
expr_stmt|;
comment|// push keys to pipeline and compact
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|isMemStoreFlushingInMemory
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|>
literal|10000000
condition|)
block|{
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|debug
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"\n\n<<< Infinite loop! :( \n"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|memstore
operator|.
name|getSnapshot
argument_list|()
operator|.
name|getCellsCount
argument_list|()
argument_list|)
expr_stmt|;
name|counter
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|Segment
name|s
range|:
name|memstore
operator|.
name|getSegments
argument_list|()
control|)
block|{
name|counter
operator|+=
name|s
operator|.
name|getCellsCount
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|counter
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|368
argument_list|,
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
argument_list|)
expr_stmt|;
name|size
operator|=
name|memstore
operator|.
name|getFlushableSize
argument_list|()
expr_stmt|;
name|MemStoreSnapshot
name|snapshot
init|=
name|memstore
operator|.
name|snapshot
argument_list|()
decl_stmt|;
comment|// push keys to snapshot
name|region
operator|.
name|addAndGetGlobalMemstoreSize
argument_list|(
operator|-
name|size
argument_list|)
expr_stmt|;
comment|// simulate flusher
name|ImmutableSegment
name|s
init|=
name|memstore
operator|.
name|getSnapshot
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|s
operator|.
name|getCellsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|clearSnapshot
argument_list|(
name|snapshot
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testCompaction3Buckets
parameter_list|()
throws|throws
name|IOException
block|{
name|String
index|[]
name|keys1
init|=
block|{
literal|"A"
block|,
literal|"A"
block|,
literal|"B"
block|,
literal|"C"
block|}
decl_stmt|;
name|String
index|[]
name|keys2
init|=
block|{
literal|"A"
block|,
literal|"B"
block|,
literal|"D"
block|}
decl_stmt|;
name|String
index|[]
name|keys3
init|=
block|{
literal|"D"
block|,
literal|"B"
block|,
literal|"B"
block|}
decl_stmt|;
name|addRowsByKeys
argument_list|(
name|memstore
argument_list|,
name|keys1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|496
argument_list|,
name|region
operator|.
name|getMemstoreSize
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|size
init|=
name|memstore
operator|.
name|getFlushableSize
argument_list|()
decl_stmt|;
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|flushInMemory
argument_list|()
expr_stmt|;
comment|// push keys to pipeline and compact
name|String
name|tstStr
init|=
literal|"\n\nFlushable size after first flush in memory:"
operator|+
name|size
operator|+
literal|". Is MemmStore in compaction?:"
operator|+
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|isMemStoreFlushingInMemory
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|isMemStoreFlushingInMemory
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|memstore
operator|.
name|getSnapshot
argument_list|()
operator|.
name|getCellsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|264
argument_list|,
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
argument_list|)
expr_stmt|;
name|addRowsByKeys
argument_list|(
name|memstore
argument_list|,
name|keys2
argument_list|)
expr_stmt|;
name|tstStr
operator|+=
literal|" After adding second part of the keys. Memstore size: "
operator|+
name|region
operator|.
name|getMemstoreSize
argument_list|()
operator|+
literal|", Memstore Total Size: "
operator|+
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
operator|+
literal|"\n\n"
expr_stmt|;
name|assertEquals
argument_list|(
literal|640
argument_list|,
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
argument_list|)
expr_stmt|;
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|disableCompaction
argument_list|()
expr_stmt|;
name|size
operator|=
name|memstore
operator|.
name|getFlushableSize
argument_list|()
expr_stmt|;
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|flushInMemory
argument_list|()
expr_stmt|;
comment|// push keys to pipeline without compaction
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|memstore
operator|.
name|getSnapshot
argument_list|()
operator|.
name|getCellsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|640
argument_list|,
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
argument_list|)
expr_stmt|;
name|addRowsByKeys
argument_list|(
name|memstore
argument_list|,
name|keys3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1016
argument_list|,
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
argument_list|)
expr_stmt|;
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|enableCompaction
argument_list|()
expr_stmt|;
name|size
operator|=
name|memstore
operator|.
name|getFlushableSize
argument_list|()
expr_stmt|;
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|flushInMemory
argument_list|()
expr_stmt|;
comment|// push keys to pipeline and compact
while|while
condition|(
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|isMemStoreFlushingInMemory
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|memstore
operator|.
name|getSnapshot
argument_list|()
operator|.
name|getCellsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|384
argument_list|,
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
argument_list|)
expr_stmt|;
name|size
operator|=
name|memstore
operator|.
name|getFlushableSize
argument_list|()
expr_stmt|;
name|MemStoreSnapshot
name|snapshot
init|=
name|memstore
operator|.
name|snapshot
argument_list|()
decl_stmt|;
comment|// push keys to snapshot
name|region
operator|.
name|addAndGetGlobalMemstoreSize
argument_list|(
operator|-
name|size
argument_list|)
expr_stmt|;
comment|// simulate flusher
name|ImmutableSegment
name|s
init|=
name|memstore
operator|.
name|getSnapshot
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|s
operator|.
name|getCellsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|regionServicesForStores
operator|.
name|getGlobalMemstoreTotalSize
argument_list|()
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|clearSnapshot
argument_list|(
name|snapshot
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Flattening tests
comment|//////////////////////////////////////////////////////////////////////////////
annotation|@
name|Test
specifier|public
name|void
name|testFlattening
parameter_list|()
throws|throws
name|IOException
block|{
name|String
index|[]
name|keys1
init|=
block|{
literal|"A"
block|,
literal|"A"
block|,
literal|"B"
block|,
literal|"C"
block|,
literal|"F"
block|,
literal|"H"
block|}
decl_stmt|;
name|String
index|[]
name|keys2
init|=
block|{
literal|"A"
block|,
literal|"B"
block|,
literal|"D"
block|,
literal|"G"
block|,
literal|"I"
block|,
literal|"J"
block|}
decl_stmt|;
name|String
index|[]
name|keys3
init|=
block|{
literal|"D"
block|,
literal|"B"
block|,
literal|"B"
block|,
literal|"E"
block|}
decl_stmt|;
comment|// set flattening to true
name|memstore
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.hregion.compacting.memstore.flatten"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|addRowsByKeys
argument_list|(
name|memstore
argument_list|,
name|keys1
argument_list|)
expr_stmt|;
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|flushInMemory
argument_list|()
expr_stmt|;
comment|// push keys to pipeline should not compact
while|while
condition|(
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|isMemStoreFlushingInMemory
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|memstore
operator|.
name|getSnapshot
argument_list|()
operator|.
name|getCellsCount
argument_list|()
argument_list|)
expr_stmt|;
name|addRowsByKeys
argument_list|(
name|memstore
argument_list|,
name|keys2
argument_list|)
expr_stmt|;
comment|// also should only flatten
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|disableCompaction
argument_list|()
expr_stmt|;
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|flushInMemory
argument_list|()
expr_stmt|;
comment|// push keys to pipeline without flattening
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|memstore
operator|.
name|getSnapshot
argument_list|()
operator|.
name|getCellsCount
argument_list|()
argument_list|)
expr_stmt|;
name|addRowsByKeys
argument_list|(
name|memstore
argument_list|,
name|keys3
argument_list|)
expr_stmt|;
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|enableCompaction
argument_list|()
expr_stmt|;
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|flushInMemory
argument_list|()
expr_stmt|;
comment|// push keys to pipeline and compact
while|while
condition|(
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|isMemStoreFlushingInMemory
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|memstore
operator|.
name|getSnapshot
argument_list|()
operator|.
name|getCellsCount
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|counter
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Segment
name|s
range|:
name|memstore
operator|.
name|getSegments
argument_list|()
control|)
block|{
name|counter
operator|+=
name|s
operator|.
name|getCellsCount
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|counter
argument_list|)
expr_stmt|;
name|MemStoreSnapshot
name|snapshot
init|=
name|memstore
operator|.
name|snapshot
argument_list|()
decl_stmt|;
comment|// push keys to snapshot
name|ImmutableSegment
name|s
init|=
name|memstore
operator|.
name|getSnapshot
argument_list|()
decl_stmt|;
name|memstore
operator|.
name|clearSnapshot
argument_list|(
name|snapshot
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCountOfCellsAfterFlatteningByScan
parameter_list|()
throws|throws
name|IOException
block|{
name|String
index|[]
name|keys1
init|=
block|{
literal|"A"
block|,
literal|"B"
block|,
literal|"C"
block|}
decl_stmt|;
comment|// A, B, C
name|addRowsByKeysWith50Cols
argument_list|(
name|memstore
argument_list|,
name|keys1
argument_list|)
expr_stmt|;
comment|// this should only flatten as there are no duplicates
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|flushInMemory
argument_list|()
expr_stmt|;
while|while
condition|(
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|isMemStoreFlushingInMemory
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
init|=
name|memstore
operator|.
name|getScanners
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
comment|// seek
name|scanners
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|seek
argument_list|(
name|KeyValue
operator|.
name|LOWESTKEY
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|scanners
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"the count should be "
argument_list|,
name|count
argument_list|,
literal|150
argument_list|)
expr_stmt|;
name|scanners
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCountOfCellsAfterFlatteningByIterator
parameter_list|()
throws|throws
name|IOException
block|{
name|String
index|[]
name|keys1
init|=
block|{
literal|"A"
block|,
literal|"B"
block|,
literal|"C"
block|}
decl_stmt|;
comment|// A, B, C
name|addRowsByKeysWith50Cols
argument_list|(
name|memstore
argument_list|,
name|keys1
argument_list|)
expr_stmt|;
comment|// this should only flatten as there are no duplicates
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|flushInMemory
argument_list|()
expr_stmt|;
while|while
condition|(
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|isMemStoreFlushingInMemory
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
comment|// Just doing the cnt operation here
name|MemStoreCompactorIterator
name|itr
init|=
operator|new
name|MemStoreCompactorIterator
argument_list|(
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|getImmutableSegments
argument_list|()
operator|.
name|getStoreSegments
argument_list|()
argument_list|,
name|CellComparator
operator|.
name|COMPARATOR
argument_list|,
literal|10
argument_list|,
operator|(
operator|(
name|CompactingMemStore
operator|)
name|memstore
operator|)
operator|.
name|getStore
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|cnt
init|=
literal|0
decl_stmt|;
try|try
block|{
while|while
condition|(
name|itr
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|cnt
operator|++
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|itr
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"the count should be "
argument_list|,
name|cnt
argument_list|,
literal|150
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addRowsByKeysWith50Cols
parameter_list|(
name|AbstractMemStore
name|hmc
parameter_list|,
name|String
index|[]
name|keys
parameter_list|)
block|{
name|byte
index|[]
name|fam
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testfamily"
argument_list|)
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
name|keys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|long
name|timestamp
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// to make sure each kv gets a different ts
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|50
condition|;
name|j
operator|++
control|)
block|{
name|byte
index|[]
name|qf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testqualifier"
operator|+
name|j
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|keys
index|[
name|i
index|]
operator|+
name|j
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf
argument_list|,
name|timestamp
argument_list|,
name|val
argument_list|)
decl_stmt|;
name|hmc
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|addRowsByKeys
parameter_list|(
specifier|final
name|AbstractMemStore
name|hmc
parameter_list|,
name|String
index|[]
name|keys
parameter_list|)
block|{
name|byte
index|[]
name|fam
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testfamily"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testqualifier"
argument_list|)
decl_stmt|;
name|long
name|size
init|=
name|hmc
operator|.
name|getActive
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
comment|//
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|long
name|timestamp
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// to make sure each kv gets a different ts
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|keys
index|[
name|i
index|]
operator|+
name|i
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf
argument_list|,
name|timestamp
argument_list|,
name|val
argument_list|)
decl_stmt|;
name|hmc
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"added kv: "
operator|+
name|kv
operator|.
name|getKeyString
argument_list|()
operator|+
literal|", timestamp"
operator|+
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|regionServicesForStores
operator|.
name|addAndGetGlobalMemstoreSize
argument_list|(
name|hmc
operator|.
name|getActive
argument_list|()
operator|.
name|size
argument_list|()
operator|-
name|size
argument_list|)
expr_stmt|;
comment|//
block|}
block|}
end_class

end_unit

