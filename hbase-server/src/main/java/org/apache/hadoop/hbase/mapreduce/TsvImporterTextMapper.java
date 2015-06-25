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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|LongWritable
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
name|hbase
operator|.
name|util
operator|.
name|Pair
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
name|Mapper
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
name|hbase
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
name|hbase
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Write table content out to map output files.  */
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
name|TsvImporterTextMapper
extends|extends
name|Mapper
argument_list|<
name|LongWritable
argument_list|,
name|Text
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|Text
argument_list|>
block|{
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
name|boolean
name|logBadLines
decl_stmt|;
specifier|private
name|ImportTsv
operator|.
name|TsvParser
name|parser
decl_stmt|;
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
name|logBadLines
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
name|LOG_BAD_LINES_CONF_KEY
argument_list|,
literal|false
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
comment|/**    * Convert a line of TSV text into an HBase table row.    */
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
name|LongWritable
name|offset
parameter_list|,
name|Text
name|value
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|rowKeyOffests
init|=
name|parser
operator|.
name|parseRowKey
argument_list|(
name|value
operator|.
name|getBytes
argument_list|()
argument_list|,
name|value
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
name|ImmutableBytesWritable
name|rowKey
init|=
operator|new
name|ImmutableBytesWritable
argument_list|(
name|value
operator|.
name|getBytes
argument_list|()
argument_list|,
name|rowKeyOffests
operator|.
name|getFirst
argument_list|()
argument_list|,
name|rowKeyOffests
operator|.
name|getSecond
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|write
argument_list|(
name|rowKey
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ImportTsv
operator|.
name|TsvParser
operator|.
name|BadTsvLineException
decl||
name|IllegalArgumentException
name|badLine
parameter_list|)
block|{
if|if
condition|(
name|logBadLines
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Bad line at offset: "
operator|+
name|offset
operator|.
name|get
argument_list|()
operator|+
literal|":\n"
operator|+
name|badLine
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|skipBadLines
condition|)
block|{
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
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

