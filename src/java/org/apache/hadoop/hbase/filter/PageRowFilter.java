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
name|List
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

begin_comment
comment|/**  * Implementation of RowFilterInterface that limits results to a specific page  * size. It terminates scanning once the number of filter-passed results is>=  * the given page size.  *   *<p>  * Note that this filter cannot guarantee that the number of results returned  * to a client are<= page size. This is because the filter is applied  * separately on different region servers. It does however optimize the scan of  * individual HRegions by making sure that the page size is never exceeded  * locally.  *</p>  */
end_comment

begin_class
specifier|public
class|class
name|PageRowFilter
implements|implements
name|RowFilterInterface
block|{
specifier|private
name|long
name|pageSize
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|private
name|int
name|rowsAccepted
init|=
literal|0
decl_stmt|;
comment|/**    * Default constructor, filters nothing. Required though for RPC    * deserialization.    */
specifier|public
name|PageRowFilter
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor that takes a maximum page size.    *     * @param pageSize Maximum result size.    */
specifier|public
name|PageRowFilter
parameter_list|(
specifier|final
name|long
name|pageSize
parameter_list|)
block|{
name|this
operator|.
name|pageSize
operator|=
name|pageSize
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
comment|// Doesn't filter columns
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|rowsAccepted
operator|=
literal|0
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
if|if
condition|(
operator|!
name|filtered
condition|)
block|{
name|this
operator|.
name|rowsAccepted
operator|++
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|processAlways
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
return|return
name|this
operator|.
name|rowsAccepted
operator|>
name|this
operator|.
name|pageSize
return|;
block|}
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
specifier|final
name|byte
index|[]
name|r
parameter_list|)
block|{
return|return
name|filterRowKey
argument_list|(
name|r
argument_list|,
literal|0
argument_list|,
name|r
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
return|return
name|filterAllRemaining
argument_list|()
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
name|colunmName
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
return|return
name|filterAllRemaining
argument_list|()
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
return|return
name|filterAllRemaining
argument_list|()
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
return|return
name|filterAllRemaining
argument_list|()
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
name|this
operator|.
name|pageSize
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
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
name|out
operator|.
name|writeLong
argument_list|(
name|pageSize
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

