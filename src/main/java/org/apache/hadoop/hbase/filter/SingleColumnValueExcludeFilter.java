begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|CompareFilter
operator|.
name|CompareOp
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
comment|/**  * A {@link Filter} that checks a single column value, but does not emit the  * tested column. This will enable a performance boost over  * {@link SingleColumnValueFilter}, if the tested column value is not actually  * needed as input (besides for the filtering itself).  */
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
class|class
name|SingleColumnValueExcludeFilter
extends|extends
name|SingleColumnValueFilter
block|{
comment|/**    * Writable constructor, do not use.    */
specifier|public
name|SingleColumnValueExcludeFilter
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor for binary compare of the value of a single column. If the    * column is found and the condition passes, all columns of the row will be    * emitted; except for the tested column value. If the column is not found or    * the condition fails, the row will not be emitted.    *    * @param family name of column family    * @param qualifier name of column qualifier    * @param compareOp operator    * @param value value to compare column values against    */
specifier|public
name|SingleColumnValueExcludeFilter
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|compareOp
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor for binary compare of the value of a single column. If the    * column is found and the condition passes, all columns of the row will be    * emitted; except for the tested column value. If the condition fails, the    * row will not be emitted.    *<p>    * Use the filterIfColumnMissing flag to set whether the rest of the columns    * in a row will be emitted if the specified column to check is not found in    * the row.    *    * @param family name of column family    * @param qualifier name of column qualifier    * @param compareOp operator    * @param comparator Comparator to use.    */
specifier|public
name|SingleColumnValueExcludeFilter
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOp
name|compareOp
parameter_list|,
name|WritableByteArrayComparable
name|comparator
parameter_list|)
block|{
name|super
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|compareOp
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|KeyValue
name|keyValue
parameter_list|)
block|{
name|ReturnCode
name|superRetCode
init|=
name|super
operator|.
name|filterKeyValue
argument_list|(
name|keyValue
argument_list|)
decl_stmt|;
if|if
condition|(
name|superRetCode
operator|==
name|ReturnCode
operator|.
name|INCLUDE
condition|)
block|{
comment|// If the current column is actually the tested column,
comment|// we will skip it instead.
if|if
condition|(
name|keyValue
operator|.
name|matchingColumn
argument_list|(
name|this
operator|.
name|columnFamily
argument_list|,
name|this
operator|.
name|columnQualifier
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|SKIP
return|;
block|}
block|}
return|return
name|superRetCode
return|;
block|}
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
name|SingleColumnValueFilter
name|tempFilter
init|=
operator|(
name|SingleColumnValueFilter
operator|)
name|SingleColumnValueFilter
operator|.
name|createFilterFromArguments
argument_list|(
name|filterArguments
argument_list|)
decl_stmt|;
name|SingleColumnValueExcludeFilter
name|filter
init|=
operator|new
name|SingleColumnValueExcludeFilter
argument_list|(
name|tempFilter
operator|.
name|getFamily
argument_list|()
argument_list|,
name|tempFilter
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|tempFilter
operator|.
name|getOperator
argument_list|()
argument_list|,
name|tempFilter
operator|.
name|getComparator
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|filterArguments
operator|.
name|size
argument_list|()
operator|==
literal|6
condition|)
block|{
name|filter
operator|.
name|setFilterIfMissing
argument_list|(
name|tempFilter
operator|.
name|getFilterIfMissing
argument_list|()
argument_list|)
expr_stmt|;
name|filter
operator|.
name|setLatestVersionOnly
argument_list|(
name|tempFilter
operator|.
name|getLatestVersionOnly
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|filter
return|;
block|}
block|}
end_class

end_unit

