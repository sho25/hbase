begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertNotNull
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
name|assertNull
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
name|HBaseAdmin
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
name|HConnection
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
name|HConnectionManager
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
name|HTable
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
name|ZKConfig
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
name|CreateMode
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
name|apache
operator|.
name|zookeeper
operator|.
name|ZooDefs
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
name|ZooKeeper
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
name|ZooKeeper
operator|.
name|States
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

begin_class
specifier|public
class|class
name|TestZooKeeper
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
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
comment|/**    * @throws java.lang.Exception    */
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
comment|// Test we can first start the ZK cluster by itself
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"dfs.support.append"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
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
name|ensureSomeRegionServersAvailable
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**    * See HBASE-1232 and http://wiki.apache.org/hadoop/ZooKeeper/FAQ#4.    * @throws IOException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testClientSessionExpired
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testClientSessionExpired"
argument_list|)
expr_stmt|;
name|Configuration
name|c
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
operator|new
name|HTable
argument_list|(
name|c
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|String
name|quorumServers
init|=
name|ZKConfig
operator|.
name|getZKQuorumServersString
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|int
name|sessionTimeout
init|=
literal|5
operator|*
literal|1000
decl_stmt|;
comment|// 5 seconds
name|HConnection
name|connection
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|ZooKeeperWatcher
name|connectionZK
init|=
name|connection
operator|.
name|getZooKeeperWatcher
argument_list|()
decl_stmt|;
name|long
name|sessionID
init|=
name|connectionZK
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getSessionId
argument_list|()
decl_stmt|;
name|byte
index|[]
name|password
init|=
name|connectionZK
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getSessionPasswd
argument_list|()
decl_stmt|;
name|ZooKeeper
name|zk
init|=
operator|new
name|ZooKeeper
argument_list|(
name|quorumServers
argument_list|,
name|sessionTimeout
argument_list|,
name|EmptyWatcher
operator|.
name|instance
argument_list|,
name|sessionID
argument_list|,
name|password
argument_list|)
decl_stmt|;
name|zk
operator|.
name|close
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|sessionTimeout
operator|*
literal|3L
argument_list|)
expr_stmt|;
comment|// provoke session expiration by doing something with ZK
name|ZKUtil
operator|.
name|dump
argument_list|(
name|connectionZK
argument_list|)
expr_stmt|;
comment|// Check that the old ZK conenction is closed, means we did expire
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"ZooKeeper should have timed out"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"state="
operator|+
name|connectionZK
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|connectionZK
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getState
argument_list|()
operator|.
name|equals
argument_list|(
name|States
operator|.
name|CLOSED
argument_list|)
argument_list|)
expr_stmt|;
comment|// Check that the client recovered
name|ZooKeeperWatcher
name|newConnectionZK
init|=
name|connection
operator|.
name|getZooKeeperWatcher
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"state="
operator|+
name|newConnectionZK
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|newConnectionZK
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getState
argument_list|()
operator|.
name|equals
argument_list|(
name|States
operator|.
name|CONNECTED
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionServerSessionExpired
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting testRegionServerSessionExpired"
argument_list|)
expr_stmt|;
name|int
name|metaIndex
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getServerWithMeta
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|expireRegionServerSession
argument_list|(
name|metaIndex
argument_list|)
expr_stmt|;
name|testSanity
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMasterSessionExpired
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting testMasterSessionExpired"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|expireMasterSession
argument_list|()
expr_stmt|;
name|testSanity
argument_list|()
expr_stmt|;
block|}
comment|/**    * Make sure we can use the cluster    * @throws Exception    */
specifier|public
name|void
name|testSanity
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|tableName
init|=
literal|"test"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
name|HColumnDescriptor
name|family
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|family
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
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
literal|"testrow"
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
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
literal|"testdata"
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Putting table "
operator|+
name|tableName
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
annotation|@
name|Test
specifier|public
name|void
name|testMultipleZK
parameter_list|()
block|{
try|try
block|{
name|HTable
name|localMeta
init|=
operator|new
name|HTable
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|Configuration
name|otherConf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|otherConf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|,
literal|"127.0.0.1"
argument_list|)
expr_stmt|;
name|HTable
name|ipMeta
init|=
operator|new
name|HTable
argument_list|(
name|otherConf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
comment|// dummy, just to open the connection
name|localMeta
operator|.
name|exists
argument_list|(
operator|new
name|Get
argument_list|(
name|HConstants
operator|.
name|LAST_ROW
argument_list|)
argument_list|)
expr_stmt|;
name|ipMeta
operator|.
name|exists
argument_list|(
operator|new
name|Get
argument_list|(
name|HConstants
operator|.
name|LAST_ROW
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure they aren't the same
name|assertFalse
argument_list|(
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|localMeta
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|getZooKeeperWatcher
argument_list|()
operator|==
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|otherConf
argument_list|)
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|localMeta
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|getZooKeeperWatcher
argument_list|()
operator|.
name|getQuorum
argument_list|()
operator|.
name|equals
argument_list|(
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|otherConf
argument_list|)
operator|.
name|getZooKeeperWatcher
argument_list|()
operator|.
name|getQuorum
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Create a bunch of znodes in a hierarchy, try deleting one that has childs    * (it will fail), then delete it recursively, then delete the last znode    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testZNodeDeletes
parameter_list|()
throws|throws
name|Exception
block|{
name|ZooKeeperWatcher
name|zkw
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|TestZooKeeper
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
literal|"/l1/l2/l3/l4"
argument_list|)
expr_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zkw
argument_list|,
literal|"/l1/l2"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"We should not be able to delete if znode has childs"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ex
parameter_list|)
block|{
name|assertNotNull
argument_list|(
name|ZKUtil
operator|.
name|getDataNoWatch
argument_list|(
name|zkw
argument_list|,
literal|"/l1/l2/l3/l4"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
literal|"/l1/l2"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|ZKUtil
operator|.
name|getDataNoWatch
argument_list|(
name|zkw
argument_list|,
literal|"/l1/l2/l3/l4"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zkw
argument_list|,
literal|"/l1"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|ZKUtil
operator|.
name|getDataNoWatch
argument_list|(
name|zkw
argument_list|,
literal|"/l1/l2"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClusterKey
parameter_list|()
throws|throws
name|Exception
block|{
name|testKey
argument_list|(
literal|"server"
argument_list|,
literal|"2181"
argument_list|,
literal|"hbase"
argument_list|)
expr_stmt|;
name|testKey
argument_list|(
literal|"server1,server2,server3"
argument_list|,
literal|"2181"
argument_list|,
literal|"hbase"
argument_list|)
expr_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|transformClusterKey
argument_list|(
literal|"2181:hbase"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|// OK
block|}
block|}
specifier|private
name|void
name|testKey
parameter_list|(
name|String
name|ensemble
parameter_list|,
name|String
name|port
parameter_list|,
name|String
name|znode
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|String
name|key
init|=
name|ensemble
operator|+
literal|":"
operator|+
name|port
operator|+
literal|":"
operator|+
name|znode
decl_stmt|;
name|String
index|[]
name|parts
init|=
name|ZKUtil
operator|.
name|transformClusterKey
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ensemble
argument_list|,
name|parts
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|port
argument_list|,
name|parts
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|znode
argument_list|,
name|parts
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|applyClusterKeyToConf
argument_list|(
name|conf
argument_list|,
name|key
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|parts
index|[
literal|1
index|]
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.zookeeper.property.clientPort"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|parts
index|[
literal|2
index|]
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|reconstructedKey
init|=
name|ZKUtil
operator|.
name|getZooKeeperClusterKey
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|key
argument_list|,
name|reconstructedKey
argument_list|)
expr_stmt|;
block|}
comment|/**    * A test for HBASE-3238    * @throws IOException A connection attempt to zk failed    * @throws InterruptedException One of the non ZKUtil actions was interrupted    * @throws KeeperException Any of the zookeeper connections had a    * KeeperException    */
annotation|@
name|Test
specifier|public
name|void
name|testCreateSilentIsReallySilent
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|KeeperException
throws|,
name|IOException
block|{
name|Configuration
name|c
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|String
name|aclZnode
init|=
literal|"/aclRoot"
decl_stmt|;
name|String
name|quorumServers
init|=
name|ZKConfig
operator|.
name|getZKQuorumServersString
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|int
name|sessionTimeout
init|=
literal|5
operator|*
literal|1000
decl_stmt|;
comment|// 5 seconds
name|ZooKeeper
name|zk
init|=
operator|new
name|ZooKeeper
argument_list|(
name|quorumServers
argument_list|,
name|sessionTimeout
argument_list|,
name|EmptyWatcher
operator|.
name|instance
argument_list|)
decl_stmt|;
name|zk
operator|.
name|addAuthInfo
argument_list|(
literal|"digest"
argument_list|,
literal|"hbase:rox"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
comment|// Assumes the  root of the ZooKeeper space is writable as it creates a node
comment|// wherever the cluster home is defined.
name|ZooKeeperWatcher
name|zk2
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"testMasterAddressManagerFromZK"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// I set this acl after the attempted creation of the cluster home node.
name|zk
operator|.
name|setACL
argument_list|(
literal|"/"
argument_list|,
name|ZooDefs
operator|.
name|Ids
operator|.
name|CREATOR_ALL_ACL
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|zk
operator|.
name|create
argument_list|(
name|aclZnode
argument_list|,
literal|null
argument_list|,
name|ZooDefs
operator|.
name|Ids
operator|.
name|CREATOR_ALL_ACL
argument_list|,
name|CreateMode
operator|.
name|PERSISTENT
argument_list|)
expr_stmt|;
name|zk
operator|.
name|close
argument_list|()
expr_stmt|;
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|zk2
argument_list|,
name|aclZnode
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

