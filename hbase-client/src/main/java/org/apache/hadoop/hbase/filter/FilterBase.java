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

begin_comment
comment|/**  * Abstract base class to help you implement new Filters.  Common "ignore" or NOOP type  * methods can go here, helping to reduce boiler plate in an ever-expanding filter  * library.  *  * If you could instantiate FilterBase, it would end up being a "null" filter -  * that is one that never filters anything.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
comment|// TODO add filter limited private level
specifier|public
specifier|abstract
class|class
name|FilterBase
extends|extends
name|Filter
block|{
comment|/**    * Filters that are purely stateless and do nothing in their reset() methods can inherit    * this null/empty implementation.    *    * @inheritDoc    */
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{   }
comment|/**    * Filters that do not filter by row key can inherit this implementation that    * never filters anything. (ie: returns false).    *    * @inheritDoc    */
annotation|@
name|Override
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|byte
index|[]
name|buffer
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
return|return
literal|false
return|;
block|}
comment|/**    * Filters that never filter all remaining can inherit this implementation that    * never stops the filter early.    *    * @inheritDoc    */
annotation|@
name|Override
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
comment|/**    * By default no transformation takes place    *    * @inheritDoc    */
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
comment|// Old filters based off of this class will override KeyValue transform(KeyValue).
comment|// Thus to maintain compatibility we need to call the old version.
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
comment|/**    * WARNING: please to not override this method.  Instead override {@link #transformCell(Cell)}.    *    * This is for transition from 0.94 -> 0.96    */
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
name|KeyValue
name|transform
parameter_list|(
name|KeyValue
name|currentKV
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|currentKV
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
block|{   }
comment|/**    * Fitlers that never filter by modifying the returned List of Cells can    * inherit this implementation that does nothing.    *    * @inheritDoc    */
annotation|@
name|Override
specifier|public
name|boolean
name|hasFilterRow
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * Filters that never filter by rows based on previously gathered state from    * {@link #filterKeyValue(Cell)} can inherit this implementation that    * never filters a row.    *    * @inheritDoc    */
annotation|@
name|Override
specifier|public
name|boolean
name|filterRow
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
comment|/**    * This method is deprecated and you should override Cell getNextKeyHint(Cell) instead.    */
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
literal|null
return|;
block|}
comment|/**    * Filters that are not sure which key must be next seeked to, can inherit    * this implementation that, by default, returns a null Cell.    *    * @inheritDoc    */
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
comment|// Old filters based off of this class will override KeyValue getNextKeyHint(KeyValue).
comment|// Thus to maintain compatibility we need to call the old version.
return|return
name|getNextKeyHint
argument_list|(
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|currentKV
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * By default, we require all scan's column families to be present. Our    * subclasses may be more precise.    *    * @inheritDoc    */
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
return|return
literal|true
return|;
block|}
comment|/**    * Given the filter's arguments it constructs the filter    *<p>    * @param filterArguments the filter's arguments    * @return constructed filter object    */
specifier|public
specifier|static
name|Filter
name|createFilterFromArguments
parameter_list|(
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|filterArguments
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"This method has not been implemented"
argument_list|)
throw|;
block|}
comment|/**    * Return filter's info for debugging and logging purpose.    */
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
return|;
block|}
comment|/**    * Return length 0 byte array for Filters that don't require special serialization    */
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|byte
index|[
literal|0
index|]
return|;
block|}
comment|/**    * Default implementation so that writers of custom filters aren't forced to implement.    *    * @param other    * @return true if and only if the fields of the filter that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
name|boolean
name|areSerializedFieldsEqual
parameter_list|(
name|Filter
name|other
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

