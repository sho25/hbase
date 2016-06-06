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
name|servlet
operator|.
name|ServletContext
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
name|rest
operator|.
name|model
operator|.
name|NamespacesModel
import|;
end_import

begin_comment
comment|/**  * Implements REST GET list of all namespaces.  *<p>  *<tt>/namespaces</tt>  *<p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|NamespacesResource
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
name|NamespacesResource
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Constructor    * @throws IOException    */
specifier|public
name|NamespacesResource
parameter_list|()
throws|throws
name|IOException
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Build a response for a list of all namespaces request.    * @param context servlet context    * @param uriInfo (JAX-RS context variable) request URL    * @return a response for a version request    */
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
name|ServletContext
name|context
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
name|NamespacesModel
name|rowModel
init|=
literal|null
decl_stmt|;
name|rowModel
operator|=
operator|new
name|NamespacesModel
argument_list|(
name|servlet
operator|.
name|getAdmin
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
name|Response
operator|.
name|ok
argument_list|(
name|rowModel
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
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot retrieve list of namespaces."
argument_list|)
throw|;
block|}
block|}
comment|/**    * Dispatch to NamespaceInstanceResource    */
annotation|@
name|Path
argument_list|(
literal|"{namespace}"
argument_list|)
specifier|public
name|NamespacesInstanceResource
name|getNamespaceInstanceResource
parameter_list|(
specifier|final
annotation|@
name|PathParam
argument_list|(
literal|"namespace"
argument_list|)
name|String
name|namespace
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|NamespacesInstanceResource
argument_list|(
name|namespace
argument_list|)
return|;
block|}
block|}
end_class

end_unit

