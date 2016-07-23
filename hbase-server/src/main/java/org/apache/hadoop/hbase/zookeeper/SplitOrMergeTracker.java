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
name|client
operator|.
name|MasterSwitchType
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
name|exceptions
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
name|ZooKeeperProtos
operator|.
name|SwitchState
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Tracks the switch of split and merge states in ZK  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SplitOrMergeTracker
block|{
specifier|private
name|String
name|splitZnode
decl_stmt|;
specifier|private
name|String
name|mergeZnode
decl_stmt|;
specifier|private
name|SwitchStateTracker
name|splitStateTracker
decl_stmt|;
specifier|private
name|SwitchStateTracker
name|mergeStateTracker
decl_stmt|;
specifier|public
name|SplitOrMergeTracker
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|getSwitchZNode
argument_list|()
argument_list|)
operator|<
literal|0
condition|)
block|{
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|getSwitchZNode
argument_list|()
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
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|splitZnode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|getSwitchZNode
argument_list|()
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.switch.split"
argument_list|,
literal|"split"
argument_list|)
argument_list|)
expr_stmt|;
name|mergeZnode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|getSwitchZNode
argument_list|()
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.switch.merge"
argument_list|,
literal|"merge"
argument_list|)
argument_list|)
expr_stmt|;
name|splitStateTracker
operator|=
operator|new
name|SwitchStateTracker
argument_list|(
name|watcher
argument_list|,
name|splitZnode
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
name|mergeStateTracker
operator|=
operator|new
name|SwitchStateTracker
argument_list|(
name|watcher
argument_list|,
name|mergeZnode
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|()
block|{
name|splitStateTracker
operator|.
name|start
argument_list|()
expr_stmt|;
name|mergeStateTracker
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|isSplitOrMergeEnabled
parameter_list|(
name|MasterSwitchType
name|switchType
parameter_list|)
block|{
switch|switch
condition|(
name|switchType
condition|)
block|{
case|case
name|SPLIT
case|:
return|return
name|splitStateTracker
operator|.
name|isSwitchEnabled
argument_list|()
return|;
case|case
name|MERGE
case|:
return|return
name|mergeStateTracker
operator|.
name|isSwitchEnabled
argument_list|()
return|;
default|default:
break|break;
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|void
name|setSplitOrMergeEnabled
parameter_list|(
name|boolean
name|enabled
parameter_list|,
name|MasterSwitchType
name|switchType
parameter_list|)
throws|throws
name|KeeperException
block|{
switch|switch
condition|(
name|switchType
condition|)
block|{
case|case
name|SPLIT
case|:
name|splitStateTracker
operator|.
name|setSwitchEnabled
argument_list|(
name|enabled
argument_list|)
expr_stmt|;
break|break;
case|case
name|MERGE
case|:
name|mergeStateTracker
operator|.
name|setSwitchEnabled
argument_list|(
name|enabled
argument_list|)
expr_stmt|;
break|break;
default|default:
break|break;
block|}
block|}
specifier|private
specifier|static
class|class
name|SwitchStateTracker
extends|extends
name|ZooKeeperNodeTracker
block|{
specifier|public
name|SwitchStateTracker
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|String
name|node
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|,
name|node
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
comment|/**      * Return true if the switch is on, false otherwise      */
specifier|public
name|boolean
name|isSwitchEnabled
parameter_list|()
block|{
name|byte
index|[]
name|upData
init|=
name|super
operator|.
name|getData
argument_list|(
literal|false
argument_list|)
decl_stmt|;
try|try
block|{
comment|// if data in ZK is null, use default of on.
return|return
name|upData
operator|==
literal|null
operator|||
name|parseFrom
argument_list|(
name|upData
argument_list|)
operator|.
name|getEnabled
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|dex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"ZK state for LoadBalancer could not be parsed "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|upData
argument_list|)
argument_list|)
expr_stmt|;
comment|// return false to be safe.
return|return
literal|false
return|;
block|}
block|}
comment|/**      * Set the switch on/off      * @param enabled switch enabled or not?      * @throws KeeperException keepException will be thrown out      */
specifier|public
name|void
name|setSwitchEnabled
parameter_list|(
name|boolean
name|enabled
parameter_list|)
throws|throws
name|KeeperException
block|{
name|byte
index|[]
name|upData
init|=
name|toByteArray
argument_list|(
name|enabled
argument_list|)
decl_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|setData
argument_list|(
name|watcher
argument_list|,
name|node
argument_list|,
name|upData
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
name|ZKUtil
operator|.
name|createAndWatch
argument_list|(
name|watcher
argument_list|,
name|node
argument_list|,
name|upData
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|nodeDataChanged
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
specifier|private
name|byte
index|[]
name|toByteArray
parameter_list|(
name|boolean
name|enabled
parameter_list|)
block|{
name|SwitchState
operator|.
name|Builder
name|builder
init|=
name|SwitchState
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setEnabled
argument_list|(
name|enabled
argument_list|)
expr_stmt|;
return|return
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|builder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|SwitchState
name|parseFrom
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|DeserializationException
block|{
name|ProtobufUtil
operator|.
name|expectPBMagicPrefix
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|SwitchState
operator|.
name|Builder
name|builder
init|=
name|SwitchState
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
try|try
block|{
name|int
name|magicLen
init|=
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
name|ProtobufUtil
operator|.
name|mergeFrom
argument_list|(
name|builder
argument_list|,
name|bytes
argument_list|,
name|magicLen
argument_list|,
name|bytes
operator|.
name|length
operator|-
name|magicLen
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

