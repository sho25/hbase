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
name|IOException
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
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|DefaultValue
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|Encoded
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|HeaderParam
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|PathParam
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|QueryParam
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|core
operator|.
name|Context
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|core
operator|.
name|UriInfo
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
name|lang
operator|.
name|StringUtils
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
name|client
operator|.
name|Table
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
name|FilterList
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
name|ParseFilter
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TableResource
extends|extends
name|ResourceBase
block|{
name|String
name|table
decl_stmt|;
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
name|TableResource
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Constructor    * @param table    * @throws IOException    */
specifier|public
name|TableResource
parameter_list|(
name|String
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
comment|/** @return the table name */
name|String
name|getName
parameter_list|()
block|{
return|return
name|table
return|;
block|}
comment|/**    * @return true if the table exists    * @throws IOException    */
name|boolean
name|exists
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|servlet
operator|.
name|getAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"exists"
argument_list|)
specifier|public
name|ExistsResource
name|getExistsResource
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|ExistsResource
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"regions"
argument_list|)
specifier|public
name|RegionsResource
name|getRegionsResource
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|RegionsResource
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"scanner"
argument_list|)
specifier|public
name|ScannerResource
name|getScannerResource
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|ScannerResource
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"schema"
argument_list|)
specifier|public
name|SchemaResource
name|getSchemaResource
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|SchemaResource
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"{multiget: multiget.*}"
argument_list|)
specifier|public
name|MultiRowResource
name|getMultipleRowResource
parameter_list|(
specifier|final
annotation|@
name|QueryParam
argument_list|(
literal|"v"
argument_list|)
name|String
name|versions
parameter_list|,
annotation|@
name|PathParam
argument_list|(
literal|"multiget"
argument_list|)
name|String
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|MultiRowResource
argument_list|(
name|this
argument_list|,
name|versions
argument_list|,
name|path
operator|.
name|replace
argument_list|(
literal|"multiget"
argument_list|,
literal|""
argument_list|)
operator|.
name|replace
argument_list|(
literal|"/"
argument_list|,
literal|""
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"{rowspec: [^*]+}"
argument_list|)
specifier|public
name|RowResource
name|getRowResource
parameter_list|(
comment|// We need the @Encoded decorator so Jersey won't urldecode before
comment|// the RowSpec constructor has a chance to parse
specifier|final
annotation|@
name|PathParam
argument_list|(
literal|"rowspec"
argument_list|)
annotation|@
name|Encoded
name|String
name|rowspec
parameter_list|,
specifier|final
annotation|@
name|QueryParam
argument_list|(
literal|"v"
argument_list|)
name|String
name|versions
parameter_list|,
specifier|final
annotation|@
name|QueryParam
argument_list|(
literal|"check"
argument_list|)
name|String
name|check
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|RowResource
argument_list|(
name|this
argument_list|,
name|rowspec
argument_list|,
name|versions
argument_list|,
name|check
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"{suffixglobbingspec: .*\\*/.+}"
argument_list|)
specifier|public
name|RowResource
name|getRowResourceWithSuffixGlobbing
parameter_list|(
comment|// We need the @Encoded decorator so Jersey won't urldecode before
comment|// the RowSpec constructor has a chance to parse
specifier|final
annotation|@
name|PathParam
argument_list|(
literal|"suffixglobbingspec"
argument_list|)
annotation|@
name|Encoded
name|String
name|suffixglobbingspec
parameter_list|,
specifier|final
annotation|@
name|QueryParam
argument_list|(
literal|"v"
argument_list|)
name|String
name|versions
parameter_list|,
specifier|final
annotation|@
name|QueryParam
argument_list|(
literal|"check"
argument_list|)
name|String
name|check
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|RowResource
argument_list|(
name|this
argument_list|,
name|suffixglobbingspec
argument_list|,
name|versions
argument_list|,
name|check
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"{scanspec: .*[*]$}"
argument_list|)
specifier|public
name|TableScanResource
name|getScanResource
parameter_list|(
specifier|final
annotation|@
name|Context
name|UriInfo
name|uriInfo
parameter_list|,
specifier|final
annotation|@
name|PathParam
argument_list|(
literal|"scanspec"
argument_list|)
name|String
name|scanSpec
parameter_list|,
specifier|final
annotation|@
name|HeaderParam
argument_list|(
literal|"Accept"
argument_list|)
name|String
name|contentType
parameter_list|,
annotation|@
name|DefaultValue
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
operator|+
literal|""
argument_list|)
annotation|@
name|QueryParam
argument_list|(
name|Constants
operator|.
name|SCAN_LIMIT
argument_list|)
name|int
name|userRequestedLimit
parameter_list|,
annotation|@
name|DefaultValue
argument_list|(
literal|""
argument_list|)
annotation|@
name|QueryParam
argument_list|(
name|Constants
operator|.
name|SCAN_START_ROW
argument_list|)
name|String
name|startRow
parameter_list|,
annotation|@
name|DefaultValue
argument_list|(
literal|""
argument_list|)
annotation|@
name|QueryParam
argument_list|(
name|Constants
operator|.
name|SCAN_END_ROW
argument_list|)
name|String
name|endRow
parameter_list|,
annotation|@
name|DefaultValue
argument_list|(
literal|""
argument_list|)
annotation|@
name|QueryParam
argument_list|(
name|Constants
operator|.
name|SCAN_COLUMN
argument_list|)
name|List
argument_list|<
name|String
argument_list|>
name|column
parameter_list|,
annotation|@
name|DefaultValue
argument_list|(
literal|"1"
argument_list|)
annotation|@
name|QueryParam
argument_list|(
name|Constants
operator|.
name|SCAN_MAX_VERSIONS
argument_list|)
name|int
name|maxVersions
parameter_list|,
annotation|@
name|DefaultValue
argument_list|(
literal|"-1"
argument_list|)
annotation|@
name|QueryParam
argument_list|(
name|Constants
operator|.
name|SCAN_BATCH_SIZE
argument_list|)
name|int
name|batchSize
parameter_list|,
annotation|@
name|DefaultValue
argument_list|(
literal|"0"
argument_list|)
annotation|@
name|QueryParam
argument_list|(
name|Constants
operator|.
name|SCAN_START_TIME
argument_list|)
name|long
name|startTime
parameter_list|,
annotation|@
name|DefaultValue
argument_list|(
name|Long
operator|.
name|MAX_VALUE
operator|+
literal|""
argument_list|)
annotation|@
name|QueryParam
argument_list|(
name|Constants
operator|.
name|SCAN_END_TIME
argument_list|)
name|long
name|endTime
parameter_list|,
annotation|@
name|DefaultValue
argument_list|(
literal|"true"
argument_list|)
annotation|@
name|QueryParam
argument_list|(
name|Constants
operator|.
name|SCAN_BATCH_SIZE
argument_list|)
name|boolean
name|cacheBlocks
parameter_list|,
annotation|@
name|DefaultValue
argument_list|(
literal|"false"
argument_list|)
annotation|@
name|QueryParam
argument_list|(
name|Constants
operator|.
name|SCAN_REVERSED
argument_list|)
name|boolean
name|reversed
parameter_list|,
annotation|@
name|DefaultValue
argument_list|(
literal|""
argument_list|)
annotation|@
name|QueryParam
argument_list|(
name|Constants
operator|.
name|SCAN_FILTER
argument_list|)
name|String
name|paramFilter
parameter_list|)
block|{
try|try
block|{
name|Filter
name|prefixFilter
init|=
literal|null
decl_stmt|;
name|Scan
name|tableScan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
if|if
condition|(
name|scanSpec
operator|.
name|indexOf
argument_list|(
literal|'*'
argument_list|)
operator|>
literal|0
condition|)
block|{
name|String
name|prefix
init|=
name|scanSpec
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|scanSpec
operator|.
name|indexOf
argument_list|(
literal|'*'
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|prefixBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|prefix
argument_list|)
decl_stmt|;
name|prefixFilter
operator|=
operator|new
name|PrefixFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|prefix
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|startRow
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tableScan
operator|.
name|setStartRow
argument_list|(
name|prefixBytes
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Query parameters  : Table Name => "
operator|+
name|this
operator|.
name|table
operator|+
literal|" Start Row => "
operator|+
name|startRow
operator|+
literal|" End Row => "
operator|+
name|endRow
operator|+
literal|" Columns => "
operator|+
name|column
operator|+
literal|" Start Time => "
operator|+
name|startTime
operator|+
literal|" End Time => "
operator|+
name|endTime
operator|+
literal|" Cache Blocks => "
operator|+
name|cacheBlocks
operator|+
literal|" Max Versions => "
operator|+
name|maxVersions
operator|+
literal|" Batch Size => "
operator|+
name|batchSize
argument_list|)
expr_stmt|;
block|}
name|Table
name|hTable
init|=
name|RESTServlet
operator|.
name|getInstance
argument_list|()
operator|.
name|getTable
argument_list|(
name|this
operator|.
name|table
argument_list|)
decl_stmt|;
name|tableScan
operator|.
name|setBatch
argument_list|(
name|batchSize
argument_list|)
expr_stmt|;
name|tableScan
operator|.
name|setMaxVersions
argument_list|(
name|maxVersions
argument_list|)
expr_stmt|;
name|tableScan
operator|.
name|setTimeRange
argument_list|(
name|startTime
argument_list|,
name|endTime
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|startRow
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tableScan
operator|.
name|setStartRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|startRow
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|tableScan
operator|.
name|setStopRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|endRow
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|csplit
range|:
name|column
control|)
block|{
name|String
index|[]
name|familysplit
init|=
name|csplit
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|familysplit
operator|.
name|length
operator|==
literal|2
condition|)
block|{
if|if
condition|(
name|familysplit
index|[
literal|1
index|]
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Scan family and column : "
operator|+
name|familysplit
index|[
literal|0
index|]
operator|+
literal|"  "
operator|+
name|familysplit
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
name|tableScan
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|familysplit
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|familysplit
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tableScan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|familysplit
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Scan family : "
operator|+
name|familysplit
index|[
literal|0
index|]
operator|+
literal|" and empty qualifier."
argument_list|)
expr_stmt|;
block|}
name|tableScan
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|familysplit
index|[
literal|0
index|]
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|familysplit
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Scan family : "
operator|+
name|familysplit
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
name|tableScan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|familysplit
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|FilterList
name|filterList
init|=
operator|new
name|FilterList
argument_list|()
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|paramFilter
argument_list|)
condition|)
block|{
name|ParseFilter
name|pf
init|=
operator|new
name|ParseFilter
argument_list|()
decl_stmt|;
name|Filter
name|parsedParamFilter
init|=
name|pf
operator|.
name|parseFilterString
argument_list|(
name|paramFilter
argument_list|)
decl_stmt|;
if|if
condition|(
name|parsedParamFilter
operator|!=
literal|null
condition|)
block|{
name|filterList
operator|.
name|addFilter
argument_list|(
name|parsedParamFilter
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|prefixFilter
operator|!=
literal|null
condition|)
block|{
name|filterList
operator|.
name|addFilter
argument_list|(
name|prefixFilter
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|filterList
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|tableScan
operator|.
name|setFilter
argument_list|(
name|filterList
argument_list|)
expr_stmt|;
block|}
name|int
name|fetchSize
init|=
name|this
operator|.
name|servlet
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|Constants
operator|.
name|SCAN_FETCH_SIZE
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|tableScan
operator|.
name|setCaching
argument_list|(
name|fetchSize
argument_list|)
expr_stmt|;
name|tableScan
operator|.
name|setReversed
argument_list|(
name|reversed
argument_list|)
expr_stmt|;
return|return
operator|new
name|TableScanResource
argument_list|(
name|hTable
operator|.
name|getScanner
argument_list|(
name|tableScan
argument_list|)
argument_list|,
name|userRequestedLimit
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exp
parameter_list|)
block|{
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementFailedScanRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|processException
argument_list|(
name|exp
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|exp
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

