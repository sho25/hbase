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
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ComparatorProtos
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|IllegalCharsetNameException
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

begin_comment
comment|/**  * This comparator is for use with {@link CompareFilter} implementations, such  * as {@link RowFilter}, {@link QualifierFilter}, and {@link ValueFilter}, for  * filtering based on the value of a given column. Use it to test if a given  * regular expression matches a cell value in the column.  *<p>  * Only EQUAL or NOT_EQUAL comparisons are valid with this comparator.  *<p>  * For example:  *<p>  *<pre>  * ValueFilter vf = new ValueFilter(CompareOp.EQUAL,  *     new RegexStringComparator(  *       // v4 IP address  *       "(((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3,3}" +  *         "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))(\\/[0-9]+)?" +  *         "|" +  *       // v6 IP address  *       "((([\\dA-Fa-f]{1,4}:){7}[\\dA-Fa-f]{1,4})(:([\\d]{1,3}.)" +  *         "{3}[\\d]{1,3})?)(\\/[0-9]+)?"));  *</pre>  *<p>  * Supports {@link java.util.regex.Pattern} flags as well:  *<p>  *<pre>  * ValueFilter vf = new ValueFilter(CompareOp.EQUAL,  *     new RegexStringComparator("regex", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));  *</pre>  * @see java.util.regex.Pattern  */
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
name|RegexStringComparator
extends|extends
name|ByteArrayComparable
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RegexStringComparator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Charset
name|charset
init|=
name|HConstants
operator|.
name|UTF8_CHARSET
decl_stmt|;
specifier|private
name|Pattern
name|pattern
decl_stmt|;
comment|/**    * Constructor    * Adds Pattern.DOTALL to the underlying Pattern    * @param expr a valid regular expression    */
specifier|public
name|RegexStringComparator
parameter_list|(
name|String
name|expr
parameter_list|)
block|{
name|this
argument_list|(
name|expr
argument_list|,
name|Pattern
operator|.
name|DOTALL
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param expr a valid regular expression    * @param flags java.util.regex.Pattern flags    */
specifier|public
name|RegexStringComparator
parameter_list|(
name|String
name|expr
parameter_list|,
name|int
name|flags
parameter_list|)
block|{
name|super
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|expr
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|pattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|expr
argument_list|,
name|flags
argument_list|)
expr_stmt|;
block|}
comment|/**    * Specifies the {@link Charset} to use to convert the row key to a String.    *<p>    * The row key needs to be converted to a String in order to be matched    * against the regular expression.  This method controls which charset is    * used to do this conversion.    *<p>    * If the row key is made of arbitrary bytes, the charset {@code ISO-8859-1}    * is recommended.    * @param charset The charset to use.    */
specifier|public
name|void
name|setCharset
parameter_list|(
specifier|final
name|Charset
name|charset
parameter_list|)
block|{
name|this
operator|.
name|charset
operator|=
name|charset
expr_stmt|;
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
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
comment|// Use find() for subsequence match instead of matches() (full sequence
comment|// match) to adhere to the principle of least surprise.
return|return
name|pattern
operator|.
name|matcher
argument_list|(
operator|new
name|String
argument_list|(
name|value
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|charset
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
comment|/**    * @return The comparator serialized using pb    */
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
name|ComparatorProtos
operator|.
name|RegexStringComparator
operator|.
name|Builder
name|builder
init|=
name|ComparatorProtos
operator|.
name|RegexStringComparator
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setPattern
argument_list|(
name|pattern
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setPatternFlags
argument_list|(
name|pattern
operator|.
name|flags
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setCharset
argument_list|(
name|charset
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
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
comment|/**    * @param pbBytes A pb serialized {@link RegexStringComparator} instance    * @return An instance of {@link RegexStringComparator} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|RegexStringComparator
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
name|ComparatorProtos
operator|.
name|RegexStringComparator
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|ComparatorProtos
operator|.
name|RegexStringComparator
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
name|RegexStringComparator
name|comparator
init|=
operator|new
name|RegexStringComparator
argument_list|(
name|proto
operator|.
name|getPattern
argument_list|()
argument_list|,
name|proto
operator|.
name|getPatternFlags
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|String
name|charset
init|=
name|proto
operator|.
name|getCharset
argument_list|()
decl_stmt|;
if|if
condition|(
name|charset
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
try|try
block|{
name|comparator
operator|.
name|setCharset
argument_list|(
name|Charset
operator|.
name|forName
argument_list|(
name|charset
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalCharsetNameException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"invalid charset"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|comparator
return|;
block|}
comment|/**    * @param other    * @return true if and only if the fields of the comparator that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
name|boolean
name|areSerializedFieldsEqual
parameter_list|(
name|ByteArrayComparable
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
name|RegexStringComparator
operator|)
condition|)
return|return
literal|false
return|;
name|RegexStringComparator
name|comparator
init|=
operator|(
name|RegexStringComparator
operator|)
name|other
decl_stmt|;
return|return
name|super
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|comparator
argument_list|)
operator|&&
name|this
operator|.
name|pattern
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|comparator
operator|.
name|pattern
operator|.
name|toString
argument_list|()
argument_list|)
operator|&&
name|this
operator|.
name|pattern
operator|.
name|flags
argument_list|()
operator|==
name|comparator
operator|.
name|pattern
operator|.
name|flags
argument_list|()
operator|&&
name|this
operator|.
name|charset
operator|.
name|equals
argument_list|(
name|comparator
operator|.
name|charset
argument_list|)
return|;
block|}
block|}
end_class

end_unit

