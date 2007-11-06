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
name|Writer
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|HTableDescriptor
import|;
end_import

begin_comment
comment|/**  * Creates tables.  */
end_comment

begin_class
specifier|public
class|class
name|CreateCommand
extends|extends
name|SchemaModificationCommand
block|{
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|columnSpecMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|CreateCommand
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
name|HTableDescriptor
name|tableDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|columnDesc
init|=
literal|null
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|columns
init|=
name|columnSpecMap
operator|.
name|keySet
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|column
range|:
name|columns
control|)
block|{
name|columnDesc
operator|=
name|getColumnDescriptor
argument_list|(
name|column
argument_list|,
name|columnSpecMap
operator|.
name|get
argument_list|(
name|column
argument_list|)
argument_list|)
expr_stmt|;
name|tableDesc
operator|.
name|addFamily
argument_list|(
name|columnDesc
argument_list|)
expr_stmt|;
block|}
name|println
argument_list|(
literal|"Creating table... Please wait."
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|tableDesc
argument_list|)
expr_stmt|;
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|0
argument_list|,
literal|"Table created successfully."
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|0
argument_list|,
name|extractErrMsg
argument_list|(
name|e
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|/**    * Sets the table to be created.    * @param table Table to be created    */
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
comment|/**    * Adds a column specification.      * @param columnSpec Column specification    */
specifier|public
name|void
name|addColumnSpec
parameter_list|(
name|String
name|column
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|columnSpec
parameter_list|)
block|{
name|columnSpecMap
operator|.
name|put
argument_list|(
name|column
argument_list|,
name|columnSpec
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

