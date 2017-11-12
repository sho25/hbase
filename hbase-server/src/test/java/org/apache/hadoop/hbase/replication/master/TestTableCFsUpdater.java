begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|replication
operator|.
name|master
package|;
end_package

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
name|Abortable
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
name|replication
operator|.
name|ReplicationPeerConfigUtil
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
name|DeserializationException
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
name|testclassification
operator|.
name|ReplicationTests
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
name|AfterClass
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
name|rules
operator|.
name|TestName
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
name|assertTrue
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ReplicationTests
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
name|TestTableCFsUpdater
extends|extends
name|TableCFsUpdater
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
name|TestTableCFsUpdater
operator|.
name|class
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
specifier|static
name|ZooKeeperWatcher
name|zkw
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Abortable
name|abortable
init|=
literal|null
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|public
name|TestTableCFsUpdater
parameter_list|()
block|{
name|super
argument_list|(
name|zkw
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
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
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|abortable
operator|=
operator|new
name|Abortable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|why
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
expr_stmt|;
name|zkw
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
literal|"TableCFs"
argument_list|,
name|abortable
argument_list|,
literal|true
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
name|TEST_UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpgrade
parameter_list|()
throws|throws
name|KeeperException
throws|,
name|InterruptedException
throws|,
name|DeserializationException
block|{
name|String
name|peerId
init|=
literal|"1"
decl_stmt|;
specifier|final
name|TableName
name|tableName1
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"1"
argument_list|)
decl_stmt|;
specifier|final
name|TableName
name|tableName2
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"2"
argument_list|)
decl_stmt|;
specifier|final
name|TableName
name|tableName3
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"3"
argument_list|)
decl_stmt|;
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
name|zkw
operator|.
name|getQuorum
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|peerNode
init|=
name|getPeerNode
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
name|peerNode
argument_list|,
name|ReplicationPeerConfigUtil
operator|.
name|toByteArray
argument_list|(
name|rpc
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|tableCFs
init|=
name|tableName1
operator|+
literal|":cf1,cf2;"
operator|+
name|tableName2
operator|+
literal|":cf3;"
operator|+
name|tableName3
decl_stmt|;
name|String
name|tableCFsNode
init|=
name|getTableCFsNode
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"create tableCFs :"
operator|+
name|tableCFsNode
operator|+
literal|" for peerId="
operator|+
name|peerId
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
name|tableCFsNode
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableCFs
argument_list|)
argument_list|)
expr_stmt|;
name|ReplicationPeerConfig
name|actualRpc
init|=
name|ReplicationPeerConfigUtil
operator|.
name|parsePeerFrom
argument_list|(
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|peerNode
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|actualTableCfs
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|tableCFsNode
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|rpc
operator|.
name|getClusterKey
argument_list|()
argument_list|,
name|actualRpc
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|actualRpc
operator|.
name|getTableCFsMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableCFs
argument_list|,
name|actualTableCfs
argument_list|)
expr_stmt|;
name|peerId
operator|=
literal|"2"
expr_stmt|;
name|rpc
operator|=
operator|new
name|ReplicationPeerConfig
argument_list|()
expr_stmt|;
name|rpc
operator|.
name|setClusterKey
argument_list|(
name|zkw
operator|.
name|getQuorum
argument_list|()
argument_list|)
expr_stmt|;
name|peerNode
operator|=
name|getPeerNode
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
name|peerNode
argument_list|,
name|ReplicationPeerConfigUtil
operator|.
name|toByteArray
argument_list|(
name|rpc
argument_list|)
argument_list|)
expr_stmt|;
name|tableCFs
operator|=
name|tableName1
operator|+
literal|":cf1,cf3;"
operator|+
name|tableName2
operator|+
literal|":cf2"
expr_stmt|;
name|tableCFsNode
operator|=
name|getTableCFsNode
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"create tableCFs :"
operator|+
name|tableCFsNode
operator|+
literal|" for peerId="
operator|+
name|peerId
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
name|tableCFsNode
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableCFs
argument_list|)
argument_list|)
expr_stmt|;
name|actualRpc
operator|=
name|ReplicationPeerConfigUtil
operator|.
name|parsePeerFrom
argument_list|(
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|peerNode
argument_list|)
argument_list|)
expr_stmt|;
name|actualTableCfs
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|tableCFsNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rpc
operator|.
name|getClusterKey
argument_list|()
argument_list|,
name|actualRpc
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|actualRpc
operator|.
name|getTableCFsMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableCFs
argument_list|,
name|actualTableCfs
argument_list|)
expr_stmt|;
name|peerId
operator|=
literal|"3"
expr_stmt|;
name|rpc
operator|=
operator|new
name|ReplicationPeerConfig
argument_list|()
expr_stmt|;
name|rpc
operator|.
name|setClusterKey
argument_list|(
name|zkw
operator|.
name|getQuorum
argument_list|()
argument_list|)
expr_stmt|;
name|peerNode
operator|=
name|getPeerNode
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
name|peerNode
argument_list|,
name|ReplicationPeerConfigUtil
operator|.
name|toByteArray
argument_list|(
name|rpc
argument_list|)
argument_list|)
expr_stmt|;
name|tableCFs
operator|=
literal|""
expr_stmt|;
name|tableCFsNode
operator|=
name|getTableCFsNode
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"create tableCFs :"
operator|+
name|tableCFsNode
operator|+
literal|" for peerId="
operator|+
name|peerId
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
name|tableCFsNode
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableCFs
argument_list|)
argument_list|)
expr_stmt|;
name|actualRpc
operator|=
name|ReplicationPeerConfigUtil
operator|.
name|parsePeerFrom
argument_list|(
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|peerNode
argument_list|)
argument_list|)
expr_stmt|;
name|actualTableCfs
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|tableCFsNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rpc
operator|.
name|getClusterKey
argument_list|()
argument_list|,
name|actualRpc
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|actualRpc
operator|.
name|getTableCFsMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableCFs
argument_list|,
name|actualTableCfs
argument_list|)
expr_stmt|;
name|peerId
operator|=
literal|"4"
expr_stmt|;
name|rpc
operator|=
operator|new
name|ReplicationPeerConfig
argument_list|()
expr_stmt|;
name|rpc
operator|.
name|setClusterKey
argument_list|(
name|zkw
operator|.
name|getQuorum
argument_list|()
argument_list|)
expr_stmt|;
name|peerNode
operator|=
name|getPeerNode
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
name|peerNode
argument_list|,
name|ReplicationPeerConfigUtil
operator|.
name|toByteArray
argument_list|(
name|rpc
argument_list|)
argument_list|)
expr_stmt|;
name|tableCFsNode
operator|=
name|getTableCFsNode
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|actualRpc
operator|=
name|ReplicationPeerConfigUtil
operator|.
name|parsePeerFrom
argument_list|(
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|peerNode
argument_list|)
argument_list|)
expr_stmt|;
name|actualTableCfs
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|tableCFsNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rpc
operator|.
name|getClusterKey
argument_list|()
argument_list|,
name|actualRpc
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|actualRpc
operator|.
name|getTableCFsMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|actualTableCfs
argument_list|)
expr_stmt|;
name|update
argument_list|()
expr_stmt|;
name|peerId
operator|=
literal|"1"
expr_stmt|;
name|peerNode
operator|=
name|getPeerNode
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|actualRpc
operator|=
name|ReplicationPeerConfigUtil
operator|.
name|parsePeerFrom
argument_list|(
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|peerNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rpc
operator|.
name|getClusterKey
argument_list|()
argument_list|,
name|actualRpc
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableNameListMap
init|=
name|actualRpc
operator|.
name|getTableCFsMap
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|tableNameListMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tableNameListMap
operator|.
name|containsKey
argument_list|(
name|tableName1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tableNameListMap
operator|.
name|containsKey
argument_list|(
name|tableName2
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tableNameListMap
operator|.
name|containsKey
argument_list|(
name|tableName3
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tableNameListMap
operator|.
name|get
argument_list|(
name|tableName1
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cf1"
argument_list|,
name|tableNameListMap
operator|.
name|get
argument_list|(
name|tableName1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cf2"
argument_list|,
name|tableNameListMap
operator|.
name|get
argument_list|(
name|tableName1
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tableNameListMap
operator|.
name|get
argument_list|(
name|tableName2
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cf3"
argument_list|,
name|tableNameListMap
operator|.
name|get
argument_list|(
name|tableName2
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|tableNameListMap
operator|.
name|get
argument_list|(
name|tableName3
argument_list|)
argument_list|)
expr_stmt|;
name|peerId
operator|=
literal|"2"
expr_stmt|;
name|peerNode
operator|=
name|getPeerNode
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|actualRpc
operator|=
name|ReplicationPeerConfigUtil
operator|.
name|parsePeerFrom
argument_list|(
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|peerNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rpc
operator|.
name|getClusterKey
argument_list|()
argument_list|,
name|actualRpc
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|tableNameListMap
operator|=
name|actualRpc
operator|.
name|getTableCFsMap
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tableNameListMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tableNameListMap
operator|.
name|containsKey
argument_list|(
name|tableName1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tableNameListMap
operator|.
name|containsKey
argument_list|(
name|tableName2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tableNameListMap
operator|.
name|get
argument_list|(
name|tableName1
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cf1"
argument_list|,
name|tableNameListMap
operator|.
name|get
argument_list|(
name|tableName1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cf3"
argument_list|,
name|tableNameListMap
operator|.
name|get
argument_list|(
name|tableName1
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tableNameListMap
operator|.
name|get
argument_list|(
name|tableName2
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"cf2"
argument_list|,
name|tableNameListMap
operator|.
name|get
argument_list|(
name|tableName2
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|peerId
operator|=
literal|"3"
expr_stmt|;
name|peerNode
operator|=
name|getPeerNode
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|actualRpc
operator|=
name|ReplicationPeerConfigUtil
operator|.
name|parsePeerFrom
argument_list|(
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|peerNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rpc
operator|.
name|getClusterKey
argument_list|()
argument_list|,
name|actualRpc
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|tableNameListMap
operator|=
name|actualRpc
operator|.
name|getTableCFsMap
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|tableNameListMap
argument_list|)
expr_stmt|;
name|peerId
operator|=
literal|"4"
expr_stmt|;
name|peerNode
operator|=
name|getPeerNode
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|actualRpc
operator|=
name|ReplicationPeerConfigUtil
operator|.
name|parsePeerFrom
argument_list|(
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|peerNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rpc
operator|.
name|getClusterKey
argument_list|()
argument_list|,
name|actualRpc
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|tableNameListMap
operator|=
name|actualRpc
operator|.
name|getTableCFsMap
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|tableNameListMap
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

