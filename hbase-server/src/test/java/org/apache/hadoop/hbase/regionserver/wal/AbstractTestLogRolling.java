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
name|wal
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
name|ColumnFamilyDescriptorBuilder
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
name|Get
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
name|Result
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
name|client
operator|.
name|TableDescriptorBuilder
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
name|Store
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
name|Rule
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
name|rules
operator|.
name|TestName
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
comment|/**  * Test log deletion as logs are rolled.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractTestLogRolling
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
name|AbstractTestLogRolling
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|HRegionServer
name|server
decl_stmt|;
specifier|protected
name|String
name|tableName
decl_stmt|;
specifier|protected
name|byte
index|[]
name|value
decl_stmt|;
specifier|protected
name|FileSystem
name|fs
decl_stmt|;
specifier|protected
name|MiniDFSCluster
name|dfsCluster
decl_stmt|;
specifier|protected
name|Admin
name|admin
decl_stmt|;
specifier|protected
name|MiniHBaseCluster
name|cluster
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|public
name|AbstractTestLogRolling
parameter_list|()
block|{
name|this
operator|.
name|server
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
literal|null
expr_stmt|;
name|String
name|className
init|=
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|StringBuilder
name|v
init|=
operator|new
name|StringBuilder
argument_list|(
name|className
argument_list|)
decl_stmt|;
while|while
condition|(
name|v
operator|.
name|length
argument_list|()
operator|<
literal|1000
condition|)
block|{
name|v
operator|.
name|append
argument_list|(
name|className
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|value
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|v
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
comment|/**** configuration for testLogRolling ****/
comment|// Force a region split after every 768KB
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HREGION_MAX_FILESIZE
argument_list|,
literal|768L
operator|*
literal|1024L
argument_list|)
expr_stmt|;
comment|// We roll the log after every 32 writes
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.maxlogentries"
argument_list|,
literal|32
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.logroll.errors.tolerated"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|conf
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
comment|// For less frequently updated regions flush after every 2 flushes
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memstore.optionalflushcount"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// We flush the cache after every 8192 bytes
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HREGION_MEMSTORE_FLUSH_SIZE
argument_list|,
literal|8192
argument_list|)
expr_stmt|;
comment|// Increase the amount of time between client retries
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// Reduce thread wake frequency so that other threads can get
comment|// a chance to run.
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|2
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// disable low replication check for log roller to get a more stable result
comment|// TestWALOpenAfterDNRollingStart will test this option.
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.regionserver.hlog.check.lowreplication.interval"
argument_list|,
literal|24L
operator|*
literal|60
operator|*
literal|60
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
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
literal|1
argument_list|,
literal|1
argument_list|,
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
name|fs
operator|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getAdmin
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
specifier|protected
name|void
name|startAndWriteData
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
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
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServer
argument_list|()
expr_stmt|;
name|Table
name|table
init|=
name|createTestTable
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
decl_stmt|;
name|server
operator|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
literal|256
condition|;
name|i
operator|++
control|)
block|{
comment|// 256 writes should cause 8 log rolls
name|doPut
argument_list|(
name|table
argument_list|,
name|i
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|%
literal|32
operator|==
literal|0
condition|)
block|{
comment|// After every 32 writes sleep to let the log roller run
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
block|}
block|}
comment|/**    * Tests that log rolling doesn't hang when no data is written.    */
annotation|@
name|Test
specifier|public
name|void
name|testLogRollOnNothingWritten
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
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
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"test.com"
argument_list|,
literal|8080
argument_list|,
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|WAL
name|newLog
init|=
name|wals
operator|.
name|getWAL
argument_list|(
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Now roll the log before we write anything.
name|newLog
operator|.
name|rollWriter
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|wals
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|assertLogFileSize
parameter_list|(
name|WAL
name|log
parameter_list|)
block|{
if|if
condition|(
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|log
argument_list|)
operator|>
literal|0
condition|)
block|{
name|assertTrue
argument_list|(
name|AbstractFSWALProvider
operator|.
name|getLogFileSize
argument_list|(
name|log
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getLogFileSize
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Tests that logs are deleted    */
annotation|@
name|Test
specifier|public
name|void
name|testLogRolling
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|tableName
operator|=
name|getName
argument_list|()
expr_stmt|;
comment|// TODO: Why does this write data take for ever?
name|startAndWriteData
argument_list|()
expr_stmt|;
name|RegionInfo
name|region
init|=
name|server
operator|.
name|getRegions
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
specifier|final
name|WAL
name|log
init|=
name|server
operator|.
name|getWAL
argument_list|(
name|region
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"after writing there are "
operator|+
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|log
argument_list|)
operator|+
literal|" log files"
argument_list|)
expr_stmt|;
name|assertLogFileSize
argument_list|(
name|log
argument_list|)
expr_stmt|;
comment|// flush all regions
for|for
control|(
name|HRegion
name|r
range|:
name|server
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
control|)
block|{
name|r
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Now roll the log
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|int
name|count
init|=
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|log
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"after flushing all regions and rolling logs there are "
operator|+
name|count
operator|+
literal|" log files"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
literal|"actual count: "
operator|+
name|count
operator|)
argument_list|,
name|count
operator|<=
literal|2
argument_list|)
expr_stmt|;
name|assertLogFileSize
argument_list|(
name|log
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"TestLogRolling-"
operator|+
name|name
operator|.
name|getMethodName
argument_list|()
return|;
block|}
name|void
name|writeData
parameter_list|(
name|Table
name|table
parameter_list|,
name|int
name|rownum
parameter_list|)
throws|throws
name|IOException
block|{
name|doPut
argument_list|(
name|table
argument_list|,
name|rownum
argument_list|)
expr_stmt|;
comment|// sleep to let the log roller run (if it needs to)
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
name|void
name|validateData
parameter_list|(
name|Table
name|table
parameter_list|,
name|int
name|rownum
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|row
init|=
literal|"row"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$04d"
argument_list|,
name|rownum
argument_list|)
decl_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|)
decl_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|value
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Validated row "
operator|+
name|row
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests that logs are deleted when some region has a compaction    * record in WAL and no other records. See HBASE-8597.    */
annotation|@
name|Test
specifier|public
name|void
name|testCompactionRecordDoesntBlockRolling
parameter_list|()
throws|throws
name|Exception
block|{
name|Table
name|table
init|=
literal|null
decl_stmt|;
comment|// When the hbase:meta table can be opened, the region servers are running
name|Table
name|t
init|=
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
decl_stmt|;
try|try
block|{
name|table
operator|=
name|createTestTable
argument_list|(
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|server
operator|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|HRegion
name|region
init|=
name|server
operator|.
name|getRegions
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|WAL
name|log
init|=
name|server
operator|.
name|getWAL
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
name|Store
name|s
init|=
name|region
operator|.
name|getStore
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
comment|//have to flush namespace to ensure it doesn't affect wall tests
name|admin
operator|.
name|flush
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|)
expr_stmt|;
comment|// Put some stuff into table, to make sure we have some files to compact.
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
literal|2
condition|;
operator|++
name|i
control|)
block|{
name|doPut
argument_list|(
name|table
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|doPut
argument_list|(
name|table
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// don't flush yet, or compaction might trigger before we roll WAL
name|assertEquals
argument_list|(
literal|"Should have no WAL after initial writes"
argument_list|,
literal|0
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|s
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// Roll the log and compact table, to have compaction record in the 2nd WAL.
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Should have WAL; one table is not flushed"
argument_list|,
literal|1
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|region
operator|.
name|compact
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Wait for compaction in case if flush triggered it before us.
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|s
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|waitTime
init|=
literal|3000
init|;
name|s
operator|.
name|getStorefilesCount
argument_list|()
operator|>
literal|1
operator|&&
name|waitTime
operator|>
literal|0
condition|;
name|waitTime
operator|-=
literal|200
control|)
block|{
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|200
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Compaction didn't happen"
argument_list|,
literal|1
argument_list|,
name|s
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// Write some value to the table so the WAL cannot be deleted until table is flushed.
name|doPut
argument_list|(
name|table
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// Now 2nd WAL will have both compaction and put record for table.
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
comment|// 1st WAL deleted, 2nd not deleted yet.
name|assertEquals
argument_list|(
literal|"Should have WAL; one table is not flushed"
argument_list|,
literal|1
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
comment|// Flush table to make latest WAL obsolete; write another record, and roll again.
name|admin
operator|.
name|flush
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|doPut
argument_list|(
name|table
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
comment|// Now 2nd WAL is deleted and 3rd is added.
name|assertEquals
argument_list|(
literal|"Should have 1 WALs at the end"
argument_list|,
literal|1
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|t
operator|!=
literal|null
condition|)
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|doPut
parameter_list|(
name|Table
name|table
parameter_list|,
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$04d"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|null
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|Table
name|createTestTable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Create the test table and open it
name|TableDescriptor
name|desc
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|getName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
return|return
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
return|;
block|}
block|}
end_class

end_unit

