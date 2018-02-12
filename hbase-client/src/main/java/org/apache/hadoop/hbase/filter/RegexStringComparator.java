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
name|Arrays
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
name|yetus
operator|.
name|audience
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
name|shaded
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
name|jcodings
operator|.
name|Encoding
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jcodings
operator|.
name|EncodingDB
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jcodings
operator|.
name|specific
operator|.
name|UTF8Encoding
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joni
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joni
operator|.
name|Option
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joni
operator|.
name|Regex
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joni
operator|.
name|Syntax
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
comment|/**  * This comparator is for use with {@link CompareFilter} implementations, such  * as {@link RowFilter}, {@link QualifierFilter}, and {@link ValueFilter}, for  * filtering based on the value of a given column. Use it to test if a given  * regular expression matches a cell value in the column.  *<p>  * Only EQUAL or NOT_EQUAL comparisons are valid with this comparator.  *<p>  * For example:  *<p>  *<pre>  * ValueFilter vf = new ValueFilter(CompareOp.EQUAL,  *     new RegexStringComparator(  *       // v4 IP address  *       "(((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3,3}" +  *         "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))(\\/[0-9]+)?" +  *         "|" +  *       // v6 IP address  *       "((([\\dA-Fa-f]{1,4}:){7}[\\dA-Fa-f]{1,4})(:([\\d]{1,3}.)" +  *         "{3}[\\d]{1,3})?)(\\/[0-9]+)?"));  *</pre>  *<p>  * Supports {@link java.util.regex.Pattern} flags as well:  *<p>  *<pre>  * ValueFilter vf = new ValueFilter(CompareOp.EQUAL,  *     new RegexStringComparator("regex", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));  *</pre>  * @see java.util.regex.Pattern  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|SuppressWarnings
argument_list|(
literal|"ComparableType"
argument_list|)
comment|// Should this move to Comparator usage?
specifier|public
class|class
name|RegexStringComparator
extends|extends
name|ByteArrayComparable
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|RegexStringComparator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Engine
name|engine
decl_stmt|;
comment|/** Engine implementation type (default=JAVA) */
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
enum|enum
name|EngineType
block|{
name|JAVA
block|,
name|JONI
block|}
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
comment|/**    * Constructor    * Adds Pattern.DOTALL to the underlying Pattern    * @param expr a valid regular expression    * @param engine engine implementation type    */
specifier|public
name|RegexStringComparator
parameter_list|(
name|String
name|expr
parameter_list|,
name|EngineType
name|engine
parameter_list|)
block|{
name|this
argument_list|(
name|expr
argument_list|,
name|Pattern
operator|.
name|DOTALL
argument_list|,
name|engine
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
name|this
argument_list|(
name|expr
argument_list|,
name|flags
argument_list|,
name|EngineType
operator|.
name|JAVA
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param expr a valid regular expression    * @param flags java.util.regex.Pattern flags    * @param engine engine implementation type    */
specifier|public
name|RegexStringComparator
parameter_list|(
name|String
name|expr
parameter_list|,
name|int
name|flags
parameter_list|,
name|EngineType
name|engine
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
switch|switch
condition|(
name|engine
condition|)
block|{
case|case
name|JAVA
case|:
name|this
operator|.
name|engine
operator|=
operator|new
name|JavaRegexEngine
argument_list|(
name|expr
argument_list|,
name|flags
argument_list|)
expr_stmt|;
break|break;
case|case
name|JONI
case|:
name|this
operator|.
name|engine
operator|=
operator|new
name|JoniRegexEngine
argument_list|(
name|expr
argument_list|,
name|flags
argument_list|)
expr_stmt|;
break|break;
block|}
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
name|engine
operator|.
name|setCharset
argument_list|(
name|charset
operator|.
name|name
argument_list|()
argument_list|)
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
return|return
name|engine
operator|.
name|compareTo
argument_list|(
name|value
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
comment|/**    * @return The comparator serialized using pb    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
return|return
name|engine
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
decl_stmt|;
if|if
condition|(
name|proto
operator|.
name|hasEngine
argument_list|()
condition|)
block|{
name|EngineType
name|engine
init|=
name|EngineType
operator|.
name|valueOf
argument_list|(
name|proto
operator|.
name|getEngine
argument_list|()
argument_list|)
decl_stmt|;
name|comparator
operator|=
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
argument_list|,
name|engine
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|comparator
operator|=
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
expr_stmt|;
block|}
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
name|getEngine
argument_list|()
operator|.
name|setCharset
argument_list|(
name|charset
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
annotation|@
name|Override
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
name|engine
operator|.
name|getClass
argument_list|()
operator|.
name|isInstance
argument_list|(
name|comparator
operator|.
name|getEngine
argument_list|()
argument_list|)
operator|&&
name|engine
operator|.
name|getPattern
argument_list|()
operator|.
name|equals
argument_list|(
name|comparator
operator|.
name|getEngine
argument_list|()
operator|.
name|getPattern
argument_list|()
argument_list|)
operator|&&
name|engine
operator|.
name|getFlags
argument_list|()
operator|==
name|comparator
operator|.
name|getEngine
argument_list|()
operator|.
name|getFlags
argument_list|()
operator|&&
name|engine
operator|.
name|getCharset
argument_list|()
operator|.
name|equals
argument_list|(
name|comparator
operator|.
name|getEngine
argument_list|()
operator|.
name|getCharset
argument_list|()
argument_list|)
return|;
block|}
name|Engine
name|getEngine
parameter_list|()
block|{
return|return
name|engine
return|;
block|}
comment|/**    * This is an internal interface for abstracting access to different regular    * expression matching engines.    */
specifier|static
interface|interface
name|Engine
block|{
comment|/**      * Returns the string representation of the configured regular expression      * for matching      */
name|String
name|getPattern
parameter_list|()
function_decl|;
comment|/**      * Returns the set of configured match flags, a bit mask that may include      * {@link Pattern} flags      */
name|int
name|getFlags
parameter_list|()
function_decl|;
comment|/**      * Returns the name of the configured charset      */
name|String
name|getCharset
parameter_list|()
function_decl|;
comment|/**      * Set the charset used when matching      * @param charset the name of the desired charset for matching      */
name|void
name|setCharset
parameter_list|(
specifier|final
name|String
name|charset
parameter_list|)
function_decl|;
comment|/**      * Return the serialized form of the configured matcher      */
name|byte
index|[]
name|toByteArray
parameter_list|()
function_decl|;
comment|/**      * Match the given input against the configured pattern      * @param value the data to be matched      * @param offset offset of the data to be matched      * @param length length of the data to be matched      * @return 0 if a match was made, 1 otherwise      */
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
function_decl|;
block|}
comment|/**    * Implementation of the Engine interface using Java's Pattern.    *<p>    * This is the default engine.    */
specifier|static
class|class
name|JavaRegexEngine
implements|implements
name|Engine
block|{
specifier|private
name|Charset
name|charset
init|=
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
decl_stmt|;
specifier|private
name|Pattern
name|pattern
decl_stmt|;
specifier|public
name|JavaRegexEngine
parameter_list|(
name|String
name|regex
parameter_list|,
name|int
name|flags
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
name|regex
argument_list|,
name|flags
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getPattern
parameter_list|()
block|{
return|return
name|pattern
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getFlags
parameter_list|()
block|{
return|return
name|pattern
operator|.
name|flags
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getCharset
parameter_list|()
block|{
return|return
name|charset
operator|.
name|name
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setCharset
parameter_list|(
name|String
name|charset
parameter_list|)
block|{
name|this
operator|.
name|charset
operator|=
name|Charset
operator|.
name|forName
argument_list|(
name|charset
argument_list|)
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
name|String
name|tmp
decl_stmt|;
if|if
condition|(
name|length
operator|<
name|value
operator|.
name|length
operator|/
literal|2
condition|)
block|{
comment|// See HBASE-9428. Make a copy of the relevant part of the byte[],
comment|// or the JDK will copy the entire byte[] during String decode
name|tmp
operator|=
operator|new
name|String
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|value
argument_list|,
name|offset
argument_list|,
name|offset
operator|+
name|length
argument_list|)
argument_list|,
name|charset
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tmp
operator|=
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
expr_stmt|;
block|}
return|return
name|pattern
operator|.
name|matcher
argument_list|(
name|tmp
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
annotation|@
name|Override
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
name|pattern
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
name|builder
operator|.
name|setEngine
argument_list|(
name|EngineType
operator|.
name|JAVA
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
block|}
comment|/**    * Implementation of the Engine interface using Jruby's joni regex engine.    *<p>    * This engine operates on byte arrays directly so is expected to be more GC    * friendly, and reportedly is twice as fast as Java's Pattern engine.    *<p>    * NOTE: Only the {@link Pattern} flags CASE_INSENSITIVE, DOTALL, and    * MULTILINE are supported.    */
specifier|static
class|class
name|JoniRegexEngine
implements|implements
name|Engine
block|{
specifier|private
name|Encoding
name|encoding
init|=
name|UTF8Encoding
operator|.
name|INSTANCE
decl_stmt|;
specifier|private
name|String
name|regex
decl_stmt|;
specifier|private
name|Regex
name|pattern
decl_stmt|;
specifier|public
name|JoniRegexEngine
parameter_list|(
name|String
name|regex
parameter_list|,
name|int
name|flags
parameter_list|)
block|{
name|this
operator|.
name|regex
operator|=
name|regex
expr_stmt|;
name|byte
index|[]
name|b
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|regex
argument_list|)
decl_stmt|;
name|this
operator|.
name|pattern
operator|=
operator|new
name|Regex
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
argument_list|,
name|patternToJoniFlags
argument_list|(
name|flags
argument_list|)
argument_list|,
name|encoding
argument_list|,
name|Syntax
operator|.
name|Java
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getPattern
parameter_list|()
block|{
return|return
name|regex
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getFlags
parameter_list|()
block|{
return|return
name|pattern
operator|.
name|getOptions
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getCharset
parameter_list|()
block|{
return|return
name|encoding
operator|.
name|getCharsetName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setCharset
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|setEncoding
argument_list|(
name|name
argument_list|)
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
comment|// Use subsequence match instead of full sequence match to adhere to the
comment|// principle of least surprise.
name|Matcher
name|m
init|=
name|pattern
operator|.
name|matcher
argument_list|(
name|value
argument_list|)
decl_stmt|;
return|return
name|m
operator|.
name|search
argument_list|(
name|offset
argument_list|,
name|length
argument_list|,
name|pattern
operator|.
name|getOptions
argument_list|()
argument_list|)
operator|<
literal|0
condition|?
literal|1
else|:
literal|0
return|;
block|}
annotation|@
name|Override
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
name|regex
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setPatternFlags
argument_list|(
name|joniToPatternFlags
argument_list|(
name|pattern
operator|.
name|getOptions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setCharset
argument_list|(
name|encoding
operator|.
name|getCharsetName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setEngine
argument_list|(
name|EngineType
operator|.
name|JONI
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
specifier|private
name|int
name|patternToJoniFlags
parameter_list|(
name|int
name|flags
parameter_list|)
block|{
name|int
name|newFlags
init|=
literal|0
decl_stmt|;
if|if
condition|(
operator|(
name|flags
operator|&
name|Pattern
operator|.
name|CASE_INSENSITIVE
operator|)
operator|!=
literal|0
condition|)
block|{
name|newFlags
operator||=
name|Option
operator|.
name|IGNORECASE
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flags
operator|&
name|Pattern
operator|.
name|DOTALL
operator|)
operator|!=
literal|0
condition|)
block|{
comment|// This does NOT mean Pattern.MULTILINE
name|newFlags
operator||=
name|Option
operator|.
name|MULTILINE
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flags
operator|&
name|Pattern
operator|.
name|MULTILINE
operator|)
operator|!=
literal|0
condition|)
block|{
comment|// This is what Java 8's Nashorn engine does when using joni and
comment|// translating Pattern's MULTILINE flag
name|newFlags
operator|&=
operator|~
name|Option
operator|.
name|SINGLELINE
expr_stmt|;
name|newFlags
operator||=
name|Option
operator|.
name|NEGATE_SINGLELINE
expr_stmt|;
block|}
return|return
name|newFlags
return|;
block|}
specifier|private
name|int
name|joniToPatternFlags
parameter_list|(
name|int
name|flags
parameter_list|)
block|{
name|int
name|newFlags
init|=
literal|0
decl_stmt|;
if|if
condition|(
operator|(
name|flags
operator|&
name|Option
operator|.
name|IGNORECASE
operator|)
operator|!=
literal|0
condition|)
block|{
name|newFlags
operator||=
name|Pattern
operator|.
name|CASE_INSENSITIVE
expr_stmt|;
block|}
comment|// This does NOT mean Pattern.MULTILINE, this is equivalent to Pattern.DOTALL
if|if
condition|(
operator|(
name|flags
operator|&
name|Option
operator|.
name|MULTILINE
operator|)
operator|!=
literal|0
condition|)
block|{
name|newFlags
operator||=
name|Pattern
operator|.
name|DOTALL
expr_stmt|;
block|}
comment|// This means Pattern.MULTILINE. Nice
if|if
condition|(
operator|(
name|flags
operator|&
name|Option
operator|.
name|NEGATE_SINGLELINE
operator|)
operator|!=
literal|0
condition|)
block|{
name|newFlags
operator||=
name|Pattern
operator|.
name|MULTILINE
expr_stmt|;
block|}
return|return
name|newFlags
return|;
block|}
specifier|private
name|void
name|setEncoding
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|EncodingDB
operator|.
name|Entry
name|e
init|=
name|EncodingDB
operator|.
name|getEncodings
argument_list|()
operator|.
name|get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|name
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
name|encoding
operator|=
name|e
operator|.
name|getEncoding
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalCharsetNameException
argument_list|(
name|name
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

