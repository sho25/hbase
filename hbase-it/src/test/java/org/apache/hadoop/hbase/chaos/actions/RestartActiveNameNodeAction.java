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
name|chaos
operator|.
name|actions
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
name|util
operator|.
name|FSUtils
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
name|RecoverableZooKeeper
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
name|ZKWatcher
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
name|ZNodePaths
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
name|hdfs
operator|.
name|DFSUtil
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
name|hdfs
operator|.
name|HAUtil
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
name|hdfs
operator|.
name|server
operator|.
name|namenode
operator|.
name|ha
operator|.
name|proto
operator|.
name|HAZKInfoProtos
operator|.
name|ActiveNodeInfo
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

begin_comment
comment|/**  * Action that tries to restart the active namenode.  */
end_comment

begin_class
specifier|public
class|class
name|RestartActiveNameNodeAction
extends|extends
name|RestartActionBaseAction
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|RestartActiveNameNodeAction
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Value taken from org.apache.hadoop.ha.ActiveStandbyElector.java, variable :- LOCK_FILENAME
specifier|private
specifier|static
specifier|final
name|String
name|ACTIVE_NN_LOCK_NAME
init|=
literal|"ActiveStandbyElectorLock"
decl_stmt|;
comment|// Value taken from org.apache.hadoop.ha.ZKFailoverController.java
comment|// variable :- ZK_PARENT_ZNODE_DEFAULT and ZK_PARENT_ZNODE_KEY
specifier|private
specifier|static
specifier|final
name|String
name|ZK_PARENT_ZNODE_DEFAULT
init|=
literal|"/hadoop-ha"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ZK_PARENT_ZNODE_KEY
init|=
literal|"ha.zookeeper.parent-znode"
decl_stmt|;
specifier|public
name|RestartActiveNameNodeAction
parameter_list|(
name|long
name|sleepTime
parameter_list|)
block|{
name|super
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|perform
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Performing action: Restart active namenode"
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|getConf
argument_list|()
argument_list|)
operator|.
name|getFileSystem
argument_list|(
name|getConf
argument_list|()
argument_list|)
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|String
name|nameServiceID
init|=
name|DFSUtil
operator|.
name|getNamenodeNameServiceId
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|HAUtil
operator|.
name|isHAEnabled
argument_list|(
name|conf
argument_list|,
name|nameServiceID
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"HA for namenode is not enabled"
argument_list|)
throw|;
block|}
name|ZKWatcher
name|zkw
init|=
literal|null
decl_stmt|;
name|RecoverableZooKeeper
name|rzk
init|=
literal|null
decl_stmt|;
name|String
name|activeNamenode
init|=
literal|null
decl_stmt|;
name|String
name|hadoopHAZkNode
init|=
name|conf
operator|.
name|get
argument_list|(
name|ZK_PARENT_ZNODE_KEY
argument_list|,
name|ZK_PARENT_ZNODE_DEFAULT
argument_list|)
decl_stmt|;
try|try
block|{
name|zkw
operator|=
operator|new
name|ZKWatcher
argument_list|(
name|conf
argument_list|,
literal|"get-active-namenode"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|rzk
operator|=
name|zkw
operator|.
name|getRecoverableZooKeeper
argument_list|()
expr_stmt|;
name|String
name|hadoopHAZkNodePath
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|hadoopHAZkNode
argument_list|,
name|nameServiceID
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|subChildern
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkw
argument_list|,
name|hadoopHAZkNodePath
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|eachEntry
range|:
name|subChildern
control|)
block|{
if|if
condition|(
name|eachEntry
operator|.
name|contains
argument_list|(
name|ACTIVE_NN_LOCK_NAME
argument_list|)
condition|)
block|{
name|byte
index|[]
name|data
init|=
name|rzk
operator|.
name|getData
argument_list|(
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|hadoopHAZkNodePath
argument_list|,
name|ACTIVE_NN_LOCK_NAME
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ActiveNodeInfo
name|proto
init|=
name|ActiveNodeInfo
operator|.
name|parseFrom
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|activeNamenode
operator|=
name|proto
operator|.
name|getHostname
argument_list|()
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|zkw
operator|!=
literal|null
condition|)
block|{
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|activeNamenode
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"No active Name node found in zookeeper under "
operator|+
name|hadoopHAZkNode
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Found active namenode host:"
operator|+
name|activeNamenode
argument_list|)
expr_stmt|;
name|ServerName
name|activeNNHost
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|activeNamenode
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Restarting Active NameNode :"
operator|+
name|activeNamenode
argument_list|)
expr_stmt|;
name|restartNameNode
argument_list|(
name|activeNNHost
argument_list|,
name|sleepTime
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

