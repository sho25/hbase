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
name|stargate
operator|.
name|model
operator|.
name|TableListModel
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
name|TableModel
import|;
end_import

begin_class
annotation|@
name|Path
argument_list|(
literal|"/"
argument_list|)
specifier|public
class|class
name|TableResource
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
name|TableResource
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|CacheControl
name|cacheControl
decl_stmt|;
specifier|public
name|TableResource
parameter_list|()
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
specifier|private
name|HTableDescriptor
index|[]
name|getTableList
parameter_list|()
throws|throws
name|IOException
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
name|HTableDescriptor
index|[]
name|list
init|=
name|admin
operator|.
name|listTables
argument_list|()
decl_stmt|;
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
literal|"getTableList:"
argument_list|)
expr_stmt|;
for|for
control|(
name|HTableDescriptor
name|htd
range|:
name|list
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|htd
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|list
return|;
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
name|TableListModel
name|tableList
init|=
operator|new
name|TableListModel
argument_list|()
decl_stmt|;
for|for
control|(
name|HTableDescriptor
name|htd
range|:
name|getTableList
argument_list|()
control|)
block|{
if|if
condition|(
name|htd
operator|.
name|isMetaRegion
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|tableList
operator|.
name|add
argument_list|(
operator|new
name|TableModel
argument_list|(
name|htd
operator|.
name|getNameAsString
argument_list|()
argument_list|)
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
name|tableList
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
name|Path
argument_list|(
literal|"{table}/scanner"
argument_list|)
specifier|public
name|ScannerResource
name|getScannerResource
parameter_list|(
annotation|@
name|PathParam
argument_list|(
literal|"table"
argument_list|)
name|String
name|table
parameter_list|)
block|{
return|return
operator|new
name|ScannerResource
argument_list|(
name|table
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"{table}/schema"
argument_list|)
specifier|public
name|SchemaResource
name|getSchemaResource
parameter_list|(
annotation|@
name|PathParam
argument_list|(
literal|"table"
argument_list|)
name|String
name|table
parameter_list|)
block|{
return|return
operator|new
name|SchemaResource
argument_list|(
name|table
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"{table}/{rowspec: .+}"
argument_list|)
specifier|public
name|RowResource
name|getRowResource
parameter_list|(
annotation|@
name|PathParam
argument_list|(
literal|"table"
argument_list|)
name|String
name|table
parameter_list|,
annotation|@
name|PathParam
argument_list|(
literal|"rowspec"
argument_list|)
name|String
name|rowspec
parameter_list|,
annotation|@
name|QueryParam
argument_list|(
literal|"v"
argument_list|)
name|String
name|versions
parameter_list|)
block|{
try|try
block|{
return|return
operator|new
name|RowResource
argument_list|(
name|table
argument_list|,
name|rowspec
argument_list|,
name|versions
argument_list|)
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
name|INTERNAL_SERVER_ERROR
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

