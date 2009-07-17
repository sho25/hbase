begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
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
name|KeyValue
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
name|hbase
operator|.
name|client
operator|.
name|HTablePool
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

begin_class
specifier|public
class|class
name|RowResultGenerator
extends|extends
name|ResultGenerator
block|{
specifier|private
name|Iterator
argument_list|<
name|KeyValue
argument_list|>
name|valuesI
decl_stmt|;
specifier|public
name|RowResultGenerator
parameter_list|(
name|String
name|tableName
parameter_list|,
name|RowSpec
name|rowspec
parameter_list|)
throws|throws
name|IllegalArgumentException
throws|,
name|IOException
block|{
name|HTablePool
name|pool
init|=
name|RESTServlet
operator|.
name|getInstance
argument_list|()
operator|.
name|getTablePool
argument_list|()
decl_stmt|;
name|HTable
name|table
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
try|try
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|rowspec
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|rowspec
operator|.
name|hasColumns
argument_list|()
condition|)
block|{
name|get
operator|.
name|addColumns
argument_list|(
name|rowspec
operator|.
name|getColumns
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// rowspec does not explicitly specify columns, return them all
for|for
control|(
name|HColumnDescriptor
name|family
range|:
name|table
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getFamilies
argument_list|()
control|)
block|{
name|get
operator|.
name|addFamily
argument_list|(
name|family
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|get
operator|.
name|setTimeRange
argument_list|(
name|rowspec
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|rowspec
operator|.
name|getEndTime
argument_list|()
argument_list|)
expr_stmt|;
name|get
operator|.
name|setMaxVersions
argument_list|(
name|rowspec
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
operator|&&
operator|!
name|result
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|valuesI
operator|=
name|result
operator|.
name|list
argument_list|()
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|pool
operator|.
name|putTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{   }
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
if|if
condition|(
name|valuesI
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|valuesI
operator|.
name|hasNext
argument_list|()
return|;
block|}
specifier|public
name|KeyValue
name|next
parameter_list|()
block|{
if|if
condition|(
name|valuesI
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
return|return
name|valuesI
operator|.
name|next
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchElementException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"remove not supported"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

