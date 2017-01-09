begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|KeyValue
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
name|KeyValueUtil
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
name|exceptions
operator|.
name|UnexpectedStateException
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
name|util
operator|.
name|MemorySizeUtil
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
name|testclassification
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|List
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

begin_comment
comment|/**  * Test the {@link MemStoreChunkPool} class  */
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMemStoreChunkPool
block|{
specifier|private
specifier|final
specifier|static
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|MemStoreChunkPool
name|chunkPool
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|chunkPoolDisabledBeforeTest
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
name|MemStoreLAB
operator|.
name|USEMSLAB_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|MemStoreLAB
operator|.
name|CHUNK_POOL_MAXSIZE_KEY
argument_list|,
literal|0.2f
argument_list|)
expr_stmt|;
name|chunkPoolDisabledBeforeTest
operator|=
name|MemStoreChunkPool
operator|.
name|chunkPoolDisabled
expr_stmt|;
name|MemStoreChunkPool
operator|.
name|chunkPoolDisabled
operator|=
literal|false
expr_stmt|;
name|long
name|globalMemStoreLimit
init|=
call|(
name|long
call|)
argument_list|(
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
operator|*
name|MemorySizeUtil
operator|.
name|getGlobalMemStoreHeapPercent
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|chunkPool
operator|=
name|MemStoreChunkPool
operator|.
name|initialize
argument_list|(
name|globalMemStoreLimit
argument_list|,
literal|0.2f
argument_list|,
name|MemStoreLAB
operator|.
name|POOL_INITIAL_SIZE_DEFAULT
argument_list|,
name|MemStoreLABImpl
operator|.
name|CHUNK_SIZE_DEFAULT
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|chunkPool
operator|!=
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|MemStoreChunkPool
operator|.
name|chunkPoolDisabled
operator|=
name|chunkPoolDisabledBeforeTest
expr_stmt|;
block|}
annotation|@
name|Before
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
name|Test
specifier|public
name|void
name|testReusingChunks
parameter_list|()
block|{
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|MemStoreLAB
name|mslab
init|=
operator|new
name|MemStoreLABImpl
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|int
name|expectedOff
init|=
literal|0
decl_stmt|;
name|byte
index|[]
name|lastBuffer
init|=
literal|null
decl_stmt|;
specifier|final
name|byte
index|[]
name|rk
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|cf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|q
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
comment|// Randomly allocate some bytes
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|int
name|valSize
init|=
name|rand
operator|.
name|nextInt
argument_list|(
literal|1000
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|rk
argument_list|,
name|cf
argument_list|,
name|q
argument_list|,
operator|new
name|byte
index|[
name|valSize
index|]
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|KeyValueUtil
operator|.
name|length
argument_list|(
name|kv
argument_list|)
decl_stmt|;
name|KeyValue
name|newKv
init|=
operator|(
name|KeyValue
operator|)
name|mslab
operator|.
name|copyCellInto
argument_list|(
name|kv
argument_list|)
decl_stmt|;
if|if
condition|(
name|newKv
operator|.
name|getBuffer
argument_list|()
operator|!=
name|lastBuffer
condition|)
block|{
name|expectedOff
operator|=
literal|0
expr_stmt|;
name|lastBuffer
operator|=
name|newKv
operator|.
name|getBuffer
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expectedOff
argument_list|,
name|newKv
operator|.
name|getOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Allocation overruns buffer"
argument_list|,
name|newKv
operator|.
name|getOffset
argument_list|()
operator|+
name|size
operator|<=
name|newKv
operator|.
name|getBuffer
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|expectedOff
operator|+=
name|size
expr_stmt|;
block|}
comment|// chunks will be put back to pool after close
name|mslab
operator|.
name|close
argument_list|()
expr_stmt|;
name|int
name|chunkCount
init|=
name|chunkPool
operator|.
name|getPoolSize
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|chunkCount
operator|>
literal|0
argument_list|)
expr_stmt|;
comment|// reconstruct mslab
name|mslab
operator|=
operator|new
name|MemStoreLABImpl
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// chunk should be got from the pool, so we can reuse it.
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|rk
argument_list|,
name|cf
argument_list|,
name|q
argument_list|,
operator|new
name|byte
index|[
literal|10
index|]
argument_list|)
decl_stmt|;
name|mslab
operator|.
name|copyCellInto
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|chunkCount
operator|-
literal|1
argument_list|,
name|chunkPool
operator|.
name|getPoolSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPuttingBackChunksAfterFlushing
parameter_list|()
throws|throws
name|UnexpectedStateException
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testrow"
argument_list|)
decl_stmt|;
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
name|qf1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testqualifier1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testqualifier2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testqualifier3"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf4
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testqualifier4"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf5
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testqualifier5"
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
literal|"testval"
argument_list|)
decl_stmt|;
name|DefaultMemStore
name|memstore
init|=
operator|new
name|DefaultMemStore
argument_list|()
decl_stmt|;
comment|// Setting up memstore
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf1
argument_list|,
name|val
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf2
argument_list|,
name|val
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf3
argument_list|,
name|val
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Creating a snapshot
name|MemStoreSnapshot
name|snapshot
init|=
name|memstore
operator|.
name|snapshot
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
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
comment|// Adding value to "new" memstore
name|assertEquals
argument_list|(
literal|0
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
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf4
argument_list|,
name|val
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf5
argument_list|,
name|val
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
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
comment|// close the scanner - this is how the snapshot will be used
name|snapshot
operator|.
name|getScanner
argument_list|()
operator|.
name|close
argument_list|()
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
name|int
name|chunkCount
init|=
name|chunkPool
operator|.
name|getPoolSize
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|chunkCount
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPuttingBackChunksWithOpeningScanner
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testrow"
argument_list|)
decl_stmt|;
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
name|qf1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testqualifier1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testqualifier2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testqualifier3"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf4
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testqualifier4"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf5
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testqualifier5"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf6
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testqualifier6"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf7
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testqualifier7"
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
literal|"testval"
argument_list|)
decl_stmt|;
name|DefaultMemStore
name|memstore
init|=
operator|new
name|DefaultMemStore
argument_list|()
decl_stmt|;
comment|// Setting up memstore
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf1
argument_list|,
name|val
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf2
argument_list|,
name|val
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf3
argument_list|,
name|val
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Creating a snapshot
name|MemStoreSnapshot
name|snapshot
init|=
name|memstore
operator|.
name|snapshot
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
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
comment|// Adding value to "new" memstore
name|assertEquals
argument_list|(
literal|0
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
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf4
argument_list|,
name|val
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf5
argument_list|,
name|val
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
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
comment|// opening scanner before clear the snapshot
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
literal|0
argument_list|)
decl_stmt|;
comment|// Shouldn't putting back the chunks to pool,since some scanners are opening
comment|// based on their data
comment|// close the snapshot scanner
name|snapshot
operator|.
name|getScanner
argument_list|()
operator|.
name|close
argument_list|()
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
name|assertTrue
argument_list|(
name|chunkPool
operator|.
name|getPoolSize
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
comment|// Chunks will be put back to pool after close scanners;
for|for
control|(
name|KeyValueScanner
name|scanner
range|:
name|scanners
control|)
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|chunkPool
operator|.
name|getPoolSize
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
comment|// clear chunks
name|chunkPool
operator|.
name|clearChunks
argument_list|()
expr_stmt|;
comment|// Creating another snapshot
name|snapshot
operator|=
name|memstore
operator|.
name|snapshot
argument_list|()
expr_stmt|;
comment|// Adding more value
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf6
argument_list|,
name|val
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf7
argument_list|,
name|val
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// opening scanners
name|scanners
operator|=
name|memstore
operator|.
name|getScanners
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// close scanners before clear the snapshot
for|for
control|(
name|KeyValueScanner
name|scanner
range|:
name|scanners
control|)
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Since no opening scanner, the chunks of snapshot should be put back to
comment|// pool
comment|// close the snapshot scanner
name|snapshot
operator|.
name|getScanner
argument_list|()
operator|.
name|close
argument_list|()
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
name|assertTrue
argument_list|(
name|chunkPool
operator|.
name|getPoolSize
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPutbackChunksMultiThreaded
parameter_list|()
throws|throws
name|Exception
block|{
name|MemStoreChunkPool
name|oldPool
init|=
name|MemStoreChunkPool
operator|.
name|GLOBAL_INSTANCE
decl_stmt|;
specifier|final
name|int
name|maxCount
init|=
literal|10
decl_stmt|;
specifier|final
name|int
name|initialCount
init|=
literal|5
decl_stmt|;
specifier|final
name|int
name|chunkSize
init|=
literal|30
decl_stmt|;
specifier|final
name|int
name|valSize
init|=
literal|7
decl_stmt|;
name|MemStoreChunkPool
name|pool
init|=
operator|new
name|MemStoreChunkPool
argument_list|(
name|chunkSize
argument_list|,
name|maxCount
argument_list|,
name|initialCount
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|initialCount
argument_list|,
name|pool
operator|.
name|getPoolSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|maxCount
argument_list|,
name|pool
operator|.
name|getMaxCount
argument_list|()
argument_list|)
expr_stmt|;
name|MemStoreChunkPool
operator|.
name|GLOBAL_INSTANCE
operator|=
name|pool
expr_stmt|;
comment|// Replace the global ref with the new one we created.
comment|// Used it for the testing. Later in finally we put
comment|// back the original
specifier|final
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|,
operator|new
name|byte
index|[
name|valSize
index|]
argument_list|)
decl_stmt|;
try|try
block|{
name|Runnable
name|r
init|=
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|MemStoreLAB
name|memStoreLAB
init|=
operator|new
name|MemStoreLABImpl
argument_list|(
name|conf
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
name|maxCount
condition|;
name|i
operator|++
control|)
block|{
name|memStoreLAB
operator|.
name|copyCellInto
argument_list|(
name|kv
argument_list|)
expr_stmt|;
comment|// Try allocate size = chunkSize. Means every
comment|// allocate call will result in a new chunk
block|}
comment|// Close MemStoreLAB so that all chunks will be tried to be put back to pool
name|memStoreLAB
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|Thread
name|t1
init|=
operator|new
name|Thread
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|Thread
name|t2
init|=
operator|new
name|Thread
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|Thread
name|t3
init|=
operator|new
name|Thread
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|t1
operator|.
name|start
argument_list|()
expr_stmt|;
name|t2
operator|.
name|start
argument_list|()
expr_stmt|;
name|t3
operator|.
name|start
argument_list|()
expr_stmt|;
name|t1
operator|.
name|join
argument_list|()
expr_stmt|;
name|t2
operator|.
name|join
argument_list|()
expr_stmt|;
name|t3
operator|.
name|join
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|pool
operator|.
name|getPoolSize
argument_list|()
operator|<=
name|maxCount
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|MemStoreChunkPool
operator|.
name|GLOBAL_INSTANCE
operator|=
name|oldPool
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

