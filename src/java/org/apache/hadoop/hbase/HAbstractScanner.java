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
name|Iterator
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
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Vector
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|DataInputBuffer
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
name|io
operator|.
name|DataOutputBuffer
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * Abstract base class that implements the HScannerInterface.  * Used by the concrete HMemcacheScanner and HStoreScanners  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HAbstractScanner
implements|implements
name|HInternalScannerInterface
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// Pattern to determine if a column key is a regex
specifier|static
name|Pattern
name|isRegexPattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"^.*[\\\\+|^&*$\\[\\]\\}{)(]+.*$"
argument_list|)
decl_stmt|;
comment|/** The kind of match we are doing on a column: */
specifier|private
specifier|static
enum|enum
name|MATCH_TYPE
block|{
comment|/** Just check the column family name */
name|FAMILY_ONLY
block|,
comment|/** Column family + matches regex */
name|REGEX
block|,
comment|/** Literal matching */
name|SIMPLE
block|}
comment|/**    * This class provides column matching functions that are more sophisticated    * than a simple string compare. There are three types of matching:    *<ol>    *<li>Match on the column family name only</li>    *<li>Match on the column family + column key regex</li>    *<li>Simple match: compare column family + column key literally</li>    *</ul>    */
specifier|private
specifier|static
class|class
name|ColumnMatcher
block|{
specifier|private
name|boolean
name|wildCardmatch
decl_stmt|;
specifier|private
name|MATCH_TYPE
name|matchType
decl_stmt|;
specifier|private
name|String
name|family
decl_stmt|;
specifier|private
name|Pattern
name|columnMatcher
decl_stmt|;
specifier|private
name|Text
name|col
decl_stmt|;
name|ColumnMatcher
parameter_list|(
specifier|final
name|Text
name|col
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
name|qualifier
init|=
name|HStoreKey
operator|.
name|extractQualifier
argument_list|(
name|col
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|qualifier
operator|==
literal|null
operator|||
name|qualifier
operator|.
name|getLength
argument_list|()
operator|==
literal|0
condition|)
block|{
name|this
operator|.
name|matchType
operator|=
name|MATCH_TYPE
operator|.
name|FAMILY_ONLY
expr_stmt|;
name|this
operator|.
name|family
operator|=
name|HStoreKey
operator|.
name|extractFamily
argument_list|(
name|col
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
name|this
operator|.
name|wildCardmatch
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|isRegexPattern
operator|.
name|matcher
argument_list|(
name|qualifier
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
name|this
operator|.
name|matchType
operator|=
name|MATCH_TYPE
operator|.
name|REGEX
expr_stmt|;
name|this
operator|.
name|columnMatcher
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|col
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|wildCardmatch
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|matchType
operator|=
name|MATCH_TYPE
operator|.
name|SIMPLE
expr_stmt|;
name|this
operator|.
name|col
operator|=
name|col
expr_stmt|;
name|this
operator|.
name|wildCardmatch
operator|=
literal|false
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Column: "
operator|+
name|col
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/** Matching method */
name|boolean
name|matches
parameter_list|(
name|Text
name|c
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|matchType
operator|==
name|MATCH_TYPE
operator|.
name|SIMPLE
condition|)
block|{
return|return
name|c
operator|.
name|equals
argument_list|(
name|this
operator|.
name|col
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|matchType
operator|==
name|MATCH_TYPE
operator|.
name|FAMILY_ONLY
condition|)
block|{
return|return
name|HStoreKey
operator|.
name|extractFamily
argument_list|(
name|c
argument_list|)
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|this
operator|.
name|family
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|matchType
operator|==
name|MATCH_TYPE
operator|.
name|REGEX
condition|)
block|{
return|return
name|this
operator|.
name|columnMatcher
operator|.
name|matcher
argument_list|(
name|c
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|matches
argument_list|()
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Invalid match type: "
operator|+
name|this
operator|.
name|matchType
argument_list|)
throw|;
block|}
block|}
name|boolean
name|isWildCardMatch
parameter_list|()
block|{
return|return
name|this
operator|.
name|wildCardmatch
return|;
block|}
block|}
specifier|protected
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|Vector
argument_list|<
name|ColumnMatcher
argument_list|>
argument_list|>
name|okCols
decl_stmt|;
comment|// Holds matchers for each column family
specifier|protected
name|boolean
name|scannerClosed
init|=
literal|false
decl_stmt|;
comment|// True when scanning is done
comment|// Keys retrieved from the sources
specifier|protected
name|HStoreKey
name|keys
index|[]
decl_stmt|;
comment|// Values that correspond to those keys
specifier|protected
name|byte
index|[]
index|[]
name|vals
decl_stmt|;
specifier|protected
name|long
name|timestamp
decl_stmt|;
comment|// The timestamp to match entries against
specifier|private
name|boolean
name|wildcardMatch
decl_stmt|;
specifier|private
name|boolean
name|multipleMatchers
decl_stmt|;
specifier|protected
name|DataOutputBuffer
name|outbuf
init|=
operator|new
name|DataOutputBuffer
argument_list|()
decl_stmt|;
specifier|protected
name|DataInputBuffer
name|inbuf
init|=
operator|new
name|DataInputBuffer
argument_list|()
decl_stmt|;
comment|/** Constructor for abstract base class */
name|HAbstractScanner
parameter_list|(
name|long
name|timestamp
parameter_list|,
name|Text
index|[]
name|targetCols
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|wildcardMatch
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|multipleMatchers
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|okCols
operator|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|Vector
argument_list|<
name|ColumnMatcher
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|targetCols
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Text
name|family
init|=
name|HStoreKey
operator|.
name|extractFamily
argument_list|(
name|targetCols
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|Vector
argument_list|<
name|ColumnMatcher
argument_list|>
name|matchers
init|=
name|okCols
operator|.
name|get
argument_list|(
name|family
argument_list|)
decl_stmt|;
if|if
condition|(
name|matchers
operator|==
literal|null
condition|)
block|{
name|matchers
operator|=
operator|new
name|Vector
argument_list|<
name|ColumnMatcher
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|ColumnMatcher
name|matcher
init|=
operator|new
name|ColumnMatcher
argument_list|(
name|targetCols
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|.
name|isWildCardMatch
argument_list|()
condition|)
block|{
name|this
operator|.
name|wildcardMatch
operator|=
literal|true
expr_stmt|;
block|}
name|matchers
operator|.
name|add
argument_list|(
name|matcher
argument_list|)
expr_stmt|;
if|if
condition|(
name|matchers
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
name|this
operator|.
name|multipleMatchers
operator|=
literal|true
expr_stmt|;
block|}
name|okCols
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|matchers
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * For a particular column i, find all the matchers defined for the column.    * Compare the column family and column key using the matchers. The first one    * that matches returns true. If no matchers are successful, return false.    *     * @param i index into the keys array    * @return true  - if any of the matchers for the column match the column family    *                 and the column key.    *                     * @throws IOException    */
name|boolean
name|columnMatch
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
name|column
init|=
name|keys
index|[
name|i
index|]
operator|.
name|getColumn
argument_list|()
decl_stmt|;
name|Text
name|family
init|=
name|HStoreKey
operator|.
name|extractFamily
argument_list|(
name|column
argument_list|)
decl_stmt|;
name|Vector
argument_list|<
name|ColumnMatcher
argument_list|>
name|matchers
init|=
name|okCols
operator|.
name|get
argument_list|(
name|family
argument_list|)
decl_stmt|;
if|if
condition|(
name|matchers
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|int
name|m
init|=
literal|0
init|;
name|m
operator|<
name|matchers
operator|.
name|size
argument_list|()
condition|;
name|m
operator|++
control|)
block|{
if|if
condition|(
name|matchers
operator|.
name|get
argument_list|(
name|m
argument_list|)
operator|.
name|matches
argument_list|(
name|column
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * If the user didn't want to start scanning at the first row, this method    * seeks to the requested row.    */
specifier|abstract
name|boolean
name|findFirstRow
parameter_list|(
name|int
name|i
parameter_list|,
name|Text
name|firstRow
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/** The concrete implementations provide a mechanism to find the next set of values */
specifier|abstract
name|boolean
name|getNext
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/** Mechanism used by concrete implementation to shut down a particular scanner */
specifier|abstract
name|void
name|closeSubScanner
parameter_list|(
name|int
name|i
parameter_list|)
function_decl|;
comment|/** {@inheritDoc} */
specifier|public
name|boolean
name|isWildcardScanner
parameter_list|()
block|{
return|return
name|this
operator|.
name|wildcardMatch
return|;
block|}
comment|/** {@inheritDoc} */
specifier|public
name|boolean
name|isMultipleMatchScanner
parameter_list|()
block|{
return|return
name|this
operator|.
name|multipleMatchers
return|;
block|}
comment|/**    * Get the next set of values for this scanner.    *     * @param key The key that matched    * @param results All the results for<code>key</code>    * @return true if a match was found    * @throws IOException    *     * @see org.apache.hadoop.hbase.HScannerInterface#next(org.apache.hadoop.hbase.HStoreKey, java.util.SortedMap)    */
specifier|public
name|boolean
name|next
parameter_list|(
name|HStoreKey
name|key
parameter_list|,
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Find the next row label (and timestamp)
name|Text
name|chosenRow
init|=
literal|null
decl_stmt|;
name|long
name|chosenTimestamp
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|(
name|keys
index|[
name|i
index|]
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|columnMatch
argument_list|(
name|i
argument_list|)
operator|)
operator|&&
operator|(
name|keys
index|[
name|i
index|]
operator|.
name|getTimestamp
argument_list|()
operator|<=
name|this
operator|.
name|timestamp
operator|)
operator|&&
operator|(
operator|(
name|chosenRow
operator|==
literal|null
operator|)
operator|||
operator|(
name|keys
index|[
name|i
index|]
operator|.
name|getRow
argument_list|()
operator|.
name|compareTo
argument_list|(
name|chosenRow
argument_list|)
operator|<
literal|0
operator|)
operator|||
operator|(
operator|(
name|keys
index|[
name|i
index|]
operator|.
name|getRow
argument_list|()
operator|.
name|compareTo
argument_list|(
name|chosenRow
argument_list|)
operator|==
literal|0
operator|)
operator|&&
operator|(
name|keys
index|[
name|i
index|]
operator|.
name|getTimestamp
argument_list|()
operator|>
name|chosenTimestamp
operator|)
operator|)
operator|)
condition|)
block|{
name|chosenRow
operator|=
operator|new
name|Text
argument_list|(
name|keys
index|[
name|i
index|]
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|chosenTimestamp
operator|=
name|keys
index|[
name|i
index|]
operator|.
name|getTimestamp
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Grab all the values that match this row/timestamp
name|boolean
name|insertedItem
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|chosenRow
operator|!=
literal|null
condition|)
block|{
name|key
operator|.
name|setRow
argument_list|(
name|chosenRow
argument_list|)
expr_stmt|;
name|key
operator|.
name|setVersion
argument_list|(
name|chosenTimestamp
argument_list|)
expr_stmt|;
name|key
operator|.
name|setColumn
argument_list|(
operator|new
name|Text
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// Fetch the data
while|while
condition|(
operator|(
name|keys
index|[
name|i
index|]
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|keys
index|[
name|i
index|]
operator|.
name|getRow
argument_list|()
operator|.
name|compareTo
argument_list|(
name|chosenRow
argument_list|)
operator|==
literal|0
operator|)
condition|)
block|{
comment|// If we are doing a wild card match or there are multiple matchers
comment|// per column, we need to scan all the older versions of this row
comment|// to pick up the rest of the family members
if|if
condition|(
operator|!
name|wildcardMatch
operator|&&
operator|!
name|multipleMatchers
operator|&&
operator|(
name|keys
index|[
name|i
index|]
operator|.
name|getTimestamp
argument_list|()
operator|!=
name|chosenTimestamp
operator|)
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|columnMatch
argument_list|(
name|i
argument_list|)
condition|)
block|{
comment|// We only want the first result for any specific family member
if|if
condition|(
operator|!
name|results
operator|.
name|containsKey
argument_list|(
name|keys
index|[
name|i
index|]
operator|.
name|getColumn
argument_list|()
argument_list|)
condition|)
block|{
name|results
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
name|keys
index|[
name|i
index|]
operator|.
name|getColumn
argument_list|()
argument_list|)
argument_list|,
name|vals
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|insertedItem
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|getNext
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|closeSubScanner
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Advance the current scanner beyond the chosen row, to
comment|// a valid timestamp, so we're ready next time.
while|while
condition|(
operator|(
name|keys
index|[
name|i
index|]
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|(
name|keys
index|[
name|i
index|]
operator|.
name|getRow
argument_list|()
operator|.
name|compareTo
argument_list|(
name|chosenRow
argument_list|)
operator|<=
literal|0
operator|)
operator|||
operator|(
name|keys
index|[
name|i
index|]
operator|.
name|getTimestamp
argument_list|()
operator|>
name|this
operator|.
name|timestamp
operator|)
operator|||
operator|(
operator|!
name|columnMatch
argument_list|(
name|i
argument_list|)
operator|)
operator|)
condition|)
block|{
name|getNext
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|insertedItem
return|;
block|}
specifier|public
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|HStoreKey
argument_list|,
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|>
name|iterator
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unimplemented serverside. "
operator|+
literal|"next(HStoreKey, StortedMap(...) is more efficient"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

