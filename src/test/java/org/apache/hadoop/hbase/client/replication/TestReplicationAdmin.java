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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|fs
operator|.
name|FileSystem
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
name|Stoppable
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
name|regionserver
operator|.
name|ReplicationSourceManager
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

begin_comment
comment|/**  * Unit testing of ReplicationAdmin  */
end_comment

begin_class
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
name|ReplicationSourceManager
name|manager
decl_stmt|;
specifier|private
specifier|static
name|ReplicationAdmin
name|admin
decl_stmt|;
specifier|private
specifier|static
name|AtomicBoolean
name|replicating
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
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
literal|true
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
name|Path
name|oldLogDir
init|=
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|)
decl_stmt|;
name|Path
name|logDir
init|=
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
decl_stmt|;
name|manager
operator|=
operator|new
name|ReplicationSourceManager
argument_list|(
name|admin
operator|.
name|getReplicationZk
argument_list|()
argument_list|,
name|conf
argument_list|,
comment|// The following stopper never stops so that we can respond
comment|// to zk notification
operator|new
name|Stoppable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
argument_list|,
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
argument_list|,
name|replicating
argument_list|,
name|logDir
argument_list|,
name|oldLogDir
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
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|manager
operator|.
name|getSources
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
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
comment|// OK!
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
block|}
block|}
end_class

end_unit

