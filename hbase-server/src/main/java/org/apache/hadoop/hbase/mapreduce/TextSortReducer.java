begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
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
name|Set
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
name|conf
operator|.
name|Configuration
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
name|Cell
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
name|KeyValueUtil
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
name|ImmutableBytesWritable
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
name|Base64
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|Counter
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
name|mapreduce
operator|.
name|Reducer
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Emits Sorted KeyValues. Parse the passed text and creates KeyValues. Sorts them before emit.  * @see HFileOutputFormat  * @see KeyValueSortReducer  * @see PutSortReducer  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|TextSortReducer
extends|extends
name|Reducer
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Text
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|KeyValue
argument_list|>
block|{
comment|/** Timestamp for all inserted rows */
specifier|private
name|long
name|ts
decl_stmt|;
comment|/** Column seperator */
specifier|private
name|String
name|separator
decl_stmt|;
comment|/** Should skip bad lines */
specifier|private
name|boolean
name|skipBadLines
decl_stmt|;
specifier|private
name|Counter
name|badLineCount
decl_stmt|;
specifier|private
name|ImportTsv
operator|.
name|TsvParser
name|parser
decl_stmt|;
comment|/** Cell visibility expr **/
specifier|private
name|String
name|cellVisibilityExpr
decl_stmt|;
specifier|private
name|CellCreator
name|kvCreator
decl_stmt|;
specifier|public
name|long
name|getTs
parameter_list|()
block|{
return|return
name|ts
return|;
block|}
specifier|public
name|boolean
name|getSkipBadLines
parameter_list|()
block|{
return|return
name|skipBadLines
return|;
block|}
specifier|public
name|Counter
name|getBadLineCount
parameter_list|()
block|{
return|return
name|badLineCount
return|;
block|}
specifier|public
name|void
name|incrementBadLineCount
parameter_list|(
name|int
name|count
parameter_list|)
block|{
name|this
operator|.
name|badLineCount
operator|.
name|increment
argument_list|(
name|count
argument_list|)
expr_stmt|;
block|}
comment|/**    * Handles initializing this class with objects specific to it (i.e., the parser).    * Common initialization that might be leveraged by a subsclass is done in    *<code>doSetup</code>. Hence a subclass may choose to override this method    * and call<code>doSetup</code> as well before handling it's own custom params.    *    * @param context    */
annotation|@
name|Override
specifier|protected
name|void
name|setup
parameter_list|(
name|Context
name|context
parameter_list|)
block|{
name|doSetup
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|parser
operator|=
operator|new
name|ImportTsv
operator|.
name|TsvParser
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
argument_list|)
argument_list|,
name|separator
argument_list|)
expr_stmt|;
if|if
condition|(
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"No row key column specified"
argument_list|)
throw|;
block|}
name|this
operator|.
name|kvCreator
operator|=
operator|new
name|CellCreator
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Handles common parameter initialization that a subclass might want to leverage.    * @param context    */
specifier|protected
name|void
name|doSetup
parameter_list|(
name|Context
name|context
parameter_list|)
block|{
name|Configuration
name|conf
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// If a custom separator has been used,
comment|// decode it back from Base64 encoding.
name|separator
operator|=
name|conf
operator|.
name|get
argument_list|(
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
argument_list|)
expr_stmt|;
if|if
condition|(
name|separator
operator|==
literal|null
condition|)
block|{
name|separator
operator|=
name|ImportTsv
operator|.
name|DEFAULT_SEPARATOR
expr_stmt|;
block|}
else|else
block|{
name|separator
operator|=
operator|new
name|String
argument_list|(
name|Base64
operator|.
name|decode
argument_list|(
name|separator
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Should never get 0 as we are setting this to a valid value in job configuration.
name|ts
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|ImportTsv
operator|.
name|TIMESTAMP_CONF_KEY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|skipBadLines
operator|=
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getBoolean
argument_list|(
name|ImportTsv
operator|.
name|SKIP_LINES_CONF_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|badLineCount
operator|=
name|context
operator|.
name|getCounter
argument_list|(
literal|"ImportTsv"
argument_list|,
literal|"Bad Lines"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|reduce
parameter_list|(
name|ImmutableBytesWritable
name|rowKey
parameter_list|,
name|java
operator|.
name|lang
operator|.
name|Iterable
argument_list|<
name|Text
argument_list|>
name|lines
parameter_list|,
name|Reducer
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Text
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|KeyValue
argument_list|>
operator|.
name|Context
name|context
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
throws|,
name|InterruptedException
block|{
comment|// although reduce() is called per-row, handle pathological case
name|long
name|threshold
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
literal|"reducer.row.threshold"
argument_list|,
literal|1L
operator|*
operator|(
literal|1
operator|<<
literal|30
operator|)
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|Text
argument_list|>
name|iter
init|=
name|lines
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Set
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
operator|new
name|TreeSet
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
name|long
name|curSize
init|=
literal|0
decl_stmt|;
comment|// stop at the end or the RAM threshold
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
operator|&&
name|curSize
operator|<
name|threshold
condition|)
block|{
name|Text
name|line
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|byte
index|[]
name|lineBytes
init|=
name|line
operator|.
name|getBytes
argument_list|()
decl_stmt|;
try|try
block|{
name|ImportTsv
operator|.
name|TsvParser
operator|.
name|ParsedLine
name|parsed
init|=
name|parser
operator|.
name|parse
argument_list|(
name|lineBytes
argument_list|,
name|line
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
comment|// Retrieve timestamp if exists
name|ts
operator|=
name|parsed
operator|.
name|getTimestamp
argument_list|(
name|ts
argument_list|)
expr_stmt|;
name|cellVisibilityExpr
operator|=
name|parsed
operator|.
name|getCellVisibility
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
name|parsed
operator|.
name|getColumnCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
operator|||
name|i
operator|==
name|parser
operator|.
name|getTimestampKeyColumnIndex
argument_list|()
operator|||
name|i
operator|==
name|parser
operator|.
name|getAttributesKeyColumnIndex
argument_list|()
operator|||
name|i
operator|==
name|parser
operator|.
name|getCellVisibilityColumnIndex
argument_list|()
condition|)
block|{
continue|continue;
block|}
comment|// Creating the KV which needs to be directly written to HFiles. Using the Facade
comment|// KVCreator for creation of kvs.
name|Cell
name|cell
init|=
name|this
operator|.
name|kvCreator
operator|.
name|create
argument_list|(
name|lineBytes
argument_list|,
name|parsed
operator|.
name|getRowKeyOffset
argument_list|()
argument_list|,
name|parsed
operator|.
name|getRowKeyLength
argument_list|()
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
name|i
argument_list|)
argument_list|,
literal|0
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
name|i
argument_list|)
operator|.
name|length
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
name|i
argument_list|)
argument_list|,
literal|0
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
name|i
argument_list|)
operator|.
name|length
argument_list|,
name|ts
argument_list|,
name|lineBytes
argument_list|,
name|parsed
operator|.
name|getColumnOffset
argument_list|(
name|i
argument_list|)
argument_list|,
name|parsed
operator|.
name|getColumnLength
argument_list|(
name|i
argument_list|)
argument_list|,
name|cellVisibilityExpr
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|kvs
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|curSize
operator|+=
name|kv
operator|.
name|heapSize
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|ImportTsv
operator|.
name|TsvParser
operator|.
name|BadTsvLineException
name|badLine
parameter_list|)
block|{
if|if
condition|(
name|skipBadLines
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Bad line."
operator|+
name|badLine
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|incrementBadLineCount
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
name|badLine
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
if|if
condition|(
name|skipBadLines
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Bad line."
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|incrementBadLineCount
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
name|context
operator|.
name|setStatus
argument_list|(
literal|"Read "
operator|+
name|kvs
operator|.
name|size
argument_list|()
operator|+
literal|" entries of "
operator|+
name|kvs
operator|.
name|getClass
argument_list|()
operator|+
literal|"("
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|curSize
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
name|context
operator|.
name|write
argument_list|(
name|rowKey
argument_list|,
name|kv
argument_list|)
expr_stmt|;
if|if
condition|(
operator|++
name|index
operator|>
literal|0
operator|&&
name|index
operator|%
literal|100
operator|==
literal|0
condition|)
name|context
operator|.
name|setStatus
argument_list|(
literal|"Wrote "
operator|+
name|index
operator|+
literal|" key values."
argument_list|)
expr_stmt|;
block|}
comment|// if we have more entries to process
if|if
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
comment|// force flush because we cannot guarantee intra-row sorted order
name|context
operator|.
name|write
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

