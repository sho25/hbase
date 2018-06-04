begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|rest
package|;
end_package

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
name|Cell
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
name|CellUtil
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
name|client
operator|.
name|Get
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
name|client
operator|.
name|Put
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
name|client
operator|.
name|Result
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
name|rest
operator|.
name|client
operator|.
name|Client
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
name|rest
operator|.
name|client
operator|.
name|Cluster
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
name|rest
operator|.
name|client
operator|.
name|RemoteHTable
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
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RESTDemoClient
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
literal|"REST Demo"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Usage: RESTDemoClient [host=localhost] [port=9090] [secure=false]"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"This demo assumes you have a table called \"example\""
operator|+
literal|" with a column family called \"family1\""
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
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|String
name|principal
init|=
name|conf
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|REST_KERBEROS_PRINCIPAL
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
name|RESTDemoClient
name|client
init|=
operator|new
name|RESTDemoClient
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
name|Cluster
name|cluster
init|=
operator|new
name|Cluster
argument_list|()
decl_stmt|;
name|cluster
operator|.
name|add
argument_list|(
name|host
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|Client
name|restClient
init|=
operator|new
name|Client
argument_list|(
name|cluster
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|Constants
operator|.
name|REST_SSL_ENABLED
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
try|try
init|(
name|RemoteHTable
name|remoteTable
init|=
operator|new
name|RemoteHTable
argument_list|(
name|restClient
argument_list|,
name|conf
argument_list|,
literal|"example"
argument_list|)
init|)
block|{
comment|// Write data to the table
name|String
name|rowKey
init|=
literal|"row1"
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|rowKey
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
literal|"family1"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"qualifier1"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"value1"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|remoteTable
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// Get the data from the table
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|rowKey
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|remoteTable
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|result
operator|!=
literal|null
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|remoteTable
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|+
literal|" should have a row with key as "
operator|+
name|rowKey
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
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
name|Cell
name|cell
range|:
name|result
operator|.
name|rawCells
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
name|Bytes
operator|.
name|toString
argument_list|(
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|cell
argument_list|)
argument_list|)
operator|+
literal|"\t"
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
name|Bytes
operator|.
name|toString
argument_list|(
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|cell
argument_list|)
argument_list|)
operator|+
literal|"\t"
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
name|Bytes
operator|.
name|toString
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|cell
argument_list|)
argument_list|)
operator|+
literal|"\t"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"timestamp = "
operator|+
name|Long
operator|.
name|toString
argument_list|(
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
comment|/*      * To authenticate the demo client, kinit should be invoked ahead. Here we try to get the      * Kerberos credential from the ticket cache.      */
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

