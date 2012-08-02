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
name|ArrayList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Implementation of Filter interface that limits results to a specific page  * size. It terminates scanning once the number of filter-passed rows is>  * the given page size.  *<p>  * Note that this filter cannot guarantee that the number of results returned  * to a client are<= page size. This is because the filter is applied  * separately on different region servers. It does however optimize the scan of  * individual HRegions by making sure that the page size is never exceeded  * locally.  */
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
name|PageFilter
extends|extends
name|FilterBase
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
name|PageFilter
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor that takes a maximum page size.    *    * @param pageSize Maximum result size.    */
specifier|public
name|PageFilter
parameter_list|(
specifier|final
name|long
name|pageSize
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|pageSize
operator|>=
literal|0
argument_list|,
literal|"must be positive %s"
argument_list|,
name|pageSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|pageSize
operator|=
name|pageSize
expr_stmt|;
block|}
specifier|public
name|long
name|getPageSize
parameter_list|()
block|{
return|return
name|pageSize
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
operator|>=
name|this
operator|.
name|pageSize
return|;
block|}
specifier|public
name|boolean
name|filterRow
parameter_list|()
block|{
name|this
operator|.
name|rowsAccepted
operator|++
expr_stmt|;
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
name|hasFilterRow
parameter_list|()
block|{
return|return
literal|true
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
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|filterArguments
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|,
literal|"Expected 1 but got: %s"
argument_list|,
name|filterArguments
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|pageSize
init|=
name|ParseFilter
operator|.
name|convertByteArrayToLong
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|PageFilter
argument_list|(
name|pageSize
argument_list|)
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
annotation|@
name|Override
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
operator|+
literal|" "
operator|+
name|this
operator|.
name|pageSize
return|;
block|}
block|}
end_class

end_unit

