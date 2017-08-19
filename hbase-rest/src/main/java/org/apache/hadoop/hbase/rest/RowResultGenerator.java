begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|DoNotRetryIOException
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
name|client
operator|.
name|Table
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
name|filter
operator|.
name|Filter
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
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RowResultGenerator
extends|extends
name|ResultGenerator
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RowResultGenerator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|valuesI
decl_stmt|;
specifier|private
name|Cell
name|cache
decl_stmt|;
specifier|public
name|RowResultGenerator
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|RowSpec
name|rowspec
parameter_list|,
specifier|final
name|Filter
name|filter
parameter_list|,
specifier|final
name|boolean
name|cacheBlocks
parameter_list|)
throws|throws
name|IllegalArgumentException
throws|,
name|IOException
block|{
name|Table
name|table
init|=
name|RESTServlet
operator|.
name|getInstance
argument_list|()
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
for|for
control|(
name|byte
index|[]
name|col
range|:
name|rowspec
operator|.
name|getColumns
argument_list|()
control|)
block|{
name|byte
index|[]
index|[]
name|split
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|col
argument_list|)
decl_stmt|;
if|if
condition|(
name|split
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|get
operator|.
name|addFamily
argument_list|(
name|split
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|split
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|get
operator|.
name|addColumn
argument_list|(
name|split
index|[
literal|0
index|]
argument_list|,
name|split
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid column specifier."
argument_list|)
throw|;
block|}
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
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
name|get
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
name|get
operator|.
name|setCacheBlocks
argument_list|(
name|cacheBlocks
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
name|listCells
argument_list|()
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|DoNotRetryIOException
name|e
parameter_list|)
block|{
comment|// Warn here because Stargate will return 404 in the case if multiple
comment|// column families were specified but one did not exist -- currently
comment|// HBase will fail the whole Get.
comment|// Specifying multiple columns in a URI should be uncommon usage but
comment|// help to avoid confusion by leaving a record of what happened here in
comment|// the log.
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
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
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
name|cache
operator|!=
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
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
name|Cell
name|next
parameter_list|()
block|{
if|if
condition|(
name|cache
operator|!=
literal|null
condition|)
block|{
name|Cell
name|kv
init|=
name|cache
decl_stmt|;
name|cache
operator|=
literal|null
expr_stmt|;
return|return
name|kv
return|;
block|}
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
name|putBack
parameter_list|(
name|Cell
name|kv
parameter_list|)
block|{
name|this
operator|.
name|cache
operator|=
name|kv
expr_stmt|;
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

