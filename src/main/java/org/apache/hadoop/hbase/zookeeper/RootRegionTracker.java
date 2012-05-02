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
name|zookeeper
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
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
name|hbase
operator|.
name|Abortable
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
name|DeserializationException
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
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
comment|/**  * Tracks the root region server location node in zookeeper.  * Root region location is set by {@link RootLocationEditor} usually called  * out of<code>RegionServerServices</code>.  * This class has a watcher on the root location and notices changes.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RootRegionTracker
extends|extends
name|ZooKeeperNodeTracker
block|{
comment|/**    * Creates a root region location tracker.    *    *<p>After construction, use {@link #start} to kick off tracking.    *    * @param watcher    * @param abortable    */
specifier|public
name|RootRegionTracker
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|rootServerZNode
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Checks if the root region location is available.    * @return true if root region location is available, false if not    */
specifier|public
name|boolean
name|isLocationAvailable
parameter_list|()
block|{
return|return
name|super
operator|.
name|getData
argument_list|(
literal|true
argument_list|)
operator|!=
literal|null
return|;
block|}
comment|/**    * Gets the root region location, if available.  Does not block.  Sets a watcher.    * @return server name or null if we failed to get the data.    * @throws InterruptedException    */
specifier|public
name|ServerName
name|getRootRegionLocation
parameter_list|()
throws|throws
name|InterruptedException
block|{
try|try
block|{
return|return
name|ServerName
operator|.
name|parseFrom
argument_list|(
name|super
operator|.
name|getData
argument_list|(
literal|true
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed parse"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Gets the root region location, if available.  Does not block.  Does not set    * a watcher (In this regard it differs from {@link #getRootRegionLocation()}.    * @param zkw    * @return server name or null if we failed to get the data.    * @throws KeeperException    */
specifier|public
specifier|static
name|ServerName
name|getRootRegionLocation
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|zkw
parameter_list|)
throws|throws
name|KeeperException
block|{
try|try
block|{
return|return
name|ServerName
operator|.
name|parseFrom
argument_list|(
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|rootServerZNode
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
throw|throw
name|ZKUtil
operator|.
name|convert
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Gets the root region location, if available, and waits for up to the    * specified timeout if not immediately available.    * Given the zookeeper notification could be delayed, we will try to    * get the latest data.    * @param timeout maximum time to wait, in millis    * @return server name for server hosting root region formatted as per    * {@link ServerName}, or null if none available    * @throws InterruptedException if interrupted while waiting    */
specifier|public
name|ServerName
name|waitRootRegionLocation
parameter_list|(
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
literal|false
operator|==
name|checkIfBaseNodeAvailable
argument_list|()
condition|)
block|{
name|String
name|errorMsg
init|=
literal|"Check the value configured in 'zookeeper.znode.parent'. "
operator|+
literal|"There could be a mismatch with the one configured in the master."
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|errorMsg
argument_list|)
throw|;
block|}
try|try
block|{
return|return
name|ServerName
operator|.
name|parseFrom
argument_list|(
name|super
operator|.
name|blockUntilAvailable
argument_list|(
name|timeout
argument_list|,
literal|true
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed parse"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
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
comment|// Make the RootRegionServer pb and then get its bytes and save this as
comment|// the znode content.
name|byte
index|[]
name|data
init|=
name|toByteArray
argument_list|(
name|location
argument_list|)
decl_stmt|;
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
name|data
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
name|data
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Build up the znode content.    * @param sn What to put into the znode.    * @return The content of the root-region-server znode    */
specifier|static
name|byte
index|[]
name|toByteArray
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
comment|// ZNode content is a pb message preceeded by some pb magic.
name|HBaseProtos
operator|.
name|ServerName
name|pbsn
init|=
name|HBaseProtos
operator|.
name|ServerName
operator|.
name|newBuilder
argument_list|()
operator|.
name|setHostName
argument_list|(
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|)
operator|.
name|setPort
argument_list|(
name|sn
operator|.
name|getPort
argument_list|()
argument_list|)
operator|.
name|setStartCode
argument_list|(
name|sn
operator|.
name|getStartcode
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ZooKeeperProtos
operator|.
name|RootRegionServer
name|pbrsr
init|=
name|ZooKeeperProtos
operator|.
name|RootRegionServer
operator|.
name|newBuilder
argument_list|()
operator|.
name|setServer
argument_list|(
name|pbsn
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|pbrsr
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
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
comment|// Just delete the node.  Don't need any watches.
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
comment|/**    * Wait until the root region is available.    * @param zkw    * @param timeout    * @return ServerName or null if we timed out.    * @throws InterruptedException    */
specifier|public
specifier|static
name|ServerName
name|blockUntilAvailable
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|zkw
parameter_list|,
specifier|final
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|blockUntilAvailable
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|rootServerZNode
argument_list|,
name|timeout
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
return|return
literal|null
return|;
try|try
block|{
return|return
name|ServerName
operator|.
name|parseFrom
argument_list|(
name|data
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed parse"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

