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
name|rest
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLDecoder
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|TreeSet
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Parses a path based row/column/timestamp specification into its component  * elements.  *<p>  *    */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RowSpec
block|{
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_START_TIMESTAMP
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_END_TIMESTAMP
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|private
name|byte
index|[]
name|row
init|=
name|HConstants
operator|.
name|EMPTY_START_ROW
decl_stmt|;
specifier|private
name|byte
index|[]
name|endRow
init|=
literal|null
decl_stmt|;
specifier|private
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|labels
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|long
name|startTime
init|=
name|DEFAULT_START_TIMESTAMP
decl_stmt|;
specifier|private
name|long
name|endTime
init|=
name|DEFAULT_END_TIMESTAMP
decl_stmt|;
specifier|private
name|int
name|maxVersions
init|=
literal|1
decl_stmt|;
specifier|private
name|int
name|maxValues
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|public
name|RowSpec
parameter_list|(
name|String
name|path
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|path
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
literal|'/'
condition|)
block|{
name|i
operator|++
expr_stmt|;
block|}
name|i
operator|=
name|parseRowKeys
argument_list|(
name|path
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|i
operator|=
name|parseColumns
argument_list|(
name|path
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|i
operator|=
name|parseTimestamp
argument_list|(
name|path
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|i
operator|=
name|parseQueryParams
argument_list|(
name|path
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|parseRowKeys
parameter_list|(
specifier|final
name|String
name|path
parameter_list|,
name|int
name|i
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|String
name|startRow
init|=
literal|null
decl_stmt|,
name|endRow
init|=
literal|null
decl_stmt|;
try|try
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|char
name|c
decl_stmt|;
while|while
condition|(
name|i
operator|<
name|path
operator|.
name|length
argument_list|()
operator|&&
operator|(
name|c
operator|=
name|path
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|)
operator|!=
literal|'/'
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
name|i
operator|++
expr_stmt|;
name|String
name|row
init|=
name|startRow
operator|=
name|sb
operator|.
name|toString
argument_list|()
decl_stmt|;
name|int
name|idx
init|=
name|startRow
operator|.
name|indexOf
argument_list|(
literal|','
argument_list|)
decl_stmt|;
if|if
condition|(
name|idx
operator|!=
operator|-
literal|1
condition|)
block|{
name|startRow
operator|=
name|URLDecoder
operator|.
name|decode
argument_list|(
name|row
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|idx
argument_list|)
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
name|endRow
operator|=
name|URLDecoder
operator|.
name|decode
argument_list|(
name|row
operator|.
name|substring
argument_list|(
name|idx
operator|+
literal|1
argument_list|)
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|startRow
operator|=
name|URLDecoder
operator|.
name|decode
argument_list|(
name|row
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IndexOutOfBoundsException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// HBase does not support wildcards on row keys so we will emulate a
comment|// suffix glob by synthesizing appropriate start and end row keys for
comment|// table scanning
if|if
condition|(
name|startRow
operator|.
name|charAt
argument_list|(
name|startRow
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
operator|==
literal|'*'
condition|)
block|{
if|if
condition|(
name|endRow
operator|!=
literal|null
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid path: start row "
operator|+
literal|"specified with wildcard"
argument_list|)
throw|;
name|this
operator|.
name|row
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|startRow
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|startRow
operator|.
name|lastIndexOf
argument_list|(
literal|"*"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|endRow
operator|=
operator|new
name|byte
index|[
name|this
operator|.
name|row
operator|.
name|length
operator|+
literal|1
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|this
operator|.
name|row
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|endRow
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|row
operator|.
name|length
argument_list|)
expr_stmt|;
name|this
operator|.
name|endRow
index|[
name|this
operator|.
name|row
operator|.
name|length
index|]
operator|=
operator|(
name|byte
operator|)
literal|255
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|row
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|startRow
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|endRow
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|endRow
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|endRow
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|i
return|;
block|}
specifier|private
name|int
name|parseColumns
parameter_list|(
specifier|final
name|String
name|path
parameter_list|,
name|int
name|i
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
if|if
condition|(
name|i
operator|>=
name|path
operator|.
name|length
argument_list|()
condition|)
block|{
return|return
name|i
return|;
block|}
try|try
block|{
name|char
name|c
decl_stmt|;
name|StringBuilder
name|column
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
name|i
operator|<
name|path
operator|.
name|length
argument_list|()
operator|&&
operator|(
name|c
operator|=
name|path
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|)
operator|!=
literal|'/'
condition|)
block|{
if|if
condition|(
name|c
operator|==
literal|','
condition|)
block|{
if|if
condition|(
name|column
operator|.
name|length
argument_list|()
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid path"
argument_list|)
throw|;
block|}
name|String
name|s
init|=
name|URLDecoder
operator|.
name|decode
argument_list|(
name|column
operator|.
name|toString
argument_list|()
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
decl_stmt|;
name|this
operator|.
name|columns
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
name|column
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
continue|continue;
block|}
name|column
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
name|i
operator|++
expr_stmt|;
comment|// trailing list entry
if|if
condition|(
name|column
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|String
name|s
init|=
name|URLDecoder
operator|.
name|decode
argument_list|(
name|column
operator|.
name|toString
argument_list|()
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
decl_stmt|;
name|this
operator|.
name|columns
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IndexOutOfBoundsException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
comment|// shouldn't happen
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|i
return|;
block|}
specifier|private
name|int
name|parseTimestamp
parameter_list|(
specifier|final
name|String
name|path
parameter_list|,
name|int
name|i
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
if|if
condition|(
name|i
operator|>=
name|path
operator|.
name|length
argument_list|()
condition|)
block|{
return|return
name|i
return|;
block|}
name|long
name|time0
init|=
literal|0
decl_stmt|,
name|time1
init|=
literal|0
decl_stmt|;
try|try
block|{
name|char
name|c
init|=
literal|0
decl_stmt|;
name|StringBuilder
name|stamp
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
name|i
operator|<
name|path
operator|.
name|length
argument_list|()
condition|)
block|{
name|c
operator|=
name|path
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
operator|==
literal|'/'
operator|||
name|c
operator|==
literal|','
condition|)
block|{
break|break;
block|}
name|stamp
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
try|try
block|{
name|time0
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|URLDecoder
operator|.
name|decode
argument_list|(
name|stamp
operator|.
name|toString
argument_list|()
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|c
operator|==
literal|','
condition|)
block|{
name|stamp
operator|=
operator|new
name|StringBuilder
argument_list|()
expr_stmt|;
name|i
operator|++
expr_stmt|;
while|while
condition|(
name|i
operator|<
name|path
operator|.
name|length
argument_list|()
operator|&&
operator|(
operator|(
name|c
operator|=
name|path
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|)
operator|!=
literal|'/'
operator|)
condition|)
block|{
name|stamp
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
try|try
block|{
name|time1
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|URLDecoder
operator|.
name|decode
argument_list|(
name|stamp
operator|.
name|toString
argument_list|()
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|c
operator|==
literal|'/'
condition|)
block|{
name|i
operator|++
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IndexOutOfBoundsException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
comment|// shouldn't happen
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|time1
operator|!=
literal|0
condition|)
block|{
name|startTime
operator|=
name|time0
expr_stmt|;
name|endTime
operator|=
name|time1
expr_stmt|;
block|}
else|else
block|{
name|endTime
operator|=
name|time0
expr_stmt|;
block|}
return|return
name|i
return|;
block|}
specifier|private
name|int
name|parseQueryParams
parameter_list|(
specifier|final
name|String
name|path
parameter_list|,
name|int
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|>=
name|path
operator|.
name|length
argument_list|()
condition|)
block|{
return|return
name|i
return|;
block|}
name|StringBuilder
name|query
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
try|try
block|{
name|query
operator|.
name|append
argument_list|(
name|URLDecoder
operator|.
name|decode
argument_list|(
name|path
operator|.
name|substring
argument_list|(
name|i
argument_list|)
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
comment|// should not happen
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|i
operator|+=
name|query
operator|.
name|length
argument_list|()
expr_stmt|;
name|int
name|j
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|j
operator|<
name|query
operator|.
name|length
argument_list|()
condition|)
block|{
name|char
name|c
init|=
name|query
operator|.
name|charAt
argument_list|(
name|j
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|'?'
operator|&&
name|c
operator|!=
literal|'&'
condition|)
block|{
break|break;
block|}
if|if
condition|(
operator|++
name|j
operator|>
name|query
operator|.
name|length
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"malformed query parameter"
argument_list|)
throw|;
block|}
name|char
name|what
init|=
name|query
operator|.
name|charAt
argument_list|(
name|j
argument_list|)
decl_stmt|;
if|if
condition|(
operator|++
name|j
operator|>
name|query
operator|.
name|length
argument_list|()
condition|)
block|{
break|break;
block|}
name|c
operator|=
name|query
operator|.
name|charAt
argument_list|(
name|j
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
operator|!=
literal|'='
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"malformed query parameter"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|++
name|j
operator|>
name|query
operator|.
name|length
argument_list|()
condition|)
block|{
break|break;
block|}
switch|switch
condition|(
name|what
condition|)
block|{
case|case
literal|'m'
case|:
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
name|j
operator|<=
name|query
operator|.
name|length
argument_list|()
condition|)
block|{
name|c
operator|=
name|query
operator|.
name|charAt
argument_list|(
name|j
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
argument_list|<
literal|'0'
operator|||
name|c
argument_list|>
literal|'9'
condition|)
block|{
name|j
operator|--
expr_stmt|;
break|break;
block|}
name|sb
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
name|maxVersions
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|'n'
case|:
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
name|j
operator|<=
name|query
operator|.
name|length
argument_list|()
condition|)
block|{
name|c
operator|=
name|query
operator|.
name|charAt
argument_list|(
name|j
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
argument_list|<
literal|'0'
operator|||
name|c
argument_list|>
literal|'9'
condition|)
block|{
name|j
operator|--
expr_stmt|;
break|break;
block|}
name|sb
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
name|maxValues
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unknown parameter '"
operator|+
name|c
operator|+
literal|"'"
argument_list|)
throw|;
block|}
block|}
return|return
name|i
return|;
block|}
specifier|public
name|RowSpec
parameter_list|(
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|endRow
parameter_list|,
name|byte
index|[]
index|[]
name|columns
parameter_list|,
name|long
name|startTime
parameter_list|,
name|long
name|endTime
parameter_list|,
name|int
name|maxVersions
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|startRow
expr_stmt|;
name|this
operator|.
name|endRow
operator|=
name|endRow
expr_stmt|;
if|if
condition|(
name|columns
operator|!=
literal|null
condition|)
block|{
name|Collections
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|columns
argument_list|,
name|columns
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|startTime
operator|=
name|startTime
expr_stmt|;
name|this
operator|.
name|endTime
operator|=
name|endTime
expr_stmt|;
name|this
operator|.
name|maxVersions
operator|=
name|maxVersions
expr_stmt|;
block|}
specifier|public
name|RowSpec
parameter_list|(
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|endRow
parameter_list|,
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|,
name|long
name|startTime
parameter_list|,
name|long
name|endTime
parameter_list|,
name|int
name|maxVersions
parameter_list|,
name|Collection
argument_list|<
name|String
argument_list|>
name|labels
parameter_list|)
block|{
name|this
argument_list|(
name|startRow
argument_list|,
name|endRow
argument_list|,
name|columns
argument_list|,
name|startTime
argument_list|,
name|endTime
argument_list|,
name|maxVersions
argument_list|)
expr_stmt|;
if|if
condition|(
name|labels
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|labels
operator|.
name|addAll
argument_list|(
name|labels
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|RowSpec
parameter_list|(
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|endRow
parameter_list|,
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|,
name|long
name|startTime
parameter_list|,
name|long
name|endTime
parameter_list|,
name|int
name|maxVersions
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|startRow
expr_stmt|;
name|this
operator|.
name|endRow
operator|=
name|endRow
expr_stmt|;
if|if
condition|(
name|columns
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|columns
operator|.
name|addAll
argument_list|(
name|columns
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|startTime
operator|=
name|startTime
expr_stmt|;
name|this
operator|.
name|endTime
operator|=
name|endTime
expr_stmt|;
name|this
operator|.
name|maxVersions
operator|=
name|maxVersions
expr_stmt|;
block|}
specifier|public
name|boolean
name|isSingleRow
parameter_list|()
block|{
return|return
name|endRow
operator|==
literal|null
return|;
block|}
specifier|public
name|int
name|getMaxVersions
parameter_list|()
block|{
return|return
name|maxVersions
return|;
block|}
specifier|public
name|void
name|setMaxVersions
parameter_list|(
specifier|final
name|int
name|maxVersions
parameter_list|)
block|{
name|this
operator|.
name|maxVersions
operator|=
name|maxVersions
expr_stmt|;
block|}
specifier|public
name|int
name|getMaxValues
parameter_list|()
block|{
return|return
name|maxValues
return|;
block|}
specifier|public
name|void
name|setMaxValues
parameter_list|(
specifier|final
name|int
name|maxValues
parameter_list|)
block|{
name|this
operator|.
name|maxValues
operator|=
name|maxValues
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasColumns
parameter_list|()
block|{
return|return
operator|!
name|columns
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|hasLabels
parameter_list|()
block|{
return|return
operator|!
name|labels
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
name|byte
index|[]
name|getRow
parameter_list|()
block|{
return|return
name|row
return|;
block|}
specifier|public
name|byte
index|[]
name|getStartRow
parameter_list|()
block|{
return|return
name|row
return|;
block|}
specifier|public
name|boolean
name|hasEndRow
parameter_list|()
block|{
return|return
name|endRow
operator|!=
literal|null
return|;
block|}
specifier|public
name|byte
index|[]
name|getEndRow
parameter_list|()
block|{
return|return
name|endRow
return|;
block|}
specifier|public
name|void
name|addColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|column
parameter_list|)
block|{
name|columns
operator|.
name|add
argument_list|(
name|column
argument_list|)
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
index|[]
name|getColumns
parameter_list|()
block|{
return|return
name|columns
operator|.
name|toArray
argument_list|(
operator|new
name|byte
index|[
name|columns
operator|.
name|size
argument_list|()
index|]
index|[]
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getLabels
parameter_list|()
block|{
return|return
name|labels
return|;
block|}
specifier|public
name|boolean
name|hasTimestamp
parameter_list|()
block|{
return|return
operator|(
name|startTime
operator|==
literal|0
operator|)
operator|&&
operator|(
name|endTime
operator|!=
name|Long
operator|.
name|MAX_VALUE
operator|)
return|;
block|}
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|endTime
return|;
block|}
specifier|public
name|long
name|getStartTime
parameter_list|()
block|{
return|return
name|startTime
return|;
block|}
specifier|public
name|void
name|setStartTime
parameter_list|(
specifier|final
name|long
name|startTime
parameter_list|)
block|{
name|this
operator|.
name|startTime
operator|=
name|startTime
expr_stmt|;
block|}
specifier|public
name|long
name|getEndTime
parameter_list|()
block|{
return|return
name|endTime
return|;
block|}
specifier|public
name|void
name|setEndTime
parameter_list|(
name|long
name|endTime
parameter_list|)
block|{
name|this
operator|.
name|endTime
operator|=
name|endTime
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|result
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|result
operator|.
name|append
argument_list|(
literal|"{startRow => '"
argument_list|)
expr_stmt|;
if|if
condition|(
name|row
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|append
argument_list|(
literal|"', endRow => '"
argument_list|)
expr_stmt|;
if|if
condition|(
name|endRow
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|endRow
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|append
argument_list|(
literal|"', columns => ["
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|col
range|:
name|columns
control|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|" '"
argument_list|)
expr_stmt|;
name|result
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|col
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|append
argument_list|(
literal|" ], startTime => "
argument_list|)
expr_stmt|;
name|result
operator|.
name|append
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|startTime
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|.
name|append
argument_list|(
literal|", endTime => "
argument_list|)
expr_stmt|;
name|result
operator|.
name|append
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|endTime
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|.
name|append
argument_list|(
literal|", maxVersions => "
argument_list|)
expr_stmt|;
name|result
operator|.
name|append
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|maxVersions
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|.
name|append
argument_list|(
literal|", maxValues => "
argument_list|)
expr_stmt|;
name|result
operator|.
name|append
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|maxValues
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|result
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

