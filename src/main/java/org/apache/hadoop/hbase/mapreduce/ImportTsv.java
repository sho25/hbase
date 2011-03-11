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
name|hbase
operator|.
name|util
operator|.
name|Base64
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
name|ArrayList
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
name|fs
operator|.
name|Path
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
name|HBaseConfiguration
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
name|HTable
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
name|Job
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
name|lib
operator|.
name|input
operator|.
name|FileInputFormat
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
name|lib
operator|.
name|input
operator|.
name|TextInputFormat
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
name|lib
operator|.
name|output
operator|.
name|FileOutputFormat
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
name|GenericOptionsParser
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
name|Splitter
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
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Tool to import data from a TSV file.  *  * This tool is rather simplistic - it doesn't do any quoting or  * escaping, but is useful for many data loads.  *  * @see ImportTsv#usage(String)  */
end_comment

begin_class
specifier|public
class|class
name|ImportTsv
block|{
specifier|final
specifier|static
name|String
name|NAME
init|=
literal|"importtsv"
decl_stmt|;
specifier|final
specifier|static
name|String
name|SKIP_LINES_CONF_KEY
init|=
literal|"importtsv.skip.bad.lines"
decl_stmt|;
specifier|final
specifier|static
name|String
name|BULK_OUTPUT_CONF_KEY
init|=
literal|"importtsv.bulk.output"
decl_stmt|;
specifier|final
specifier|static
name|String
name|COLUMNS_CONF_KEY
init|=
literal|"importtsv.columns"
decl_stmt|;
specifier|final
specifier|static
name|String
name|SEPARATOR_CONF_KEY
init|=
literal|"importtsv.separator"
decl_stmt|;
specifier|final
specifier|static
name|String
name|DEFAULT_SEPARATOR
init|=
literal|"\t"
decl_stmt|;
specifier|static
class|class
name|TsvParser
block|{
comment|/**      * Column families and qualifiers mapped to the TSV columns      */
specifier|private
specifier|final
name|byte
index|[]
index|[]
name|families
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
index|[]
name|qualifiers
decl_stmt|;
specifier|private
specifier|final
name|byte
name|separatorByte
decl_stmt|;
specifier|private
name|int
name|rowKeyColumnIndex
decl_stmt|;
specifier|public
specifier|static
name|String
name|ROWKEY_COLUMN_SPEC
init|=
literal|"HBASE_ROW_KEY"
decl_stmt|;
comment|/**      * @param columnsSpecification the list of columns to parser out, comma separated.      * The row key should be the special token TsvParser.ROWKEY_COLUMN_SPEC      */
specifier|public
name|TsvParser
parameter_list|(
name|String
name|columnsSpecification
parameter_list|,
name|String
name|separatorStr
parameter_list|)
block|{
comment|// Configure separator
name|byte
index|[]
name|separator
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|separatorStr
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|separator
operator|.
name|length
operator|==
literal|1
argument_list|,
literal|"TsvParser only supports single-byte separators"
argument_list|)
expr_stmt|;
name|separatorByte
operator|=
name|separator
index|[
literal|0
index|]
expr_stmt|;
comment|// Configure columns
name|ArrayList
argument_list|<
name|String
argument_list|>
name|columnStrings
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|Splitter
operator|.
name|on
argument_list|(
literal|','
argument_list|)
operator|.
name|trimResults
argument_list|()
operator|.
name|split
argument_list|(
name|columnsSpecification
argument_list|)
argument_list|)
decl_stmt|;
name|families
operator|=
operator|new
name|byte
index|[
name|columnStrings
operator|.
name|size
argument_list|()
index|]
index|[]
expr_stmt|;
name|qualifiers
operator|=
operator|new
name|byte
index|[
name|columnStrings
operator|.
name|size
argument_list|()
index|]
index|[]
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
name|columnStrings
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|str
init|=
name|columnStrings
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|ROWKEY_COLUMN_SPEC
operator|.
name|equals
argument_list|(
name|str
argument_list|)
condition|)
block|{
name|rowKeyColumnIndex
operator|=
name|i
expr_stmt|;
continue|continue;
block|}
name|String
index|[]
name|parts
init|=
name|str
operator|.
name|split
argument_list|(
literal|":"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
if|if
condition|(
name|parts
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|families
index|[
name|i
index|]
operator|=
name|str
operator|.
name|getBytes
argument_list|()
expr_stmt|;
name|qualifiers
index|[
name|i
index|]
operator|=
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
expr_stmt|;
block|}
else|else
block|{
name|families
index|[
name|i
index|]
operator|=
name|parts
index|[
literal|0
index|]
operator|.
name|getBytes
argument_list|()
expr_stmt|;
name|qualifiers
index|[
name|i
index|]
operator|=
name|parts
index|[
literal|1
index|]
operator|.
name|getBytes
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|int
name|getRowKeyColumnIndex
parameter_list|()
block|{
return|return
name|rowKeyColumnIndex
return|;
block|}
specifier|public
name|byte
index|[]
name|getFamily
parameter_list|(
name|int
name|idx
parameter_list|)
block|{
return|return
name|families
index|[
name|idx
index|]
return|;
block|}
specifier|public
name|byte
index|[]
name|getQualifier
parameter_list|(
name|int
name|idx
parameter_list|)
block|{
return|return
name|qualifiers
index|[
name|idx
index|]
return|;
block|}
specifier|public
name|ParsedLine
name|parse
parameter_list|(
name|byte
index|[]
name|lineBytes
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|BadTsvLineException
block|{
comment|// Enumerate separator offsets
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|tabOffsets
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|families
operator|.
name|length
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
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|lineBytes
index|[
name|i
index|]
operator|==
name|separatorByte
condition|)
block|{
name|tabOffsets
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|tabOffsets
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|BadTsvLineException
argument_list|(
literal|"No delimiter"
argument_list|)
throw|;
block|}
name|tabOffsets
operator|.
name|add
argument_list|(
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|tabOffsets
operator|.
name|size
argument_list|()
operator|>
name|families
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|BadTsvLineException
argument_list|(
literal|"Excessive columns"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|tabOffsets
operator|.
name|size
argument_list|()
operator|<=
name|getRowKeyColumnIndex
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|BadTsvLineException
argument_list|(
literal|"No row key"
argument_list|)
throw|;
block|}
return|return
operator|new
name|ParsedLine
argument_list|(
name|tabOffsets
argument_list|,
name|lineBytes
argument_list|)
return|;
block|}
class|class
name|ParsedLine
block|{
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|tabOffsets
decl_stmt|;
specifier|private
name|byte
index|[]
name|lineBytes
decl_stmt|;
name|ParsedLine
parameter_list|(
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|tabOffsets
parameter_list|,
name|byte
index|[]
name|lineBytes
parameter_list|)
block|{
name|this
operator|.
name|tabOffsets
operator|=
name|tabOffsets
expr_stmt|;
name|this
operator|.
name|lineBytes
operator|=
name|lineBytes
expr_stmt|;
block|}
specifier|public
name|int
name|getRowKeyOffset
parameter_list|()
block|{
return|return
name|getColumnOffset
argument_list|(
name|rowKeyColumnIndex
argument_list|)
return|;
block|}
specifier|public
name|int
name|getRowKeyLength
parameter_list|()
block|{
return|return
name|getColumnLength
argument_list|(
name|rowKeyColumnIndex
argument_list|)
return|;
block|}
specifier|public
name|int
name|getColumnOffset
parameter_list|(
name|int
name|idx
parameter_list|)
block|{
if|if
condition|(
name|idx
operator|>
literal|0
condition|)
return|return
name|tabOffsets
operator|.
name|get
argument_list|(
name|idx
operator|-
literal|1
argument_list|)
operator|+
literal|1
return|;
else|else
return|return
literal|0
return|;
block|}
specifier|public
name|int
name|getColumnLength
parameter_list|(
name|int
name|idx
parameter_list|)
block|{
return|return
name|tabOffsets
operator|.
name|get
argument_list|(
name|idx
argument_list|)
operator|-
name|getColumnOffset
argument_list|(
name|idx
argument_list|)
return|;
block|}
specifier|public
name|int
name|getColumnCount
parameter_list|()
block|{
return|return
name|tabOffsets
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|byte
index|[]
name|getLineBytes
parameter_list|()
block|{
return|return
name|lineBytes
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|BadTsvLineException
extends|extends
name|Exception
block|{
specifier|public
name|BadTsvLineException
parameter_list|(
name|String
name|err
parameter_list|)
block|{
name|super
argument_list|(
name|err
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
block|}
block|}
comment|/**    * Write table content out to files in hdfs.    */
specifier|static
class|class
name|TsvImporter
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
specifier|private
name|long
name|ts
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
name|TsvParser
name|parser
decl_stmt|;
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
name|String
name|separator
init|=
name|conf
operator|.
name|get
argument_list|(
name|SEPARATOR_CONF_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|separator
operator|==
literal|null
condition|)
block|{
name|separator
operator|=
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
name|parser
operator|=
operator|new
name|TsvParser
argument_list|(
name|conf
operator|.
name|get
argument_list|(
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
name|ts
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
comment|/**      * Convert a line of TSV text into an HBase table row.      */
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
condition|)
continue|continue;
name|KeyValue
name|kv
init|=
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
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|kv
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
name|badLineCount
operator|.
name|increment
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
block|}
comment|/**    * Sets up the actual job.    *    * @param conf  The current configuration.    * @param args  The command line parameters.    * @return The newly created job.    * @throws IOException When setting up the job fails.    */
specifier|public
specifier|static
name|Job
name|createSubmittableJob
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Support non-XML supported characters
comment|// by re-encoding the passed separator as a Base64 string.
name|String
name|actualSeparator
init|=
name|conf
operator|.
name|get
argument_list|(
name|SEPARATOR_CONF_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|actualSeparator
operator|!=
literal|null
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|SEPARATOR_CONF_KEY
argument_list|,
operator|new
name|String
argument_list|(
name|Base64
operator|.
name|encodeBytes
argument_list|(
name|actualSeparator
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|tableName
init|=
name|args
index|[
literal|0
index|]
decl_stmt|;
name|Path
name|inputDir
init|=
operator|new
name|Path
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|,
name|NAME
operator|+
literal|"_"
operator|+
name|tableName
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|TsvImporter
operator|.
name|class
argument_list|)
expr_stmt|;
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
name|inputDir
argument_list|)
expr_stmt|;
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|TextInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|TsvImporter
operator|.
name|class
argument_list|)
expr_stmt|;
name|String
name|hfileOutPath
init|=
name|conf
operator|.
name|get
argument_list|(
name|BULK_OUTPUT_CONF_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|hfileOutPath
operator|!=
literal|null
condition|)
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|job
operator|.
name|setReducerClass
argument_list|(
name|PutSortReducer
operator|.
name|class
argument_list|)
expr_stmt|;
name|Path
name|outputDir
init|=
operator|new
name|Path
argument_list|(
name|hfileOutPath
argument_list|)
decl_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|job
argument_list|,
name|outputDir
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|Put
operator|.
name|class
argument_list|)
expr_stmt|;
name|HFileOutputFormat
operator|.
name|configureIncrementalLoad
argument_list|(
name|job
argument_list|,
name|table
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// No reducers.  Just write straight to table.  Call initTableReducerJob
comment|// to set up the TableOutputFormat.
name|TableMapReduceUtil
operator|.
name|initTableReducerJob
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|,
name|job
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|TableMapReduceUtil
operator|.
name|addDependencyJars
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|addDependencyJars
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
operator|.
name|class
comment|/* Guava used by TsvParser */
argument_list|)
expr_stmt|;
return|return
name|job
return|;
block|}
comment|/*    * @param errorMsg Error message.  Can be null.    */
specifier|private
specifier|static
name|void
name|usage
parameter_list|(
specifier|final
name|String
name|errorMsg
parameter_list|)
block|{
if|if
condition|(
name|errorMsg
operator|!=
literal|null
operator|&&
name|errorMsg
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"ERROR: "
operator|+
name|errorMsg
argument_list|)
expr_stmt|;
block|}
name|String
name|usage
init|=
literal|"Usage: "
operator|+
name|NAME
operator|+
literal|" -Dimporttsv.columns=a,b,c<tablename><inputdir>\n"
operator|+
literal|"\n"
operator|+
literal|"Imports the given input directory of TSV data into the specified table.\n"
operator|+
literal|"\n"
operator|+
literal|"The column names of the TSV data must be specified using the -Dimporttsv.columns\n"
operator|+
literal|"option. This option takes the form of comma-separated column names, where each\n"
operator|+
literal|"column name is either a simple column family, or a columnfamily:qualifier. The special\n"
operator|+
literal|"column name HBASE_ROW_KEY is used to designate that this column should be used\n"
operator|+
literal|"as the row key for each imported record. You must specify exactly one column\n"
operator|+
literal|"to be the row key.\n"
operator|+
literal|"\n"
operator|+
literal|"In order to prepare data for a bulk data load, pass the option:\n"
operator|+
literal|"  -D"
operator|+
name|BULK_OUTPUT_CONF_KEY
operator|+
literal|"=/path/for/output\n"
operator|+
literal|"\n"
operator|+
literal|"Other options that may be specified with -D include:\n"
operator|+
literal|"  -D"
operator|+
name|SKIP_LINES_CONF_KEY
operator|+
literal|"=false - fail if encountering an invalid line\n"
operator|+
literal|"  '-D"
operator|+
name|SEPARATOR_CONF_KEY
operator|+
literal|"=|' - eg separate on pipes instead of tabs"
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|usage
argument_list|)
expr_stmt|;
block|}
comment|/**    * Main entry point.    *    * @param args  The command line parameters.    * @throws Exception When running the job fails.    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|String
index|[]
name|otherArgs
init|=
operator|new
name|GenericOptionsParser
argument_list|(
name|conf
argument_list|,
name|args
argument_list|)
operator|.
name|getRemainingArgs
argument_list|()
decl_stmt|;
if|if
condition|(
name|otherArgs
operator|.
name|length
operator|<
literal|2
condition|)
block|{
name|usage
argument_list|(
literal|"Wrong number of arguments: "
operator|+
name|otherArgs
operator|.
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// Make sure columns are specified
name|String
name|columns
index|[]
init|=
name|conf
operator|.
name|getStrings
argument_list|(
name|COLUMNS_CONF_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|columns
operator|==
literal|null
condition|)
block|{
name|usage
argument_list|(
literal|"No columns specified. Please specify with -D"
operator|+
name|COLUMNS_CONF_KEY
operator|+
literal|"=..."
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// Make sure they specify exactly one column as the row key
name|int
name|rowkeysFound
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|col
range|:
name|columns
control|)
block|{
if|if
condition|(
name|col
operator|.
name|equals
argument_list|(
name|TsvParser
operator|.
name|ROWKEY_COLUMN_SPEC
argument_list|)
condition|)
name|rowkeysFound
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|rowkeysFound
operator|!=
literal|1
condition|)
block|{
name|usage
argument_list|(
literal|"Must specify exactly one column as "
operator|+
name|TsvParser
operator|.
name|ROWKEY_COLUMN_SPEC
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// Make sure one or more columns are specified
if|if
condition|(
name|columns
operator|.
name|length
operator|<
literal|2
condition|)
block|{
name|usage
argument_list|(
literal|"One or more columns in addition to the row key are required"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|Job
name|job
init|=
name|createSubmittableJob
argument_list|(
name|conf
argument_list|,
name|otherArgs
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
condition|?
literal|0
else|:
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

