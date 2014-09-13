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
name|master
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
name|*
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
name|io
operator|.
name|StringWriter
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
name|NavigableMap
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
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|*
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
name|testclassification
operator|.
name|MasterTests
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
name|zookeeper
operator|.
name|MasterAddressTracker
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
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|MetricsRegionServer
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
name|MetricsRegionServerWrapperStub
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
name|tmpl
operator|.
name|master
operator|.
name|AssignmentManagerStatusTmpl
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
name|tmpl
operator|.
name|master
operator|.
name|MasterStatusTmpl
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

begin_comment
comment|/**  * Tests for the master status page and its template.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestMasterStatusServlet
block|{
specifier|private
name|HMaster
name|master
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|HBaseAdmin
name|admin
decl_stmt|;
specifier|static
specifier|final
name|ServerName
name|FAKE_HOST
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"fakehost"
argument_list|,
literal|12345
argument_list|,
literal|1234567890
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|HTableDescriptor
name|FAKE_TABLE
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"mytable"
argument_list|)
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|HRegionInfo
name|FAKE_HRI
init|=
operator|new
name|HRegionInfo
argument_list|(
name|FAKE_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
argument_list|)
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setupBasicMocks
parameter_list|()
block|{
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|master
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HMaster
operator|.
name|class
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|FAKE_HOST
argument_list|)
operator|.
name|when
argument_list|(
name|master
argument_list|)
operator|.
name|getServerName
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|conf
argument_list|)
operator|.
name|when
argument_list|(
name|master
argument_list|)
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
comment|//Fake DeadServer
name|DeadServer
name|deadServer
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|DeadServer
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Fake serverManager
name|ServerManager
name|serverManager
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ServerManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|1.0
argument_list|)
operator|.
name|when
argument_list|(
name|serverManager
argument_list|)
operator|.
name|getAverageLoad
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|serverManager
argument_list|)
operator|.
name|when
argument_list|(
name|master
argument_list|)
operator|.
name|getServerManager
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|deadServer
argument_list|)
operator|.
name|when
argument_list|(
name|serverManager
argument_list|)
operator|.
name|getDeadServers
argument_list|()
expr_stmt|;
comment|// Fake AssignmentManager and RIT
name|AssignmentManager
name|am
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|AssignmentManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|RegionStates
name|rs
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RegionStates
operator|.
name|class
argument_list|)
decl_stmt|;
name|NavigableMap
argument_list|<
name|String
argument_list|,
name|RegionState
argument_list|>
name|regionsInTransition
init|=
name|Maps
operator|.
name|newTreeMap
argument_list|()
decl_stmt|;
name|regionsInTransition
operator|.
name|put
argument_list|(
literal|"r1"
argument_list|,
operator|new
name|RegionState
argument_list|(
name|FAKE_HRI
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|CLOSING
argument_list|,
literal|12345L
argument_list|,
name|FAKE_HOST
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|rs
argument_list|)
operator|.
name|when
argument_list|(
name|am
argument_list|)
operator|.
name|getRegionStates
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|regionsInTransition
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|getRegionsInTransition
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|am
argument_list|)
operator|.
name|when
argument_list|(
name|master
argument_list|)
operator|.
name|getAssignmentManager
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|serverManager
argument_list|)
operator|.
name|when
argument_list|(
name|master
argument_list|)
operator|.
name|getServerManager
argument_list|()
expr_stmt|;
comment|// Fake ZKW
name|ZooKeeperWatcher
name|zkw
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ZooKeeperWatcher
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|"fakequorum"
argument_list|)
operator|.
name|when
argument_list|(
name|zkw
argument_list|)
operator|.
name|getQuorum
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|zkw
argument_list|)
operator|.
name|when
argument_list|(
name|master
argument_list|)
operator|.
name|getZooKeeper
argument_list|()
expr_stmt|;
comment|// Fake MasterAddressTracker
name|MasterAddressTracker
name|tracker
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MasterAddressTracker
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|tracker
argument_list|)
operator|.
name|when
argument_list|(
name|master
argument_list|)
operator|.
name|getMasterAddressTracker
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|FAKE_HOST
argument_list|)
operator|.
name|when
argument_list|(
name|tracker
argument_list|)
operator|.
name|getMasterAddress
argument_list|()
expr_stmt|;
name|MetricsRegionServer
name|rms
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MetricsRegionServer
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
operator|new
name|MetricsRegionServerWrapperStub
argument_list|()
argument_list|)
operator|.
name|when
argument_list|(
name|rms
argument_list|)
operator|.
name|getRegionServerWrapper
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|rms
argument_list|)
operator|.
name|when
argument_list|(
name|master
argument_list|)
operator|.
name|getRegionServerMetrics
argument_list|()
expr_stmt|;
comment|// Mock admin
name|admin
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HBaseAdmin
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setupMockTables
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|tables
index|[]
init|=
operator|new
name|HTableDescriptor
index|[]
block|{
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
block|,
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|tables
argument_list|)
operator|.
name|when
argument_list|(
name|admin
argument_list|)
operator|.
name|listTables
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStatusTemplateNoTables
parameter_list|()
throws|throws
name|IOException
block|{
operator|new
name|MasterStatusTmpl
argument_list|()
operator|.
name|render
argument_list|(
operator|new
name|StringWriter
argument_list|()
argument_list|,
name|master
argument_list|,
name|admin
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStatusTemplateMetaAvailable
parameter_list|()
throws|throws
name|IOException
block|{
name|setupMockTables
argument_list|()
expr_stmt|;
operator|new
name|MasterStatusTmpl
argument_list|()
operator|.
name|setMetaLocation
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"metaserver:123,12345"
argument_list|)
argument_list|)
operator|.
name|render
argument_list|(
operator|new
name|StringWriter
argument_list|()
argument_list|,
name|master
argument_list|,
name|admin
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStatusTemplateWithServers
parameter_list|()
throws|throws
name|IOException
block|{
name|setupMockTables
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"rootserver:123,12345"
argument_list|)
argument_list|,
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"metaserver:123,12345"
argument_list|)
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|ServerName
argument_list|>
name|deadServers
init|=
operator|new
name|HashSet
argument_list|<
name|ServerName
argument_list|>
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"badserver:123,12345"
argument_list|)
argument_list|,
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"uglyserver:123,12345"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
operator|new
name|MasterStatusTmpl
argument_list|()
operator|.
name|setMetaLocation
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"metaserver:123,12345"
argument_list|)
argument_list|)
operator|.
name|setServers
argument_list|(
name|servers
argument_list|)
operator|.
name|setDeadServers
argument_list|(
name|deadServers
argument_list|)
operator|.
name|render
argument_list|(
operator|new
name|StringWriter
argument_list|()
argument_list|,
name|master
argument_list|,
name|admin
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAssignmentManagerTruncatedList
parameter_list|()
throws|throws
name|IOException
block|{
name|AssignmentManager
name|am
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|AssignmentManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|RegionStates
name|rs
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RegionStates
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Add 100 regions as in-transition
name|NavigableMap
argument_list|<
name|String
argument_list|,
name|RegionState
argument_list|>
name|regionsInTransition
init|=
name|Maps
operator|.
name|newTreeMap
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|FAKE_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|,
operator|new
name|byte
index|[]
block|{
name|i
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
call|(
name|byte
call|)
argument_list|(
name|i
operator|+
literal|1
argument_list|)
block|}
argument_list|)
decl_stmt|;
name|regionsInTransition
operator|.
name|put
argument_list|(
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|,
operator|new
name|RegionState
argument_list|(
name|hri
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|CLOSING
argument_list|,
literal|12345L
argument_list|,
name|FAKE_HOST
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Add hbase:meta in transition as well
name|regionsInTransition
operator|.
name|put
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedName
argument_list|()
argument_list|,
operator|new
name|RegionState
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|CLOSING
argument_list|,
literal|12345L
argument_list|,
name|FAKE_HOST
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|rs
argument_list|)
operator|.
name|when
argument_list|(
name|am
argument_list|)
operator|.
name|getRegionStates
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|regionsInTransition
argument_list|)
operator|.
name|when
argument_list|(
name|rs
argument_list|)
operator|.
name|getRegionsInTransition
argument_list|()
expr_stmt|;
comment|// Render to a string
name|StringWriter
name|sw
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
operator|new
name|AssignmentManagerStatusTmpl
argument_list|()
operator|.
name|setLimit
argument_list|(
literal|50
argument_list|)
operator|.
name|render
argument_list|(
name|sw
argument_list|,
name|am
argument_list|)
expr_stmt|;
name|String
name|result
init|=
name|sw
operator|.
name|toString
argument_list|()
decl_stmt|;
comment|// Should always include META
name|assertTrue
argument_list|(
name|result
operator|.
name|contains
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure we only see 50 of them
name|Matcher
name|matcher
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"CLOSING"
argument_list|)
operator|.
name|matcher
argument_list|(
name|result
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|matcher
operator|.
name|find
argument_list|()
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|50
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

