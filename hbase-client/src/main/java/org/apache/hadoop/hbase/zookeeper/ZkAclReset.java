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
name|HBaseConfiguration
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
name|classification
operator|.
name|InterfaceAudience
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

begin_comment
comment|/**  * You may add the jaas.conf option  *    -Djava.security.auth.login.config=/PATH/jaas.conf  *  * You may also specify -D to set options  *    "hbase.zookeeper.quorum"    (it should be in hbase-site.xml)  *    "zookeeper.znode.parent"    (it should be in hbase-site.xml)  *  * Use -set-acls to set the ACLs, no option to erase ACLs  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
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
name|void
name|resetAcls
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|zkw
parameter_list|,
specifier|final
name|String
name|znode
parameter_list|,
specifier|final
name|boolean
name|eraseAcls
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
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkw
argument_list|,
name|znode
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
name|zkw
argument_list|,
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|znode
argument_list|,
name|child
argument_list|)
argument_list|,
name|eraseAcls
argument_list|)
expr_stmt|;
block|}
block|}
name|ZooKeeper
name|zk
init|=
name|zkw
operator|.
name|getRecoverableZooKeeper
argument_list|()
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
if|if
condition|(
name|eraseAcls
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|" - erase ACLs for "
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
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|" - set ACLs for "
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
name|ZKUtil
operator|.
name|createACL
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|,
literal|true
argument_list|)
argument_list|,
operator|-
literal|1
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
name|Configuration
name|conf
parameter_list|,
name|boolean
name|eraseAcls
parameter_list|)
throws|throws
name|Exception
block|{
name|ZooKeeperWatcher
name|zkw
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
literal|"ZkAclReset"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
operator|(
name|eraseAcls
condition|?
literal|"Erase"
else|:
literal|"Set"
operator|)
operator|+
literal|" HBase ACLs for "
operator|+
name|zkw
operator|.
name|getQuorum
argument_list|()
operator|+
literal|" "
operator|+
name|zkw
operator|.
name|znodePaths
operator|.
name|baseZNode
argument_list|)
expr_stmt|;
name|resetAcls
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|znodePaths
operator|.
name|baseZNode
argument_list|,
name|eraseAcls
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|printUsageAndExit
parameter_list|()
block|{
name|System
operator|.
name|err
operator|.
name|printf
argument_list|(
literal|"Usage: hbase %s [options]%n"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" where [options] are:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -h|-help                Show this help and exit."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -set-acls               Setup the hbase znode ACLs for a secure cluster"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Examples:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  To reset the ACLs to the unsecure cluster behavior:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  hbase "
operator|+
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  To reset the ACLs to the secure cluster behavior:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  hbase "
operator|+
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" -set-acls"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
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
name|boolean
name|eraseAcls
init|=
literal|true
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|args
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|args
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
literal|"-help"
argument_list|)
condition|)
block|{
name|printUsageAndExit
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|args
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
literal|"-set-acls"
argument_list|)
condition|)
block|{
name|eraseAcls
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|printUsageAndExit
argument_list|()
expr_stmt|;
block|}
block|}
name|resetAcls
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|eraseAcls
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
name|HBaseConfiguration
operator|.
name|create
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

