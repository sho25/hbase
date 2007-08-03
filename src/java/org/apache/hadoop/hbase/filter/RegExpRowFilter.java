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
name|filter
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

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
name|Arrays
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
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * Implementation of RowFilterInterface that can filter by rowkey regular  * expression and/or individual column values (equals comparison only).  * Multiple column filters imply an implicit conjunction of filter criteria.  */
end_comment

begin_class
specifier|public
class|class
name|RegExpRowFilter
implements|implements
name|RowFilterInterface
block|{
specifier|private
name|Pattern
name|rowKeyPattern
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|rowKeyRegExp
init|=
literal|null
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|equalsMap
init|=
operator|new
name|HashMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|Text
argument_list|>
name|nullColumns
init|=
operator|new
name|HashSet
argument_list|<
name|Text
argument_list|>
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RegExpRowFilter
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Default constructor, filters nothing. Required though for RPC    * deserialization.    */
specifier|public
name|RegExpRowFilter
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor that takes a row key regular expression to filter on.    *     * @param rowKeyRegExp    */
specifier|public
name|RegExpRowFilter
parameter_list|(
specifier|final
name|String
name|rowKeyRegExp
parameter_list|)
block|{
name|this
operator|.
name|rowKeyRegExp
operator|=
name|rowKeyRegExp
expr_stmt|;
block|}
comment|/**    * Constructor that takes a row key regular expression to filter on.    *     * @param rowKeyRegExp    * @param columnFilter    */
specifier|public
name|RegExpRowFilter
parameter_list|(
specifier|final
name|String
name|rowKeyRegExp
parameter_list|,
specifier|final
name|Map
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|columnFilter
parameter_list|)
block|{
name|this
operator|.
name|rowKeyRegExp
operator|=
name|rowKeyRegExp
expr_stmt|;
name|this
operator|.
name|setColumnFilters
argument_list|(
name|columnFilter
argument_list|)
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|public
name|void
name|rowProcessed
parameter_list|(
name|boolean
name|filtered
parameter_list|,
name|Text
name|rowKey
parameter_list|)
block|{
comment|//doesn't care
block|}
comment|/** {@inheritDoc} */
specifier|public
name|boolean
name|processAlways
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * Specify a value that must be matched for the given column.    *     * @param colKey    *          the column to match on    * @param value    *          the value that must equal the stored value.    */
specifier|public
name|void
name|setColumnFilter
parameter_list|(
specifier|final
name|Text
name|colKey
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|nullColumns
operator|.
name|add
argument_list|(
name|colKey
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|equalsMap
operator|.
name|put
argument_list|(
name|colKey
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Set column filters for a number of columns.    *     * @param columnFilter    *          Map of columns with value criteria.    */
specifier|public
name|void
name|setColumnFilters
parameter_list|(
specifier|final
name|Map
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|columnFilter
parameter_list|)
block|{
if|if
condition|(
literal|null
operator|==
name|columnFilter
condition|)
block|{
name|nullColumns
operator|.
name|clear
argument_list|()
expr_stmt|;
name|equalsMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|Entry
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|entry
range|:
name|columnFilter
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|setColumnFilter
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    *     * {@inheritDoc}    */
specifier|public
name|void
name|reset
parameter_list|()
block|{
comment|// Nothing to reset
block|}
comment|/**    *     * {@inheritDoc}    */
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    *     * {@inheritDoc}    */
specifier|public
name|boolean
name|filter
parameter_list|(
specifier|final
name|Text
name|rowKey
parameter_list|)
block|{
if|if
condition|(
name|filtersByRowKey
argument_list|()
operator|&&
name|rowKey
operator|!=
literal|null
condition|)
block|{
name|boolean
name|result
init|=
operator|!
name|getRowKeyPattern
argument_list|()
operator|.
name|matcher
argument_list|(
name|rowKey
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|matches
argument_list|()
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"filter returning "
operator|+
name|result
operator|+
literal|" for rowKey: "
operator|+
name|rowKey
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    *     * {@inheritDoc}    */
specifier|public
name|boolean
name|filter
parameter_list|(
specifier|final
name|Text
name|rowKey
parameter_list|,
specifier|final
name|Text
name|colKey
parameter_list|,
specifier|final
name|byte
index|[]
name|data
parameter_list|)
block|{
if|if
condition|(
name|filter
argument_list|(
name|rowKey
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|filtersByColumnValue
argument_list|()
condition|)
block|{
name|byte
index|[]
name|filterValue
init|=
name|equalsMap
operator|.
name|get
argument_list|(
name|colKey
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|filterValue
condition|)
block|{
name|boolean
name|result
init|=
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|filterValue
argument_list|,
name|data
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"filter returning "
operator|+
name|result
operator|+
literal|" for rowKey: "
operator|+
name|rowKey
operator|+
literal|" colKey: "
operator|+
name|colKey
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
if|if
condition|(
name|nullColumns
operator|.
name|contains
argument_list|(
name|colKey
argument_list|)
condition|)
block|{
if|if
condition|(
name|data
operator|!=
literal|null
operator|&&
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|DELETE_BYTES
operator|.
name|get
argument_list|()
argument_list|,
name|data
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"filter returning true for rowKey: "
operator|+
name|rowKey
operator|+
literal|" colKey: "
operator|+
name|colKey
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"filter returning false for rowKey: "
operator|+
name|rowKey
operator|+
literal|" colKey: "
operator|+
name|colKey
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    *     * {@inheritDoc}    */
specifier|public
name|boolean
name|filterNotNull
parameter_list|(
specifier|final
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|columns
parameter_list|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|col
range|:
name|columns
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|nullColumns
operator|.
name|contains
argument_list|(
name|col
operator|.
name|getKey
argument_list|()
argument_list|)
operator|&&
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|DELETE_BYTES
operator|.
name|get
argument_list|()
argument_list|,
name|col
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"filterNotNull returning true for colKey: "
operator|+
name|col
operator|.
name|getKey
argument_list|()
operator|+
literal|", column should be null."
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
for|for
control|(
name|Text
name|col
range|:
name|equalsMap
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|columns
operator|.
name|containsKey
argument_list|(
name|col
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"filterNotNull returning true for colKey: "
operator|+
name|col
operator|+
literal|", column not found in given TreeMap<Text, byte[]>."
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"filterNotNull returning false."
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|filtersByColumnValue
parameter_list|()
block|{
return|return
name|equalsMap
operator|!=
literal|null
operator|&&
name|equalsMap
operator|.
name|size
argument_list|()
operator|>
literal|0
return|;
block|}
specifier|private
name|boolean
name|filtersByRowKey
parameter_list|()
block|{
return|return
literal|null
operator|!=
name|rowKeyPattern
operator|||
literal|null
operator|!=
name|rowKeyRegExp
return|;
block|}
specifier|private
name|String
name|getRowKeyRegExp
parameter_list|()
block|{
if|if
condition|(
literal|null
operator|==
name|rowKeyRegExp
operator|&&
name|rowKeyPattern
operator|!=
literal|null
condition|)
block|{
name|rowKeyRegExp
operator|=
name|rowKeyPattern
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
return|return
name|rowKeyRegExp
return|;
block|}
specifier|private
name|Pattern
name|getRowKeyPattern
parameter_list|()
block|{
if|if
condition|(
name|rowKeyPattern
operator|==
literal|null
operator|&&
name|rowKeyRegExp
operator|!=
literal|null
condition|)
block|{
name|rowKeyPattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|rowKeyRegExp
argument_list|)
expr_stmt|;
block|}
return|return
name|rowKeyPattern
return|;
block|}
comment|/**    *     * {@inheritDoc}    */
specifier|public
name|void
name|readFields
parameter_list|(
specifier|final
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|hasRowKeyPattern
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|hasRowKeyPattern
condition|)
block|{
name|rowKeyRegExp
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
comment|// equals map
name|equalsMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readInt
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|Text
name|key
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|key
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|len
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|value
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|len
operator|>=
literal|0
condition|)
block|{
name|value
operator|=
operator|new
name|byte
index|[
name|len
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|setColumnFilter
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|// nullColumns
name|nullColumns
operator|.
name|clear
argument_list|()
expr_stmt|;
name|size
operator|=
name|in
operator|.
name|readInt
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|Text
name|key
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|key
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|setColumnFilter
argument_list|(
name|key
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    *     * {@inheritDoc}    */
specifier|public
name|void
name|validate
parameter_list|(
specifier|final
name|Text
index|[]
name|columns
parameter_list|)
block|{
name|Set
argument_list|<
name|Text
argument_list|>
name|invalids
init|=
operator|new
name|HashSet
argument_list|<
name|Text
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Text
name|colKey
range|:
name|getFilterColumns
argument_list|()
control|)
block|{
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Text
name|col
range|:
name|columns
control|)
block|{
if|if
condition|(
name|col
operator|.
name|equals
argument_list|(
name|colKey
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|found
condition|)
block|{
name|invalids
operator|.
name|add
argument_list|(
name|colKey
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|invalids
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|InvalidRowFilterException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"RowFilter contains criteria on columns %s not in %s"
argument_list|,
name|invalids
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|columns
argument_list|)
argument_list|)
argument_list|)
throw|;
block|}
block|}
specifier|private
name|Set
argument_list|<
name|Text
argument_list|>
name|getFilterColumns
parameter_list|()
block|{
name|Set
argument_list|<
name|Text
argument_list|>
name|cols
init|=
operator|new
name|HashSet
argument_list|<
name|Text
argument_list|>
argument_list|()
decl_stmt|;
name|cols
operator|.
name|addAll
argument_list|(
name|equalsMap
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|cols
operator|.
name|addAll
argument_list|(
name|nullColumns
argument_list|)
expr_stmt|;
return|return
name|cols
return|;
block|}
comment|/**    *     * {@inheritDoc}    */
specifier|public
name|void
name|write
parameter_list|(
specifier|final
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|filtersByRowKey
argument_list|()
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|getRowKeyRegExp
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// equalsMap
name|out
operator|.
name|writeInt
argument_list|(
name|equalsMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|entry
range|:
name|equalsMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|byte
index|[]
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|value
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
comment|// null columns
name|out
operator|.
name|writeInt
argument_list|(
name|nullColumns
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Text
name|col
range|:
name|nullColumns
control|)
block|{
name|col
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

