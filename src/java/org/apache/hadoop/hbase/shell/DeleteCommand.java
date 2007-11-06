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
name|List
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
name|HTable
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
comment|/**  * Deletes values from tables.  */
end_comment

begin_class
specifier|public
class|class
name|DeleteCommand
extends|extends
name|BasicCommand
block|{
specifier|public
name|DeleteCommand
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
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|String
name|rowKey
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|columnList
decl_stmt|;
specifier|public
name|ReturnMsg
name|execute
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|columnList
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Column list is null"
argument_list|)
throw|;
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
name|HTable
name|hTable
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|lockID
init|=
name|hTable
operator|.
name|startUpdate
argument_list|(
operator|new
name|Text
argument_list|(
name|rowKey
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Text
name|column
range|:
name|getColumnList
argument_list|(
name|admin
argument_list|,
name|hTable
argument_list|)
control|)
block|{
name|hTable
operator|.
name|delete
argument_list|(
name|lockID
argument_list|,
operator|new
name|Text
argument_list|(
name|column
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|hTable
operator|.
name|commit
argument_list|(
name|lockID
argument_list|)
expr_stmt|;
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|1
argument_list|,
literal|"Column(s) deleted successfully."
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
specifier|public
name|void
name|setRow
parameter_list|(
name|String
name|row
parameter_list|)
block|{
name|this
operator|.
name|rowKey
operator|=
name|row
expr_stmt|;
block|}
comment|/**    * Sets the column list.    * @param columnList    */
specifier|public
name|void
name|setColumnList
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columnList
parameter_list|)
block|{
name|this
operator|.
name|columnList
operator|=
name|columnList
expr_stmt|;
block|}
comment|/**    * @param admin    * @param hTable    * @return return the column list.    */
specifier|public
name|Text
index|[]
name|getColumnList
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|HTable
name|hTable
parameter_list|)
block|{
name|Text
index|[]
name|columns
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|this
operator|.
name|columnList
operator|.
name|contains
argument_list|(
literal|"*"
argument_list|)
condition|)
block|{
name|columns
operator|=
name|hTable
operator|.
name|getRow
argument_list|(
operator|new
name|Text
argument_list|(
name|this
operator|.
name|rowKey
argument_list|)
argument_list|)
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|Text
index|[]
block|{}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|Text
argument_list|>
name|tmpList
init|=
operator|new
name|ArrayList
argument_list|<
name|Text
argument_list|>
argument_list|()
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
name|this
operator|.
name|columnList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Text
name|column
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|columnList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|contains
argument_list|(
literal|":"
argument_list|)
condition|)
name|column
operator|=
operator|new
name|Text
argument_list|(
name|this
operator|.
name|columnList
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
else|else
name|column
operator|=
operator|new
name|Text
argument_list|(
name|this
operator|.
name|columnList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|":"
argument_list|)
expr_stmt|;
name|tmpList
operator|.
name|add
argument_list|(
name|column
argument_list|)
expr_stmt|;
block|}
name|columns
operator|=
name|tmpList
operator|.
name|toArray
argument_list|(
operator|new
name|Text
index|[]
block|{}
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
return|return
name|columns
return|;
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
name|DELETE
return|;
block|}
block|}
end_class

end_unit

