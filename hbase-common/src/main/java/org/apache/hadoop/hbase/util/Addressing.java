begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|Inet4Address
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|Inet6Address
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|NetworkInterface
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Enumeration
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

begin_comment
comment|/**  * Utility for network addresses, resolving and naming.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Addressing
block|{
specifier|public
specifier|static
specifier|final
name|String
name|VALID_PORT_REGEX
init|=
literal|"[\\d]+"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HOSTNAME_PORT_SEPARATOR
init|=
literal|":"
decl_stmt|;
comment|/**    * @param hostAndPort Formatted as<code>&lt;hostname> ':'&lt;port></code>    * @return An InetSocketInstance    */
specifier|public
specifier|static
name|InetSocketAddress
name|createInetSocketAddressFromHostAndPortStr
parameter_list|(
specifier|final
name|String
name|hostAndPort
parameter_list|)
block|{
return|return
operator|new
name|InetSocketAddress
argument_list|(
name|parseHostname
argument_list|(
name|hostAndPort
argument_list|)
argument_list|,
name|parsePort
argument_list|(
name|hostAndPort
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param hostname Server hostname    * @param port Server port    * @return Returns a concatenation of<code>hostname</code> and    *<code>port</code> in following    * form:<code>&lt;hostname> ':'&lt;port></code>.  For example, if hostname    * is<code>example.org</code> and port is 1234, this method will return    *<code>example.org:1234</code>    */
specifier|public
specifier|static
name|String
name|createHostAndPortStr
parameter_list|(
specifier|final
name|String
name|hostname
parameter_list|,
specifier|final
name|int
name|port
parameter_list|)
block|{
return|return
name|hostname
operator|+
name|HOSTNAME_PORT_SEPARATOR
operator|+
name|port
return|;
block|}
comment|/**    * @param hostAndPort Formatted as<code>&lt;hostname> ':'&lt;port></code>    * @return The hostname portion of<code>hostAndPort</code>    */
specifier|public
specifier|static
name|String
name|parseHostname
parameter_list|(
specifier|final
name|String
name|hostAndPort
parameter_list|)
block|{
name|int
name|colonIndex
init|=
name|hostAndPort
operator|.
name|lastIndexOf
argument_list|(
name|HOSTNAME_PORT_SEPARATOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|colonIndex
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Not a host:port pair: "
operator|+
name|hostAndPort
argument_list|)
throw|;
block|}
return|return
name|hostAndPort
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|colonIndex
argument_list|)
return|;
block|}
comment|/**    * @param hostAndPort Formatted as<code>&lt;hostname> ':'&lt;port></code>    * @return The port portion of<code>hostAndPort</code>    */
specifier|public
specifier|static
name|int
name|parsePort
parameter_list|(
specifier|final
name|String
name|hostAndPort
parameter_list|)
block|{
name|int
name|colonIndex
init|=
name|hostAndPort
operator|.
name|lastIndexOf
argument_list|(
name|HOSTNAME_PORT_SEPARATOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|colonIndex
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Not a host:port pair: "
operator|+
name|hostAndPort
argument_list|)
throw|;
block|}
return|return
name|Integer
operator|.
name|parseInt
argument_list|(
name|hostAndPort
operator|.
name|substring
argument_list|(
name|colonIndex
operator|+
literal|1
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|InetAddress
name|getIpAddress
parameter_list|()
throws|throws
name|SocketException
block|{
comment|// Before we connect somewhere, we cannot be sure about what we'd be bound to; however,
comment|// we only connect when the message where client ID is, is long constructed. Thus,
comment|// just use whichever IP address we can find.
name|Enumeration
argument_list|<
name|NetworkInterface
argument_list|>
name|interfaces
init|=
name|NetworkInterface
operator|.
name|getNetworkInterfaces
argument_list|()
decl_stmt|;
while|while
condition|(
name|interfaces
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|NetworkInterface
name|current
init|=
name|interfaces
operator|.
name|nextElement
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|current
operator|.
name|isUp
argument_list|()
operator|||
name|current
operator|.
name|isLoopback
argument_list|()
operator|||
name|current
operator|.
name|isVirtual
argument_list|()
condition|)
continue|continue;
name|Enumeration
argument_list|<
name|InetAddress
argument_list|>
name|addresses
init|=
name|current
operator|.
name|getInetAddresses
argument_list|()
decl_stmt|;
while|while
condition|(
name|addresses
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|InetAddress
name|addr
init|=
name|addresses
operator|.
name|nextElement
argument_list|()
decl_stmt|;
if|if
condition|(
name|addr
operator|.
name|isLoopbackAddress
argument_list|()
condition|)
continue|continue;
if|if
condition|(
name|addr
operator|instanceof
name|Inet4Address
operator|||
name|addr
operator|instanceof
name|Inet6Address
condition|)
block|{
return|return
name|addr
return|;
block|}
block|}
block|}
throw|throw
operator|new
name|SocketException
argument_list|(
literal|"Can't get our ip address, interfaces are: "
operator|+
name|interfaces
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

