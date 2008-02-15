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
operator|.
name|hql
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
name|io
operator|.
name|Writer
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
name|HBaseAdmin
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * Enables tables.  */
end_comment

begin_class
specifier|public
class|class
name|EnableCommand
extends|extends
name|BasicCommand
block|{
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|public
name|EnableCommand
parameter_list|(
name|Writer
name|o
parameter_list|)
block|{
name|super
argument_list|(
name|o
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReturnMsg
name|execute
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|)
block|{
assert|assert
name|tableName
operator|!=
literal|null
assert|;
try|try
block|{
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|admin
operator|.
name|tableExists
argument_list|(
operator|new
name|Text
argument_list|(
name|tableName
argument_list|)
argument_list|)
condition|)
block|{
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|0
argument_list|,
literal|"'"
operator|+
name|tableName
operator|+
literal|"'"
operator|+
name|TABLE_NOT_FOUND
argument_list|)
return|;
block|}
name|admin
operator|.
name|enableTable
argument_list|(
operator|new
name|Text
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|1
argument_list|,
literal|"Table enabled successfully."
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|String
index|[]
name|msg
init|=
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|split
argument_list|(
literal|"[\n]"
argument_list|)
decl_stmt|;
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|0
argument_list|,
name|msg
index|[
literal|0
index|]
argument_list|)
return|;
block|}
block|}
specifier|public
name|void
name|setTable
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|table
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|CommandType
name|getCommandType
parameter_list|()
block|{
return|return
name|CommandType
operator|.
name|DDL
return|;
block|}
block|}
end_class

end_unit

