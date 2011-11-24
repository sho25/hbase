begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|handler
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
name|hbase
operator|.
name|HRegionInfo
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
name|Server
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
name|executor
operator|.
name|EventHandler
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
name|AssignmentManager
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
name|ZKAssign
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
name|zookeeper
operator|.
name|KeeperException
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
name|KeeperException
operator|.
name|NoNodeException
import|;
end_import

begin_comment
comment|/**  * Handles SPLIT region event on Master.  */
end_comment

begin_class
specifier|public
class|class
name|SplitRegionHandler
extends|extends
name|EventHandler
implements|implements
name|TotesHRegionInfo
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
name|SplitRegionHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AssignmentManager
name|assignmentManager
decl_stmt|;
specifier|private
specifier|final
name|HRegionInfo
name|parent
decl_stmt|;
specifier|private
specifier|final
name|ServerName
name|sn
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|daughters
decl_stmt|;
comment|/**    * For testing only!  Set to true to skip handling of split.    */
specifier|public
specifier|static
name|boolean
name|TEST_SKIP
init|=
literal|false
decl_stmt|;
specifier|public
name|SplitRegionHandler
parameter_list|(
name|Server
name|server
parameter_list|,
name|AssignmentManager
name|assignmentManager
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|ServerName
name|sn
parameter_list|,
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|daughters
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|EventType
operator|.
name|RS_ZK_REGION_SPLIT
argument_list|)
expr_stmt|;
name|this
operator|.
name|assignmentManager
operator|=
name|assignmentManager
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|regionInfo
expr_stmt|;
name|this
operator|.
name|sn
operator|=
name|sn
expr_stmt|;
name|this
operator|.
name|daughters
operator|=
name|daughters
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HRegionInfo
name|getHRegionInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|parent
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|name
init|=
literal|"UnknownServerName"
decl_stmt|;
if|if
condition|(
name|server
operator|!=
literal|null
operator|&&
name|server
operator|.
name|getServerName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|name
operator|=
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|String
name|parentRegion
init|=
literal|"UnknownRegion"
decl_stmt|;
if|if
condition|(
name|parent
operator|!=
literal|null
condition|)
block|{
name|parentRegion
operator|=
name|parent
operator|.
name|getRegionNameAsString
argument_list|()
expr_stmt|;
block|}
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"-"
operator|+
name|name
operator|+
literal|"-"
operator|+
name|getSeqid
argument_list|()
operator|+
literal|"-"
operator|+
name|parentRegion
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
block|{
name|String
name|encodedRegionName
init|=
name|this
operator|.
name|parent
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Handling SPLIT event for "
operator|+
name|encodedRegionName
operator|+
literal|"; deleting node"
argument_list|)
expr_stmt|;
comment|// The below is for testing ONLY!  We can't do fault injection easily, so
comment|// resort to this kinda uglyness -- St.Ack 02/25/2011.
if|if
condition|(
name|TEST_SKIP
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Skipping split message, TEST_SKIP is set"
argument_list|)
expr_stmt|;
return|return;
block|}
name|this
operator|.
name|assignmentManager
operator|.
name|handleSplitReport
argument_list|(
name|this
operator|.
name|sn
argument_list|,
name|this
operator|.
name|parent
argument_list|,
name|this
operator|.
name|daughters
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|this
operator|.
name|daughters
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// Remove region from ZK
try|try
block|{
name|boolean
name|successful
init|=
literal|false
decl_stmt|;
while|while
condition|(
operator|!
name|successful
condition|)
block|{
comment|// It's possible that the RS tickles in between the reading of the
comment|// znode and the deleting, so it's safe to retry.
name|successful
operator|=
name|ZKAssign
operator|.
name|deleteNode
argument_list|(
name|this
operator|.
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|encodedRegionName
argument_list|,
name|EventHandler
operator|.
name|EventType
operator|.
name|RS_ZK_REGION_SPLIT
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|NoNodeException
condition|)
block|{
name|String
name|znodePath
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|server
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|splitLogZNode
argument_list|,
name|encodedRegionName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"The znode "
operator|+
name|znodePath
operator|+
literal|" does not exist.  May be deleted already."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|server
operator|.
name|abort
argument_list|(
literal|"Error deleting SPLIT node in ZK for transition ZK node ("
operator|+
name|parent
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|")"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Handled SPLIT event; parent="
operator|+
name|this
operator|.
name|parent
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" daughter a="
operator|+
name|this
operator|.
name|daughters
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|"daughter b="
operator|+
name|this
operator|.
name|daughters
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

