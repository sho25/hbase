begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ClusterStatus
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
name|HServerInfo
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
name|HServerLoad
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
name|StorageClusterStatusModel
import|;
end_import

begin_class
specifier|public
class|class
name|StorageClusterStatusResource
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
name|StorageClusterStatusResource
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|CacheControl
name|cacheControl
decl_stmt|;
specifier|private
name|RESTServlet
name|servlet
decl_stmt|;
specifier|public
name|StorageClusterStatusResource
parameter_list|()
throws|throws
name|IOException
block|{
name|servlet
operator|=
name|RESTServlet
operator|.
name|getInstance
argument_list|()
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
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|servlet
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|ClusterStatus
name|status
init|=
name|admin
operator|.
name|getClusterStatus
argument_list|()
decl_stmt|;
name|StorageClusterStatusModel
name|model
init|=
operator|new
name|StorageClusterStatusModel
argument_list|()
decl_stmt|;
name|model
operator|.
name|setRegions
argument_list|(
name|status
operator|.
name|getRegionsCount
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|.
name|setRequests
argument_list|(
name|status
operator|.
name|getRequestsCount
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|.
name|setAverageLoad
argument_list|(
name|status
operator|.
name|getAverageLoad
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HServerInfo
name|info
range|:
name|status
operator|.
name|getServerInfo
argument_list|()
control|)
block|{
name|HServerLoad
name|load
init|=
name|info
operator|.
name|getLoad
argument_list|()
decl_stmt|;
name|StorageClusterStatusModel
operator|.
name|Node
name|node
init|=
name|model
operator|.
name|addLiveNode
argument_list|(
name|info
operator|.
name|getServerAddress
argument_list|()
operator|.
name|getHostname
argument_list|()
operator|+
literal|":"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|info
operator|.
name|getServerAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|,
name|info
operator|.
name|getStartCode
argument_list|()
argument_list|,
name|load
operator|.
name|getUsedHeapMB
argument_list|()
argument_list|,
name|load
operator|.
name|getMaxHeapMB
argument_list|()
argument_list|)
decl_stmt|;
name|node
operator|.
name|setRequests
argument_list|(
name|load
operator|.
name|getNumberOfRequests
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HServerLoad
operator|.
name|RegionLoad
name|region
range|:
name|load
operator|.
name|getRegionsLoad
argument_list|()
control|)
block|{
name|node
operator|.
name|addRegion
argument_list|(
name|region
operator|.
name|getName
argument_list|()
argument_list|,
name|region
operator|.
name|getStores
argument_list|()
argument_list|,
name|region
operator|.
name|getStorefiles
argument_list|()
argument_list|,
name|region
operator|.
name|getStorefileSizeMB
argument_list|()
argument_list|,
name|region
operator|.
name|getMemStoreSizeMB
argument_list|()
argument_list|,
name|region
operator|.
name|getStorefileIndexSizeMB
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|String
name|name
range|:
name|status
operator|.
name|getDeadServerNames
argument_list|()
control|)
block|{
name|model
operator|.
name|addDeadNode
argument_list|(
name|name
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

