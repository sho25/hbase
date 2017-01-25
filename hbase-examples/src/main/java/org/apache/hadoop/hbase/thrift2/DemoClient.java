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
name|thrift2
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
name|HBaseConfiguration
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
name|thrift2
operator|.
name|generated
operator|.
name|TColumnValue
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
name|thrift2
operator|.
name|generated
operator|.
name|TGet
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
name|thrift2
operator|.
name|generated
operator|.
name|THBaseService
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
name|thrift2
operator|.
name|generated
operator|.
name|TPut
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
name|thrift2
operator|.
name|generated
operator|.
name|TResult
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
name|TFramedTransport
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

begin_class
specifier|public
class|class
name|DemoClient
block|{
specifier|private
specifier|static
name|String
name|host
init|=
literal|"localhost"
decl_stmt|;
specifier|private
specifier|static
name|int
name|port
init|=
literal|9090
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
name|user
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
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Thrift2 Demo"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Usage: DemoClient [host=localhost] [port=9090] [secure=false]"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"This demo assumes you have a table called \"example\" with a column family called \"family1\""
argument_list|)
expr_stmt|;
comment|// use passed in arguments instead of defaults
if|if
condition|(
name|args
operator|.
name|length
operator|>=
literal|1
condition|)
block|{
name|host
operator|=
name|args
index|[
literal|0
index|]
expr_stmt|;
block|}
if|if
condition|(
name|args
operator|.
name|length
operator|>=
literal|2
condition|)
block|{
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
block|}
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|String
name|principal
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.thrift.kerberos.principal"
argument_list|)
decl_stmt|;
if|if
condition|(
name|principal
operator|!=
literal|null
condition|)
block|{
name|secure
operator|=
literal|true
expr_stmt|;
name|int
name|slashIdx
init|=
name|principal
operator|.
name|indexOf
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
name|int
name|atIdx
init|=
name|principal
operator|.
name|indexOf
argument_list|(
literal|"@"
argument_list|)
decl_stmt|;
name|int
name|idx
init|=
name|slashIdx
operator|!=
operator|-
literal|1
condition|?
name|slashIdx
else|:
name|atIdx
operator|!=
operator|-
literal|1
condition|?
name|atIdx
else|:
name|principal
operator|.
name|length
argument_list|()
decl_stmt|;
name|user
operator|=
name|principal
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|idx
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|args
operator|.
name|length
operator|>=
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
literal|2
index|]
argument_list|)
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
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|timeout
init|=
literal|10000
decl_stmt|;
name|boolean
name|framed
init|=
literal|false
decl_stmt|;
name|TTransport
name|transport
init|=
operator|new
name|TSocket
argument_list|(
name|host
argument_list|,
name|port
argument_list|,
name|timeout
argument_list|)
decl_stmt|;
if|if
condition|(
name|framed
condition|)
block|{
name|transport
operator|=
operator|new
name|TFramedTransport
argument_list|(
name|transport
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|secure
condition|)
block|{
comment|/**        * The Thrift server the DemoClient is trying to connect to        * must have a matching principal, and support authentication.        *        * The HBase cluster must be secure, allow proxy user.        */
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
name|transport
operator|=
operator|new
name|TSaslClientTransport
argument_list|(
literal|"GSSAPI"
argument_list|,
literal|null
argument_list|,
name|user
operator|!=
literal|null
condition|?
name|user
else|:
literal|"hbase"
argument_list|,
comment|// Thrift server user name, should be an authorized proxy user
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
name|TProtocol
name|protocol
init|=
operator|new
name|TBinaryProtocol
argument_list|(
name|transport
argument_list|)
decl_stmt|;
comment|// This is our thrift client.
name|THBaseService
operator|.
name|Iface
name|client
init|=
operator|new
name|THBaseService
operator|.
name|Client
argument_list|(
name|protocol
argument_list|)
decl_stmt|;
comment|// open the transport
name|transport
operator|.
name|open
argument_list|()
expr_stmt|;
name|ByteBuffer
name|table
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
literal|"example"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|TPut
name|put
init|=
operator|new
name|TPut
argument_list|()
decl_stmt|;
name|put
operator|.
name|setRow
argument_list|(
literal|"row1"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|TColumnValue
name|columnValue
init|=
operator|new
name|TColumnValue
argument_list|()
decl_stmt|;
name|columnValue
operator|.
name|setFamily
argument_list|(
literal|"family1"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|columnValue
operator|.
name|setQualifier
argument_list|(
literal|"qualifier1"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|columnValue
operator|.
name|setValue
argument_list|(
literal|"value1"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TColumnValue
argument_list|>
name|columnValues
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|columnValues
operator|.
name|add
argument_list|(
name|columnValue
argument_list|)
expr_stmt|;
name|put
operator|.
name|setColumnValues
argument_list|(
name|columnValues
argument_list|)
expr_stmt|;
name|client
operator|.
name|put
argument_list|(
name|table
argument_list|,
name|put
argument_list|)
expr_stmt|;
name|TGet
name|get
init|=
operator|new
name|TGet
argument_list|()
decl_stmt|;
name|get
operator|.
name|setRow
argument_list|(
literal|"row1"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|TResult
name|result
init|=
name|client
operator|.
name|get
argument_list|(
name|table
argument_list|,
name|get
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"row = "
operator|+
operator|new
name|String
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|TColumnValue
name|resultColumnValue
range|:
name|result
operator|.
name|getColumnValues
argument_list|()
control|)
block|{
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"family = "
operator|+
operator|new
name|String
argument_list|(
name|resultColumnValue
operator|.
name|getFamily
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"qualifier = "
operator|+
operator|new
name|String
argument_list|(
name|resultColumnValue
operator|.
name|getFamily
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"value = "
operator|+
operator|new
name|String
argument_list|(
name|resultColumnValue
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"timestamp = "
operator|+
name|resultColumnValue
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|transport
operator|.
name|close
argument_list|()
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

