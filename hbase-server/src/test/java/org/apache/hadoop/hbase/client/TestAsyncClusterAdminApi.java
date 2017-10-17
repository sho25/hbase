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
name|client
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
name|client
operator|.
name|AsyncProcess
operator|.
name|START_LOG_ERRORS_AFTER_COUNT_KEY
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
name|nio
operator|.
name|file
operator|.
name|FileSystems
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|StandardCopyOption
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
name|EnumSet
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
name|ClusterStatus
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
name|ClusterStatus
operator|.
name|Option
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
name|RegionLoad
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
name|ServerLoad
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
name|testclassification
operator|.
name|ClientTests
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
name|wal
operator|.
name|AbstractFSWALProvider
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
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
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
name|TestAsyncClusterAdminApi
extends|extends
name|TestAsyncAdminBase
block|{
specifier|private
specifier|final
name|Path
name|cnfPath
init|=
name|FileSystems
operator|.
name|getDefault
argument_list|()
operator|.
name|getPath
argument_list|(
literal|"target/test-classes/hbase-site.xml"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Path
name|cnf2Path
init|=
name|FileSystems
operator|.
name|getDefault
argument_list|()
operator|.
name|getPath
argument_list|(
literal|"target/test-classes/hbase-site2.xml"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Path
name|cnf3Path
init|=
name|FileSystems
operator|.
name|getDefault
argument_list|()
operator|.
name|getPath
argument_list|(
literal|"target/test-classes/hbase-site3.xml"
argument_list|)
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|MASTER_INFO_PORT
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_TIMEOUT_KEY
argument_list|,
literal|60000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
literal|120000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
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
name|START_LOG_ERRORS_AFTER_COUNT_KEY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|ASYNC_CONN
operator|=
name|ConnectionFactory
operator|.
name|createAsyncConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetMasterInfoPort
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getInfoServer
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|,
operator|(
name|int
operator|)
name|admin
operator|.
name|getMasterInfoPort
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionServerOnlineConfigChange
parameter_list|()
throws|throws
name|Exception
block|{
name|replaceHBaseSiteXML
argument_list|()
expr_stmt|;
name|admin
operator|.
name|getRegionServers
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|forEach
argument_list|(
name|server
lambda|->
name|admin
operator|.
name|updateConfiguration
argument_list|(
name|server
argument_list|)
operator|.
name|join
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check the configuration of the RegionServers
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|forEach
argument_list|(
name|thread
lambda|->
block|{
name|Configuration
name|conf
init|=
name|thread
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.custom.config"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|restoreHBaseSiteXML
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMasterOnlineConfigChange
parameter_list|()
throws|throws
name|Exception
block|{
name|replaceHBaseSiteXML
argument_list|()
expr_stmt|;
name|ServerName
name|master
init|=
name|admin
operator|.
name|getMaster
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|admin
operator|.
name|updateConfiguration
argument_list|(
name|master
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
name|admin
operator|.
name|getBackupMasters
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|forEach
argument_list|(
name|backupMaster
lambda|->
name|admin
operator|.
name|updateConfiguration
argument_list|(
name|backupMaster
argument_list|)
operator|.
name|join
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check the configuration of the Masters
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMasterThreads
argument_list|()
operator|.
name|forEach
argument_list|(
name|thread
lambda|->
block|{
name|Configuration
name|conf
init|=
name|thread
operator|.
name|getMaster
argument_list|()
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.custom.config"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|restoreHBaseSiteXML
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAllClusterOnlineConfigChange
parameter_list|()
throws|throws
name|IOException
block|{
name|replaceHBaseSiteXML
argument_list|()
expr_stmt|;
name|admin
operator|.
name|updateConfiguration
argument_list|()
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// Check the configuration of the Masters
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMasterThreads
argument_list|()
operator|.
name|forEach
argument_list|(
name|thread
lambda|->
block|{
name|Configuration
name|conf
init|=
name|thread
operator|.
name|getMaster
argument_list|()
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.custom.config"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
comment|// Check the configuration of the RegionServers
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|forEach
argument_list|(
name|thread
lambda|->
block|{
name|Configuration
name|conf
init|=
name|thread
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.custom.config"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|restoreHBaseSiteXML
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|replaceHBaseSiteXML
parameter_list|()
throws|throws
name|IOException
block|{
comment|// make a backup of hbase-site.xml
name|Files
operator|.
name|copy
argument_list|(
name|cnfPath
argument_list|,
name|cnf3Path
argument_list|,
name|StandardCopyOption
operator|.
name|REPLACE_EXISTING
argument_list|)
expr_stmt|;
comment|// update hbase-site.xml by overwriting it
name|Files
operator|.
name|copy
argument_list|(
name|cnf2Path
argument_list|,
name|cnfPath
argument_list|,
name|StandardCopyOption
operator|.
name|REPLACE_EXISTING
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|restoreHBaseSiteXML
parameter_list|()
throws|throws
name|IOException
block|{
comment|// restore hbase-site.xml
name|Files
operator|.
name|copy
argument_list|(
name|cnf3Path
argument_list|,
name|cnfPath
argument_list|,
name|StandardCopyOption
operator|.
name|REPLACE_EXISTING
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRollWALWALWriter
parameter_list|()
throws|throws
name|Exception
block|{
name|setUpforLogRolling
argument_list|()
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
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|v
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionServer
name|regionServer
init|=
name|startAndWriteData
argument_list|(
name|tableName
argument_list|,
name|value
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
name|regionServer
operator|.
name|getWAL
argument_list|(
literal|null
argument_list|)
argument_list|)
operator|+
literal|" log files"
argument_list|)
expr_stmt|;
comment|// flush all regions
for|for
control|(
name|HRegion
name|r
range|:
name|regionServer
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
name|admin
operator|.
name|rollWALWriter
argument_list|(
name|regionServer
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
name|int
name|count
init|=
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|regionServer
operator|.
name|getWAL
argument_list|(
literal|null
argument_list|)
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
block|}
specifier|private
name|void
name|setUpforLogRolling
parameter_list|()
block|{
comment|// Force a region split after every 768KB
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.maxlogentries"
argument_list|,
literal|32
argument_list|)
expr_stmt|;
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
comment|// For less frequently updated regions flush after every 2 flushes
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memstore.optionalflushcount"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// We flush the cache after every 8192 bytes
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
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
comment|/**** configuration for testLogRollOnDatanodeDeath ****/
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
literal|30
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.hlog.tolerable.lowreplication"
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
literal|"hbase.regionserver.hlog.lowreplication.rolllimit"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HRegionServer
name|startAndWriteData
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|Exception
block|{
name|createTableWithDefaultConf
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|RawAsyncTable
name|table
init|=
name|ASYNC_CONN
operator|.
name|getRawTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
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
name|FAMILY
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
operator|.
name|join
argument_list|()
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
return|return
name|regionServer
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetRegionLoads
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Turn off the balancer
name|admin
operator|.
name|setBalancerOn
argument_list|(
literal|false
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
name|TableName
index|[]
name|tables
init|=
operator|new
name|TableName
index|[]
block|{
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"1"
argument_list|)
block|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"2"
argument_list|)
block|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"3"
argument_list|)
block|}
decl_stmt|;
name|createAndLoadTable
argument_list|(
name|tables
argument_list|)
expr_stmt|;
comment|// Sleep to wait region server report
name|Thread
operator|.
name|sleep
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.regionserver.msginterval"
argument_list|,
literal|3
operator|*
literal|1000
argument_list|)
operator|*
literal|2
argument_list|)
expr_stmt|;
comment|// Check if regions match with the regionLoad from the server
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|servers
init|=
name|admin
operator|.
name|getRegionServers
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|serverName
range|:
name|servers
control|)
block|{
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
init|=
name|admin
operator|.
name|getOnlineRegions
argument_list|(
name|serverName
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|checkRegionsAndRegionLoads
argument_list|(
name|regions
argument_list|,
name|admin
operator|.
name|getRegionLoads
argument_list|(
name|serverName
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Check if regionLoad matches the table's regions and nothing is missed
for|for
control|(
name|TableName
name|table
range|:
name|tables
control|)
block|{
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|tableRegions
init|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|table
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RegionLoad
argument_list|>
name|regionLoads
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|serverName
range|:
name|servers
control|)
block|{
name|regionLoads
operator|.
name|addAll
argument_list|(
name|admin
operator|.
name|getRegionLoads
argument_list|(
name|serverName
argument_list|,
name|Optional
operator|.
name|of
argument_list|(
name|table
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|checkRegionsAndRegionLoads
argument_list|(
name|tableRegions
argument_list|,
name|regionLoads
argument_list|)
expr_stmt|;
block|}
comment|// Check RegionLoad matches the regionLoad from ClusterStatus
name|ClusterStatus
name|clusterStatus
init|=
name|admin
operator|.
name|getClusterStatus
argument_list|(
name|EnumSet
operator|.
name|of
argument_list|(
name|Option
operator|.
name|LIVE_SERVERS
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|serverName
range|:
name|clusterStatus
operator|.
name|getServers
argument_list|()
control|)
block|{
name|ServerLoad
name|serverLoad
init|=
name|clusterStatus
operator|.
name|getLoad
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
name|compareRegionLoads
argument_list|(
name|serverLoad
operator|.
name|getRegionsLoad
argument_list|()
operator|.
name|values
argument_list|()
argument_list|,
name|admin
operator|.
name|getRegionLoads
argument_list|(
name|serverName
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|compareRegionLoads
parameter_list|(
name|Collection
argument_list|<
name|RegionLoad
argument_list|>
name|regionLoadCluster
parameter_list|,
name|Collection
argument_list|<
name|RegionLoad
argument_list|>
name|regionLoads
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"No of regionLoads from clusterStatus and regionloads from RS doesn't match"
argument_list|,
name|regionLoadCluster
operator|.
name|size
argument_list|()
argument_list|,
name|regionLoads
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionLoad
name|loadCluster
range|:
name|regionLoadCluster
control|)
block|{
name|boolean
name|matched
init|=
literal|false
decl_stmt|;
for|for
control|(
name|RegionLoad
name|load
range|:
name|regionLoads
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|loadCluster
operator|.
name|getName
argument_list|()
argument_list|,
name|load
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|matched
operator|=
literal|true
expr_stmt|;
continue|continue;
block|}
block|}
name|assertTrue
argument_list|(
literal|"The contents of region load from cluster and server should match"
argument_list|,
name|matched
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|checkRegionsAndRegionLoads
parameter_list|(
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
name|regions
parameter_list|,
name|Collection
argument_list|<
name|RegionLoad
argument_list|>
name|regionLoads
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"No of regions and regionloads doesn't match"
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|,
name|regionLoads
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionLoad
argument_list|>
name|regionLoadMap
init|=
name|Maps
operator|.
name|newTreeMap
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|RegionLoad
name|regionLoad
range|:
name|regionLoads
control|)
block|{
name|regionLoadMap
operator|.
name|put
argument_list|(
name|regionLoad
operator|.
name|getName
argument_list|()
argument_list|,
name|regionLoad
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|RegionInfo
name|info
range|:
name|regions
control|)
block|{
name|assertTrue
argument_list|(
literal|"Region not in regionLoadMap region:"
operator|+
name|info
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" regionMap: "
operator|+
name|regionLoadMap
argument_list|,
name|regionLoadMap
operator|.
name|containsKey
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|createAndLoadTable
parameter_list|(
name|TableName
index|[]
name|tables
parameter_list|)
block|{
for|for
control|(
name|TableName
name|table
range|:
name|tables
control|)
block|{
name|TableDescriptorBuilder
name|builder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|builder
operator|.
name|addColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaaaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzzzz"
argument_list|)
argument_list|,
literal|16
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
name|RawAsyncTable
name|asyncTable
init|=
name|ASYNC_CONN
operator|.
name|getRawTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|row
range|:
name|HBaseTestingUtility
operator|.
name|ROWS
control|)
block|{
name|puts
operator|.
name|add
argument_list|(
operator|new
name|Put
argument_list|(
name|row
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|asyncTable
operator|.
name|putAll
argument_list|(
name|puts
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

