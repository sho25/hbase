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
package|;
end_package

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
name|assertFalse
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
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
name|CoordinatedStateManager
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
name|Server
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
name|ServerName
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
name|SmallTests
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
name|catalog
operator|.
name|CatalogTracker
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
name|BlockCache
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
name|BlockCacheKey
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
name|CacheStats
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
name|Cacheable
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
name|CachedBlock
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
name|ResizableBlockCache
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
name|HeapMemoryManager
operator|.
name|TunerContext
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
name|HeapMemoryManager
operator|.
name|TunerResult
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
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

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestHeapMemoryManager
block|{
specifier|private
name|long
name|maxHeapSize
init|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getMax
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testAutoTunerShouldBeOffWhenMaxMinRangesForMemstoreIsNotGiven
parameter_list|()
throws|throws
name|Exception
block|{
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
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MAX_RANGE_KEY
argument_list|,
literal|0.75f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MIN_RANGE_KEY
argument_list|,
literal|0.05f
argument_list|)
expr_stmt|;
name|HeapMemoryManager
name|manager
init|=
operator|new
name|HeapMemoryManager
argument_list|(
operator|new
name|BlockCacheStub
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|MemstoreFlusherStub
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|RegionServerStub
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|manager
operator|.
name|isTunerOn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAutoTunerShouldBeOffWhenMaxMinRangesForBlockCacheIsNotGiven
parameter_list|()
throws|throws
name|Exception
block|{
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
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MAX_RANGE_KEY
argument_list|,
literal|0.75f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MIN_RANGE_KEY
argument_list|,
literal|0.05f
argument_list|)
expr_stmt|;
name|HeapMemoryManager
name|manager
init|=
operator|new
name|HeapMemoryManager
argument_list|(
operator|new
name|BlockCacheStub
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|MemstoreFlusherStub
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|RegionServerStub
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|manager
operator|.
name|isTunerOn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWhenMemstoreAndBlockCacheMaxMinChecksFails
parameter_list|()
throws|throws
name|Exception
block|{
name|BlockCacheStub
name|blockCache
init|=
operator|new
name|BlockCacheStub
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|MemstoreFlusherStub
name|memStoreFlusher
init|=
operator|new
name|MemstoreFlusherStub
argument_list|(
literal|0
argument_list|)
decl_stmt|;
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
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MAX_RANGE_KEY
argument_list|,
literal|0.75f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MIN_RANGE_KEY
argument_list|,
literal|0.06f
argument_list|)
expr_stmt|;
try|try
block|{
operator|new
name|HeapMemoryManager
argument_list|(
name|blockCache
argument_list|,
name|memStoreFlusher
argument_list|,
operator|new
name|RegionServerStub
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{     }
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MIN_RANGE_KEY
argument_list|,
literal|0.2f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MAX_RANGE_KEY
argument_list|,
literal|0.7f
argument_list|)
expr_stmt|;
try|try
block|{
operator|new
name|HeapMemoryManager
argument_list|(
name|blockCache
argument_list|,
name|memStoreFlusher
argument_list|,
operator|new
name|RegionServerStub
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{     }
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWhenClusterIsWriteHeavy
parameter_list|()
throws|throws
name|Exception
block|{
name|BlockCacheStub
name|blockCache
init|=
operator|new
name|BlockCacheStub
argument_list|(
call|(
name|long
call|)
argument_list|(
name|maxHeapSize
operator|*
literal|0.4
argument_list|)
argument_list|)
decl_stmt|;
name|MemstoreFlusherStub
name|memStoreFlusher
init|=
operator|new
name|MemstoreFlusherStub
argument_list|(
call|(
name|long
call|)
argument_list|(
name|maxHeapSize
operator|*
literal|0.4
argument_list|)
argument_list|)
decl_stmt|;
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
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MAX_RANGE_KEY
argument_list|,
literal|0.75f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MIN_RANGE_KEY
argument_list|,
literal|0.10f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MAX_RANGE_KEY
argument_list|,
literal|0.7f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MIN_RANGE_KEY
argument_list|,
literal|0.05f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HeapMemoryManager
operator|.
name|HBASE_RS_HEAP_MEMORY_TUNER_PERIOD
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// Let the system start with default values for memstore heap and block cache size.
name|HeapMemoryManager
name|heapMemoryManager
init|=
operator|new
name|HeapMemoryManager
argument_list|(
name|blockCache
argument_list|,
name|memStoreFlusher
argument_list|,
operator|new
name|RegionServerStub
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|oldMemstoreHeapSize
init|=
name|memStoreFlusher
operator|.
name|memstoreSize
decl_stmt|;
name|long
name|oldBlockCacheSize
init|=
name|blockCache
operator|.
name|maxSize
decl_stmt|;
name|heapMemoryManager
operator|.
name|start
argument_list|()
expr_stmt|;
name|memStoreFlusher
operator|.
name|flushType
operator|=
name|FlushType
operator|.
name|ABOVE_HIGHER_MARK
expr_stmt|;
name|memStoreFlusher
operator|.
name|requestFlush
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|memStoreFlusher
operator|.
name|requestFlush
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|memStoreFlusher
operator|.
name|requestFlush
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|memStoreFlusher
operator|.
name|flushType
operator|=
name|FlushType
operator|.
name|ABOVE_LOWER_MARK
expr_stmt|;
name|memStoreFlusher
operator|.
name|requestFlush
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1500
argument_list|)
expr_stmt|;
comment|// Allow the tuner to run once and do necessary memory up
name|assertHeapSpaceDelta
argument_list|(
name|DefaultHeapMemoryTuner
operator|.
name|DEFAULT_STEP_VALUE
argument_list|,
name|oldMemstoreHeapSize
argument_list|,
name|memStoreFlusher
operator|.
name|memstoreSize
argument_list|)
expr_stmt|;
name|assertHeapSpaceDelta
argument_list|(
operator|-
operator|(
name|DefaultHeapMemoryTuner
operator|.
name|DEFAULT_STEP_VALUE
operator|)
argument_list|,
name|oldBlockCacheSize
argument_list|,
name|blockCache
operator|.
name|maxSize
argument_list|)
expr_stmt|;
name|oldMemstoreHeapSize
operator|=
name|memStoreFlusher
operator|.
name|memstoreSize
expr_stmt|;
name|oldBlockCacheSize
operator|=
name|blockCache
operator|.
name|maxSize
expr_stmt|;
comment|// Do some more flushes before the next run of HeapMemoryTuner
name|memStoreFlusher
operator|.
name|flushType
operator|=
name|FlushType
operator|.
name|ABOVE_HIGHER_MARK
expr_stmt|;
name|memStoreFlusher
operator|.
name|requestFlush
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|memStoreFlusher
operator|.
name|requestFlush
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1500
argument_list|)
expr_stmt|;
name|assertHeapSpaceDelta
argument_list|(
name|DefaultHeapMemoryTuner
operator|.
name|DEFAULT_STEP_VALUE
argument_list|,
name|oldMemstoreHeapSize
argument_list|,
name|memStoreFlusher
operator|.
name|memstoreSize
argument_list|)
expr_stmt|;
name|assertHeapSpaceDelta
argument_list|(
operator|-
operator|(
name|DefaultHeapMemoryTuner
operator|.
name|DEFAULT_STEP_VALUE
operator|)
argument_list|,
name|oldBlockCacheSize
argument_list|,
name|blockCache
operator|.
name|maxSize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWhenClusterIsReadHeavy
parameter_list|()
throws|throws
name|Exception
block|{
name|BlockCacheStub
name|blockCache
init|=
operator|new
name|BlockCacheStub
argument_list|(
call|(
name|long
call|)
argument_list|(
name|maxHeapSize
operator|*
literal|0.4
argument_list|)
argument_list|)
decl_stmt|;
name|MemstoreFlusherStub
name|memStoreFlusher
init|=
operator|new
name|MemstoreFlusherStub
argument_list|(
call|(
name|long
call|)
argument_list|(
name|maxHeapSize
operator|*
literal|0.4
argument_list|)
argument_list|)
decl_stmt|;
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
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MAX_RANGE_KEY
argument_list|,
literal|0.75f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MIN_RANGE_KEY
argument_list|,
literal|0.10f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MAX_RANGE_KEY
argument_list|,
literal|0.7f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MIN_RANGE_KEY
argument_list|,
literal|0.05f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HeapMemoryManager
operator|.
name|HBASE_RS_HEAP_MEMORY_TUNER_PERIOD
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// Let the system start with default values for memstore heap and block cache size.
name|HeapMemoryManager
name|heapMemoryManager
init|=
operator|new
name|HeapMemoryManager
argument_list|(
name|blockCache
argument_list|,
name|memStoreFlusher
argument_list|,
operator|new
name|RegionServerStub
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|oldMemstoreHeapSize
init|=
name|memStoreFlusher
operator|.
name|memstoreSize
decl_stmt|;
name|long
name|oldBlockCacheSize
init|=
name|blockCache
operator|.
name|maxSize
decl_stmt|;
name|heapMemoryManager
operator|.
name|start
argument_list|()
expr_stmt|;
name|blockCache
operator|.
name|evictBlock
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|blockCache
operator|.
name|evictBlock
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|blockCache
operator|.
name|evictBlock
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1500
argument_list|)
expr_stmt|;
comment|// Allow the tuner to run once and do necessary memory up
name|assertHeapSpaceDelta
argument_list|(
operator|-
operator|(
name|DefaultHeapMemoryTuner
operator|.
name|DEFAULT_STEP_VALUE
operator|)
argument_list|,
name|oldMemstoreHeapSize
argument_list|,
name|memStoreFlusher
operator|.
name|memstoreSize
argument_list|)
expr_stmt|;
name|assertHeapSpaceDelta
argument_list|(
name|DefaultHeapMemoryTuner
operator|.
name|DEFAULT_STEP_VALUE
argument_list|,
name|oldBlockCacheSize
argument_list|,
name|blockCache
operator|.
name|maxSize
argument_list|)
expr_stmt|;
name|oldMemstoreHeapSize
operator|=
name|memStoreFlusher
operator|.
name|memstoreSize
expr_stmt|;
name|oldBlockCacheSize
operator|=
name|blockCache
operator|.
name|maxSize
expr_stmt|;
comment|// Do some more evictions before the next run of HeapMemoryTuner
name|blockCache
operator|.
name|evictBlock
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1500
argument_list|)
expr_stmt|;
name|assertHeapSpaceDelta
argument_list|(
operator|-
operator|(
name|DefaultHeapMemoryTuner
operator|.
name|DEFAULT_STEP_VALUE
operator|)
argument_list|,
name|oldMemstoreHeapSize
argument_list|,
name|memStoreFlusher
operator|.
name|memstoreSize
argument_list|)
expr_stmt|;
name|assertHeapSpaceDelta
argument_list|(
name|DefaultHeapMemoryTuner
operator|.
name|DEFAULT_STEP_VALUE
argument_list|,
name|oldBlockCacheSize
argument_list|,
name|blockCache
operator|.
name|maxSize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPluggingInHeapMemoryTuner
parameter_list|()
throws|throws
name|Exception
block|{
name|BlockCacheStub
name|blockCache
init|=
operator|new
name|BlockCacheStub
argument_list|(
call|(
name|long
call|)
argument_list|(
name|maxHeapSize
operator|*
literal|0.4
argument_list|)
argument_list|)
decl_stmt|;
name|MemstoreFlusherStub
name|memStoreFlusher
init|=
operator|new
name|MemstoreFlusherStub
argument_list|(
call|(
name|long
call|)
argument_list|(
name|maxHeapSize
operator|*
literal|0.4
argument_list|)
argument_list|)
decl_stmt|;
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
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MAX_RANGE_KEY
argument_list|,
literal|0.78f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MIN_RANGE_KEY
argument_list|,
literal|0.05f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MAX_RANGE_KEY
argument_list|,
literal|0.75f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MIN_RANGE_KEY
argument_list|,
literal|0.02f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HeapMemoryManager
operator|.
name|HBASE_RS_HEAP_MEMORY_TUNER_PERIOD
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
name|HeapMemoryManager
operator|.
name|HBASE_RS_HEAP_MEMORY_TUNER_CLASS
argument_list|,
name|CustomHeapMemoryTuner
operator|.
name|class
argument_list|,
name|HeapMemoryTuner
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// Let the system start with default values for memstore heap and block cache size.
name|HeapMemoryManager
name|heapMemoryManager
init|=
operator|new
name|HeapMemoryManager
argument_list|(
name|blockCache
argument_list|,
name|memStoreFlusher
argument_list|,
operator|new
name|RegionServerStub
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|heapMemoryManager
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// Now we wants to be in write mode. Set bigger memstore size from CustomHeapMemoryTuner
name|CustomHeapMemoryTuner
operator|.
name|memstoreSize
operator|=
literal|0.78f
expr_stmt|;
name|CustomHeapMemoryTuner
operator|.
name|blockCacheSize
operator|=
literal|0.02f
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1500
argument_list|)
expr_stmt|;
comment|// Allow the tuner to run once and do necessary memory up
name|assertHeapSpace
argument_list|(
literal|0.78f
argument_list|,
name|memStoreFlusher
operator|.
name|memstoreSize
argument_list|)
expr_stmt|;
comment|// Memstore
name|assertHeapSpace
argument_list|(
literal|0.02f
argument_list|,
name|blockCache
operator|.
name|maxSize
argument_list|)
expr_stmt|;
comment|// BlockCache
comment|// Now we wants to be in read mode. Set bigger memstore size from CustomHeapMemoryTuner
name|CustomHeapMemoryTuner
operator|.
name|blockCacheSize
operator|=
literal|0.75f
expr_stmt|;
name|CustomHeapMemoryTuner
operator|.
name|memstoreSize
operator|=
literal|0.05f
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1500
argument_list|)
expr_stmt|;
comment|// Allow the tuner to run once and do necessary memory up
name|assertHeapSpace
argument_list|(
literal|0.75f
argument_list|,
name|blockCache
operator|.
name|maxSize
argument_list|)
expr_stmt|;
comment|// BlockCache
name|assertHeapSpace
argument_list|(
literal|0.05f
argument_list|,
name|memStoreFlusher
operator|.
name|memstoreSize
argument_list|)
expr_stmt|;
comment|// Memstore
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWhenSizeGivenByHeapTunerGoesOutsideRange
parameter_list|()
throws|throws
name|Exception
block|{
name|BlockCacheStub
name|blockCache
init|=
operator|new
name|BlockCacheStub
argument_list|(
call|(
name|long
call|)
argument_list|(
name|maxHeapSize
operator|*
literal|0.4
argument_list|)
argument_list|)
decl_stmt|;
name|MemstoreFlusherStub
name|memStoreFlusher
init|=
operator|new
name|MemstoreFlusherStub
argument_list|(
call|(
name|long
call|)
argument_list|(
name|maxHeapSize
operator|*
literal|0.4
argument_list|)
argument_list|)
decl_stmt|;
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
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MAX_RANGE_KEY
argument_list|,
literal|0.7f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MIN_RANGE_KEY
argument_list|,
literal|0.1f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MAX_RANGE_KEY
argument_list|,
literal|0.7f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MIN_RANGE_KEY
argument_list|,
literal|0.1f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HeapMemoryManager
operator|.
name|HBASE_RS_HEAP_MEMORY_TUNER_PERIOD
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
name|HeapMemoryManager
operator|.
name|HBASE_RS_HEAP_MEMORY_TUNER_CLASS
argument_list|,
name|CustomHeapMemoryTuner
operator|.
name|class
argument_list|,
name|HeapMemoryTuner
operator|.
name|class
argument_list|)
expr_stmt|;
name|HeapMemoryManager
name|heapMemoryManager
init|=
operator|new
name|HeapMemoryManager
argument_list|(
name|blockCache
argument_list|,
name|memStoreFlusher
argument_list|,
operator|new
name|RegionServerStub
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|heapMemoryManager
operator|.
name|start
argument_list|()
expr_stmt|;
name|CustomHeapMemoryTuner
operator|.
name|memstoreSize
operator|=
literal|0.78f
expr_stmt|;
name|CustomHeapMemoryTuner
operator|.
name|blockCacheSize
operator|=
literal|0.02f
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1500
argument_list|)
expr_stmt|;
comment|// Allow the tuner to run once and do necessary memory up
comment|// Even if the tuner says to set the memstore to 78%, HBase makes it as 70% as that is the
comment|// upper bound. Same with block cache as 10% is the lower bound.
name|assertHeapSpace
argument_list|(
literal|0.7f
argument_list|,
name|memStoreFlusher
operator|.
name|memstoreSize
argument_list|)
expr_stmt|;
name|assertHeapSpace
argument_list|(
literal|0.1f
argument_list|,
name|blockCache
operator|.
name|maxSize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWhenCombinedHeapSizesFromTunerGoesOutSideMaxLimit
parameter_list|()
throws|throws
name|Exception
block|{
name|BlockCacheStub
name|blockCache
init|=
operator|new
name|BlockCacheStub
argument_list|(
call|(
name|long
call|)
argument_list|(
name|maxHeapSize
operator|*
literal|0.4
argument_list|)
argument_list|)
decl_stmt|;
name|MemstoreFlusherStub
name|memStoreFlusher
init|=
operator|new
name|MemstoreFlusherStub
argument_list|(
call|(
name|long
call|)
argument_list|(
name|maxHeapSize
operator|*
literal|0.4
argument_list|)
argument_list|)
decl_stmt|;
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
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MAX_RANGE_KEY
argument_list|,
literal|0.7f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MIN_RANGE_KEY
argument_list|,
literal|0.1f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MAX_RANGE_KEY
argument_list|,
literal|0.7f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MIN_RANGE_KEY
argument_list|,
literal|0.1f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HeapMemoryManager
operator|.
name|HBASE_RS_HEAP_MEMORY_TUNER_PERIOD
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
name|HeapMemoryManager
operator|.
name|HBASE_RS_HEAP_MEMORY_TUNER_CLASS
argument_list|,
name|CustomHeapMemoryTuner
operator|.
name|class
argument_list|,
name|HeapMemoryTuner
operator|.
name|class
argument_list|)
expr_stmt|;
name|HeapMemoryManager
name|heapMemoryManager
init|=
operator|new
name|HeapMemoryManager
argument_list|(
name|blockCache
argument_list|,
name|memStoreFlusher
argument_list|,
operator|new
name|RegionServerStub
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|oldMemstoreSize
init|=
name|memStoreFlusher
operator|.
name|memstoreSize
decl_stmt|;
name|long
name|oldBlockCacheSize
init|=
name|blockCache
operator|.
name|maxSize
decl_stmt|;
name|heapMemoryManager
operator|.
name|start
argument_list|()
expr_stmt|;
name|CustomHeapMemoryTuner
operator|.
name|memstoreSize
operator|=
literal|0.7f
expr_stmt|;
name|CustomHeapMemoryTuner
operator|.
name|blockCacheSize
operator|=
literal|0.3f
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1500
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|oldMemstoreSize
argument_list|,
name|memStoreFlusher
operator|.
name|memstoreSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|oldBlockCacheSize
argument_list|,
name|blockCache
operator|.
name|maxSize
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertHeapSpace
parameter_list|(
name|float
name|expectedHeapPercentage
parameter_list|,
name|long
name|currentHeapSpace
parameter_list|)
block|{
name|long
name|expected
init|=
call|(
name|long
call|)
argument_list|(
name|this
operator|.
name|maxHeapSize
operator|*
name|expectedHeapPercentage
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|currentHeapSpace
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertHeapSpaceDelta
parameter_list|(
name|float
name|expectedDeltaPercent
parameter_list|,
name|long
name|oldHeapSpace
parameter_list|,
name|long
name|newHeapSpace
parameter_list|)
block|{
name|long
name|expctedMinDelta
init|=
call|(
name|long
call|)
argument_list|(
name|this
operator|.
name|maxHeapSize
operator|*
name|expectedDeltaPercent
argument_list|)
decl_stmt|;
if|if
condition|(
name|expectedDeltaPercent
operator|>
literal|0
condition|)
block|{
name|assertTrue
argument_list|(
name|expctedMinDelta
operator|<=
operator|(
name|newHeapSpace
operator|-
name|oldHeapSpace
operator|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
name|expctedMinDelta
operator|<=
operator|(
name|oldHeapSpace
operator|-
name|newHeapSpace
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|BlockCacheStub
implements|implements
name|ResizableBlockCache
block|{
name|CacheStats
name|stats
init|=
operator|new
name|CacheStats
argument_list|()
decl_stmt|;
name|long
name|maxSize
init|=
literal|0
decl_stmt|;
specifier|public
name|BlockCacheStub
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|this
operator|.
name|maxSize
operator|=
name|size
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cacheBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|Cacheable
name|buf
parameter_list|,
name|boolean
name|inMemory
parameter_list|,
name|boolean
name|cacheDataInL1
parameter_list|)
block|{      }
annotation|@
name|Override
specifier|public
name|void
name|cacheBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|Cacheable
name|buf
parameter_list|)
block|{      }
annotation|@
name|Override
specifier|public
name|Cacheable
name|getBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|boolean
name|caching
parameter_list|,
name|boolean
name|repeat
parameter_list|,
name|boolean
name|updateCacheMetrics
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|evictBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|)
block|{
name|stats
operator|.
name|evicted
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|evictBlocksByHfileName
parameter_list|(
name|String
name|hfileName
parameter_list|)
block|{
name|stats
operator|.
name|evicted
argument_list|()
expr_stmt|;
comment|// Just assuming only one block for file here.
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|CacheStats
name|getStats
parameter_list|()
block|{
return|return
name|this
operator|.
name|stats
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
block|{      }
annotation|@
name|Override
specifier|public
name|long
name|size
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getFreeSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCurrentSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCount
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setMaxSize
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|this
operator|.
name|maxSize
operator|=
name|size
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|CachedBlock
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|BlockCache
index|[]
name|getBlockCaches
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|MemstoreFlusherStub
implements|implements
name|FlushRequester
block|{
name|long
name|memstoreSize
decl_stmt|;
name|FlushRequestListener
name|listener
decl_stmt|;
name|FlushType
name|flushType
init|=
name|FlushType
operator|.
name|NORMAL
decl_stmt|;
specifier|public
name|MemstoreFlusherStub
parameter_list|(
name|long
name|memstoreSize
parameter_list|)
block|{
name|this
operator|.
name|memstoreSize
operator|=
name|memstoreSize
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|requestFlush
parameter_list|(
name|HRegion
name|region
parameter_list|)
block|{
name|this
operator|.
name|listener
operator|.
name|flushRequested
argument_list|(
name|flushType
argument_list|,
name|region
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|requestDelayedFlush
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|long
name|delay
parameter_list|)
block|{      }
annotation|@
name|Override
specifier|public
name|void
name|registerFlushRequestListener
parameter_list|(
name|FlushRequestListener
name|listener
parameter_list|)
block|{
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|unregisterFlushRequestListener
parameter_list|(
name|FlushRequestListener
name|listener
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setGlobalMemstoreLimit
parameter_list|(
name|long
name|globalMemStoreSize
parameter_list|)
block|{
name|this
operator|.
name|memstoreSize
operator|=
name|globalMemStoreSize
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|RegionServerStub
implements|implements
name|Server
block|{
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
specifier|public
name|RegionServerStub
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{      }
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
name|this
operator|.
name|stopped
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|this
operator|.
name|stopped
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|this
operator|.
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZooKeeperWatcher
name|getZooKeeper
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CoordinatedStateManager
name|getCoordinatedStateManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CatalogTracker
name|getCatalogTracker
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"server1"
argument_list|,
literal|4000
argument_list|,
literal|12345
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|CustomHeapMemoryTuner
implements|implements
name|HeapMemoryTuner
block|{
specifier|static
name|float
name|blockCacheSize
init|=
literal|0.4f
decl_stmt|;
specifier|static
name|float
name|memstoreSize
init|=
literal|0.4f
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|arg0
parameter_list|)
block|{      }
annotation|@
name|Override
specifier|public
name|TunerResult
name|tune
parameter_list|(
name|TunerContext
name|context
parameter_list|)
block|{
name|TunerResult
name|result
init|=
operator|new
name|TunerResult
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|result
operator|.
name|setBlockCacheSize
argument_list|(
name|blockCacheSize
argument_list|)
expr_stmt|;
name|result
operator|.
name|setMemstoreSize
argument_list|(
name|memstoreSize
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
block|}
end_class

end_unit

