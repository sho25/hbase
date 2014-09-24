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
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|DELETE
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
name|CacheControl
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
name|CellSetModel
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
name|ScannerInstanceResource
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
name|ScannerInstanceResource
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
name|CacheControl
name|cacheControl
decl_stmt|;
static|static
block|{
name|cacheControl
operator|=
operator|new
name|CacheControl
argument_list|()
expr_stmt|;
name|cacheControl
operator|.
name|setNoCache
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|cacheControl
operator|.
name|setNoTransform
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|ResultGenerator
name|generator
init|=
literal|null
decl_stmt|;
name|String
name|id
init|=
literal|null
decl_stmt|;
name|int
name|batch
init|=
literal|1
decl_stmt|;
specifier|public
name|ScannerInstanceResource
parameter_list|()
throws|throws
name|IOException
block|{ }
specifier|public
name|ScannerInstanceResource
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|id
parameter_list|,
name|ResultGenerator
name|generator
parameter_list|,
name|int
name|batch
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|generator
operator|=
name|generator
expr_stmt|;
name|this
operator|.
name|batch
operator|=
name|batch
expr_stmt|;
block|}
annotation|@
name|GET
annotation|@
name|Produces
argument_list|(
block|{
name|MIMETYPE_XML
block|,
name|MIMETYPE_JSON
block|,
name|MIMETYPE_PROTOBUF
block|,
name|MIMETYPE_PROTOBUF_IETF
block|}
argument_list|)
specifier|public
name|Response
name|get
parameter_list|(
specifier|final
annotation|@
name|Context
name|UriInfo
name|uriInfo
parameter_list|,
annotation|@
name|QueryParam
argument_list|(
literal|"n"
argument_list|)
name|int
name|maxRows
parameter_list|,
specifier|final
annotation|@
name|QueryParam
argument_list|(
literal|"c"
argument_list|)
name|int
name|maxValues
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"GET "
operator|+
name|uriInfo
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|generator
operator|==
literal|null
condition|)
block|{
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementFailedGetRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return
name|Response
operator|.
name|status
argument_list|(
name|Response
operator|.
name|Status
operator|.
name|NOT_FOUND
argument_list|)
operator|.
name|type
argument_list|(
name|MIMETYPE_TEXT
argument_list|)
operator|.
name|entity
argument_list|(
literal|"Not found"
operator|+
name|CRLF
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
name|CellSetModel
name|model
init|=
operator|new
name|CellSetModel
argument_list|()
decl_stmt|;
name|RowModel
name|rowModel
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|rowKey
init|=
literal|null
decl_stmt|;
name|int
name|limit
init|=
name|batch
decl_stmt|;
if|if
condition|(
name|maxValues
operator|>
literal|0
condition|)
block|{
name|limit
operator|=
name|maxValues
expr_stmt|;
block|}
name|int
name|count
init|=
name|limit
decl_stmt|;
do|do
block|{
name|Cell
name|value
init|=
literal|null
decl_stmt|;
try|try
block|{
name|value
operator|=
name|generator
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
if|if
condition|(
name|ScannerResource
operator|.
name|delete
argument_list|(
name|id
argument_list|)
condition|)
block|{
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementSucessfulDeleteRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementFailedDeleteRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementFailedGetRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return
name|Response
operator|.
name|status
argument_list|(
name|Response
operator|.
name|Status
operator|.
name|GONE
argument_list|)
operator|.
name|type
argument_list|(
name|MIMETYPE_TEXT
argument_list|)
operator|.
name|entity
argument_list|(
literal|"Gone"
operator|+
name|CRLF
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"generator exhausted"
argument_list|)
expr_stmt|;
comment|// respond with 204 (No Content) if an empty cell set would be
comment|// returned
if|if
condition|(
name|count
operator|==
name|limit
condition|)
block|{
return|return
name|Response
operator|.
name|noContent
argument_list|()
operator|.
name|build
argument_list|()
return|;
block|}
break|break;
block|}
if|if
condition|(
name|rowKey
operator|==
literal|null
condition|)
block|{
name|rowKey
operator|=
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|rowModel
operator|=
operator|new
name|RowModel
argument_list|(
name|rowKey
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|value
argument_list|)
argument_list|,
name|rowKey
argument_list|)
condition|)
block|{
comment|// if maxRows was given as a query param, stop if we would exceed the
comment|// specified number of rows
if|if
condition|(
name|maxRows
operator|>
literal|0
condition|)
block|{
if|if
condition|(
operator|--
name|maxRows
operator|==
literal|0
condition|)
block|{
name|generator
operator|.
name|putBack
argument_list|(
name|value
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|model
operator|.
name|addRow
argument_list|(
name|rowModel
argument_list|)
expr_stmt|;
name|rowKey
operator|=
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|rowModel
operator|=
operator|new
name|RowModel
argument_list|(
name|rowKey
argument_list|)
expr_stmt|;
block|}
name|rowModel
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
name|value
argument_list|)
argument_list|,
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|value
argument_list|)
argument_list|,
name|value
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|--
name|count
operator|>
literal|0
condition|)
do|;
name|model
operator|.
name|addRow
argument_list|(
name|rowModel
argument_list|)
expr_stmt|;
name|ResponseBuilder
name|response
init|=
name|Response
operator|.
name|ok
argument_list|(
name|model
argument_list|)
decl_stmt|;
name|response
operator|.
name|cacheControl
argument_list|(
name|cacheControl
argument_list|)
expr_stmt|;
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementSucessfulGetRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return
name|response
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|GET
annotation|@
name|Produces
argument_list|(
name|MIMETYPE_BINARY
argument_list|)
specifier|public
name|Response
name|getBinary
parameter_list|(
specifier|final
annotation|@
name|Context
name|UriInfo
name|uriInfo
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"GET "
operator|+
name|uriInfo
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|" as "
operator|+
name|MIMETYPE_BINARY
argument_list|)
expr_stmt|;
block|}
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
name|Cell
name|value
init|=
name|generator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"generator exhausted"
argument_list|)
expr_stmt|;
return|return
name|Response
operator|.
name|noContent
argument_list|()
operator|.
name|build
argument_list|()
return|;
block|}
name|ResponseBuilder
name|response
init|=
name|Response
operator|.
name|ok
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|value
argument_list|)
argument_list|)
decl_stmt|;
name|response
operator|.
name|cacheControl
argument_list|(
name|cacheControl
argument_list|)
expr_stmt|;
name|response
operator|.
name|header
argument_list|(
literal|"X-Row"
argument_list|,
name|Base64
operator|.
name|encodeBytes
argument_list|(
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|response
operator|.
name|header
argument_list|(
literal|"X-Column"
argument_list|,
name|Base64
operator|.
name|encodeBytes
argument_list|(
name|KeyValue
operator|.
name|makeColumn
argument_list|(
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|value
argument_list|)
argument_list|,
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|response
operator|.
name|header
argument_list|(
literal|"X-Timestamp"
argument_list|,
name|value
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementSucessfulGetRequests
argument_list|(
literal|1
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
name|IllegalStateException
name|e
parameter_list|)
block|{
if|if
condition|(
name|ScannerResource
operator|.
name|delete
argument_list|(
name|id
argument_list|)
condition|)
block|{
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementSucessfulDeleteRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementFailedDeleteRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementFailedGetRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return
name|Response
operator|.
name|status
argument_list|(
name|Response
operator|.
name|Status
operator|.
name|GONE
argument_list|)
operator|.
name|type
argument_list|(
name|MIMETYPE_TEXT
argument_list|)
operator|.
name|entity
argument_list|(
literal|"Gone"
operator|+
name|CRLF
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
annotation|@
name|DELETE
specifier|public
name|Response
name|delete
parameter_list|(
specifier|final
annotation|@
name|Context
name|UriInfo
name|uriInfo
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"DELETE "
operator|+
name|uriInfo
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|servlet
operator|.
name|isReadOnly
argument_list|()
condition|)
block|{
return|return
name|Response
operator|.
name|status
argument_list|(
name|Response
operator|.
name|Status
operator|.
name|FORBIDDEN
argument_list|)
operator|.
name|type
argument_list|(
name|MIMETYPE_TEXT
argument_list|)
operator|.
name|entity
argument_list|(
literal|"Forbidden"
operator|+
name|CRLF
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
if|if
condition|(
name|ScannerResource
operator|.
name|delete
argument_list|(
name|id
argument_list|)
condition|)
block|{
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementSucessfulDeleteRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementFailedDeleteRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|Response
operator|.
name|ok
argument_list|()
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

