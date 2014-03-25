begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServlet
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletRequest
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletResponse
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
name|conf
operator|.
name|Configuration
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
name|protobuf
operator|.
name|RequestConverter
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
name|tmpl
operator|.
name|master
operator|.
name|MasterStatusTmpl
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
name|FSUtils
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
import|;
end_import

begin_comment
comment|/**  * The servlet responsible for rendering the index page of the  * master.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MasterStatusServlet
extends|extends
name|HttpServlet
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
name|MasterStatusServlet
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|doGet
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|IOException
block|{
name|HMaster
name|master
init|=
operator|(
name|HMaster
operator|)
name|getServletContext
argument_list|()
operator|.
name|getAttribute
argument_list|(
name|HMaster
operator|.
name|MASTER
argument_list|)
decl_stmt|;
assert|assert
name|master
operator|!=
literal|null
operator|:
literal|"No Master in context!"
assert|;
name|response
operator|.
name|setContentType
argument_list|(
literal|"text/html"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|master
operator|.
name|isOnline
argument_list|()
condition|)
block|{
name|response
operator|.
name|getWriter
argument_list|()
operator|.
name|write
argument_list|(
literal|"The Master is initializing!"
argument_list|)
expr_stmt|;
name|response
operator|.
name|getWriter
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
return|return;
block|}
name|Configuration
name|conf
init|=
name|master
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|frags
init|=
name|getFragmentationInfo
argument_list|(
name|master
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|ServerName
name|metaLocation
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
init|=
literal|null
decl_stmt|;
name|Set
argument_list|<
name|ServerName
argument_list|>
name|deadServers
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|master
operator|.
name|isActiveMaster
argument_list|()
condition|)
block|{
if|if
condition|(
name|master
operator|.
name|getServerManager
argument_list|()
operator|==
literal|null
condition|)
block|{
name|response
operator|.
name|sendError
argument_list|(
literal|503
argument_list|,
literal|"Master not ready"
argument_list|)
expr_stmt|;
return|return;
block|}
name|metaLocation
operator|=
name|getMetaLocationOrNull
argument_list|(
name|master
argument_list|)
expr_stmt|;
comment|//ServerName metaLocation = master.getCatalogTracker().getMetaLocation();
name|servers
operator|=
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
expr_stmt|;
name|deadServers
operator|=
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getDeadServers
argument_list|()
operator|.
name|copyServerNames
argument_list|()
expr_stmt|;
block|}
name|MasterStatusTmpl
name|tmpl
decl_stmt|;
try|try
block|{
name|tmpl
operator|=
operator|new
name|MasterStatusTmpl
argument_list|()
operator|.
name|setFrags
argument_list|(
name|frags
argument_list|)
operator|.
name|setMetaLocation
argument_list|(
name|metaLocation
argument_list|)
operator|.
name|setServers
argument_list|(
name|servers
argument_list|)
operator|.
name|setDeadServers
argument_list|(
name|deadServers
argument_list|)
operator|.
name|setCatalogJanitorEnabled
argument_list|(
name|master
operator|.
name|getMasterRpcServices
argument_list|()
operator|.
name|isCatalogJanitorEnabled
argument_list|(
literal|null
argument_list|,
name|RequestConverter
operator|.
name|buildIsCatalogJanitorEnabledRequest
argument_list|()
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|s
parameter_list|)
block|{
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|s
argument_list|)
throw|;
block|}
if|if
condition|(
name|request
operator|.
name|getParameter
argument_list|(
literal|"filter"
argument_list|)
operator|!=
literal|null
condition|)
name|tmpl
operator|.
name|setFilter
argument_list|(
name|request
operator|.
name|getParameter
argument_list|(
literal|"filter"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|getParameter
argument_list|(
literal|"format"
argument_list|)
operator|!=
literal|null
condition|)
name|tmpl
operator|.
name|setFormat
argument_list|(
name|request
operator|.
name|getParameter
argument_list|(
literal|"format"
argument_list|)
argument_list|)
expr_stmt|;
name|tmpl
operator|.
name|render
argument_list|(
name|response
operator|.
name|getWriter
argument_list|()
argument_list|,
name|master
argument_list|,
name|admin
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ServerName
name|getMetaLocationOrNull
parameter_list|(
name|HMaster
name|master
parameter_list|)
block|{
try|try
block|{
return|return
operator|(
name|master
operator|.
name|getCatalogTracker
argument_list|()
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|master
operator|.
name|getCatalogTracker
argument_list|()
operator|.
name|getMetaLocation
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to get meta location"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|getFragmentationInfo
parameter_list|(
name|HMaster
name|master
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|showFragmentation
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.master.ui.fragmentation.enabled"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|showFragmentation
condition|)
block|{
return|return
name|FSUtils
operator|.
name|getTableFragmentation
argument_list|(
name|master
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

