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
name|client
operator|.
name|Put
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
name|mapreduce
operator|.
name|ImportTsv
operator|.
name|TsvParser
operator|.
name|BadTsvLineException
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
name|security
operator|.
name|visibility
operator|.
name|CellVisibility
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
name|Mapper
import|;
end_import

begin_comment
comment|/**  * Write table content out to files in hdfs.  */
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
name|TsvImporterMapper
extends|extends
name|Mapper
argument_list|<
name|LongWritable
argument_list|,
name|Text
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|Put
argument_list|>
block|{
comment|/** Timestamp for all inserted rows */
specifier|protected
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
specifier|protected
name|ImportTsv
operator|.
name|TsvParser
name|parser
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|String
name|cellVisibilityExpr
decl_stmt|;
specifier|protected
name|CellCreator
name|kvCreator
decl_stmt|;
specifier|private
name|String
name|hfileOutPath
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
name|conf
operator|=
name|context
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
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
comment|// Should never get 0 as we are setting this to a valid value in job
comment|// configuration.
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
name|hfileOutPath
operator|=
name|conf
operator|.
name|get
argument_list|(
name|ImportTsv
operator|.
name|BULK_OUTPUT_CONF_KEY
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
name|byte
index|[]
name|lineBytes
init|=
name|value
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
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|rowKey
operator|.
name|copyBytes
argument_list|()
argument_list|)
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
name|populatePut
argument_list|(
name|lineBytes
argument_list|,
name|parsed
argument_list|,
name|put
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|write
argument_list|(
name|rowKey
argument_list|,
name|put
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
name|incrementBadLineCount
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|badLine
argument_list|)
throw|;
block|}
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
literal|"Bad line at offset: "
operator|+
name|offset
operator|.
name|get
argument_list|()
operator|+
literal|":\n"
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
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
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
block|}
block|}
specifier|protected
name|void
name|populatePut
parameter_list|(
name|byte
index|[]
name|lineBytes
parameter_list|,
name|ImportTsv
operator|.
name|TsvParser
operator|.
name|ParsedLine
name|parsed
parameter_list|,
name|Put
name|put
parameter_list|,
name|int
name|i
parameter_list|)
throws|throws
name|BadTsvLineException
throws|,
name|IOException
block|{
name|Cell
name|cell
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|hfileOutPath
operator|==
literal|null
condition|)
block|{
name|cell
operator|=
operator|new
name|KeyValue
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
name|KeyValue
operator|.
name|Type
operator|.
name|Put
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
argument_list|)
expr_stmt|;
if|if
condition|(
name|cellVisibilityExpr
operator|!=
literal|null
condition|)
block|{
comment|// We won't be validating the expression here. The Visibility CP will do
comment|// the validation
name|put
operator|.
name|setCellVisibility
argument_list|(
operator|new
name|CellVisibility
argument_list|(
name|cellVisibilityExpr
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Creating the KV which needs to be directly written to HFiles. Using the Facade
comment|// KVCreator for creation of kvs.
name|cell
operator|=
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
expr_stmt|;
block|}
name|put
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

