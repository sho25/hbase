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
operator|.
name|wal
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|NavigableMap
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
name|HColumnDescriptor
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|MiniHBaseCluster
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
name|TableName
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
name|Admin
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
name|client
operator|.
name|Table
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
name|MultiVersionConcurrencyControl
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
name|FSUtils
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
name|wal
operator|.
name|AbstractFSWALProvider
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
name|wal
operator|.
name|WAL
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
name|wal
operator|.
name|WALEdit
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
name|wal
operator|.
name|WALFactory
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
name|wal
operator|.
name|WALKeyImpl
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
name|wal
operator|.
name|WALSplitter
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
name|hdfs
operator|.
name|MiniDFSCluster
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
name|Assert
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

begin_comment
comment|/**  * Tests for conditions that should trigger RegionServer aborts when  * rolling the current WAL fails.  */
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
name|TestLogRollAbort
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
name|AbstractTestLogRolling
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|MiniDFSCluster
name|dfsCluster
decl_stmt|;
specifier|private
specifier|static
name|Admin
name|admin
decl_stmt|;
specifier|private
specifier|static
name|MiniHBaseCluster
name|cluster
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|/* For the split-then-roll test */
specifier|private
specifier|static
specifier|final
name|Path
name|HBASEDIR
init|=
operator|new
name|Path
argument_list|(
literal|"/hbase"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Path
name|HBASELOGDIR
init|=
operator|new
name|Path
argument_list|(
literal|"/hbaselog"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Path
name|OLDLOGDIR
init|=
operator|new
name|Path
argument_list|(
name|HBASELOGDIR
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|)
decl_stmt|;
comment|// Need to override this setup so we can edit the config before it gets sent
comment|// to the HDFS& HBase cluster startup.
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
comment|// Tweak default timeout values down for faster recovery
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.logroll.errors.tolerated"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.rpc.timeout"
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// Increase the amount of time between client retries
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|5
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// lower the namenode& datanode heartbeat so the namenode
comment|// quickly detects datanode failures
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.namenode.heartbeat.recheck-interval"
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.heartbeat.interval"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// the namenode might still try to choose the recently-dead datanode
comment|// for a pipeline, so try to a new pipeline multiple times
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.client.block.write.retries"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|WALFactory
operator|.
name|WAL_PROVIDER
argument_list|,
literal|"filesystem"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|cluster
operator|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
expr_stmt|;
name|dfsCluster
operator|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
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
comment|// disable region rebalancing (interferes with log watching)
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|balanceSwitch
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|setRootDir
argument_list|(
name|conf
argument_list|,
name|HBASEDIR
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|setWALRootDir
argument_list|(
name|conf
argument_list|,
name|HBASELOGDIR
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
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Tests that RegionServer aborts if we hit an error closing the WAL when    * there are unsynced WAL edits.  See HBASE-4282.    */
annotation|@
name|Test
specifier|public
name|void
name|testRSAbortWithUnflushedEdits
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting testRSAbortWithUnflushedEdits()"
argument_list|)
expr_stmt|;
comment|// When the hbase:meta table can be opened, the region servers are running
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Create the test table and open it
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|HRegionServer
name|server
init|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|WAL
name|log
init|=
name|server
operator|.
name|getWAL
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2001"
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|2001
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|log
operator|.
name|sync
argument_list|()
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2002"
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|2002
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|dfsCluster
operator|.
name|restartDataNodes
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Restarted datanodes"
argument_list|)
expr_stmt|;
try|try
block|{
name|log
operator|.
name|rollWriter
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FailedLogCloseException
name|flce
parameter_list|)
block|{
comment|// Expected exception.  We used to expect that there would be unsynced appends but this
comment|// not reliable now that sync plays a roll in wall rolling.  The above puts also now call
comment|// sync.
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"FAILED TEST: Got wrong exception"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Tests the case where a RegionServer enters a GC pause,    * comes back online after the master declared it dead and started to split.    * Want log rolling after a master split to fail. See HBASE-2312.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testLogRollAfterSplitStart
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Verify wal roll after split starts will fail."
argument_list|)
expr_stmt|;
name|String
name|logName
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"testLogRollAfterSplitStart"
argument_list|,
literal|16010
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|Path
name|thisTestsDir
init|=
operator|new
name|Path
argument_list|(
name|HBASELOGDIR
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getWALDirectoryName
argument_list|(
name|logName
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|WALFactory
name|wals
init|=
operator|new
name|WALFactory
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|,
name|logName
argument_list|)
decl_stmt|;
try|try
block|{
comment|// put some entries in an WAL
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionInfo
name|regioninfo
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
decl_stmt|;
specifier|final
name|WAL
name|log
init|=
name|wals
operator|.
name|getWAL
argument_list|(
name|regioninfo
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|regioninfo
operator|.
name|getTable
argument_list|()
operator|.
name|getNamespace
argument_list|()
argument_list|)
decl_stmt|;
name|MultiVersionConcurrencyControl
name|mvcc
init|=
operator|new
name|MultiVersionConcurrencyControl
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|int
name|total
init|=
literal|20
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
name|total
condition|;
name|i
operator|++
control|)
block|{
name|WALEdit
name|kvs
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|kvs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
name|tableName
operator|.
name|getName
argument_list|()
argument_list|,
name|tableName
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
literal|"column"
argument_list|)
argument_list|)
expr_stmt|;
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scopes
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
for|for
control|(
name|byte
index|[]
name|fam
range|:
name|htd
operator|.
name|getFamiliesKeys
argument_list|()
control|)
block|{
name|scopes
operator|.
name|put
argument_list|(
name|fam
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
name|log
operator|.
name|append
argument_list|(
name|regioninfo
argument_list|,
operator|new
name|WALKeyImpl
argument_list|(
name|regioninfo
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|mvcc
argument_list|,
name|scopes
argument_list|)
argument_list|,
name|kvs
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Send the data to HDFS datanodes and close the HDFS writer
name|log
operator|.
name|sync
argument_list|()
expr_stmt|;
operator|(
operator|(
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
operator|)
name|log
operator|)
operator|.
name|replaceWriter
argument_list|(
operator|(
operator|(
name|FSHLog
operator|)
name|log
operator|)
operator|.
name|getOldPath
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|/* code taken from MasterFileSystem.getLogDirs(), which is called from MasterFileSystem.splitLog()        * handles RS shutdowns (as observed by the splitting process)        */
comment|// rename the directory so a rogue RS doesn't create more WALs
name|Path
name|rsSplitDir
init|=
name|thisTestsDir
operator|.
name|suffix
argument_list|(
name|AbstractFSWALProvider
operator|.
name|SPLITTING_EXT
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|thisTestsDir
argument_list|,
name|rsSplitDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed fs.rename for log split: "
operator|+
name|thisTestsDir
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Renamed region directory: "
operator|+
name|rsSplitDir
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Processing the old log files."
argument_list|)
expr_stmt|;
name|WALSplitter
operator|.
name|split
argument_list|(
name|HBASELOGDIR
argument_list|,
name|rsSplitDir
argument_list|,
name|OLDLOGDIR
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|wals
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Trying to roll the WAL."
argument_list|)
expr_stmt|;
try|try
block|{
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"rollWriter() did not throw any exception."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
if|if
condition|(
name|ioe
operator|.
name|getCause
argument_list|()
operator|instanceof
name|FileNotFoundException
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got the expected exception: "
argument_list|,
name|ioe
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Unexpected exception: "
operator|+
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|wals
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|thisTestsDir
argument_list|)
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|thisTestsDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

