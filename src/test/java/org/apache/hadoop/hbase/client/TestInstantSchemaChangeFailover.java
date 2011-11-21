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
name|LargeTests
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
name|zookeeper
operator|.
name|MasterSchemaChangeTracker
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
name|ZKUtil
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
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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
name|Before
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
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestInstantSchemaChangeFailover
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|HBaseAdmin
name|admin
decl_stmt|;
specifier|private
specifier|static
name|MiniHBaseCluster
name|miniHBaseCluster
init|=
literal|null
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|ZooKeeperWatcher
name|zkw
decl_stmt|;
specifier|private
specifier|static
name|MasterSchemaChangeTracker
name|msct
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
annotation|@
name|Before
specifier|public
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
literal|"hbase.regionserver.msginterval"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|250
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.online.schema.update.enable"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.instant.schema.alter.enabled"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.instant.schema.janitor.period"
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.instant.schema.alter.timeout"
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
comment|//
name|miniHBaseCluster
operator|=
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|msct
operator|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getSchemaChangeTracker
argument_list|()
expr_stmt|;
name|this
operator|.
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDownAfterClass
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
comment|/**    * This a pretty low cost signalling mechanism. It is quite possible that we will    * miss out the ZK node creation signal as in some cases the schema change process    * happens rather quickly and our thread waiting for ZK node creation might wait forver.    * The fool-proof strategy would be to directly listen for ZK events.    * @param tableName    * @throws KeeperException    * @throws InterruptedException    */
specifier|private
name|void
name|waitForSchemaChangeProcess
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for ZK node creation for table = "
operator|+
name|tableName
argument_list|)
expr_stmt|;
specifier|final
name|MasterSchemaChangeTracker
name|msct
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getSchemaChangeTracker
argument_list|()
decl_stmt|;
specifier|final
name|Runnable
name|r
init|=
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
while|while
condition|(
operator|!
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|20
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|ke
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for ZK node deletion for table = "
operator|+
name|tableName
argument_list|)
expr_stmt|;
try|try
block|{
while|while
condition|(
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|20
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|ke
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|t
operator|.
name|join
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
block|}
comment|/**    * Kill a random RS and see that the schema change can succeed.    * @throws IOException    * @throws KeeperException    * @throws InterruptedException    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|50000
argument_list|)
specifier|public
name|void
name|testInstantSchemaChangeWhileRSCrash
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testInstantSchemaChangeWhileRSCrash()"
argument_list|)
expr_stmt|;
name|zkw
operator|=
name|miniHBaseCluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getZooKeeperWatcher
argument_list|()
expr_stmt|;
specifier|final
name|String
name|tableName
init|=
literal|"TestRSCrashDuringSchemaChange"
decl_stmt|;
name|HTable
name|ht
init|=
name|createTableAndValidate
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"family2"
argument_list|)
decl_stmt|;
name|admin
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
name|miniHBaseCluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|abort
argument_list|(
literal|"Killing while instant schema change"
argument_list|)
expr_stmt|;
comment|// Let the dust settle down
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|waitForSchemaChangeProcess
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|Put
name|put2
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put2
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|put2
argument_list|)
expr_stmt|;
name|Get
name|get2
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get2
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
name|Result
name|r2
init|=
name|ht
operator|.
name|get
argument_list|(
name|get2
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tvalue2
init|=
name|r2
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
name|int
name|result2
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|value
argument_list|,
name|tvalue2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|result2
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|String
name|nodePath
init|=
name|msct
operator|.
name|getSchemaChangeNodePathForTable
argument_list|(
literal|"TestRSCrashDuringSchemaChange"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|zkw
argument_list|,
name|nodePath
argument_list|)
operator|==
operator|-
literal|1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"result2 = "
operator|+
name|result2
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"end testInstantSchemaChangeWhileRSCrash()"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Randomly bring down/up RS servers while schema change is in progress. This test    * is same as the above one but the only difference is that we intent to kill and start    * new RS instances while a schema change is in progress.    * @throws IOException    * @throws KeeperException    * @throws InterruptedException    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|70000
argument_list|)
specifier|public
name|void
name|testInstantSchemaChangeWhileRandomRSCrashAndStart
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testInstantSchemaChangeWhileRandomRSCrashAndStart()"
argument_list|)
expr_stmt|;
name|miniHBaseCluster
operator|.
name|getRegionServer
argument_list|(
literal|4
argument_list|)
operator|.
name|abort
argument_list|(
literal|"Killing RS 4"
argument_list|)
expr_stmt|;
comment|// Start a new RS before schema change .
comment|// Commenting the start RS as it is failing with DFS user permission NPE.
comment|//miniHBaseCluster.startRegionServer();
comment|// Let the dust settle
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
specifier|final
name|String
name|tableName
init|=
literal|"testInstantSchemaChangeWhileRandomRSCrashAndStart"
decl_stmt|;
name|HTable
name|ht
init|=
name|createTableAndValidate
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"family2"
argument_list|)
decl_stmt|;
name|admin
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
comment|// Kill 2 RS now.
name|miniHBaseCluster
operator|.
name|getRegionServer
argument_list|(
literal|2
argument_list|)
operator|.
name|abort
argument_list|(
literal|"Killing RS 2"
argument_list|)
expr_stmt|;
comment|// Let the dust settle
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
comment|// We will be left with only one RS.
name|waitForSchemaChangeProcess
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|Put
name|put2
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put2
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|put2
argument_list|)
expr_stmt|;
name|Get
name|get2
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get2
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
name|Result
name|r2
init|=
name|ht
operator|.
name|get
argument_list|(
name|get2
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tvalue2
init|=
name|r2
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
name|int
name|result2
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|value
argument_list|,
name|tvalue2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|result2
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"result2 = "
operator|+
name|result2
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"end testInstantSchemaChangeWhileRandomRSCrashAndStart()"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test scenario where primary master is brought down while processing an    * alter request. This is harder one as it is very difficult the time this.    * @throws IOException    * @throws KeeperException    * @throws InterruptedException    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|50000
argument_list|)
specifier|public
name|void
name|testInstantSchemaChangeWhileMasterFailover
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testInstantSchemaChangeWhileMasterFailover()"
argument_list|)
expr_stmt|;
comment|//Thread.sleep(5000);
specifier|final
name|String
name|tableName
init|=
literal|"testInstantSchemaChangeWhileMasterFailover"
decl_stmt|;
name|HTable
name|ht
init|=
name|createTableAndValidate
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"family2"
argument_list|)
decl_stmt|;
name|admin
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
comment|// Kill primary master now.
name|Thread
operator|.
name|sleep
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|miniHBaseCluster
operator|.
name|getMaster
argument_list|()
operator|.
name|abort
argument_list|(
literal|"Aborting master now"
argument_list|,
operator|new
name|Exception
argument_list|(
literal|"Schema exception"
argument_list|)
argument_list|)
expr_stmt|;
comment|// It may not be possible for us to check the schema change status
comment|// using waitForSchemaChangeProcess as our ZK session in MasterSchemachangeTracker will be
comment|// lost when master dies and hence may not be accurate. So relying on old-fashioned
comment|// sleep here.
name|Thread
operator|.
name|sleep
argument_list|(
literal|25000
argument_list|)
expr_stmt|;
name|Put
name|put2
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put2
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|put2
argument_list|)
expr_stmt|;
name|Get
name|get2
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get2
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
name|Result
name|r2
init|=
name|ht
operator|.
name|get
argument_list|(
name|get2
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tvalue2
init|=
name|r2
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
name|int
name|result2
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|value
argument_list|,
name|tvalue2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|result2
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"result2 = "
operator|+
name|result2
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"end testInstantSchemaChangeWhileMasterFailover()"
argument_list|)
expr_stmt|;
block|}
comment|/**    * TEst the master fail over during a schema change request in ZK.    * We create a fake schema change request in ZK and abort the primary master    * mid-flight to simulate a master fail over scenario during a mid-flight    * schema change process. The new master's schema janitor will eventually    * cleanup this fake request after time out.    * @throws IOException    * @throws KeeperException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testInstantSchemaOperationsInZKForMasterFailover
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testInstantSchemaOperationsInZKForMasterFailover() "
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
literal|"testInstantSchemaOperationsInZKForMasterFailover"
decl_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|MasterSchemaChangeTracker
name|activesct
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getSchemaChangeTracker
argument_list|()
decl_stmt|;
name|activesct
operator|.
name|createSchemaChangeNode
argument_list|(
name|tableName
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|activesct
operator|.
name|getSchemaChangeNodePathForTable
argument_list|(
name|tableName
argument_list|)
operator|+
literal|" created"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|activesct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
comment|// Kill primary master now.
name|miniHBaseCluster
operator|.
name|getMaster
argument_list|()
operator|.
name|abort
argument_list|(
literal|"Aborting master now"
argument_list|,
operator|new
name|Exception
argument_list|(
literal|"Schema exception"
argument_list|)
argument_list|)
expr_stmt|;
comment|// wait for 50 secs. This is so that our schema janitor from fail-over master will kick-in and
comment|// cleanup this failed/expired schema change request.
name|Thread
operator|.
name|sleep
argument_list|(
literal|50000
argument_list|)
expr_stmt|;
name|MasterSchemaChangeTracker
name|newmsct
init|=
name|miniHBaseCluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getSchemaChangeTracker
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|newmsct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|newmsct
operator|.
name|getSchemaChangeNodePathForTable
argument_list|(
name|tableName
argument_list|)
operator|+
literal|" deleted"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"END testInstantSchemaOperationsInZKForMasterFailover() "
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HTable
name|createTableAndValidate
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Start createTableAndValidate()"
argument_list|)
expr_stmt|;
name|HTableDescriptor
index|[]
name|tables
init|=
name|admin
operator|.
name|listTables
argument_list|()
decl_stmt|;
name|int
name|numTables
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|tables
operator|!=
literal|null
condition|)
block|{
name|numTables
operator|=
name|tables
operator|.
name|length
expr_stmt|;
block|}
name|HTable
name|ht
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
name|tables
operator|=
name|this
operator|.
name|admin
operator|.
name|listTables
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|numTables
operator|+
literal|1
argument_list|,
name|tables
operator|.
name|length
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"created table = "
operator|+
name|tableName
argument_list|)
expr_stmt|;
return|return
name|ht
return|;
block|}
block|}
end_class

end_unit

