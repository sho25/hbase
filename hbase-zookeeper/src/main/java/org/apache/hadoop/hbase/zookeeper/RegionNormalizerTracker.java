begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|shaded
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|RegionNormalizerProtos
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Tracks region normalizer state up in ZK  */
end_comment

begin_class
specifier|public
class|class
name|RegionNormalizerTracker
extends|extends
name|ZKNodeTracker
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
name|RegionNormalizerTracker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|RegionNormalizerTracker
parameter_list|(
name|ZKWatcher
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
name|znodePaths
operator|.
name|regionNormalizerZNode
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Return true if region normalizer is on, false otherwise    */
specifier|public
name|boolean
name|isNormalizerOn
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
name|getNormalizerOn
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
literal|"ZK state for RegionNormalizer could not be parsed "
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
comment|/**    * Set region normalizer on/off    * @param normalizerOn whether normalizer should be on or off    * @throws KeeperException    */
specifier|public
name|void
name|setNormalizerOn
parameter_list|(
name|boolean
name|normalizerOn
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
name|normalizerOn
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
name|watcher
operator|.
name|znodePaths
operator|.
name|regionNormalizerZNode
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
name|watcher
operator|.
name|znodePaths
operator|.
name|regionNormalizerZNode
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
name|znodePaths
operator|.
name|regionNormalizerZNode
argument_list|)
expr_stmt|;
block|}
specifier|private
name|byte
index|[]
name|toByteArray
parameter_list|(
name|boolean
name|isNormalizerOn
parameter_list|)
block|{
name|RegionNormalizerProtos
operator|.
name|RegionNormalizerState
operator|.
name|Builder
name|builder
init|=
name|RegionNormalizerProtos
operator|.
name|RegionNormalizerState
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setNormalizerOn
argument_list|(
name|isNormalizerOn
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
name|RegionNormalizerProtos
operator|.
name|RegionNormalizerState
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
name|RegionNormalizerProtos
operator|.
name|RegionNormalizerState
operator|.
name|Builder
name|builder
init|=
name|RegionNormalizerProtos
operator|.
name|RegionNormalizerState
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
end_class

end_unit
