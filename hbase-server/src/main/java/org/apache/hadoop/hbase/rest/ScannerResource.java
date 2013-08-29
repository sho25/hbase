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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|UriBuilder
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
name|rest
operator|.
name|model
operator|.
name|ScannerModel
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ScannerResource
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
name|ScannerResource
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ScannerInstanceResource
argument_list|>
name|scanners
init|=
name|Collections
operator|.
name|synchronizedMap
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ScannerInstanceResource
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
name|TableResource
name|tableResource
decl_stmt|;
comment|/**    * Constructor    * @param tableResource    * @throws IOException    */
specifier|public
name|ScannerResource
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
specifier|static
name|boolean
name|delete
parameter_list|(
specifier|final
name|String
name|id
parameter_list|)
block|{
name|ScannerInstanceResource
name|instance
init|=
name|scanners
operator|.
name|remove
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|instance
operator|!=
literal|null
condition|)
block|{
name|instance
operator|.
name|generator
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
name|Response
name|update
parameter_list|(
specifier|final
name|ScannerModel
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
name|byte
index|[]
name|endRow
init|=
name|model
operator|.
name|hasEndRow
argument_list|()
condition|?
name|model
operator|.
name|getEndRow
argument_list|()
else|:
literal|null
decl_stmt|;
name|RowSpec
name|spec
init|=
operator|new
name|RowSpec
argument_list|(
name|model
operator|.
name|getStartRow
argument_list|()
argument_list|,
name|endRow
argument_list|,
name|model
operator|.
name|getColumns
argument_list|()
argument_list|,
name|model
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|model
operator|.
name|getEndTime
argument_list|()
argument_list|,
name|model
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Filter
name|filter
init|=
name|ScannerResultGenerator
operator|.
name|buildFilterFromModel
argument_list|(
name|model
argument_list|)
decl_stmt|;
name|String
name|tableName
init|=
name|tableResource
operator|.
name|getName
argument_list|()
decl_stmt|;
name|ScannerResultGenerator
name|gen
init|=
operator|new
name|ScannerResultGenerator
argument_list|(
name|tableName
argument_list|,
name|spec
argument_list|,
name|filter
argument_list|,
name|model
operator|.
name|getCaching
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|id
init|=
name|gen
operator|.
name|getID
argument_list|()
decl_stmt|;
name|ScannerInstanceResource
name|instance
init|=
operator|new
name|ScannerInstanceResource
argument_list|(
name|tableName
argument_list|,
name|id
argument_list|,
name|gen
argument_list|,
name|model
operator|.
name|getBatch
argument_list|()
argument_list|)
decl_stmt|;
name|scanners
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|instance
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
literal|"new scanner: "
operator|+
name|id
argument_list|)
expr_stmt|;
block|}
name|UriBuilder
name|builder
init|=
name|uriInfo
operator|.
name|getAbsolutePathBuilder
argument_list|()
decl_stmt|;
name|URI
name|uri
init|=
name|builder
operator|.
name|path
argument_list|(
name|id
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
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
name|created
argument_list|(
name|uri
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
if|if
condition|(
name|e
operator|instanceof
name|TableNotFoundException
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
elseif|else
if|if
condition|(
name|e
operator|instanceof
name|RuntimeException
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
name|BAD_REQUEST
argument_list|)
operator|.
name|type
argument_list|(
name|MIMETYPE_TEXT
argument_list|)
operator|.
name|entity
argument_list|(
literal|"Bad request"
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
name|ScannerModel
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
name|ScannerModel
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
argument_list|,
name|uriInfo
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"{scanner: .+}"
argument_list|)
specifier|public
name|ScannerInstanceResource
name|getScannerInstanceResource
parameter_list|(
specifier|final
annotation|@
name|PathParam
argument_list|(
literal|"scanner"
argument_list|)
name|String
name|id
parameter_list|)
throws|throws
name|IOException
block|{
name|ScannerInstanceResource
name|instance
init|=
name|scanners
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|instance
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
operator|new
name|ScannerInstanceResource
argument_list|()
return|;
block|}
else|else
block|{
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
block|}
return|return
name|instance
return|;
block|}
block|}
end_class

end_unit

