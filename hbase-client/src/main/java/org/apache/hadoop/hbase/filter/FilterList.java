begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|classification
operator|.
name|InterfaceStability
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
name|KeyValueUtil
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
name|exceptions
operator|.
name|DeserializationException
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|FilterProtos
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * Implementation of {@link Filter} that represents an ordered List of Filters  * which will be evaluated with a specified boolean operator {@link Operator#MUST_PASS_ALL}  * (<code>AND</code>) or {@link Operator#MUST_PASS_ONE} (<code>OR</code>).  * Since you can use Filter Lists as children of Filter Lists, you can create a  * hierarchy of filters to be evaluated.  *  *<br/>  * {@link Operator#MUST_PASS_ALL} evaluates lazily: evaluation stops as soon as one filter does  * not include the KeyValue.  *  *<br/>  * {@link Operator#MUST_PASS_ONE} evaluates non-lazily: all filters are always evaluated.  *  *<br/>  * Defaults to {@link Operator#MUST_PASS_ALL}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|final
specifier|public
class|class
name|FilterList
extends|extends
name|Filter
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
specifier|static
specifier|final
name|int
name|MAX_LOG_FILTERS
init|=
literal|5
decl_stmt|;
specifier|private
name|Operator
name|operator
init|=
name|Operator
operator|.
name|MUST_PASS_ALL
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Filter
argument_list|>
name|filters
init|=
operator|new
name|ArrayList
argument_list|<
name|Filter
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Filter
name|seekHintFilter
init|=
literal|null
decl_stmt|;
comment|/** Reference Cell used by {@link #transformCell(Cell)} for validation purpose. */
specifier|private
name|Cell
name|referenceKV
init|=
literal|null
decl_stmt|;
comment|/**    * When filtering a given Cell in {@link #filterKeyValue(Cell)},    * this stores the transformed Cell to be returned by {@link #transformCell(Cell)}.    *    * Individual filters transformation are applied only when the filter includes the Cell.    * Transformations are composed in the order specified by {@link #filters}.    */
specifier|private
name|Cell
name|transformedKV
init|=
literal|null
decl_stmt|;
comment|/**    * Constructor that takes a set of {@link Filter}s. The default operator    * MUST_PASS_ALL is assumed.    *    * @param rowFilters list of filters    */
specifier|public
name|FilterList
parameter_list|(
specifier|final
name|List
argument_list|<
name|Filter
argument_list|>
name|rowFilters
parameter_list|)
block|{
if|if
condition|(
name|rowFilters
operator|instanceof
name|ArrayList
condition|)
block|{
name|this
operator|.
name|filters
operator|=
name|rowFilters
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|filters
operator|=
operator|new
name|ArrayList
argument_list|<
name|Filter
argument_list|>
argument_list|(
name|rowFilters
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Constructor that takes a var arg number of {@link Filter}s. The fefault operator    * MUST_PASS_ALL is assumed.    * @param rowFilters    */
specifier|public
name|FilterList
parameter_list|(
specifier|final
name|Filter
modifier|...
name|rowFilters
parameter_list|)
block|{
name|this
operator|.
name|filters
operator|=
operator|new
name|ArrayList
argument_list|<
name|Filter
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|rowFilters
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor that takes an operator.    *    * @param operator Operator to process filter set with.    */
specifier|public
name|FilterList
parameter_list|(
specifier|final
name|Operator
name|operator
parameter_list|)
block|{
name|this
operator|.
name|operator
operator|=
name|operator
expr_stmt|;
block|}
comment|/**    * Constructor that takes a set of {@link Filter}s and an operator.    *    * @param operator Operator to process filter set with.    * @param rowFilters Set of row filters.    */
specifier|public
name|FilterList
parameter_list|(
specifier|final
name|Operator
name|operator
parameter_list|,
specifier|final
name|List
argument_list|<
name|Filter
argument_list|>
name|rowFilters
parameter_list|)
block|{
name|this
operator|.
name|filters
operator|=
operator|new
name|ArrayList
argument_list|<
name|Filter
argument_list|>
argument_list|(
name|rowFilters
argument_list|)
expr_stmt|;
name|this
operator|.
name|operator
operator|=
name|operator
expr_stmt|;
block|}
comment|/**    * Constructor that takes a var arg number of {@link Filter}s and an operator.    *    * @param operator Operator to process filter set with.    * @param rowFilters Filters to use    */
specifier|public
name|FilterList
parameter_list|(
specifier|final
name|Operator
name|operator
parameter_list|,
specifier|final
name|Filter
modifier|...
name|rowFilters
parameter_list|)
block|{
name|this
operator|.
name|filters
operator|=
operator|new
name|ArrayList
argument_list|<
name|Filter
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|rowFilters
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|operator
operator|=
name|operator
expr_stmt|;
block|}
comment|/**    * Get the operator.    *    * @return operator    */
specifier|public
name|Operator
name|getOperator
parameter_list|()
block|{
return|return
name|operator
return|;
block|}
comment|/**    * Get the filters.    *    * @return filters    */
specifier|public
name|List
argument_list|<
name|Filter
argument_list|>
name|getFilters
parameter_list|()
block|{
return|return
name|filters
return|;
block|}
comment|/**    * Add a filter.    *    * @param filter another filter    */
specifier|public
name|void
name|addFilter
parameter_list|(
name|Filter
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
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|Filter
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
name|seekHintFilter
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
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
throws|throws
name|IOException
block|{
name|boolean
name|flag
init|=
operator|(
name|this
operator|.
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ONE
operator|)
condition|?
literal|true
else|:
literal|false
decl_stmt|;
for|for
control|(
name|Filter
name|filter
range|:
name|filters
control|)
block|{
if|if
condition|(
name|this
operator|.
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
name|flag
operator|=
literal|true
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|this
operator|.
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
name|flag
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
return|return
name|flag
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|Filter
name|filter
range|:
name|filters
control|)
block|{
if|if
condition|(
name|filter
operator|.
name|filterAllRemaining
argument_list|()
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
return|return
literal|true
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ONE
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ONE
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|transformCell
parameter_list|(
name|Cell
name|v
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|transform
argument_list|(
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|v
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * WARNING: please to not override this method.  Instead override {@link #transformCell(Cell)}.    *    * When removing this, its body should be placed in transformCell.    *    * This is for transition from 0.94 -> 0.96    */
annotation|@
name|Deprecated
annotation|@
name|Override
specifier|public
name|KeyValue
name|transform
parameter_list|(
name|KeyValue
name|v
parameter_list|)
throws|throws
name|IOException
block|{
comment|// transform() is expected to follow an inclusive filterKeyValue() immediately:
if|if
condition|(
operator|!
name|v
operator|.
name|equals
argument_list|(
name|this
operator|.
name|referenceKV
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Reference Cell: "
operator|+
name|this
operator|.
name|referenceKV
operator|+
literal|" does not match: "
operator|+
name|v
argument_list|)
throw|;
block|}
return|return
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|this
operator|.
name|transformedKV
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|Cell
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|referenceKV
operator|=
name|v
expr_stmt|;
comment|// Accumulates successive transformation of every filter that includes the Cell:
name|Cell
name|transformed
init|=
name|v
decl_stmt|;
name|ReturnCode
name|rc
init|=
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ONE
condition|?
name|ReturnCode
operator|.
name|SKIP
else|:
name|ReturnCode
operator|.
name|INCLUDE
decl_stmt|;
for|for
control|(
name|Filter
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
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
name|ReturnCode
name|code
init|=
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|v
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|code
condition|)
block|{
comment|// Override INCLUDE and continue to evaluate.
case|case
name|INCLUDE_AND_NEXT_COL
case|:
name|rc
operator|=
name|ReturnCode
operator|.
name|INCLUDE_AND_NEXT_COL
expr_stmt|;
case|case
name|INCLUDE
case|:
name|transformed
operator|=
name|filter
operator|.
name|transformCell
argument_list|(
name|transformed
argument_list|)
expr_stmt|;
continue|continue;
case|case
name|SEEK_NEXT_USING_HINT
case|:
name|seekHintFilter
operator|=
name|filter
expr_stmt|;
return|return
name|code
return|;
default|default:
return|return
name|code
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
name|filter
operator|.
name|filterAllRemaining
argument_list|()
condition|)
block|{
continue|continue;
block|}
switch|switch
condition|(
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|v
argument_list|)
condition|)
block|{
case|case
name|INCLUDE
case|:
if|if
condition|(
name|rc
operator|!=
name|ReturnCode
operator|.
name|INCLUDE_AND_NEXT_COL
condition|)
block|{
name|rc
operator|=
name|ReturnCode
operator|.
name|INCLUDE
expr_stmt|;
block|}
name|transformed
operator|=
name|filter
operator|.
name|transformCell
argument_list|(
name|transformed
argument_list|)
expr_stmt|;
break|break;
case|case
name|INCLUDE_AND_NEXT_COL
case|:
name|rc
operator|=
name|ReturnCode
operator|.
name|INCLUDE_AND_NEXT_COL
expr_stmt|;
name|transformed
operator|=
name|filter
operator|.
name|transformCell
argument_list|(
name|transformed
argument_list|)
expr_stmt|;
comment|// must continue here to evaluate all filters
break|break;
case|case
name|NEXT_ROW
case|:
break|break;
case|case
name|SKIP
case|:
break|break;
case|case
name|NEXT_COL
case|:
break|break;
case|case
name|SEEK_NEXT_USING_HINT
case|:
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Received code is not valid."
argument_list|)
throw|;
block|}
block|}
block|}
comment|// Save the transformed Cell for transform():
name|this
operator|.
name|transformedKV
operator|=
name|transformed
expr_stmt|;
return|return
name|rc
return|;
block|}
comment|/**    * Filters that never filter by modifying the returned List of Cells can    * inherit this implementation that does nothing.    *    * @inheritDoc    */
annotation|@
name|Override
specifier|public
name|void
name|filterRowCells
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|ignored
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Old filters based off of this class will override KeyValue transform(KeyValue).
comment|// Thus to maintain compatibility we need to call the old version.
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|ignored
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Cell
name|c
range|:
name|ignored
control|)
block|{
name|kvs
operator|.
name|add
argument_list|(
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|filterRow
argument_list|(
name|kvs
argument_list|)
expr_stmt|;
name|ignored
operator|.
name|clear
argument_list|()
expr_stmt|;
name|ignored
operator|.
name|addAll
argument_list|(
name|kvs
argument_list|)
expr_stmt|;
block|}
comment|/**    * WARNING: please to not override this method.  Instead override {@link #transformCell(Cell)}.    *    * This is for transition from 0.94 -> 0.96    */
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
name|void
name|filterRow
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
parameter_list|)
throws|throws
name|IOException
block|{
comment|// when removing this, this body should be in filterRowCells
comment|// convert to List<Cell> and call the new interface (this will call 0.96-style
comment|// #filterRowCells(List<Cell>) which may delegate to legacy #filterRow(List<KV>)
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|(
name|kvs
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|cells
operator|.
name|addAll
argument_list|(
name|kvs
argument_list|)
expr_stmt|;
for|for
control|(
name|Filter
name|filter
range|:
name|filters
control|)
block|{
name|filter
operator|.
name|filterRowCells
argument_list|(
name|cells
argument_list|)
expr_stmt|;
block|}
comment|// convert results into kvs
name|kvs
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Cell
name|c
range|:
name|cells
control|)
block|{
name|kvs
operator|.
name|add
argument_list|(
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasFilterRow
parameter_list|()
block|{
for|for
control|(
name|Filter
name|filter
range|:
name|filters
control|)
block|{
if|if
condition|(
name|filter
operator|.
name|hasFilterRow
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
annotation|@
name|Override
specifier|public
name|boolean
name|filterRow
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|Filter
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
name|filterRow
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
name|filterRow
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
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ONE
return|;
block|}
comment|/**    * @return The filter serialized using pb    */
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
throws|throws
name|IOException
block|{
name|FilterProtos
operator|.
name|FilterList
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|FilterList
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setOperator
argument_list|(
name|FilterProtos
operator|.
name|FilterList
operator|.
name|Operator
operator|.
name|valueOf
argument_list|(
name|operator
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Filter
name|filter
range|:
name|filters
control|)
block|{
name|builder
operator|.
name|addFilters
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|filter
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
comment|/**    * @param pbBytes A pb serialized {@link FilterList} instance    * @return An instance of {@link FilterList} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|FilterList
name|parseFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|pbBytes
parameter_list|)
throws|throws
name|DeserializationException
block|{
name|FilterProtos
operator|.
name|FilterList
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|FilterList
operator|.
name|parseFrom
argument_list|(
name|pbBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|Filter
argument_list|>
name|rowFilters
init|=
operator|new
name|ArrayList
argument_list|<
name|Filter
argument_list|>
argument_list|(
name|proto
operator|.
name|getFiltersCount
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|FilterProtos
operator|.
name|Filter
name|filter
range|:
name|proto
operator|.
name|getFiltersList
argument_list|()
control|)
block|{
name|rowFilters
operator|.
name|add
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|filter
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
return|return
operator|new
name|FilterList
argument_list|(
name|Operator
operator|.
name|valueOf
argument_list|(
name|proto
operator|.
name|getOperator
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
argument_list|,
name|rowFilters
argument_list|)
return|;
block|}
comment|/**    * @param other    * @return true if and only if the fields of the filter that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
name|boolean
name|areSerializedFieldsEqual
parameter_list|(
name|Filter
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
name|this
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|other
operator|instanceof
name|FilterList
operator|)
condition|)
return|return
literal|false
return|;
name|FilterList
name|o
init|=
operator|(
name|FilterList
operator|)
name|other
decl_stmt|;
return|return
name|this
operator|.
name|getOperator
argument_list|()
operator|.
name|equals
argument_list|(
name|o
operator|.
name|getOperator
argument_list|()
argument_list|)
operator|&&
operator|(
operator|(
name|this
operator|.
name|getFilters
argument_list|()
operator|==
name|o
operator|.
name|getFilters
argument_list|()
operator|)
operator|||
name|this
operator|.
name|getFilters
argument_list|()
operator|.
name|equals
argument_list|(
name|o
operator|.
name|getFilters
argument_list|()
argument_list|)
operator|)
return|;
block|}
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
name|KeyValue
name|getNextKeyHint
parameter_list|(
name|KeyValue
name|currentKV
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|getNextCellHint
argument_list|(
operator|(
name|Cell
operator|)
name|currentKV
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getNextCellHint
parameter_list|(
name|Cell
name|currentKV
parameter_list|)
throws|throws
name|IOException
block|{
name|Cell
name|keyHint
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ALL
condition|)
block|{
name|keyHint
operator|=
name|seekHintFilter
operator|.
name|getNextCellHint
argument_list|(
name|currentKV
argument_list|)
expr_stmt|;
return|return
name|keyHint
return|;
block|}
comment|// If any condition can pass, we need to keep the min hint
for|for
control|(
name|Filter
name|filter
range|:
name|filters
control|)
block|{
name|Cell
name|curKeyHint
init|=
name|filter
operator|.
name|getNextCellHint
argument_list|(
name|currentKV
argument_list|)
decl_stmt|;
if|if
condition|(
name|curKeyHint
operator|==
literal|null
condition|)
block|{
comment|// If we ever don't have a hint and this is must-pass-one, then no hint
return|return
literal|null
return|;
block|}
if|if
condition|(
name|curKeyHint
operator|!=
literal|null
condition|)
block|{
comment|// If this is the first hint we find, set it
if|if
condition|(
name|keyHint
operator|==
literal|null
condition|)
block|{
name|keyHint
operator|=
name|curKeyHint
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|KeyValue
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|keyHint
argument_list|,
name|curKeyHint
argument_list|)
operator|>
literal|0
condition|)
block|{
name|keyHint
operator|=
name|curKeyHint
expr_stmt|;
block|}
block|}
block|}
return|return
name|keyHint
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isFamilyEssential
parameter_list|(
name|byte
index|[]
name|name
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Filter
name|filter
range|:
name|filters
control|)
block|{
if|if
condition|(
name|filter
operator|.
name|isFamilyEssential
argument_list|(
name|name
argument_list|)
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
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|toString
argument_list|(
name|MAX_LOG_FILTERS
argument_list|)
return|;
block|}
specifier|protected
name|String
name|toString
parameter_list|(
name|int
name|maxFilters
parameter_list|)
block|{
name|int
name|endIndex
init|=
name|this
operator|.
name|filters
operator|.
name|size
argument_list|()
operator|<
name|maxFilters
condition|?
name|this
operator|.
name|filters
operator|.
name|size
argument_list|()
else|:
name|maxFilters
decl_stmt|;
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s %s (%d/%d): %s"
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|this
operator|.
name|operator
operator|==
name|Operator
operator|.
name|MUST_PASS_ALL
condition|?
literal|"AND"
else|:
literal|"OR"
argument_list|,
name|endIndex
argument_list|,
name|this
operator|.
name|filters
operator|.
name|size
argument_list|()
argument_list|,
name|this
operator|.
name|filters
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|endIndex
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

