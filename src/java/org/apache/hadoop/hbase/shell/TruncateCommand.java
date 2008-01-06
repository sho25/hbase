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
name|shell
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
name|hbase
operator|.
name|HColumnDescriptor
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
name|HConnection
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
name|HConnectionManager
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * Truncate table is used to clean all data from a table.  */
end_comment

begin_class
specifier|public
class|class
name|TruncateCommand
extends|extends
name|BasicCommand
block|{
specifier|private
name|Text
name|tableName
decl_stmt|;
specifier|public
name|TruncateCommand
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
specifier|final
name|HBaseConfiguration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|tableName
operator|==
literal|null
condition|)
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|0
argument_list|,
literal|"Syntax error : Please check 'Truncate' syntax."
argument_list|)
return|;
try|try
block|{
name|HConnection
name|conn
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
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
name|conn
operator|.
name|tableExists
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
condition|)
block|{
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|0
argument_list|,
literal|"Table not found."
argument_list|)
return|;
block|}
name|HTableDescriptor
index|[]
name|tables
init|=
name|conn
operator|.
name|listTables
argument_list|()
decl_stmt|;
name|HColumnDescriptor
index|[]
name|columns
init|=
literal|null
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
name|tables
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|tables
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
condition|)
block|{
name|columns
operator|=
name|tables
index|[
name|i
index|]
operator|.
name|getFamilies
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|HColumnDescriptor
index|[]
block|{}
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|println
argument_list|(
literal|"Truncating a '"
operator|+
name|tableName
operator|+
literal|"' table ... Please wait."
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// delete the table
name|HTableDescriptor
name|tableDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
operator|.
name|toString
argument_list|()
argument_list|)
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
name|columns
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|tableDesc
operator|.
name|addFamily
argument_list|(
name|columns
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|createTable
argument_list|(
name|tableDesc
argument_list|)
expr_stmt|;
comment|// re-create the table
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|0
argument_list|,
literal|"error msg : "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
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
literal|"' is successfully truncated."
argument_list|)
return|;
block|}
specifier|public
name|void
name|setTableName
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
operator|new
name|Text
argument_list|(
name|tableName
argument_list|)
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

