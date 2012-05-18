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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|Stat
import|;
end_import

begin_comment
comment|/**  * Manages the location of the current active Master for the RegionServer.  *<p>  * Listens for ZooKeeper events related to the master address. The node  *<code>/master</code> will contain the address of the current master.  * This listener is interested in  *<code>NodeDeleted</code> and<code>NodeCreated</code> events on  *<code>/master</code>.  *<p>  * Utilizes {@link ZooKeeperNodeTracker} for zk interactions.  *<p>  * You can get the current master via {@link #getMasterAddress()} or via  * {@link #getMasterAddress(ZooKeeperWatcher)} if you do not have a running  * instance of this Tracker in your context.  *<p>  * This class also includes utility for interacting with the master znode, for  * writing and reading the znode content.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MasterAddressTracker
extends|extends
name|ZooKeeperNodeTracker
block|{
comment|/**    * Construct a master address listener with the specified    *<code>zookeeper</code> reference.    *<p>    * This constructor does not trigger any actions, you must call methods    * explicitly.  Normally you will just want to execute {@link #start()} to    * begin tracking of the master address.    *    * @param watcher zk reference and watcher    * @param abortable abortable in case of fatal error    */
specifier|public
name|MasterAddressTracker
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
name|getMasterAddressZNode
argument_list|()
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the address of the current master if one is available.  Returns null    * if no current master.    * @return Server name or null if timed out.    */
specifier|public
name|ServerName
name|getMasterAddress
parameter_list|()
block|{
return|return
name|getMasterAddress
argument_list|(
literal|false
argument_list|)
return|;
block|}
comment|/**    * Get the address of the current master if one is available.  Returns null    * if no current master. If refresh is set, try to load the data from ZK again,    * otherwise, cached data will be used.    *    * @param refresh whether to refresh the data by calling ZK directly.    * @return Server name or null if timed out.    */
specifier|public
name|ServerName
name|getMasterAddress
parameter_list|(
specifier|final
name|boolean
name|refresh
parameter_list|)
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
name|refresh
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
comment|/**    * Get master address.    * Use this instead of {@link #getMasterAddress()} if you do not have an    * instance of this tracker in your context.    * @param zkw ZooKeeperWatcher to use    * @return ServerName stored in the the master address znode or null if no    * znode present.    * @throws KeeperException     * @throws IOException     */
specifier|public
specifier|static
name|ServerName
name|getMasterAddress
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|zkw
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
block|{
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
name|zkw
operator|.
name|getMasterAddressZNode
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Can't get master address from ZooKeeper; znode data == null"
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
name|KeeperException
name|ke
init|=
operator|new
name|KeeperException
operator|.
name|DataInconsistencyException
argument_list|()
decl_stmt|;
name|ke
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|ke
throw|;
block|}
block|}
comment|/**    * Set master address into the<code>master</code> znode or into the backup    * subdirectory of backup masters; switch off the passed in<code>znode</code>    * path.    * @param zkw The ZooKeeperWatcher to use.    * @param znode Where to create the znode; could be at the top level or it    * could be under backup masters    * @param master ServerName of the current master    * @return true if node created, false if not; a watch is set in both cases    * @throws KeeperException    */
specifier|public
specifier|static
name|boolean
name|setMasterAddress
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
name|ServerName
name|master
parameter_list|)
throws|throws
name|KeeperException
block|{
return|return
name|ZKUtil
operator|.
name|createEphemeralNodeAndWatch
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|,
name|toByteArray
argument_list|(
name|master
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Check if there is a master available.    * @return true if there is a master set, false if not.    */
specifier|public
name|boolean
name|hasMaster
parameter_list|()
block|{
return|return
name|super
operator|.
name|getData
argument_list|(
literal|false
argument_list|)
operator|!=
literal|null
return|;
block|}
comment|/**    * @param sn    * @return Content of the master znode as a serialized pb with the pb    * magic as prefix.    */
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
name|ZooKeeperProtos
operator|.
name|Master
operator|.
name|Builder
name|mbuilder
init|=
name|ZooKeeperProtos
operator|.
name|Master
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|HBaseProtos
operator|.
name|ServerName
operator|.
name|Builder
name|snbuilder
init|=
name|HBaseProtos
operator|.
name|ServerName
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|snbuilder
operator|.
name|setHostName
argument_list|(
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
name|snbuilder
operator|.
name|setPort
argument_list|(
name|sn
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|snbuilder
operator|.
name|setStartCode
argument_list|(
name|sn
operator|.
name|getStartcode
argument_list|()
argument_list|)
expr_stmt|;
name|mbuilder
operator|.
name|setMaster
argument_list|(
name|snbuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|mbuilder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * delete the master znode if its content is same as the parameter    */
specifier|public
specifier|static
name|boolean
name|deleteIfEquals
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
specifier|final
name|String
name|content
parameter_list|)
block|{
if|if
condition|(
name|content
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Content must not be null"
argument_list|)
throw|;
block|}
try|try
block|{
name|Stat
name|stat
init|=
operator|new
name|Stat
argument_list|()
decl_stmt|;
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getDataNoWatch
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|getMasterAddressZNode
argument_list|()
argument_list|,
name|stat
argument_list|)
decl_stmt|;
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|parseFrom
argument_list|(
name|data
argument_list|)
decl_stmt|;
if|if
condition|(
name|sn
operator|!=
literal|null
operator|&&
name|content
operator|.
name|equals
argument_list|(
name|sn
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
return|return
operator|(
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|getMasterAddressZNode
argument_list|()
argument_list|,
name|stat
operator|.
name|getVersion
argument_list|()
argument_list|)
operator|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't get or delete the master znode"
argument_list|,
name|e
argument_list|)
expr_stmt|;
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
literal|"Can't get or delete the master znode"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

