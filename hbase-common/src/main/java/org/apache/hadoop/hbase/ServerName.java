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
name|net
operator|.
name|HostAndPort
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|net
operator|.
name|InetAddresses
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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
name|classification
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
name|exceptions
operator|.
name|DeserializationException
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
name|protobuf
operator|.
name|ProtobufMagic
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
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
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
comment|/**  * Instance of an HBase ServerName.  * A server name is used uniquely identifying a server instance in a cluster and is made  * of the combination of hostname, port, and startcode.  The startcode distingushes restarted  * servers on same hostname and port (startcode is usually timestamp of server startup). The  * {@link #toString()} format of ServerName is safe to use in the  filesystem and as znode name  * up in ZooKeeper.  Its format is:  *<code>&lt;hostname&gt; '{@link #SERVERNAME_SEPARATOR}'&lt;port&gt;  * '{@link #SERVERNAME_SEPARATOR}'&lt;startcode&gt;</code>.  * For example, if hostname is<code>www.example.org</code>, port is<code>1234</code>,  * and the startcode for the regionserver is<code>1212121212</code>, then  * the {@link #toString()} would be<code>www.example.org,1234,1212121212</code>.  *   *<p>You can obtain a versioned serialized form of this class by calling  * {@link #getVersionedBytes()}.  To deserialize, call {@link #parseVersionedServerName(byte[])}  *   *<p>Immutable.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ServerName
implements|implements
name|Comparable
argument_list|<
name|ServerName
argument_list|>
implements|,
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1367463982557264981L
decl_stmt|;
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
specifier|final
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
comment|/**    * What to use if server name is unknown.    */
specifier|public
specifier|static
specifier|final
name|String
name|UNKNOWN_SERVERNAME
init|=
literal|"#unknown#"
decl_stmt|;
specifier|private
specifier|final
name|String
name|servername
decl_stmt|;
specifier|private
specifier|final
name|String
name|hostnameOnly
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
specifier|private
specifier|transient
name|HostAndPort
name|hostAndPort
decl_stmt|;
comment|/**    * Cached versioned bytes of this ServerName instance.    * @see #getVersionedBytes()    */
specifier|private
name|byte
index|[]
name|bytes
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|List
argument_list|<
name|ServerName
argument_list|>
name|EMPTY_SERVER_LIST
init|=
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
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
comment|// Drop the domain is there is one; no need of it in a local cluster.  With it, we get long
comment|// unwieldy names.
name|this
operator|.
name|hostnameOnly
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
comment|/**    * @param hostname    * @return hostname minus the domain, if there is one (will do pass-through on ip addresses)    */
specifier|static
name|String
name|getHostNameMinusDomain
parameter_list|(
specifier|final
name|String
name|hostname
parameter_list|)
block|{
if|if
condition|(
name|InetAddresses
operator|.
name|isInetAddress
argument_list|(
name|hostname
argument_list|)
condition|)
return|return
name|hostname
return|;
name|String
index|[]
name|parts
init|=
name|hostname
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
if|if
condition|(
name|parts
operator|==
literal|null
operator|||
name|parts
operator|.
name|length
operator|==
literal|0
condition|)
return|return
name|hostname
return|;
return|return
name|parts
index|[
literal|0
index|]
return|;
block|}
specifier|private
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
specifier|private
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
if|if
condition|(
operator|!
name|Character
operator|.
name|isLetterOrDigit
argument_list|(
name|serverName
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Bad passed hostname, serverName="
operator|+
name|serverName
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
comment|/**    * Retrieve an instance of ServerName.    * Callers should use the equals method to compare returned instances, though we may return    * a shared immutable object as an internal optimization.    */
specifier|public
specifier|static
name|ServerName
name|valueOf
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
return|return
operator|new
name|ServerName
argument_list|(
name|hostname
argument_list|,
name|port
argument_list|,
name|startcode
argument_list|)
return|;
block|}
comment|/**    * Retrieve an instance of ServerName.    * Callers should use the equals method to compare returned instances, though we may return    * a shared immutable object as an internal optimization.    */
specifier|public
specifier|static
name|ServerName
name|valueOf
parameter_list|(
specifier|final
name|String
name|serverName
parameter_list|)
block|{
return|return
operator|new
name|ServerName
argument_list|(
name|serverName
argument_list|)
return|;
block|}
comment|/**    * Retrieve an instance of ServerName.    * Callers should use the equals method to compare returned instances, though we may return    * a shared immutable object as an internal optimization.    */
specifier|public
specifier|static
name|ServerName
name|valueOf
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
return|return
operator|new
name|ServerName
argument_list|(
name|hostAndPort
argument_list|,
name|startCode
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
comment|/**    * @return Return a SHORT version of {@link ServerName#toString()}, one that has the host only,    * minus the domain, and the port only -- no start code; the String is for us internally mostly    * tying threads to their server.  Not for external use.  It is lossy and will not work in    * in compares, etc.    */
specifier|public
name|String
name|toShortString
parameter_list|()
block|{
return|return
name|Addressing
operator|.
name|createHostAndPortStr
argument_list|(
name|getHostNameMinusDomain
argument_list|(
name|hostnameOnly
argument_list|)
argument_list|,
name|port
argument_list|)
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
name|hostnameOnly
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
comment|/**    * For internal use only.    * @param hostName    * @param port    * @param startcode    * @return Server name made of the concatenation of hostname, port and    * startcode formatted as<code>&lt;hostname&gt; ','&lt;port&gt; ','&lt;startcode&gt;</code>    */
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
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
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
comment|/**    * @param hostAndPort String in form of&lt;hostname&gt; ':'&lt;port&gt;    * @param startcode    * @return Server name made of the concatenation of hostname, port and    * startcode formatted as<code>&lt;hostname&gt; ','&lt;port&gt; ','&lt;startcode&gt;</code>    */
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
name|hostnameOnly
argument_list|,
name|port
argument_list|)
return|;
block|}
specifier|public
name|HostAndPort
name|getHostPort
parameter_list|()
block|{
if|if
condition|(
name|hostAndPort
operator|==
literal|null
condition|)
block|{
name|hostAndPort
operator|=
name|HostAndPort
operator|.
name|fromParts
argument_list|(
name|hostnameOnly
argument_list|,
name|port
argument_list|)
expr_stmt|;
block|}
return|return
name|hostAndPort
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
name|compareToIgnoreCase
argument_list|(
name|other
operator|.
name|getHostname
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
name|Long
operator|.
name|compare
argument_list|(
name|this
operator|.
name|getStartcode
argument_list|()
argument_list|,
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
name|compareToIgnoreCase
argument_list|(
name|right
operator|.
name|getHostname
argument_list|()
argument_list|)
operator|==
literal|0
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
name|valueOf
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
name|valueOf
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
comment|/**    * @param str Either an instance of {@link ServerName#toString()} or a    * "'&lt;hostname&gt;' ':' '&lt;port&gt;'".    * @return A ServerName instance.    */
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
name|valueOf
argument_list|(
name|str
argument_list|)
else|:
name|valueOf
argument_list|(
name|str
argument_list|,
name|NON_STARTCODE
argument_list|)
return|;
block|}
comment|/**    * @return true if the String follows the pattern of {@link ServerName#toString()}, false    *  otherwise.    */
specifier|public
specifier|static
name|boolean
name|isFullServerName
parameter_list|(
specifier|final
name|String
name|str
parameter_list|)
block|{
if|if
condition|(
name|str
operator|==
literal|null
operator|||
name|str
operator|.
name|isEmpty
argument_list|()
condition|)
return|return
literal|false
return|;
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
return|;
block|}
comment|/**    * Get a ServerName from the passed in data bytes.    * @param data Data with a serialize server name in it; can handle the old style    * servername where servername was host and port.  Works too with data that    * begins w/ the pb 'PBUF' magic and that is then followed by a protobuf that    * has a serialized {@link ServerName} in it.    * @return Returns null if<code>data</code> is null else converts passed data    * to a ServerName instance.    * @throws DeserializationException     */
specifier|public
specifier|static
name|ServerName
name|parseFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|DeserializationException
block|{
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|data
operator|.
name|length
operator|<=
literal|0
condition|)
return|return
literal|null
return|;
if|if
condition|(
name|ProtobufMagic
operator|.
name|isPBMagicPrefix
argument_list|(
name|data
argument_list|)
condition|)
block|{
name|int
name|prefixLen
init|=
name|ProtobufMagic
operator|.
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
try|try
block|{
name|ZooKeeperProtos
operator|.
name|Master
name|rss
init|=
name|ZooKeeperProtos
operator|.
name|Master
operator|.
name|PARSER
operator|.
name|parseFrom
argument_list|(
name|data
argument_list|,
name|prefixLen
argument_list|,
name|data
operator|.
name|length
operator|-
name|prefixLen
argument_list|)
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|ServerName
name|sn
init|=
name|rss
operator|.
name|getMaster
argument_list|()
decl_stmt|;
return|return
name|valueOf
argument_list|(
name|sn
operator|.
name|getHostName
argument_list|()
argument_list|,
name|sn
operator|.
name|getPort
argument_list|()
argument_list|,
name|sn
operator|.
name|getStartCode
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
comment|// A failed parse of the znode is pretty catastrophic. Rather than loop
comment|// retrying hoping the bad bytes will changes, and rather than change
comment|// the signature on this method to add an IOE which will send ripples all
comment|// over the code base, throw a RuntimeException.  This should "never" happen.
comment|// Fail fast if it does.
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|// The str returned could be old style -- pre hbase-1502 -- which was
comment|// hostname and port seperated by a colon rather than hostname, port and
comment|// startcode delimited by a ','.
name|String
name|str
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|int
name|index
init|=
name|str
operator|.
name|indexOf
argument_list|(
name|ServerName
operator|.
name|SERVERNAME_SEPARATOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// Presume its ServerName serialized with versioned bytes.
return|return
name|ServerName
operator|.
name|parseVersionedServerName
argument_list|(
name|data
argument_list|)
return|;
block|}
comment|// Presume it a hostname:port format.
name|String
name|hostname
init|=
name|Addressing
operator|.
name|parseHostname
argument_list|(
name|str
argument_list|)
decl_stmt|;
name|int
name|port
init|=
name|Addressing
operator|.
name|parsePort
argument_list|(
name|str
argument_list|)
decl_stmt|;
return|return
name|valueOf
argument_list|(
name|hostname
argument_list|,
name|port
argument_list|,
operator|-
literal|1L
argument_list|)
return|;
block|}
block|}
end_class

end_unit

