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
comment|/**  * This comparator is for use with ColumnValueFilter, for filtering based on  * the value of a given column. Use it to test if a given regular expression  * matches a cell value in the column.  *<p>  * Only EQUAL or NOT_EQUAL tests are valid with this comparator.   *<p>  * For example:  *<p>  *<pre>  * ColumnValueFilter cvf =  *   new ColumnValueFilter("col",  *     ColumnValueFilter.CompareOp.EQUAL,  *     new RegexStringComparator(  *       // v4 IP address  *       "(((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3,3}" +  *         "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))(\\/[0-9]+)?" +  *         "|" +  *       // v6 IP address  *       "((([\\dA-Fa-f]{1,4}:){7}[\\dA-Fa-f]{1,4})(:([\\d]{1,3}.)" +  *         "{3}[\\d]{1,3})?)(\\/[0-9]+)?"));  *</pre>  */
end_comment

begin_class
specifier|public
class|class
name|RegexStringComparator
implements|implements
name|WritableByteArrayComparable
block|{
specifier|private
name|Pattern
name|pattern
decl_stmt|;
comment|/** Nullary constructor for Writable */
specifier|public
name|RegexStringComparator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor    * @param expr a valid regular expression    */
specifier|public
name|RegexStringComparator
parameter_list|(
name|String
name|expr
parameter_list|)
block|{
name|this
operator|.
name|pattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|expr
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|compareTo
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
comment|// Use find() for subsequence match instead of matches() (full sequence
comment|// match) to adhere to the principle of least surprise.
return|return
name|pattern
operator|.
name|matcher
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|)
operator|.
name|find
argument_list|()
condition|?
literal|0
else|:
literal|1
return|;
block|}
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
name|this
operator|.
name|pattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|pattern
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

