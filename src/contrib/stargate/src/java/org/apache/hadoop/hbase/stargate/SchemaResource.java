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
name|TableNotFoundException
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
name|HBaseAdmin
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
name|stargate
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
name|stargate
operator|.
name|model
operator|.
name|TableSchemaModel
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
name|SchemaResource
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
name|SchemaResource
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|String
name|table
decl_stmt|;
specifier|private
name|CacheControl
name|cacheControl
decl_stmt|;
specifier|public
name|SchemaResource
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
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
specifier|private
name|HTableDescriptor
name|getTableSchema
parameter_list|()
throws|throws
name|IOException
throws|,
name|TableNotFoundException
block|{
name|HTablePool
name|pool
init|=
name|RESTServlet
operator|.
name|getInstance
argument_list|()
operator|.
name|getTablePool
argument_list|()
decl_stmt|;
name|HTable
name|table
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|this
operator|.
name|table
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
name|pool
operator|.
name|putTable
argument_list|(
name|table
argument_list|)
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
name|HTableDescriptor
name|htd
init|=
name|getTableSchema
argument_list|()
decl_stmt|;
name|TableSchemaModel
name|model
init|=
operator|new
name|TableSchemaModel
argument_list|()
decl_stmt|;
name|model
operator|.
name|setName
argument_list|(
name|htd
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|ImmutableBytesWritable
argument_list|>
name|e
range|:
name|htd
operator|.
name|getValues
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|model
operator|.
name|addAttribute
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|HColumnDescriptor
name|hcd
range|:
name|htd
operator|.
name|getFamilies
argument_list|()
control|)
block|{
name|ColumnSchemaModel
name|columnModel
init|=
operator|new
name|ColumnSchemaModel
argument_list|()
decl_stmt|;
name|columnModel
operator|.
name|setName
argument_list|(
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|ImmutableBytesWritable
argument_list|>
name|e
range|:
name|hcd
operator|.
name|getValues
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|columnModel
operator|.
name|addAttribute
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|model
operator|.
name|addColumnFamily
argument_list|(
name|columnModel
argument_list|)
expr_stmt|;
block|}
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
name|TableNotFoundException
name|e
parameter_list|)
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
name|TableSchemaModel
name|model
parameter_list|,
name|boolean
name|replace
parameter_list|,
name|UriInfo
name|uriInfo
parameter_list|)
block|{
comment|// NOTE: 'replace' is currently ignored... we always replace the schema
try|try
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|table
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
name|RESTServlet
name|server
init|=
name|RESTServlet
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|server
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|table
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|table
argument_list|)
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|server
operator|.
name|invalidateMaxAge
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|table
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
else|else
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
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
block|}
catch|catch
parameter_list|(
name|TableExistsException
name|e
parameter_list|)
block|{
comment|// race, someone else created a table with the same name
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
name|NOT_MODIFIED
argument_list|)
throw|;
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
name|TableSchemaModel
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
name|MIMETYPE_JAVASCRIPT
block|,
name|MIMETYPE_PROTOBUF
block|}
argument_list|)
specifier|public
name|Response
name|post
parameter_list|(
name|TableSchemaModel
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
literal|false
argument_list|,
name|uriInfo
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
try|try
block|{
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|RESTServlet
operator|.
name|getInstance
argument_list|()
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|table
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
name|TableNotFoundException
name|e
parameter_list|)
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
block|}
end_class

end_unit

