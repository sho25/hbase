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
name|thrift
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharacterCodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharsetDecoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
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
name|HashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|Subject
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|AppConfigurationEntry
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginContext
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
name|thrift
operator|.
name|generated
operator|.
name|AlreadyExists
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
name|thrift
operator|.
name|generated
operator|.
name|ColumnDescriptor
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
name|thrift
operator|.
name|generated
operator|.
name|Hbase
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
name|thrift
operator|.
name|generated
operator|.
name|TCell
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
name|thrift
operator|.
name|generated
operator|.
name|TRowResult
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
name|Base64
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|THttpClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TSocket
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransport
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
name|ietf
operator|.
name|jgss
operator|.
name|GSSContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSCredential
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|Oid
import|;
end_import

begin_comment
comment|/**  * See the instructions under hbase-examples/README.txt  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HttpDoAsClient
block|{
specifier|static
specifier|protected
name|int
name|port
decl_stmt|;
specifier|static
specifier|protected
name|String
name|host
decl_stmt|;
name|CharsetDecoder
name|decoder
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|secure
init|=
literal|false
decl_stmt|;
specifier|static
specifier|protected
name|String
name|doAsUser
init|=
literal|null
decl_stmt|;
specifier|static
specifier|protected
name|String
name|principal
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|args
operator|.
name|length
argument_list|<
literal|3
operator|||
name|args
operator|.
name|length
argument_list|>
literal|4
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Invalid arguments!"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Usage: HttpDoAsClient host port doAsUserName [security=true]"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|host
operator|=
name|args
index|[
literal|0
index|]
expr_stmt|;
name|port
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|doAsUser
operator|=
name|args
index|[
literal|2
index|]
expr_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|>
literal|3
condition|)
block|{
name|secure
operator|=
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|args
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
name|principal
operator|=
name|getSubject
argument_list|()
operator|.
name|getPrincipals
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
specifier|final
name|HttpDoAsClient
name|client
init|=
operator|new
name|HttpDoAsClient
argument_list|()
decl_stmt|;
name|Subject
operator|.
name|doAs
argument_list|(
name|getSubject
argument_list|()
argument_list|,
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|client
operator|.
name|run
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|HttpDoAsClient
parameter_list|()
block|{
name|decoder
operator|=
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
operator|.
name|newDecoder
argument_list|()
expr_stmt|;
block|}
comment|// Helper to translate byte[]'s to UTF8 strings
specifier|private
name|String
name|utf8
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|)
block|{
try|try
block|{
return|return
name|decoder
operator|.
name|decode
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buf
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|CharacterCodingException
name|e
parameter_list|)
block|{
return|return
literal|"[INVALID UTF-8]"
return|;
block|}
block|}
comment|// Helper to translate strings to UTF8 bytes
specifier|private
name|byte
index|[]
name|bytes
parameter_list|(
name|String
name|s
parameter_list|)
block|{
try|try
block|{
return|return
name|s
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|TTransport
name|transport
init|=
operator|new
name|TSocket
argument_list|(
name|host
argument_list|,
name|port
argument_list|)
decl_stmt|;
name|transport
operator|.
name|open
argument_list|()
expr_stmt|;
name|String
name|url
init|=
literal|"http://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
decl_stmt|;
name|THttpClient
name|httpClient
init|=
operator|new
name|THttpClient
argument_list|(
name|url
argument_list|)
decl_stmt|;
name|httpClient
operator|.
name|open
argument_list|()
expr_stmt|;
name|TProtocol
name|protocol
init|=
operator|new
name|TBinaryProtocol
argument_list|(
name|httpClient
argument_list|)
decl_stmt|;
name|Hbase
operator|.
name|Client
name|client
init|=
operator|new
name|Hbase
operator|.
name|Client
argument_list|(
name|protocol
argument_list|)
decl_stmt|;
name|byte
index|[]
name|t
init|=
name|bytes
argument_list|(
literal|"demo_table"
argument_list|)
decl_stmt|;
comment|//
comment|// Scan all tables, look for the demo table and delete it.
comment|//
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"scanning tables..."
argument_list|)
expr_stmt|;
for|for
control|(
name|ByteBuffer
name|name
range|:
name|refresh
argument_list|(
name|client
argument_list|,
name|httpClient
argument_list|)
operator|.
name|getTableNames
argument_list|()
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"  found: "
operator|+
name|utf8
argument_list|(
name|name
operator|.
name|array
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|utf8
argument_list|(
name|name
operator|.
name|array
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
name|utf8
argument_list|(
name|t
argument_list|)
argument_list|)
condition|)
block|{
if|if
condition|(
name|refresh
argument_list|(
name|client
argument_list|,
name|httpClient
argument_list|)
operator|.
name|isTableEnabled
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"    disabling table: "
operator|+
name|utf8
argument_list|(
name|name
operator|.
name|array
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|refresh
argument_list|(
name|client
argument_list|,
name|httpClient
argument_list|)
operator|.
name|disableTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"    deleting table: "
operator|+
name|utf8
argument_list|(
name|name
operator|.
name|array
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|refresh
argument_list|(
name|client
argument_list|,
name|httpClient
argument_list|)
operator|.
name|deleteTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
comment|//
comment|// Create the demo table with two column families, entry: and unused:
comment|//
name|ArrayList
argument_list|<
name|ColumnDescriptor
argument_list|>
name|columns
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|ColumnDescriptor
name|col
decl_stmt|;
name|col
operator|=
operator|new
name|ColumnDescriptor
argument_list|()
expr_stmt|;
name|col
operator|.
name|name
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:"
argument_list|)
argument_list|)
expr_stmt|;
name|col
operator|.
name|timeToLive
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
name|col
operator|.
name|maxVersions
operator|=
literal|10
expr_stmt|;
name|columns
operator|.
name|add
argument_list|(
name|col
argument_list|)
expr_stmt|;
name|col
operator|=
operator|new
name|ColumnDescriptor
argument_list|()
expr_stmt|;
name|col
operator|.
name|name
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"unused:"
argument_list|)
argument_list|)
expr_stmt|;
name|col
operator|.
name|timeToLive
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
name|columns
operator|.
name|add
argument_list|(
name|col
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"creating table: "
operator|+
name|utf8
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|refresh
argument_list|(
name|client
argument_list|,
name|httpClient
argument_list|)
operator|.
name|createTable
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|columns
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AlreadyExists
name|ae
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"WARN: "
operator|+
name|ae
operator|.
name|message
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"column families in "
operator|+
name|utf8
argument_list|(
name|t
argument_list|)
operator|+
literal|": "
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|ByteBuffer
argument_list|,
name|ColumnDescriptor
argument_list|>
name|columnMap
init|=
name|refresh
argument_list|(
name|client
argument_list|,
name|httpClient
argument_list|)
operator|.
name|getColumnDescriptors
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|ColumnDescriptor
name|col2
range|:
name|columnMap
operator|.
name|values
argument_list|()
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"  column: "
operator|+
name|utf8
argument_list|(
name|col2
operator|.
name|name
operator|.
name|array
argument_list|()
argument_list|)
operator|+
literal|", maxVer: "
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|col2
operator|.
name|maxVersions
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
name|httpClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Hbase
operator|.
name|Client
name|refresh
parameter_list|(
name|Hbase
operator|.
name|Client
name|client
parameter_list|,
name|THttpClient
name|httpClient
parameter_list|)
block|{
name|httpClient
operator|.
name|setCustomHeader
argument_list|(
literal|"doAs"
argument_list|,
name|doAsUser
argument_list|)
expr_stmt|;
if|if
condition|(
name|secure
condition|)
block|{
try|try
block|{
name|httpClient
operator|.
name|setCustomHeader
argument_list|(
literal|"Authorization"
argument_list|,
name|generateTicket
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|GSSException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|client
return|;
block|}
specifier|private
name|String
name|generateTicket
parameter_list|()
throws|throws
name|GSSException
block|{
specifier|final
name|GSSManager
name|manager
init|=
name|GSSManager
operator|.
name|getInstance
argument_list|()
decl_stmt|;
comment|// Oid for kerberos principal name
name|Oid
name|krb5PrincipalOid
init|=
operator|new
name|Oid
argument_list|(
literal|"1.2.840.113554.1.2.2.1"
argument_list|)
decl_stmt|;
name|Oid
name|KERB_V5_OID
init|=
operator|new
name|Oid
argument_list|(
literal|"1.2.840.113554.1.2.2"
argument_list|)
decl_stmt|;
specifier|final
name|GSSName
name|clientName
init|=
name|manager
operator|.
name|createName
argument_list|(
name|principal
argument_list|,
name|krb5PrincipalOid
argument_list|)
decl_stmt|;
specifier|final
name|GSSCredential
name|clientCred
init|=
name|manager
operator|.
name|createCredential
argument_list|(
name|clientName
argument_list|,
literal|8
operator|*
literal|3600
argument_list|,
name|KERB_V5_OID
argument_list|,
name|GSSCredential
operator|.
name|INITIATE_ONLY
argument_list|)
decl_stmt|;
specifier|final
name|GSSName
name|serverName
init|=
name|manager
operator|.
name|createName
argument_list|(
name|principal
argument_list|,
name|krb5PrincipalOid
argument_list|)
decl_stmt|;
specifier|final
name|GSSContext
name|context
init|=
name|manager
operator|.
name|createContext
argument_list|(
name|serverName
argument_list|,
name|KERB_V5_OID
argument_list|,
name|clientCred
argument_list|,
name|GSSContext
operator|.
name|DEFAULT_LIFETIME
argument_list|)
decl_stmt|;
name|context
operator|.
name|requestMutualAuth
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|context
operator|.
name|requestConf
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|context
operator|.
name|requestInteg
argument_list|(
literal|true
argument_list|)
expr_stmt|;
specifier|final
name|byte
index|[]
name|outToken
init|=
name|context
operator|.
name|initSecContext
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|StringBuffer
name|outputBuffer
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|outputBuffer
operator|.
name|append
argument_list|(
literal|"Negotiate "
argument_list|)
expr_stmt|;
name|outputBuffer
operator|.
name|append
argument_list|(
name|Base64
operator|.
name|encodeBytes
argument_list|(
name|outToken
argument_list|)
operator|.
name|replace
argument_list|(
literal|"\n"
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"Ticket is: "
operator|+
name|outputBuffer
argument_list|)
expr_stmt|;
return|return
name|outputBuffer
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|void
name|printVersions
parameter_list|(
name|ByteBuffer
name|row
parameter_list|,
name|List
argument_list|<
name|TCell
argument_list|>
name|versions
parameter_list|)
block|{
name|StringBuilder
name|rowStr
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|TCell
name|cell
range|:
name|versions
control|)
block|{
name|rowStr
operator|.
name|append
argument_list|(
name|utf8
argument_list|(
name|cell
operator|.
name|value
operator|.
name|array
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|rowStr
operator|.
name|append
argument_list|(
literal|"; "
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"row: "
operator|+
name|utf8
argument_list|(
name|row
operator|.
name|array
argument_list|()
argument_list|)
operator|+
literal|", values: "
operator|+
name|rowStr
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|printRow
parameter_list|(
name|TRowResult
name|rowResult
parameter_list|)
block|{
comment|// copy values into a TreeMap to get them in sorted order
name|TreeMap
argument_list|<
name|String
argument_list|,
name|TCell
argument_list|>
name|sorted
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ByteBuffer
argument_list|,
name|TCell
argument_list|>
name|column
range|:
name|rowResult
operator|.
name|columns
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|sorted
operator|.
name|put
argument_list|(
name|utf8
argument_list|(
name|column
operator|.
name|getKey
argument_list|()
operator|.
name|array
argument_list|()
argument_list|)
argument_list|,
name|column
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|StringBuilder
name|rowStr
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|SortedMap
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|TCell
argument_list|>
name|entry
range|:
name|sorted
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|rowStr
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|rowStr
operator|.
name|append
argument_list|(
literal|" => "
argument_list|)
expr_stmt|;
name|rowStr
operator|.
name|append
argument_list|(
name|utf8
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|value
operator|.
name|array
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|rowStr
operator|.
name|append
argument_list|(
literal|"; "
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"row: "
operator|+
name|utf8
argument_list|(
name|rowResult
operator|.
name|row
operator|.
name|array
argument_list|()
argument_list|)
operator|+
literal|", cols: "
operator|+
name|rowStr
argument_list|)
expr_stmt|;
block|}
specifier|static
name|Subject
name|getSubject
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|secure
condition|)
return|return
operator|new
name|Subject
argument_list|()
return|;
comment|/*      * To authenticate the DemoClient, kinit should be invoked ahead.      * Here we try to get the Kerberos credential from the ticket cache.      */
name|LoginContext
name|context
init|=
operator|new
name|LoginContext
argument_list|(
literal|""
argument_list|,
operator|new
name|Subject
argument_list|()
argument_list|,
literal|null
argument_list|,
operator|new
name|Configuration
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|AppConfigurationEntry
index|[]
name|getAppConfigurationEntry
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|options
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|options
operator|.
name|put
argument_list|(
literal|"useKeyTab"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|options
operator|.
name|put
argument_list|(
literal|"storeKey"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|options
operator|.
name|put
argument_list|(
literal|"doNotPrompt"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|options
operator|.
name|put
argument_list|(
literal|"useTicketCache"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|options
operator|.
name|put
argument_list|(
literal|"renewTGT"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|options
operator|.
name|put
argument_list|(
literal|"refreshKrb5Config"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|options
operator|.
name|put
argument_list|(
literal|"isInitiator"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|String
name|ticketCache
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"KRB5CCNAME"
argument_list|)
decl_stmt|;
if|if
condition|(
name|ticketCache
operator|!=
literal|null
condition|)
block|{
name|options
operator|.
name|put
argument_list|(
literal|"ticketCache"
argument_list|,
name|ticketCache
argument_list|)
expr_stmt|;
block|}
name|options
operator|.
name|put
argument_list|(
literal|"debug"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
return|return
operator|new
name|AppConfigurationEntry
index|[]
block|{
operator|new
name|AppConfigurationEntry
argument_list|(
literal|"com.sun.security.auth.module.Krb5LoginModule"
argument_list|,
name|AppConfigurationEntry
operator|.
name|LoginModuleControlFlag
operator|.
name|REQUIRED
argument_list|,
name|options
argument_list|)
block|}
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|context
operator|.
name|login
argument_list|()
expr_stmt|;
return|return
name|context
operator|.
name|getSubject
argument_list|()
return|;
block|}
block|}
end_class

end_unit

