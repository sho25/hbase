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
name|zookeeper
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
name|conf
operator|.
name|Configured
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
name|util
operator|.
name|Tool
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
name|apache
operator|.
name|zookeeper
operator|.
name|ZooDefs
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
name|ZooKeeper
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
name|Watcher
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
name|WatchedEvent
import|;
end_import

begin_comment
comment|/**  * You may add the jaas.conf option  *    -Djava.security.auth.login.config=/PATH/jaas.conf  *  * You may also specify -D to set options  *    "hbase.zookeeper.quorum"    (it should be in hbase-site.xml)  *    "zookeeper.znode.parent"    (it should be in hbase-site.xml)  */
end_comment

begin_class
specifier|public
class|class
name|ZkAclReset
extends|extends
name|Configured
implements|implements
name|Tool
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
name|ZkAclReset
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ZK_SESSION_TIMEOUT_DEFAULT
init|=
literal|5
operator|*
literal|1000
decl_stmt|;
specifier|private
specifier|static
class|class
name|ZkWatcher
implements|implements
name|Watcher
block|{
specifier|public
name|ZkWatcher
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|WatchedEvent
name|event
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Received ZooKeeper Event, "
operator|+
literal|"type="
operator|+
name|event
operator|.
name|getType
argument_list|()
operator|+
literal|", "
operator|+
literal|"state="
operator|+
name|event
operator|.
name|getState
argument_list|()
operator|+
literal|", "
operator|+
literal|"path="
operator|+
name|event
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|resetAcls
parameter_list|(
specifier|final
name|ZooKeeper
name|zk
parameter_list|,
specifier|final
name|String
name|znode
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|children
init|=
name|zk
operator|.
name|getChildren
argument_list|(
name|znode
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|child
range|:
name|children
control|)
block|{
name|resetAcls
argument_list|(
name|zk
argument_list|,
name|znode
operator|+
literal|'/'
operator|+
name|child
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|" - reset acl for "
operator|+
name|znode
argument_list|)
expr_stmt|;
name|zk
operator|.
name|setACL
argument_list|(
name|znode
argument_list|,
name|ZooDefs
operator|.
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|resetAcls
parameter_list|(
specifier|final
name|String
name|quorumServers
parameter_list|,
specifier|final
name|int
name|zkTimeout
parameter_list|,
specifier|final
name|String
name|znode
parameter_list|)
throws|throws
name|Exception
block|{
name|ZooKeeper
name|zk
init|=
operator|new
name|ZooKeeper
argument_list|(
name|quorumServers
argument_list|,
name|zkTimeout
argument_list|,
operator|new
name|ZkWatcher
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|resetAcls
argument_list|(
name|zk
argument_list|,
name|znode
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|zk
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|resetHBaseAcls
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|quorumServers
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.zookeeper.quorum"
argument_list|,
name|HConstants
operator|.
name|LOCALHOST
argument_list|)
decl_stmt|;
name|int
name|sessionTimeout
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"zookeeper.session.timeout"
argument_list|,
name|ZK_SESSION_TIMEOUT_DEFAULT
argument_list|)
decl_stmt|;
name|String
name|znode
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.parent"
argument_list|,
name|HConstants
operator|.
name|DEFAULT_ZOOKEEPER_ZNODE_PARENT
argument_list|)
decl_stmt|;
if|if
condition|(
name|quorumServers
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to load hbase.zookeeper.quorum (try with: -conf hbase-site.xml)"
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Reset HBase ACLs for "
operator|+
name|quorumServers
operator|+
literal|" "
operator|+
name|znode
argument_list|)
expr_stmt|;
name|resetAcls
argument_list|(
name|quorumServers
argument_list|,
name|sessionTimeout
argument_list|,
name|znode
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|resetHBaseAcls
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
operator|(
literal|0
operator|)
return|;
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
name|System
operator|.
name|exit
argument_list|(
name|ToolRunner
operator|.
name|run
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
operator|new
name|ZkAclReset
argument_list|()
argument_list|,
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

