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
operator|.
name|replication
package|;
end_package

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
name|TreeMap
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
name|replication
operator|.
name|ReplicationException
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

begin_comment
comment|/**  * Unit testing of ReplicationAdmin  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestReplicationAdmin
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
name|TestReplicationAdmin
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
specifier|final
name|String
name|ID_ONE
init|=
literal|"1"
decl_stmt|;
specifier|private
specifier|final
name|String
name|KEY_ONE
init|=
literal|"127.0.0.1:2181:/hbase"
decl_stmt|;
specifier|private
specifier|final
name|String
name|ID_SECOND
init|=
literal|"2"
decl_stmt|;
specifier|private
specifier|final
name|String
name|KEY_SECOND
init|=
literal|"127.0.0.1:2181:/hbase2"
decl_stmt|;
specifier|private
specifier|static
name|ReplicationAdmin
name|admin
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
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_ENABLE_KEY
argument_list|,
name|HConstants
operator|.
name|REPLICATION_ENABLE_DEFAULT
argument_list|)
expr_stmt|;
name|admin
operator|=
operator|new
name|ReplicationAdmin
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Simple testing of adding and removing peers, basically shows that    * all interactions with ZK work    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testAddRemovePeer
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Add a valid peer
name|admin
operator|.
name|addPeer
argument_list|(
name|ID_ONE
argument_list|,
name|KEY_ONE
argument_list|)
expr_stmt|;
comment|// try adding the same (fails)
try|try
block|{
name|admin
operator|.
name|addPeer
argument_list|(
name|ID_ONE
argument_list|,
name|KEY_ONE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{
comment|// OK!
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|admin
operator|.
name|getPeersCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// Try to remove an inexisting peer
try|try
block|{
name|admin
operator|.
name|removePeer
argument_list|(
name|ID_SECOND
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{
comment|// OK!
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|admin
operator|.
name|getPeersCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// Add a second since multi-slave is supported
try|try
block|{
name|admin
operator|.
name|addPeer
argument_list|(
name|ID_SECOND
argument_list|,
name|KEY_SECOND
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|iae
parameter_list|)
block|{
name|fail
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|admin
operator|.
name|getPeersCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// Remove the first peer we added
name|admin
operator|.
name|removePeer
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|admin
operator|.
name|getPeersCount
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|removePeer
argument_list|(
name|ID_SECOND
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|admin
operator|.
name|getPeersCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * basic checks that when we add a peer that it is enabled, and that we can disable    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testEnableDisable
parameter_list|()
throws|throws
name|Exception
block|{
name|admin
operator|.
name|addPeer
argument_list|(
name|ID_ONE
argument_list|,
name|KEY_ONE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|admin
operator|.
name|getPeersCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|getPeerState
argument_list|(
name|ID_ONE
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disablePeer
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|admin
operator|.
name|getPeerState
argument_list|(
name|ID_ONE
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|getPeerState
argument_list|(
name|ID_SECOND
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{
comment|// OK!
block|}
name|admin
operator|.
name|removePeer
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetTableCfsStr
parameter_list|()
block|{
comment|// opposite of TestPerTableCFReplication#testParseTableCFsFromConfig()
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tabCFsMap
init|=
literal|null
decl_stmt|;
comment|// 1. null or empty string, result should be null
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|ReplicationAdmin
operator|.
name|getTableCfsStr
argument_list|(
name|tabCFsMap
argument_list|)
argument_list|)
expr_stmt|;
comment|// 2. single table: "tab1" / "tab2:cf1" / "tab3:cf1,cf3"
name|tabCFsMap
operator|=
operator|new
name|TreeMap
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|tabCFsMap
operator|.
name|put
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tab1"
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// its table name is "tab1"
name|assertEquals
argument_list|(
literal|"tab1"
argument_list|,
name|ReplicationAdmin
operator|.
name|getTableCfsStr
argument_list|(
name|tabCFsMap
argument_list|)
argument_list|)
expr_stmt|;
name|tabCFsMap
operator|=
operator|new
name|TreeMap
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|tabCFsMap
operator|.
name|put
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tab1"
argument_list|)
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"cf1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"tab1:cf1"
argument_list|,
name|ReplicationAdmin
operator|.
name|getTableCfsStr
argument_list|(
name|tabCFsMap
argument_list|)
argument_list|)
expr_stmt|;
name|tabCFsMap
operator|=
operator|new
name|TreeMap
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|tabCFsMap
operator|.
name|put
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tab1"
argument_list|)
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"cf1"
argument_list|,
literal|"cf3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"tab1:cf1,cf3"
argument_list|,
name|ReplicationAdmin
operator|.
name|getTableCfsStr
argument_list|(
name|tabCFsMap
argument_list|)
argument_list|)
expr_stmt|;
comment|// 3. multiple tables: "tab1 ; tab2:cf1 ; tab3:cf1,cf3"
name|tabCFsMap
operator|=
operator|new
name|TreeMap
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|tabCFsMap
operator|.
name|put
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tab1"
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|tabCFsMap
operator|.
name|put
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tab2"
argument_list|)
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"cf1"
argument_list|)
argument_list|)
expr_stmt|;
name|tabCFsMap
operator|.
name|put
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tab3"
argument_list|)
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"cf1"
argument_list|,
literal|"cf3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"tab1;tab2:cf1;tab3:cf1,cf3"
argument_list|,
name|ReplicationAdmin
operator|.
name|getTableCfsStr
argument_list|(
name|tabCFsMap
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAppendPeerTableCFs
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Add a valid peer
name|admin
operator|.
name|addPeer
argument_list|(
name|ID_ONE
argument_list|,
name|KEY_ONE
argument_list|)
expr_stmt|;
name|admin
operator|.
name|appendPeerTableCFs
argument_list|(
name|ID_ONE
argument_list|,
literal|"t1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"t1"
argument_list|,
name|admin
operator|.
name|getPeerTableCFs
argument_list|(
name|ID_ONE
argument_list|)
argument_list|)
expr_stmt|;
comment|// append table t2 to replication
name|admin
operator|.
name|appendPeerTableCFs
argument_list|(
name|ID_ONE
argument_list|,
literal|"t2"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"t2;t1"
argument_list|,
name|admin
operator|.
name|getPeerTableCFs
argument_list|(
name|ID_ONE
argument_list|)
argument_list|)
expr_stmt|;
comment|// append table column family: f1 of t3 to replication
name|admin
operator|.
name|appendPeerTableCFs
argument_list|(
name|ID_ONE
argument_list|,
literal|"t3:f1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"t3:f1;t2;t1"
argument_list|,
name|admin
operator|.
name|getPeerTableCFs
argument_list|(
name|ID_ONE
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|removePeer
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemovePeerTableCFs
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Add a valid peer
name|admin
operator|.
name|addPeer
argument_list|(
name|ID_ONE
argument_list|,
name|KEY_ONE
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|removePeerTableCFs
argument_list|(
name|ID_ONE
argument_list|,
literal|"t3"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{     }
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|admin
operator|.
name|getPeerTableCFs
argument_list|(
name|ID_ONE
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|setPeerTableCFs
argument_list|(
name|ID_ONE
argument_list|,
literal|"t1;t2:cf1"
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|removePeerTableCFs
argument_list|(
name|ID_ONE
argument_list|,
literal|"t3"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{     }
name|assertEquals
argument_list|(
literal|"t1;t2:cf1"
argument_list|,
name|admin
operator|.
name|getPeerTableCFs
argument_list|(
name|ID_ONE
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|removePeerTableCFs
argument_list|(
name|ID_ONE
argument_list|,
literal|"t1:f1"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{     }
name|admin
operator|.
name|removePeerTableCFs
argument_list|(
name|ID_ONE
argument_list|,
literal|"t1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"t2:cf1"
argument_list|,
name|admin
operator|.
name|getPeerTableCFs
argument_list|(
name|ID_ONE
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|removePeerTableCFs
argument_list|(
name|ID_ONE
argument_list|,
literal|"t2"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{     }
name|admin
operator|.
name|removePeerTableCFs
argument_list|(
name|ID_ONE
argument_list|,
literal|"t2:cf1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|admin
operator|.
name|getPeerTableCFs
argument_list|(
name|ID_ONE
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|removePeer
argument_list|(
name|ID_ONE
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

