begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|Addressing
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
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Instance of an HBase ServerName.  * A server name is used uniquely identifying a server instance and is made  * of the combination of hostname, port, and startcode. The startcode  * distingushes restarted servers on same hostname and port (startcode is  * usually timestamp of server startup). The {@link #toString()} format of  * ServerName is safe to use in the  filesystem and as znode name up in  * ZooKeeper.  Its format is:  *<code>&lt;hostname> '{@link #SERVERNAME_SEPARATOR}'&lt;port> '{@link #SERVERNAME_SEPARATOR}'&lt;startcode></code>.  * For example, if hostname is<code>example.org</code>, port is<code>1234</code>,  * and the startcode for the regionserver is<code>1212121212</code>, then  * the {@link #toString()} would be<code>example.org,1234,1212121212</code>.  *   *<p>You can obtain a versioned serialized form of this class by calling  * {@link #getVersionedBytes()}.  To deserialize, call {@link #parseVersionedServerName(byte[])}  *   *<p>Immutable.  */
end_comment

begin_class
specifier|public
class|class
name|ServerName
implements|implements
name|Comparable
argument_list|<
name|ServerName
argument_list|>
block|{
comment|/**    * Version for this class.    * Its a short rather than a byte so I can for sure distinguish between this    * version of this class and the version previous to this which did not have    * a version.    */
specifier|private
specifier|static
specifier|final
name|short
name|VERSION
init|=
literal|0
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|VERSION_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VERSION
argument_list|)
decl_stmt|;
comment|/**    * What to use if no startcode supplied.    */
specifier|public
specifier|static
specifier|final
name|int
name|NON_STARTCODE
init|=
operator|-
literal|1
decl_stmt|;
comment|/**    * This character is used as separator between server hostname, port and    * startcode.    */
specifier|public
specifier|static
specifier|final
name|String
name|SERVERNAME_SEPARATOR
init|=
literal|","
decl_stmt|;
specifier|public
specifier|static
name|Pattern
name|SERVERNAME_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"[^"
operator|+
name|SERVERNAME_SEPARATOR
operator|+
literal|"]+"
operator|+
name|SERVERNAME_SEPARATOR
operator|+
name|Addressing
operator|.
name|VALID_PORT_REGEX
operator|+
name|SERVERNAME_SEPARATOR
operator|+
name|Addressing
operator|.
name|VALID_PORT_REGEX
operator|+
literal|"$"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|servername
decl_stmt|;
specifier|private
specifier|final
name|String
name|hostname
decl_stmt|;
specifier|private
specifier|final
name|int
name|port
decl_stmt|;
specifier|private
specifier|final
name|long
name|startcode
decl_stmt|;
comment|/**    * Cached versioned bytes of this ServerName instance.    * @see #getVersionedBytes()    */
specifier|private
name|byte
index|[]
name|bytes
decl_stmt|;
specifier|public
name|ServerName
parameter_list|(
specifier|final
name|String
name|hostname
parameter_list|,
specifier|final
name|int
name|port
parameter_list|,
specifier|final
name|long
name|startcode
parameter_list|)
block|{
name|this
operator|.
name|hostname
operator|=
name|hostname
expr_stmt|;
name|this
operator|.
name|port
operator|=
name|port
expr_stmt|;
name|this
operator|.
name|startcode
operator|=
name|startcode
expr_stmt|;
name|this
operator|.
name|servername
operator|=
name|getServerName
argument_list|(
name|hostname
argument_list|,
name|port
argument_list|,
name|startcode
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ServerName
parameter_list|(
specifier|final
name|String
name|serverName
parameter_list|)
block|{
name|this
argument_list|(
name|parseHostname
argument_list|(
name|serverName
argument_list|)
argument_list|,
name|parsePort
argument_list|(
name|serverName
argument_list|)
argument_list|,
name|parseStartcode
argument_list|(
name|serverName
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ServerName
parameter_list|(
specifier|final
name|String
name|hostAndPort
parameter_list|,
specifier|final
name|long
name|startCode
parameter_list|)
block|{
name|this
argument_list|(
name|Addressing
operator|.
name|parseHostname
argument_list|(
name|hostAndPort
argument_list|)
argument_list|,
name|Addressing
operator|.
name|parsePort
argument_list|(
name|hostAndPort
argument_list|)
argument_list|,
name|startCode
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|String
name|parseHostname
parameter_list|(
specifier|final
name|String
name|serverName
parameter_list|)
block|{
if|if
condition|(
name|serverName
operator|==
literal|null
operator|||
name|serverName
operator|.
name|length
argument_list|()
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Passed hostname is null or empty"
argument_list|)
throw|;
block|}
name|int
name|index
init|=
name|serverName
operator|.
name|indexOf
argument_list|(
name|SERVERNAME_SEPARATOR
argument_list|)
decl_stmt|;
return|return
name|serverName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|int
name|parsePort
parameter_list|(
specifier|final
name|String
name|serverName
parameter_list|)
block|{
name|String
index|[]
name|split
init|=
name|serverName
operator|.
name|split
argument_list|(
name|SERVERNAME_SEPARATOR
argument_list|)
decl_stmt|;
return|return
name|Integer
operator|.
name|parseInt
argument_list|(
name|split
index|[
literal|1
index|]
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|long
name|parseStartcode
parameter_list|(
specifier|final
name|String
name|serverName
parameter_list|)
block|{
name|int
name|index
init|=
name|serverName
operator|.
name|lastIndexOf
argument_list|(
name|SERVERNAME_SEPARATOR
argument_list|)
decl_stmt|;
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|serverName
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
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
return|return
name|getServerName
argument_list|()
return|;
block|}
comment|/**    * @return {@link #getServerName()} as bytes with a short-sized prefix with    * the ServerName#VERSION of this class.    */
specifier|public
specifier|synchronized
name|byte
index|[]
name|getVersionedBytes
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|bytes
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|bytes
operator|=
name|Bytes
operator|.
name|add
argument_list|(
name|VERSION_BYTES
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|bytes
return|;
block|}
specifier|public
name|String
name|getServerName
parameter_list|()
block|{
return|return
name|servername
return|;
block|}
specifier|public
name|String
name|getHostname
parameter_list|()
block|{
return|return
name|hostname
return|;
block|}
specifier|public
name|int
name|getPort
parameter_list|()
block|{
return|return
name|port
return|;
block|}
specifier|public
name|long
name|getStartcode
parameter_list|()
block|{
return|return
name|startcode
return|;
block|}
comment|/**    * @param hostName    * @param port    * @param startcode    * @return Server name made of the concatenation of hostname, port and    * startcode formatted as<code>&lt;hostname> ','&lt;port> ','&lt;startcode></code>    */
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
name|startcode
parameter_list|)
block|{
specifier|final
name|StringBuilder
name|name
init|=
operator|new
name|StringBuilder
argument_list|(
name|hostName
operator|.
name|length
argument_list|()
operator|+
literal|1
operator|+
literal|5
operator|+
literal|1
operator|+
literal|13
argument_list|)
decl_stmt|;
name|name
operator|.
name|append
argument_list|(
name|hostName
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
name|startcode
argument_list|)
expr_stmt|;
return|return
name|name
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * @param hostAndPort String in form of&lt;hostname> ':'&lt;port>    * @param startcode    * @return Server name made of the concatenation of hostname, port and    * startcode formatted as<code>&lt;hostname> ','&lt;port> ','&lt;startcode></code>    */
specifier|public
specifier|static
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
comment|/**    * @return Hostname and port formatted as described at    * {@link Addressing#createHostAndPortStr(String, int)}    */
specifier|public
name|String
name|getHostAndPort
parameter_list|()
block|{
return|return
name|Addressing
operator|.
name|createHostAndPortStr
argument_list|(
name|this
operator|.
name|hostname
argument_list|,
name|this
operator|.
name|port
argument_list|)
return|;
block|}
comment|/**    * @param serverName ServerName in form specified by {@link #getServerName()}    * @return The server start code parsed from<code>servername</code>    */
specifier|public
specifier|static
name|long
name|getServerStartcodeFromServerName
parameter_list|(
specifier|final
name|String
name|serverName
parameter_list|)
block|{
name|int
name|index
init|=
name|serverName
operator|.
name|lastIndexOf
argument_list|(
name|SERVERNAME_SEPARATOR
argument_list|)
decl_stmt|;
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|serverName
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
argument_list|)
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
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|ServerName
name|other
parameter_list|)
block|{
name|int
name|compare
init|=
name|this
operator|.
name|getHostname
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|getHostname
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|compare
operator|!=
literal|0
condition|)
return|return
name|compare
return|;
name|compare
operator|=
name|this
operator|.
name|getPort
argument_list|()
operator|-
name|other
operator|.
name|getPort
argument_list|()
expr_stmt|;
if|if
condition|(
name|compare
operator|!=
literal|0
condition|)
return|return
name|compare
return|;
return|return
call|(
name|int
call|)
argument_list|(
name|this
operator|.
name|getStartcode
argument_list|()
operator|-
name|other
operator|.
name|getStartcode
argument_list|()
argument_list|)
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
name|getServerName
argument_list|()
operator|.
name|hashCode
argument_list|()
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
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|ServerName
operator|)
condition|)
return|return
literal|false
return|;
return|return
name|this
operator|.
name|compareTo
argument_list|(
operator|(
name|ServerName
operator|)
name|o
argument_list|)
operator|==
literal|0
return|;
block|}
comment|/**    * @return ServerName with matching hostname and port.    */
specifier|public
specifier|static
name|ServerName
name|findServerWithSameHostnamePort
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|names
parameter_list|,
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
for|for
control|(
name|ServerName
name|sn
range|:
name|names
control|)
block|{
if|if
condition|(
name|isSameHostnameAndPort
argument_list|(
name|serverName
argument_list|,
name|sn
argument_list|)
condition|)
return|return
name|sn
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * @param left    * @param right    * @return True if<code>other</code> has same hostname and port.    */
specifier|public
specifier|static
name|boolean
name|isSameHostnameAndPort
parameter_list|(
specifier|final
name|ServerName
name|left
parameter_list|,
specifier|final
name|ServerName
name|right
parameter_list|)
block|{
if|if
condition|(
name|left
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|right
operator|==
literal|null
condition|)
return|return
literal|false
return|;
return|return
name|left
operator|.
name|getHostname
argument_list|()
operator|.
name|equals
argument_list|(
name|right
operator|.
name|getHostname
argument_list|()
argument_list|)
operator|&&
name|left
operator|.
name|getPort
argument_list|()
operator|==
name|right
operator|.
name|getPort
argument_list|()
return|;
block|}
comment|/**    * Use this method instantiating a {@link ServerName} from bytes    * gotten from a call to {@link #getVersionedBytes()}.  Will take care of the    * case where bytes were written by an earlier version of hbase.    * @param versionedBytes Pass bytes gotten from a call to {@link #getVersionedBytes()}    * @return A ServerName instance.    * @see #getVersionedBytes()    */
specifier|public
specifier|static
name|ServerName
name|parseVersionedServerName
parameter_list|(
specifier|final
name|byte
index|[]
name|versionedBytes
parameter_list|)
block|{
comment|// Version is a short.
name|short
name|version
init|=
name|Bytes
operator|.
name|toShort
argument_list|(
name|versionedBytes
argument_list|)
decl_stmt|;
if|if
condition|(
name|version
operator|==
name|VERSION
condition|)
block|{
name|int
name|length
init|=
name|versionedBytes
operator|.
name|length
operator|-
name|Bytes
operator|.
name|SIZEOF_SHORT
decl_stmt|;
return|return
operator|new
name|ServerName
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|versionedBytes
argument_list|,
name|Bytes
operator|.
name|SIZEOF_SHORT
argument_list|,
name|length
argument_list|)
argument_list|)
return|;
block|}
comment|// Presume the bytes were written with an old version of hbase and that the
comment|// bytes are actually a String of the form "'<hostname>' ':' '<port>'".
return|return
operator|new
name|ServerName
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|versionedBytes
argument_list|)
argument_list|,
name|NON_STARTCODE
argument_list|)
return|;
block|}
comment|/**    * @param str Either an instance of {@link ServerName#toString()} or a    * "'<hostname>' ':' '<port>'".    * @return A ServerName instance.    */
specifier|public
specifier|static
name|ServerName
name|parseServerName
parameter_list|(
specifier|final
name|String
name|str
parameter_list|)
block|{
return|return
name|SERVERNAME_PATTERN
operator|.
name|matcher
argument_list|(
name|str
argument_list|)
operator|.
name|matches
argument_list|()
condition|?
operator|new
name|ServerName
argument_list|(
name|str
argument_list|)
else|:
operator|new
name|ServerName
argument_list|(
name|str
argument_list|,
name|NON_STARTCODE
argument_list|)
return|;
block|}
block|}
end_class

end_unit

