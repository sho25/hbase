begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
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
name|ipc
operator|.
name|CoprocessorProtocol
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
name|VersionedProtocol
import|;
end_import

begin_comment
comment|/**  * This abstract class provides default implementation of an Endpoint.  * It also maintains a CoprocessorEnvironment object which can be  * used to access region resource.  *  * It's recommended to use this abstract class to implement your Endpoint.  * However you still can just implement the interface CoprocessorProtocol  * and Coprocessor to develop an Endpoint. But you won't be able to access  * the region related resource, i.e., CoprocessorEnvironment.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|BaseEndpointCoprocessor
implements|implements
name|Coprocessor
implements|,
name|CoprocessorProtocol
implements|,
name|VersionedProtocol
block|{
comment|/**    * This Interfaces' version. Version changes when the Interface changes.    */
comment|// All HBase Interfaces used derive from HBaseRPCProtocolVersion.  It
comment|// maintained a single global version number on all HBase Interfaces.  This
comment|// meant all HBase RPC was broke though only one of the three RPC Interfaces
comment|// had changed.  This has since been undone.
specifier|public
specifier|static
specifier|final
name|long
name|VERSION
init|=
literal|28L
decl_stmt|;
specifier|private
name|CoprocessorEnvironment
name|env
decl_stmt|;
comment|/**    * @return env Coprocessor environment.    */
specifier|public
name|CoprocessorEnvironment
name|getEnvironment
parameter_list|()
block|{
return|return
name|env
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
block|{
name|this
operator|.
name|env
operator|=
name|env
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|long
name|getProtocolVersion
parameter_list|(
name|String
name|protocol
parameter_list|,
name|long
name|clientVersion
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|VERSION
return|;
block|}
block|}
end_class

end_unit

