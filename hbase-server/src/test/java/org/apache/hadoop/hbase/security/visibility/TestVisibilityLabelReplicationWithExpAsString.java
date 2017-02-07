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
name|security
operator|.
name|visibility
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
name|security
operator|.
name|visibility
operator|.
name|VisibilityConstants
operator|.
name|LABELS_TABLE_NAME
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
name|assertArrayEquals
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
name|security
operator|.
name|PrivilegedExceptionAction
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
name|CellScanner
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
name|CellUtil
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
name|Tag
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
name|TagUtil
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
name|replication
operator|.
name|ReplicationAdmin
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
name|codec
operator|.
name|KeyValueCodecWithTags
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|security
operator|.
name|User
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
name|security
operator|.
name|visibility
operator|.
name|VisibilityController
operator|.
name|VisibilityReplication
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
name|SecurityTests
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
name|MiniZooKeeperCluster
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
name|Before
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
block|{
name|SecurityTests
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
name|TestVisibilityLabelReplicationWithExpAsString
extends|extends
name|TestVisibilityLabelsReplication
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
name|TestVisibilityLabelReplicationWithExpAsString
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|expected
index|[
literal|0
index|]
operator|=
literal|4
expr_stmt|;
name|expected
index|[
literal|1
index|]
operator|=
literal|6
expr_stmt|;
name|expected
index|[
literal|2
index|]
operator|=
literal|4
expr_stmt|;
name|expected
index|[
literal|3
index|]
operator|=
literal|0
expr_stmt|;
name|expected
index|[
literal|3
index|]
operator|=
literal|3
expr_stmt|;
name|expectedVisString
index|[
literal|0
index|]
operator|=
literal|"(\"public\"&\"secret\"&\"topsecret\")|(\"confidential\"&\"topsecret\")"
expr_stmt|;
name|expectedVisString
index|[
literal|1
index|]
operator|=
literal|"(\"private\"&\"public\")|(\"private\"&\"topsecret\")|"
operator|+
literal|"(\"confidential\"&\"public\")|(\"confidential\"&\"topsecret\")"
expr_stmt|;
name|expectedVisString
index|[
literal|2
index|]
operator|=
literal|"(!\"topsecret\"&\"secret\")|(!\"topsecret\"&\"confidential\")"
expr_stmt|;
name|expectedVisString
index|[
literal|3
index|]
operator|=
literal|"(\"secret\"&\""
operator|+
name|COPYRIGHT
operator|+
literal|"\\\""
operator|+
name|ACCENT
operator|+
literal|"\\\\"
operator|+
name|SECRET
operator|+
literal|"\\\""
operator|+
literal|"\u0027&\\\\"
operator|+
literal|"\")"
expr_stmt|;
comment|// setup configuration
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|DISTRIBUTED_LOG_REPLAY_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
literal|"/1"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"replication.source.size.capacity"
argument_list|,
literal|10240
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"replication.source.sleepforretries"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.maxlogs"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.master.logcleaner.ttl"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"zookeeper.recovery.retry"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"zookeeper.recovery.retry.intervalmill"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"replication.stats.thread.period.seconds"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.tests.use.shortcircuit.reads"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|setVisibilityLabelServiceImpl
argument_list|(
name|conf
argument_list|,
name|ExpAsStringVisibilityLabelServiceImpl
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|HConstants
operator|.
name|REPLICATION_CODEC_CONF_KEY
argument_list|,
name|KeyValueCodecWithTags
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|VisibilityTestUtil
operator|.
name|enableVisiblityLabels
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGIONSERVER_COPROCESSOR_CONF_KEY
argument_list|,
name|VisibilityReplication
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|USER_REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|SimpleCP
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Have to reset conf1 in case zk cluster location different
comment|// than default
name|conf
operator|.
name|setClass
argument_list|(
name|VisibilityUtils
operator|.
name|VISIBILITY_LABEL_GENERATOR_CLASS
argument_list|,
name|SimpleScanLabelGenerator
operator|.
name|class
argument_list|,
name|ScanLabelGenerator
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.superuser"
argument_list|,
literal|"admin"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.superuser"
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getShortName
argument_list|()
argument_list|)
expr_stmt|;
name|SUPERUSER
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getShortName
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"supergroup"
block|}
argument_list|)
expr_stmt|;
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getShortName
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"supergroup"
block|}
argument_list|)
expr_stmt|;
name|USER1
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"user1"
argument_list|,
operator|new
name|String
index|[]
block|{}
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|MiniZooKeeperCluster
name|miniZK
init|=
name|TEST_UTIL
operator|.
name|getZkCluster
argument_list|()
decl_stmt|;
name|zkw1
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
literal|"cluster1"
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
comment|// Base conf2 on conf1 so it gets the right zk cluster.
name|conf1
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
literal|"/2"
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setBoolean
argument_list|(
literal|"hbase.tests.use.shortcircuit.reads"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setStrings
argument_list|(
name|HConstants
operator|.
name|REPLICATION_CODEC_CONF_KEY
argument_list|,
name|KeyValueCodecWithTags
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|USER_REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|TestCoprocessorForTagsAtSink
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|setVisibilityLabelServiceImpl
argument_list|(
name|conf1
argument_list|,
name|ExpAsStringVisibilityLabelServiceImpl
operator|.
name|class
argument_list|)
expr_stmt|;
name|TEST_UTIL1
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
name|TEST_UTIL1
operator|.
name|setZkCluster
argument_list|(
name|miniZK
argument_list|)
expr_stmt|;
name|zkw2
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf1
argument_list|,
literal|"cluster2"
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// Wait for the labels table to become available
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|LABELS_TABLE_NAME
operator|.
name|getName
argument_list|()
argument_list|,
literal|50000
argument_list|)
expr_stmt|;
name|TEST_UTIL1
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ReplicationPeerConfig
name|rpc
init|=
operator|new
name|ReplicationPeerConfig
argument_list|()
decl_stmt|;
name|rpc
operator|.
name|setClusterKey
argument_list|(
name|TEST_UTIL1
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|addReplicationPeer
argument_list|(
literal|"2"
argument_list|,
name|rpc
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|table
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|desc
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|fam
argument_list|)
decl_stmt|;
name|desc
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
expr_stmt|;
name|table
operator|.
name|addFamily
argument_list|(
name|desc
argument_list|)
expr_stmt|;
try|try
init|(
name|Admin
name|hBaseAdmin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|hBaseAdmin
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Admin
name|hBaseAdmin1
init|=
name|TEST_UTIL1
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|hBaseAdmin1
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
name|addLabels
argument_list|()
expr_stmt|;
name|setAuths
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|setAuths
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|setVisibilityLabelServiceImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Class
name|clazz
parameter_list|)
block|{
name|conf
operator|.
name|setClass
argument_list|(
name|VisibilityLabelServiceManager
operator|.
name|VISIBILITY_LABEL_SERVICE_CLASS
argument_list|,
name|clazz
argument_list|,
name|VisibilityLabelService
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|verifyGet
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|String
name|visString
parameter_list|,
specifier|final
name|int
name|expected
parameter_list|,
specifier|final
name|boolean
name|nullExpected
parameter_list|,
specifier|final
name|String
modifier|...
name|auths
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
name|scanAction
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
specifier|public
name|Void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf1
argument_list|)
init|;
name|Table
name|table2
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
init|)
block|{
name|CellScanner
name|cellScanner
decl_stmt|;
name|Cell
name|current
decl_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
name|auths
argument_list|)
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|table2
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|cellScanner
operator|=
name|result
operator|.
name|cellScanner
argument_list|()
expr_stmt|;
name|boolean
name|advance
init|=
name|cellScanner
operator|.
name|advance
argument_list|()
decl_stmt|;
if|if
condition|(
name|nullExpected
condition|)
block|{
name|assertTrue
argument_list|(
operator|!
name|advance
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|current
operator|=
name|cellScanner
operator|.
name|current
argument_list|()
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|current
argument_list|)
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|TestCoprocessorForTagsAtSink
operator|.
name|tags
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|boolean
name|foundNonVisTag
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Tag
name|t
range|:
name|TestCoprocessorForTagsAtSink
operator|.
name|tags
control|)
block|{
if|if
condition|(
name|t
operator|.
name|getType
argument_list|()
operator|==
name|NON_VIS_TAG_TYPE
condition|)
block|{
name|assertEquals
argument_list|(
name|TEMP
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|TagUtil
operator|.
name|cloneValue
argument_list|(
name|t
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|foundNonVisTag
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|doAssert
argument_list|(
name|row
argument_list|,
name|visString
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|foundNonVisTag
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
decl_stmt|;
name|USER1
operator|.
name|runAs
argument_list|(
name|scanAction
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

