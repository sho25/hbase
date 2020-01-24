begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
operator|.
name|webapp
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
name|net
operator|.
name|URLEncoder
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
name|StandardCharsets
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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|stream
operator|.
name|StreamSupport
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletRequest
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
name|lang3
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
name|lang3
operator|.
name|builder
operator|.
name|ToStringBuilder
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
name|lang3
operator|.
name|builder
operator|.
name|ToStringStyle
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
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|AdvancedScanResultConsumer
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
name|AsyncConnection
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
name|AsyncTable
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
name|ResultScanner
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
name|SingleColumnValueFilter
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
name|master
operator|.
name|RegionState
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
name|collect
operator|.
name|Iterators
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
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|QueryStringEncoder
import|;
end_import

begin_comment
comment|/**  *<p>  * Support class for the "Meta Entries" section in {@code resources/hbase-webapps/master/table.jsp}.  *</p>  *<p>  *<b>Interface</b>. This class's intended consumer is {@code table.jsp}. As such, it's primary  * interface is the active {@link HttpServletRequest}, from which it uses the {@code scan_*}  * request parameters. This class supports paging through an optionally filtered view of the  * contents of {@code hbase:meta}. Those filters and the pagination offset are specified via these  * request parameters. It provides helper methods for constructing pagination links.  *<ul>  *<li>{@value #NAME_PARAM} - the name of the table requested. The only table of our concern here  *   is {@code hbase:meta}; any other value is effectively ignored by the giant conditional in the  *   jsp.</li>  *<li>{@value #SCAN_LIMIT_PARAM} - specifies a limit on the number of region (replicas) rendered  *   on the by the table in a single request -- a limit on page size. This corresponds to the  *   number of {@link RegionReplicaInfo} objects produced by {@link Results#iterator()}. When a  *   value for {@code scan_limit} is invalid or not specified, the default value of  *   {@value #SCAN_LIMIT_DEFAULT} is used. In order to avoid excessive resource consumption, a  *   maximum value of {@value #SCAN_LIMIT_MAX} is enforced.</li>  *<li>{@value #SCAN_REGION_STATE_PARAM} - an optional filter on {@link RegionState}.</li>  *<li>{@value #SCAN_START_PARAM} - specifies the rowkey at which a scan should start. For usage  *   details, see the below section on<b>Pagination</b>.</li>  *<li>{@value #SCAN_TABLE_PARAM} - specifies a filter on the values returned, limiting them to  *   regions from a specified table. This parameter is implemented as a prefix filter on the  *   {@link Scan}, so in effect it can be used for simple namespace and multi-table matches.</li>  *</ul>  *</p>  *<p>  *<b>Pagination</b>. A single page of results are made available via {@link #getResults()} / an  * instance of {@link Results}. Callers use its {@link Iterator} consume the page of  * {@link RegionReplicaInfo} instances, each of which represents a region or region replica. Helper  * methods are provided for building page navigation controls preserving the user's selected filter  * set: {@link #buildFirstPageUrl()}, {@link #buildNextPageUrl(byte[])}. Pagination is implemented  * using a simple offset + limit system. Offset is provided by the {@value #SCAN_START_PARAM},  * limit via {@value #SCAN_LIMIT_PARAM}. Under the hood, the {@link Scan} is constructed with  * {@link Scan#setMaxResultSize(long)} set to ({@value SCAN_LIMIT_PARAM} +1), while the  * {@link Results} {@link Iterator} honors {@value #SCAN_LIMIT_PARAM}. The +1 allows the caller to  * know if a "next page" is available via {@link Results#hasMoreResults()}. Note that this  * pagination strategy is incomplete when it comes to region replicas and can potentially omit  * rendering replicas that fall between the last rowkey offset and {@code replicaCount % page size}.  *</p>  *<p>  *<b>Error Messages</b>. Any time there's an error parsing user input, a message will be populated  * in {@link #getErrorMessages()}. Any fields which produce an error will have their filter values  * set to the default, except for a value of {@value  #SCAN_LIMIT_PARAM} that exceeds  * {@value #SCAN_LIMIT_MAX}, in which case {@value #SCAN_LIMIT_MAX} is used.  *</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetaBrowser
block|{
specifier|public
specifier|static
specifier|final
name|String
name|NAME_PARAM
init|=
literal|"name"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_LIMIT_PARAM
init|=
literal|"scan_limit"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_REGION_STATE_PARAM
init|=
literal|"scan_region_state"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_START_PARAM
init|=
literal|"scan_start"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_TABLE_PARAM
init|=
literal|"scan_table"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|SCAN_LIMIT_DEFAULT
init|=
literal|10
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|SCAN_LIMIT_MAX
init|=
literal|10_000
decl_stmt|;
specifier|private
specifier|final
name|AsyncConnection
name|connection
decl_stmt|;
specifier|private
specifier|final
name|HttpServletRequest
name|request
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|errorMessages
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|Integer
name|scanLimit
decl_stmt|;
specifier|private
specifier|final
name|RegionState
operator|.
name|State
name|scanRegionState
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|scanStart
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|scanTable
decl_stmt|;
specifier|public
name|MetaBrowser
parameter_list|(
specifier|final
name|AsyncConnection
name|connection
parameter_list|,
specifier|final
name|HttpServletRequest
name|request
parameter_list|)
block|{
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|errorMessages
operator|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|resolveName
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|this
operator|.
name|scanLimit
operator|=
name|resolveScanLimit
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|this
operator|.
name|scanRegionState
operator|=
name|resolveScanRegionState
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|this
operator|.
name|scanStart
operator|=
name|resolveScanStart
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|this
operator|.
name|scanTable
operator|=
name|resolveScanTable
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getErrorMessages
parameter_list|()
block|{
return|return
name|errorMessages
return|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
name|Integer
name|getScanLimit
parameter_list|()
block|{
return|return
name|scanLimit
return|;
block|}
specifier|public
name|byte
index|[]
name|getScanStart
parameter_list|()
block|{
return|return
name|scanStart
return|;
block|}
specifier|public
name|RegionState
operator|.
name|State
name|getScanRegionState
parameter_list|()
block|{
return|return
name|scanRegionState
return|;
block|}
specifier|public
name|TableName
name|getScanTable
parameter_list|()
block|{
return|return
name|scanTable
return|;
block|}
specifier|public
name|Results
name|getResults
parameter_list|()
block|{
specifier|final
name|AsyncTable
argument_list|<
name|AdvancedScanResultConsumer
argument_list|>
name|asyncTable
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
return|return
operator|new
name|Results
argument_list|(
name|asyncTable
operator|.
name|getScanner
argument_list|(
name|buildScan
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
operator|new
name|ToStringBuilder
argument_list|(
name|this
argument_list|,
name|ToStringStyle
operator|.
name|SHORT_PREFIX_STYLE
argument_list|)
operator|.
name|append
argument_list|(
literal|"scanStart"
argument_list|,
name|scanStart
argument_list|)
operator|.
name|append
argument_list|(
literal|"scanLimit"
argument_list|,
name|scanLimit
argument_list|)
operator|.
name|append
argument_list|(
literal|"scanTable"
argument_list|,
name|scanTable
argument_list|)
operator|.
name|append
argument_list|(
literal|"scanRegionState"
argument_list|,
name|scanRegionState
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|String
name|resolveName
parameter_list|(
specifier|final
name|HttpServletRequest
name|request
parameter_list|)
block|{
return|return
name|resolveRequestParameter
argument_list|(
name|request
argument_list|,
name|NAME_PARAM
argument_list|)
return|;
block|}
specifier|private
name|Integer
name|resolveScanLimit
parameter_list|(
specifier|final
name|HttpServletRequest
name|request
parameter_list|)
block|{
specifier|final
name|String
name|requestValueStr
init|=
name|resolveRequestParameter
argument_list|(
name|request
argument_list|,
name|SCAN_LIMIT_PARAM
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|requestValueStr
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|Integer
name|requestValue
init|=
name|tryParseInt
argument_list|(
name|requestValueStr
argument_list|)
decl_stmt|;
if|if
condition|(
name|requestValue
operator|==
literal|null
condition|)
block|{
name|errorMessages
operator|.
name|add
argument_list|(
name|buildScanLimitMalformedErrorMessage
argument_list|(
name|requestValueStr
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
if|if
condition|(
name|requestValue
operator|<=
literal|0
condition|)
block|{
name|errorMessages
operator|.
name|add
argument_list|(
name|buildScanLimitLTEQZero
argument_list|(
name|requestValue
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|SCAN_LIMIT_DEFAULT
return|;
block|}
specifier|final
name|int
name|truncatedValue
init|=
name|Math
operator|.
name|min
argument_list|(
name|requestValue
argument_list|,
name|SCAN_LIMIT_MAX
argument_list|)
decl_stmt|;
if|if
condition|(
name|requestValue
operator|!=
name|truncatedValue
condition|)
block|{
name|errorMessages
operator|.
name|add
argument_list|(
name|buildScanLimitExceededErrorMessage
argument_list|(
name|requestValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|truncatedValue
return|;
block|}
specifier|private
name|RegionState
operator|.
name|State
name|resolveScanRegionState
parameter_list|(
specifier|final
name|HttpServletRequest
name|request
parameter_list|)
block|{
specifier|final
name|String
name|requestValueStr
init|=
name|resolveRequestParameter
argument_list|(
name|request
argument_list|,
name|SCAN_REGION_STATE_PARAM
argument_list|)
decl_stmt|;
if|if
condition|(
name|requestValueStr
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|RegionState
operator|.
name|State
name|requestValue
init|=
name|tryValueOf
argument_list|(
name|RegionState
operator|.
name|State
operator|.
name|class
argument_list|,
name|requestValueStr
argument_list|)
decl_stmt|;
if|if
condition|(
name|requestValue
operator|==
literal|null
condition|)
block|{
name|errorMessages
operator|.
name|add
argument_list|(
name|buildScanRegionStateMalformedErrorMessage
argument_list|(
name|requestValueStr
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|requestValue
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|resolveScanStart
parameter_list|(
specifier|final
name|HttpServletRequest
name|request
parameter_list|)
block|{
comment|// TODO: handle replicas that fall between the last rowkey and pagination limit.
specifier|final
name|String
name|requestValue
init|=
name|resolveRequestParameter
argument_list|(
name|request
argument_list|,
name|SCAN_START_PARAM
argument_list|)
decl_stmt|;
if|if
condition|(
name|requestValue
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
name|requestValue
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TableName
name|resolveScanTable
parameter_list|(
specifier|final
name|HttpServletRequest
name|request
parameter_list|)
block|{
specifier|final
name|String
name|requestValue
init|=
name|resolveRequestParameter
argument_list|(
name|request
argument_list|,
name|SCAN_TABLE_PARAM
argument_list|)
decl_stmt|;
if|if
condition|(
name|requestValue
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|TableName
operator|.
name|valueOf
argument_list|(
name|requestValue
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|resolveRequestParameter
parameter_list|(
specifier|final
name|HttpServletRequest
name|request
parameter_list|,
specifier|final
name|String
name|param
parameter_list|)
block|{
if|if
condition|(
name|request
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|String
name|requestValueStrEnc
init|=
name|request
operator|.
name|getParameter
argument_list|(
name|param
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|requestValueStrEnc
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|urlDecode
argument_list|(
name|requestValueStrEnc
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Filter
name|buildTableFilter
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
block|{
return|return
operator|new
name|PrefixFilter
argument_list|(
name|tableName
operator|.
name|toBytes
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Filter
name|buildScanRegionStateFilter
parameter_list|(
specifier|final
name|RegionState
operator|.
name|State
name|state
parameter_list|)
block|{
return|return
operator|new
name|SingleColumnValueFilter
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|STATE_QUALIFIER
argument_list|,
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
comment|// use the same serialization strategy as found in MetaTableAccessor#addRegionStateToPut
name|Bytes
operator|.
name|toBytes
argument_list|(
name|state
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|Filter
name|buildScanFilter
parameter_list|()
block|{
if|if
condition|(
name|scanTable
operator|==
literal|null
operator|&&
name|scanRegionState
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|List
argument_list|<
name|Filter
argument_list|>
name|filters
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
if|if
condition|(
name|scanTable
operator|!=
literal|null
condition|)
block|{
name|filters
operator|.
name|add
argument_list|(
name|buildTableFilter
argument_list|(
name|scanTable
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scanRegionState
operator|!=
literal|null
condition|)
block|{
name|filters
operator|.
name|add
argument_list|(
name|buildScanRegionStateFilter
argument_list|(
name|scanRegionState
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|filters
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
name|filters
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
return|return
operator|new
name|FilterList
argument_list|(
name|FilterList
operator|.
name|Operator
operator|.
name|MUST_PASS_ALL
argument_list|,
name|filters
argument_list|)
return|;
block|}
specifier|private
name|Scan
name|buildScan
parameter_list|()
block|{
specifier|final
name|Scan
name|metaScan
init|=
operator|new
name|Scan
argument_list|()
operator|.
name|addFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
operator|.
name|readVersions
argument_list|(
literal|1
argument_list|)
operator|.
name|setLimit
argument_list|(
operator|(
name|scanLimit
operator|!=
literal|null
condition|?
name|scanLimit
else|:
name|SCAN_LIMIT_DEFAULT
operator|)
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|scanStart
operator|!=
literal|null
condition|)
block|{
name|metaScan
operator|.
name|withStartRow
argument_list|(
name|scanStart
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|final
name|Filter
name|filter
init|=
name|buildScanFilter
argument_list|()
decl_stmt|;
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
name|metaScan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
return|return
name|metaScan
return|;
block|}
comment|/**    * Adds {@code value} to {@code encoder} under {@code paramName} when {@code value} is non-null.    */
specifier|private
name|void
name|addParam
parameter_list|(
specifier|final
name|QueryStringEncoder
name|encoder
parameter_list|,
specifier|final
name|String
name|paramName
parameter_list|,
specifier|final
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|encoder
operator|.
name|addParam
argument_list|(
name|paramName
argument_list|,
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|QueryStringEncoder
name|buildFirstPageEncoder
parameter_list|()
block|{
specifier|final
name|QueryStringEncoder
name|encoder
init|=
operator|new
name|QueryStringEncoder
argument_list|(
name|request
operator|.
name|getRequestURI
argument_list|()
argument_list|)
decl_stmt|;
name|addParam
argument_list|(
name|encoder
argument_list|,
name|NAME_PARAM
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|addParam
argument_list|(
name|encoder
argument_list|,
name|SCAN_LIMIT_PARAM
argument_list|,
name|scanLimit
argument_list|)
expr_stmt|;
name|addParam
argument_list|(
name|encoder
argument_list|,
name|SCAN_REGION_STATE_PARAM
argument_list|,
name|scanRegionState
argument_list|)
expr_stmt|;
name|addParam
argument_list|(
name|encoder
argument_list|,
name|SCAN_TABLE_PARAM
argument_list|,
name|scanTable
argument_list|)
expr_stmt|;
return|return
name|encoder
return|;
block|}
specifier|public
name|String
name|buildFirstPageUrl
parameter_list|()
block|{
return|return
name|buildFirstPageEncoder
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|static
name|String
name|buildStartParamFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|lastRow
parameter_list|)
block|{
if|if
condition|(
name|lastRow
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|urlEncode
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|lastRow
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|String
name|buildNextPageUrl
parameter_list|(
specifier|final
name|byte
index|[]
name|lastRow
parameter_list|)
block|{
specifier|final
name|QueryStringEncoder
name|encoder
init|=
name|buildFirstPageEncoder
argument_list|()
decl_stmt|;
specifier|final
name|String
name|startRow
init|=
name|buildStartParamFrom
argument_list|(
name|lastRow
argument_list|)
decl_stmt|;
name|addParam
argument_list|(
name|encoder
argument_list|,
name|SCAN_START_PARAM
argument_list|,
name|startRow
argument_list|)
expr_stmt|;
return|return
name|encoder
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|String
name|urlEncode
parameter_list|(
specifier|final
name|String
name|val
parameter_list|)
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|val
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
return|return
name|URLEncoder
operator|.
name|encode
argument_list|(
name|val
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
name|String
name|urlDecode
parameter_list|(
specifier|final
name|String
name|val
parameter_list|)
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|val
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
return|return
name|URLDecoder
operator|.
name|decode
argument_list|(
name|val
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
name|Integer
name|tryParseInt
parameter_list|(
specifier|final
name|String
name|val
parameter_list|)
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|val
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
return|return
name|Integer
operator|.
name|parseInt
argument_list|(
name|val
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
parameter_list|<
name|T
extends|extends
name|Enum
argument_list|<
name|T
argument_list|>
parameter_list|>
name|T
name|tryValueOf
parameter_list|(
specifier|final
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|,
specifier|final
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|clazz
operator|==
literal|null
operator|||
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
return|return
name|Enum
operator|.
name|valueOf
argument_list|(
name|clazz
argument_list|,
name|value
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
name|String
name|buildScanLimitExceededErrorMessage
parameter_list|(
specifier|final
name|int
name|requestValue
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"Requested SCAN_LIMIT value %d exceeds maximum value %d."
argument_list|,
name|requestValue
argument_list|,
name|SCAN_LIMIT_MAX
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|buildScanLimitMalformedErrorMessage
parameter_list|(
specifier|final
name|String
name|requestValue
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"Requested SCAN_LIMIT value '%s' cannot be parsed as an integer."
argument_list|,
name|requestValue
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|buildScanLimitLTEQZero
parameter_list|(
specifier|final
name|int
name|requestValue
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"Requested SCAN_LIMIT value %d is<= 0."
argument_list|,
name|requestValue
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|buildScanRegionStateMalformedErrorMessage
parameter_list|(
specifier|final
name|String
name|requestValue
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"Requested SCAN_REGION_STATE value '%s' cannot be parsed as a RegionState."
argument_list|,
name|requestValue
argument_list|)
return|;
block|}
comment|/**    * Encapsulates the results produced by this {@link MetaBrowser} instance.    */
specifier|public
specifier|final
class|class
name|Results
implements|implements
name|AutoCloseable
implements|,
name|Iterable
argument_list|<
name|RegionReplicaInfo
argument_list|>
block|{
specifier|private
specifier|final
name|ResultScanner
name|resultScanner
decl_stmt|;
specifier|private
specifier|final
name|Iterator
argument_list|<
name|RegionReplicaInfo
argument_list|>
name|sourceIterator
decl_stmt|;
specifier|private
name|Results
parameter_list|(
specifier|final
name|ResultScanner
name|resultScanner
parameter_list|)
block|{
name|this
operator|.
name|resultScanner
operator|=
name|resultScanner
expr_stmt|;
name|this
operator|.
name|sourceIterator
operator|=
name|StreamSupport
operator|.
name|stream
argument_list|(
name|resultScanner
operator|.
name|spliterator
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|map
argument_list|(
name|RegionReplicaInfo
operator|::
name|from
argument_list|)
operator|.
name|flatMap
argument_list|(
name|Collection
operator|::
name|stream
argument_list|)
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
comment|/**      * @return {@code true} when the underlying {@link ResultScanner} is not yet exhausted,      *   {@code false} otherwise.      */
specifier|public
name|boolean
name|hasMoreResults
parameter_list|()
block|{
return|return
name|sourceIterator
operator|.
name|hasNext
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|resultScanner
operator|!=
literal|null
condition|)
block|{
name|resultScanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|RegionReplicaInfo
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|Iterators
operator|.
name|limit
argument_list|(
name|sourceIterator
argument_list|,
name|scanLimit
operator|!=
literal|null
condition|?
name|scanLimit
else|:
name|SCAN_LIMIT_DEFAULT
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit
