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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Threads
operator|.
name|sleep
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
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
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
name|HashSet
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
name|Version
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
name|master
operator|.
name|procedure
operator|.
name|ServerCrashProcedure
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
name|util
operator|.
name|JVMClusterUtil
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
name|VersionInfo
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
name|collect
operator|.
name|Sets
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
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRSGroupsKillRS
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
name|TestRSGroupsKillRS
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
name|TestRSGroupsKillRS
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
name|testKillRS
parameter_list|()
throws|throws
name|Exception
block|{
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
name|getNameWithoutIndex
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
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
name|TableDescriptor
name|desc
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
literal|"f"
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
name|ServerName
name|targetServer
init|=
name|getServerName
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
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|admin
operator|.
name|getRegions
argument_list|(
name|targetServer
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
comment|// stopping may cause an exception
comment|// due to the connection loss
name|admin
operator|.
name|stopRegionServer
argument_list|(
name|targetServer
operator|.
name|getAddress
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{     }
comment|// wait until the server is actually down
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
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// there is only one rs in the group and we killed it, so the region can not be online, until
comment|// later we add new servers to it.
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
name|cluster
operator|.
name|getClusterMetrics
argument_list|()
operator|.
name|getRegionStatesInTransition
argument_list|()
operator|.
name|isEmpty
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|Address
argument_list|>
name|newServers
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|newServers
operator|.
name|add
argument_list|(
name|rsGroupAdmin
operator|.
name|getRSGroup
argument_list|(
name|RSGroupInfo
operator|.
name|DEFAULT_GROUP
argument_list|)
operator|.
name|getServers
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
name|rsGroupAdmin
operator|.
name|moveToRSGroup
argument_list|(
name|newServers
argument_list|,
name|appInfo
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Make sure all the table's regions get reassigned
comment|// disabling the table guarantees no conflicting assign/unassign (ie SSH) happens
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// wait for region to be assigned
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
name|getRegionStatesInTransition
argument_list|()
operator|.
name|isEmpty
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|ServerName
name|targetServer1
init|=
name|getServerName
argument_list|(
name|newServers
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|admin
operator|.
name|getRegions
argument_list|(
name|targetServer1
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|admin
operator|.
name|getRegions
argument_list|(
name|targetServer1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testKillAllRSInGroup
parameter_list|()
throws|throws
name|Exception
block|{
comment|// create a rsgroup and move two regionservers to it
name|String
name|groupName
init|=
literal|"my_group"
decl_stmt|;
name|int
name|groupRSCount
init|=
literal|2
decl_stmt|;
name|addGroup
argument_list|(
name|groupName
argument_list|,
name|groupRSCount
argument_list|)
expr_stmt|;
comment|// create a table, and move it to my_group
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|tableName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|t
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|TableName
argument_list|>
name|toAddTables
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|toAddTables
operator|.
name|add
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|rsGroupAdmin
operator|.
name|setRSGroup
argument_list|(
name|toAddTables
argument_list|,
name|groupName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|rsGroupAdmin
operator|.
name|getRSGroup
argument_list|(
name|groupName
argument_list|)
operator|.
name|getTables
argument_list|()
operator|.
name|contains
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
comment|// check my_group servers and table regions
name|Set
argument_list|<
name|Address
argument_list|>
name|servers
init|=
name|rsGroupAdmin
operator|.
name|getRSGroup
argument_list|(
name|groupName
argument_list|)
operator|.
name|getServers
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|servers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"group servers {}"
argument_list|,
name|servers
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionInfo
name|tr
range|:
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsOfTable
argument_list|(
name|tableName
argument_list|)
control|)
block|{
name|assertTrue
argument_list|(
name|servers
operator|.
name|contains
argument_list|(
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
name|get
argument_list|(
name|tr
argument_list|)
operator|.
name|getAddress
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Move a region, to ensure there exists a region whose 'lastHost' is in my_group
comment|// ('lastHost' of other regions are in 'default' group)
comment|// and check if all table regions are online
name|List
argument_list|<
name|ServerName
argument_list|>
name|gsn
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Address
name|addr
range|:
name|servers
control|)
block|{
name|gsn
operator|.
name|add
argument_list|(
name|getServerName
argument_list|(
name|addr
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|gsn
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|RegionInfo
argument_list|,
name|ServerName
argument_list|>
name|entry
range|:
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
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"move region {} from {} to {}"
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|gsn
operator|.
name|get
argument_list|(
literal|1
operator|-
name|gsn
operator|.
name|indexOf
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|moveRegionAndWait
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|gsn
operator|.
name|get
argument_list|(
literal|1
operator|-
name|gsn
operator|.
name|indexOf
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
comment|// case 1: stop all the regionservers in my_group, and restart a regionserver in my_group,
comment|// and then check if all table regions are online
for|for
control|(
name|Address
name|addr
range|:
name|rsGroupAdmin
operator|.
name|getRSGroup
argument_list|(
name|groupName
argument_list|)
operator|.
name|getServers
argument_list|()
control|)
block|{
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|stopRegionServer
argument_list|(
name|getServerName
argument_list|(
name|addr
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// better wait for a while for region reassign
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NUM_SLAVES_BASE
operator|-
name|gsn
operator|.
name|size
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|startRegionServer
argument_list|(
name|gsn
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getHostname
argument_list|()
argument_list|,
name|gsn
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NUM_SLAVES_BASE
operator|-
name|gsn
operator|.
name|size
argument_list|()
operator|+
literal|1
argument_list|,
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
comment|// case 2: stop all the regionservers in my_group, and move another
comment|// regionserver(from the 'default' group) to my_group,
comment|// and then check if all table regions are online
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|rst
range|:
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getLiveRegionServerThreads
argument_list|()
control|)
block|{
if|if
condition|(
name|rst
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|.
name|getAddress
argument_list|()
operator|.
name|equals
argument_list|(
name|gsn
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getAddress
argument_list|()
argument_list|)
condition|)
block|{
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|stopRegionServer
argument_list|(
name|rst
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NUM_SLAVES_BASE
operator|-
name|gsn
operator|.
name|size
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|ServerName
name|newServer
init|=
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|rsGroupAdmin
operator|.
name|moveToRSGroup
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|newServer
operator|.
name|getAddress
argument_list|()
argument_list|)
argument_list|,
name|groupName
argument_list|)
expr_stmt|;
comment|// wait and check if table regions are online
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLowerMetaGroupVersion
parameter_list|()
throws|throws
name|Exception
block|{
comment|// create a rsgroup and move one regionserver to it
name|String
name|groupName
init|=
literal|"meta_group"
decl_stmt|;
name|int
name|groupRSCount
init|=
literal|1
decl_stmt|;
name|addGroup
argument_list|(
name|groupName
argument_list|,
name|groupRSCount
argument_list|)
expr_stmt|;
comment|// move hbase:meta to meta_group
name|Set
argument_list|<
name|TableName
argument_list|>
name|toAddTables
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|toAddTables
operator|.
name|add
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|rsGroupAdmin
operator|.
name|setRSGroup
argument_list|(
name|toAddTables
argument_list|,
name|groupName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|rsGroupAdmin
operator|.
name|getRSGroup
argument_list|(
name|groupName
argument_list|)
operator|.
name|getTables
argument_list|()
operator|.
name|contains
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
comment|// restart the regionserver in meta_group, and lower its version
name|String
name|originVersion
init|=
literal|""
decl_stmt|;
name|Set
argument_list|<
name|Address
argument_list|>
name|servers
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Address
name|addr
range|:
name|rsGroupAdmin
operator|.
name|getRSGroup
argument_list|(
name|groupName
argument_list|)
operator|.
name|getServers
argument_list|()
control|)
block|{
name|servers
operator|.
name|add
argument_list|(
name|addr
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|stopRegionServer
argument_list|(
name|getServerName
argument_list|(
name|addr
argument_list|)
argument_list|)
expr_stmt|;
name|originVersion
operator|=
name|master
operator|.
name|getRegionServerVersion
argument_list|(
name|getServerName
argument_list|(
name|addr
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// better wait for a while for region reassign
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NUM_SLAVES_BASE
operator|-
name|groupRSCount
argument_list|,
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Address
name|address
init|=
name|servers
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|int
name|majorVersion
init|=
name|VersionInfo
operator|.
name|getMajorVersion
argument_list|(
name|originVersion
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|majorVersion
operator|>=
literal|1
argument_list|)
expr_stmt|;
name|String
name|lowerVersion
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|majorVersion
operator|-
literal|1
argument_list|)
operator|+
name|originVersion
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
index|[
literal|1
index|]
decl_stmt|;
name|setFinalStatic
argument_list|(
name|Version
operator|.
name|class
operator|.
name|getField
argument_list|(
literal|"version"
argument_list|)
argument_list|,
name|lowerVersion
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|startRegionServer
argument_list|(
name|address
operator|.
name|getHostname
argument_list|()
argument_list|,
name|address
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NUM_SLAVES_BASE
argument_list|,
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|VersionInfo
operator|.
name|compareVersion
argument_list|(
name|originVersion
argument_list|,
name|master
operator|.
name|getRegionServerVersion
argument_list|(
name|getServerName
argument_list|(
name|servers
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
argument_list|)
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"wait for META assigned..."
argument_list|)
expr_stmt|;
comment|// SCP finished, which means all regions assigned too.
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|60000
argument_list|,
parameter_list|()
lambda|->
operator|!
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getProcedures
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|p
lambda|->
operator|(
name|p
operator|instanceof
name|ServerCrashProcedure
operator|)
argument_list|)
operator|.
name|findAny
argument_list|()
operator|.
name|isPresent
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|setFinalStatic
parameter_list|(
name|Field
name|field
parameter_list|,
name|Object
name|newValue
parameter_list|)
throws|throws
name|Exception
block|{
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Field
name|modifiersField
init|=
name|Field
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"modifiers"
argument_list|)
decl_stmt|;
name|modifiersField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|modifiersField
operator|.
name|setInt
argument_list|(
name|field
argument_list|,
name|field
operator|.
name|getModifiers
argument_list|()
operator|&
operator|~
name|Modifier
operator|.
name|FINAL
argument_list|)
expr_stmt|;
name|field
operator|.
name|set
argument_list|(
literal|null
argument_list|,
name|newValue
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

