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
name|util
operator|.
name|Collection
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
name|FileSystem
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
name|client
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
name|snapshot
operator|.
name|SnapshotTestingUtils
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
name|FSUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|Assert
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
name|TestCompactSplitThread
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
name|TestCompactSplitThread
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_RS
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|blockingStoreFiles
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
name|Path
name|rootDir
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
comment|/**    * Setup the config for the cluster    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|setupConf
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|NUM_RS
argument_list|)
expr_stmt|;
name|fs
operator|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
name|rootDir
operator|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// disable the ui
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionsever.info.port"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// so make sure we get a compaction when doing a load, but keep around some
comment|// files in the store
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compaction.min"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
comment|// change the flush size to a small amount, regulating number of store files
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memstore.flush.size"
argument_list|,
literal|25000
argument_list|)
expr_stmt|;
comment|// block writes if we get to blockingStoreFiles store files
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.blockingStoreFiles"
argument_list|,
name|blockingStoreFiles
argument_list|)
expr_stmt|;
comment|// Ensure no extra cleaners on by default (e.g. TimeToLiveHFileCleaner)
name|conf
operator|.
name|setInt
argument_list|(
name|CompactSplit
operator|.
name|LARGE_COMPACTION_THREADS
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|CompactSplit
operator|.
name|SMALL_COMPACTION_THREADS
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|CompactSplit
operator|.
name|SPLIT_THREADS
argument_list|,
literal|5
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|cleanupTest
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// NOOP;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testThreadPoolSizeTuning
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setCompactionEnabled
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|HRegionServer
name|regionServer
init|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// check initial configuration of thread pool sizes
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|getLargeCompactionThreadNum
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|getSmallCompactionThreadNum
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|getSplitThreadNum
argument_list|()
argument_list|)
expr_stmt|;
comment|// change bigger configurations and do online update
name|conf
operator|.
name|setInt
argument_list|(
name|CompactSplit
operator|.
name|LARGE_COMPACTION_THREADS
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|CompactSplit
operator|.
name|SMALL_COMPACTION_THREADS
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|CompactSplit
operator|.
name|SPLIT_THREADS
argument_list|,
literal|6
argument_list|)
expr_stmt|;
try|try
block|{
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|onConfigurationChange
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Update bigger configuration failed!"
argument_list|)
expr_stmt|;
block|}
comment|// check again after online update
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|getLargeCompactionThreadNum
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|getSmallCompactionThreadNum
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|getSplitThreadNum
argument_list|()
argument_list|)
expr_stmt|;
comment|// change smaller configurations and do online update
name|conf
operator|.
name|setInt
argument_list|(
name|CompactSplit
operator|.
name|LARGE_COMPACTION_THREADS
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|CompactSplit
operator|.
name|SMALL_COMPACTION_THREADS
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|CompactSplit
operator|.
name|SPLIT_THREADS
argument_list|,
literal|4
argument_list|)
expr_stmt|;
try|try
block|{
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|onConfigurationChange
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Update smaller configuration failed!"
argument_list|)
expr_stmt|;
block|}
comment|// check again after online update
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|getLargeCompactionThreadNum
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|getSmallCompactionThreadNum
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|getSplitThreadNum
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testFlushWithTableCompactionDisabled
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|htd
operator|.
name|setCompactionEnabled
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|family
block|}
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// load the table
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|blockingStoreFiles
operator|+
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|family
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|// Make sure that store file number is greater than blockingStoreFiles + 1
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|hfiles
init|=
name|SnapshotTestingUtils
operator|.
name|listHFileNames
argument_list|(
name|fs
argument_list|,
name|tableDir
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|hfiles
operator|.
name|size
argument_list|()
operator|>
name|blockingStoreFiles
operator|+
literal|1
operator|)
assert|;
block|}
block|}
end_class

end_unit

