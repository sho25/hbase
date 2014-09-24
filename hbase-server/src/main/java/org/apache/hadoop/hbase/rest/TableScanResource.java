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
name|ArrayList
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
name|GET
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
name|Produces
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
name|Response
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
name|Response
operator|.
name|ResponseBuilder
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
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|annotation
operator|.
name|XmlAccessType
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|annotation
operator|.
name|XmlAccessorType
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|annotation
operator|.
name|XmlElement
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|annotation
operator|.
name|XmlRootElement
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
name|CellUtil
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
name|Result
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
name|rest
operator|.
name|model
operator|.
name|CellModel
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
name|rest
operator|.
name|model
operator|.
name|RowModel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|annotate
operator|.
name|JsonIgnore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|annotate
operator|.
name|JsonProperty
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TableScanResource
extends|extends
name|ResourceBase
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
name|TableScanResource
operator|.
name|class
argument_list|)
decl_stmt|;
name|TableResource
name|tableResource
decl_stmt|;
name|ResultScanner
name|results
decl_stmt|;
name|int
name|userRequestedLimit
decl_stmt|;
specifier|public
name|TableScanResource
parameter_list|(
name|ResultScanner
name|scanner
parameter_list|,
name|int
name|userRequestedLimit
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|results
operator|=
name|scanner
expr_stmt|;
name|this
operator|.
name|userRequestedLimit
operator|=
name|userRequestedLimit
expr_stmt|;
block|}
annotation|@
name|GET
annotation|@
name|Produces
argument_list|(
block|{
name|Constants
operator|.
name|MIMETYPE_XML
block|,
name|Constants
operator|.
name|MIMETYPE_JSON
block|}
argument_list|)
specifier|public
name|CellSetModelStream
name|get
parameter_list|(
specifier|final
annotation|@
name|Context
name|UriInfo
name|uriInfo
parameter_list|)
block|{
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
specifier|final
name|int
name|rowsToSend
init|=
name|userRequestedLimit
decl_stmt|;
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementSucessfulScanRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
specifier|final
name|Iterator
argument_list|<
name|Result
argument_list|>
name|itr
init|=
name|results
operator|.
name|iterator
argument_list|()
decl_stmt|;
return|return
operator|new
name|CellSetModelStream
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|RowModel
argument_list|>
argument_list|()
block|{
specifier|public
name|Iterator
argument_list|<
name|RowModel
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|Iterator
argument_list|<
name|RowModel
argument_list|>
argument_list|()
block|{
name|int
name|count
init|=
name|rowsToSend
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
if|if
condition|(
name|count
operator|>
literal|0
condition|)
block|{
return|return
name|itr
operator|.
name|hasNext
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Remove method cannot be used in CellSetModelStream"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|RowModel
name|next
parameter_list|()
block|{
name|Result
name|rs
init|=
name|itr
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|rs
operator|==
literal|null
operator|)
operator|||
operator|(
name|count
operator|<=
literal|0
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|byte
index|[]
name|rowKey
init|=
name|rs
operator|.
name|getRow
argument_list|()
decl_stmt|;
name|RowModel
name|rModel
init|=
operator|new
name|RowModel
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
init|=
name|rs
operator|.
name|listCells
argument_list|()
decl_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|kvs
control|)
block|{
name|rModel
operator|.
name|addCell
argument_list|(
operator|new
name|CellModel
argument_list|(
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|kv
argument_list|)
argument_list|,
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|kv
argument_list|)
argument_list|,
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|kv
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|count
operator|--
expr_stmt|;
return|return
name|rModel
return|;
block|}
block|}
return|;
block|}
block|}
argument_list|)
return|;
block|}
annotation|@
name|GET
annotation|@
name|Produces
argument_list|(
block|{
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
block|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF_IETF
block|}
argument_list|)
specifier|public
name|Response
name|getProtobuf
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
literal|"column"
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
parameter_list|)
block|{
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
try|try
block|{
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
name|ProtobufStreamingUtil
name|stream
init|=
operator|new
name|ProtobufStreamingUtil
argument_list|(
name|this
operator|.
name|results
argument_list|,
name|contentType
argument_list|,
name|userRequestedLimit
argument_list|,
name|fetchSize
argument_list|)
decl_stmt|;
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementSucessfulScanRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ResponseBuilder
name|response
init|=
name|Response
operator|.
name|ok
argument_list|(
name|stream
argument_list|)
decl_stmt|;
name|response
operator|.
name|header
argument_list|(
literal|"content-type"
argument_list|,
name|contentType
argument_list|)
expr_stmt|;
return|return
name|response
operator|.
name|build
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
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
annotation|@
name|XmlRootElement
argument_list|(
name|name
operator|=
literal|"CellSet"
argument_list|)
annotation|@
name|XmlAccessorType
argument_list|(
name|XmlAccessType
operator|.
name|FIELD
argument_list|)
specifier|public
specifier|static
class|class
name|CellSetModelStream
block|{
comment|// JAXB needs an arraylist for streaming
annotation|@
name|XmlElement
argument_list|(
name|name
operator|=
literal|"Row"
argument_list|)
annotation|@
name|JsonIgnore
specifier|private
name|ArrayList
argument_list|<
name|RowModel
argument_list|>
name|Row
decl_stmt|;
specifier|public
name|CellSetModelStream
parameter_list|()
block|{     }
specifier|public
name|CellSetModelStream
parameter_list|(
specifier|final
name|ArrayList
argument_list|<
name|RowModel
argument_list|>
name|rowList
parameter_list|)
block|{
name|this
operator|.
name|Row
operator|=
name|rowList
expr_stmt|;
block|}
comment|// jackson needs an iterator for streaming
annotation|@
name|JsonProperty
argument_list|(
literal|"Row"
argument_list|)
specifier|public
name|Iterator
argument_list|<
name|RowModel
argument_list|>
name|getIterator
parameter_list|()
block|{
return|return
name|Row
operator|.
name|iterator
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

