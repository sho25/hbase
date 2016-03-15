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
name|rsgroup
package|;
end_package

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
name|Sets
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
name|net
operator|.
name|HostAndPort
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
name|Waiter
operator|.
name|Predicate
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
name|master
operator|.
name|HMaster
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
name|master
operator|.
name|MasterServices
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
name|master
operator|.
name|ServerManager
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
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanServer
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|ObjectName
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
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
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
name|TestRSGroups
extends|extends
name|TestRSGroupsBase
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestRSGroups
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HMaster
name|master
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|init
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|RSGroupAdminEndpoint
name|RSGroupAdminEndpoint
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
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_MASTER_LOADBALANCER_CLASS
argument_list|,
name|RSGroupBasedLoadBalancer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|RSGroupAdminEndpoint
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_USEMULTI
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|NUM_SLAVES_BASE
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|ServerManager
operator|.
name|WAIT_ON_REGIONSERVERS_MINTOSTART
argument_list|,
literal|""
operator|+
name|NUM_SLAVES_BASE
argument_list|)
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
expr_stmt|;
name|cluster
operator|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
expr_stmt|;
name|master
operator|=
operator|(
operator|(
name|MiniHBaseCluster
operator|)
name|cluster
operator|)
operator|.
name|getMaster
argument_list|()
expr_stmt|;
comment|//wait for balancer to come online
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
name|master
operator|.
name|isInitialized
argument_list|()
operator|&&
operator|(
operator|(
name|RSGroupBasedLoadBalancer
operator|)
name|master
operator|.
name|getLoadBalancer
argument_list|()
operator|)
operator|.
name|isOnline
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|admin
operator|.
name|setBalancerRunning
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|rsGroupAdmin
operator|=
operator|new
name|VerifyingRSGroupAdminClient
argument_list|(
name|rsGroupAdmin
operator|.
name|newClient
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|)
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|RSGroupAdminEndpoint
operator|=
name|master
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|findCoprocessors
argument_list|(
name|RSGroupAdminEndpoint
operator|.
name|class
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
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
if|if
condition|(
operator|!
name|init
condition|)
block|{
name|init
operator|=
literal|true
expr_stmt|;
name|afterMethod
argument_list|()
expr_stmt|;
block|}
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
name|deleteTableIfNecessary
argument_list|()
expr_stmt|;
name|deleteNamespaceIfNecessary
argument_list|()
expr_stmt|;
name|deleteGroups
argument_list|()
expr_stmt|;
name|int
name|missing
init|=
name|NUM_SLAVES_BASE
operator|-
name|getNumServers
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring servers: "
operator|+
name|missing
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|missing
condition|;
name|i
operator|++
control|)
block|{
operator|(
operator|(
name|MiniHBaseCluster
operator|)
name|cluster
operator|)
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
block|}
name|rsGroupAdmin
operator|.
name|addRSGroup
argument_list|(
literal|"master"
argument_list|)
expr_stmt|;
name|ServerName
name|masterServerName
init|=
operator|(
operator|(
name|MiniHBaseCluster
operator|)
name|cluster
operator|)
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
decl_stmt|;
try|try
block|{
name|rsGroupAdmin
operator|.
name|moveServers
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|masterServerName
operator|.
name|getHostPort
argument_list|()
argument_list|)
argument_list|,
literal|"master"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
comment|// ignore
block|}
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for cleanup to finish "
operator|+
name|rsGroupAdmin
operator|.
name|listRSGroups
argument_list|()
argument_list|)
expr_stmt|;
comment|//Might be greater since moving servers back to default
comment|//is after starting a server
return|return
name|rsGroupAdmin
operator|.
name|getRSGroupInfo
argument_list|(
name|RSGroupInfo
operator|.
name|DEFAULT_GROUP
argument_list|)
operator|.
name|getServers
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|NUM_SLAVES_BASE
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
name|rsGroupAdmin
argument_list|,
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
name|NAMESPACEDESC_PROP_GROUP
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
name|admin
operator|.
name|getConnection
argument_list|()
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
specifier|final
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tablePrefix
operator|+
literal|"_testCreateAndAssign"
argument_list|)
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
name|NAMESPACEDESC_PROP_GROUP
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNamespaceConstraint
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|nsName
init|=
name|tablePrefix
operator|+
literal|"_foo"
decl_stmt|;
name|String
name|groupName
init|=
name|tablePrefix
operator|+
literal|"_foo"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"testNamespaceConstraint"
argument_list|)
expr_stmt|;
name|rsGroupAdmin
operator|.
name|addRSGroup
argument_list|(
name|groupName
argument_list|)
expr_stmt|;
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
name|NAMESPACEDESC_PROP_GROUP
argument_list|,
name|groupName
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|//test removing a referenced group
try|try
block|{
name|rsGroupAdmin
operator|.
name|removeRSGroup
argument_list|(
name|groupName
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected a constraint exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{     }
comment|//test modify group
comment|//changing with the same name is fine
name|admin
operator|.
name|modifyNamespace
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
name|NAMESPACEDESC_PROP_GROUP
argument_list|,
name|groupName
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|anotherGroup
init|=
name|tablePrefix
operator|+
literal|"_anotherGroup"
decl_stmt|;
name|rsGroupAdmin
operator|.
name|addRSGroup
argument_list|(
name|anotherGroup
argument_list|)
expr_stmt|;
comment|//test add non-existent group
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|nsName
argument_list|)
expr_stmt|;
name|rsGroupAdmin
operator|.
name|removeRSGroup
argument_list|(
name|groupName
argument_list|)
expr_stmt|;
try|try
block|{
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
name|NAMESPACEDESC_PROP_GROUP
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected a constraint exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{     }
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGroupInfoMultiAccessing
parameter_list|()
throws|throws
name|Exception
block|{
name|RSGroupInfoManager
name|manager
init|=
name|RSGroupAdminEndpoint
operator|.
name|getGroupInfoManager
argument_list|()
decl_stmt|;
specifier|final
name|RSGroupInfo
name|defaultGroup
init|=
name|manager
operator|.
name|getRSGroup
argument_list|(
literal|"default"
argument_list|)
decl_stmt|;
comment|// getRSGroup updates default group's server list
comment|// this process must not affect other threads iterating the list
name|Iterator
argument_list|<
name|HostAndPort
argument_list|>
name|it
init|=
name|defaultGroup
operator|.
name|getServers
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|manager
operator|.
name|getRSGroup
argument_list|(
literal|"default"
argument_list|)
expr_stmt|;
name|it
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMisplacedRegions
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|"_testMisplacedRegions"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"testMisplacedRegions"
argument_list|)
expr_stmt|;
specifier|final
name|RSGroupInfo
name|RSGroupInfo
init|=
name|addGroup
argument_list|(
name|rsGroupAdmin
argument_list|,
literal|"testMisplacedRegions"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|'f'
block|}
argument_list|,
literal|15
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|RSGroupAdminEndpoint
operator|.
name|getGroupInfoManager
argument_list|()
operator|.
name|moveTables
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|RSGroupInfo
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|rsGroupAdmin
operator|.
name|balanceRSGroup
argument_list|(
name|RSGroupInfo
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|60000
argument_list|,
operator|new
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
name|ServerName
name|serverName
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|RSGroupInfo
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
argument_list|,
literal|1
argument_list|)
decl_stmt|;
return|return
name|admin
operator|.
name|getConnection
argument_list|()
operator|.
name|getAdmin
argument_list|()
operator|.
name|getOnlineRegions
argument_list|(
name|serverName
argument_list|)
operator|.
name|size
argument_list|()
operator|==
literal|15
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

