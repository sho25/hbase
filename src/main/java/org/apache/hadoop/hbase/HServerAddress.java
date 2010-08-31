begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|WritableComparable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|InetAddress
import|;
end_import

begin_comment
comment|/**  * HServerAddress is a "label" for a HBase server made of host and port number.  */
end_comment

begin_class
specifier|public
class|class
name|HServerAddress
implements|implements
name|WritableComparable
argument_list|<
name|HServerAddress
argument_list|>
block|{
specifier|private
name|InetSocketAddress
name|address
decl_stmt|;
name|String
name|stringValue
decl_stmt|;
specifier|public
name|HServerAddress
parameter_list|()
block|{
name|this
operator|.
name|address
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|stringValue
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Construct an instance from an {@link InetSocketAddress}.    * @param address InetSocketAddress of server    */
specifier|public
name|HServerAddress
parameter_list|(
name|InetSocketAddress
name|address
parameter_list|)
block|{
name|this
operator|.
name|address
operator|=
name|address
expr_stmt|;
name|this
operator|.
name|stringValue
operator|=
name|address
operator|.
name|getAddress
argument_list|()
operator|.
name|getHostName
argument_list|()
operator|+
literal|":"
operator|+
name|address
operator|.
name|getPort
argument_list|()
expr_stmt|;
name|checkBindAddressCanBeResolved
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param hostAndPort Hostname and port formatted as<code>&lt;hostname> ':'&lt;port></code>    */
specifier|public
name|HServerAddress
parameter_list|(
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
literal|':'
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
name|String
name|host
init|=
name|hostAndPort
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|colonIndex
argument_list|)
decl_stmt|;
name|int
name|port
init|=
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
decl_stmt|;
name|this
operator|.
name|address
operator|=
operator|new
name|InetSocketAddress
argument_list|(
name|host
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|this
operator|.
name|stringValue
operator|=
name|hostAndPort
expr_stmt|;
name|checkBindAddressCanBeResolved
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param bindAddress Hostname    * @param port Port number    */
specifier|public
name|HServerAddress
parameter_list|(
name|String
name|bindAddress
parameter_list|,
name|int
name|port
parameter_list|)
block|{
name|this
operator|.
name|address
operator|=
operator|new
name|InetSocketAddress
argument_list|(
name|bindAddress
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|this
operator|.
name|stringValue
operator|=
name|bindAddress
operator|+
literal|":"
operator|+
name|port
expr_stmt|;
name|checkBindAddressCanBeResolved
argument_list|()
expr_stmt|;
block|}
comment|/**    * Copy-constructor.    * @param other HServerAddress to copy from    */
specifier|public
name|HServerAddress
parameter_list|(
name|HServerAddress
name|other
parameter_list|)
block|{
name|String
name|bindAddress
init|=
name|other
operator|.
name|getBindAddress
argument_list|()
decl_stmt|;
name|int
name|port
init|=
name|other
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|this
operator|.
name|address
operator|=
operator|new
name|InetSocketAddress
argument_list|(
name|bindAddress
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|stringValue
operator|=
name|other
operator|.
name|stringValue
expr_stmt|;
name|checkBindAddressCanBeResolved
argument_list|()
expr_stmt|;
block|}
comment|/** @return Bind address */
specifier|public
name|String
name|getBindAddress
parameter_list|()
block|{
specifier|final
name|InetAddress
name|addr
init|=
name|address
operator|.
name|getAddress
argument_list|()
decl_stmt|;
if|if
condition|(
name|addr
operator|!=
literal|null
condition|)
block|{
return|return
name|addr
operator|.
name|getHostAddress
argument_list|()
return|;
block|}
else|else
block|{
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HServerAddress
operator|.
name|class
argument_list|)
operator|.
name|error
argument_list|(
literal|"Could not resolve the"
operator|+
literal|" DNS name of "
operator|+
name|stringValue
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|void
name|checkBindAddressCanBeResolved
parameter_list|()
block|{
if|if
condition|(
name|getBindAddress
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not resolve the"
operator|+
literal|" DNS name of "
operator|+
name|stringValue
argument_list|)
throw|;
block|}
block|}
comment|/** @return Port number */
specifier|public
name|int
name|getPort
parameter_list|()
block|{
return|return
name|address
operator|.
name|getPort
argument_list|()
return|;
block|}
comment|/** @return Hostname */
specifier|public
name|String
name|getHostname
parameter_list|()
block|{
return|return
name|address
operator|.
name|getHostName
argument_list|()
return|;
block|}
comment|/** @return The InetSocketAddress */
specifier|public
name|InetSocketAddress
name|getInetSocketAddress
parameter_list|()
block|{
return|return
name|address
return|;
block|}
comment|/**    * @return String formatted as<code>&lt;bind address> ':'&lt;port></code>    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|stringValue
operator|==
literal|null
condition|?
literal|""
else|:
name|stringValue
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|compareTo
argument_list|(
operator|(
name|HServerAddress
operator|)
name|o
argument_list|)
operator|==
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|address
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|^=
name|stringValue
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
comment|//
comment|// Writable
comment|//
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|bindAddress
init|=
name|in
operator|.
name|readUTF
argument_list|()
decl_stmt|;
name|int
name|port
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|bindAddress
operator|==
literal|null
operator|||
name|bindAddress
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|address
operator|=
literal|null
expr_stmt|;
name|stringValue
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|address
operator|=
operator|new
name|InetSocketAddress
argument_list|(
name|bindAddress
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|stringValue
operator|=
name|bindAddress
operator|+
literal|":"
operator|+
name|port
expr_stmt|;
name|checkBindAddressCanBeResolved
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|address
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|address
operator|.
name|getAddress
argument_list|()
operator|.
name|getHostAddress
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|address
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|//
comment|// Comparable
comment|//
specifier|public
name|int
name|compareTo
parameter_list|(
name|HServerAddress
name|o
parameter_list|)
block|{
comment|// Addresses as Strings may not compare though address is for the one
comment|// server with only difference being that one address has hostname
comment|// resolved whereas other only has IP.
if|if
condition|(
name|address
operator|.
name|equals
argument_list|(
name|o
operator|.
name|address
argument_list|)
condition|)
return|return
literal|0
return|;
return|return
name|toString
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

