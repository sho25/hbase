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
name|rsgroup
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
name|Iterator
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseClassTestRule
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
name|NamespaceDescriptor
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
name|Waiter
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
name|ClusterConnection
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
name|net
operator|.
name|Address
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
name|quotas
operator|.
name|QuotaTableUtil
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
name|quotas
operator|.
name|QuotaUtil
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
name|ClassRule
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
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|GetServerInfoRequest
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
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRSGroupsBasics
extends|extends
name|TestRSGroupsBase
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestRSGroupsBasics
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestRSGroupsBasics
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|setUpTestBeforeClass
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|tearDownAfterClass
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|beforeMethod
parameter_list|()
throws|throws
name|Exception
block|{
name|setUpBeforeMethod
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|afterMethod
parameter_list|()
throws|throws
name|Exception
block|{
name|tearDownAfterMethod
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasicStartUp
parameter_list|()
throws|throws
name|IOException
block|{
name|RSGroupInfo
name|defaultInfo
init|=
name|rsGroupAdmin
operator|.
name|getRSGroupInfo
argument_list|(
name|RSGroupInfo
operator|.
name|DEFAULT_GROUP
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|defaultInfo
operator|.
name|getServers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Assignment of root and meta regions.
name|int
name|count
init|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionAssignments
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
comment|//3 meta,namespace, group
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateAndDrop
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
argument_list|)
expr_stmt|;
comment|//wait for created table to be assigned
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
name|WAIT_TIMEOUT
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|getTableRegionMap
argument_list|()
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
operator|!=
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateMultiRegion
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|end
init|=
block|{
literal|1
block|,
literal|3
block|,
literal|5
block|,
literal|7
block|,
literal|9
block|}
decl_stmt|;
name|byte
index|[]
name|start
init|=
block|{
literal|0
block|,
literal|2
block|,
literal|4
block|,
literal|6
block|,
literal|8
block|}
decl_stmt|;
name|byte
index|[]
index|[]
name|f
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
block|}
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|f
argument_list|,
literal|1
argument_list|,
name|start
argument_list|,
name|end
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNamespaceCreateAndAssign
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testNamespaceCreateAndAssign"
argument_list|)
expr_stmt|;
name|String
name|nsName
init|=
name|tablePrefix
operator|+
literal|"_foo"
decl_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|nsName
argument_list|,
name|tablePrefix
operator|+
literal|"_testCreateAndAssign"
argument_list|)
decl_stmt|;
name|RSGroupInfo
name|appInfo
init|=
name|addGroup
argument_list|(
literal|"appInfo"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|admin
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|nsName
argument_list|)
operator|.
name|addConfiguration
argument_list|(
name|RSGroupInfo
operator|.
name|NAMESPACE_DESC_PROP_GROUP
argument_list|,
literal|"appInfo"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
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
literal|"f"
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
comment|//wait for created table to be assigned
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
name|WAIT_TIMEOUT
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|getTableRegionMap
argument_list|()
operator|.
name|get
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|!=
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|ServerName
name|targetServer
init|=
name|ServerName
operator|.
name|parseServerName
argument_list|(
name|appInfo
operator|.
name|getServers
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|rs
init|=
operator|(
operator|(
name|ClusterConnection
operator|)
name|admin
operator|.
name|getConnection
argument_list|()
operator|)
operator|.
name|getAdmin
argument_list|(
name|targetServer
argument_list|)
decl_stmt|;
comment|//verify it was assigned to the right group
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ProtobufUtil
operator|.
name|getOnlineRegions
argument_list|(
name|rs
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateWhenRsgroupNoOnlineServers
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testCreateWhenRsgroupNoOnlineServers"
argument_list|)
expr_stmt|;
comment|// set rsgroup has no online servers and test create table
specifier|final
name|RSGroupInfo
name|appInfo
init|=
name|addGroup
argument_list|(
literal|"appInfo"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|Address
argument_list|>
name|iterator
init|=
name|appInfo
operator|.
name|getServers
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|serversToDecommission
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ServerName
name|targetServer
init|=
name|ServerName
operator|.
name|parseServerName
argument_list|(
name|iterator
operator|.
name|next
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|targetRS
init|=
operator|(
operator|(
name|ClusterConnection
operator|)
name|admin
operator|.
name|getConnection
argument_list|()
operator|)
operator|.
name|getAdmin
argument_list|(
name|targetServer
argument_list|)
decl_stmt|;
name|targetServer
operator|=
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|targetRS
operator|.
name|getServerInfo
argument_list|(
literal|null
argument_list|,
name|GetServerInfoRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|getServerInfo
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServers
argument_list|()
operator|.
name|containsKey
argument_list|(
name|targetServer
argument_list|)
argument_list|)
expr_stmt|;
name|serversToDecommission
operator|.
name|add
argument_list|(
name|targetServer
argument_list|)
expr_stmt|;
name|admin
operator|.
name|decommissionRegionServers
argument_list|(
name|serversToDecommission
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|admin
operator|.
name|listDecommissionedRegionServers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tablePrefix
operator|+
literal|"_ns"
argument_list|,
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|admin
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|tableName
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|)
operator|.
name|addConfiguration
argument_list|(
name|RSGroupInfo
operator|.
name|NAMESPACE_DESC_PROP_GROUP
argument_list|,
name|appInfo
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
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
literal|"f"
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Shouldn't create table successfully!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"create table error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|// recommission and test create table
name|admin
operator|.
name|recommissionRegionServer
argument_list|(
name|targetServer
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|admin
operator|.
name|listDecommissionedRegionServers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
comment|// wait for created table to be assigned
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
name|WAIT_TIMEOUT
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|getTableRegionMap
argument_list|()
operator|.
name|get
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|!=
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDefaultNamespaceCreateAndAssign
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testDefaultNamespaceCreateAndAssign"
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
name|tablePrefix
operator|+
literal|"_testCreateAndAssign"
decl_stmt|;
name|admin
operator|.
name|modifyNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
literal|"default"
argument_list|)
operator|.
name|addConfiguration
argument_list|(
name|RSGroupInfo
operator|.
name|NAMESPACE_DESC_PROP_GROUP
argument_list|,
literal|"default"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"f"
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
comment|//wait for created table to be assigned
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
name|WAIT_TIMEOUT
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|getTableRegionMap
argument_list|()
operator|.
name|get
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|!=
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCloneSnapshot
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|String
name|snapshotName
init|=
name|tableName
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"_snap"
decl_stmt|;
name|TableName
name|clonedTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"_clone"
argument_list|)
decl_stmt|;
comment|// create base table
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
comment|// create snapshot
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshotName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// clone
name|admin
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName
argument_list|,
name|clonedTableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClearDeadServers
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testClearDeadServers"
argument_list|)
expr_stmt|;
specifier|final
name|RSGroupInfo
name|newGroup
init|=
name|addGroup
argument_list|(
name|getGroupName
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|NUM_DEAD_SERVERS
operator|=
name|cluster
operator|.
name|getClusterMetrics
argument_list|()
operator|.
name|getDeadServerNames
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
name|ServerName
name|targetServer
init|=
name|ServerName
operator|.
name|parseServerName
argument_list|(
name|newGroup
operator|.
name|getServers
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|targetRS
init|=
operator|(
operator|(
name|ClusterConnection
operator|)
name|admin
operator|.
name|getConnection
argument_list|()
operator|)
operator|.
name|getAdmin
argument_list|(
name|targetServer
argument_list|)
decl_stmt|;
try|try
block|{
name|targetServer
operator|=
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|targetRS
operator|.
name|getServerInfo
argument_list|(
literal|null
argument_list|,
name|GetServerInfoRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|getServerInfo
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|//stopping may cause an exception
comment|//due to the connection loss
name|targetRS
operator|.
name|stopServer
argument_list|(
literal|null
argument_list|,
name|AdminProtos
operator|.
name|StopServerRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setReason
argument_list|(
literal|"Die"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|NUM_DEAD_SERVERS
operator|++
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{     }
comment|//wait for stopped regionserver to dead server list
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
name|WAIT_TIMEOUT
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|!
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|areDeadServersInProgress
argument_list|()
operator|&&
name|cluster
operator|.
name|getClusterMetrics
argument_list|()
operator|.
name|getDeadServerNames
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|NUM_DEAD_SERVERS
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cluster
operator|.
name|getClusterMetrics
argument_list|()
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|containsKey
argument_list|(
name|targetServer
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cluster
operator|.
name|getClusterMetrics
argument_list|()
operator|.
name|getDeadServerNames
argument_list|()
operator|.
name|contains
argument_list|(
name|targetServer
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|newGroup
operator|.
name|getServers
argument_list|()
operator|.
name|contains
argument_list|(
name|targetServer
operator|.
name|getAddress
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|//clear dead servers list
name|List
argument_list|<
name|ServerName
argument_list|>
name|notClearedServers
init|=
name|admin
operator|.
name|clearDeadServers
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|targetServer
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|notClearedServers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|Address
argument_list|>
name|newGroupServers
init|=
name|rsGroupAdmin
operator|.
name|getRSGroupInfo
argument_list|(
name|newGroup
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|getServers
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|newGroupServers
operator|.
name|contains
argument_list|(
name|targetServer
operator|.
name|getAddress
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|newGroupServers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClearNotProcessedDeadServer
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testClearNotProcessedDeadServer"
argument_list|)
expr_stmt|;
name|NUM_DEAD_SERVERS
operator|=
name|cluster
operator|.
name|getClusterMetrics
argument_list|()
operator|.
name|getDeadServerNames
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
name|RSGroupInfo
name|appInfo
init|=
name|addGroup
argument_list|(
literal|"deadServerGroup"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|ServerName
name|targetServer
init|=
name|ServerName
operator|.
name|parseServerName
argument_list|(
name|appInfo
operator|.
name|getServers
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|targetRS
init|=
operator|(
operator|(
name|ClusterConnection
operator|)
name|admin
operator|.
name|getConnection
argument_list|()
operator|)
operator|.
name|getAdmin
argument_list|(
name|targetServer
argument_list|)
decl_stmt|;
try|try
block|{
name|targetServer
operator|=
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|targetRS
operator|.
name|getServerInfo
argument_list|(
literal|null
argument_list|,
name|AdminProtos
operator|.
name|GetServerInfoRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|getServerInfo
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|//stopping may cause an exception
comment|//due to the connection loss
name|targetRS
operator|.
name|stopServer
argument_list|(
literal|null
argument_list|,
name|AdminProtos
operator|.
name|StopServerRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setReason
argument_list|(
literal|"Die"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|NUM_DEAD_SERVERS
operator|++
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{     }
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
name|WAIT_TIMEOUT
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|cluster
operator|.
name|getClusterMetrics
argument_list|()
operator|.
name|getDeadServerNames
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|NUM_DEAD_SERVERS
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|notClearedServers
init|=
name|admin
operator|.
name|clearDeadServers
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|targetServer
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|notClearedServers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRSGroupsWithHBaseQuota
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|QuotaUtil
operator|.
name|QUOTA_CONF_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|restartHBaseCluster
argument_list|()
expr_stmt|;
try|try
block|{
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|90000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|admin
operator|.
name|isTableAvailable
argument_list|(
name|QuotaTableUtil
operator|.
name|QUOTA_TABLE_NAME
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|QuotaUtil
operator|.
name|QUOTA_CONF_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|restartHBaseCluster
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|restartHBaseCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\nShutting down cluster"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\nSleeping a bit"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|restartHBaseCluster
argument_list|(
name|NUM_SLAVES_BASE
operator|-
literal|1
argument_list|)
expr_stmt|;
name|initialize
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit
