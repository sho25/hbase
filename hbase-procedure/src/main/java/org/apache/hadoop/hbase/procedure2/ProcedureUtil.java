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
name|procedure2
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
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|hbase
operator|.
name|util
operator|.
name|NonceKey
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
name|RetryCounter
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
name|RetryCounter
operator|.
name|ExponentialBackoffPolicyWithLimit
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
name|RetryCounter
operator|.
name|RetryConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Any
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Internal
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Parser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|UnsafeByteOperations
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
name|LockServiceProtos
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
name|ProcedureProtos
import|;
end_import

begin_comment
comment|/**  * Helper to convert to/from ProcedureProtos  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ProcedureUtil
block|{
specifier|private
name|ProcedureUtil
parameter_list|()
block|{ }
comment|// ==========================================================================
comment|//  Reflection helpers to create/validate a Procedure object
comment|// ==========================================================================
specifier|private
specifier|static
name|Procedure
argument_list|<
name|?
argument_list|>
name|newProcedure
parameter_list|(
name|String
name|className
parameter_list|)
throws|throws
name|BadProcedureException
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Modifier
operator|.
name|isPublic
argument_list|(
name|clazz
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"the "
operator|+
name|clazz
operator|+
literal|" class is not public"
argument_list|)
throw|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|Constructor
argument_list|<
name|?
extends|extends
name|Procedure
argument_list|>
name|ctor
init|=
name|clazz
operator|.
name|asSubclass
argument_list|(
name|Procedure
operator|.
name|class
argument_list|)
operator|.
name|getConstructor
argument_list|()
decl_stmt|;
assert|assert
name|ctor
operator|!=
literal|null
operator|:
literal|"no constructor found"
assert|;
if|if
condition|(
operator|!
name|Modifier
operator|.
name|isPublic
argument_list|(
name|ctor
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"the "
operator|+
name|clazz
operator|+
literal|" constructor is not public"
argument_list|)
throw|;
block|}
return|return
name|ctor
operator|.
name|newInstance
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BadProcedureException
argument_list|(
literal|"The procedure class "
operator|+
name|className
operator|+
literal|" must be accessible and have an empty constructor"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|static
name|void
name|validateClass
parameter_list|(
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
parameter_list|)
throws|throws
name|BadProcedureException
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|proc
operator|.
name|getClass
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|Modifier
operator|.
name|isPublic
argument_list|(
name|clazz
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"the "
operator|+
name|clazz
operator|+
literal|" class is not public"
argument_list|)
throw|;
block|}
name|Constructor
argument_list|<
name|?
argument_list|>
name|ctor
init|=
name|clazz
operator|.
name|getConstructor
argument_list|()
decl_stmt|;
assert|assert
name|ctor
operator|!=
literal|null
assert|;
if|if
condition|(
operator|!
name|Modifier
operator|.
name|isPublic
argument_list|(
name|ctor
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"the "
operator|+
name|clazz
operator|+
literal|" constructor is not public"
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BadProcedureException
argument_list|(
literal|"The procedure class "
operator|+
name|proc
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" must be accessible and have an empty constructor"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|// ==========================================================================
comment|//  convert to and from Procedure object
comment|// ==========================================================================
comment|/**    * A serializer for our Procedures. Instead of the previous serializer, it    * uses the stateMessage list to store the internal state of the Procedures.    */
specifier|private
specifier|static
class|class
name|StateSerializer
implements|implements
name|ProcedureStateSerializer
block|{
specifier|private
specifier|final
name|ProcedureProtos
operator|.
name|Procedure
operator|.
name|Builder
name|builder
decl_stmt|;
specifier|private
name|int
name|deserializeIndex
decl_stmt|;
specifier|public
name|StateSerializer
parameter_list|(
name|ProcedureProtos
operator|.
name|Procedure
operator|.
name|Builder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|builder
operator|=
name|builder
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serialize
parameter_list|(
name|Message
name|message
parameter_list|)
throws|throws
name|IOException
block|{
name|Any
name|packedMessage
init|=
name|Any
operator|.
name|pack
argument_list|(
name|message
argument_list|)
decl_stmt|;
name|builder
operator|.
name|addStateMessage
argument_list|(
name|packedMessage
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|M
extends|extends
name|Message
parameter_list|>
name|M
name|deserialize
parameter_list|(
name|Class
argument_list|<
name|M
argument_list|>
name|clazz
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|deserializeIndex
operator|>=
name|builder
operator|.
name|getStateMessageCount
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Invalid state message index: "
operator|+
name|deserializeIndex
argument_list|)
throw|;
block|}
try|try
block|{
name|Any
name|packedMessage
init|=
name|builder
operator|.
name|getStateMessage
argument_list|(
name|deserializeIndex
operator|++
argument_list|)
decl_stmt|;
return|return
name|packedMessage
operator|.
name|unpack
argument_list|(
name|clazz
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
name|e
operator|.
name|unwrapIOException
argument_list|()
throw|;
block|}
block|}
block|}
comment|/**    * A serializer (deserializer) for those Procedures which were serialized    * before this patch. It deserializes the old, binary stateData field.    */
specifier|private
specifier|static
class|class
name|CompatStateSerializer
implements|implements
name|ProcedureStateSerializer
block|{
specifier|private
name|InputStream
name|inputStream
decl_stmt|;
specifier|public
name|CompatStateSerializer
parameter_list|(
name|InputStream
name|inputStream
parameter_list|)
block|{
name|this
operator|.
name|inputStream
operator|=
name|inputStream
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serialize
parameter_list|(
name|Message
name|message
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|public
parameter_list|<
name|M
extends|extends
name|Message
parameter_list|>
name|M
name|deserialize
parameter_list|(
name|Class
argument_list|<
name|M
argument_list|>
name|clazz
parameter_list|)
throws|throws
name|IOException
block|{
name|Parser
argument_list|<
name|M
argument_list|>
name|parser
init|=
operator|(
name|Parser
argument_list|<
name|M
argument_list|>
operator|)
name|Internal
operator|.
name|getDefaultInstance
argument_list|(
name|clazz
argument_list|)
operator|.
name|getParserForType
argument_list|()
decl_stmt|;
try|try
block|{
return|return
name|parser
operator|.
name|parseDelimitedFrom
argument_list|(
name|inputStream
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
name|e
operator|.
name|unwrapIOException
argument_list|()
throw|;
block|}
block|}
block|}
comment|/**    * Helper to convert the procedure to protobuf.    *<p/>    * Used by ProcedureStore implementations.    */
specifier|public
specifier|static
name|ProcedureProtos
operator|.
name|Procedure
name|convertToProtoProcedure
parameter_list|(
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|proc
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|validateClass
argument_list|(
name|proc
argument_list|)
expr_stmt|;
specifier|final
name|ProcedureProtos
operator|.
name|Procedure
operator|.
name|Builder
name|builder
init|=
name|ProcedureProtos
operator|.
name|Procedure
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClassName
argument_list|(
name|proc
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|setProcId
argument_list|(
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|)
operator|.
name|setState
argument_list|(
name|proc
operator|.
name|getState
argument_list|()
argument_list|)
operator|.
name|setSubmittedTime
argument_list|(
name|proc
operator|.
name|getSubmittedTime
argument_list|()
argument_list|)
operator|.
name|setLastUpdate
argument_list|(
name|proc
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|proc
operator|.
name|hasParent
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setParentId
argument_list|(
name|proc
operator|.
name|getParentProcId
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proc
operator|.
name|hasTimeout
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setTimeout
argument_list|(
name|proc
operator|.
name|getTimeout
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proc
operator|.
name|hasOwner
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setOwner
argument_list|(
name|proc
operator|.
name|getOwner
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|final
name|int
index|[]
name|stackIds
init|=
name|proc
operator|.
name|getStackIndexes
argument_list|()
decl_stmt|;
if|if
condition|(
name|stackIds
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|stackIds
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|builder
operator|.
name|addStackId
argument_list|(
name|stackIds
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|proc
operator|.
name|hasException
argument_list|()
condition|)
block|{
name|RemoteProcedureException
name|exception
init|=
name|proc
operator|.
name|getException
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setException
argument_list|(
name|RemoteProcedureException
operator|.
name|toProto
argument_list|(
name|exception
operator|.
name|getSource
argument_list|()
argument_list|,
name|exception
operator|.
name|getCause
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|byte
index|[]
name|result
init|=
name|proc
operator|.
name|getResult
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setResult
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|result
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ProcedureStateSerializer
name|serializer
init|=
operator|new
name|StateSerializer
argument_list|(
name|builder
argument_list|)
decl_stmt|;
name|proc
operator|.
name|serializeStateData
argument_list|(
name|serializer
argument_list|)
expr_stmt|;
if|if
condition|(
name|proc
operator|.
name|getNonceKey
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setNonceGroup
argument_list|(
name|proc
operator|.
name|getNonceKey
argument_list|()
operator|.
name|getNonceGroup
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setNonce
argument_list|(
name|proc
operator|.
name|getNonceKey
argument_list|()
operator|.
name|getNonce
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proc
operator|.
name|hasLock
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setLocked
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proc
operator|.
name|isBypass
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setBypass
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Helper to convert the protobuf procedure.    *<p/>    * Used by ProcedureStore implementations.    *<p/>    * TODO: OPTIMIZATION: some of the field never change during the execution (e.g. className,    * procId, parentId, ...). We can split in 'data' and 'state', and the store may take advantage of    * it by storing the data only on insert().    */
specifier|public
specifier|static
name|Procedure
argument_list|<
name|?
argument_list|>
name|convertToProcedure
parameter_list|(
name|ProcedureProtos
operator|.
name|Procedure
name|proto
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Procedure from class name
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
init|=
name|newProcedure
argument_list|(
name|proto
operator|.
name|getClassName
argument_list|()
argument_list|)
decl_stmt|;
comment|// set fields
name|proc
operator|.
name|setProcId
argument_list|(
name|proto
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|proc
operator|.
name|setState
argument_list|(
name|proto
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|proc
operator|.
name|setSubmittedTime
argument_list|(
name|proto
operator|.
name|getSubmittedTime
argument_list|()
argument_list|)
expr_stmt|;
name|proc
operator|.
name|setLastUpdate
argument_list|(
name|proto
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|proto
operator|.
name|hasParentId
argument_list|()
condition|)
block|{
name|proc
operator|.
name|setParentProcId
argument_list|(
name|proto
operator|.
name|getParentId
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasOwner
argument_list|()
condition|)
block|{
name|proc
operator|.
name|setOwner
argument_list|(
name|proto
operator|.
name|getOwner
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasTimeout
argument_list|()
condition|)
block|{
name|proc
operator|.
name|setTimeout
argument_list|(
name|proto
operator|.
name|getTimeout
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|getStackIdCount
argument_list|()
operator|>
literal|0
condition|)
block|{
name|proc
operator|.
name|setStackIndexes
argument_list|(
name|proto
operator|.
name|getStackIdList
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasException
argument_list|()
condition|)
block|{
assert|assert
name|proc
operator|.
name|getState
argument_list|()
operator|==
name|ProcedureProtos
operator|.
name|ProcedureState
operator|.
name|FAILED
operator|||
name|proc
operator|.
name|getState
argument_list|()
operator|==
name|ProcedureProtos
operator|.
name|ProcedureState
operator|.
name|ROLLEDBACK
operator|:
literal|"The procedure must be failed (waiting to rollback) or rolledback"
assert|;
name|proc
operator|.
name|setFailure
argument_list|(
name|RemoteProcedureException
operator|.
name|fromProto
argument_list|(
name|proto
operator|.
name|getException
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasResult
argument_list|()
condition|)
block|{
name|proc
operator|.
name|setResult
argument_list|(
name|proto
operator|.
name|getResult
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|getNonce
argument_list|()
operator|!=
name|HConstants
operator|.
name|NO_NONCE
condition|)
block|{
name|proc
operator|.
name|setNonceKey
argument_list|(
operator|new
name|NonceKey
argument_list|(
name|proto
operator|.
name|getNonceGroup
argument_list|()
argument_list|,
name|proto
operator|.
name|getNonce
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|getLocked
argument_list|()
condition|)
block|{
name|proc
operator|.
name|lockedWhenLoading
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|getBypass
argument_list|()
condition|)
block|{
name|proc
operator|.
name|bypass
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|ProcedureStateSerializer
name|serializer
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|proto
operator|.
name|getStateMessageCount
argument_list|()
operator|>
literal|0
condition|)
block|{
name|serializer
operator|=
operator|new
name|StateSerializer
argument_list|(
name|proto
operator|.
name|toBuilder
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|proto
operator|.
name|hasStateData
argument_list|()
condition|)
block|{
name|InputStream
name|inputStream
init|=
name|proto
operator|.
name|getStateData
argument_list|()
operator|.
name|newInput
argument_list|()
decl_stmt|;
name|serializer
operator|=
operator|new
name|CompatStateSerializer
argument_list|(
name|inputStream
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|serializer
operator|!=
literal|null
condition|)
block|{
name|proc
operator|.
name|deserializeStateData
argument_list|(
name|serializer
argument_list|)
expr_stmt|;
block|}
return|return
name|proc
return|;
block|}
comment|// ==========================================================================
comment|//  convert from LockedResource object
comment|// ==========================================================================
specifier|public
specifier|static
name|LockServiceProtos
operator|.
name|LockedResourceType
name|convertToProtoResourceType
parameter_list|(
name|LockedResourceType
name|resourceType
parameter_list|)
block|{
return|return
name|LockServiceProtos
operator|.
name|LockedResourceType
operator|.
name|valueOf
argument_list|(
name|resourceType
operator|.
name|name
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|LockServiceProtos
operator|.
name|LockType
name|convertToProtoLockType
parameter_list|(
name|LockType
name|lockType
parameter_list|)
block|{
return|return
name|LockServiceProtos
operator|.
name|LockType
operator|.
name|valueOf
argument_list|(
name|lockType
operator|.
name|name
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|LockServiceProtos
operator|.
name|LockedResource
name|convertToProtoLockedResource
parameter_list|(
name|LockedResource
name|lockedResource
parameter_list|)
throws|throws
name|IOException
block|{
name|LockServiceProtos
operator|.
name|LockedResource
operator|.
name|Builder
name|builder
init|=
name|LockServiceProtos
operator|.
name|LockedResource
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setResourceType
argument_list|(
name|convertToProtoResourceType
argument_list|(
name|lockedResource
operator|.
name|getResourceType
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setResourceName
argument_list|(
name|lockedResource
operator|.
name|getResourceName
argument_list|()
argument_list|)
operator|.
name|setLockType
argument_list|(
name|convertToProtoLockType
argument_list|(
name|lockedResource
operator|.
name|getLockType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Procedure
argument_list|<
name|?
argument_list|>
name|exclusiveLockOwnerProcedure
init|=
name|lockedResource
operator|.
name|getExclusiveLockOwnerProcedure
argument_list|()
decl_stmt|;
if|if
condition|(
name|exclusiveLockOwnerProcedure
operator|!=
literal|null
condition|)
block|{
name|ProcedureProtos
operator|.
name|Procedure
name|exclusiveLockOwnerProcedureProto
init|=
name|convertToProtoProcedure
argument_list|(
name|exclusiveLockOwnerProcedure
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setExclusiveLockOwnerProcedure
argument_list|(
name|exclusiveLockOwnerProcedureProto
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setSharedLockCount
argument_list|(
name|lockedResource
operator|.
name|getSharedLockCount
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Procedure
argument_list|<
name|?
argument_list|>
name|waitingProcedure
range|:
name|lockedResource
operator|.
name|getWaitingProcedures
argument_list|()
control|)
block|{
name|ProcedureProtos
operator|.
name|Procedure
name|waitingProcedureProto
init|=
name|convertToProtoProcedure
argument_list|(
name|waitingProcedure
argument_list|)
decl_stmt|;
name|builder
operator|.
name|addWaitingProcedures
argument_list|(
name|waitingProcedureProto
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|public
specifier|static
specifier|final
name|String
name|PROCEDURE_RETRY_SLEEP_INTERVAL_MS
init|=
literal|"hbase.procedure.retry.sleep.interval.ms"
decl_stmt|;
comment|// default to 1 second
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_PROCEDURE_RETRY_SLEEP_INTERVAL_MS
init|=
literal|1000
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PROCEDURE_RETRY_MAX_SLEEP_TIME_MS
init|=
literal|"hbase.procedure.retry.max.sleep.time.ms"
decl_stmt|;
comment|// default to 10 minutes
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_PROCEDURE_RETRY_MAX_SLEEP_TIME_MS
init|=
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|toMillis
argument_list|(
literal|10
argument_list|)
decl_stmt|;
comment|/**    * Get a retry counter for getting the backoff time. We will use the    * {@link ExponentialBackoffPolicyWithLimit} policy, and the base unit is 1 second, max sleep time    * is 10 minutes by default.    *<p/>    * For UTs, you can set the {@link #PROCEDURE_RETRY_SLEEP_INTERVAL_MS} and    * {@link #PROCEDURE_RETRY_MAX_SLEEP_TIME_MS} to make more frequent retry so your UT will not    * timeout.    */
specifier|public
specifier|static
name|RetryCounter
name|createRetryCounter
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|long
name|sleepIntervalMs
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|PROCEDURE_RETRY_SLEEP_INTERVAL_MS
argument_list|,
name|DEFAULT_PROCEDURE_RETRY_SLEEP_INTERVAL_MS
argument_list|)
decl_stmt|;
name|long
name|maxSleepTimeMs
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|PROCEDURE_RETRY_MAX_SLEEP_TIME_MS
argument_list|,
name|DEFAULT_PROCEDURE_RETRY_MAX_SLEEP_TIME_MS
argument_list|)
decl_stmt|;
name|RetryConfig
name|retryConfig
init|=
operator|new
name|RetryConfig
argument_list|()
operator|.
name|setSleepInterval
argument_list|(
name|sleepIntervalMs
argument_list|)
operator|.
name|setMaxSleepTime
argument_list|(
name|maxSleepTimeMs
argument_list|)
operator|.
name|setBackoffPolicy
argument_list|(
operator|new
name|ExponentialBackoffPolicyWithLimit
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|RetryCounter
argument_list|(
name|retryConfig
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isFinished
parameter_list|(
name|ProcedureProtos
operator|.
name|Procedure
name|proc
parameter_list|)
block|{
if|if
condition|(
operator|!
name|proc
operator|.
name|hasParentId
argument_list|()
condition|)
block|{
switch|switch
condition|(
name|proc
operator|.
name|getState
argument_list|()
condition|)
block|{
case|case
name|ROLLEDBACK
case|:
case|case
name|SUCCESS
case|:
return|return
literal|true
return|;
default|default:
break|break;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

