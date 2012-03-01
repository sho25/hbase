begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance                                                                       with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|KeyValue
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
name|ArrayList
import|;
end_import

begin_comment
comment|/**  * Abstract base class to help you implement new Filters.  Common "ignore" or NOOP type  * methods can go here, helping to reduce boiler plate in an ever-expanding filter  * library.  *  * If you could instantiate FilterBase, it would end up being a "null" filter -  * that is one that never filters anything.  */
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
specifier|public
specifier|abstract
class|class
name|FilterBase
implements|implements
name|Filter
block|{
comment|/**    * Filters that are purely stateless and do nothing in their reset() methods can inherit    * this null/empty implementation.    *    * @inheritDoc    */
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
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
block|{
return|return
literal|false
return|;
block|}
comment|/**    * Filters that dont filter by key value can inherit this implementation that    * includes all KeyValues.    *    * @inheritDoc    */
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|KeyValue
name|ignored
parameter_list|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
comment|/**    * By default no transformation takes place    *    * @inheritDoc    */
annotation|@
name|Override
specifier|public
name|KeyValue
name|transform
parameter_list|(
name|KeyValue
name|v
parameter_list|)
block|{
return|return
name|v
return|;
block|}
comment|/**    * Filters that never filter by modifying the returned List of KeyValues can    * inherit this implementation that does nothing.    *    * @inheritDoc    */
annotation|@
name|Override
specifier|public
name|void
name|filterRow
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|ignored
parameter_list|)
block|{   }
comment|/**    * Fitlers that never filter by modifying the returned List of KeyValues can    * inherit this implementation that does nothing.    *    * @inheritDoc    */
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
comment|/**    * Filters that never filter by rows based on previously gathered state from    * {@link #filterKeyValue(KeyValue)} can inherit this implementation that    * never filters a row.    *    * @inheritDoc    */
annotation|@
name|Override
specifier|public
name|boolean
name|filterRow
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * Filters that are not sure which key must be next seeked to, can inherit    * this implementation that, by default, returns a null KeyValue.    *    * @inheritDoc    */
specifier|public
name|KeyValue
name|getNextKeyHint
parameter_list|(
name|KeyValue
name|currentKV
parameter_list|)
block|{
return|return
literal|null
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
block|}
end_class

end_unit

