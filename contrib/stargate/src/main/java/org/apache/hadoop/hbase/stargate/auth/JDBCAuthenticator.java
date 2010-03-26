begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|stargate
operator|.
name|auth
package|;
end_package

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
name|sql
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|DriverManager
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|PreparedStatement
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|ResultSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
import|;
end_import

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
name|Log
import|;
end_import

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
name|stargate
operator|.
name|User
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_class
specifier|public
class|class
name|JDBCAuthenticator
extends|extends
name|Authenticator
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|JDBCAuthenticator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|int
name|MAX_RETRIES
init|=
literal|5
decl_stmt|;
specifier|static
specifier|final
name|long
name|RETRY_SLEEP_TIME
init|=
literal|1000
operator|*
literal|2
decl_stmt|;
name|String
name|url
decl_stmt|;
name|String
name|table
decl_stmt|;
name|String
name|user
decl_stmt|;
name|String
name|password
decl_stmt|;
name|Connection
name|connection
decl_stmt|;
name|PreparedStatement
name|userFetchStmt
decl_stmt|;
comment|/**    * Constructor    * @param conf    */
specifier|public
name|JDBCAuthenticator
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|)
block|{
name|this
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"stargate.auth.jdbc.url"
argument_list|)
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"stargate.auth.jdbc.table"
argument_list|)
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"stargate.auth.jdbc.user"
argument_list|)
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"stargate.auth.jdbc.password"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param url    * @param table    * @param user    * @param password    */
specifier|public
name|JDBCAuthenticator
parameter_list|(
name|String
name|url
parameter_list|,
name|String
name|table
parameter_list|,
name|String
name|user
parameter_list|,
name|String
name|password
parameter_list|)
block|{
name|this
operator|.
name|url
operator|=
name|url
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
name|this
operator|.
name|password
operator|=
name|password
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|User
name|getUserForToken
parameter_list|(
name|String
name|token
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|retries
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
try|try
block|{
if|if
condition|(
name|connection
operator|==
literal|null
condition|)
block|{
name|connection
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|url
argument_list|,
name|user
argument_list|,
name|password
argument_list|)
expr_stmt|;
name|userFetchStmt
operator|=
name|connection
operator|.
name|prepareStatement
argument_list|(
literal|"SELECT name, admin, disabled FROM "
operator|+
name|table
operator|+
literal|" WHERE token = ?"
argument_list|)
expr_stmt|;
block|}
name|ResultSet
name|results
decl_stmt|;
synchronized|synchronized
init|(
name|userFetchStmt
init|)
block|{
name|userFetchStmt
operator|.
name|setString
argument_list|(
literal|1
argument_list|,
name|token
argument_list|)
expr_stmt|;
name|results
operator|=
name|userFetchStmt
operator|.
name|executeQuery
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|results
operator|.
name|next
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|User
argument_list|(
name|results
operator|.
name|getString
argument_list|(
literal|1
argument_list|)
argument_list|,
name|token
argument_list|,
name|results
operator|.
name|getBoolean
argument_list|(
literal|2
argument_list|)
argument_list|,
name|results
operator|.
name|getBoolean
argument_list|(
literal|3
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|connection
operator|=
literal|null
expr_stmt|;
if|if
condition|(
operator|++
name|retries
operator|>
name|MAX_RETRIES
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
else|else
try|try
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|RETRY_SLEEP_TIME
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
block|}
end_class

end_unit

