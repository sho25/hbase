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
name|ipc
operator|.
name|RemoteException
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|ErrorHandlingProtos
operator|.
name|ForeignExceptionMessage
import|;
end_import

begin_comment
comment|/**  * A RemoteProcedureException is an exception from another thread or process.  *<p>  * RemoteProcedureExceptions are sent to 'remote' peers to signal an abort in the face of failures.  * When serialized for transmission we encode using Protobufs to ensure version compatibility.  *<p>  * RemoteProcedureException exceptions contain a Throwable as its cause.  * This can be a "regular" exception generated locally or a ProxyThrowable that is a representation  * of the original exception created on original 'remote' source.  These ProxyThrowables have their  * their stacks traces and messages overridden to reflect the original 'remote' exception.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|public
class|class
name|RemoteProcedureException
extends|extends
name|ProcedureException
block|{
comment|/**    * Name of the throwable's source such as a host or thread name.  Must be non-null.    */
specifier|private
specifier|final
name|String
name|source
decl_stmt|;
comment|/**    * Create a new RemoteProcedureException that can be serialized.    * It is assumed that this came form a local source.    * @param source the host or thread name of the source    * @param cause the actual cause of the exception    */
specifier|public
name|RemoteProcedureException
parameter_list|(
name|String
name|source
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|cause
argument_list|)
expr_stmt|;
assert|assert
name|source
operator|!=
literal|null
assert|;
assert|assert
name|cause
operator|!=
literal|null
assert|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
block|}
specifier|public
name|String
name|getSource
parameter_list|()
block|{
return|return
name|source
return|;
block|}
specifier|public
name|Exception
name|unwrapRemoteException
parameter_list|()
block|{
specifier|final
name|Throwable
name|cause
init|=
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|instanceof
name|RemoteException
condition|)
block|{
return|return
operator|(
operator|(
name|RemoteException
operator|)
name|cause
operator|)
operator|.
name|unwrapRemoteException
argument_list|()
return|;
block|}
if|if
condition|(
name|cause
operator|instanceof
name|Exception
condition|)
block|{
return|return
operator|(
name|Exception
operator|)
name|cause
return|;
block|}
return|return
operator|new
name|Exception
argument_list|(
name|cause
argument_list|)
return|;
block|}
comment|// NOTE: Does not throw DoNotRetryIOE because it does not
comment|// have access (DNRIOE is in the client module). Use
comment|// MasterProcedureUtil.unwrapRemoteIOException if need to
comment|// throw DNRIOE.
specifier|public
name|IOException
name|unwrapRemoteIOException
parameter_list|()
block|{
specifier|final
name|Exception
name|cause
init|=
name|unwrapRemoteException
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|instanceof
name|IOException
condition|)
block|{
return|return
operator|(
name|IOException
operator|)
name|cause
return|;
block|}
return|return
operator|new
name|IOException
argument_list|(
name|cause
argument_list|)
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
name|className
init|=
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
name|className
operator|+
literal|" via "
operator|+
name|getSource
argument_list|()
operator|+
literal|":"
operator|+
name|getLocalizedMessage
argument_list|()
return|;
block|}
comment|/**    * Converts a RemoteProcedureException to an array of bytes.    * @param source the name of the external exception source    * @param t the "local" external exception (local)    * @return protobuf serialized version of RemoteProcedureException    */
specifier|public
specifier|static
name|byte
index|[]
name|serialize
parameter_list|(
name|String
name|source
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
return|return
name|toProto
argument_list|(
name|source
argument_list|,
name|t
argument_list|)
operator|.
name|toByteArray
argument_list|()
return|;
block|}
comment|/**    * Takes a series of bytes and tries to generate an RemoteProcedureException instance for it.    * @param bytes the bytes to generate the {@link RemoteProcedureException} from    * @return the ForeignExcpetion instance    * @throws IOException if there was deserialization problem this is thrown.    */
specifier|public
specifier|static
name|RemoteProcedureException
name|deserialize
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fromProto
argument_list|(
name|ForeignExceptionMessage
operator|.
name|parseFrom
argument_list|(
name|bytes
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|ForeignExceptionMessage
name|convert
parameter_list|()
block|{
return|return
name|ForeignExceptionUtil
operator|.
name|toProtoForeignException
argument_list|(
name|getSource
argument_list|()
argument_list|,
name|getCause
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ForeignExceptionMessage
name|toProto
parameter_list|(
name|String
name|source
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
return|return
name|ForeignExceptionUtil
operator|.
name|toProtoForeignException
argument_list|(
name|source
argument_list|,
name|t
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|RemoteProcedureException
name|fromProto
parameter_list|(
specifier|final
name|ForeignExceptionMessage
name|eem
parameter_list|)
block|{
return|return
operator|new
name|RemoteProcedureException
argument_list|(
name|eem
operator|.
name|getSource
argument_list|()
argument_list|,
name|ForeignExceptionUtil
operator|.
name|toException
argument_list|(
name|eem
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

