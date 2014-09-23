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
name|Configurable
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Convert HBase tabular data into a format that is consumable by Map/Reduce.  */
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
name|TableInputFormat
extends|extends
name|TableInputFormatBase
implements|implements
name|Configurable
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
name|TableInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Job parameter that specifies the input table. */
specifier|public
specifier|static
specifier|final
name|String
name|INPUT_TABLE
init|=
literal|"hbase.mapreduce.inputtable"
decl_stmt|;
comment|/** Base-64 encoded scanner. All other SCAN_ confs are ignored if this is specified.    * See {@link TableMapReduceUtil#convertScanToString(Scan)} for more details.    */
specifier|public
specifier|static
specifier|final
name|String
name|SCAN
init|=
literal|"hbase.mapreduce.scan"
decl_stmt|;
comment|/** Scan start row */
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_ROW_START
init|=
literal|"hbase.mapreduce.scan.row.start"
decl_stmt|;
comment|/** Scan stop row */
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_ROW_STOP
init|=
literal|"hbase.mapreduce.scan.row.stop"
decl_stmt|;
comment|/** Column Family to Scan */
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_COLUMN_FAMILY
init|=
literal|"hbase.mapreduce.scan.column.family"
decl_stmt|;
comment|/** Space delimited list of columns and column families to scan. */
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_COLUMNS
init|=
literal|"hbase.mapreduce.scan.columns"
decl_stmt|;
comment|/** The timestamp used to filter columns with a specific timestamp. */
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_TIMESTAMP
init|=
literal|"hbase.mapreduce.scan.timestamp"
decl_stmt|;
comment|/** The starting timestamp used to filter columns with a specific range of versions. */
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_TIMERANGE_START
init|=
literal|"hbase.mapreduce.scan.timerange.start"
decl_stmt|;
comment|/** The ending timestamp used to filter columns with a specific range of versions. */
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_TIMERANGE_END
init|=
literal|"hbase.mapreduce.scan.timerange.end"
decl_stmt|;
comment|/** The maximum number of version to return. */
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_MAXVERSIONS
init|=
literal|"hbase.mapreduce.scan.maxversions"
decl_stmt|;
comment|/** Set to false to disable server-side caching of blocks for this scan. */
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_CACHEBLOCKS
init|=
literal|"hbase.mapreduce.scan.cacheblocks"
decl_stmt|;
comment|/** The number of rows for caching that will be passed to scanners. */
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_CACHEDROWS
init|=
literal|"hbase.mapreduce.scan.cachedrows"
decl_stmt|;
comment|/** Set the maximum number of values to return for each call to next(). */
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_BATCHSIZE
init|=
literal|"hbase.mapreduce.scan.batchsize"
decl_stmt|;
comment|/** The configuration. */
specifier|private
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
comment|/**    * Returns the current configuration.    *    * @return The current configuration.    * @see org.apache.hadoop.conf.Configurable#getConf()    */
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
comment|/**    * Sets the configuration. This is used to set the details for the table to    * be scanned.    *    * @param configuration  The configuration to set.    * @see org.apache.hadoop.conf.Configurable#setConf(    *   org.apache.hadoop.conf.Configuration)    */
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|configuration
expr_stmt|;
name|String
name|tableName
init|=
name|conf
operator|.
name|get
argument_list|(
name|INPUT_TABLE
argument_list|)
decl_stmt|;
try|try
block|{
name|setHTable
argument_list|(
operator|new
name|HTable
argument_list|(
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Scan
name|scan
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|SCAN
argument_list|)
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|scan
operator|=
name|TableMapReduceUtil
operator|.
name|convertStringToScan
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|SCAN
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"An error occurred."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
try|try
block|{
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|SCAN_ROW_START
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setStartRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|conf
operator|.
name|get
argument_list|(
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
name|SCAN_ROW_STOP
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setStopRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|SCAN_ROW_STOP
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
name|SCAN_COLUMNS
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|addColumns
argument_list|(
name|scan
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|SCAN_COLUMNS
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
name|SCAN_COLUMN_FAMILY
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|SCAN_COLUMN_FAMILY
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
name|SCAN_TIMESTAMP
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setTimeStamp
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|SCAN_TIMESTAMP
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
name|SCAN_TIMERANGE_START
argument_list|)
operator|!=
literal|null
operator|&&
name|conf
operator|.
name|get
argument_list|(
name|SCAN_TIMERANGE_END
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setTimeRange
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|SCAN_TIMERANGE_START
argument_list|)
argument_list|)
argument_list|,
name|Long
operator|.
name|parseLong
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|SCAN_TIMERANGE_END
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
name|SCAN_MAXVERSIONS
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|SCAN_MAXVERSIONS
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
name|SCAN_CACHEDROWS
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setCaching
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|SCAN_CACHEDROWS
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
name|SCAN_BATCHSIZE
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setBatch
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|SCAN_BATCHSIZE
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// false by default, full table scans generate too much BC churn
name|scan
operator|.
name|setCacheBlocks
argument_list|(
operator|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|SCAN_CACHEBLOCKS
argument_list|,
literal|false
argument_list|)
operator|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|setScan
argument_list|(
name|scan
argument_list|)
expr_stmt|;
block|}
comment|/**    * Parses a combined family and qualifier and adds either both or just the    * family in case there is no qualifier. This assumes the older colon    * divided notation, e.g. "family:qualifier".    *    * @param scan The Scan to update.    * @param familyAndQualifier family and qualifier    * @return A reference to this instance.    * @throws IllegalArgumentException When familyAndQualifier is invalid.    */
specifier|private
specifier|static
name|void
name|addColumn
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|byte
index|[]
name|familyAndQualifier
parameter_list|)
block|{
name|byte
index|[]
index|[]
name|fq
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|familyAndQualifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|fq
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|scan
operator|.
name|addFamily
argument_list|(
name|fq
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fq
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|scan
operator|.
name|addColumn
argument_list|(
name|fq
index|[
literal|0
index|]
argument_list|,
name|fq
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid familyAndQualifier provided."
argument_list|)
throw|;
block|}
block|}
comment|/**    * Adds an array of columns specified using old format, family:qualifier.    *<p>    * Overrides previous calls to {@link Scan#addColumn(byte[], byte[])}for any families in the    * input.    *    * @param scan The Scan to update.    * @param columns array of columns, formatted as<code>family:qualifier</code>    * @see Scan#addColumn(byte[], byte[])    */
specifier|public
specifier|static
name|void
name|addColumns
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|byte
index|[]
index|[]
name|columns
parameter_list|)
block|{
for|for
control|(
name|byte
index|[]
name|column
range|:
name|columns
control|)
block|{
name|addColumn
argument_list|(
name|scan
argument_list|,
name|column
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Convenience method to parse a string representation of an array of column specifiers.    *    * @param scan The Scan to update.    * @param columns  The columns to parse.    */
specifier|private
specifier|static
name|void
name|addColumns
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|String
name|columns
parameter_list|)
block|{
name|String
index|[]
name|cols
init|=
name|columns
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|col
range|:
name|cols
control|)
block|{
name|addColumn
argument_list|(
name|scan
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|col
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

