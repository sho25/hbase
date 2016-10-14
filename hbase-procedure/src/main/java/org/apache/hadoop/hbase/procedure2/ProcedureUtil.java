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
name|ProcedureInfo
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
name|ProcedureState
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
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
name|ProcedureProtos
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
name|ForeignExceptionUtil
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
specifier|public
specifier|static
name|Procedure
name|newProcedure
parameter_list|(
specifier|final
name|String
name|className
parameter_list|)
throws|throws
name|BadProcedureException
block|{
try|try
block|{
specifier|final
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
specifier|final
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
operator|(
name|Procedure
operator|)
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
specifier|public
specifier|static
name|void
name|validateClass
parameter_list|(
specifier|final
name|Procedure
name|proc
parameter_list|)
throws|throws
name|BadProcedureException
block|{
try|try
block|{
specifier|final
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
specifier|final
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
comment|/**    * Helper to convert the procedure to protobuf.    * Used by ProcedureStore implementations.    */
specifier|public
specifier|static
name|ProcedureProtos
operator|.
name|Procedure
name|convertToProtoProcedure
parameter_list|(
specifier|final
name|Procedure
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
name|setStartTime
argument_list|(
name|proc
operator|.
name|getStartTime
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
specifier|final
name|ByteString
operator|.
name|Output
name|stateStream
init|=
name|ByteString
operator|.
name|newOutput
argument_list|()
decl_stmt|;
try|try
block|{
name|proc
operator|.
name|serializeStateData
argument_list|(
name|stateStream
argument_list|)
expr_stmt|;
if|if
condition|(
name|stateStream
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|setStateData
argument_list|(
name|stateStream
operator|.
name|toByteString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|stateStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Helper to convert the protobuf procedure.    * Used by ProcedureStore implementations.    *    * TODO: OPTIMIZATION: some of the field never change during the execution    *                     (e.g. className, procId, parentId, ...).    *                     We can split in 'data' and 'state', and the store    *                     may take advantage of it by storing the data only on insert().    */
specifier|public
specifier|static
name|Procedure
name|convertToProcedure
parameter_list|(
specifier|final
name|ProcedureProtos
operator|.
name|Procedure
name|proto
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Procedure from class name
specifier|final
name|Procedure
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
name|setStartTime
argument_list|(
name|proto
operator|.
name|getStartTime
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
name|FINISHED
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
comment|// we want to call deserialize even when the stream is empty, mainly for testing.
name|proc
operator|.
name|deserializeStateData
argument_list|(
name|proto
operator|.
name|getStateData
argument_list|()
operator|.
name|newInput
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|proc
return|;
block|}
comment|// ==========================================================================
comment|//  convert to and from ProcedureInfo object
comment|// ==========================================================================
comment|/**    * @return Convert the current {@link ProcedureInfo} into a Protocol Buffers Procedure    * instance.    */
specifier|public
specifier|static
name|ProcedureProtos
operator|.
name|Procedure
name|convertToProtoProcedure
parameter_list|(
specifier|final
name|ProcedureInfo
name|procInfo
parameter_list|)
block|{
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
decl_stmt|;
name|builder
operator|.
name|setClassName
argument_list|(
name|procInfo
operator|.
name|getProcName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setProcId
argument_list|(
name|procInfo
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setStartTime
argument_list|(
name|procInfo
operator|.
name|getStartTime
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setState
argument_list|(
name|ProcedureProtos
operator|.
name|ProcedureState
operator|.
name|valueOf
argument_list|(
name|procInfo
operator|.
name|getProcState
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setLastUpdate
argument_list|(
name|procInfo
operator|.
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|procInfo
operator|.
name|hasParentId
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setParentId
argument_list|(
name|procInfo
operator|.
name|getParentId
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|procInfo
operator|.
name|hasOwner
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setOwner
argument_list|(
name|procInfo
operator|.
name|getProcOwner
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|procInfo
operator|.
name|isFailed
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setException
argument_list|(
name|ForeignExceptionUtil
operator|.
name|toProtoForeignException
argument_list|(
name|procInfo
operator|.
name|getException
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|procInfo
operator|.
name|hasResultData
argument_list|()
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
name|procInfo
operator|.
name|getResult
argument_list|()
argument_list|)
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
comment|/**    * Helper to convert the protobuf object.    * @return Convert the current Protocol Buffers Procedure to {@link ProcedureInfo}    * instance.    */
specifier|public
specifier|static
name|ProcedureInfo
name|convertToProcedureInfo
parameter_list|(
specifier|final
name|ProcedureProtos
operator|.
name|Procedure
name|procProto
parameter_list|)
block|{
name|NonceKey
name|nonceKey
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|procProto
operator|.
name|getNonce
argument_list|()
operator|!=
name|HConstants
operator|.
name|NO_NONCE
condition|)
block|{
name|nonceKey
operator|=
operator|new
name|NonceKey
argument_list|(
name|procProto
operator|.
name|getNonceGroup
argument_list|()
argument_list|,
name|procProto
operator|.
name|getNonce
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ProcedureInfo
argument_list|(
name|procProto
operator|.
name|getProcId
argument_list|()
argument_list|,
name|procProto
operator|.
name|getClassName
argument_list|()
argument_list|,
name|procProto
operator|.
name|hasOwner
argument_list|()
condition|?
name|procProto
operator|.
name|getOwner
argument_list|()
else|:
literal|null
argument_list|,
name|convertToProcedureState
argument_list|(
name|procProto
operator|.
name|getState
argument_list|()
argument_list|)
argument_list|,
name|procProto
operator|.
name|hasParentId
argument_list|()
condition|?
name|procProto
operator|.
name|getParentId
argument_list|()
else|:
operator|-
literal|1
argument_list|,
name|nonceKey
argument_list|,
name|procProto
operator|.
name|hasException
argument_list|()
condition|?
name|ForeignExceptionUtil
operator|.
name|toIOException
argument_list|(
name|procProto
operator|.
name|getException
argument_list|()
argument_list|)
else|:
literal|null
argument_list|,
name|procProto
operator|.
name|getLastUpdate
argument_list|()
argument_list|,
name|procProto
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|procProto
operator|.
name|hasResult
argument_list|()
condition|?
name|procProto
operator|.
name|getResult
argument_list|()
operator|.
name|toByteArray
argument_list|()
else|:
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ProcedureState
name|convertToProcedureState
parameter_list|(
name|ProcedureProtos
operator|.
name|ProcedureState
name|state
parameter_list|)
block|{
return|return
name|ProcedureState
operator|.
name|valueOf
argument_list|(
name|state
operator|.
name|name
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ProcedureInfo
name|convertToProcedureInfo
parameter_list|(
specifier|final
name|Procedure
name|proc
parameter_list|)
block|{
return|return
name|convertToProcedureInfo
argument_list|(
name|proc
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Helper to create the ProcedureInfo from Procedure.    */
specifier|public
specifier|static
name|ProcedureInfo
name|convertToProcedureInfo
parameter_list|(
specifier|final
name|Procedure
name|proc
parameter_list|,
specifier|final
name|NonceKey
name|nonceKey
parameter_list|)
block|{
specifier|final
name|RemoteProcedureException
name|exception
init|=
name|proc
operator|.
name|hasException
argument_list|()
condition|?
name|proc
operator|.
name|getException
argument_list|()
else|:
literal|null
decl_stmt|;
return|return
operator|new
name|ProcedureInfo
argument_list|(
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|,
name|proc
operator|.
name|toStringClass
argument_list|()
argument_list|,
name|proc
operator|.
name|getOwner
argument_list|()
argument_list|,
name|convertToProcedureState
argument_list|(
name|proc
operator|.
name|getState
argument_list|()
argument_list|)
argument_list|,
name|proc
operator|.
name|hasParent
argument_list|()
condition|?
name|proc
operator|.
name|getParentProcId
argument_list|()
else|:
operator|-
literal|1
argument_list|,
name|nonceKey
argument_list|,
name|exception
operator|!=
literal|null
condition|?
name|exception
operator|.
name|unwrapRemoteIOException
argument_list|()
else|:
literal|null
argument_list|,
name|proc
operator|.
name|getLastUpdate
argument_list|()
argument_list|,
name|proc
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|proc
operator|.
name|getResult
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

