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
name|LoadBalancerProtos
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * Tracks the load balancer state up in ZK  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|LoadBalancerTracker
extends|extends
name|ZooKeeperNodeTracker
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
name|LoadBalancerTracker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|LoadBalancerTracker
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
name|balancerZNode
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Return true if the balance switch is on, false otherwise    */
specifier|public
name|boolean
name|isBalancerOn
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
name|getBalancerOn
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
comment|/**    * Set the balancer on/off    * @param balancerOn    * @throws KeeperException    */
specifier|public
name|void
name|setBalancerOn
parameter_list|(
name|boolean
name|balancerOn
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
name|balancerOn
argument_list|)
decl_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|createAndWatch
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|balancerZNode
argument_list|,
name|upData
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
name|ZKUtil
operator|.
name|setData
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|balancerZNode
argument_list|,
name|upData
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|nodeDataChanged
argument_list|(
name|watcher
operator|.
name|balancerZNode
argument_list|)
expr_stmt|;
block|}
specifier|private
name|byte
index|[]
name|toByteArray
parameter_list|(
name|boolean
name|isBalancerOn
parameter_list|)
block|{
name|LoadBalancerProtos
operator|.
name|LoadBalancerState
operator|.
name|Builder
name|builder
init|=
name|LoadBalancerProtos
operator|.
name|LoadBalancerState
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setBalancerOn
argument_list|(
name|isBalancerOn
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
name|LoadBalancerProtos
operator|.
name|LoadBalancerState
name|parseFrom
parameter_list|(
name|byte
index|[]
name|pbBytes
parameter_list|)
throws|throws
name|DeserializationException
block|{
name|ProtobufUtil
operator|.
name|expectPBMagicPrefix
argument_list|(
name|pbBytes
argument_list|)
expr_stmt|;
name|LoadBalancerProtos
operator|.
name|LoadBalancerState
operator|.
name|Builder
name|builder
init|=
name|LoadBalancerProtos
operator|.
name|LoadBalancerState
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
name|builder
operator|.
name|mergeFrom
argument_list|(
name|pbBytes
argument_list|,
name|magicLen
argument_list|,
name|pbBytes
operator|.
name|length
operator|-
name|magicLen
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
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
end_class

end_unit

