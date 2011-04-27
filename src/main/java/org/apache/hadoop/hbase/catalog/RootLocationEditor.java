begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|catalog
package|;
end_package

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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Makes changes to the location of<code>-ROOT-</code> in ZooKeeper.  */
end_comment

begin_class
specifier|public
class|class
name|RootLocationEditor
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
name|RootLocationEditor
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Deletes the location of<code>-ROOT-</code> in ZooKeeper.    * @param zookeeper zookeeper reference    * @throws KeeperException unexpected zookeeper exception    */
specifier|public
specifier|static
name|void
name|deleteRootLocation
parameter_list|(
name|ZooKeeperWatcher
name|zookeeper
parameter_list|)
throws|throws
name|KeeperException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Unsetting ROOT region location in ZooKeeper"
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Just delete the node.  Don't need any watches, only we will create it.
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zookeeper
argument_list|,
name|zookeeper
operator|.
name|rootServerZNode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|nne
parameter_list|)
block|{
comment|// Has already been deleted
block|}
block|}
comment|/**    * Sets the location of<code>-ROOT-</code> in ZooKeeper to the    * specified server address.    * @param zookeeper zookeeper reference    * @param location The server hosting<code>-ROOT-</code>    * @throws KeeperException unexpected zookeeper exception    */
specifier|public
specifier|static
name|void
name|setRootLocation
parameter_list|(
name|ZooKeeperWatcher
name|zookeeper
parameter_list|,
specifier|final
name|ServerName
name|location
parameter_list|)
throws|throws
name|KeeperException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting ROOT region location in ZooKeeper as "
operator|+
name|location
argument_list|)
expr_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|createAndWatch
argument_list|(
name|zookeeper
argument_list|,
name|zookeeper
operator|.
name|rootServerZNode
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|location
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NodeExistsException
name|nee
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"ROOT region location already existed, updated location"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|setData
argument_list|(
name|zookeeper
argument_list|,
name|zookeeper
operator|.
name|rootServerZNode
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|location
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

