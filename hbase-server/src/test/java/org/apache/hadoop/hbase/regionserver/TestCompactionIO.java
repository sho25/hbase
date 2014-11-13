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
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseTestingUtility
operator|.
name|START_KEY
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseTestingUtility
operator|.
name|fam1
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
name|concurrent
operator|.
name|CountDownLatch
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
name|FileStatus
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
name|Durability
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
name|Put
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
name|compactions
operator|.
name|DefaultCompactor
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

begin_comment
comment|/**  * Test compaction IO cancellation.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestCompactionIO
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
name|HBaseTestingUtility
operator|.
name|createLocalHTU
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|/**    * verify that a compaction stuck in IO is aborted when we attempt to close a region    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testInterruptCompactionIO
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|STARTROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|START_KEY
argument_list|)
decl_stmt|;
name|byte
index|[]
name|COLUMN_FAMILY
init|=
name|fam1
decl_stmt|;
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HREGION_MEMSTORE_FLUSH_SIZE
argument_list|,
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memstore.block.multiplier"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|DefaultStoreEngine
operator|.
name|DEFAULT_COMPACTOR_CLASS_KEY
argument_list|,
name|BlockedCompactor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|compactionThreshold
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|3
argument_list|)
decl_stmt|;
specifier|final
name|HRegion
name|r
init|=
name|UTIL
operator|.
name|createLocalHRegion
argument_list|(
name|UTIL
operator|.
name|createTableDescriptor
argument_list|(
literal|"TestCompactionIO"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|//Create a couple store files w/ 15KB (over 10KB interval)
name|int
name|jmax
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
literal|15.0
operator|/
name|compactionThreshold
argument_list|)
decl_stmt|;
name|byte
index|[]
name|pad
init|=
operator|new
name|byte
index|[
literal|1000
index|]
decl_stmt|;
comment|// 1 KB chunk
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|compactionThreshold
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|add
argument_list|(
name|STARTROW
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|jmax
condition|;
name|j
operator|++
control|)
block|{
name|p
operator|.
name|add
argument_list|(
name|COLUMN_FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|j
argument_list|)
argument_list|,
name|pad
argument_list|)
expr_stmt|;
block|}
name|UTIL
operator|.
name|loadRegion
argument_list|(
name|r
argument_list|,
name|COLUMN_FAMILY
argument_list|)
expr_stmt|;
name|r
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|r
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
operator|new
name|Thread
argument_list|(
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
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|x
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|x
argument_list|)
throw|;
block|}
block|}
block|}
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// hangs
name|r
operator|.
name|compactStores
argument_list|()
expr_stmt|;
comment|// ensure that the compaction stopped, all old files are intact,
name|Store
name|s
init|=
name|r
operator|.
name|stores
operator|.
name|get
argument_list|(
name|COLUMN_FAMILY
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|compactionThreshold
argument_list|,
name|s
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|s
operator|.
name|getStorefilesSize
argument_list|()
operator|>
literal|15
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// and no new store files persisted past compactStores()
name|FileStatus
index|[]
name|ls
init|=
name|r
operator|.
name|getFilesystem
argument_list|()
operator|.
name|listStatus
argument_list|(
name|r
operator|.
name|getRegionFileSystem
argument_list|()
operator|.
name|getTempDir
argument_list|()
argument_list|)
decl_stmt|;
comment|// this is happening after the compaction start, the DefaultCompactor does not
comment|// clean tmp files when it encounters an IOException. Should it?
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ls
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|BlockedCompactor
extends|extends
name|DefaultCompactor
block|{
specifier|public
name|BlockedCompactor
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Store
name|store
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|store
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|performCompaction
parameter_list|(
name|InternalScanner
name|scanner
parameter_list|,
name|CellSink
name|writer
parameter_list|,
name|long
name|smallestReadPoint
parameter_list|,
name|boolean
name|cleanSeqId
parameter_list|)
throws|throws
name|IOException
block|{
name|CellSink
name|myWriter
init|=
operator|new
name|CellSink
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
name|ie
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
decl_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
return|return
name|super
operator|.
name|performCompaction
argument_list|(
name|scanner
argument_list|,
name|myWriter
argument_list|,
name|smallestReadPoint
argument_list|,
name|cleanSeqId
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

