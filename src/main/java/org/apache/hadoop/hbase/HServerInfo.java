begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|regionserver
operator|.
name|HRegionServer
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
name|VersionedWritable
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
name|Writable
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
comment|/**  * HServerInfo is meta info about an {@link HRegionServer}.  It is the token  * by which a master distingushes a particular regionserver from the rest.  * It holds hostname, ports, regionserver startcode, and load.  Each server has  * a<code>servername</code> where servername is made up of a concatenation of  * hostname, port, and regionserver startcode.  This servername is used in  * various places identifying this regionserver.  Its even used as part of  * a pathname in the filesystem.  As part of the initialization,  * master will pass the regionserver the address that it knows this regionserver  * by.  In subsequent communications, the regionserver will pass a HServerInfo  * with the master-supplied address.  */
end_comment

begin_class
specifier|public
class|class
name|HServerInfo
extends|extends
name|VersionedWritable
implements|implements
name|WritableComparable
argument_list|<
name|HServerInfo
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|byte
name|VERSION
init|=
literal|0
decl_stmt|;
comment|/*    * This character is used as separator between server hostname and port and    * its startcode. Servername is formatted as    *<code>&lt;hostname> '{@ink #SERVERNAME_SEPARATOR"}'&lt;port> '{@ink #SERVERNAME_SEPARATOR"}'&lt;startcode></code>.    */
specifier|private
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
name|String
name|cachedHostnamePort
init|=
literal|null
decl_stmt|;
comment|/** @return the object version number */
specifier|public
name|byte
name|getVersion
parameter_list|()
block|{
return|return
name|VERSION
return|;
block|}
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
comment|/**    * Constructor that creates a HServerInfo with a generated startcode and an    * empty load.    * @param serverAddress An {@link InetSocketAddress} encased in a {@link Writable}    * @param infoPort Port the webui runs on.    * @param hostname Server hostname.    */
specifier|public
name|HServerInfo
parameter_list|(
name|HServerAddress
name|serverAddress
parameter_list|,
specifier|final
name|int
name|infoPort
parameter_list|,
specifier|final
name|String
name|hostname
parameter_list|)
block|{
name|this
argument_list|(
name|serverAddress
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|infoPort
argument_list|,
name|hostname
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
name|hostname
operator|=
name|serverAddress
operator|.
name|getHostname
argument_list|()
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
name|String
name|getHostname
parameter_list|()
block|{
return|return
name|this
operator|.
name|hostname
return|;
block|}
comment|/**    * @return The hostname and port concatenated with a ':' as separator.    */
specifier|public
specifier|synchronized
name|String
name|getHostnamePort
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|cachedHostnamePort
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|cachedHostnamePort
operator|=
name|getHostnamePort
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
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|cachedHostnamePort
return|;
block|}
comment|/**    * @param hostname    * @param port    * @return The hostname and port concatenated with a ':' as separator.    */
specifier|public
specifier|static
name|String
name|getHostnamePort
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
literal|":"
operator|+
name|port
return|;
block|}
comment|/**    * Gets the unique server instance name.  Includes the hostname, port, and    * start code.    * @return Server name made of the concatenation of hostname, port and    * startcode formatted as<code>&lt;hostname> ','&lt;port> ','&lt;startcode></code>    */
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
return|return
name|this
operator|.
name|serverName
return|;
block|}
specifier|public
specifier|static
specifier|synchronized
name|String
name|getServerName
parameter_list|(
specifier|final
name|String
name|hostAndPort
parameter_list|,
specifier|final
name|long
name|startcode
parameter_list|)
block|{
name|int
name|index
init|=
name|hostAndPort
operator|.
name|indexOf
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|<=
literal|0
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected<hostname> ':'<port>"
argument_list|)
throw|;
return|return
name|getServerName
argument_list|(
name|hostAndPort
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
argument_list|,
name|Integer
operator|.
name|parseInt
argument_list|(
name|hostAndPort
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
argument_list|)
argument_list|,
name|startcode
argument_list|)
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
specifier|public
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
comment|/**    * Orders HServerInfos by load then name.  Natural/ascending order.    */
specifier|public
specifier|static
class|class
name|LoadComparator
implements|implements
name|Comparator
argument_list|<
name|HServerInfo
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|HServerInfo
name|left
parameter_list|,
name|HServerInfo
name|right
parameter_list|)
block|{
name|int
name|loadCompare
init|=
name|left
operator|.
name|getLoad
argument_list|()
operator|.
name|compareTo
argument_list|(
name|right
operator|.
name|getLoad
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|loadCompare
operator|!=
literal|0
condition|?
name|loadCompare
else|:
name|left
operator|.
name|compareTo
argument_list|(
name|right
argument_list|)
return|;
block|}
block|}
comment|/**    * Utility method that does a find of a servername or a hostandport combination    * in the passed Set.    * @param servers Set of server names    * @param serverName Name to look for    * @param hostAndPortOnly If<code>serverName</code> is a    *<code>hostname ':' port</code>    * or<code>hostname , port , startcode</code>.    * @return True if<code>serverName</code> found in<code>servers</code>    */
specifier|public
specifier|static
name|boolean
name|isServer
parameter_list|(
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|servers
parameter_list|,
specifier|final
name|String
name|serverName
parameter_list|,
specifier|final
name|boolean
name|hostAndPortOnly
parameter_list|)
block|{
if|if
condition|(
operator|!
name|hostAndPortOnly
condition|)
return|return
name|servers
operator|.
name|contains
argument_list|(
name|serverName
argument_list|)
return|;
name|String
name|serverNameColonReplaced
init|=
name|serverName
operator|.
name|replaceFirst
argument_list|(
literal|":"
argument_list|,
name|SERVERNAME_SEPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|hostPortStartCode
range|:
name|servers
control|)
block|{
name|int
name|index
init|=
name|hostPortStartCode
operator|.
name|lastIndexOf
argument_list|(
name|SERVERNAME_SEPARATOR
argument_list|)
decl_stmt|;
name|String
name|hostPortStrippedOfStartCode
init|=
name|hostPortStartCode
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|hostPortStrippedOfStartCode
operator|.
name|equals
argument_list|(
name|serverNameColonReplaced
argument_list|)
condition|)
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Utility method to excise the start code from a server name    * @param inServerName full server name    * @return server name less its start code    */
specifier|public
specifier|static
name|String
name|getServerNameLessStartCode
parameter_list|(
name|String
name|inServerName
parameter_list|)
block|{
if|if
condition|(
name|inServerName
operator|!=
literal|null
operator|&&
name|inServerName
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|int
name|index
init|=
name|inServerName
operator|.
name|lastIndexOf
argument_list|(
name|SERVERNAME_SEPARATOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|>
literal|0
condition|)
block|{
return|return
name|inServerName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
return|;
block|}
block|}
return|return
name|inServerName
return|;
block|}
block|}
end_class

end_unit

