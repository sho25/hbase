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
name|HashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
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
name|io
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
name|io
operator|.
name|ObjectWritable
import|;
end_import

begin_comment
comment|/**  * Implementation of RowFilterInterface that represents a set of RowFilters  * which will be evaluated with a specified boolean operator MUST_PASS_ALL   * (!AND) or MUST_PASS_ONE (!OR).  Since you can use RowFilterSets as children   * of RowFilterSet, you can create a hierarchy of filters to be evaluated.  */
end_comment

begin_class
specifier|public
class|class
name|RowFilterSet
implements|implements
name|RowFilterInterface
block|{
comment|/** set operator */
specifier|public
specifier|static
enum|enum
name|Operator
block|{
comment|/** !AND */
name|MUST_PASS_ALL
block|,
comment|/** !OR */
name|MUST_PASS_ONE
block|}
specifier|private
name|Operator
name|operator
init|=
name|Operator
operator|.
name|MUST_PASS_ALL
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|RowFilterInterface
argument_list|>
name|filters
init|=
operator|new
name|HashSet
argument_list|<
name|RowFilterInterface
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Default constructor, filters nothing. Required though for RPC    * deserialization.    */
specifier|public
name|RowFilterSet
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor that takes a set of RowFilters. The default operator     * MUST_PASS_ALL is assumed.    *     * @param rowFilters    */
specifier|public
name|RowFilterSet
parameter_list|(
specifier|final
name|Set
argument_list|<
name|RowFilterInterface
argument_list|>
name|rowFilters
parameter_list|)
block|{
name|this
operator|.
name|filters
operator|=
name|rowFilters
expr_stmt|;
block|}
comment|/**    * Constructor that takes a set of RowFilters and an operator.    *     * @param operator Operator to process filter set with.    * @param rowFilters Set of row filters.    */
specifier|public
name|RowFilterSet
parameter_list|(
specifier|final
name|Operator
name|operator
parameter_list|,
specifier|final
name|Set
argument_list|<
name|RowFilterInterface
argument_list|>
name|rowFilters
parameter_list|)
block|{
name|this
operator|.
name|filters
operator|=
name|rowFilters
expr_stmt|;
name|this
operator|.
name|operator
operator|=
name|operator
expr_stmt|;
block|}
comment|/** Get the operator.    *     * @return operator    */
specifier|public
name|Operator
name|getOperator
parameter_list|()
block|{
return|return
name|operator
return|;
block|}
comment|/** Get the filters.    *     * @return filters    */
specifier|public
name|Set
argument_list|<
name|RowFilterInterface
argument_list|>
name|getFilters
parameter_list|()
block|{
return|return
name|filters
return|;
block|}
comment|/** Add a filter.    *     * @param filter    */
specifier|public
name|void
name|addFilter
parameter_list|(
name|RowFilterInterface
name|filter
parameter_list|)
block|{
name|this
operator|.
name|filters
operator|.
name|add
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|validate
parameter_list|(
specifier|final
name|byte
index|[]
index|[]
name|columns
parameter_list|)
block|{
for|for
control|(
name|RowFilterInterface
name|filter
range|:
name|filters
control|)
block|{
name|filter
operator|.
name|validate
argument_list|(
name|columns
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
for|for
control|(
name|RowFilterInterface
name|filter
range|:
name|filters
control|)
block|{
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|rowProcessed
parameter_list|(
name|boolean
name|filtered
parameter_list|,
name|byte
index|[]
name|rowKey
parameter_list|)
block|{
name|rowProcessed
argument_list|(
name|filtered
argument_list|,
name|rowKey
argument_list|,
literal|0
argument_list|,
name|rowKey
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|rowProcessed
parameter_list|(
name|boolean
name|filtered
parameter_list|,
name|byte
index|[]
name|key
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
for|for
control|(
name|RowFilterInterface
name|filter
range|:
name|filters
control|)
block|{
name|filter
operator|.
name|rowProcessed
argument_list|(
name|filtered
argument_list|,
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|processAlways
parameter_list|()
block|{
for|for
control|(
name|RowFilterInterface
name|filter
range|:
name|filters
control|)
block|{
if|if
condition|(
name|filter
operator|.
name|processAlways
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
name|boolean
name|result
init|=
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ONE
decl_stmt|;
for|for
control|(
name|RowFilterInterface
name|filter
range|:
name|filters
control|)
block|{
if|if
condition|(
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ALL
condition|)
block|{
if|if
condition|(
name|filter
operator|.
name|filterAllRemaining
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ONE
condition|)
block|{
if|if
condition|(
operator|!
name|filter
operator|.
name|filterAllRemaining
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
specifier|final
name|byte
index|[]
name|rowKey
parameter_list|)
block|{
return|return
name|filterRowKey
argument_list|(
name|rowKey
argument_list|,
literal|0
argument_list|,
name|rowKey
operator|.
name|length
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|boolean
name|resultFound
init|=
literal|false
decl_stmt|;
name|boolean
name|result
init|=
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ONE
decl_stmt|;
for|for
control|(
name|RowFilterInterface
name|filter
range|:
name|filters
control|)
block|{
if|if
condition|(
operator|!
name|resultFound
condition|)
block|{
if|if
condition|(
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ALL
condition|)
block|{
if|if
condition|(
name|filter
operator|.
name|filterAllRemaining
argument_list|()
operator|||
name|filter
operator|.
name|filterRowKey
argument_list|(
name|rowKey
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
condition|)
block|{
name|result
operator|=
literal|true
expr_stmt|;
name|resultFound
operator|=
literal|true
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ONE
condition|)
block|{
if|if
condition|(
operator|!
name|filter
operator|.
name|filterAllRemaining
argument_list|()
operator|&&
operator|!
name|filter
operator|.
name|filterRowKey
argument_list|(
name|rowKey
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
condition|)
block|{
name|result
operator|=
literal|false
expr_stmt|;
name|resultFound
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|filter
operator|.
name|processAlways
argument_list|()
condition|)
block|{
name|filter
operator|.
name|filterRowKey
argument_list|(
name|rowKey
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
name|boolean
name|filterColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|rowKey
parameter_list|,
specifier|final
name|byte
index|[]
name|colKey
parameter_list|,
specifier|final
name|byte
index|[]
name|data
parameter_list|)
block|{
return|return
name|filterColumn
argument_list|(
name|rowKey
argument_list|,
literal|0
argument_list|,
name|rowKey
operator|.
name|length
argument_list|,
name|colKey
argument_list|,
literal|0
argument_list|,
name|colKey
operator|.
name|length
argument_list|,
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|filterColumn
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|int
name|roffset
parameter_list|,
name|int
name|rlength
parameter_list|,
name|byte
index|[]
name|columnName
parameter_list|,
name|int
name|coffset
parameter_list|,
name|int
name|clength
parameter_list|,
name|byte
index|[]
name|columnValue
parameter_list|,
name|int
name|voffset
parameter_list|,
name|int
name|vlength
parameter_list|)
block|{
name|boolean
name|resultFound
init|=
literal|false
decl_stmt|;
name|boolean
name|result
init|=
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ONE
decl_stmt|;
for|for
control|(
name|RowFilterInterface
name|filter
range|:
name|filters
control|)
block|{
if|if
condition|(
operator|!
name|resultFound
condition|)
block|{
if|if
condition|(
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ALL
condition|)
block|{
if|if
condition|(
name|filter
operator|.
name|filterAllRemaining
argument_list|()
operator|||
name|filter
operator|.
name|filterColumn
argument_list|(
name|rowKey
argument_list|,
name|roffset
argument_list|,
name|rlength
argument_list|,
name|columnName
argument_list|,
name|coffset
argument_list|,
name|clength
argument_list|,
name|columnValue
argument_list|,
name|voffset
argument_list|,
name|vlength
argument_list|)
condition|)
block|{
name|result
operator|=
literal|true
expr_stmt|;
name|resultFound
operator|=
literal|true
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ONE
condition|)
block|{
if|if
condition|(
operator|!
name|filter
operator|.
name|filterAllRemaining
argument_list|()
operator|&&
operator|!
name|filter
operator|.
name|filterColumn
argument_list|(
name|rowKey
argument_list|,
name|roffset
argument_list|,
name|rlength
argument_list|,
name|columnName
argument_list|,
name|coffset
argument_list|,
name|clength
argument_list|,
name|columnValue
argument_list|,
name|voffset
argument_list|,
name|vlength
argument_list|)
condition|)
block|{
name|result
operator|=
literal|false
expr_stmt|;
name|resultFound
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|filter
operator|.
name|processAlways
argument_list|()
condition|)
block|{
name|filter
operator|.
name|filterColumn
argument_list|(
name|rowKey
argument_list|,
name|roffset
argument_list|,
name|rlength
argument_list|,
name|columnName
argument_list|,
name|coffset
argument_list|,
name|clength
argument_list|,
name|columnValue
argument_list|,
name|voffset
argument_list|,
name|vlength
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
name|boolean
name|filterRow
parameter_list|(
specifier|final
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|columns
parameter_list|)
block|{
name|boolean
name|resultFound
init|=
literal|false
decl_stmt|;
name|boolean
name|result
init|=
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ONE
decl_stmt|;
for|for
control|(
name|RowFilterInterface
name|filter
range|:
name|filters
control|)
block|{
if|if
condition|(
operator|!
name|resultFound
condition|)
block|{
if|if
condition|(
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ALL
condition|)
block|{
if|if
condition|(
name|filter
operator|.
name|filterAllRemaining
argument_list|()
operator|||
name|filter
operator|.
name|filterRow
argument_list|(
name|columns
argument_list|)
condition|)
block|{
name|result
operator|=
literal|true
expr_stmt|;
name|resultFound
operator|=
literal|true
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ONE
condition|)
block|{
if|if
condition|(
operator|!
name|filter
operator|.
name|filterAllRemaining
argument_list|()
operator|&&
operator|!
name|filter
operator|.
name|filterRow
argument_list|(
name|columns
argument_list|)
condition|)
block|{
name|result
operator|=
literal|false
expr_stmt|;
name|resultFound
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|filter
operator|.
name|processAlways
argument_list|()
condition|)
block|{
name|filter
operator|.
name|filterRow
argument_list|(
name|columns
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
name|boolean
name|filterRow
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
parameter_list|)
block|{
if|if
condition|(
literal|true
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not Yet Implemented"
argument_list|)
throw|;
return|return
literal|false
return|;
block|}
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
name|Configuration
name|conf
init|=
operator|new
name|HBaseConfiguration
argument_list|()
decl_stmt|;
name|byte
name|opByte
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
name|operator
operator|=
name|Operator
operator|.
name|values
argument_list|()
index|[
name|opByte
index|]
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|filters
operator|=
operator|new
name|HashSet
argument_list|<
name|RowFilterInterface
argument_list|>
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
name|RowFilterInterface
name|filter
init|=
operator|(
name|RowFilterInterface
operator|)
name|ObjectWritable
operator|.
name|readObject
argument_list|(
name|in
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|filters
operator|.
name|add
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
name|Configuration
name|conf
init|=
operator|new
name|HBaseConfiguration
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|operator
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|filters
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|RowFilterInterface
name|filter
range|:
name|filters
control|)
block|{
name|ObjectWritable
operator|.
name|writeObject
argument_list|(
name|out
argument_list|,
name|filter
argument_list|,
name|RowFilterInterface
operator|.
name|class
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

