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
name|Arrays
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
name|CompareOperator
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
name|TableName
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
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Scan
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
name|filter
operator|.
name|CompareFilter
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
name|filter
operator|.
name|Filter
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
name|filter
operator|.
name|IncompatibleFilterException
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
name|filter
operator|.
name|PrefixFilter
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
name|filter
operator|.
name|RegexStringComparator
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
name|filter
operator|.
name|RowFilter
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
name|Authorizations
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
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|hbase
operator|.
name|util
operator|.
name|Triple
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

begin_comment
comment|/**  * Some helper methods are used by {@link org.apache.hadoop.hbase.mapreduce.Export}  * and org.apache.hadoop.hbase.coprocessor.Export (in hbase-endpooint).  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ExportUtils
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
name|ExportUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RAW_SCAN
init|=
literal|"hbase.mapreduce.include.deleted.rows"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXPORT_BATCHING
init|=
literal|"hbase.export.scanner.batch"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXPORT_CACHING
init|=
literal|"hbase.export.scanner.caching"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXPORT_VISIBILITY_LABELS
init|=
literal|"hbase.export.visibility.labels"
decl_stmt|;
comment|/**    * Common usage for other export tools.    * @param errorMsg Error message.  Can be null.    */
specifier|public
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
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage: Export [-D<property=value>]*<tablename><outputdir> [<versions> "
operator|+
literal|"[<starttime> [<endtime>]] [^[regex pattern] or [Prefix] to filter]]\n"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  Note: -D properties will be applied to the conf used. "
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  For example: "
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|FileOutputFormat
operator|.
name|COMPRESS
operator|+
literal|"=true"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|FileOutputFormat
operator|.
name|COMPRESS_CODEC
operator|+
literal|"=org.apache.hadoop.io.compress.GzipCodec"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|FileOutputFormat
operator|.
name|COMPRESS_TYPE
operator|+
literal|"=BLOCK"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  Additionally, the following SCAN properties can be specified"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  to control/limit what is exported.."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|TableInputFormat
operator|.
name|SCAN_COLUMN_FAMILY
operator|+
literal|"=<family1>,<family2>, ..."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|RAW_SCAN
operator|+
literal|"=true"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|TableInputFormat
operator|.
name|SCAN_ROW_START
operator|+
literal|"=<ROWSTART>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|TableInputFormat
operator|.
name|SCAN_ROW_STOP
operator|+
literal|"=<ROWSTOP>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_CACHING
operator|+
literal|"=100"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|EXPORT_VISIBILITY_LABELS
operator|+
literal|"=<labels>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"For tables with very wide rows consider setting the batch size as below:\n"
operator|+
literal|"   -D "
operator|+
name|EXPORT_BATCHING
operator|+
literal|"=10\n"
operator|+
literal|"   -D "
operator|+
name|EXPORT_CACHING
operator|+
literal|"=100"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|Filter
name|getExportFilter
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|Filter
name|exportFilter
decl_stmt|;
name|String
name|filterCriteria
init|=
operator|(
name|args
operator|.
name|length
operator|>
literal|5
operator|)
condition|?
name|args
index|[
literal|5
index|]
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|filterCriteria
operator|==
literal|null
condition|)
return|return
literal|null
return|;
if|if
condition|(
name|filterCriteria
operator|.
name|startsWith
argument_list|(
literal|"^"
argument_list|)
condition|)
block|{
name|String
name|regexPattern
init|=
name|filterCriteria
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|filterCriteria
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|exportFilter
operator|=
operator|new
name|RowFilter
argument_list|(
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|RegexStringComparator
argument_list|(
name|regexPattern
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|exportFilter
operator|=
operator|new
name|PrefixFilter
argument_list|(
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
name|filterCriteria
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|exportFilter
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isValidArguements
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
return|return
name|args
operator|!=
literal|null
operator|&&
name|args
operator|.
name|length
operator|>=
literal|2
return|;
block|}
specifier|public
specifier|static
name|Triple
argument_list|<
name|TableName
argument_list|,
name|Scan
argument_list|,
name|Path
argument_list|>
name|getArgumentsFromCommandLine
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
if|if
condition|(
operator|!
name|isValidArguements
argument_list|(
name|args
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|Triple
argument_list|<>
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|getScanFromCommandLine
argument_list|(
name|conf
argument_list|,
name|args
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|static
name|Scan
name|getScanFromCommandLine
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
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
comment|// Optional arguments.
comment|// Set Scan Versions
name|int
name|versions
init|=
name|args
operator|.
name|length
operator|>
literal|2
condition|?
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|2
index|]
argument_list|)
else|:
literal|1
decl_stmt|;
name|s
operator|.
name|setMaxVersions
argument_list|(
name|versions
argument_list|)
expr_stmt|;
comment|// Set Scan Range
name|long
name|startTime
init|=
name|args
operator|.
name|length
operator|>
literal|3
condition|?
name|Long
operator|.
name|parseLong
argument_list|(
name|args
index|[
literal|3
index|]
argument_list|)
else|:
literal|0L
decl_stmt|;
name|long
name|endTime
init|=
name|args
operator|.
name|length
operator|>
literal|4
condition|?
name|Long
operator|.
name|parseLong
argument_list|(
name|args
index|[
literal|4
index|]
argument_list|)
else|:
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|s
operator|.
name|setTimeRange
argument_list|(
name|startTime
argument_list|,
name|endTime
argument_list|)
expr_stmt|;
comment|// Set cache blocks
name|s
operator|.
name|setCacheBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// set Start and Stop row
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|TableInputFormat
operator|.
name|SCAN_ROW_START
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|s
operator|.
name|setStartRow
argument_list|(
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|TableInputFormat
operator|.
name|SCAN_ROW_START
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|TableInputFormat
operator|.
name|SCAN_ROW_STOP
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|s
operator|.
name|setStopRow
argument_list|(
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|TableInputFormat
operator|.
name|SCAN_ROW_STOP
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Set Scan Column Family
name|boolean
name|raw
init|=
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|RAW_SCAN
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|raw
condition|)
block|{
name|s
operator|.
name|setRaw
argument_list|(
name|raw
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|columnFamily
range|:
name|conf
operator|.
name|getTrimmedStrings
argument_list|(
name|TableInputFormat
operator|.
name|SCAN_COLUMN_FAMILY
argument_list|)
control|)
block|{
name|s
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columnFamily
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Set RowFilter or Prefix Filter if applicable.
name|Filter
name|exportFilter
init|=
name|getExportFilter
argument_list|(
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|exportFilter
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting Scan Filter for Export."
argument_list|)
expr_stmt|;
name|s
operator|.
name|setFilter
argument_list|(
name|exportFilter
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|labels
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|EXPORT_VISIBILITY_LABELS
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|labels
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|conf
operator|.
name|getStrings
argument_list|(
name|EXPORT_VISIBILITY_LABELS
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|labels
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|s
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
name|labels
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|batching
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|EXPORT_BATCHING
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|batching
operator|!=
operator|-
literal|1
condition|)
block|{
try|try
block|{
name|s
operator|.
name|setBatch
argument_list|(
name|batching
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IncompatibleFilterException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Batching could not be set"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|caching
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|EXPORT_CACHING
argument_list|,
literal|100
argument_list|)
decl_stmt|;
if|if
condition|(
name|caching
operator|!=
operator|-
literal|1
condition|)
block|{
try|try
block|{
name|s
operator|.
name|setCaching
argument_list|(
name|caching
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IncompatibleFilterException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caching could not be set"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"versions="
operator|+
name|versions
operator|+
literal|", starttime="
operator|+
name|startTime
operator|+
literal|", endtime="
operator|+
name|endTime
operator|+
literal|", keepDeletedCells="
operator|+
name|raw
operator|+
literal|", visibility labels="
operator|+
name|labels
argument_list|)
expr_stmt|;
return|return
name|s
return|;
block|}
specifier|private
name|ExportUtils
parameter_list|()
block|{   }
block|}
end_class

end_unit

