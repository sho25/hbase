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
comment|/**  * Prints information about tables.  */
end_comment

begin_class
specifier|public
class|class
name|DescCommand
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
literal|"Column Family Descriptor"
block|}
decl_stmt|;
specifier|private
name|Text
name|tableName
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
name|DescCommand
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
name|DescCommand
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
block|}
specifier|public
name|ReturnMsg
name|execute
parameter_list|(
specifier|final
name|Configuration
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
literal|"Syntax error : Please check 'Describe' syntax"
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
literal|"Table not found"
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
name|formatter
operator|.
name|header
argument_list|(
name|HEADER
argument_list|)
expr_stmt|;
comment|// Do a toString on the HColumnDescriptors
name|String
index|[]
name|columnStrs
init|=
operator|new
name|String
index|[
name|columns
operator|.
name|length
index|]
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
name|String
name|tmp
init|=
name|columns
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
decl_stmt|;
comment|// Strip the curly-brackets if present.
if|if
condition|(
name|tmp
operator|.
name|length
argument_list|()
operator|>
literal|2
operator|&&
name|tmp
operator|.
name|startsWith
argument_list|(
literal|"{"
argument_list|)
operator|&&
name|tmp
operator|.
name|endsWith
argument_list|(
literal|"}"
argument_list|)
condition|)
block|{
name|tmp
operator|=
name|tmp
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|tmp
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|columnStrs
index|[
name|i
index|]
operator|=
name|tmp
expr_stmt|;
name|formatter
operator|.
name|row
argument_list|(
operator|new
name|String
index|[]
block|{
name|columnStrs
index|[
name|i
index|]
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
name|columns
operator|.
name|length
operator|+
literal|" columnfamily(s) in set"
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
name|table
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
operator|new
name|Text
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

