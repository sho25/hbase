begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license  * agreements. See the NOTICE file distributed with this work for additional information regarding  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License. You may obtain a  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable  * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  * for the specific language governing permissions and limitations under the License.  */
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
name|net
operator|.
name|Socket
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|net
operator|.
name|ssl
operator|.
name|SSLSocket
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|rmi
operator|.
name|ssl
operator|.
name|SslRMIClientSocketFactory
import|;
end_import

begin_comment
comment|/**  * Avoid SSL V3.0 "Poodle" Vulnerability - CVE-2014-3566  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|public
class|class
name|SslRMIClientSocketFactorySecure
extends|extends
name|SslRMIClientSocketFactory
block|{
annotation|@
name|Override
specifier|public
name|Socket
name|createSocket
parameter_list|(
name|String
name|host
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
name|SSLSocket
name|socket
init|=
operator|(
name|SSLSocket
operator|)
name|super
operator|.
name|createSocket
argument_list|(
name|host
argument_list|,
name|port
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|secureProtocols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|p
range|:
name|socket
operator|.
name|getEnabledProtocols
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|p
operator|.
name|contains
argument_list|(
literal|"SSLv3"
argument_list|)
condition|)
block|{
name|secureProtocols
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
block|}
name|socket
operator|.
name|setEnabledProtocols
argument_list|(
name|secureProtocols
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|secureProtocols
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|socket
return|;
block|}
block|}
end_class

end_unit

