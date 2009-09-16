begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|stargate
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
name|Consumes
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
name|POST
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
name|PUT
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
name|WebApplicationException
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
name|HttpHeaders
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
name|UriInfo
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
name|Delete
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
name|HTableInterface
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
name|HTablePool
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
name|stargate
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
name|stargate
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
name|stargate
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
name|Bytes
import|;
end_import

begin_class
specifier|public
class|class
name|RowResource
implements|implements
name|Constants
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
name|RowResource
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|String
name|table
decl_stmt|;
specifier|private
name|RowSpec
name|rowspec
decl_stmt|;
specifier|private
name|CacheControl
name|cacheControl
decl_stmt|;
specifier|public
name|RowResource
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|rowspec
parameter_list|,
name|String
name|versions
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|rowspec
operator|=
operator|new
name|RowSpec
argument_list|(
name|rowspec
argument_list|)
expr_stmt|;
if|if
condition|(
name|versions
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|rowspec
operator|.
name|setMaxVersions
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|versions
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|cacheControl
operator|=
operator|new
name|CacheControl
argument_list|()
expr_stmt|;
name|cacheControl
operator|.
name|setMaxAge
argument_list|(
name|RESTServlet
operator|.
name|getInstance
argument_list|()
operator|.
name|getMaxAge
argument_list|(
name|table
argument_list|)
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
name|MIMETYPE_JAVASCRIPT
block|,
name|MIMETYPE_PROTOBUF
block|}
argument_list|)
specifier|public
name|Response
name|get
parameter_list|(
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
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|ResultGenerator
name|generator
init|=
name|ResultGenerator
operator|.
name|fromRowSpec
argument_list|(
name|table
argument_list|,
name|rowspec
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|generator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|Response
operator|.
name|Status
operator|.
name|NOT_FOUND
argument_list|)
throw|;
block|}
name|CellSetModel
name|model
init|=
operator|new
name|CellSetModel
argument_list|()
decl_stmt|;
name|KeyValue
name|value
init|=
name|generator
operator|.
name|next
argument_list|()
decl_stmt|;
name|byte
index|[]
name|rowKey
init|=
name|value
operator|.
name|getRow
argument_list|()
decl_stmt|;
name|RowModel
name|rowModel
init|=
operator|new
name|RowModel
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
do|do
block|{
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|value
operator|.
name|getRow
argument_list|()
argument_list|,
name|rowKey
argument_list|)
condition|)
block|{
name|model
operator|.
name|addRow
argument_list|(
name|rowModel
argument_list|)
expr_stmt|;
name|rowKey
operator|=
name|value
operator|.
name|getRow
argument_list|()
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
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|value
operator|=
name|generator
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|value
operator|!=
literal|null
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
return|return
name|response
operator|.
name|build
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|e
argument_list|,
name|Response
operator|.
name|Status
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
throw|;
block|}
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
comment|// doesn't make sense to use a non specific coordinate as this can only
comment|// return a single cell
if|if
condition|(
operator|!
name|rowspec
operator|.
name|hasColumns
argument_list|()
operator|||
name|rowspec
operator|.
name|getColumns
argument_list|()
operator|.
name|length
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|Response
operator|.
name|Status
operator|.
name|BAD_REQUEST
argument_list|)
throw|;
block|}
try|try
block|{
name|ResultGenerator
name|generator
init|=
name|ResultGenerator
operator|.
name|fromRowSpec
argument_list|(
name|table
argument_list|,
name|rowspec
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|generator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|Response
operator|.
name|Status
operator|.
name|NOT_FOUND
argument_list|)
throw|;
block|}
name|KeyValue
name|value
init|=
name|generator
operator|.
name|next
argument_list|()
decl_stmt|;
name|ResponseBuilder
name|response
init|=
name|Response
operator|.
name|ok
argument_list|(
name|value
operator|.
name|getValue
argument_list|()
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
literal|"X-Timestamp"
argument_list|,
name|value
operator|.
name|getTimestamp
argument_list|()
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
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|e
argument_list|,
name|Response
operator|.
name|Status
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
throw|;
block|}
block|}
specifier|private
name|Response
name|update
parameter_list|(
name|CellSetModel
name|model
parameter_list|,
name|boolean
name|replace
parameter_list|)
block|{
name|HTablePool
name|pool
decl_stmt|;
try|try
block|{
name|pool
operator|=
name|RESTServlet
operator|.
name|getInstance
argument_list|()
operator|.
name|getTablePool
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|e
argument_list|,
name|Response
operator|.
name|Status
operator|.
name|INTERNAL_SERVER_ERROR
argument_list|)
throw|;
block|}
name|HTableInterface
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
name|table
operator|=
name|pool
operator|.
name|getTable
argument_list|(
name|this
operator|.
name|table
argument_list|)
expr_stmt|;
for|for
control|(
name|RowModel
name|row
range|:
name|model
operator|.
name|getRows
argument_list|()
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|CellModel
name|cell
range|:
name|row
operator|.
name|getCells
argument_list|()
control|)
block|{
name|byte
index|[]
index|[]
name|parts
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|cell
operator|.
name|getColumn
argument_list|()
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
name|put
operator|.
name|add
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|cell
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|put
operator|.
name|add
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|,
name|parts
index|[
literal|1
index|]
argument_list|,
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|cell
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
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
literal|"PUT "
operator|+
name|put
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|ResponseBuilder
name|response
init|=
name|Response
operator|.
name|ok
argument_list|()
decl_stmt|;
return|return
name|response
operator|.
name|build
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|e
argument_list|,
name|Response
operator|.
name|Status
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|pool
operator|.
name|putTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|Response
name|updateBinary
parameter_list|(
name|byte
index|[]
name|message
parameter_list|,
name|HttpHeaders
name|headers
parameter_list|,
name|boolean
name|replace
parameter_list|)
block|{
name|HTablePool
name|pool
decl_stmt|;
try|try
block|{
name|pool
operator|=
name|RESTServlet
operator|.
name|getInstance
argument_list|()
operator|.
name|getTablePool
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|e
argument_list|,
name|Response
operator|.
name|Status
operator|.
name|INTERNAL_SERVER_ERROR
argument_list|)
throw|;
block|}
name|HTableInterface
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
name|byte
index|[]
name|row
init|=
name|rowspec
operator|.
name|getRow
argument_list|()
decl_stmt|;
name|byte
index|[]
index|[]
name|columns
init|=
name|rowspec
operator|.
name|getColumns
argument_list|()
decl_stmt|;
name|byte
index|[]
name|column
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|columns
operator|!=
literal|null
condition|)
block|{
name|column
operator|=
name|columns
index|[
literal|0
index|]
expr_stmt|;
block|}
name|long
name|timestamp
init|=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|vals
init|=
name|headers
operator|.
name|getRequestHeader
argument_list|(
literal|"X-Row"
argument_list|)
decl_stmt|;
if|if
condition|(
name|vals
operator|!=
literal|null
operator|&&
operator|!
name|vals
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|row
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|vals
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|vals
operator|=
name|headers
operator|.
name|getRequestHeader
argument_list|(
literal|"X-Column"
argument_list|)
expr_stmt|;
if|if
condition|(
name|vals
operator|!=
literal|null
operator|&&
operator|!
name|vals
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|column
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|vals
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|vals
operator|=
name|headers
operator|.
name|getRequestHeader
argument_list|(
literal|"X-Timestamp"
argument_list|)
expr_stmt|;
if|if
condition|(
name|vals
operator|!=
literal|null
operator|&&
operator|!
name|vals
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|timestamp
operator|=
name|Long
operator|.
name|valueOf
argument_list|(
name|vals
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|column
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|Response
operator|.
name|Status
operator|.
name|BAD_REQUEST
argument_list|)
throw|;
block|}
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|byte
name|parts
index|[]
index|[]
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|column
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
name|put
operator|.
name|add
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|timestamp
argument_list|,
name|message
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|put
operator|.
name|add
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|,
name|parts
index|[
literal|1
index|]
argument_list|,
name|timestamp
argument_list|,
name|message
argument_list|)
expr_stmt|;
block|}
name|table
operator|=
name|pool
operator|.
name|getTable
argument_list|(
name|this
operator|.
name|table
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
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
literal|"PUT "
operator|+
name|put
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
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
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|e
argument_list|,
name|Response
operator|.
name|Status
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|pool
operator|.
name|putTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|PUT
annotation|@
name|Consumes
argument_list|(
block|{
name|MIMETYPE_XML
block|,
name|MIMETYPE_JSON
block|,
name|MIMETYPE_JAVASCRIPT
block|,
name|MIMETYPE_PROTOBUF
block|}
argument_list|)
specifier|public
name|Response
name|put
parameter_list|(
name|CellSetModel
name|model
parameter_list|,
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
literal|"PUT "
operator|+
name|uriInfo
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|update
argument_list|(
name|model
argument_list|,
literal|true
argument_list|)
return|;
block|}
annotation|@
name|PUT
annotation|@
name|Consumes
argument_list|(
name|MIMETYPE_BINARY
argument_list|)
specifier|public
name|Response
name|putBinary
parameter_list|(
name|byte
index|[]
name|message
parameter_list|,
annotation|@
name|Context
name|UriInfo
name|uriInfo
parameter_list|,
annotation|@
name|Context
name|HttpHeaders
name|headers
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
literal|"PUT "
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
return|return
name|updateBinary
argument_list|(
name|message
argument_list|,
name|headers
argument_list|,
literal|true
argument_list|)
return|;
block|}
annotation|@
name|POST
annotation|@
name|Consumes
argument_list|(
block|{
name|MIMETYPE_XML
block|,
name|MIMETYPE_JSON
block|,
name|MIMETYPE_JAVASCRIPT
block|,
name|MIMETYPE_PROTOBUF
block|}
argument_list|)
specifier|public
name|Response
name|post
parameter_list|(
name|CellSetModel
name|model
parameter_list|,
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
literal|"POST "
operator|+
name|uriInfo
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|update
argument_list|(
name|model
argument_list|,
literal|false
argument_list|)
return|;
block|}
annotation|@
name|POST
annotation|@
name|Consumes
argument_list|(
name|MIMETYPE_BINARY
argument_list|)
specifier|public
name|Response
name|postBinary
parameter_list|(
name|byte
index|[]
name|message
parameter_list|,
annotation|@
name|Context
name|UriInfo
name|uriInfo
parameter_list|,
annotation|@
name|Context
name|HttpHeaders
name|headers
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
literal|"POST "
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
return|return
name|updateBinary
argument_list|(
name|message
argument_list|,
name|headers
argument_list|,
literal|false
argument_list|)
return|;
block|}
annotation|@
name|DELETE
specifier|public
name|Response
name|delete
parameter_list|(
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
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|rowspec
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|column
range|:
name|rowspec
operator|.
name|getColumns
argument_list|()
control|)
block|{
name|byte
index|[]
index|[]
name|split
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|column
argument_list|)
decl_stmt|;
if|if
condition|(
name|rowspec
operator|.
name|hasTimestamp
argument_list|()
condition|)
block|{
if|if
condition|(
name|split
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|delete
operator|.
name|deleteColumns
argument_list|(
name|split
index|[
literal|0
index|]
argument_list|,
name|split
index|[
literal|1
index|]
argument_list|,
name|rowspec
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|delete
operator|.
name|deleteFamily
argument_list|(
name|split
index|[
literal|0
index|]
argument_list|,
name|rowspec
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|split
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|delete
operator|.
name|deleteColumns
argument_list|(
name|split
index|[
literal|0
index|]
argument_list|,
name|split
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|delete
operator|.
name|deleteFamily
argument_list|(
name|split
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|HTablePool
name|pool
decl_stmt|;
try|try
block|{
name|pool
operator|=
name|RESTServlet
operator|.
name|getInstance
argument_list|()
operator|.
name|getTablePool
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|e
argument_list|,
name|Response
operator|.
name|Status
operator|.
name|INTERNAL_SERVER_ERROR
argument_list|)
throw|;
block|}
name|HTableInterface
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
name|table
operator|=
name|pool
operator|.
name|getTable
argument_list|(
name|this
operator|.
name|table
argument_list|)
expr_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
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
name|delete
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|WebApplicationException
argument_list|(
name|e
argument_list|,
name|Response
operator|.
name|Status
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|pool
operator|.
name|putTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
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

