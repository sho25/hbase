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
package|;
end_package

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
name|TestMetaWithReplicas
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
name|StorefileRefresherChore
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
name|IntegrationTests
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

begin_comment
comment|/**  * An integration test that starts the cluster with three replicas for the meta  * It then creates a table, flushes the meta, kills the server holding the primary.  * After that a client issues put/get requests on the created table - the other   * replicas of the meta would be used to get the location of the region of the created  * table.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|IntegrationTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|IntegrationTestMetaReplicas
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
name|IntegrationTestMetaReplicas
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Util to get at the cluster.    */
specifier|private
specifier|static
name|IntegrationTestingUtility
name|util
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
comment|// Set up the integration test util
if|if
condition|(
name|util
operator|==
literal|null
condition|)
block|{
name|util
operator|=
operator|new
name|IntegrationTestingUtility
argument_list|()
expr_stmt|;
block|}
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|META_REPLICAS_NUM
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|StorefileRefresherChore
operator|.
name|REGIONSERVER_STOREFILE_REFRESH_PERIOD
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// Make sure there are three servers.
name|util
operator|.
name|initializeCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|ZooKeeperWatcher
name|zkw
init|=
name|util
operator|.
name|getZooKeeperWatcher
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|String
name|baseZNode
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_ZOOKEEPER_ZNODE_PARENT
argument_list|)
decl_stmt|;
name|String
name|primaryMetaZnode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.metaserver"
argument_list|,
literal|"meta-region-server"
argument_list|)
argument_list|)
decl_stmt|;
comment|// check that the data in the znode is parseable (this would also mean the znode exists)
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|primaryMetaZnode
argument_list|)
decl_stmt|;
name|ServerName
operator|.
name|parseFrom
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|waitUntilZnodeAvailable
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|waitUntilZnodeAvailable
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|teardown
parameter_list|()
throws|throws
name|Exception
block|{
comment|//Clean everything up.
name|util
operator|.
name|restoreCluster
argument_list|()
expr_stmt|;
name|util
operator|=
literal|null
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|waitUntilZnodeAvailable
parameter_list|(
name|int
name|replicaId
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|znode
init|=
name|util
operator|.
name|getZooKeeperWatcher
argument_list|()
operator|.
name|getZNodeForReplica
argument_list|(
name|replicaId
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|i
operator|<
literal|1000
condition|)
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|util
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|znode
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
else|else
break|break;
block|}
if|if
condition|(
name|i
operator|==
literal|1000
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"znode for meta replica "
operator|+
name|replicaId
operator|+
literal|" not available"
argument_list|)
throw|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testShutdownHandling
parameter_list|()
throws|throws
name|Exception
block|{
comment|// This test creates a table, flushes the meta (with 3 replicas), kills the
comment|// server holding the primary meta replica. Then it does a put/get into/from
comment|// the test table. The put/get operations would use the replicas to locate the
comment|// location of the test table's region
name|TestMetaWithReplicas
operator|.
name|shutdownMetaAndDoValidations
argument_list|(
name|util
argument_list|)
expr_stmt|;
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
name|setUp
argument_list|()
expr_stmt|;
operator|new
name|IntegrationTestMetaReplicas
argument_list|()
operator|.
name|testShutdownHandling
argument_list|()
expr_stmt|;
name|teardown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

