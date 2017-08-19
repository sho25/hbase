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
name|security
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
name|hbase
operator|.
name|shaded
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelPipeline
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
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|LengthFieldBasedFrameDecoder
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
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|Sasl
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
name|hadoop
operator|.
name|security
operator|.
name|token
operator|.
name|Token
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
name|security
operator|.
name|token
operator|.
name|TokenIdentifier
import|;
end_import

begin_comment
comment|/**  * Implement SASL logic for netty rpc client.  * @since 2.0.0  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|NettyHBaseSaslRpcClient
extends|extends
name|AbstractHBaseSaslRpcClient
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
name|NettyHBaseSaslRpcClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|NettyHBaseSaslRpcClient
parameter_list|(
name|AuthMethod
name|method
parameter_list|,
name|Token
argument_list|<
name|?
extends|extends
name|TokenIdentifier
argument_list|>
name|token
parameter_list|,
name|String
name|serverPrincipal
parameter_list|,
name|boolean
name|fallbackAllowed
parameter_list|,
name|String
name|rpcProtection
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|method
argument_list|,
name|token
argument_list|,
name|serverPrincipal
argument_list|,
name|fallbackAllowed
argument_list|,
name|rpcProtection
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setupSaslHandler
parameter_list|(
name|ChannelPipeline
name|p
parameter_list|)
block|{
name|String
name|qop
init|=
operator|(
name|String
operator|)
name|saslClient
operator|.
name|getNegotiatedProperty
argument_list|(
name|Sasl
operator|.
name|QOP
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"SASL client context established. Negotiated QoP: "
operator|+
name|qop
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|qop
operator|==
literal|null
operator|||
literal|"auth"
operator|.
name|equalsIgnoreCase
argument_list|(
name|qop
argument_list|)
condition|)
block|{
return|return;
block|}
comment|// add wrap and unwrap handlers to pipeline.
name|p
operator|.
name|addFirst
argument_list|(
operator|new
name|SaslWrapHandler
argument_list|(
name|saslClient
argument_list|)
argument_list|,
operator|new
name|LengthFieldBasedFrameDecoder
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|0
argument_list|,
literal|4
argument_list|,
literal|0
argument_list|,
literal|4
argument_list|)
argument_list|,
operator|new
name|SaslUnwrapHandler
argument_list|(
name|saslClient
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getSaslQOP
parameter_list|()
block|{
return|return
operator|(
name|String
operator|)
name|saslClient
operator|.
name|getNegotiatedProperty
argument_list|(
name|Sasl
operator|.
name|QOP
argument_list|)
return|;
block|}
block|}
end_class

end_unit

