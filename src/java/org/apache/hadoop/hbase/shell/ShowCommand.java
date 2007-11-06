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
name|HTableDescriptor
import|;
end_import

begin_comment
comment|/**  * Shows all available tables.  */
end_comment

begin_class
specifier|public
class|class
name|ShowCommand
extends|extends
name|BasicCommand
block|{
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|HEADER
init|=
operator|new
name|String
index|[]
block|{
literal|"Name"
block|,
literal|"Descriptor"
block|}
decl_stmt|;
specifier|private
name|String
name|command
decl_stmt|;
specifier|private
specifier|final
name|TableFormatter
name|formatter
decl_stmt|;
comment|// Not instantiable
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
name|ShowCommand
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ShowCommand
parameter_list|(
specifier|final
name|Writer
name|o
parameter_list|,
specifier|final
name|TableFormatter
name|f
parameter_list|)
block|{
name|this
argument_list|(
name|o
argument_list|,
name|f
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ShowCommand
parameter_list|(
specifier|final
name|Writer
name|o
parameter_list|,
specifier|final
name|TableFormatter
name|f
parameter_list|,
specifier|final
name|String
name|argument
parameter_list|)
block|{
name|super
argument_list|(
name|o
argument_list|)
expr_stmt|;
name|this
operator|.
name|formatter
operator|=
name|f
expr_stmt|;
name|this
operator|.
name|command
operator|=
name|argument
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
name|command
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|0
argument_list|,
literal|"Syntax error : Please check 'Show' syntax"
argument_list|)
return|;
block|}
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
name|int
name|tableLength
init|=
literal|0
decl_stmt|;
if|if
condition|(
literal|"tables"
operator|.
name|equals
argument_list|(
name|this
operator|.
name|command
argument_list|)
condition|)
block|{
name|HTableDescriptor
index|[]
name|tables
init|=
name|admin
operator|.
name|listTables
argument_list|()
decl_stmt|;
name|tableLength
operator|=
name|tables
operator|.
name|length
expr_stmt|;
if|if
condition|(
name|tableLength
operator|==
literal|0
condition|)
block|{
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|0
argument_list|,
literal|"No tables found"
argument_list|)
return|;
block|}
name|formatter
operator|.
name|header
argument_list|(
name|HEADER
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|tableLength
condition|;
name|i
operator|++
control|)
block|{
name|String
name|tableName
init|=
name|tables
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|formatter
operator|.
name|row
argument_list|(
operator|new
name|String
index|[]
block|{
name|tableName
block|,
name|tables
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
block|}
argument_list|)
expr_stmt|;
block|}
name|formatter
operator|.
name|footer
argument_list|()
expr_stmt|;
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|1
argument_list|,
name|tableLength
operator|+
literal|" table(s) in set"
argument_list|)
return|;
block|}
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|0
argument_list|,
literal|"Missing parameters. Please check 'Show' syntax"
argument_list|)
return|;
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
block|}
specifier|public
name|void
name|setArgument
parameter_list|(
name|String
name|argument
parameter_list|)
block|{
name|this
operator|.
name|command
operator|=
name|argument
expr_stmt|;
block|}
block|}
end_class

end_unit

