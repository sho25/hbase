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
name|Map
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
name|javax
operator|.
name|xml
operator|.
name|namespace
operator|.
name|QName
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|TableExistsException
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
name|TableNotEnabledException
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
name|TableNotFoundException
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
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Admin
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
name|rest
operator|.
name|model
operator|.
name|ColumnSchemaModel
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
name|TableSchemaModel
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SchemaResource
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
name|SchemaResource
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
name|TableResource
name|tableResource
decl_stmt|;
comment|/**    * Constructor    * @param tableResource    * @throws IOException    */
specifier|public
name|SchemaResource
parameter_list|(
name|TableResource
name|tableResource
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableResource
operator|=
name|tableResource
expr_stmt|;
block|}
specifier|private
name|HTableDescriptor
name|getTableSchema
parameter_list|()
throws|throws
name|IOException
throws|,
name|TableNotFoundException
block|{
name|Table
name|table
init|=
name|servlet
operator|.
name|getTable
argument_list|(
name|tableResource
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|table
operator|.
name|getTableDescriptor
argument_list|()
return|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|GET
annotation|@
name|Produces
argument_list|(
block|{
name|MIMETYPE_TEXT
block|,
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
parameter_list|)
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
try|try
block|{
name|ResponseBuilder
name|response
init|=
name|Response
operator|.
name|ok
argument_list|(
operator|new
name|TableSchemaModel
argument_list|(
name|getTableSchema
argument_list|()
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
name|Exception
name|e
parameter_list|)
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
name|processException
argument_list|(
name|e
argument_list|)
return|;
block|}
block|}
specifier|private
name|Response
name|replace
parameter_list|(
specifier|final
name|TableName
name|name
parameter_list|,
specifier|final
name|TableSchemaModel
name|model
parameter_list|,
specifier|final
name|UriInfo
name|uriInfo
parameter_list|,
specifier|final
name|Admin
name|admin
parameter_list|)
block|{
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
try|try
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|name
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|QName
argument_list|,
name|Object
argument_list|>
name|e
range|:
name|model
operator|.
name|getAny
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|htd
operator|.
name|setValue
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|getLocalPart
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ColumnSchemaModel
name|family
range|:
name|model
operator|.
name|getColumns
argument_list|()
control|)
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|QName
argument_list|,
name|Object
argument_list|>
name|e
range|:
name|family
operator|.
name|getAny
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|hcd
operator|.
name|setValue
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|getLocalPart
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyTable
argument_list|(
name|name
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementSucessfulPutRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementSucessfulPutRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableExistsException
name|e
parameter_list|)
block|{
comment|// race, someone else created a table with the same name
return|return
name|Response
operator|.
name|status
argument_list|(
name|Response
operator|.
name|Status
operator|.
name|NOT_MODIFIED
argument_list|)
operator|.
name|type
argument_list|(
name|MIMETYPE_TEXT
argument_list|)
operator|.
name|entity
argument_list|(
literal|"Not modified"
operator|+
name|CRLF
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
return|return
name|Response
operator|.
name|created
argument_list|(
name|uriInfo
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementFailedPutRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return
name|processException
argument_list|(
name|e
argument_list|)
return|;
block|}
block|}
specifier|private
name|Response
name|update
parameter_list|(
specifier|final
name|TableName
name|name
parameter_list|,
specifier|final
name|TableSchemaModel
name|model
parameter_list|,
specifier|final
name|UriInfo
name|uriInfo
parameter_list|,
specifier|final
name|Admin
name|admin
parameter_list|)
block|{
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
try|try
block|{
name|HTableDescriptor
name|htd
init|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
try|try
block|{
for|for
control|(
name|ColumnSchemaModel
name|family
range|:
name|model
operator|.
name|getColumns
argument_list|()
control|)
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|QName
argument_list|,
name|Object
argument_list|>
name|e
range|:
name|family
operator|.
name|getAny
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|hcd
operator|.
name|setValue
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|getLocalPart
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|htd
operator|.
name|hasFamily
argument_list|(
name|hcd
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|admin
operator|.
name|modifyColumnFamily
argument_list|(
name|name
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|admin
operator|.
name|addColumnFamily
argument_list|(
name|name
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
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
name|SERVICE_UNAVAILABLE
argument_list|)
operator|.
name|type
argument_list|(
name|MIMETYPE_TEXT
argument_list|)
operator|.
name|entity
argument_list|(
literal|"Unavailable"
operator|+
name|CRLF
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
finally|finally
block|{
name|admin
operator|.
name|enableTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableResource
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementSucessfulPutRequests
argument_list|(
literal|1
argument_list|)
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
name|Exception
name|e
parameter_list|)
block|{
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementFailedPutRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return
name|processException
argument_list|(
name|e
argument_list|)
return|;
block|}
block|}
specifier|private
name|Response
name|update
parameter_list|(
specifier|final
name|TableSchemaModel
name|model
parameter_list|,
specifier|final
name|boolean
name|replace
parameter_list|,
specifier|final
name|UriInfo
name|uriInfo
parameter_list|)
block|{
try|try
block|{
name|TableName
name|name
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableResource
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Admin
name|admin
init|=
name|servlet
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
name|replace
operator|||
operator|!
name|admin
operator|.
name|tableExists
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|replace
argument_list|(
name|name
argument_list|,
name|model
argument_list|,
name|uriInfo
argument_list|,
name|admin
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|update
argument_list|(
name|name
argument_list|,
name|model
argument_list|,
name|uriInfo
argument_list|,
name|admin
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|servlet
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrementFailedPutRequests
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return
name|processException
argument_list|(
name|e
argument_list|)
return|;
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
name|MIMETYPE_PROTOBUF
block|,
name|MIMETYPE_PROTOBUF_IETF
block|}
argument_list|)
specifier|public
name|Response
name|put
parameter_list|(
specifier|final
name|TableSchemaModel
name|model
parameter_list|,
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
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
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
return|return
name|update
argument_list|(
name|model
argument_list|,
literal|true
argument_list|,
name|uriInfo
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
name|MIMETYPE_PROTOBUF
block|,
name|MIMETYPE_PROTOBUF_IETF
block|}
argument_list|)
specifier|public
name|Response
name|post
parameter_list|(
specifier|final
name|TableSchemaModel
name|model
parameter_list|,
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
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
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
return|return
name|update
argument_list|(
name|model
argument_list|,
literal|false
argument_list|,
name|uriInfo
argument_list|)
return|;
block|}
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"DE_MIGHT_IGNORE"
argument_list|,
name|justification
operator|=
literal|"Expected"
argument_list|)
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
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
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
try|try
block|{
name|Admin
name|admin
init|=
name|servlet
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableResource
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotEnabledException
name|e
parameter_list|)
block|{
comment|/* this is what we want anyway */
block|}
name|admin
operator|.
name|deleteTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableResource
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
name|Exception
name|e
parameter_list|)
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
return|return
name|processException
argument_list|(
name|e
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

