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
name|HRegionInfo
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
name|ServerName
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
name|MetaScanner
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
name|TableInfoModel
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
name|TableRegionModel
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionsResource
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
name|RegionsResource
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
name|RegionsResource
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
try|try
block|{
name|TableName
name|tableName
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
name|TableInfoModel
name|model
init|=
operator|new
name|TableInfoModel
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regions
init|=
name|MetaScanner
operator|.
name|allTableRegions
argument_list|(
name|servlet
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|null
argument_list|,
name|tableName
argument_list|,
literal|false
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|e
range|:
name|regions
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HRegionInfo
name|hri
init|=
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|ServerName
name|addr
init|=
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|model
operator|.
name|add
argument_list|(
operator|new
name|TableRegionModel
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|hri
operator|.
name|getRegionId
argument_list|()
argument_list|,
name|hri
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|hri
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|addr
operator|.
name|getHostAndPort
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
catch|catch
parameter_list|(
name|TableNotFoundException
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
catch|catch
parameter_list|(
name|IOException
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
block|}
block|}
end_class

end_unit

