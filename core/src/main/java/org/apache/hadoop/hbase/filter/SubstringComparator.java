begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Bytes
import|;
end_import

begin_comment
comment|/**  * This comparator is for use with ColumnValueFilter, for filtering based on  * the value of a given column. Use it to test if a given substring appears  * in a cell value in the column. The comparison is case insensitive.  *<p>  * Only EQUAL or NOT_EQUAL tests are valid with this comparator.   *<p>  * For example:  *<p>  *<pre>  * ColumnValueFilter cvf =  *   new ColumnValueFilter("col", ColumnValueFilter.CompareOp.EQUAL,  *     new SubstringComparator("substr"));  *</pre>  */
end_comment

begin_class
specifier|public
class|class
name|SubstringComparator
extends|extends
name|WritableByteArrayComparable
block|{
specifier|private
name|String
name|substr
decl_stmt|;
comment|/** Nullary constructor for Writable, do not use */
specifier|public
name|SubstringComparator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor    * @param substr the substring    */
specifier|public
name|SubstringComparator
parameter_list|(
name|String
name|substr
parameter_list|)
block|{
name|super
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|substr
operator|.
name|toLowerCase
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|substr
operator|=
name|substr
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getValue
parameter_list|()
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|substr
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toString
argument_list|(
name|value
argument_list|)
operator|.
name|toLowerCase
argument_list|()
operator|.
name|contains
argument_list|(
name|substr
argument_list|)
condition|?
literal|0
else|:
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|substr
init|=
name|in
operator|.
name|readUTF
argument_list|()
decl_stmt|;
name|this
operator|.
name|value
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|substr
argument_list|)
expr_stmt|;
name|this
operator|.
name|substr
operator|=
name|substr
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|substr
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

