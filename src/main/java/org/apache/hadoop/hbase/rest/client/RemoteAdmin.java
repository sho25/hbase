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
name|rest
operator|.
name|client
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
name|org
operator|.
name|apache
operator|.
name|hadoop
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
name|conf
operator|.
name|Configuration
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
name|HTableDescriptor
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
name|Constants
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
name|model
operator|.
name|TableSchemaModel
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|RemoteAdmin
block|{
specifier|final
name|Client
name|client
decl_stmt|;
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|final
name|String
name|accessToken
decl_stmt|;
specifier|final
name|int
name|maxRetries
decl_stmt|;
specifier|final
name|long
name|sleepTime
decl_stmt|;
comment|/**    * Constructor    * @param client    * @param conf    */
specifier|public
name|RemoteAdmin
parameter_list|(
name|Client
name|client
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
argument_list|(
name|client
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param client    * @param conf    * @param accessToken    */
specifier|public
name|RemoteAdmin
parameter_list|(
name|Client
name|client
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|accessToken
parameter_list|)
block|{
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|accessToken
operator|=
name|accessToken
expr_stmt|;
name|this
operator|.
name|maxRetries
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.rest.client.max.retries"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|this
operator|.
name|sleepTime
operator|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.rest.client.sleep"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param tableName name of table to check    * @return true if all regions of the table are available    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|boolean
name|isTableAvailable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|isTableAvailable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param tableName name of table to check    * @return true if all regions of the table are available    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|boolean
name|isTableAvailable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
if|if
condition|(
name|accessToken
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|accessToken
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"exists"
argument_list|)
expr_stmt|;
name|int
name|code
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|maxRetries
condition|;
name|i
operator|++
control|)
block|{
name|Response
name|response
init|=
name|client
operator|.
name|get
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|code
operator|=
name|response
operator|.
name|getCode
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|code
condition|)
block|{
case|case
literal|200
case|:
return|return
literal|true
return|;
case|case
literal|404
case|:
return|return
literal|false
return|;
case|case
literal|509
case|:
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{ }
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"exists request returned "
operator|+
name|code
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"exists request timed out"
argument_list|)
throw|;
block|}
comment|/**    * Creates a new table.    * @param desc table descriptor for table    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|void
name|createTable
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
name|TableSchemaModel
name|model
init|=
operator|new
name|TableSchemaModel
argument_list|(
name|desc
argument_list|)
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
if|if
condition|(
name|accessToken
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|accessToken
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|desc
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"schema"
argument_list|)
expr_stmt|;
name|int
name|code
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|maxRetries
condition|;
name|i
operator|++
control|)
block|{
name|Response
name|response
init|=
name|client
operator|.
name|put
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|,
name|model
operator|.
name|createProtobufOutput
argument_list|()
argument_list|)
decl_stmt|;
name|code
operator|=
name|response
operator|.
name|getCode
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|code
condition|)
block|{
case|case
literal|201
case|:
return|return;
case|case
literal|509
case|:
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{ }
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"create request returned "
operator|+
name|code
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"create request timed out"
argument_list|)
throw|;
block|}
comment|/**    * Deletes a table.    * @param tableName name of table to delete    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|void
name|deleteTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Deletes a table.    * @param tableName name of table to delete    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|void
name|deleteTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
if|if
condition|(
name|accessToken
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|accessToken
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"schema"
argument_list|)
expr_stmt|;
name|int
name|code
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|maxRetries
condition|;
name|i
operator|++
control|)
block|{
name|Response
name|response
init|=
name|client
operator|.
name|delete
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|code
operator|=
name|response
operator|.
name|getCode
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|code
condition|)
block|{
case|case
literal|200
case|:
return|return;
case|case
literal|509
case|:
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{ }
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"delete request returned "
operator|+
name|code
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"delete request timed out"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

