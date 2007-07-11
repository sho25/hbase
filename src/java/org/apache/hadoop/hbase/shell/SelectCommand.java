begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
operator|.
name|HClient
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
name|HRegionInfo
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
name|DataInputBuffer
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

begin_class
specifier|public
class|class
name|SelectCommand
extends|extends
name|BasicCommand
block|{
name|String
name|table
decl_stmt|;
name|int
name|limit
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|condition
decl_stmt|;
specifier|public
name|ReturnMsg
name|execute
parameter_list|(
name|HClient
name|client
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|condition
operator|!=
literal|null
operator|&&
name|this
operator|.
name|condition
operator|.
name|containsKey
argument_list|(
literal|"error"
argument_list|)
condition|)
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|0
argument_list|,
literal|"Syntax error : Please check 'Select' syntax."
argument_list|)
return|;
try|try
block|{
name|client
operator|.
name|openTable
argument_list|(
operator|new
name|Text
argument_list|(
name|this
operator|.
name|table
argument_list|)
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|getCondition
argument_list|()
condition|)
block|{
case|case
literal|0
case|:
name|HTableDescriptor
index|[]
name|tables
init|=
name|client
operator|.
name|listTables
argument_list|()
decl_stmt|;
name|Text
index|[]
name|columns
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|table
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|ROOT_TABLE_NAME
operator|.
name|toString
argument_list|()
argument_list|)
operator|||
name|this
operator|.
name|table
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|META_TABLE_NAME
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
name|columns
operator|=
name|HConstants
operator|.
name|COLUMN_FAMILY_ARRAY
expr_stmt|;
block|}
else|else
block|{
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
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|this
operator|.
name|table
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
name|families
argument_list|()
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
block|}
block|}
name|HScannerInterface
name|scan
init|=
name|client
operator|.
name|obtainScanner
argument_list|(
name|columns
argument_list|,
operator|new
name|Text
argument_list|(
literal|""
argument_list|)
argument_list|)
decl_stmt|;
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
name|ConsoleTable
operator|.
name|selectHead
argument_list|()
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
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
condition|)
block|{
name|Text
name|rowKey
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
name|byte
index|[]
name|value
init|=
name|results
operator|.
name|get
argument_list|(
name|columnKey
argument_list|)
decl_stmt|;
name|String
name|cellData
init|=
operator|new
name|String
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnKey
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
condition|)
block|{
name|DataInputBuffer
name|inbuf
init|=
operator|new
name|DataInputBuffer
argument_list|()
decl_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|()
decl_stmt|;
name|inbuf
operator|.
name|reset
argument_list|(
name|value
argument_list|,
name|value
operator|.
name|length
argument_list|)
expr_stmt|;
name|info
operator|.
name|readFields
argument_list|(
name|inbuf
argument_list|)
expr_stmt|;
name|cellData
operator|=
literal|"ID : "
operator|+
name|String
operator|.
name|valueOf
argument_list|(
name|info
operator|.
name|getRegionId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ConsoleTable
operator|.
name|printLine
argument_list|(
name|count
argument_list|,
name|rowKey
operator|.
name|toString
argument_list|()
argument_list|,
name|columnKey
operator|.
name|toString
argument_list|()
argument_list|,
name|cellData
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|results
operator|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|ConsoleTable
operator|.
name|selectFoot
argument_list|()
expr_stmt|;
name|scan
operator|.
name|close
argument_list|()
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|count
operator|=
literal|0
expr_stmt|;
name|ConsoleTable
operator|.
name|selectHead
argument_list|()
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
name|entry
range|:
name|client
operator|.
name|getRow
argument_list|(
operator|new
name|Text
argument_list|(
name|getRow
argument_list|()
argument_list|)
argument_list|)
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|byte
index|[]
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|String
name|cellData
init|=
operator|new
name|String
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
condition|)
block|{
name|DataInputBuffer
name|inbuf
init|=
operator|new
name|DataInputBuffer
argument_list|()
decl_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|()
decl_stmt|;
name|inbuf
operator|.
name|reset
argument_list|(
name|value
argument_list|,
name|value
operator|.
name|length
argument_list|)
expr_stmt|;
name|info
operator|.
name|readFields
argument_list|(
name|inbuf
argument_list|)
expr_stmt|;
name|cellData
operator|=
literal|"ID : "
operator|+
name|String
operator|.
name|valueOf
argument_list|(
name|info
operator|.
name|getRegionId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ConsoleTable
operator|.
name|printLine
argument_list|(
name|count
argument_list|,
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|cellData
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|ConsoleTable
operator|.
name|selectFoot
argument_list|()
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|Text
index|[]
name|column
init|=
operator|new
name|Text
index|[]
block|{
operator|new
name|Text
argument_list|(
name|getColumn
argument_list|()
argument_list|)
block|}
decl_stmt|;
name|HScannerInterface
name|scanner
init|=
name|client
operator|.
name|obtainScanner
argument_list|(
name|column
argument_list|,
operator|new
name|Text
argument_list|(
literal|""
argument_list|)
argument_list|)
decl_stmt|;
name|HStoreKey
name|k
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
name|r
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
name|ConsoleTable
operator|.
name|selectHead
argument_list|()
expr_stmt|;
name|count
operator|=
literal|0
expr_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|k
argument_list|,
name|r
argument_list|)
condition|)
block|{
name|Text
name|rowKey
init|=
name|k
operator|.
name|getRow
argument_list|()
decl_stmt|;
for|for
control|(
name|Text
name|columnKey
range|:
name|r
operator|.
name|keySet
argument_list|()
control|)
block|{
name|byte
index|[]
name|value
init|=
name|r
operator|.
name|get
argument_list|(
name|columnKey
argument_list|)
decl_stmt|;
name|String
name|cellData
init|=
operator|new
name|String
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|ConsoleTable
operator|.
name|printLine
argument_list|(
name|count
argument_list|,
name|rowKey
operator|.
name|toString
argument_list|()
argument_list|,
name|columnKey
operator|.
name|toString
argument_list|()
argument_list|,
name|cellData
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|results
operator|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|ConsoleTable
operator|.
name|selectFoot
argument_list|()
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|byte
index|[]
name|rs1
init|=
name|client
operator|.
name|get
argument_list|(
operator|new
name|Text
argument_list|(
name|getRow
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
name|getColumn
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ConsoleTable
operator|.
name|selectHead
argument_list|()
expr_stmt|;
name|ConsoleTable
operator|.
name|printLine
argument_list|(
literal|0
argument_list|,
name|getRow
argument_list|()
argument_list|,
name|getColumn
argument_list|()
argument_list|,
operator|new
name|String
argument_list|(
name|rs1
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|ConsoleTable
operator|.
name|selectFoot
argument_list|()
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|byte
index|[]
index|[]
name|rs2
init|=
name|client
operator|.
name|get
argument_list|(
operator|new
name|Text
argument_list|(
name|getRow
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
name|getColumn
argument_list|()
argument_list|)
argument_list|,
name|this
operator|.
name|limit
argument_list|)
decl_stmt|;
name|ConsoleTable
operator|.
name|selectHead
argument_list|()
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
name|rs2
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ConsoleTable
operator|.
name|printLine
argument_list|(
name|i
argument_list|,
name|getRow
argument_list|()
argument_list|,
name|getColumn
argument_list|()
argument_list|,
operator|new
name|String
argument_list|(
name|rs2
index|[
name|i
index|]
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ConsoleTable
operator|.
name|selectFoot
argument_list|()
expr_stmt|;
break|break;
case|case
literal|5
case|:
name|byte
index|[]
index|[]
name|rs3
init|=
name|client
operator|.
name|get
argument_list|(
operator|new
name|Text
argument_list|(
name|getRow
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
name|getColumn
argument_list|()
argument_list|)
argument_list|,
name|getTime
argument_list|()
argument_list|,
name|this
operator|.
name|limit
argument_list|)
decl_stmt|;
name|ConsoleTable
operator|.
name|selectHead
argument_list|()
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
name|rs3
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ConsoleTable
operator|.
name|printLine
argument_list|(
name|i
argument_list|,
name|getRow
argument_list|()
argument_list|,
name|getColumn
argument_list|()
argument_list|,
operator|new
name|String
argument_list|(
name|rs3
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ConsoleTable
operator|.
name|selectFoot
argument_list|()
expr_stmt|;
break|break;
block|}
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|1
argument_list|,
literal|"Successfully print out the selected data."
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
name|table
operator|=
name|table
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
name|setCondition
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|cond
parameter_list|)
block|{
name|this
operator|.
name|condition
operator|=
name|cond
expr_stmt|;
block|}
specifier|public
name|String
name|getRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|condition
operator|.
name|get
argument_list|(
literal|"row"
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
return|;
block|}
specifier|public
name|String
name|getColumn
parameter_list|()
block|{
return|return
name|this
operator|.
name|condition
operator|.
name|get
argument_list|(
literal|"column"
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
return|;
block|}
specifier|public
name|long
name|getTime
parameter_list|()
block|{
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|this
operator|.
name|condition
operator|.
name|get
argument_list|(
literal|"time"
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|int
name|getConditionSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|condition
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|int
name|getCondition
parameter_list|()
block|{
name|int
name|type
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|condition
operator|==
literal|null
condition|)
block|{
name|type
operator|=
literal|0
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|condition
operator|.
name|containsKey
argument_list|(
literal|"row"
argument_list|)
condition|)
block|{
if|if
condition|(
name|getConditionSize
argument_list|()
operator|==
literal|1
condition|)
block|{
name|type
operator|=
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|condition
operator|.
name|containsKey
argument_list|(
literal|"column"
argument_list|)
condition|)
block|{
if|if
condition|(
name|getConditionSize
argument_list|()
operator|==
literal|2
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|limit
operator|==
literal|0
condition|)
block|{
name|type
operator|=
literal|3
expr_stmt|;
block|}
else|else
block|{
name|type
operator|=
literal|4
expr_stmt|;
block|}
block|}
else|else
block|{
name|type
operator|=
literal|5
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|condition
operator|.
name|containsKey
argument_list|(
literal|"column"
argument_list|)
condition|)
block|{
name|type
operator|=
literal|2
expr_stmt|;
block|}
return|return
name|type
return|;
block|}
block|}
end_class

end_unit

