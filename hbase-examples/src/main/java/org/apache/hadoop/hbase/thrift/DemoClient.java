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
name|nio
operator|.
name|ByteBuffer
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
name|text
operator|.
name|NumberFormat
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
name|LoginContext
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
name|Mutation
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
name|Bytes
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
name|ClientUtils
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
name|TSaslClientTransport
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|DemoClient
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|DemoClient
operator|.
name|class
argument_list|)
decl_stmt|;
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
specifier|private
specifier|static
name|boolean
name|secure
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|String
name|serverPrincipal
init|=
literal|"hbase"
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
literal|2
operator|||
name|args
operator|.
name|length
argument_list|>
literal|4
operator|||
operator|(
name|args
operator|.
name|length
operator|>
literal|2
operator|&&
operator|!
name|isBoolean
argument_list|(
name|args
index|[
literal|2
index|]
argument_list|)
operator|)
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
literal|"Usage: DemoClient host port [secure=false [server-principal=hbase] ]"
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
name|host
operator|=
name|args
index|[
literal|0
index|]
expr_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|>
literal|2
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
literal|2
index|]
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|args
operator|.
name|length
operator|==
literal|4
condition|)
block|{
name|serverPrincipal
operator|=
name|args
index|[
literal|3
index|]
expr_stmt|;
block|}
specifier|final
name|DemoClient
name|client
init|=
operator|new
name|DemoClient
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
specifier|private
specifier|static
name|boolean
name|isBoolean
parameter_list|(
name|String
name|s
parameter_list|)
block|{
return|return
name|Boolean
operator|.
name|TRUE
operator|.
name|toString
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|s
argument_list|)
operator|||
name|Boolean
operator|.
name|FALSE
operator|.
name|toString
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|s
argument_list|)
return|;
block|}
name|DemoClient
parameter_list|()
block|{   }
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
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|s
argument_list|)
return|;
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
if|if
condition|(
name|secure
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|saslProperties
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|saslProperties
operator|.
name|put
argument_list|(
name|Sasl
operator|.
name|QOP
argument_list|,
literal|"auth-conf,auth-int,auth"
argument_list|)
expr_stmt|;
comment|/*        * The Thrift server the DemoClient is trying to connect to        * must have a matching principal, and support authentication.        *        * The HBase cluster must be secure, allow proxy user.        */
name|transport
operator|=
operator|new
name|TSaslClientTransport
argument_list|(
literal|"GSSAPI"
argument_list|,
literal|null
argument_list|,
name|serverPrincipal
argument_list|,
comment|// Thrift server user name, should be an authorized proxy user.
name|host
argument_list|,
comment|// Thrift server domain
name|saslProperties
argument_list|,
literal|null
argument_list|,
name|transport
argument_list|)
expr_stmt|;
block|}
name|transport
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
name|transport
argument_list|,
literal|true
argument_list|,
literal|true
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
comment|// Scan all tables, look for the demo table and delete it.
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
name|client
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
name|ClientUtils
operator|.
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
name|ClientUtils
operator|.
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
name|ClientUtils
operator|.
name|utf8
argument_list|(
name|t
argument_list|)
argument_list|)
condition|)
block|{
if|if
condition|(
name|client
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
name|ClientUtils
operator|.
name|utf8
argument_list|(
name|name
operator|.
name|array
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|client
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
name|ClientUtils
operator|.
name|utf8
argument_list|(
name|name
operator|.
name|array
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|deleteTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Create the demo table with two column families, entry: and unused:
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
name|ClientUtils
operator|.
name|utf8
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|client
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
name|ClientUtils
operator|.
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
name|client
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
name|ClientUtils
operator|.
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
name|col2
operator|.
name|maxVersions
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|ByteBuffer
argument_list|,
name|ByteBuffer
argument_list|>
name|dummyAttributes
init|=
literal|null
decl_stmt|;
name|boolean
name|writeToWal
init|=
literal|false
decl_stmt|;
comment|// Test UTF-8 handling
name|byte
index|[]
name|invalid
init|=
block|{
operator|(
name|byte
operator|)
literal|'f'
block|,
operator|(
name|byte
operator|)
literal|'o'
block|,
operator|(
name|byte
operator|)
literal|'o'
block|,
operator|(
name|byte
operator|)
literal|'-'
block|,
operator|(
name|byte
operator|)
literal|0xfc
block|,
operator|(
name|byte
operator|)
literal|0xa1
block|,
operator|(
name|byte
operator|)
literal|0xa1
block|,
operator|(
name|byte
operator|)
literal|0xa1
block|,
operator|(
name|byte
operator|)
literal|0xa1
block|}
decl_stmt|;
name|byte
index|[]
name|valid
init|=
block|{
operator|(
name|byte
operator|)
literal|'f'
block|,
operator|(
name|byte
operator|)
literal|'o'
block|,
operator|(
name|byte
operator|)
literal|'o'
block|,
operator|(
name|byte
operator|)
literal|'-'
block|,
operator|(
name|byte
operator|)
literal|0xE7
block|,
operator|(
name|byte
operator|)
literal|0x94
block|,
operator|(
name|byte
operator|)
literal|0x9F
block|,
operator|(
name|byte
operator|)
literal|0xE3
block|,
operator|(
name|byte
operator|)
literal|0x83
block|,
operator|(
name|byte
operator|)
literal|0x93
block|,
operator|(
name|byte
operator|)
literal|0xE3
block|,
operator|(
name|byte
operator|)
literal|0x83
block|,
operator|(
name|byte
operator|)
literal|0xBC
block|,
operator|(
name|byte
operator|)
literal|0xE3
block|,
operator|(
name|byte
operator|)
literal|0x83
block|,
operator|(
name|byte
operator|)
literal|0xAB
block|}
decl_stmt|;
name|ArrayList
argument_list|<
name|Mutation
argument_list|>
name|mutations
decl_stmt|;
comment|// non-utf8 is fine for data
name|mutations
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
operator|new
name|Mutation
argument_list|(
literal|false
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:foo"
argument_list|)
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|invalid
argument_list|)
argument_list|,
name|writeToWal
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|mutateRow
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
argument_list|,
name|mutations
argument_list|,
name|dummyAttributes
argument_list|)
expr_stmt|;
comment|// this row name is valid utf8
name|mutations
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
operator|new
name|Mutation
argument_list|(
literal|false
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:foo"
argument_list|)
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|valid
argument_list|)
argument_list|,
name|writeToWal
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|mutateRow
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|valid
argument_list|)
argument_list|,
name|mutations
argument_list|,
name|dummyAttributes
argument_list|)
expr_stmt|;
comment|// non-utf8 is now allowed in row names because HBase stores values as binary
name|mutations
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
operator|new
name|Mutation
argument_list|(
literal|false
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:foo"
argument_list|)
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|invalid
argument_list|)
argument_list|,
name|writeToWal
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|mutateRow
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|invalid
argument_list|)
argument_list|,
name|mutations
argument_list|,
name|dummyAttributes
argument_list|)
expr_stmt|;
comment|// Run a scanner on the rows we just created
name|ArrayList
argument_list|<
name|ByteBuffer
argument_list|>
name|columnNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|columnNames
operator|.
name|add
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Starting scanner..."
argument_list|)
expr_stmt|;
name|int
name|scanner
init|=
name|client
operator|.
name|scannerOpen
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|""
argument_list|)
argument_list|)
argument_list|,
name|columnNames
argument_list|,
name|dummyAttributes
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|List
argument_list|<
name|TRowResult
argument_list|>
name|entry
init|=
name|client
operator|.
name|scannerGet
argument_list|(
name|scanner
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
break|break;
block|}
name|printRow
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
comment|// Run some operations on a bunch of rows
for|for
control|(
name|int
name|i
init|=
literal|100
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
comment|// format row keys as "00000" to "00100"
name|NumberFormat
name|nf
init|=
name|NumberFormat
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|nf
operator|.
name|setMinimumIntegerDigits
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|nf
operator|.
name|setGroupingUsed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|byte
index|[]
name|row
init|=
name|bytes
argument_list|(
name|nf
operator|.
name|format
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|mutations
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
operator|new
name|Mutation
argument_list|(
literal|false
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"unused:"
argument_list|)
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"DELETE_ME"
argument_list|)
argument_list|)
argument_list|,
name|writeToWal
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|mutateRow
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|mutations
argument_list|,
name|dummyAttributes
argument_list|)
expr_stmt|;
name|printRow
argument_list|(
name|client
operator|.
name|getRow
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|dummyAttributes
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|deleteAllRow
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|dummyAttributes
argument_list|)
expr_stmt|;
comment|// sleep to force later timestamp
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|50
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// no-op
block|}
name|mutations
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
operator|new
name|Mutation
argument_list|(
literal|false
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:num"
argument_list|)
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"0"
argument_list|)
argument_list|)
argument_list|,
name|writeToWal
argument_list|)
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
operator|new
name|Mutation
argument_list|(
literal|false
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:foo"
argument_list|)
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"FOO"
argument_list|)
argument_list|)
argument_list|,
name|writeToWal
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|mutateRow
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|mutations
argument_list|,
name|dummyAttributes
argument_list|)
expr_stmt|;
name|printRow
argument_list|(
name|client
operator|.
name|getRow
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|dummyAttributes
argument_list|)
argument_list|)
expr_stmt|;
name|Mutation
name|m
decl_stmt|;
name|mutations
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|m
operator|=
operator|new
name|Mutation
argument_list|()
expr_stmt|;
name|m
operator|.
name|column
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:foo"
argument_list|)
argument_list|)
expr_stmt|;
name|m
operator|.
name|isDelete
operator|=
literal|true
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
name|m
argument_list|)
expr_stmt|;
name|m
operator|=
operator|new
name|Mutation
argument_list|()
expr_stmt|;
name|m
operator|.
name|column
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:num"
argument_list|)
argument_list|)
expr_stmt|;
name|m
operator|.
name|value
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"-1"
argument_list|)
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
name|m
argument_list|)
expr_stmt|;
name|client
operator|.
name|mutateRow
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|mutations
argument_list|,
name|dummyAttributes
argument_list|)
expr_stmt|;
name|printRow
argument_list|(
name|client
operator|.
name|getRow
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|dummyAttributes
argument_list|)
argument_list|)
expr_stmt|;
name|mutations
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
operator|new
name|Mutation
argument_list|(
literal|false
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:num"
argument_list|)
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|writeToWal
argument_list|)
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
operator|new
name|Mutation
argument_list|(
literal|false
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:sqr"
argument_list|)
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
operator|*
name|i
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|writeToWal
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|mutateRow
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|mutations
argument_list|,
name|dummyAttributes
argument_list|)
expr_stmt|;
name|printRow
argument_list|(
name|client
operator|.
name|getRow
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|dummyAttributes
argument_list|)
argument_list|)
expr_stmt|;
comment|// sleep to force later timestamp
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|50
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// no-op
block|}
name|mutations
operator|.
name|clear
argument_list|()
expr_stmt|;
name|m
operator|=
operator|new
name|Mutation
argument_list|()
expr_stmt|;
name|m
operator|.
name|column
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:num"
argument_list|)
argument_list|)
expr_stmt|;
name|m
operator|.
name|value
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"-999"
argument_list|)
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
name|m
argument_list|)
expr_stmt|;
name|m
operator|=
operator|new
name|Mutation
argument_list|()
expr_stmt|;
name|m
operator|.
name|column
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:sqr"
argument_list|)
argument_list|)
expr_stmt|;
name|m
operator|.
name|isDelete
operator|=
literal|true
expr_stmt|;
name|client
operator|.
name|mutateRowTs
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|mutations
argument_list|,
literal|1
argument_list|,
name|dummyAttributes
argument_list|)
expr_stmt|;
comment|// shouldn't override latest
name|printRow
argument_list|(
name|client
operator|.
name|getRow
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|dummyAttributes
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TCell
argument_list|>
name|versions
init|=
name|client
operator|.
name|getVer
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:num"
argument_list|)
argument_list|)
argument_list|,
literal|10
argument_list|,
name|dummyAttributes
argument_list|)
decl_stmt|;
name|printVersions
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|versions
argument_list|)
expr_stmt|;
if|if
condition|(
name|versions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"FATAL: wrong # of versions"
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
name|List
argument_list|<
name|TCell
argument_list|>
name|result
init|=
name|client
operator|.
name|get
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"entry:foo"
argument_list|)
argument_list|)
argument_list|,
name|dummyAttributes
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|result
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"FATAL: shouldn't get here"
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
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
comment|// scan all rows/columnNames
name|columnNames
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|ColumnDescriptor
name|col2
range|:
name|client
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
literal|"column with name: "
operator|+
operator|new
name|String
argument_list|(
name|col2
operator|.
name|name
operator|.
name|array
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|col2
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|columnNames
operator|.
name|add
argument_list|(
name|col2
operator|.
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
literal|"Starting scanner..."
argument_list|)
expr_stmt|;
name|scanner
operator|=
name|client
operator|.
name|scannerOpenWithStop
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|t
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"00020"
argument_list|)
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|(
literal|"00040"
argument_list|)
argument_list|)
argument_list|,
name|columnNames
argument_list|,
name|dummyAttributes
argument_list|)
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|List
argument_list|<
name|TRowResult
argument_list|>
name|entry
init|=
name|client
operator|.
name|scannerGet
argument_list|(
name|scanner
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Scanner finished"
argument_list|)
expr_stmt|;
break|break;
block|}
name|printRow
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|ClientUtils
operator|.
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
name|ClientUtils
operator|.
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
name|ClientUtils
operator|.
name|printRow
argument_list|(
name|rowResult
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|printRow
parameter_list|(
name|List
argument_list|<
name|TRowResult
argument_list|>
name|rows
parameter_list|)
block|{
for|for
control|(
name|TRowResult
name|rowResult
range|:
name|rows
control|)
block|{
name|printRow
argument_list|(
name|rowResult
argument_list|)
expr_stmt|;
block|}
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
block|{
return|return
operator|new
name|Subject
argument_list|()
return|;
block|}
name|LoginContext
name|context
init|=
name|ClientUtils
operator|.
name|getLoginContext
argument_list|()
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

