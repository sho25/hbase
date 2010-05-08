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
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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

begin_comment
comment|/**  * HServerInfo is meta info about an {@link HRegionServer}.  * Holds hostname, ports, regionserver startcode, and load.  Each server has  * a<code>servername</code> where servername is made up of a concatenation of  * hostname, port, and regionserver startcode.  */
end_comment

begin_class
specifier|public
class|class
name|HServerInfo
implements|implements
name|WritableComparable
argument_list|<
name|HServerInfo
argument_list|>
block|{
comment|/**    * This character is used as separator making up the<code>servername</code>.    * Servername is made of host, port, and startcode formatted as    *<code>&lt;hostname> '{@link #SERVERNAME_SEPARATOR}'&lt;port> '{@ink #SEPARATOR"}'&lt;startcode></code>    * where {@link SEPARATOR is usually a ','.    */
specifier|public
specifier|static
specifier|final
name|String
name|SERVERNAME_SEPARATOR
init|=
literal|","
decl_stmt|;
specifier|private
name|HServerAddress
name|serverAddress
decl_stmt|;
specifier|private
name|long
name|startCode
decl_stmt|;
specifier|private
name|HServerLoad
name|load
decl_stmt|;
specifier|private
name|int
name|infoPort
decl_stmt|;
comment|// Servername is made of hostname, port and startcode.
specifier|private
name|String
name|serverName
init|=
literal|null
decl_stmt|;
comment|// Hostname of the regionserver.
specifier|private
name|String
name|hostname
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dnsCache
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|HServerInfo
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|HServerAddress
argument_list|()
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|DEFAULT_REGIONSERVER_INFOPORT
argument_list|,
literal|"default name"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HServerInfo
parameter_list|(
name|HServerAddress
name|serverAddress
parameter_list|,
name|long
name|startCode
parameter_list|,
specifier|final
name|int
name|infoPort
parameter_list|,
name|String
name|hostname
parameter_list|)
block|{
name|this
operator|.
name|serverAddress
operator|=
name|serverAddress
expr_stmt|;
name|this
operator|.
name|startCode
operator|=
name|startCode
expr_stmt|;
name|this
operator|.
name|load
operator|=
operator|new
name|HServerLoad
argument_list|()
expr_stmt|;
name|this
operator|.
name|infoPort
operator|=
name|infoPort
expr_stmt|;
name|this
operator|.
name|hostname
operator|=
name|hostname
expr_stmt|;
block|}
comment|/**    * Copy-constructor    * @param other    */
specifier|public
name|HServerInfo
parameter_list|(
name|HServerInfo
name|other
parameter_list|)
block|{
name|this
operator|.
name|serverAddress
operator|=
operator|new
name|HServerAddress
argument_list|(
name|other
operator|.
name|getServerAddress
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|startCode
operator|=
name|other
operator|.
name|getStartCode
argument_list|()
expr_stmt|;
name|this
operator|.
name|load
operator|=
name|other
operator|.
name|getLoad
argument_list|()
expr_stmt|;
name|this
operator|.
name|infoPort
operator|=
name|other
operator|.
name|getInfoPort
argument_list|()
expr_stmt|;
name|this
operator|.
name|hostname
operator|=
name|other
operator|.
name|hostname
expr_stmt|;
block|}
specifier|public
name|HServerLoad
name|getLoad
parameter_list|()
block|{
return|return
name|load
return|;
block|}
specifier|public
name|void
name|setLoad
parameter_list|(
name|HServerLoad
name|load
parameter_list|)
block|{
name|this
operator|.
name|load
operator|=
name|load
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|HServerAddress
name|getServerAddress
parameter_list|()
block|{
return|return
operator|new
name|HServerAddress
argument_list|(
name|serverAddress
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|setServerAddress
parameter_list|(
name|HServerAddress
name|serverAddress
parameter_list|)
block|{
name|this
operator|.
name|serverAddress
operator|=
name|serverAddress
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|long
name|getStartCode
parameter_list|()
block|{
return|return
name|startCode
return|;
block|}
specifier|public
name|int
name|getInfoPort
parameter_list|()
block|{
return|return
name|this
operator|.
name|infoPort
return|;
block|}
specifier|public
name|void
name|setInfoPort
parameter_list|(
name|int
name|infoPort
parameter_list|)
block|{
name|this
operator|.
name|infoPort
operator|=
name|infoPort
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|setStartCode
parameter_list|(
name|long
name|startCode
parameter_list|)
block|{
name|this
operator|.
name|startCode
operator|=
name|startCode
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * @return Server name made of the concatenation of hostname, port and    * startcode formatted as<code>&lt;hostname> ','&lt;port> ','&lt;startcode></code>    */
specifier|public
specifier|synchronized
name|String
name|getServerName
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|serverName
operator|==
literal|null
condition|)
block|{
comment|// if we have the hostname of the RS, use it
if|if
condition|(
name|this
operator|.
name|hostname
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|serverName
operator|=
name|getServerName
argument_list|(
name|this
operator|.
name|hostname
argument_list|,
name|this
operator|.
name|serverAddress
operator|.
name|getPort
argument_list|()
argument_list|,
name|this
operator|.
name|startCode
argument_list|)
expr_stmt|;
block|}
comment|// go to DNS name resolution only if we dont have the name of the RS
else|else
block|{
name|this
operator|.
name|serverName
operator|=
name|getServerName
argument_list|(
name|this
operator|.
name|serverAddress
argument_list|,
name|this
operator|.
name|startCode
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|this
operator|.
name|serverName
return|;
block|}
comment|/**    * @param serverAddress In form<code>&lt;hostname> ':'&lt;port></code>    * @param startCode Server startcode    * @return Server name made of the concatenation of hostname, port and    * startcode formatted as<code>&lt;hostname> ','&lt;port> ','&lt;startcode></code>    */
specifier|public
specifier|static
name|String
name|getServerName
parameter_list|(
name|String
name|serverAddress
parameter_list|,
name|long
name|startCode
parameter_list|)
block|{
name|String
name|name
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|serverAddress
operator|!=
literal|null
condition|)
block|{
name|int
name|colonIndex
init|=
name|serverAddress
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
name|serverAddress
argument_list|)
throw|;
block|}
name|String
name|host
init|=
name|serverAddress
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
name|valueOf
argument_list|(
name|serverAddress
operator|.
name|substring
argument_list|(
name|colonIndex
operator|+
literal|1
argument_list|)
argument_list|)
operator|.
name|intValue
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|dnsCache
operator|.
name|containsKey
argument_list|(
name|host
argument_list|)
condition|)
block|{
name|HServerAddress
name|address
init|=
operator|new
name|HServerAddress
argument_list|(
name|serverAddress
argument_list|)
decl_stmt|;
name|dnsCache
operator|.
name|put
argument_list|(
name|host
argument_list|,
name|address
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|host
operator|=
name|dnsCache
operator|.
name|get
argument_list|(
name|host
argument_list|)
expr_stmt|;
name|name
operator|=
name|getServerName
argument_list|(
name|host
argument_list|,
name|port
argument_list|,
name|startCode
argument_list|)
expr_stmt|;
block|}
return|return
name|name
return|;
block|}
comment|/**    * @param address Server address    * @param startCode Server startcode    * @return Server name made of the concatenation of hostname, port and    * startcode formatted as<code>&lt;hostname> ','&lt;port> ','&lt;startcode></code>    */
specifier|public
specifier|static
name|String
name|getServerName
parameter_list|(
name|HServerAddress
name|address
parameter_list|,
name|long
name|startCode
parameter_list|)
block|{
return|return
name|getServerName
argument_list|(
name|address
operator|.
name|getHostname
argument_list|()
argument_list|,
name|address
operator|.
name|getPort
argument_list|()
argument_list|,
name|startCode
argument_list|)
return|;
block|}
comment|/*    * @param hostName    * @param port    * @param startCode    * @return Server name made of the concatenation of hostname, port and    * startcode formatted as<code>&lt;hostname> ','&lt;port> ','&lt;startcode></code>    */
specifier|private
specifier|static
name|String
name|getServerName
parameter_list|(
name|String
name|hostName
parameter_list|,
name|int
name|port
parameter_list|,
name|long
name|startCode
parameter_list|)
block|{
name|StringBuilder
name|name
init|=
operator|new
name|StringBuilder
argument_list|(
name|hostName
argument_list|)
decl_stmt|;
name|name
operator|.
name|append
argument_list|(
name|SERVERNAME_SEPARATOR
argument_list|)
expr_stmt|;
name|name
operator|.
name|append
argument_list|(
name|port
argument_list|)
expr_stmt|;
name|name
operator|.
name|append
argument_list|(
name|SERVERNAME_SEPARATOR
argument_list|)
expr_stmt|;
name|name
operator|.
name|append
argument_list|(
name|startCode
argument_list|)
expr_stmt|;
return|return
name|name
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * @return ServerName and load concatenated.    * @see #getServerName()    * @see #getLoad()    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"serverName="
operator|+
name|getServerName
argument_list|()
operator|+
literal|", load=("
operator|+
name|this
operator|.
name|load
operator|.
name|toString
argument_list|()
operator|+
literal|")"
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
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
name|obj
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
name|HServerInfo
operator|)
name|obj
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
return|return
name|this
operator|.
name|getServerName
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
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
name|this
operator|.
name|serverAddress
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|startCode
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|this
operator|.
name|load
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|infoPort
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|this
operator|.
name|hostname
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
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
name|this
operator|.
name|serverAddress
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|this
operator|.
name|startCode
argument_list|)
expr_stmt|;
name|this
operator|.
name|load
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|infoPort
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|hostname
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|compareTo
parameter_list|(
name|HServerInfo
name|o
parameter_list|)
block|{
return|return
name|this
operator|.
name|getServerName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|getServerName
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

