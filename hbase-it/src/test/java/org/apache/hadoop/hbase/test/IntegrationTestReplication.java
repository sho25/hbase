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
name|test
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
name|conf
operator|.
name|Configured
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
name|HRegionLocation
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
name|IntegrationTestingUtility
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
name|Connection
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
name|ConnectionFactory
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
name|replication
operator|.
name|ReplicationPeerConfig
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
name|replication
operator|.
name|ReplicationPeerDescription
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
name|util
operator|.
name|Tool
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
name|util
operator|.
name|ToolRunner
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
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
import|;
end_import

begin_comment
comment|/**  * This is an integration test for replication. It is derived off  * {@link org.apache.hadoop.hbase.test.IntegrationTestBigLinkedList} that creates a large circular  * linked list in one cluster and verifies that the data is correct in a sink cluster. The test  * handles creating the tables and schema and setting up the replication.  */
end_comment

begin_class
specifier|public
class|class
name|IntegrationTestReplication
extends|extends
name|IntegrationTestBigLinkedList
block|{
specifier|protected
name|String
name|sourceClusterIdString
decl_stmt|;
specifier|protected
name|String
name|sinkClusterIdString
decl_stmt|;
specifier|protected
name|int
name|numIterations
decl_stmt|;
specifier|protected
name|int
name|numMappers
decl_stmt|;
specifier|protected
name|long
name|numNodes
decl_stmt|;
specifier|protected
name|String
name|outputDir
decl_stmt|;
specifier|protected
name|int
name|numReducers
decl_stmt|;
specifier|protected
name|int
name|generateVerifyGap
decl_stmt|;
specifier|protected
name|Integer
name|width
decl_stmt|;
specifier|protected
name|Integer
name|wrapMultiplier
decl_stmt|;
specifier|protected
name|boolean
name|noReplicationSetup
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|String
name|SOURCE_CLUSTER_OPT
init|=
literal|"sourceCluster"
decl_stmt|;
specifier|private
specifier|final
name|String
name|DEST_CLUSTER_OPT
init|=
literal|"destCluster"
decl_stmt|;
specifier|private
specifier|final
name|String
name|ITERATIONS_OPT
init|=
literal|"iterations"
decl_stmt|;
specifier|private
specifier|final
name|String
name|NUM_MAPPERS_OPT
init|=
literal|"numMappers"
decl_stmt|;
specifier|private
specifier|final
name|String
name|OUTPUT_DIR_OPT
init|=
literal|"outputDir"
decl_stmt|;
specifier|private
specifier|final
name|String
name|NUM_REDUCERS_OPT
init|=
literal|"numReducers"
decl_stmt|;
specifier|private
specifier|final
name|String
name|NO_REPLICATION_SETUP_OPT
init|=
literal|"noReplicationSetup"
decl_stmt|;
comment|/**    * The gap (in seconds) from when data is finished being generated at the source    * to when it can be verified. This is the replication lag we are willing to tolerate    */
specifier|private
specifier|final
name|String
name|GENERATE_VERIFY_GAP_OPT
init|=
literal|"generateVerifyGap"
decl_stmt|;
comment|/**    * The width of the linked list.    * See {@link org.apache.hadoop.hbase.test.IntegrationTestBigLinkedList} for more details    */
specifier|private
specifier|final
name|String
name|WIDTH_OPT
init|=
literal|"width"
decl_stmt|;
comment|/**    * The number of rows after which the linked list points to the first row.    * See {@link org.apache.hadoop.hbase.test.IntegrationTestBigLinkedList} for more details    */
specifier|private
specifier|final
name|String
name|WRAP_MULTIPLIER_OPT
init|=
literal|"wrapMultiplier"
decl_stmt|;
comment|/**    * The number of nodes in the test setup. This has to be a multiple of WRAP_MULTIPLIER * WIDTH    * in order to ensure that the linked list can is complete.    * See {@link org.apache.hadoop.hbase.test.IntegrationTestBigLinkedList} for more details    */
specifier|private
specifier|final
name|String
name|NUM_NODES_OPT
init|=
literal|"numNodes"
decl_stmt|;
specifier|private
specifier|final
name|int
name|DEFAULT_NUM_MAPPERS
init|=
literal|1
decl_stmt|;
specifier|private
specifier|final
name|int
name|DEFAULT_NUM_REDUCERS
init|=
literal|1
decl_stmt|;
specifier|private
specifier|final
name|int
name|DEFAULT_NUM_ITERATIONS
init|=
literal|1
decl_stmt|;
specifier|private
specifier|final
name|int
name|DEFAULT_GENERATE_VERIFY_GAP
init|=
literal|60
decl_stmt|;
specifier|private
specifier|final
name|int
name|DEFAULT_WIDTH
init|=
literal|1000000
decl_stmt|;
specifier|private
specifier|final
name|int
name|DEFAULT_WRAP_MULTIPLIER
init|=
literal|25
decl_stmt|;
specifier|private
specifier|final
name|int
name|DEFAULT_NUM_NODES
init|=
name|DEFAULT_WIDTH
operator|*
name|DEFAULT_WRAP_MULTIPLIER
decl_stmt|;
comment|/**    * Wrapper around an HBase ClusterID allowing us    * to get admin connections and configurations for it    */
specifier|protected
class|class
name|ClusterID
block|{
specifier|private
specifier|final
name|Configuration
name|configuration
decl_stmt|;
specifier|private
name|Connection
name|connection
init|=
literal|null
decl_stmt|;
comment|/**      * This creates a new ClusterID wrapper that will automatically build connections and      * configurations to be able to talk to the specified cluster      *      * @param base the base configuration that this class will add to      * @param key the cluster key in the form of zk_quorum:zk_port:zk_parent_node      */
specifier|public
name|ClusterID
parameter_list|(
name|Configuration
name|base
parameter_list|,
name|String
name|key
parameter_list|)
block|{
name|configuration
operator|=
operator|new
name|Configuration
argument_list|(
name|base
argument_list|)
expr_stmt|;
name|String
index|[]
name|parts
init|=
name|key
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
name|configuration
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|,
name|parts
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|,
name|parts
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
name|parts
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|Joiner
operator|.
name|on
argument_list|(
literal|":"
argument_list|)
operator|.
name|join
argument_list|(
name|configuration
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|)
argument_list|,
name|configuration
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|)
argument_list|,
name|configuration
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|this
operator|.
name|configuration
return|;
block|}
specifier|public
name|Connection
name|getConnection
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|this
operator|.
name|connection
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|this
operator|.
name|configuration
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|connection
return|;
block|}
specifier|public
name|void
name|closeConnection
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|connection
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|boolean
name|equals
parameter_list|(
name|ClusterID
name|other
parameter_list|)
block|{
return|return
name|this
operator|.
name|toString
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|other
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
comment|/**    * The main runner loop for the test. It uses    * {@link org.apache.hadoop.hbase.test.IntegrationTestBigLinkedList}    * for the generation and verification of the linked list. It is heavily based on    * {@link org.apache.hadoop.hbase.test.IntegrationTestBigLinkedList.Loop}    */
specifier|protected
class|class
name|VerifyReplicationLoop
extends|extends
name|Configured
implements|implements
name|Tool
block|{
specifier|private
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|VerifyReplicationLoop
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|ClusterID
name|source
decl_stmt|;
specifier|protected
name|ClusterID
name|sink
decl_stmt|;
name|IntegrationTestBigLinkedList
name|integrationTestBigLinkedList
decl_stmt|;
comment|/**      * This tears down any tables that existed from before and rebuilds the tables and schemas on      * the source cluster. It then sets up replication from the source to the sink cluster by using      * the {@link org.apache.hadoop.hbase.client.replication.ReplicationAdmin}      * connection.      *      * @throws Exception      */
specifier|protected
name|void
name|setupTablesAndReplication
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|getTableName
argument_list|(
name|source
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|ClusterID
index|[]
name|clusters
init|=
block|{
name|source
block|,
name|sink
block|}
decl_stmt|;
comment|// delete any old tables in the source and sink
for|for
control|(
name|ClusterID
name|cluster
range|:
name|clusters
control|)
block|{
name|Admin
name|admin
init|=
name|cluster
operator|.
name|getConnection
argument_list|()
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
if|if
condition|(
name|admin
operator|.
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|/**            * TODO: This is a work around on a replication bug (HBASE-13416)            * When we recreate a table against that has recently been            * deleted, the contents of the logs are replayed even though            * they should not. This ensures that we flush the logs            * before the table gets deleted. Eventually the bug should be            * fixed and this should be removed.            */
name|Set
argument_list|<
name|ServerName
argument_list|>
name|regionServers
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionLocation
name|rl
range|:
name|cluster
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
operator|.
name|getAllRegionLocations
argument_list|()
control|)
block|{
name|regionServers
operator|.
name|add
argument_list|(
name|rl
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ServerName
name|server
range|:
name|regionServers
control|)
block|{
name|source
operator|.
name|getConnection
argument_list|()
operator|.
name|getAdmin
argument_list|()
operator|.
name|rollWALWriter
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
comment|// create the schema
name|Generator
name|generator
init|=
operator|new
name|Generator
argument_list|()
decl_stmt|;
name|generator
operator|.
name|setConf
argument_list|(
name|source
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|generator
operator|.
name|createSchema
argument_list|()
expr_stmt|;
comment|// setup the replication on the source
if|if
condition|(
operator|!
name|source
operator|.
name|equals
argument_list|(
name|sink
argument_list|)
condition|)
block|{
try|try
init|(
specifier|final
name|Admin
name|admin
init|=
name|source
operator|.
name|getConnection
argument_list|()
operator|.
name|getAdmin
argument_list|()
init|)
block|{
comment|// remove any old replication peers
for|for
control|(
name|ReplicationPeerDescription
name|peer
range|:
name|admin
operator|.
name|listReplicationPeers
argument_list|()
control|)
block|{
name|admin
operator|.
name|removeReplicationPeer
argument_list|(
name|peer
operator|.
name|getPeerId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// set the test table to be the table to replicate
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|toReplicate
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|toReplicate
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
comment|// set the sink to be the target
specifier|final
name|ReplicationPeerConfig
name|peerConfig
init|=
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|sink
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|setReplicateAllUserTables
argument_list|(
literal|false
argument_list|)
operator|.
name|setTableCFsMap
argument_list|(
name|toReplicate
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|addReplicationPeer
argument_list|(
literal|"TestPeer"
argument_list|,
name|peerConfig
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTableReplication
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|ClusterID
name|cluster
range|:
name|clusters
control|)
block|{
name|cluster
operator|.
name|closeConnection
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|waitForReplication
parameter_list|()
throws|throws
name|Exception
block|{
comment|// TODO: we shouldn't be sleeping here. It would be better to query the region servers
comment|// and wait for them to report 0 replication lag.
name|Thread
operator|.
name|sleep
argument_list|(
name|generateVerifyGap
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
comment|/**      * Run the {@link org.apache.hadoop.hbase.test.IntegrationTestBigLinkedList.Generator} in the      * source cluster. This assumes that the tables have been setup via setupTablesAndReplication.      *      * @throws Exception      */
specifier|protected
name|void
name|runGenerator
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|outputPath
init|=
operator|new
name|Path
argument_list|(
name|outputDir
argument_list|)
decl_stmt|;
name|UUID
name|uuid
init|=
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
comment|//create a random UUID.
name|Path
name|generatorOutput
init|=
operator|new
name|Path
argument_list|(
name|outputPath
argument_list|,
name|uuid
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|Generator
name|generator
init|=
operator|new
name|Generator
argument_list|()
decl_stmt|;
name|generator
operator|.
name|setConf
argument_list|(
name|source
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
comment|// Disable concurrent walkers for IntegrationTestReplication
name|int
name|retCode
init|=
name|generator
operator|.
name|run
argument_list|(
name|numMappers
argument_list|,
name|numNodes
argument_list|,
name|generatorOutput
argument_list|,
name|width
argument_list|,
name|wrapMultiplier
argument_list|,
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|retCode
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Generator failed with return code: "
operator|+
name|retCode
argument_list|)
throw|;
block|}
block|}
comment|/**      * Run the {@link org.apache.hadoop.hbase.test.IntegrationTestBigLinkedList.Verify}      * in the sink cluster. If replication is working properly the data written at the source      * cluster should be available in the sink cluster after a reasonable gap      *      * @param expectedNumNodes the number of nodes we are expecting to see in the sink cluster      * @throws Exception      */
specifier|protected
name|void
name|runVerify
parameter_list|(
name|long
name|expectedNumNodes
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|outputPath
init|=
operator|new
name|Path
argument_list|(
name|outputDir
argument_list|)
decl_stmt|;
name|UUID
name|uuid
init|=
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
comment|//create a random UUID.
name|Path
name|iterationOutput
init|=
operator|new
name|Path
argument_list|(
name|outputPath
argument_list|,
name|uuid
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|Verify
name|verify
init|=
operator|new
name|Verify
argument_list|()
decl_stmt|;
name|verify
operator|.
name|setConf
argument_list|(
name|sink
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|retCode
init|=
name|verify
operator|.
name|run
argument_list|(
name|iterationOutput
argument_list|,
name|numReducers
argument_list|)
decl_stmt|;
if|if
condition|(
name|retCode
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Verify.run failed with return code: "
operator|+
name|retCode
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|verify
operator|.
name|verify
argument_list|(
name|expectedNumNodes
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Verify.verify failed"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Verify finished with success. Total nodes="
operator|+
name|expectedNumNodes
argument_list|)
expr_stmt|;
block|}
comment|/**      * The main test runner      *      * This test has 4 steps:      *  1: setupTablesAndReplication      *  2: generate the data into the source cluster      *  3: wait for replication to propagate      *  4: verify that the data is available in the sink cluster      *      * @param args should be empty      * @return 0 on success      * @throws Exception on an error      */
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|source
operator|=
operator|new
name|ClusterID
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|sourceClusterIdString
argument_list|)
expr_stmt|;
name|sink
operator|=
operator|new
name|ClusterID
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|sinkClusterIdString
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|noReplicationSetup
condition|)
block|{
name|setupTablesAndReplication
argument_list|()
expr_stmt|;
block|}
name|int
name|expectedNumNodes
init|=
literal|0
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
name|numIterations
condition|;
name|i
operator|++
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting iteration = "
operator|+
name|i
argument_list|)
expr_stmt|;
name|expectedNumNodes
operator|+=
name|numMappers
operator|*
name|numNodes
expr_stmt|;
name|runGenerator
argument_list|()
expr_stmt|;
name|waitForReplication
argument_list|()
expr_stmt|;
name|runVerify
argument_list|(
name|expectedNumNodes
argument_list|)
expr_stmt|;
block|}
comment|/**        * we are always returning 0 because exceptions are thrown when there is an error        * in the verification step.        */
return|return
literal|0
return|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|super
operator|.
name|addOptions
argument_list|()
expr_stmt|;
name|addRequiredOptWithArg
argument_list|(
literal|"s"
argument_list|,
name|SOURCE_CLUSTER_OPT
argument_list|,
literal|"Cluster ID of the source cluster (e.g. localhost:2181:/hbase)"
argument_list|)
expr_stmt|;
name|addRequiredOptWithArg
argument_list|(
literal|"r"
argument_list|,
name|DEST_CLUSTER_OPT
argument_list|,
literal|"Cluster ID of the sink cluster (e.g. localhost:2182:/hbase)"
argument_list|)
expr_stmt|;
name|addRequiredOptWithArg
argument_list|(
literal|"d"
argument_list|,
name|OUTPUT_DIR_OPT
argument_list|,
literal|"Temporary directory where to write keys for the test"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
literal|"nm"
argument_list|,
name|NUM_MAPPERS_OPT
argument_list|,
literal|"Number of mappers (default: "
operator|+
name|DEFAULT_NUM_MAPPERS
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
literal|"nr"
argument_list|,
name|NUM_REDUCERS_OPT
argument_list|,
literal|"Number of reducers (default: "
operator|+
name|DEFAULT_NUM_MAPPERS
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|addOptNoArg
argument_list|(
literal|"nrs"
argument_list|,
name|NO_REPLICATION_SETUP_OPT
argument_list|,
literal|"Don't setup tables or configure replication before starting test"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
literal|"n"
argument_list|,
name|NUM_NODES_OPT
argument_list|,
literal|"Number of nodes. This should be a multiple of width * wrapMultiplier."
operator|+
literal|" (default: "
operator|+
name|DEFAULT_NUM_NODES
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
literal|"i"
argument_list|,
name|ITERATIONS_OPT
argument_list|,
literal|"Number of iterations to run (default: "
operator|+
name|DEFAULT_NUM_ITERATIONS
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
literal|"t"
argument_list|,
name|GENERATE_VERIFY_GAP_OPT
argument_list|,
literal|"Gap between generate and verify steps in seconds (default: "
operator|+
name|DEFAULT_GENERATE_VERIFY_GAP
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
literal|"w"
argument_list|,
name|WIDTH_OPT
argument_list|,
literal|"Width of the linked list chain (default: "
operator|+
name|DEFAULT_WIDTH
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
literal|"wm"
argument_list|,
name|WRAP_MULTIPLIER_OPT
argument_list|,
literal|"How many times to wrap around (default: "
operator|+
name|DEFAULT_WRAP_MULTIPLIER
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|processBaseOptions
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
name|sourceClusterIdString
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|SOURCE_CLUSTER_OPT
argument_list|)
expr_stmt|;
name|sinkClusterIdString
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|DEST_CLUSTER_OPT
argument_list|)
expr_stmt|;
name|outputDir
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OUTPUT_DIR_OPT
argument_list|)
expr_stmt|;
comment|/** This uses parseInt from {@link org.apache.hadoop.hbase.util.AbstractHBaseTool} */
name|numMappers
operator|=
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|NUM_MAPPERS_OPT
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|DEFAULT_NUM_MAPPERS
argument_list|)
argument_list|)
argument_list|,
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|numReducers
operator|=
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|NUM_REDUCERS_OPT
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|DEFAULT_NUM_REDUCERS
argument_list|)
argument_list|)
argument_list|,
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|numNodes
operator|=
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|NUM_NODES_OPT
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|DEFAULT_NUM_NODES
argument_list|)
argument_list|)
argument_list|,
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|generateVerifyGap
operator|=
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|GENERATE_VERIFY_GAP_OPT
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|DEFAULT_GENERATE_VERIFY_GAP
argument_list|)
argument_list|)
argument_list|,
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|numIterations
operator|=
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|ITERATIONS_OPT
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|DEFAULT_NUM_ITERATIONS
argument_list|)
argument_list|)
argument_list|,
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|width
operator|=
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|WIDTH_OPT
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|DEFAULT_WIDTH
argument_list|)
argument_list|)
argument_list|,
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|wrapMultiplier
operator|=
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|WRAP_MULTIPLIER_OPT
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|DEFAULT_WRAP_MULTIPLIER
argument_list|)
argument_list|)
argument_list|,
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|NO_REPLICATION_SETUP_OPT
argument_list|)
condition|)
block|{
name|noReplicationSetup
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|numNodes
operator|%
operator|(
name|width
operator|*
name|wrapMultiplier
operator|)
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"numNodes must be a multiple of width and wrap multiplier"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|runTestFromCommandLine
parameter_list|()
throws|throws
name|Exception
block|{
name|VerifyReplicationLoop
name|tool
init|=
operator|new
name|VerifyReplicationLoop
argument_list|()
decl_stmt|;
name|tool
operator|.
name|integrationTestBigLinkedList
operator|=
name|this
expr_stmt|;
return|return
name|ToolRunner
operator|.
name|run
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|tool
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
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
name|IntegrationTestingUtility
operator|.
name|setUseDistributedCluster
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|IntegrationTestReplication
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

