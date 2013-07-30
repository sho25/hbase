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
name|UnknownScannerException
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
name|HTableInterface
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
name|ResultScanner
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
name|Scan
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
name|hbase
operator|.
name|rest
operator|.
name|model
operator|.
name|ScannerModel
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
name|ScannerResultGenerator
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
name|ScannerResultGenerator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Filter
name|buildFilterFromModel
parameter_list|(
specifier|final
name|ScannerModel
name|model
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|filter
init|=
name|model
operator|.
name|getFilter
argument_list|()
decl_stmt|;
if|if
condition|(
name|filter
operator|==
literal|null
operator|||
name|filter
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|buildFilter
argument_list|(
name|filter
argument_list|)
return|;
block|}
specifier|private
name|String
name|id
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|KeyValue
argument_list|>
name|rowI
decl_stmt|;
specifier|private
name|KeyValue
name|cache
decl_stmt|;
specifier|private
name|ResultScanner
name|scanner
decl_stmt|;
specifier|private
name|Result
name|cached
decl_stmt|;
specifier|public
name|ScannerResultGenerator
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
parameter_list|)
throws|throws
name|IllegalArgumentException
throws|,
name|IOException
block|{
name|this
argument_list|(
name|tableName
argument_list|,
name|rowspec
argument_list|,
name|filter
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ScannerResultGenerator
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
name|int
name|caching
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
name|HTableInterface
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
name|Scan
name|scan
decl_stmt|;
if|if
condition|(
name|rowspec
operator|.
name|hasEndRow
argument_list|()
condition|)
block|{
name|scan
operator|=
operator|new
name|Scan
argument_list|(
name|rowspec
operator|.
name|getStartRow
argument_list|()
argument_list|,
name|rowspec
operator|.
name|getEndRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scan
operator|=
operator|new
name|Scan
argument_list|(
name|rowspec
operator|.
name|getStartRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rowspec
operator|.
name|hasColumns
argument_list|()
condition|)
block|{
name|byte
index|[]
index|[]
name|columns
init|=
name|rowspec
operator|.
name|getColumns
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|column
range|:
name|columns
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
name|column
argument_list|)
decl_stmt|;
if|if
condition|(
name|split
operator|.
name|length
operator|>
literal|1
operator|&&
operator|(
name|split
index|[
literal|1
index|]
operator|!=
literal|null
operator|&&
name|split
index|[
literal|1
index|]
operator|.
name|length
operator|!=
literal|0
operator|)
condition|)
block|{
name|scan
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
name|scan
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
block|}
block|}
name|scan
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
name|scan
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
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
comment|// always disable block caching on the cluster when scanning
name|scan
operator|.
name|setCacheBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|caching
operator|>
literal|0
condition|)
block|{
name|scan
operator|.
name|setCaching
argument_list|(
name|caching
argument_list|)
expr_stmt|;
block|}
name|scanner
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|cached
operator|=
literal|null
expr_stmt|;
name|id
operator|=
name|Long
operator|.
name|toString
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|+
name|Integer
operator|.
name|toHexString
argument_list|(
name|scanner
operator|.
name|hashCode
argument_list|()
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
name|String
name|getID
parameter_list|()
block|{
return|return
name|id
return|;
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
name|rowI
operator|!=
literal|null
operator|&&
name|rowI
operator|.
name|hasNext
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|cached
operator|!=
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
try|try
block|{
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
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
name|cached
operator|=
name|result
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|UnknownScannerException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
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
return|return
name|cached
operator|!=
literal|null
return|;
block|}
specifier|public
name|KeyValue
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
name|KeyValue
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
name|boolean
name|loop
decl_stmt|;
do|do
block|{
name|loop
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|rowI
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|rowI
operator|.
name|hasNext
argument_list|()
condition|)
block|{
return|return
name|rowI
operator|.
name|next
argument_list|()
return|;
block|}
else|else
block|{
name|rowI
operator|=
literal|null
expr_stmt|;
block|}
block|}
if|if
condition|(
name|cached
operator|!=
literal|null
condition|)
block|{
name|rowI
operator|=
name|cached
operator|.
name|list
argument_list|()
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|loop
operator|=
literal|true
expr_stmt|;
name|cached
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|Result
name|result
init|=
literal|null
decl_stmt|;
try|try
block|{
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownScannerException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
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
name|rowI
operator|=
name|result
operator|.
name|list
argument_list|()
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|loop
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
do|while
condition|(
name|loop
condition|)
do|;
return|return
literal|null
return|;
block|}
specifier|public
name|void
name|putBack
parameter_list|(
name|KeyValue
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

