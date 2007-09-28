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
name|Arrays
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
name|TreeMap
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
name|HConstants
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
name|HScannerInterface
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
name|HStoreKey
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
name|shell
operator|.
name|generated
operator|.
name|ParseException
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
name|shell
operator|.
name|generated
operator|.
name|Parser
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
name|Writables
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
comment|/**  * Selects values from tables.  *   * TODO: INTO FILE is not yet implemented.  */
end_comment

begin_class
specifier|public
class|class
name|SelectCommand
extends|extends
name|BasicCommand
block|{
specifier|private
name|Text
name|tableName
decl_stmt|;
specifier|private
name|Text
name|rowKey
init|=
operator|new
name|Text
argument_list|(
literal|""
argument_list|)
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|columns
decl_stmt|;
specifier|private
name|long
name|timestamp
decl_stmt|;
specifier|private
name|int
name|limit
decl_stmt|;
comment|// Count of versions to return.
specifier|private
name|int
name|version
decl_stmt|;
specifier|private
name|boolean
name|whereClause
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|HEADER_ROW_CELL
init|=
operator|new
name|String
index|[]
block|{
literal|"Row"
block|,
literal|"Cell"
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|HEADER_COLUMN_CELL
init|=
operator|new
name|String
index|[]
block|{
literal|"Column"
block|,
literal|"Cell"
block|}
decl_stmt|;
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
literal|"Row"
block|,
literal|"Column"
block|,
literal|"Cell"
block|}
decl_stmt|;
specifier|public
name|ReturnMsg
name|execute
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|tableName
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
operator|||
name|this
operator|.
name|rowKey
operator|==
literal|null
operator|||
name|this
operator|.
name|columns
operator|.
name|size
argument_list|()
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
literal|"Syntax error : Please check 'Select' syntax."
argument_list|)
return|;
block|}
try|try
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|this
operator|.
name|tableName
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
name|int
name|count
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|whereClause
condition|)
block|{
name|count
operator|=
name|compoundWherePrint
argument_list|(
name|table
argument_list|,
name|admin
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|count
operator|=
name|scanPrint
argument_list|(
name|table
argument_list|,
name|admin
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|1
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|count
argument_list|)
operator|+
literal|" row(s) in set"
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
literal|"[,]"
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
specifier|private
name|int
name|compoundWherePrint
parameter_list|(
name|HTable
name|table
parameter_list|,
name|HBaseAdmin
name|admin
parameter_list|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
name|TableFormatter
name|formatter
init|=
name|TableFormatterFactory
operator|.
name|get
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|this
operator|.
name|version
operator|!=
literal|0
condition|)
block|{
comment|// A number of versions has been specified.
name|byte
index|[]
index|[]
name|result
init|=
literal|null
decl_stmt|;
name|ParsedColumns
name|parsedColumns
init|=
name|getColumns
argument_list|(
name|admin
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|boolean
name|multiple
init|=
name|parsedColumns
operator|.
name|isMultiple
argument_list|()
operator|||
name|this
operator|.
name|version
operator|>
literal|1
decl_stmt|;
name|formatter
operator|.
name|header
argument_list|(
name|multiple
condition|?
name|HEADER_COLUMN_CELL
else|:
literal|null
argument_list|)
expr_stmt|;
for|for
control|(
name|Text
name|column
range|:
name|parsedColumns
operator|.
name|getColumns
argument_list|()
control|)
block|{
if|if
condition|(
name|this
operator|.
name|timestamp
operator|!=
literal|0
condition|)
block|{
name|result
operator|=
name|table
operator|.
name|get
argument_list|(
name|this
operator|.
name|rowKey
argument_list|,
name|column
argument_list|,
name|this
operator|.
name|timestamp
argument_list|,
name|this
operator|.
name|version
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
name|table
operator|.
name|get
argument_list|(
name|this
operator|.
name|rowKey
argument_list|,
name|column
argument_list|,
name|this
operator|.
name|version
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|result
operator|!=
literal|null
operator|&&
name|ii
operator|<
name|result
operator|.
name|length
condition|;
name|ii
operator|++
control|)
block|{
if|if
condition|(
name|multiple
condition|)
block|{
name|formatter
operator|.
name|row
argument_list|(
operator|new
name|String
index|[]
block|{
name|column
operator|.
name|toString
argument_list|()
block|,
name|toString
argument_list|(
name|column
argument_list|,
name|result
index|[
name|ii
index|]
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|formatter
operator|.
name|row
argument_list|(
operator|new
name|String
index|[]
block|{
name|toString
argument_list|(
name|column
argument_list|,
name|result
index|[
name|ii
index|]
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|formatter
operator|.
name|header
argument_list|(
name|isMultiple
argument_list|()
condition|?
name|HEADER_COLUMN_CELL
else|:
literal|null
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|e
range|:
name|table
operator|.
name|getRow
argument_list|(
name|this
operator|.
name|rowKey
argument_list|)
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Text
name|key
init|=
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|columns
operator|.
name|contains
argument_list|(
name|key
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|String
name|cellData
init|=
name|toString
argument_list|(
name|key
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|isMultiple
argument_list|()
condition|)
block|{
name|formatter
operator|.
name|row
argument_list|(
operator|new
name|String
index|[]
block|{
name|key
operator|.
name|toString
argument_list|()
block|,
name|cellData
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|formatter
operator|.
name|row
argument_list|(
operator|new
name|String
index|[]
block|{
name|cellData
block|}
argument_list|)
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
block|}
block|}
name|formatter
operator|.
name|footer
argument_list|()
expr_stmt|;
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
name|count
return|;
block|}
specifier|private
name|String
name|toString
parameter_list|(
specifier|final
name|Text
name|columnName
parameter_list|,
specifier|final
name|byte
index|[]
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|result
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|columnName
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
operator|||
name|columnName
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|COL_SPLITA
argument_list|)
operator|||
name|columnName
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|COL_SPLITA
argument_list|)
condition|)
block|{
name|result
operator|=
name|Writables
operator|.
name|getHRegionInfoOrNull
argument_list|(
name|cell
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|columnName
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|COL_STARTCODE
argument_list|)
condition|)
block|{
name|result
operator|=
name|Long
operator|.
name|toString
argument_list|(
name|Writables
operator|.
name|bytesToLong
argument_list|(
name|cell
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
name|Writables
operator|.
name|bytesToString
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Data structure with columns to use scanning and whether or not the    * scan could return more than one column.    */
class|class
name|ParsedColumns
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|Text
argument_list|>
name|cols
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isMultiple
decl_stmt|;
name|ParsedColumns
parameter_list|(
specifier|final
name|List
argument_list|<
name|Text
argument_list|>
name|columns
parameter_list|)
block|{
name|this
argument_list|(
name|columns
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|ParsedColumns
parameter_list|(
specifier|final
name|List
argument_list|<
name|Text
argument_list|>
name|columns
parameter_list|,
specifier|final
name|boolean
name|isMultiple
parameter_list|)
block|{
name|this
operator|.
name|cols
operator|=
name|columns
expr_stmt|;
name|this
operator|.
name|isMultiple
operator|=
name|isMultiple
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Text
argument_list|>
name|getColumns
parameter_list|()
block|{
return|return
name|this
operator|.
name|cols
return|;
block|}
specifier|public
name|boolean
name|isMultiple
parameter_list|()
block|{
return|return
name|this
operator|.
name|isMultiple
return|;
block|}
block|}
specifier|private
name|int
name|scanPrint
parameter_list|(
name|HTable
name|table
parameter_list|,
name|HBaseAdmin
name|admin
parameter_list|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
name|HScannerInterface
name|scan
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ParsedColumns
name|parsedColumns
init|=
name|getColumns
argument_list|(
name|admin
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Text
index|[]
name|cols
init|=
name|parsedColumns
operator|.
name|getColumns
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|Text
index|[]
block|{}
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|timestamp
operator|==
literal|0
condition|)
block|{
name|scan
operator|=
name|table
operator|.
name|obtainScanner
argument_list|(
name|cols
argument_list|,
name|this
operator|.
name|rowKey
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scan
operator|=
name|table
operator|.
name|obtainScanner
argument_list|(
name|cols
argument_list|,
name|this
operator|.
name|rowKey
argument_list|,
name|this
operator|.
name|timestamp
argument_list|)
expr_stmt|;
block|}
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|TableFormatter
name|formatter
init|=
name|TableFormatterFactory
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// If only one column in query, then don't print out the column.
name|formatter
operator|.
name|header
argument_list|(
operator|(
name|parsedColumns
operator|.
name|isMultiple
argument_list|()
operator|)
condition|?
name|HEADER
else|:
name|HEADER_ROW_CELL
argument_list|)
expr_stmt|;
while|while
condition|(
name|scan
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|results
argument_list|)
operator|&&
name|checkLimit
argument_list|(
name|count
argument_list|)
condition|)
block|{
name|Text
name|r
init|=
name|key
operator|.
name|getRow
argument_list|()
decl_stmt|;
for|for
control|(
name|Text
name|columnKey
range|:
name|results
operator|.
name|keySet
argument_list|()
control|)
block|{
name|String
name|cellData
init|=
name|toString
argument_list|(
name|columnKey
argument_list|,
name|results
operator|.
name|get
argument_list|(
name|columnKey
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|parsedColumns
operator|.
name|isMultiple
argument_list|()
condition|)
block|{
name|formatter
operator|.
name|row
argument_list|(
operator|new
name|String
index|[]
block|{
name|r
operator|.
name|toString
argument_list|()
block|,
name|columnKey
operator|.
name|toString
argument_list|()
block|,
name|cellData
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Don't print out the column since only one specified in query.
name|formatter
operator|.
name|row
argument_list|(
operator|new
name|String
index|[]
block|{
name|r
operator|.
name|toString
argument_list|()
block|,
name|cellData
block|}
argument_list|)
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|limit
operator|>
literal|0
operator|&&
name|count
operator|>=
name|this
operator|.
name|limit
condition|)
block|{
break|break;
block|}
block|}
block|}
name|formatter
operator|.
name|footer
argument_list|()
expr_stmt|;
name|scan
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|count
return|;
block|}
comment|/**    * Make sense of the supplied list of columns.    * @param admin Admin to use.    * @return Interpretation of supplied list of columns.    */
specifier|public
name|ParsedColumns
name|getColumns
parameter_list|(
specifier|final
name|HBaseAdmin
name|admin
parameter_list|,
specifier|final
name|boolean
name|scanning
parameter_list|)
block|{
name|ParsedColumns
name|result
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|this
operator|.
name|columns
operator|.
name|contains
argument_list|(
literal|"*"
argument_list|)
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|tableName
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|ROOT_TABLE_NAME
argument_list|)
operator|||
name|this
operator|.
name|tableName
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
condition|)
block|{
name|result
operator|=
operator|new
name|ParsedColumns
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
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
name|result
operator|=
operator|new
name|ParsedColumns
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Text
argument_list|>
argument_list|(
name|tables
index|[
name|i
index|]
operator|.
name|families
argument_list|()
operator|.
name|keySet
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
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
name|columns
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
comment|// Add '$' to column name if we are scanning.  Scanners support
comment|// regex column names.  Adding '$', the column becomes a
comment|// regex that does an explicit match on the supplied column name.
comment|// Otherwise, if the specified column is a column family, then
comment|// default behavior is to fetch all columns that have a matching
comment|// column family.
name|column
operator|=
operator|(
name|this
operator|.
name|columns
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
operator|)
condition|?
operator|new
name|Text
argument_list|(
name|this
operator|.
name|columns
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
operator|(
name|scanning
condition|?
literal|"$"
else|:
literal|""
operator|)
argument_list|)
else|:
operator|new
name|Text
argument_list|(
name|this
operator|.
name|columns
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|":"
operator|+
operator|(
name|scanning
condition|?
literal|"$"
else|:
literal|""
operator|)
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
name|result
operator|=
operator|new
name|ParsedColumns
argument_list|(
name|tmpList
argument_list|,
name|tmpList
operator|.
name|size
argument_list|()
operator|>
literal|1
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
name|result
return|;
block|}
comment|/*    * @return True if query contains multiple columns.    */
specifier|private
name|boolean
name|isMultiple
parameter_list|()
block|{
return|return
name|this
operator|.
name|columns
operator|.
name|size
argument_list|()
operator|>
literal|1
operator|||
name|this
operator|.
name|columns
operator|.
name|contains
argument_list|(
literal|"*"
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|checkLimit
parameter_list|(
name|int
name|count
parameter_list|)
block|{
return|return
operator|(
name|this
operator|.
name|limit
operator|==
literal|0
operator|)
condition|?
literal|true
else|:
operator|(
name|this
operator|.
name|limit
operator|>
name|count
operator|)
condition|?
literal|true
else|:
literal|false
return|;
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
operator|new
name|Text
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setLimit
parameter_list|(
name|int
name|limit
parameter_list|)
block|{
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
block|}
specifier|public
name|void
name|setWhere
parameter_list|(
name|boolean
name|isWhereClause
parameter_list|)
block|{
if|if
condition|(
name|isWhereClause
condition|)
name|this
operator|.
name|whereClause
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|setTimestamp
parameter_list|(
name|String
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|timestamp
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setColumns
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|)
block|{
name|this
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
block|}
specifier|public
name|void
name|setRowKey
parameter_list|(
name|String
name|rowKey
parameter_list|)
block|{
if|if
condition|(
name|rowKey
operator|==
literal|null
condition|)
name|this
operator|.
name|rowKey
operator|=
literal|null
expr_stmt|;
else|else
name|this
operator|.
name|rowKey
operator|=
operator|new
name|Text
argument_list|(
name|rowKey
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param version Set maximum versions for this selection    */
specifier|public
name|void
name|setVersion
parameter_list|(
name|int
name|version
parameter_list|)
block|{
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|ParseException
block|{
comment|// For debugging
name|Parser
name|parser
init|=
operator|new
name|Parser
argument_list|(
literal|"select * from -ROOT-;"
argument_list|)
decl_stmt|;
name|Command
name|cmd
init|=
name|parser
operator|.
name|terminatedCommand
argument_list|()
decl_stmt|;
name|ReturnMsg
name|rm
init|=
name|cmd
operator|.
name|execute
argument_list|(
operator|new
name|HBaseConfiguration
argument_list|()
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

